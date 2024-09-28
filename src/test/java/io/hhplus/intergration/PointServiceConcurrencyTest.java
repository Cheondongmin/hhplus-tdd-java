package io.hhplus.intergration;

import io.hhplus.tdd.TddApplication;
import io.hhplus.tdd.point.domain.UserPoint;
import io.hhplus.tdd.point.repository.UserPointRepository;
import io.hhplus.tdd.point.service.PointService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = TddApplication.class)
public class PointServiceConcurrencyTest {

    @Autowired
    private PointService pointService; // 실제 서비스 사용

    @Autowired
    private UserPointRepository userPointRepository; // 실제 리포지토리 사용

    // 동시성 포인트 충전 테스트
    @Test
    void 동시성_포인트_충전_테스트() throws InterruptedException {
        // given
        long userId = 1L;
        long initialAmount = 1000L;
        long chargeAmount = 100L;
        int threadCount = 20; // 20개의 스레드를 통해 포인트 충전

        // 미리 유저 포인트를 저장
        userPointRepository.saveOrUpdate(userId, initialAmount);

        // 스레드 풀 생성 및 CountDownLatch 설정
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount); // 모든 스레드가 작업을 마칠 때까지 대기

        // when: 여러 스레드가 동시에 포인트 충전을 요청
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    pointService.chargeUserPoint(userId, chargeAmount);  // 각 스레드가 포인트를 충전
                } finally {
                    latch.countDown(); // 스레드 작업이 끝날 때 latch 카운트 감소
                }
            });
        }

        // 모든 스레드가 끝날 때까지 대기
        latch.await();

        // then: 최종 포인트 확인
        UserPoint resultUserPoint = userPointRepository.findById(userId);
        assertEquals(initialAmount + (chargeAmount * threadCount), resultUserPoint.point());

        // 스레드 종료
        executorService.shutdown();
    }

    // 동시성 포인트 사용 테스트
    @Test
    void 동시성_포인트_사용_테스트() throws InterruptedException {
        // given
        long userId = 1L;
        long initialAmount = 1000L;
        long useAmount = 100L;
        int threadCount = 11; // 포인트 총액 이상을 사용하려는 11개의 스레드

        // 미리 유저 포인트를 저장
        userPointRepository.saveOrUpdate(userId, initialAmount);

        // 스레드 풀 생성 및 CountDownLatch 설정
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount); // 모든 스레드가 작업을 마칠 때까지 대기

        List<Exception> exceptions = new ArrayList<>(); // 예외를 저장할 리스트

        // when: 여러 스레드가 동시에 포인트 사용을 요청
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    pointService.useUserPoint(userId, useAmount);  // 각 스레드가 포인트를 사용
                } catch (Exception e) {
                    exceptions.add(e); // 예외가 발생한 스레드의 예외를 리스트에 추가
                } finally {
                    latch.countDown(); // 스레드 작업이 끝날 때 latch 카운트 감소
                }
            });
        }

        // 모든 스레드가 끝날 때까지 대기
        latch.await();

        // then: 최종 포인트 확인 및 예외 처리
        UserPoint resultUserPoint = userPointRepository.findById(userId);

        // 총 포인트 사용 가능 수량만큼만 사용했는지 확인
        assertEquals(0, resultUserPoint.point());

        // 1개의 스레드는 잔여 포인트 부족으로 인해 예외가 발생해야 함
        assertEquals(1, exceptions.size());
        assertInstanceOf(IllegalArgumentException.class, exceptions.get(0));
        assertEquals("잔여 포인트가 부족합니다.", exceptions.get(0).getMessage());

        // 스레드 종료
        executorService.shutdown();
    }

    // 포인트 충전 및 사용 동시성 테스트
    @Test
    void 포인트_충전_및_사용_동시성_테스트() throws InterruptedException {
        // given
        long userId = 1L;
        long initialAmount = 5000L;
        long chargeAmount = 100L;
        long useAmount = 50L;
        int threadCount = 20; // 20개의 스레드에서 동시에 충전과 사용 수행

        // 미리 유저 포인트를 저장
        userPointRepository.saveOrUpdate(userId, initialAmount);

        // 스레드 풀 생성 및 CountDownLatch 설정
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount); // 모든 스레드가 작업을 마칠 때까지 대기

        List<Exception> exceptions = new ArrayList<>(); // 예외를 저장할 리스트

        // when: 여러 스레드가 동시에 포인트 충전과 사용을 요청
        for (int i = 0; i < threadCount; i++) {
            int finalI = i;
            executorService.submit(() -> {
                try {
                    if (finalI % 2 == 0) {
                        // 짝수 스레드는 포인트 충전
                        pointService.chargeUserPoint(userId, chargeAmount);
                    } else {
                        // 홀수 스레드는 포인트 사용
                        pointService.useUserPoint(userId, useAmount);
                    }
                } catch (Exception e) {
                    exceptions.add(e); // 예외가 발생하면 리스트에 추가
                } finally {
                    latch.countDown(); // 스레드 작업이 끝날 때 latch 카운트 감소
                }
            });
        }

        // 모든 스레드가 끝날 때까지 대기
        latch.await();

        // then: 최종 포인트 확인
        UserPoint resultUserPoint = userPointRepository.findById(userId);

        // 충전과 사용의 예상 결과 계산
        long expectedPoints = initialAmount + (chargeAmount * (threadCount / 2)) - (useAmount * (threadCount / 2));
        assertEquals(expectedPoints, resultUserPoint.point());

        // 포인트가 부족해서 발생한 예외가 없었는지 예외체크
        assertTrue(exceptions.isEmpty(), "발생된 예외: " + exceptions);

        // 스레드 종료
        executorService.shutdown();
    }
}
