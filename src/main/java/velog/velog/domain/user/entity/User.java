package velog.velog.domain.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import velog.velog.common.UserBaseEntity;

@Entity
@Getter
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends UserBaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String firstName; // 성

    @Column(nullable = false)
    private String lastName; // 이름 -> 로그인 시 배너 이름's 개발 블로그

    @Builder.Default
    private boolean isVerified = false; // 이메일 인증 여부

    public void updateInfo(String lastName, String firstName) {
        if(lastName != null && !lastName.isBlank()) {
            this.lastName = lastName;
        }

        if (firstName != null && !firstName.isBlank()) {
            this.firstName = firstName;
        }
    }
    public void updatePassword(String password) {
        this.password = password;
    }
}
