package velog.velog.domain.post.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import velog.velog.domain.post.dto.PostDto;
import velog.velog.domain.post.entity.Post;
import velog.velog.domain.post.repository.PostRepository;
import velog.velog.domain.user.entity.User;
import velog.velog.domain.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Transactional
    public Long create(PostDto.CreateRequest request, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        Post post = Post.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .user(user)
                .build();

        return postRepository.save(post).getId();
    }

    public List<PostDto.ListResponse> findAll() {
        return postRepository.findAllWithUser().stream()
                .map(PostDto.ListResponse::from)
                .collect(Collectors.toList());
    }

    public PostDto.DetailResponse findById(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));
        return PostDto.DetailResponse.from(post);
    }

    @Transactional
    public void update(Long id, PostDto.CreateRequest request, String email) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        // 권한 확인: 작성자와 수정 요청자가 일치하는지 검증
        validateAuthor(post, email);

        post.update(request.getTitle(), request.getContent());
    }

    @Transactional
    public void delete(Long id, String email) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        validateAuthor(post, email);

        postRepository.delete(post);
    }

    private void validateAuthor(Post post, String email) {
        if (!post.getUser().getEmail().equals(email)) {
            throw new RuntimeException("해당 게시글에 대한 권한이 없습니다.");
        }
    }
}
