package com.cloudforge.application.Controller;

import com.cloudforge.application.dto.LoginRequest;
import com.cloudforge.application.dto.RegisterRequest;
import com.cloudforge.application.dto.UserResponse;
import com.cloudforge.application.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {
    private final AuthService authService;

    AuthController(AuthService authService){
        this.authService = authService;
    }
    @PostMapping("/api/auth/login")
    public String login(@Valid @RequestBody LoginRequest request) {

        return authService.login(request);
    }

    @PostMapping("/api/auth/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(
           @Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }
}
