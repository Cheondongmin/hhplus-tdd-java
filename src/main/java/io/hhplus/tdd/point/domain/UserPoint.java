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

    // 포인트 충전 및 검증 로직
    public UserPoint addPoints(long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("충전 포인트는 0보다 커야 합니다.");
        } else if (amount > 99999) {
            throw new IllegalArgumentException("충전 금액의 최대한도는 99999 입니다.");
        }

        long newPoint = this.point + amount;
        if (newPoint < 0) {
            throw new IllegalArgumentException("포인트 합계가 잘못되었습니다. 비정상적인 금액을 충전하려고 합니다.");
        }

        return new UserPoint(this.id, newPoint, System.currentTimeMillis());
    }

    // 포인트 차감 로직 (차감할 때 유효성 검증 포함)
    public UserPoint subtractPoints(long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("사용할 포인트는 0보다 커야 합니다.");
        }

        long newPoint = this.point - amount;

        if (newPoint < 0) {
            throw new IllegalArgumentException("잔여 포인트가 부족합니다.");
        }
        return new UserPoint(this.id, newPoint, System.currentTimeMillis());
    }
}
