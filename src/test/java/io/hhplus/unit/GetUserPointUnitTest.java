package io.hhplus.unit;

import io.hhplus.tdd.point.domain.UserPoint;
import io.hhplus.tdd.point.repository.UserPointRepository;
import io.hhplus.tdd.point.service.PointServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class) // Mockito 확장을 통해 Mockito가 테스트에서 사용할 목업 객체를 주입해줄 수 있도록 설정
public class GetUserPointUnitTest {

    @Mock
    private UserPointRepository userPointRepository; // UserPointRepository를 목(mock) 객체로 만듦. 테스트에서 실제 구현이 아닌 목 객체를 사용하기 위함.

    @InjectMocks
    private PointServiceImpl pointService; // 목 객체들을 주입받을 구현체 객체를 생성. 실제 테스트할 대상 클래스.

    @Test
    void 회원조회_성공케이스() {
        // given
        long userId = 1L;
        long currentAmount = 1000L;
        UserPoint userPoint = new UserPoint(userId, currentAmount, System.currentTimeMillis());

        // when
        when(userPointRepository.findById(eq(userId))).thenReturn(userPoint); // Mock을 통해 유저 포인트 정보를 반환

        // then
        UserPoint result = pointService.getUserPoint(userId);

        // 회원 조회 후 결과 검증
        assertNotNull(result); // 결과가 null이 아님을 검증
        assertEquals(userId, result.id()); // 유저 ID가 일치하는지 검증
        assertEquals(currentAmount, result.point()); // 포인트 금액이 일치하는지 검증

        // findById가 올바르게 호출되었는지 검증
        verify(userPointRepository).findById(eq(userId));
    }
}
