package velog.velog.domain.user.serivce;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import velog.velog.domain.user.dto.UserDto;
import velog.velog.domain.user.entity.User;
import velog.velog.domain.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
    private final UserRepository userRepository;

    /**
     * 내 정보 조회
     */
    public UserDto.UserResponse getMyInfo(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        return UserDto.UserResponse.of(user);
    }

    /**
     * 내 정보 수정
     */
    @Transactional
    public void updateMyInfo(String email, UserDto.UpdateRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        user.updateInfo(request.getLastName(), request.getFirstName());
    }
}
