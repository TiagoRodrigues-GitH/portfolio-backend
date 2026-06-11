// src/test/java/org/example/portfolio/service/ProjectServiceTest.java


import org.example.dto.request.ProjectRequest;
import org.example.dto.response.ProjectResponse;
import org.example.entity.Project;
import org.example.repository.ProjectRepository;
import org.example.service.ProjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private ProjectService projectService;

    private Project testProject;
    private ProjectRequest testRequest;

    @BeforeEach
    void setUp() {
        testProject = Project.builder()
                .id(1L)
                .title("Test Project")
                .description("Test Description")
                .mediaType(Project.MediaType.IMAGE)
                .published(true)
                .displayOrder(1)
                .build();

        testRequest = new ProjectRequest();
        testRequest.setTitle("New Project");
        testRequest.setDescription("New Description");
        testRequest.setMediaType(Project.MediaType.VIDEO);
        testRequest.setPublished(true);
    }

    @Test
    void whenGetAllPublishedProjects_thenReturnList() {
        // Given
        when(projectRepository.findByPublishedTrueOrderByDisplayOrderAscCreatedAtDesc())
                .thenReturn(List.of(testProject));

        // When
        List<ProjectResponse> projects = projectService.getAllPublishedProjects();

        // Then
        assertThat(projects).hasSize(1);
        assertThat(projects.get(0).getTitle()).isEqualTo("Test Project");
        verify(projectRepository, times(1)).findByPublishedTrueOrderByDisplayOrderAscCreatedAtDesc();
    }

    @Test
    void whenGetProjectById_thenReturnProject() {
        // Given
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));

        // When
        ProjectResponse project = projectService.getProjectById(1L);

        // Then
        assertThat(project).isNotNull();
        assertThat(project.getTitle()).isEqualTo("Test Project");
    }

    @Test
    void whenGetProjectByIdNotFound_thenThrowException() {
        // Given
        when(projectRepository.findById(99L)).thenReturn(Optional.empty());

        // Then
        assertThatThrownBy(() -> projectService.getProjectById(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Project not found");
    }

    @Test
    void whenCreateProject_thenReturnSavedProject() {
        // Given
        when(projectRepository.save(any(Project.class))).thenReturn(testProject);

        // When
        ProjectResponse created = projectService.createProject(testRequest);

        // Then
        assertThat(created).isNotNull();
        verify(projectRepository, times(1)).save(any(Project.class));
    }
}