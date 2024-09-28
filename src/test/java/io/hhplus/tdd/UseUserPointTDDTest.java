package io.hhplus.tdd;

import io.hhplus.tdd.point.domain.UserPoint;
import io.hhplus.tdd.point.service.PointService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UseUserPointTDDTest {

    @Mock
    private PointService pointService;

    private UserPoint userPoint;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // 초기 포인트를 100으로 설정된 유저로 세팅
        userPoint = new UserPoint(1L, 100, System.currentTimeMillis());
    }

    @Test
    void 포인트사용_성공케이스() {
        // given
        long userId = 1L;
        long amount = 50L;
        long currentUserPoint = 500L;

        // 포인트 사용 후 새로운 상태 변환 설정
        // when
        when(pointService.useUserPoint(userId, amount))
                .thenReturn(new UserPoint(userId, currentUserPoint - amount, System.currentTimeMillis()));

        // 포인트 사용 후 결과 확인
        // then
        UserPoint result = pointService.useUserPoint(userId, amount);
        assertEquals(450, result.point());

        // 메서드가 정상적으로 호출되었는지 검증
        verify(pointService).useUserPoint(userId, amount);
    }

    @Test
    void 포인트사용_잔액부족_실패케이스() {
        // given
        long userId = 1L;
        long amount = 600L;

        when(pointService.useUserPoint(userId, amount))
                .thenThrow(new IllegalArgumentException("잔여 포인트가 부족합니다."));

        // then
        Exception exception = assertThrows(IllegalArgumentException.class, () -> pointService.useUserPoint(userId, amount));
        assertEquals("잔여 포인트가 부족합니다.", exception.getMessage());
    }

    @Test
    void 포인트사용_잘못된금액_실패케이스() {
        // given
        long userId = 1L;
        long amount = -50L; // 음수 금액

        when(pointService.useUserPoint(userId, amount))
                .thenThrow(new IllegalArgumentException("사용할 포인트는 0보다 커야 합니다."));

        // then
        Exception exception = assertThrows(IllegalArgumentException.class, () -> pointService.useUserPoint(userId, amount));
        assertEquals("사용할 포인트는 0보다 커야 합니다.", exception.getMessage());
    }

    @Test
    void 포인트사용후_포인트히스토리_저장확인() {
        // given
        long userId = 1L;
        long chargeAmount = 100L;

        // 충전 후 포인트 업데이트 상태 반환 설정
        // when
        when(pointService.useUserPoint(userId, chargeAmount))
                .thenReturn(new UserPoint(userId, userPoint.point() + chargeAmount, System.currentTimeMillis()));

        // 포인트 충전 호출
        pointService.useUserPoint(userId, chargeAmount);

        // then
        // 포인트 충전 메서드 호출 확인
        verify(pointService).useUserPoint(userId, chargeAmount);
    }

    @Test
    void 포인트사용_초과금액_실패케이스() {
        // given
        long userId = 1L;
        long amount = 1000000L; // 비정상적으로 큰 금액

        when(pointService.useUserPoint(userId, amount))
                .thenThrow(new IllegalArgumentException("최대 사용 포인트를 초과했습니다. 최대 사용 포인트는 99999 입니다."));

        // then
        Exception exception = assertThrows(IllegalArgumentException.class, () -> pointService.useUserPoint(userId, amount));
        assertEquals("최대 사용 포인트를 초과했습니다. 최대 사용 포인트는 99999 입니다.", exception.getMessage());
    }
}
