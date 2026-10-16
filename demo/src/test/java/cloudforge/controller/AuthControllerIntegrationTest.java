package cloudforge.controller;

import com.cloudforge.DemoApplication;
import com.cloudforge.application.dto.LoginRequest;
import com.cloudforge.application.dto.ProjectRequest;
import com.cloudforge.application.dto.RegisterRequest;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest(classes = DemoApplication.class)
@AutoConfigureMockMvc
public class AuthControllerIntegrationTest {


    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();


    @Test
    void register_shouldReturnCreated() throws Exception {

        RegisterRequest request = new RegisterRequest();

        request.setUsername("integrationuser_new");
        request.setEmail("integration_new_" + System.currentTimeMillis() + "@example.com");
        request.setPassword("Password123");


        mockMvc.perform(
                post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated());
    }


    @Test
    void register_shouldReturnConflictForDuplicateEmail()
            throws Exception {

        RegisterRequest request = new RegisterRequest();

        request.setUsername("duplicateuser");
        request.setEmail("integration_test_01@example.com");
        request.setPassword("Password123");


        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                ).andExpect(status().isConflict());
    }


    @Test
    void login_shouldReturnJwtToken() throws Exception {

        LoginRequest request = new LoginRequest();

        request.setEmail("integration_test_01@example.com");
        request.setPassword("Password123");


        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                ).andExpect(status().isOk());
    }

    @Test
    void createProject_shouldReturnCreatedForAuthenticatedUser()
            throws Exception {

        String token = loginAndGetToken(
                "integration_test_01@example.com",
                "Password123"
        );


        ProjectRequest request = new ProjectRequest();

        request.setName("Integration Test Project");
        request.setDescription("Testing project creation");
        request.setStatus("ACTIVE");


        mockMvc.perform(
                        post("/api/projects")
                                .header("Authorization",
                                        "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                ).andExpect(status().isCreated());
    }


    @Test
    void createProject_shouldReturnForbiddenWithoutToken()
            throws Exception {

        ProjectRequest request = new ProjectRequest();

        request.setName("Unauthorized Project");
        request.setDescription("Should not be created");
        request.setStatus("ACTIVE");


        mockMvc.perform(
                        post("/api/projects")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                ).andExpect(status().isForbidden());
    }


    @Test
    void getProjects_shouldReturnSuccessForAuthenticatedUser()
            throws Exception {

        String token = loginAndGetToken(
                "integration_test_01@example.com",
                "Password123"
        );


        mockMvc.perform(
                        get("/api/projects")
                                .header("Authorization",
                                        "Bearer " + token)
                ).andExpect(status().isOk());
    }



    @Test
    void updateProject_shouldReturnSuccessForOwner()
            throws Exception {

        String token = loginAndGetToken(
                "integration_test_01@example.com",
                "Password123"
        );


        ProjectRequest createRequest = new ProjectRequest();

        createRequest.setName("Project For Update Test");
        createRequest.setDescription("Created for update test");
        createRequest.setStatus("ACTIVE");


        String response = mockMvc.perform(
                        post("/api/projects")
                                .header("Authorization",
                                        "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createRequest)
                                )
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();


        Long projectId =
                objectMapper
                        .readTree(response)
                        .get("id")
                        .asLong();


        ProjectRequest updateRequest = new ProjectRequest();

        updateRequest.setName("Updated Integration Project");
        updateRequest.setDescription("Updated description");
        updateRequest.setStatus("ACTIVE");


        mockMvc.perform(
                        put("/api/projects/" + projectId)
                                .header("Authorization",
                                        "Bearer " + token
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateRequest)
                                )
                )
                .andExpect(status().isOk());
    }



    @Test
    void deleteProject_shouldReturnNoContentForOwner()
            throws Exception {

        String token = loginAndGetToken(
                "integration_test_02@example.com",
                "Password123"
        );


        ProjectRequest createRequest = new ProjectRequest();

        createRequest.setName("Project For Delete Test");
        createRequest.setDescription("Created for delete test");
        createRequest.setStatus("ACTIVE");


        String response = mockMvc.perform(
                        post("/api/projects")
                                .header("Authorization",
                                        "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createRequest))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();



        Long projectId =
                objectMapper
                        .readTree(response)
                        .get("id")
                        .asLong();



        mockMvc.perform(
                        delete("/api/projects/" + projectId)
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isNoContent());
    }

    @Test
    void updateProject_shouldReturnForbiddenForNonOwner()
            throws Exception {

        String ownerToken = loginAndGetToken(
                "integration_test_01@example.com",
                "Password123"
        );


        ProjectRequest createRequest = new ProjectRequest();

        createRequest.setName("Protected Update Project");
        createRequest.setDescription("Testing authorization");
        createRequest.setStatus("ACTIVE");


        String response = mockMvc.perform(
                        post("/api/projects")
                                .header(
                                        "Authorization",
                                        "Bearer " + ownerToken
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                createRequest
                                        )
                                )
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();


        Long projectId =
                objectMapper
                        .readTree(response)
                        .get("id")
                        .asLong();


        String nonOwnerToken = loginAndGetToken(
                "integration_test_02@example.com",
                "Password123"
        );


        ProjectRequest updateRequest = new ProjectRequest();

        updateRequest.setName("Unauthorized Update");
        updateRequest.setDescription("Should fail");
        updateRequest.setStatus("ACTIVE");


        mockMvc.perform(
                        put("/api/projects/" + projectId)
                                .header(
                                        "Authorization",
                                        "Bearer " + nonOwnerToken
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                updateRequest
                                        )
                                )
                )
                .andExpect(status().isForbidden());
    }


    @Test
    void deleteProject_shouldReturnForbiddenForNonOwner()
            throws Exception {

        String ownerToken = loginAndGetToken(
                "integration_test_01@example.com",
                "Password123"
        );


        ProjectRequest createRequest = new ProjectRequest();

        createRequest.setName("Protected Delete Project");
        createRequest.setDescription("Testing delete authorization");
        createRequest.setStatus("ACTIVE");


        String response = mockMvc.perform(
                        post("/api/projects")
                                .header("Authorization", "Bearer " + ownerToken
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createRequest))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();


        Long projectId =
                objectMapper
                        .readTree(response)
                        .get("id")
                        .asLong();


        String nonOwnerToken = loginAndGetToken(
                "integration_test_02@example.com",
                "Password123"
        );


        mockMvc.perform(
                        delete("/api/projects/" + projectId)
                                .header(
                                        "Authorization",
                                        "Bearer " + nonOwnerToken
                                )
                )
                .andExpect(status().isForbidden());
    }

    private String loginAndGetToken(
            String email,
            String password) throws Exception {

        LoginRequest loginRequest = new LoginRequest();

        loginRequest.setEmail(email);
        loginRequest.setPassword(password);


        return mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                                loginRequest
                                        )
                                )
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
    }
}