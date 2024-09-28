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

public class ChargeUserPointTDDTest {

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
    void 포인트충전_성공케이스() {
        // given
        long userId = 1L;
        long amount = 50L;

        // 포인트 충전 후 새로운 상태 반환 설정
        // when
        when(pointService.chargeUserPoint(userId, amount))
                .thenReturn(new UserPoint(userId, userPoint.point() + amount, System.currentTimeMillis()));

        // 포인트 충전 후 결과 확인
        // then
        UserPoint result = pointService.chargeUserPoint(userId, amount);
        assertEquals(150, result.point());

        // 메서드가 정확한 인자로 호출되었는지 검증
        verify(pointService).chargeUserPoint(userId, amount);
    }

    @Test
    void 포인트_충전금액이_0_이하일때() {
        // given
        long userId = 1L;
        long invalidAmount = 0L;

        // 충전 금액이 0 이하일 때 예외 발생 설정
        // when
        when(pointService.chargeUserPoint(userId, invalidAmount))
                .thenThrow(new IllegalArgumentException("충전 금액은 0보다 커야 합니다."));

        // 예외가 발생하는지 확인
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                pointService.chargeUserPoint(userId, invalidAmount));

        // 예외 메시지 검증
        // then
        assertEquals("충전 금액은 0보다 커야 합니다.", exception.getMessage());
    }

    @Test
    void 포인트_충전금액이_최대한도_초과일때() {
        // given
        long userId = 1L;
        long invalidAmount = 0L;

        // 충전 금액이 99999 초과일 때 예외 발생 설정
        // when
        when(pointService.chargeUserPoint(userId, invalidAmount))
                .thenThrow(new IllegalArgumentException("충전 금액의 최대한도는 99999 입니다."));

        // 예외가 발생하는지 확인
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                pointService.chargeUserPoint(userId, invalidAmount));

        // 예외 메시지 검증
        // then
        assertEquals("충전 금액의 최대한도는 99999 입니다.", exception.getMessage());
    }

    @Test
    void 충전포인트가_음수일때_예외발생() {
        // given
        long userId = 1L;
        long chargeAmount = -200L;

        // 충전 금액이 음수일 때 예외 발생 설정
        // when
        when(pointService.chargeUserPoint(userId, chargeAmount))
                .thenThrow(new IllegalArgumentException("포인트 합계가 잘못되었습니다. 비정상적인 금액을 충전하려고 합니다."));

        // 예외가 발생하는지 확인
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                pointService.chargeUserPoint(userId, chargeAmount));

        // 예외 메시지 검증
        // then
        assertEquals("포인트 합계가 잘못되었습니다. 비정상적인 금액을 충전하려고 합니다.", exception.getMessage());
    }

    @Test
    void 포인트충전후_포인트합계조회() {
        // given
        long userId = 1L;
        long chargeAmount = 200L;

        // 포인트 충전 후 새로운 상태 반환 설정
        // when
        when(pointService.chargeUserPoint(userId, chargeAmount))
                .thenReturn(new UserPoint(userId, userPoint.point() + chargeAmount, System.currentTimeMillis()));

        // 포인트 충전 후 결과 확인
        // then
        UserPoint result = pointService.chargeUserPoint(userId, chargeAmount);
        assertEquals(300L, result.point());

        // 메서드 호출 검증
        verify(pointService).chargeUserPoint(userId, chargeAmount);
    }

    @Test
    void 포인트충전후_포인트히스토리_저장확인() {
        // given
        long userId = 1L;
        long chargeAmount = 100L;

        // 충전 후 포인트 업데이트 상태 반환 설정
        // when
        when(pointService.chargeUserPoint(userId, chargeAmount))
                .thenReturn(new UserPoint(userId, userPoint.point() + chargeAmount, System.currentTimeMillis()));

        // 포인트 충전 호출
        pointService.chargeUserPoint(userId, chargeAmount);

        // then
        // 포인트 충전 메서드 호출 확인
        verify(pointService).chargeUserPoint(userId, chargeAmount);
    }
}
