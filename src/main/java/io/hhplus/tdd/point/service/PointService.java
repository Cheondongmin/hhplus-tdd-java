package io.hhplus.tdd.point.service;

import io.hhplus.tdd.point.domain.PointHistory;
import io.hhplus.tdd.point.domain.UserPoint;

import java.util.List;

public interface PointService {
    UserPoint getUserPoint(long id);

    List<PointHistory> getUserPointHistory(long id);

    UserPoint chargeUserPoint(long id, long amount);

    UserPoint useUserPoint(long id, long amount);
}
