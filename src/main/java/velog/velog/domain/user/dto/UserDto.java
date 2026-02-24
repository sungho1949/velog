package velog.velog.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import velog.velog.domain.user.entity.User;

public class UserDto {
    @Getter @Builder @AllArgsConstructor @NoArgsConstructor
    public static class UserResponse {
        private String email;
        private String lastName;
        private String firstName;

        public static UserResponse of(User user) {
            return UserResponse.builder()
                    .email(user.getEmail())
                    .lastName(user.getLastName())
                    .firstName(user.getFirstName())
                    .build();
        }
    }
}
