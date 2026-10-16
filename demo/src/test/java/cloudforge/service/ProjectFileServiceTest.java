package cloudforge.service;


import com.cloudforge.application.dto.ProjectFileRequest;
import com.cloudforge.application.dto.ProjectFileResponse;
import com.cloudforge.application.dto.ProjectRequest;
import com.cloudforge.application.entity.Project;
import com.cloudforge.application.entity.ProjectFile;
import com.cloudforge.application.exception.FileConflictException;
import com.cloudforge.application.exception.ResourceAccessDeniedException;
import com.cloudforge.application.exception.ResourceNotFoundException;
import com.cloudforge.application.repository.ProjectFileRepository;
import com.cloudforge.application.repository.ProjectRepository;
import com.cloudforge.application.service.ProjectFileService;
import com.cloudforge.application.service.ProjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProjectFileServiceTest {

    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private ProjectFileRepository projectFileRepository;
    @Mock
    private ProjectService projectService;
    @InjectMocks
    private ProjectFileService projectFileService;

    private Project project;
    private ProjectFile file;
    private ProjectFileRequest request;

    @BeforeEach
    void setUp() {

        project = new Project();
        project.setId(1L);
        project.setName("CloudForge");
        project.setDescription("Cloud development platform");
        project.setStatus("Active");

        request = new ProjectFileRequest();
        request.setFileName("test.java");
        request.setFilePath("/src/test.java");
        request.setContent("public class Test {}");
        request.setVersion(1L);

        file = new ProjectFile();
        file.setId(1L);
        file.setFileName("test.java");
        file.setFilePath("/src/test.java");
        file.setContent("public class Test {}");
        file.setVersion(1L);
        file.setProject(project);
    }

    @Test
    void createFile_shouldCreateSuccessfully() {

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        doNothing().when(projectService).checkCanModifyFiles(project, "owner@test.com");

        when(projectFileRepository.save(any(ProjectFile.class))).thenReturn(file);

        ProjectFileResponse response = projectFileService.createFile(
                1L,
                "owner@test.com",
                request
        );

        assertEquals(file.getFileName(), response.getFileName());
        assertEquals(file.getFilePath(), response.getFilePath());
        assertEquals(file.getContent(), response.getContent());

        verify(projectFileRepository).save(any(ProjectFile.class));

    }

    @Test
    void createFile_projectNotFound_shouldThrowException() {

        when(projectRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> projectFileService.createFile(
                        1L,
                        "owner@test.com",
                        request
                ));

        verify(projectFileRepository, never()).save(any(ProjectFile.class));
    }

    @Test
    void createFile_byUser_shouldThrowException() {

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        doThrow(new ResourceAccessDeniedException("You do not have permission to modify files"))
                .when(projectService)
                .checkCanModifyFiles(project, "user@test.com");

        assertThrows(ResourceAccessDeniedException.class,
                () -> projectFileService.createFile(
                        1L,
                        "user@test.com",
                        request
                ));

        verify(projectFileRepository, never()).save(any(ProjectFile.class));

    }

    @Test
    void getProjectFiles_shouldSuccessfullyReturnUserFiles() {

        when(projectService.getProjectForUser(1L, "user@test.com"))
                .thenReturn(project);

        when(projectFileRepository.findByProjectId(1L))
                .thenReturn(List.of(file));

        List<ProjectFileResponse> response = projectFileService.getProjectFiles(
                1L,
                "user@test.com"
        );

        assertEquals(1, response.size());
        assertEquals(file.getFileName(), response.get(0).getFileName());
        assertEquals(file.getFilePath(), response.get(0).getFilePath());
        assertEquals(file.getContent(), response.get(0).getContent());

        verify(projectService).getProjectForUser(1L, "user@test.com");

        verify(projectFileRepository).findByProjectId(1L);
    }

    @Test
    void getProjectFiles_unauthorizedUser_shouldThrowException() {

        doThrow(new ResourceAccessDeniedException("Access denied"))
                .when(projectService)
                .getProjectForUser(1L, "user@test.com");

        assertThrows(ResourceAccessDeniedException.class,
                () -> projectFileService.getProjectFiles(
                        1L,
                        "user@test.com"
                ));

        verify(projectFileRepository, never()).findByProjectId(1L);
    }

    @Test
    void getProjectFiles_noFiles_shouldReturnEmptyList() {

        when(projectService.getProjectForUser(1L, "user@test.com"))
                .thenReturn(project);

        when(projectFileRepository.findByProjectId(1L))
                .thenReturn(List.of());

        List<ProjectFileResponse> response =
                projectFileService.getProjectFiles(
                        1L,
                        "user@test.com"
                );

        assertEquals(0, response.size());

        verify(projectService).getProjectForUser(1L, "user@test.com");

        verify(projectFileRepository).findByProjectId(1L);
    }

    @Test
    void getFile_shouldReturnFiles() {

        when(projectService.getProjectForUser(1L, "user@test.com"))
                .thenReturn(project);

        when(projectFileRepository.findByIdAndProjectId(2L, 1L))
                .thenReturn(Optional.of(file));

        ProjectFileResponse response = projectFileService.getFile(
                1L, 2L,
                "user@test.com"
        );

        assertEquals(file.getFileName(), response.getFileName());
        assertEquals(file.getFilePath(), response.getFilePath());
        assertEquals(file.getContent(), response.getContent());


        verify(projectService).getProjectForUser(1L, "user@test.com");

        verify(projectFileRepository).findByIdAndProjectId(2L, 1L);

    }

    @Test
    void getFile_NotFound_shouldThrowException() {

        when(projectService.getProjectForUser(1L, "user@test.com"))
                .thenReturn(project);

        when(projectFileRepository.findByIdAndProjectId(2L, 1L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> projectFileService.getFile(1L, 2L, "user@test.com"));

        verify(projectService).getProjectForUser(1L, "user@test.com");

        verify(projectFileRepository).findByIdAndProjectId(2L, 1L);


    }


    @Test
    void updateFile_ShouldSuccessfullyUpdateProject(){

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        when(projectFileRepository.findByIdAndProjectId(2L,1L))
                .thenReturn(Optional.of(file));

        when(projectFileRepository.save(any(ProjectFile.class)))
                .thenReturn(file);

        ProjectFileResponse response = projectFileService.updateFile(
                1L,2L,
                "user@test.com",
                request
        );

        assertEquals(file.getFileName(), response.getFileName());
        assertEquals(file.getFilePath(), response.getFilePath());
        assertEquals(file.getContent(), response.getContent());

        verify(projectRepository).findById(1L);
        verify(projectFileRepository).save(any(ProjectFile.class));
        verify(projectFileRepository).findByIdAndProjectId(2L,1L);
    }

    @Test
    void updateFile_projectNotFound_shouldThrowException(){

        when(projectRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> projectFileService.updateFile(
                        1L, 2L,
                        "owner@test.com",
                        request
                ));

        verify(projectFileRepository,never()).findByIdAndProjectId(2L,1L);

        verify(projectFileRepository,never()).save(any(ProjectFile.class));
    }

    @Test
    void updateFile_fileNotFound_shouldThrowException(){

        when(projectRepository.findById(1L))
                .thenReturn(Optional.of(project));

        when(projectFileRepository.findByIdAndProjectId(2L,1L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> projectFileService.updateFile(
                        1L, 2L,
                        "owner@test.com",
                        request
                ));

        verify(projectRepository).findById(1L);

        verify(projectFileRepository).findByIdAndProjectId(2L,1L);

        verify(projectFileRepository,never()).save(any(ProjectFile.class));
    }

    @Test
    void updateFile_versionConflict_shouldThrowException(){

        request.setVersion(2L);

        when(projectRepository.findById(1L))
                .thenReturn(Optional.of(project));

        when(projectFileRepository.findByIdAndProjectId(2L,1L))
                .thenReturn(Optional.of(file));

        assertThrows(FileConflictException.class,
                () -> projectFileService.updateFile(
                        1L,
                        2L,
                        "owner@test.com",
                        request
                ));

        verify(projectRepository).findById(1L);

        verify(projectFileRepository).findByIdAndProjectId(2L,1L);

        verify(projectFileRepository,never()).save(any(ProjectFile.class));
    }

    @Test
    void deleteFile_shouldSuccessfullyDeleteFile(){

        when(projectRepository.findById(1L))
                .thenReturn(Optional.of(project));

        when(projectFileRepository.findByIdAndProjectId(2L,1L))
                .thenReturn(Optional.of(file));

        projectFileService.deleteFile(
                1L,
                2L,
                "owner@test.com"
        );

        verify(projectRepository).findById(1L);

        verify(projectFileRepository).findByIdAndProjectId(2L,1L);

        verify(projectFileRepository).delete(file);
    }

    @Test
    void deleteFile_projectNotFound_shouldThrowException(){

        when(projectRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> projectFileService.deleteFile(
                        1L, 2L,
                        "owner@test.com"
                ));

        verify(projectFileRepository,never()).findByIdAndProjectId(2L,1L);

        verify(projectFileRepository,never()).delete(any(ProjectFile.class));
    }

    @Test
    void deleteFile_fileNotFound_shouldThrowException(){

        when(projectRepository.findById(1L))
                .thenReturn(Optional.of(project));

        when(projectFileRepository.findByIdAndProjectId(2L,1L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> projectFileService.deleteFile(
                        1L, 2L,
                        "owner@test.com"
                ));

        verify(projectRepository).findById(1L);

        verify(projectFileRepository).findByIdAndProjectId(2L,1L);

        verify(projectFileRepository,never()).delete(any(ProjectFile.class));
    }
}


















