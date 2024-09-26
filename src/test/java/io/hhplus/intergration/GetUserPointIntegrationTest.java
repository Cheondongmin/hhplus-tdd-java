package io.hhplus.intergration;

import io.hhplus.tdd.TddApplication;
import io.hhplus.tdd.point.domain.UserPoint;
import io.hhplus.tdd.point.repository.UserPointRepository;
import io.hhplus.tdd.point.service.PointService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = TddApplication.class) // Spring 컨텍스트를 로드하여 통합 테스트 수행
public class GetUserPointIntegrationTest {

    @Autowired
    private PointService pointService; // 실제 서비스 사용

    @Autowired
    private UserPointRepository userPointRepository; // 실제 리포지토리 사용

    @Test
    void 유저_포인트조회_성공케이스() {
        // given
        long userId = 1L;
        long currentAmount = 1000L;

        // 미리 유저 포인트를 저장
        userPointRepository.saveOrUpdate(userId, currentAmount);

        // when
        UserPoint resultUserPoint = pointService.getUserPoint(userId);

        // then
        assertNotNull(resultUserPoint);
        assertEquals(userId, resultUserPoint.id());
        assertEquals(currentAmount, resultUserPoint.point());
    }
}
