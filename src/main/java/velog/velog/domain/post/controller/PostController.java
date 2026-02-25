package velog.velog.domain.post.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import velog.velog.domain.post.dto.PostDto;
import velog.velog.domain.post.service.PostService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    // 글 작성
    @PostMapping
    public ResponseEntity<Long> create(@RequestBody @Valid PostDto.CreateRequest request,
                                       @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(postService.create(request, userDetails.getUsername()));
    }

    // 글 목록 조회
    @GetMapping
    public ResponseEntity<List<PostDto.ListResponse>> getList() {
        return ResponseEntity.ok(postService.findAll());
    }

    // 글 상세 조회
    @GetMapping("/{postId}")
    public ResponseEntity<PostDto.DetailResponse> getDetail(@PathVariable(name = "postId") Long postId) {
        return ResponseEntity.ok(postService.findById(postId));
    }

    // 글 수정
    @PatchMapping("/{postId}")
    public ResponseEntity<Void> update(@PathVariable(name = "postId") Long postId,
                                       @RequestBody @Valid PostDto.CreateRequest request,
                                       @AuthenticationPrincipal UserDetails userDetails) {
        postService.update(postId, request, userDetails.getUsername());
        return ResponseEntity.ok().build();
    }

    // 글 삭제
    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> delete(@PathVariable(name = "postId") Long postId,
                                       @AuthenticationPrincipal UserDetails userDetails) {
        postService.delete(postId, userDetails.getUsername());
        return ResponseEntity.ok().build();
    }
}