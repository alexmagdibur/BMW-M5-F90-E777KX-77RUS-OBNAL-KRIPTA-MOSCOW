package domain;

import java.util.concurrent.atomic.AtomicInteger;

public class PitLane {

    private final int capacity;
    // AtomicInteger — потокобезопасный счётчик без synchronized,
    // compareAndSet гарантирует атомарность проверки и занятия места
    private final AtomicInteger occupiedSlots = new AtomicInteger(0);

    public PitLane(int capacity) {
        this.capacity = capacity;
    }

    public int getCapacity() { return capacity; }

    public int getAvailableSlots() {
        return capacity - occupiedSlots.get();
    }

    // атомарно занимает место: возвращает true если место нашлось
    public boolean tryEnter() {
        while (true) {
            int current = occupiedSlots.get();
            if (current >= capacity) return false;
            if (occupiedSlots.compareAndSet(current, current + 1)) return true;
        }
    }

    // освобождает место после выезда из боксов
    public void leave() {
        occupiedSlots.decrementAndGet();
    }

    @Override
    public String toString() {
        return String.format("PitLane | Вместимость: %d | Занято: %d | Свободно: %d",
            capacity, occupiedSlots.get(), getAvailableSlots());
    }
}
