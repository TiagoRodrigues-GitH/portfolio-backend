// src/test/java/org/example/portfolio/controller/ProjectControllerTest.java


import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.controller.ProjectController;
import org.example.dto.request.ProjectRequest;
import org.example.dto.response.ProjectResponse;
import org.example.entity.Project;
import org.example.service.ProjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProjectController.class)
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Mock
    private ProjectService projectService;

    private ProjectResponse testResponse;
    private ProjectRequest testRequest;

    @BeforeEach
    void setUp() {
        testResponse = ProjectResponse.builder()
                .id(1L)
                .title("Test Project")
                .description("Test Description")
                .mediaType(Project.MediaType.IMAGE)
                .published(true)
                .build();

        testRequest = new ProjectRequest();
        testRequest.setTitle("New Project");
        testRequest.setDescription("New Description");
        testRequest.setMediaType(Project.MediaType.VIDEO);
        testRequest.setPublished(true);
    }

    @Test
    void whenGetAllProjects_thenReturnList() throws Exception {
        // Given
        when(projectService.getAllPublishedProjects()).thenReturn(List.of(testResponse));

        // When & Then
        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Test Project"))
                .andExpect(jsonPath("$[0].description").value("Test Description"));
    }

    @Test
    void whenGetProjectById_thenReturnProject() throws Exception {
        // Given
        when(projectService.getProjectById(1L)).thenReturn(testResponse);

        // When & Then
        mockMvc.perform(get("/api/projects/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Project"));
    }

    @Test
    void whenCreateProject_thenReturnCreatedProject() throws Exception {
        // Given
        when(projectService.createProject(any(ProjectRequest.class))).thenReturn(testResponse);

        // When & Then
        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Test Project"));
    }

    @Test
    void whenDeleteProject_thenReturnNoContent() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/projects/1"))
                .andExpect(status().isNoContent());

        verify(projectService, times(1)).deleteProject(1L);
    }
}