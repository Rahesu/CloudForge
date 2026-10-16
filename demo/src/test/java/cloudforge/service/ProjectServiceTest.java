package cloudforge.service;

import com.cloudforge.application.dto.ProjectPageResponse;
import com.cloudforge.application.dto.ProjectRequest;
import com.cloudforge.application.dto.ProjectResponse;
import com.cloudforge.application.entity.Project;
import com.cloudforge.application.entity.ProjectMember;
import com.cloudforge.application.entity.ProjectRole;
import com.cloudforge.application.entity.User;
import com.cloudforge.application.exception.ResourceAccessDeniedException;
import com.cloudforge.application.exception.ResourceNotFoundException;
import com.cloudforge.application.repository.ProjectMemberRepository;
import com.cloudforge.application.repository.ProjectRepository;
import com.cloudforge.application.repository.UserRepository;
import com.cloudforge.application.service.ProjectService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;


import java.util.List;
import java.util.Optional;

import static com.cloudforge.application.entity.ProjectRole.VIEWER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ProjectMemberRepository projectMemberRepository;
    @InjectMocks
    private ProjectService projectService;

    private Project project;
    private User owner;
    private User user;

    @BeforeEach
    void setUp(){
         project = new Project();

        project.setName("cloudForge");
        project.setDescription("cloud development platform");
        project.setStatus("active");

         owner=new User();

        owner.setEmail("owner@test.com");
        project.setOwner(owner);

    }


    @Test
    void createProject_shouldCreateProjectSuccessfully(){

        User user=new User();
        user.setId(1L);
        user.setEmail("owner@test.com");
        user.setUsername("owner");

        when(userRepository.findByEmail("owner@test.com"))
                .thenReturn(Optional.of(user));

        when(projectRepository.save(project))
                .thenReturn(project);

        ProjectResponse response =  projectService.createProject(project,"owner@test.com");

        assertEquals("cloudForge", response.getName());
        assertEquals("owner@test.com" , response.getOwnerEmail());
        assertEquals(1L , response.getOwnerId());

        assertEquals(user,project.getOwner());

        verify(userRepository).findByEmail("owner@test.com");
        verify(projectRepository).save(project);

    }

    @Test
    void createProject_shouldThrowExceptionWhenUserNotFound(){

        when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> projectService.createProject(project,"unknown@test.com"));

        verify(userRepository).findByEmail("unknown@test.com");

        verify(projectRepository,never()).save(project);
    }

    @Test
    void updateProjectById_nonOwnerShouldThrowResourceAccessDeniedException(){

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        ProjectRequest request = new ProjectRequest();

        assertThrows(ResourceAccessDeniedException.class ,
                () -> projectService.updateProjectById(1L,"user@test.com",request));


        verify(projectRepository,never()).save(project);
    }

    @Test
    void updateProjectById_OwnerShouldSuccessfullyUpdateProject(){

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        ProjectRequest request = new ProjectRequest();

        request.setName("cloudForge");
        request.setDescription("cloud development platform");
        request.setStatus("Running");

        when(projectRepository.save(project)).thenReturn(project);

        ProjectResponse response =  projectService.updateProjectById(1L,"owner@test.com",request);

        assertEquals("cloudForge",response.getName());
        assertEquals("cloud development platform",response.getDescription());
        assertEquals("Running",response.getStatus());

        verify(projectRepository).save(project);
    }



    @Test
    void deleteProject_ByOwnerShouldSuccessfullyDeleteProject(){

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        projectService.deleteProject(1L,"owner@test.com");

        verify(projectRepository).delete(project);

    }

    @Test
    void updateProjectById_notFound_ShouldReturnResourceNotFoundException(){

        when(projectRepository.findById(999L)).thenReturn(Optional.empty());

        ProjectRequest request = new ProjectRequest();

        assertThrows(ResourceNotFoundException.class,
                ()-> projectService.updateProjectById(999L,"owner@test.com",request));


        verify(projectRepository,never()).save(project);

    }

    @Test
    void deleteProject_ByNonOwner_shouldReturnResourceAccessDenied(){

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        assertThrows(ResourceAccessDeniedException.class,
                     () -> projectService.deleteProject(1L,"user@test.com"));

        verify(projectRepository,never()).delete(project);
    }

    @Test
    void deleteProject_ResourceNotFoundException(){

        when(projectRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> projectService.deleteProject(1L,"owner@test.com"));


    }

    @Test
    void getProjectById_authorizedUser_shouldSuccessfullyReturnProject(){


        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

       ProjectResponse response= projectService.getProjectById(1L,"owner@test.com");

        assertEquals("cloudForge",response.getName());
        assertEquals("cloud development platform",response.getDescription());
        assertEquals("active",response.getStatus());

    }

    @Test
    void getProjectById_unAuthorizedUser_shouldReturnResourceAccessDenied(){

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        assertThrows(ResourceAccessDeniedException.class,
                () -> projectService.getProjectById(1L,"user@test.com"));


    }

    @Test
    void getMyProjects_shouldSuccessfullyReturnUserProject() {

        owner.setId(1L);

        Pageable pageable = PageRequest.of(0, 5);

        when(userRepository.findByEmail("owner@test.com")).thenReturn(Optional.of(owner));

        Page<Project> page = new PageImpl<>(
                        List.of(project),
                        pageable,
                        1
                );

        when(projectRepository.findByOwnerId(1L, pageable)).thenReturn(page);


        ProjectPageResponse response = projectService.getMyProjects(
                        "owner@test.com",
                        "",
                        pageable
                );

        assertEquals(1, response.getContent().size());
        assertEquals("cloudForge",
                response.getContent().get(0).getName());


        verify(userRepository).findByEmail("owner@test.com");
        verify(projectRepository).findByOwnerId(1L, pageable);
    }

    @Test
    void getMyProjects_shouldSearchProjectsSuccessfully() {

        owner.setId(1L);

        Pageable pageable = PageRequest.of(0, 5);

        when(userRepository.findByEmail("owner@test.com")).thenReturn(Optional.of(owner));

        Page<Project> page = new PageImpl<>(
                        List.of(project),
                        pageable,
                        1
                );

        when(projectRepository.searchProjects(1L, "cloudForge", pageable))
                .thenReturn(page);

        ProjectPageResponse response = projectService.getMyProjects(
                        "owner@test.com",
                        "cloudForge",
                        pageable
                );

        assertEquals(1, response.getContent().size());
        assertEquals("cloudForge", response.getContent().get(0).getName());

        verify(userRepository).findByEmail("owner@test.com");
        verify(projectRepository).searchProjects(1L, "cloudForge", pageable);

        verify(projectRepository, never()).findByOwnerId(anyLong(), any(Pageable.class));
    }


    @Test
    void getProjectById_notFound_shouldThrowException(){

        when(projectRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> projectService.getProjectById(1L,"owner@test.com"));

        verify(projectRepository).findById(1L);
    }

    @Test
    void addMember_shouldSuccessfullyAddMember() {

        owner.setId(1L);

        User developer = new User();
        developer.setId(2L);
        developer.setEmail("developer@test.com");

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        when(userRepository.findById(2L)).thenReturn(Optional.of(developer));

        when(projectMemberRepository.existsByProjectIdAndUserId(1L, 2L))
                .thenReturn(false);

        ProjectResponse response = projectService.addMember(
                        1L,
                        2L,
                        ProjectRole.DEVELOPER,
                        "owner@test.com"
                );

        assertEquals("cloudForge", response.getName());

        verify(projectRepository).findById(1L);
        verify(userRepository).findById(2L);
        verify(projectMemberRepository).existsByProjectIdAndUserId(1L, 2L);
        verify(projectMemberRepository).save(any(ProjectMember.class));
    }

    @Test
    void addMember_nonOwner_shouldThrowAccessDenied() {

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        assertThrows(ResourceAccessDeniedException.class,
                () -> projectService.addMember(
                        1L,
                        2L,
                        ProjectRole.DEVELOPER,
                        "user@test.com"
                )
        );

        verify(userRepository, never()).findById(2L);
        verify(projectMemberRepository, never()).save(any(ProjectMember.class));
    }

    @Test
    void addMember_userNotFound_shouldThrowException() {

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> projectService.addMember(
                        1L,
                        2L,
                        ProjectRole.DEVELOPER,
                        "owner@test.com"
                )
        );

        verify(projectMemberRepository, never()).save(any(ProjectMember.class));
    }

    @Test
    void addMember_existingMember_shouldThrowException() {

        owner.setId(1L);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        User developer = new User();
        developer.setId(2L);
        developer.setEmail("developer@test.com");

        when(userRepository.findById(2L)).thenReturn(Optional.of(developer));

        when(projectMemberRepository.existsByProjectIdAndUserId(1L, 2L))
                .thenReturn(true);

        assertThrows(ResourceAccessDeniedException.class,
                () -> projectService.addMember(
                        1L,
                        2L,
                        ProjectRole.DEVELOPER,
                        "owner@test.com"
                )
        );

        verify(projectMemberRepository, never()).save(any(ProjectMember.class));
    }

    @Test
    void removeMember_existingMember_shouldRemoveSuccessfully(){

        User developer = new User();
        developer.setId(2L);
        developer.setEmail("developer@test.com");

        ProjectMember member = new ProjectMember();
        member.setProject(project);
        member.setUser(developer);
        member.setRole(ProjectRole.DEVELOPER);

        project.getMembers().add(member);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        when(projectMemberRepository.findByProjectIdAndUserId(1L,2L))
                .thenReturn(Optional.of(member));

        project.getMembers().add(member);

        ProjectResponse response= projectService.removeMember(
                1L,2L,"owner@test.com"
        );

        verify(projectRepository).findById(1L);
        verify(projectMemberRepository).findByProjectIdAndUserId(1L,2L);
        verify(projectMemberRepository).delete(any(ProjectMember.class));

    }

    @Test
    void removeMember_nonOwner_shouldThrowAccessDenied() {

        owner.setId(1L);

        when(projectRepository.findById(1L))
                .thenReturn(Optional.of(project));

        assertThrows(ResourceAccessDeniedException.class,
                () -> projectService.removeMember(
                        1L,
                        2L,
                        "user@test.com"
                ));

        verify(projectMemberRepository, never())
                .findByProjectIdAndUserId(1L, 2L);

        verify(projectMemberRepository, never())
                .delete(any(ProjectMember.class));
    }

    @Test
    void removeMember_memberNotFound_shouldThrowException() {

        owner.setId(1L);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        when(projectMemberRepository.findByProjectIdAndUserId(1L, 2L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> projectService.removeMember(
                        1L,
                        2L,
                        "owner@test.com"
                ));

        verify(projectMemberRepository)
                .findByProjectIdAndUserId(1L, 2L);

        verify(projectMemberRepository, never())
                .delete(any(ProjectMember.class));
    }

    @Test
    void changeMemberRole_ShouldChangeSuccessfully(){

        User developer = new User();
        developer.setId(2L);
        developer.setEmail("developer@test.com");

        ProjectMember member = new ProjectMember();
        member.setProject(project);
        member.setUser(developer);
        member.setRole(ProjectRole.DEVELOPER);

        project.getMembers().add(member);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        when(projectMemberRepository.findByProjectIdAndUserId(1L,2L))
                .thenReturn(Optional.of(member));

        ProjectResponse response = projectService.changeMemberRole(
                1L,
                2L,
               VIEWER,
                "owner@test.com"
        );

        assertEquals(VIEWER,member.getRole());

        verify(projectMemberRepository).findByProjectIdAndUserId(1L,2L);
        verify(projectMemberRepository).save(any(ProjectMember.class));


    }

    @Test
    void changeMemberRole_nonOwner_ShouldThrowException(){

        User developer = new User();
        developer.setId(2L);
        developer.setEmail("developer@test.com");

        ProjectMember member = new ProjectMember();
        member.setProject(project);
        member.setUser(developer);
        member.setRole(ProjectRole.DEVELOPER);

        project.getMembers().add(member);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        when(projectMemberRepository.findByProjectIdAndUserId(1L,2L))
                .thenReturn(Optional.of(member));

        assertThrows(ResourceAccessDeniedException.class,
                () -> projectService.changeMemberRole(
                        1L,
                        2L,
                        VIEWER,
                        "user@test.com"
                ));

        verify(projectRepository).findById(1L);
        verify(projectMemberRepository).findByProjectIdAndUserId(1L,2L);
        verify(projectMemberRepository,never()).save(member);
    }

    @Test
    void changeMemberRole_memberNotFound_shouldThrowException(){


        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        when(projectMemberRepository.findByProjectIdAndUserId(1L,2L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> projectService.changeMemberRole(
                        1L,
                        2L,
                        VIEWER,
                        "owner@test.com"
                ));

        verify(projectRepository).findById(1L);
        verify(projectMemberRepository).findByProjectIdAndUserId(1L,2L);
        verify(projectMemberRepository,never()).save(any(ProjectMember.class));
    }
}
