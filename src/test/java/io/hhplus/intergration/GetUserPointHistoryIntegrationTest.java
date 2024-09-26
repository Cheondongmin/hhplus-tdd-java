package io.hhplus.intergration;

import io.hhplus.tdd.TddApplication;
import io.hhplus.tdd.point.domain.PointHistory;
import io.hhplus.tdd.point.enums.TransactionType;
import io.hhplus.tdd.point.repository.PointHistoryRepository;
import io.hhplus.tdd.point.service.PointService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = TddApplication.class) // Spring 컨텍스트를 로드하여 통합 테스트 수행
public class GetUserPointHistoryIntegrationTest {

    @Autowired
    private PointService pointService; // 실제 서비스 사용

    @Autowired
    private PointHistoryRepository pointHistoryRepository; // 실제 리포지토리 사용

    @Test
    void 유저의_포인트_이용내역_조회() {
        // given
        long userId = 1L;
        long ammount = 100L;

        // 미리 유저의 포인트 내역을 저장
        pointHistoryRepository.save(userId, ammount, TransactionType.CHARGE, System.currentTimeMillis());

        // when
        List<PointHistory> pointHistoryList = pointService.getUserPointHistory(userId);

        // then
        assertNotNull(pointHistoryList);
        assertEquals(ammount, pointHistoryList.get(0).amount());
        assertEquals(userId, pointHistoryList.get(0).id());
        assertEquals(TransactionType.CHARGE, pointHistoryList.get(0).type());
    }
}
