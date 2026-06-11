// src/test/java/org/example/portfolio/repository/ProjectRepositoryTest.java


import org.example.entity.Project;
import org.example.repository.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ProjectRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProjectRepository projectRepository;

    private Project testProject;

    @BeforeEach
    void setUp() {
        testProject = Project.builder()
                .title("Test Project")
                .description("This is a test project description")
                .mediaType(Project.MediaType.IMAGE)
                .mediaUrl("/uploads/test.jpg")
                .published(true)
                .displayOrder(1)
                .build();

        entityManager.persist(testProject);
        entityManager.flush();
    }

    @Test
    void whenFindById_thenReturnProject() {
        // When
        Project found = projectRepository.findById(testProject.getId()).orElse(null);

        // Then
        assertThat(found).isNotNull();
        assertThat(found.getTitle()).isEqualTo("Test Project");
        assertThat(found.getDescription()).isEqualTo("This is a test project description");
    }

    @Test
    void whenFindByPublishedTrue_thenReturnOnlyPublishedProjects() {
        // Given - create an unpublished project
        Project unpublishedProject = Project.builder()
                .title("Unpublished Project")
                .description("Should not appear")
                .mediaType(Project.MediaType.VIDEO)
                .published(false)
                .build();
        entityManager.persist(unpublishedProject);
        entityManager.flush();

        // When
        List<Project> publishedProjects = projectRepository.findByPublishedTrueOrderByDisplayOrderAscCreatedAtDesc();

        // Then
        assertThat(publishedProjects).hasSize(1);
        assertThat(publishedProjects.get(0).getTitle()).isEqualTo("Test Project");
    }

    @Test
    void whenSaveProject_thenGenerateId() {
        // Given
        Project newProject = Project.builder()
                .title("New Project")
                .description("Brand new project")
                .mediaType(Project.MediaType.VIDEO)
                .published(true)
                .build();

        // When
        Project saved = projectRepository.save(newProject);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
    }
}