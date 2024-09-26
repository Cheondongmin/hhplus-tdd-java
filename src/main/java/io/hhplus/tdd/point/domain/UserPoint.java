package io.hhplus.tdd.point.domain;

import io.hhplus.tdd.point.repository.UserPointRepository;

public record UserPoint(
        long id,
        long point,
        long updateMillis
) {

    public static UserPoint empty(long id) {
        return new UserPoint(id, 0, System.currentTimeMillis());
    }

    // UserPoint 조회 로직 메서드
    public static UserPoint findById(long id, UserPointRepository userPointRepository) {
        UserPoint userPoint = userPointRepository.findById(id);
        if (userPoint == null) {
            throw new IllegalArgumentException("유저가 존재하지 않습니다.");
        }
        return userPoint;
    }
}
