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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Mockito 확장을 통해 Mockito가 테스트에서 사용할 목업 객체를 주입해줄 수 있도록 설정
public class UseUserPointUnitTest {

    @Mock
    private UserPointRepository userPointRepository; // UserPointRepository를 목(mock) 객체로 만듦. 테스트에서 실제 구현이 아닌 목 객체를 사용하기 위함.

    @Mock
    private PointHistoryRepository pointHistoryRepository; // PointHistoryRepository도 목 객체로 만듦.

    @Mock
    private PointServiceLock pointServiceLock;  // PointServiceLock을 Mock 객체로 추가

    @InjectMocks
    private PointServiceImpl pointService; // 목 객체들을 주입받을 구현체 객체를 생성. 실제 테스트할 대상 클래스.

    @Test
    void 포인트_사용시_성공케이스() {
        // given
        long userId = 1L;
        long currentAmount = 1000L;
        long useAmount = 500L;
        UserPoint userPoint = new UserPoint(userId, currentAmount, System.currentTimeMillis());
        UserPoint updatedUserPoint = new UserPoint(userId, currentAmount - useAmount, System.currentTimeMillis());

        // when
        when(userPointRepository.findById(eq(userId))).thenReturn(userPoint);  // Mock을 통해 기존 포인트 정보 반환
        when(userPointRepository.saveOrUpdate(eq(userId), eq(currentAmount - useAmount)))
                .thenReturn(updatedUserPoint);  // Mock을 통해 업데이트된 포인트 정보 반환

        // 포인트 사용 로직으로 변경
        UserPoint result = pointService.useUserPoint(userId, useAmount);

        // 사용 후 결과 검증
        assertNotNull(result);
        assertEquals(currentAmount - useAmount, result.point());  // 포인트 차감 검증

        // saveOrUpdate와 findById가 올바르게 호출되었는지 검증
        verify(userPointRepository).saveOrUpdate(eq(userId), eq(currentAmount - useAmount));
        verify(userPointRepository, times(1)).findById(eq(userId));

        // 포인트 히스토리가 올바르게 저장되었는지 검증
        verify(pointHistoryRepository).save(eq(userId), eq(useAmount), eq(TransactionType.USE), anyLong());
    }

    @Test
    void 사용금액이_0_이하일때_예외케이스() {
        // given
        long userId = 1L;
        long useAmount = 0L;  // 유효하지 않은 사용 금액 (0 이하)

        UserPoint userPoint = new UserPoint(userId, 1000L, System.currentTimeMillis());

        // when
        when(userPointRepository.findById(eq(userId))).thenReturn(userPoint); // 유저가 존재한다고 가정

        // then
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            pointService.useUserPoint(userId, useAmount);
        });

        assertEquals("사용할 포인트는 0보다 커야 합니다.", exception.getMessage());

        // 사용 금액이 0 이하일 때 saveOrUpdate는 호출되지 않아야 함
        verify(userPointRepository, never()).saveOrUpdate(anyLong(), anyLong());
        verify(pointHistoryRepository, never()).save(anyLong(), anyLong(), any(), anyLong());
    }

    @Test
    void 사용_후_포인트합계가_음수일때_예외케이스() {
        // given
        long userId = 1L;
        long currentAmount = 500L;
        long useAmount = 600L;  // 사용 후 잔액이 음수가 되는 금액

        UserPoint userPoint = new UserPoint(userId, currentAmount, System.currentTimeMillis());

        // when
        when(userPointRepository.findById(eq(userId))).thenReturn(userPoint);  // 유저 포인트 정보를 반환

        // then
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            pointService.useUserPoint(userId, useAmount);
        });

        // 잔액이 음수일 때 예외 메시지를 검증
        assertEquals("잔여 포인트가 부족합니다.", exception.getMessage());

        // 포인트 합계가 음수가 될 때 saveOrUpdate는 호출되지 않아야 함
        verify(userPointRepository, never()).saveOrUpdate(anyLong(), anyLong());
        verify(pointHistoryRepository, never()).save(anyLong(), anyLong(), any(), anyLong());
    }

    @Test
    void 유효하지_않은_사용금액으로_포인트를_사용할때() {
        // given
        long userId = 1L;
        long currentAmount = 1000L;
        long useAmount = -500L;  // 유효하지 않은 사용 금액 (음수)

        UserPoint userPoint = new UserPoint(userId, currentAmount, System.currentTimeMillis());

        // when
        when(userPointRepository.findById(eq(userId))).thenReturn(userPoint);  // 유저가 존재한다고 가정

        // then
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            pointService.useUserPoint(userId, useAmount);
        });

        // 사용 금액이 음수일 때 예외 메시지를 검증
        assertEquals("사용할 포인트는 0보다 커야 합니다.", exception.getMessage());

        // 유효하지 않은 사용 금액일 때 saveOrUpdate는 호출되지 않아야 함
        verify(userPointRepository, never()).saveOrUpdate(anyLong(), anyLong());
        verify(pointHistoryRepository, never()).save(anyLong(), anyLong(), any(), anyLong());
    }
}
