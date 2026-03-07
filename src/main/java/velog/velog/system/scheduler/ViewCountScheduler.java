package velog.velog.system.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import velog.velog.common.enums.ViewDomain;
import velog.velog.common.service.RedisViewService;
import velog.velog.domain.post.repository.PostRepository;

import java.util.Map;
import java.util.function.BiConsumer;

@Slf4j
@Component
@RequiredArgsConstructor
public class ViewCountScheduler {

    private final RedisViewService redisViewService;
    private final PostRepository postRepository;

    private static final int BATCH_SIZE = 5000;

    /**
     * 게시글 조회수 동기화
     */
    @Scheduled(cron = "0 0/1 * * * *") // 1분마다 실행
    public void syncPostViews() {
        log.info("[Scheduler] Starting Post View Count Sync...");
        try {
            syncViews(ViewDomain.POST, postRepository::increaseViewCount);
            log.info("[Scheduler] Post View Sync Completed");
        } catch (Exception e) {
            log.error("[Scheduler] Post View Sync Failed", e);
        }
    }

    /**
     * 공통 동기화 로직
     */
    private void syncViews(ViewDomain domain, BiConsumer<Long, Long> dbUpdater) {
        // SEQ 1. Redis에서 데이터 가져오기
        Map<Long, Long> counts = redisViewService.getAndFlushViewCount(domain, BATCH_SIZE);
        if (counts.isEmpty()) return;

        counts.forEach((id, count) -> {
            if (count > 0) {
                try {
                    // SEQ 2. DB 업데이트
                    dbUpdater.accept(id, count);

                    // SEQ 3. Redis 수치 차감
                    redisViewService.decrementCount(domain, id, count);
                } catch (Exception e) {
                    log.error("View Sync Error ID: {} ({})", id, domain, e);
                }
            } else {
                // SEQ 4. 0 이하의 불필요한 키 정리
                redisViewService.deleteViewCountKey(domain, id);
            }
        });
    }
}
