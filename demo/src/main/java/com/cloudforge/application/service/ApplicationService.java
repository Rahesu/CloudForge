package com.cloudforge.application.service;

import com.cloudforge.application.repository.ApplicationRepository;
import com.cloudforge.application.entity.Application;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ApplicationService {

    private final ApplicationRepository repository;


    public ApplicationService(ApplicationRepository repository) {
        this.repository = repository;
    }

    public Application create(Application application){

       return repository.save(application);

    }

    public List<Application> getAll(){
        return repository.findAll();
    }

    public Application getById(Long id){

        Application application =repository.findById(id)
                .orElseThrow(() -> new RuntimeException("not found"));

        return application;
    }

    public Application delete(Long id){
        Application application = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("not found"));

        repository.deleteById(id);
        return application;
    }
}

