package velog.velog.domain.post.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import velog.velog.common.enums.ViewDomain;
import velog.velog.common.service.RedisViewService;
import velog.velog.domain.post.dto.PostDto;
import velog.velog.domain.post.entity.Post;
import velog.velog.domain.post.repository.PostQueryRepository;
import velog.velog.domain.post.repository.PostRepository;
import velog.velog.domain.user.entity.User;
import velog.velog.domain.user.repository.UserRepository;
import velog.velog.system.exception.model.ErrorCode;
import velog.velog.system.exception.model.RestException;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostQueryRepository postQueryRepository;
    private final RedisViewService redisViewService;

    /**
     * 글 상세 조회
     */
    @Transactional
    public PostDto.DetailResponse getDetail(Long id, String email, String ip) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RestException(ErrorCode.POST_NOT_FOUND));

        // 로그인 여부 확인
        Long userId = (email != null) ?
                userRepository.findByEmail(email).map(User::getId).orElse(null) : null;

        // Redis 조회수 증가 호출
        redisViewService.incrementViewCount(ViewDomain.POST, id, userId, ip);

        return PostDto.DetailResponse.from(post);
    }

    /**
     * 글 작성
     */
    @Transactional
    public Long create(PostDto.CreateRequest request, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RestException(ErrorCode.USER_NOT_FOUND));

        Post post = Post.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .user(user)
                .build();

        return postRepository.save(post).getId();
    }

    /**
     * 글 목록 조회(커버링 인덱스)
     */
    public Page<PostDto.ListResponse> findAll(Pageable pageable) {
        return postQueryRepository.findAllPaged(pageable)
                .map(PostDto.ListResponse::from);
    }

    /**
     * 글 수정
     */
    @Transactional
    public void update(Long id, PostDto.CreateRequest request, String email) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RestException(ErrorCode.POST_NOT_FOUND));

        // 권한 확인: 작성자와 수정 요청자가 일치하는지 검증
        validateAuthor(post, email);

        post.update(request.getTitle(), request.getContent());
    }

    /**
     * 글 삭제
     */
    @Transactional
    public void delete(Long id, String email) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RestException(ErrorCode.POST_NOT_FOUND));

        validateAuthor(post, email);

        postRepository.delete(post);
    }

    private void validateAuthor(Post post, String email) {
        if (!post.getUser().getEmail().equals(email)) {
            throw new RestException(ErrorCode.POST_FORBIDDEN);
        }
    }
}
