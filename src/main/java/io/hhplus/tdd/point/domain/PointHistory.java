package io.hhplus.tdd.point.domain;

import io.hhplus.tdd.point.enums.TransactionType;
import io.hhplus.tdd.point.repository.PointHistoryRepository;

import java.util.List;

public record PointHistory(
        long id,
        long userId,
        long amount,
        TransactionType type,
        long updateMillis
) {
    // point 히스토리 조회 메서드
    public static List<PointHistory> findAllByUserId(long userId, PointHistoryRepository pointHistoryRepository) {
        return pointHistoryRepository.findAllByUserId(userId);
    }

    // PointHistory를 저장하는 메서드
    public void save(PointHistoryRepository pointHistoryRepository) {
        pointHistoryRepository.save(this.userId, this.amount, this.type, this.updateMillis);
    }

    // 포인트 히스토리 생성 로직을 책임지도록 팩토리 메서드 추가
    public static PointHistory create(long userId, long amount, TransactionType type) {
        return new PointHistory(
                System.currentTimeMillis(),  // 고유 ID를 시간으로 임시 생성 (필요에 따라 수정 가능)
                userId,
                amount,
                type,
                System.currentTimeMillis()
        );
    }
}
