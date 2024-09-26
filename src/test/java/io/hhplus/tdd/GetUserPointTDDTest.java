package io.hhplus.tdd;

import io.hhplus.tdd.point.domain.UserPoint;
import io.hhplus.tdd.point.service.PointService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

public class GetUserPointTDDTest {
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
    void 특정유저의_포인트조회_성공케이스() {
        // given
        long userId = 1L;

        // 특정 유저의 포인트를 조회하는 객체를 생성합니다.
        // when
        when(pointService.getUserPoint(userId)).
                thenReturn(userPoint);

        // 실제 테스트 실행
        UserPoint result = pointService.getUserPoint(userId);

        // then
        assertNotNull(result); // 결과가 null이 아닌지 확인
        assertEquals(userId, result.id()); // 유저 ID가 맞는지 확인
        assertEquals(100, result.point()); // 포인트가 100인지 확인
    }
}
