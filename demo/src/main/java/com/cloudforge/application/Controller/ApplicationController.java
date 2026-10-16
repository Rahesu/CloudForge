package com.cloudforge.application.Controller;

import com.cloudforge.application.entity.Application;
import com.cloudforge.application.service.ApplicationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    private final ApplicationService service;

   public ApplicationController(ApplicationService service) {
       this.service=service;
   }

   @PostMapping
   public Application create(@Valid @RequestBody Application application){
       return service.create(application);
   }

   @GetMapping
   public List<Application> getAll(){
       return service.getAll();
   }

   @GetMapping("{id}")
   public Application getById(@PathVariable Long id){
       return service.getById(id);

   }

   @DeleteMapping("{id}")
   public Application delete(@PathVariable Long id){
       return service.delete(id);
   }

}
