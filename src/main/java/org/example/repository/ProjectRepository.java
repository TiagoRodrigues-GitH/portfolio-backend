package org.example.repository;

// src/main/java/org/example/portfolio/repository/ProjectRepository.java


import org.example.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByPublishedTrueOrderByDisplayOrderAscCreatedAtDesc();
    List<Project> findByMediaType(Project.MediaType mediaType);
}