package velog.velog.domain.post.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import velog.velog.common.TimeBaseEntity;
import velog.velog.domain.user.entity.User;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "posts")
public class Post extends TimeBaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title; // 제목

    @Column(nullable = false)
    private String content; // 내용

    @Column(length = 100)
    private String summary; // 요약문

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Builder
    public Post(String title, String content, User user) {
        this.title = title;
        this.content = content;
        this.user = user;
        this.summary = generateSummary(content);
    }

    public void update(String title, String content) {
        this.title = title;
        this.content = content;
        this.summary = generateSummary(content);
    }

    private String generateSummary(String content) {
        if (content == null || content.isBlank()) return "";
        return content.length() > 50
                ? content.substring(0, 50) + "..."
                : content;
    }
}
