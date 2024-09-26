package io.hhplus.tdd.point.service;

import io.hhplus.tdd.point.domain.PointHistory;
import io.hhplus.tdd.point.domain.UserPoint;
import io.hhplus.tdd.point.repository.PointHistoryRepository;
import io.hhplus.tdd.point.repository.UserPointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PointServiceImpl implements PointService {
    private final UserPointRepository userPointRepository;
    private final PointHistoryRepository pointHistoryRepository;

    // readOnly
    @Override
    public UserPoint getUserPoint(long id) {
        return UserPoint.findById(id, userPointRepository);
    }

    // readOnly
    @Override
    public List<PointHistory> getUserPointHistory(long id) {
        // 먼저 기존 회원이 존재하는지 먼저 조회
        UserPoint userPoint = UserPoint.findById(id, userPointRepository);

        // 조회가 확인되었다면 해당 회원의 정보로 포인트 히스토리 정보 조회
        return PointHistory.findAllByUserId(userPoint.id(), pointHistoryRepository);
    }
}

