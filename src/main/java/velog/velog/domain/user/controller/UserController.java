package velog.velog.domain.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import velog.velog.domain.user.dto.UserDto;
import velog.velog.domain.user.serivce.UserService;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    public ResponseEntity<UserDto.UserResponse> getMyInfo(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(userService.getMyInfo(userDetails.getUsername()));
    }

    @PatchMapping
    public ResponseEntity<Void> updateMyInfo(@AuthenticationPrincipal UserDetails userDetails,
                                             @RequestBody UserDto.UpdateRequest request) {
        userService.updateMyInfo(userDetails.getUsername(), request);
        return ResponseEntity.ok().build();
    }
}
