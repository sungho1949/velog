package velog.velog.domain.auth.dto;

import lombok.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import velog.velog.domain.user.dto.UserDto;
import velog.velog.system.security.jwt.dto.JwtDto;

public class AuthDto {
    @Builder @AllArgsConstructor @NoArgsConstructor @Getter
    public static class SignUpRequest {
        @NotBlank(message = "이메일을 입력해주세요.") @Email
        private String email;

        @NotBlank(message = "비밀번호를 입력해주세요.") @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{8,16}$")
        private String password;

        @NotBlank(message = "비밀번호를 다시 입력해주세요.")
        private String passwordCheck;

        @NotBlank(message = "성을 입력해주세요.")
        private String lastName;

        @NotBlank(message = "이름을 입력해주세요.")
        private String firstName;
    }

    @Builder @AllArgsConstructor @NoArgsConstructor @Getter
    public static class LoginRequest {
        @NotBlank(message = "이메일을 입력해주세요.")
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        private String email;

        @NotBlank(message = "비밀번호를 입력해주세요.")
        private String password;
    }

    @Builder @AllArgsConstructor @NoArgsConstructor @Getter
    public static class LoginResponse {
        private UserDto.UserResponse user;
        private JwtDto.TokenExpiresInfo tokenExpiresInfo;

        public static LoginResponse of(UserDto.UserResponse user, JwtDto.TokenExpiresInfo tokenExpiresInfo) {
            return LoginResponse.builder()
                    .user(user)
                    .tokenExpiresInfo(tokenExpiresInfo)
                    .build();
        }
    }

    @Builder @AllArgsConstructor @NoArgsConstructor @Getter
    public static class VerifyEmailRequest {
        @NotBlank(message = "이메일을 입력해주세요")
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        private String email;

        @NotBlank
        @Pattern(regexp = "^[0-9]{6}$", message = "인증번호는 6자리 숫자입니다.")
        private String code;

        @NotBlank
        private EmailPurpose purpose; // SIGNUP인지 PASSWORD_RESET인지
    }

    @Getter @Builder @AllArgsConstructor @NoArgsConstructor
    public static class ResetPasswordRequest {
        @NotBlank(message = "이메일을 입력해주세요.")
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        private String email;

        @NotBlank(message = "새로운 비밀번호를 입력해주세요.")
        @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{8,16}$")
        private String newPassword;

        @NotBlank(message = "비밀번호 확인을 입력해주세요.")
        private String newPasswordCheck;
    }
}
