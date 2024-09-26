package io.hhplus.tdd;

import io.hhplus.tdd.point.domain.PointHistory;
import io.hhplus.tdd.point.enums.TransactionType;
import io.hhplus.tdd.point.service.PointService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

public class GetUserPointHistoryTDDTest {
    @Mock
    private PointService pointService;
    private final List<PointHistory> pointHistoryList = new ArrayList<>();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // 히스토리 리스트 첫번째에 100을 충전한 내역이 있는 내역 추가
        pointHistoryList.add(new PointHistory(1L, 1L, 100, TransactionType.CHARGE, System.currentTimeMillis()));
    }

    @Test
    void 유저의_포인트_이용내역_조회() {
        // given
        long userId = 1L;

        // 특정 유저의 포인트 히스토리를 조회하는 객체를 생성합니다.
        // when
        when(pointService.getUserPointHistory(userId)).
                thenReturn(pointHistoryList);

        // 실제 테스트 실행
        List<PointHistory> result = pointService.getUserPointHistory(userId);

        // then
        assertNotNull(result); // 결과가 null이 아닌지 확인
        assertEquals(userId, result.get(0).id()); // 유저 ID가 맞는지 확인
        assertEquals(100, result.get(0).amount()); // 포인트가 100인지 확인
    }
}
