package velog.velog.domain.post.repository;

import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import velog.velog.domain.post.entity.Post;

import java.util.List;

import static velog.velog.domain.post.entity.QPost.post;

@Repository
@RequiredArgsConstructor
public class PostQueryRepository {

    private final JPAQueryFactory queryFactory;

    public Page<Post> findAllPaged(Pageable pageable) {
        // 1. 커버링 인덱스 ID만 조회
        List<Long> ids = queryFactory
                .select(post.id)
                .from(post)
                .orderBy(post.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        if (ids.isEmpty()) {
            return Page.empty();
        }

        // 2. ID로 Fetch Join
        List<Post> content = queryFactory
                .selectFrom(post)
                .leftJoin(post.user).fetchJoin()
                .where(post.id.in(ids))
                .orderBy(post.id.desc())
                .fetch();

        // 3. Count 쿼리 분리
        JPAQuery<Long> countQuery = queryFactory
                .select(post.count())
                .from(post);

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }
}
