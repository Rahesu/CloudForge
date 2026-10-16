package com.cloudforge.application.service;

import com.cloudforge.application.Security.JwtService;
import com.cloudforge.application.dto.LoginRequest;
import com.cloudforge.application.dto.UserResponse;
import com.cloudforge.application.exception.DuplicateResourceException;
import com.cloudforge.application.kafka.ActivityEventProducer;
import com.cloudforge.application.repository.UserRepository;
import com.cloudforge.application.dto.RegisterRequest;
import com.cloudforge.application.entity.User;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final ActivityEventProducer activityEventProducer;


   public AuthService(PasswordEncoder passwordEncoder, UserRepository userRepository, AuthenticationManager authenticationManager, JwtService jwtService, ActivityEventProducer activityEventProducer){
        this.passwordEncoder =passwordEncoder;
        this.userRepository =userRepository;
        this.authenticationManager=authenticationManager;
        this.jwtService = jwtService;
       this.activityEventProducer = activityEventProducer;
   }

    public UserResponse register(RegisterRequest request){

       if(userRepository.existsByEmail( request.getEmail() )){
            throw new DuplicateResourceException("Email is already registered");
       }

        User user=new User();
                    user.setEmail(request.getEmail());
                    user.setPassword(passwordEncoder.encode(request.getPassword()));
                    user.setUsername(request.getUsername());
                    user.setRole("User");

        userRepository.save(user);

        UserResponse response = new UserResponse();

        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());

        return response;
    }

    public String login(LoginRequest request){
         String email= request.getEmail();
         String password = request.getPassword();


        Authentication authenticated =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                email,
                                password
                        )
                );


        userRepository.findByEmail(authenticated.getName())
                .ifPresent(user ->
                        activityEventProducer.publishLogin(
                                user.getId(),
                                user.getEmail()
                        )
                );

        return jwtService.generateToken(request.getEmail());
    }

}
