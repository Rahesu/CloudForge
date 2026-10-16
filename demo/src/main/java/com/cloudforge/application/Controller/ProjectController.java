package com.cloudforge.application.Controller;

import com.cloudforge.application.dto.ProjectMemberRequest;
import com.cloudforge.application.dto.ProjectPageResponse;
import com.cloudforge.application.dto.ProjectRequest;
import com.cloudforge.application.dto.ProjectResponse;
import com.cloudforge.application.entity.Project;
import com.cloudforge.application.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController (ProjectService projectService){
        this.projectService = projectService;

    }

    // create project
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectResponse create(@Valid @RequestBody ProjectRequest request, Authentication authentication){

        Project project=new Project();

        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setStatus(request.getStatus());

        String email=authentication.getName();

        return projectService.createProject(project,email);
    }

    // get my projects
    @GetMapping
    public ProjectPageResponse getMyProjects(
            Authentication authentication,
            @RequestParam(required = false,defaultValue = "")String search,
            Pageable pageable){
        String email=authentication.getName();

        return projectService.getMyProjects(email,search,pageable);
    }

    @GetMapping("/{id}")
    public ProjectResponse getProjectById(@PathVariable Long id, Authentication authentication){
        String email=authentication.getName();

        return projectService.getProjectById(id,email);
    }

    // update project
    @PutMapping("/{id}")
    public ProjectResponse updateProject(@PathVariable Long id,@Valid @RequestBody ProjectRequest request,Authentication authentication){
        String email=authentication.getName();
        return projectService.updateProjectById(id,email,request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProject(@PathVariable Long id,Authentication authentication) {
       String email=authentication.getName();

        projectService.deleteProject(id,email);

    }

    // add member
    @PostMapping("/{projectId}/members")
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectResponse addMember(
                @PathVariable Long projectId,
                @Valid @RequestBody ProjectMemberRequest request,
                Authentication authentication) {

        String email = authentication.getName();

        return projectService.addMember(
                projectId,
                request.getUserId(),
                request.getRole(),
                email
        );
    }

    // remove member
    @DeleteMapping("/{projectId}/members/{userId}")
    public ProjectResponse removeMember(
                @PathVariable Long projectId,
                @PathVariable Long userId,
                Authentication authentication) {

        String email = authentication.getName();

        return projectService.removeMember(
                projectId,
                userId,
                email
        );

    }

    @PutMapping("/{projectId}/members/{userId}")
    public ProjectResponse changeMemberRole(
            @PathVariable Long projectId,
            @PathVariable Long userId,
            @Valid @RequestBody ProjectMemberRequest request,
            Authentication authentication) {

        String email = authentication.getName();

        return projectService.changeMemberRole(
                projectId,
                userId,
                request.getRole(),
                email
        );
    }

}
