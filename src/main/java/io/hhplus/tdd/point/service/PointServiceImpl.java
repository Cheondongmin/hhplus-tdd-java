package io.hhplus.tdd.point.service;

import io.hhplus.tdd.point.domain.UserPoint;
import io.hhplus.tdd.point.repository.UserPointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PointServiceImpl implements PointService {
    private final UserPointRepository userPointRepository;

    // readOnly
    @Override
    public UserPoint getUserPoint(long id) {
        return UserPoint.findById(id, userPointRepository);
    }
}

