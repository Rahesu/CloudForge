package com.cloudforge.application.Controller;

import com.cloudforge.application.dto.ProjectFileRequest;
import com.cloudforge.application.dto.ProjectFileResponse;
import com.cloudforge.application.service.ProjectFileService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/files")
public class ProjectFileController {

    private final ProjectFileService projectFileService;
    public ProjectFileController(ProjectFileService projectFileService) {
        this.projectFileService = projectFileService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectFileResponse createFile(@PathVariable Long projectId,
                                  @Valid @RequestBody ProjectFileRequest request,
                                  Authentication authentication){

       String email = authentication.getName();

        return projectFileService.createFile(projectId,email,request);

    }

    @GetMapping
    public List<ProjectFileResponse> getProjectFiles(@PathVariable Long projectId,
                                                     Authentication authentication){
        String email=authentication.getName();

        return projectFileService.getProjectFiles(projectId,email);
    }

    @GetMapping("/{fileId}")
    public ProjectFileResponse getFile(@PathVariable Long projectId,
                               @PathVariable Long fileId,
                               Authentication authentication){

        String email= authentication.getName();

        return projectFileService.getFile(projectId,fileId,email);
    }

    @PutMapping("/{fileId}")
    public ProjectFileResponse updateFile(@PathVariable Long projectId,@PathVariable Long fileId,
                                  @Valid @RequestBody ProjectFileRequest request,
                                  Authentication authentication){

        String email = authentication.getName();

        return projectFileService.updateFile(projectId,fileId,email,request);

    }

    @DeleteMapping("/{fileId}")
    @ResponseStatus(HttpStatus.CREATED)
    public void deleteFile(@PathVariable Long projectId,
                             @PathVariable Long fileId,
                             Authentication authentication) {

        String email = authentication.getName();

        projectFileService.deleteFile(
                projectId,
                fileId,
                email
        );


    }

}
