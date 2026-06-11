package org.example.dto.request;

// src/main/java/org/example/portfolio/dto/request/ProjectRequest.java

// src/main/java/org/example/portfolio/dto/request/ProjectRequest.java


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.example.entity.Project;

@Data
public class ProjectRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    private String mediaUrl;

    @NotNull(message = "Media type is required")
    private Project.MediaType mediaType;

    private String thumbnailUrl;

    private String projectUrl;

    private Boolean published = true;

    private Integer displayOrder = 0;
}