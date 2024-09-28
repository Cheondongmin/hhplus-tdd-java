package io.hhplus.tdd.point.lock;

import org.springframework.stereotype.Component;

import java.util.concurrent.locks.ReentrantLock;

@Component
public class PointServiceLock {
    private final ReentrantLock lock = new ReentrantLock(true); // 공정 모드 활성화

    public void lock() {
        lock.lock(); // 공정 모드에서는 먼저 대기한 스레드가 우선권을 가짐
    }

    public void unLock() {
        lock.unlock();
    }
}
