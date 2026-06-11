package org.example.dto.response;



import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class CommentResponse {
    private Long id;
    private String authorName;
    private String content;
    private LocalDateTime createdAt;
    private Boolean approved;
}