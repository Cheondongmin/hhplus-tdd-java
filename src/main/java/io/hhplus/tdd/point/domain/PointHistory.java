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
}
