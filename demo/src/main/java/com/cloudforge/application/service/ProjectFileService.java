package com.cloudforge.application.service;

import com.cloudforge.application.dto.ProjectFileRequest;
import com.cloudforge.application.dto.ProjectFileResponse;
import com.cloudforge.application.entity.Project;
import com.cloudforge.application.entity.ProjectFile;
import com.cloudforge.application.exception.FileConflictException;
import com.cloudforge.application.exception.ResourceNotFoundException;
import com.cloudforge.application.repository.ProjectFileRepository;
import com.cloudforge.application.repository.ProjectRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectFileService {

    private final ProjectRepository projectRepository;
    private final ProjectFileRepository projectFileRepository;
    private final ProjectService projectService;

    public ProjectFileService(ProjectFileRepository projectFileRepository,
                              ProjectRepository projectRepository,
                              ProjectService projectService) {

        this.projectFileRepository = projectFileRepository;
        this.projectRepository = projectRepository;
        this.projectService = projectService;
    }


    public ProjectFileResponse createFile(
            Long projectId,
            String email,
            ProjectFileRequest request) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Project not found"));

        projectService.checkCanModifyFiles(project, email);

        ProjectFile file = new ProjectFile();

        file.setFileName(request.getFileName());
        file.setFilePath(request.getFilePath());
        file.setContent(request.getContent());
        file.setProject(project);

        ProjectFile savedFile = projectFileRepository.save(file);

        return toResponse(savedFile);
    }


    public List<ProjectFileResponse> getProjectFiles(
            Long projectId,
            String email) {

        projectService.getProjectForUser(projectId, email);

        return projectFileRepository.findByProjectId(projectId)
                .stream()
                .map(this::toResponse)
                .toList();
    }


    public ProjectFileResponse getFile(
            Long projectId,
            Long fileId,
            String email) {

        projectService.getProjectForUser(projectId, email);

        ProjectFile file = projectFileRepository
                .findByIdAndProjectId(fileId, projectId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("File not found"));

        return toResponse(file);
    }


    public ProjectFileResponse updateFile(
            Long projectId,
            Long fileId,
            String email,
            ProjectFileRequest request) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Project not found"));

        projectService.checkCanModifyFiles(project, email);

        ProjectFile file = projectFileRepository
                .findByIdAndProjectId(fileId, projectId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("File not found"));

        if (!file.getVersion().equals(request.getVersion())) {
            throw new FileConflictException(
                    "File was modified by another user. Please refresh and try again."
            );
        }

        file.setFileName(request.getFileName());
        file.setFilePath(request.getFilePath());
        file.setContent(request.getContent());

        ProjectFile savedFile = projectFileRepository.save(file);

        return toResponse(savedFile);
    }


    public void deleteFile(
            Long projectId,
            Long fileId,
            String email) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Project not found"));

        projectService.checkCanModifyFiles(project, email);

        ProjectFile file = projectFileRepository
                .findByIdAndProjectId(fileId, projectId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("File not found"));

        projectFileRepository.delete(file);
    }


    private ProjectFileResponse toResponse(ProjectFile file) {

        ProjectFileResponse response = new ProjectFileResponse();

        response.setId(file.getId());
        response.setFileName(file.getFileName());
        response.setFilePath(file.getFilePath());
        response.setContent(file.getContent());
        response.setVersion(file.getVersion());
        response.setCreatedAt(file.getCreatedAt());
        response.setUpdatedAt(file.getUpdatedAt());

        response.setProjectId(file.getProject().getId());

        return response;
    }
}

