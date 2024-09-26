package io.hhplus.tdd.point.service;

import io.hhplus.tdd.point.domain.PointHistory;
import io.hhplus.tdd.point.domain.UserPoint;
import io.hhplus.tdd.point.enums.TransactionType;
import io.hhplus.tdd.point.lock.PointServiceLock;
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
    private final PointServiceLock pointServiceLock;

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

    @Override
    public UserPoint chargeUserPoint(long id, long amount) {
        pointServiceLock.lock();
        try {
            // 유저 포인트 조회를 UserPoint 객체로 위임
            UserPoint userPoint = UserPoint.findById(id, userPointRepository);

            // 포인트 충전 로직을 UserPoint 객체로 위임
            UserPoint updatedUserPoint = userPoint.addPoints(amount);

            // 포인트 히스토리 저장
            PointHistory pointHistory = PointHistory.create(id, amount, TransactionType.CHARGE);
            pointHistory.save(pointHistoryRepository);

            // 업데이트된 포인트 저장
            userPointRepository.saveOrUpdate(id, updatedUserPoint.point());

            return updatedUserPoint;
        } finally {
            pointServiceLock.unLock();
        }
    }

    @Override
    public UserPoint useUserPoint(long id, long amount) {
        pointServiceLock.lock();
        try {
            // 유저 포인트 조회를 UserPoint 객체로 위임
            UserPoint userPoint = UserPoint.findById(id, userPointRepository);

            // 포인트 사용 로직을 UserPoint 객체로 위임
            UserPoint updatedUserPoint = userPoint.subtractPoints(amount);

            // 포인트 히스토리 저장
            PointHistory pointHistory = PointHistory.create(id, amount, TransactionType.USE);
            pointHistory.save(pointHistoryRepository);

            // 업데이트된 포인트 저장
            userPointRepository.saveOrUpdate(id, updatedUserPoint.point());

            return updatedUserPoint;
        } finally {
            pointServiceLock.unLock();
        }
    }
}

