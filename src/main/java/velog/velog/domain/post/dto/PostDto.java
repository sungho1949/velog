package velog.velog.domain.post.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import velog.velog.domain.post.entity.Post;

import java.time.LocalDateTime;

public class PostDto {

    @Getter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CreateRequest {
        @NotBlank(message = "제목은 필수입니다.")
        private String title;

        @NotBlank(message = "내용은 필수입니다.")
        private String content;
    }

    @Getter @Builder @AllArgsConstructor
    public static class DetailResponse {
        private Long id;
        private String title;
        private String content;
        private String authorName;
        private LocalDateTime createdAt;

        public static DetailResponse from(Post post) {
            return DetailResponse.builder()
                    .id(post.getId())
                    .title(post.getTitle())
                    .content(post.getContent())
                    .authorName(post.getUser().getFirstName()) // 작성자 이름
                    .createdAt(post.getCreatedAt())
                    .build();
        }
    }

    @Getter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ListResponse {
        private Long id;
        private String title;
        private String summary;
        private String authorName;
        private LocalDateTime createdAt;

        public static ListResponse from(Post post) {
            String content = post.getContent();
            String summary = (content != null && content.length() > 50)
                    ? content.substring(0, 50) + "..."
                    : content;

            return ListResponse.builder()
                    .id(post.getId())
                    .title(post.getTitle())
                    .summary(summary)
                    .authorName(post.getUser().getFirstName())
                    .createdAt(post.getCreatedAt())
                    .build();
        }
    }
}
