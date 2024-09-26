
# 동시성 제어 방식 분석 보고서

## 1. 동시성 문제의 발생 원인

다수의 스레드가 동시에 같은 자원에 접근하거나 수정하려 할 때, 동시성 문제가 발생할 수 있습니다.
이번 과제에서는 포인트 충전 및 사용 로직에서 동일한 사용자가 여러 번 충전 또는 사용을 시도하는 상황을 처리해야 했습니다.
이러한 상황에서 자원 무결성을 보장하지 않으면 예상치 못한 데이터 손실이나 중복 처리 등의 문제가 발생할 수 있습니다.

---

## 2. 동시성을 문제를 해결한 방법

여러 수강생 분들께서 Synchronized를 많이 고민하셨던 것 같습니다.
저 또한 고민을 많이 했었구요. 하지만 둘 중 고민끝에
**ReentrantLock**을 통해 동시성 문제를 해결하였습니다. 이 방식은 다음과 같은 이점을 제공해줍니다:

### 2.1 세밀한 동시성 제어
ReentrantLock은 Synchronized 키워드보다 더 세밀한 제어를 가능하게 합니다.
락을 명시적으로 잠그고 해제할 수 있기 때문에 더 복잡한 로직에 유연하게 대응할 수 있습니다.

### 2.2 공정성 보장
ReentrantLock은 공정 모드를 제공하여, 먼저 대기한 스레드가 락을 선점할 수 있게 합니다.
이를 통해 특정 스레드가 계속 락을 선점하여 다른 스레드가 락을 얻지 못하는 기아(starvation) 상태를 방지할 수 있습니다.
순서또한 보장되었습니다.

---

## 3. ReentrantLock과 Synchronized 비교

| **특징**              | **ReentrantLock**                                         | **Synchronized**                                          |
|-----------------------|-----------------------------------------------------------|-----------------------------------------------------------|
| **공정성 보장**       | 공정 모드를 통해 대기 중인 스레드가 우선 락을 획득 가능       | 기본적으로 공정성을 보장하지 않음                         |
| **락 해제 시점**      | 명시적으로 `unlock()` 호출 필요                             | Synchronized 블록을 벗어나면 자동으로 해제                 |
| **락 대기 시간 제어** | `tryLock()` 사용 가능 (락 획득 실패 시 타임아웃 설정 가능)      | 대기 시간 제어 불가능                                      |
| **성능**              | 다양한 동시성 상황에서 제어 가능하나 약간의 성능 저하 발생 가능| 경량이지만 복잡한 동시성 처리를 다루기 어려울 수 있음      |

---

## 4. ReentrantLock 사용 이유

초기에는 BlockingQueue와 SingleThreadExecutor를 활용하여 작업을 순차적으로 처리하는 방식으로 해결을 시도하려 했습니다.
그러나 이 방법은 동작이 복잡하고 원하는 퍼포먼스를 얻기 어려웠습니다.
(제가 아직 해당 로직에 대한 이해도가 많이 부족해서 더 좋은 코드를 짤 자신도 없었구요…)

그래서 저도 Synchronized와 ReentrantLock 중 하나를 고민하였고
사용이 쉽고, 원하는 기능을 보장해주는 ReentrantLock을 사용하였습니다.

---

## 5. 테스트 방법

동시성 제어가 잘 적용되었는지 검증하기 위해 다음과 같은 테스트를 수행했습니다.

### 5.1 포인트 충전 테스트

20개의 스레드가 각각 100 포인트씩 충전하는 테스트입니다. 각 스레드는 동일한 사용자를 대상으로 동시다발적으로 충전을 시도하며, 충전 후의 포인트가 예상치와 동일한지 검증합니다.

```java
@Test
void 동시성_포인트_충전_테스트() {
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
```

### 5.2 포인트 사용 테스트

11개의 스레드가 각각 100 포인트씩 사용하는 테스트입니다. 포인트 총액은 1000 포인트이므로, 한 스레드에서 잔여 포인트 부족으로 예외가 발생하는지 확인했습니다.

```java
@Test
void 동시성_포인트_사용_테스트() {
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
```

---

### 5.3 포인트 충전 및 사용 동시성 테스트

포인트 충전과 사용을 동시에 테스트하기 위해, 20개의 스레드 중 절반은 포인트를 충전하고, 나머지 절반은 포인트를 사용하도록 설정하였습니다.

```java
@Test
void 포인트_충전_및_사용_동시성_테스트() {
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
```
---
## 6. 결론
처음에는 BlockingQueue와 SingleThreadExecutor를 사용하여 작업을 순차적으로 처리하는 방식을 시도했습니다. 하지만 이 방식은 복잡하고, 로직을 더 잘 이해하고 개선할 자신도 부족했습니다. 그래서 간결하고 성능이 우수하며 필요한 기능을 제공해주는 ReentrantLock으로 전환하게 되었습니다.

ReentrantLock은 synchronized와 비교할 때 더 유연한 동시성 제어를 제공했습니다. synchronized는 간단하지만, 잠금 해제 시점과 공정성을 제어할 수 없고, 락이 걸리는 범위도 코드 블록 전체로 한정됩니다. 반면, ReentrantLock은 락 해제와 공정성 설정이 가능하며, 락을 더욱 세밀하게 관리할 수 있습니다. 이 프로젝트에서는 공정 모드를 활성화하여 먼저 요청한 스레드가 우선 실행되는 것을 보장해야 했기에, ReentrantLock이 적합한 선택이었습니다.

결론적으로, 공정성과 성능을 보장하기 위해 ReentrantLock을 사용하는 것이 더 적합한 선택이었습니다.
