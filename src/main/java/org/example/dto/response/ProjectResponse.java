package org.example.dto.response;

// src/main/java/org/example/portfolio/dto/response/ProjectResponse.java

// src/main/java/org/example/portfolio/dto/response/ProjectResponse.java


import lombok.Builder;
import lombok.Data;
import org.example.entity.Project;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ProjectResponse {
    private Long id;
    private String title;
    private String description;
    private String mediaUrl;
    private Project.MediaType mediaType;
    private String thumbnailUrl;
    private String projectUrl;
    private Boolean published;
    private Integer displayOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<CommentResponse> comments; // Será implementado depois
}