package io.hhplus.unit;

import io.hhplus.tdd.point.domain.UserPoint;
import io.hhplus.tdd.point.enums.TransactionType;
import io.hhplus.tdd.point.lock.PointServiceLock;
import io.hhplus.tdd.point.repository.PointHistoryRepository;
import io.hhplus.tdd.point.repository.UserPointRepository;
import io.hhplus.tdd.point.service.PointServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Mockito 확장을 통해 Mockito가 테스트에서 사용할 목업 객체를 주입해줄 수 있도록 설정
public class ChargeUserPointUnitTest {

    @Mock
    private UserPointRepository userPointRepository; // UserPointRepository를 목(mock) 객체로 만듦. 테스트에서 실제 구현이 아닌 목 객체를 사용하기 위함.

    @Mock
    private PointHistoryRepository pointHistoryRepository; // PointHistoryRepository도 목 객체로 만듦.

    @Mock
    private PointServiceLock pointServiceLock;  // PointServiceLock을 Mock 객체로 추가

    @InjectMocks
    private PointServiceImpl pointService; // 목 객체들을 주입받을 구현체 객체를 생성. 실제 테스트할 대상 클래스.

    @Test
    void 포인트충전_성공케이스() {
        // given
        long userId = 1L;
        long currentAmount = 1000L;
        long chargeAmount = 500L;
        UserPoint userPoint = new UserPoint(userId, currentAmount, System.currentTimeMillis());
        UserPoint updatedUserPoint = new UserPoint(userId, currentAmount + chargeAmount, System.currentTimeMillis());

        // when
        when(userPointRepository.findById(eq(userId))).thenReturn(userPoint);  // Mock을 통해 기존 포인트 정보 반환
        when(userPointRepository.saveOrUpdate(eq(userId), eq(currentAmount + chargeAmount)))
                .thenReturn(updatedUserPoint);  // Mock을 통해 업데이트된 포인트 정보 반환

        // then
        UserPoint result = pointService.chargeUserPoint(userId, chargeAmount);

        // 충전 후 결과 검증
        assertNotNull(result);
        assertEquals(currentAmount + chargeAmount, result.point());  // 포인트 합계 검증

        // saveOrUpdate와 findById가 올바르게 호출되었는지 검증
        verify(userPointRepository).saveOrUpdate(eq(userId), eq(currentAmount + chargeAmount));
        verify(userPointRepository, times(1)).findById(eq(userId));

        // 포인트 히스토리가 올바르게 저장되었는지 검증
        verify(pointHistoryRepository).save(eq(userId), eq(chargeAmount), eq(TransactionType.CHARGE), anyLong());
    }

    @Test
    void 충전금액이_0_이하일때_예외케이스() {
        // given
        long userId = 1L;
        long chargeAmount = 0L;  // 유효하지 않은 충전 금액 (0 이하)

        UserPoint userPoint = new UserPoint(userId, 1000L, System.currentTimeMillis());

        // when
        when(userPointRepository.findById(eq(userId))).thenReturn(userPoint); // 유저가 존재한다고 가정

        // then
        Exception exception = assertThrows(IllegalArgumentException.class, () -> pointService.chargeUserPoint(userId, chargeAmount));

        assertEquals("충전 포인트는 0보다 커야 합니다.", exception.getMessage());

        // 충전 금액이 0 이하일 때 saveOrUpdate는 호출되지 않아야 함
        verify(userPointRepository, never()).saveOrUpdate(anyLong(), anyLong());
        verify(pointHistoryRepository, never()).save(anyLong(), anyLong(), any(), anyLong());
    }

    @Test
    void 포인트_충전금액이_최대한도_초과일때() {
        // given
        long userId = 1L;
        long chargeAmount = 100000L;  // 유효하지 않은 충전 금액 (0 이하)

        UserPoint userPoint = new UserPoint(userId, 1000L, System.currentTimeMillis());

        // when
        when(userPointRepository.findById(eq(userId))).thenReturn(userPoint); // 유저가 존재한다고 가정

        // then
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            pointService.chargeUserPoint(userId, chargeAmount);
        });

        assertEquals("충전 금액의 최대한도는 99999 입니다.", exception.getMessage());

        // 충전 금액이 0 이하일 때 saveOrUpdate는 호출되지 않아야 함
        verify(userPointRepository, never()).saveOrUpdate(anyLong(), anyLong());
        verify(pointHistoryRepository, never()).save(anyLong(), anyLong(), any(), anyLong());
    }

    @Test
    void 포인트합계가_음수일때_예외케이스() {
        // given
        long userId = 1L;
        long currentAmount = -2L;
        long chargeAmount = 1L;  // 포인트 합계가 음수가 될 수 있는 금액

        UserPoint userPoint = new UserPoint(userId, currentAmount, System.currentTimeMillis());

        // when
        when(userPointRepository.findById(eq(userId))).thenReturn(userPoint);  // 유저 포인트 정보를 반환

        // then
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            pointService.chargeUserPoint(userId, chargeAmount);
        });

        // 음수가 되기 전에
        assertEquals("포인트 합계가 잘못되었습니다. 비정상적인 금액을 충전하려고 합니다.", exception.getMessage());

        // 포인트 합계가 음수일 때 saveOrUpdate는 호출되지 않아야 함
        verify(userPointRepository, never()).saveOrUpdate(anyLong(), anyLong());
        verify(pointHistoryRepository, never()).save(anyLong(), anyLong(), any(), anyLong());
    }
}
