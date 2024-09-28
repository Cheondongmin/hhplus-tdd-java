package io.hhplus.intergration;

import io.hhplus.tdd.TddApplication;
import io.hhplus.tdd.point.domain.PointHistory;
import io.hhplus.tdd.point.domain.UserPoint;
import io.hhplus.tdd.point.enums.TransactionType;
import io.hhplus.tdd.point.repository.PointHistoryRepository;
import io.hhplus.tdd.point.repository.UserPointRepository;
import io.hhplus.tdd.point.service.PointService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = TddApplication.class) // Spring 컨텍스트를 로드하여 통합 테스트 수행
public class UseUserPointIntegrationTest {

    @Autowired
    private PointService pointService; // 실제 서비스 사용

    @Autowired
    private UserPointRepository userPointRepository; // 실제 리포지토리 사용

    @Autowired
    private PointHistoryRepository pointHistoryRepository; // 실제 리포지토리 사용

    @Test
    void 포인트사용_성공케이스() {
        // given
        long userId = 1L;
        long currentAmount = 1000L;
        long useAmount = 100L;

        // 미리 유저 포인트를 저장
        userPointRepository.saveOrUpdate(userId, currentAmount);

        // when
        UserPoint updatedUserPoint = pointService.useUserPoint(userId, useAmount);

        // then
        assertNotNull(updatedUserPoint);
        assertEquals(currentAmount - useAmount, updatedUserPoint.point());

        // 실제 DB에 저장된 포인트 확인
        UserPoint resultUserPoint = userPointRepository.findById(userId);
        assertEquals(currentAmount - useAmount, resultUserPoint.point());

        // 포인트 히스토리도 실제로 저장되었는지 확인
        List<PointHistory> pointHistoryList = pointHistoryRepository.findAllByUserId(userId);
        assertEquals(1, pointHistoryList.size());
        assertEquals(useAmount, pointHistoryList.get(0).amount());
        assertEquals(TransactionType.USE, pointHistoryList.get(0).type());
    }

    @Test
    void 사용금액이_0_이하일때_예외케이스() {
        // given
        long userId = 1L;
        long useAmount = 0L;  // 유효하지 않은 사용 금액 (0 이하)

        // 미리 유저 포인트를 저장
        userPointRepository.saveOrUpdate(userId, 1000L);

        // when
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            pointService.useUserPoint(userId, useAmount);
        });

        // then
        assertEquals("사용할 포인트는 0보다 커야 합니다.", exception.getMessage());
    }

    @Test
    void 사용후_포인트합계가_음수일때_예외케이스() {
        // given
        long userId = 1L;
        long currentAmount = 500L;
        long useAmount = 600L;  // 사용 후 잔액이 음수가 되는 금액

        // 미리 유저 포인트를 저장
        userPointRepository.saveOrUpdate(userId, currentAmount);

        // when
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            pointService.useUserPoint(userId, useAmount);
        });

        // then
        assertEquals("잔여 포인트가 부족합니다.", exception.getMessage());
    }
}
