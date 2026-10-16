package com.cloudforge.application.service;

import com.cloudforge.application.dto.ProjectMemberResponse;
import com.cloudforge.application.dto.ProjectPageResponse;
import com.cloudforge.application.dto.ProjectRequest;
import com.cloudforge.application.dto.ProjectResponse;
import com.cloudforge.application.entity.Project;
import com.cloudforge.application.entity.ProjectMember;
import com.cloudforge.application.entity.ProjectRole;
import com.cloudforge.application.entity.User;
import com.cloudforge.application.event.ProjectCreatedEvent;
import com.cloudforge.application.exception.ResourceAccessDeniedException;
import com.cloudforge.application.exception.ResourceNotFoundException;
import com.cloudforge.application.kafka.ActivityEventProducer;
import com.cloudforge.application.repository.ProjectMemberRepository;
import com.cloudforge.application.repository.ProjectRepository;
import com.cloudforge.application.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final ActivityEventProducer activityEventProducer;

    public ProjectService(
            ProjectRepository projectRepository,
            UserRepository userRepository,
            ProjectMemberRepository projectMemberRepository, ActivityEventProducer activityEventProducer) {

        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.activityEventProducer = activityEventProducer;
    }


    public ProjectResponse createProject(
            Project project,
            String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        project.setOwner(user);

        Project savedProject =
                projectRepository.save(project);

        activityEventProducer.publishProjectCreated(
                savedProject.getId(),
                savedProject.getName(),
                project.getOwner().getId(),
                project.getOwner().getEmail()
        );
        return toResponse(savedProject);
    }


    public ProjectPageResponse getMyProjects(
            String email,
            String search,
            Pageable pageable) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        Page<Project> projectPage;

        if (search == null || search.isBlank()) {

            projectPage =
                    projectRepository.findByOwnerId(
                            user.getId(),
                            pageable
                    );

        } else {

            projectPage =
                    projectRepository.searchProjects(
                            user.getId(),
                            search,
                            pageable
                    );
        }

        List<ProjectResponse> projects =
                projectPage.getContent()
                        .stream()
                        .map(this::toResponse)
                        .toList();

        ProjectPageResponse response =
                new ProjectPageResponse();

        response.setContent(projects);
        response.setPage(projectPage.getNumber());
        response.setSize(projectPage.getSize());
        response.setTotalElements(
                projectPage.getTotalElements()
        );
        response.setTotalPages(
                projectPage.getTotalPages()
        );

        return response;
    }


    public ProjectResponse getProjectById(
            Long id,
            String email) {

        Project project =
                projectRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Project not found"
                                ));

        getProjectRole(project, email);

        return toResponse(project);
    }


    public ProjectResponse updateProjectById(
            Long id,
            String email,
            ProjectRequest updatedProject) {

        Project project =
                projectRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Project not found"
                                ));

        checkOwner(project, email);

        project.setName(
                updatedProject.getName()
        );

        project.setDescription(
                updatedProject.getDescription()
        );

        project.setStatus(
                updatedProject.getStatus()
        );

        Project savedProject =
                projectRepository.save(project);

        return toResponse(savedProject);
    }


    public void deleteProject(
            Long id,
            String email) {

        Project project =
                projectRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Project not found"
                                ));

        checkOwner(project, email);

        projectRepository.delete(project);
    }


    @Transactional
    public ProjectResponse addMember(
            Long projectId,
            Long userId,
            ProjectRole role,
            String ownerEmail) {

        Project project =
                projectRepository.findById(projectId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Project not found"
                                ));

        checkOwner(project, ownerEmail);

        User user =
                userRepository.findById(userId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "User not found"
                                ));

        if (project.getOwner()
                .getId()
                .equals(userId)) {

            throw new ResourceAccessDeniedException(
                    "Project owner is already the owner"
            );
        }

        if (projectMemberRepository
                .existsByProjectIdAndUserId(
                        projectId,
                        userId)) {

            throw new ResourceAccessDeniedException(
                    "User is already a member"
            );
        }

        ProjectMember projectMember =
                new ProjectMember();

        projectMember.setProject(project);
        projectMember.setUser(user);
        projectMember.setRole(role);

        projectMemberRepository.save(projectMember);

        return toResponse(project);
    }


    @Transactional
    public ProjectResponse removeMember(
            Long projectId,
            Long userId,
            String ownerEmail) {

        Project project =
                projectRepository.findById(projectId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Project not found"
                                ));

        checkOwner(project, ownerEmail);

        ProjectMember member =
                projectMemberRepository
                        .findByProjectIdAndUserId(
                                projectId,
                                userId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Member not found"
                                ));

        project.getMembers().remove(member);

        projectMemberRepository.delete(member);

        return toResponse(project);
    }


    @Transactional
    public ProjectResponse changeMemberRole(
            Long projectId,
            Long userId,
            ProjectRole newRole,
            String ownerEmail) {

        Project project =
                projectRepository.findById(projectId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Project not found"
                                ));

        checkOwner(project, ownerEmail);

        ProjectMember member =
                projectMemberRepository
                        .findByProjectIdAndUserId(
                                projectId,
                                userId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Member not found"
                                ));

        member.setRole(newRole);

        projectMemberRepository.save(member);

        return toResponse(project);
    }


    public ProjectRole getProjectRole(
            Project project,
            String email) {

        if (project.getOwner()
                .getEmail()
                .equals(email)) {

            return ProjectRole.OWNER;
        }

        return project.getMembers()
                .stream()
                .filter(member ->
                        member.getUser()
                                .getEmail()
                                .equals(email)
                )
                .map(ProjectMember::getRole)
                .findFirst()
                .orElseThrow(() ->
                        new ResourceAccessDeniedException(
                                "Access denied"
                        ));
    }


    public void checkCanModifyFiles(
            Project project,
            String email) {

        ProjectRole role =
                getProjectRole(project, email);

        if (role != ProjectRole.OWNER &&
                role != ProjectRole.DEVELOPER) {

            throw new ResourceAccessDeniedException(
                    "You do not have permission to modify files"
            );
        }
    }


    public void checkOwner(Project project, String email) {

        if (!project.getOwner()
                .getEmail()
                .equals(email)) {

            throw new ResourceAccessDeniedException(
                    "Only the project owner can perform this action"
            );
        }
    }


    public Project getProjectForUser(
            Long projectId,
            String email) {

        Project project =
                projectRepository.findById(projectId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Project not found"
                                ));

        getProjectRole(project, email);

        return project;
    }


    private ProjectResponse toResponse(
            Project project) {

        ProjectResponse response =
                new ProjectResponse();

        response.setId(project.getId());
        response.setName(project.getName());
        response.setDescription(
                project.getDescription()
        );
        response.setStatus(project.getStatus());
        response.setCreatedAt(
                project.getCreatedAt()
        );
        response.setUpdatedAt(
                project.getUpdatedAt()
        );

        response.setOwnerId(
                project.getOwner().getId()
        );

        response.setOwnerEmail(
                project.getOwner().getEmail()
        );


        List<Long> memberIds =
                project.getMembers()
                        .stream()
                        .map(member ->
                                member.getUser().getId()
                        )
                        .toList();

        response.setMemberIds(memberIds);


        List<ProjectMemberResponse> members =
                project.getMembers()
                        .stream()
                        .map(member -> {

                            ProjectMemberResponse memberResponse =
                                    new ProjectMemberResponse();

                            memberResponse.setUserId(
                                    member.getUser().getId()
                            );

                            memberResponse.setEmail(
                                    member.getUser().getEmail()
                            );

                            memberResponse.setRole(
                                    member.getRole()
                            );

                            return memberResponse;
                        })
                        .toList();

        response.setMembers(members);

        return response;
    }
}