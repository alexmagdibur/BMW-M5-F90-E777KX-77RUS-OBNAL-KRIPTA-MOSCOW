import domain.PitLane;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class PitLaneTest {

    @Test
    void testPitLaneAllowsUpToCapacity() {
        PitLane lane = new PitLane(2);
        assertTrue(lane.tryEnter(),  "первый вход должен быть разрешён");
        assertTrue(lane.tryEnter(),  "второй вход должен быть разрешён");
        assertFalse(lane.tryEnter(), "третий вход должен быть отклонён — мест нет");
    }

    @Test
    void testPitLaneAfterLeaveAcceptsAgain() {
        PitLane lane = new PitLane(2);
        lane.tryEnter();
        lane.tryEnter();
        lane.leave(); // освобождаем одно место
        assertTrue(lane.tryEnter(), "после leave() должно снова открыться место");
    }

    @Test
    void testConcurrentAccessRespectsCapacity() throws InterruptedException {
        int capacity = 2;
        int threads  = 5;
        PitLane lane = new PitLane(capacity);

        CountDownLatch ready  = new CountDownLatch(threads); // все потоки готовы
        CountDownLatch start  = new CountDownLatch(1);       // сигнал старта
        AtomicInteger entered = new AtomicInteger(0);        // счётчик успешных входов

        for (int i = 0; i < threads; i++) {
            new Thread(() -> {
                ready.countDown();
                try { start.await(); } catch (InterruptedException ignored) {}
                if (lane.tryEnter()) entered.incrementAndGet();
            }).start();
        }

        ready.await();  // ждём пока все потоки выйдут на старт
        start.countDown(); // одновременный старт
        Thread.sleep(500); // даём время завершиться

        assertTrue(entered.get() <= capacity,
            "не более " + capacity + " потоков должны войти одновременно, вошло: " + entered.get());
        assertTrue(entered.get() > 0,
            "хотя бы один поток должен войти");
    }
}
