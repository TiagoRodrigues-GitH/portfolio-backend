// src/main/java/org/example/service/ProjectService.java
package org.example.service;

import org.example.dto.request.ProjectRequest;
import org.example.dto.response.ProjectResponse;
import org.example.entity.Project;
import org.example.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProjectService {

    @Autowired
    private ProjectRepository projectRepository;

    public List<ProjectResponse> getAllPublishedProjects() {
        return projectRepository.findByPublishedTrueOrderByDisplayOrderAscCreatedAtDesc()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<ProjectResponse> getAllProjects() {
        return projectRepository.findAll()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public ProjectResponse getProjectById(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + id));
        return convertToResponse(project);
    }

    public ProjectResponse createProject(ProjectRequest request) {
        Project project = new Project();
        project.setTitle(request.getTitle());
        project.setDescription(request.getDescription());
        project.setMediaType(request.getMediaType());
        project.setMediaUrl(request.getMediaUrl());
        project.setThumbnailUrl(request.getThumbnailUrl());
        project.setProjectUrl(request.getProjectUrl());
        project.setPublished(request.getPublished() != null ? request.getPublished() : true);
        project.setDisplayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0);

        Project saved = projectRepository.save(project);
        return convertToResponse(saved);
    }

    public ProjectResponse updateProject(Long id, ProjectRequest request) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + id));

        project.setTitle(request.getTitle());
        project.setDescription(request.getDescription());
        project.setMediaType(request.getMediaType());
        project.setMediaUrl(request.getMediaUrl());
        project.setThumbnailUrl(request.getThumbnailUrl());
        project.setProjectUrl(request.getProjectUrl());
        project.setPublished(request.getPublished());
        project.setDisplayOrder(request.getDisplayOrder());

        Project updated = projectRepository.save(project);
        return convertToResponse(updated);
    }

    public void deleteProject(Long id) {
        projectRepository.deleteById(id);
    }

    private ProjectResponse convertToResponse(Project project) {
        return ProjectResponse.builder()
                .id(project.getId())
                .title(project.getTitle())
                .description(project.getDescription())
                .mediaUrl(project.getMediaUrl())
                .mediaType(project.getMediaType())
                .thumbnailUrl(project.getThumbnailUrl())
                .projectUrl(project.getProjectUrl())
                .published(project.getPublished())
                .displayOrder(project.getDisplayOrder())
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .build();
    }
}