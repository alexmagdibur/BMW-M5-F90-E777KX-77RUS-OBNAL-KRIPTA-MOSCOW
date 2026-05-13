package service.threads;

import domain.Weather;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class RaceState {

    // volatile — пишет WeatherThread, читают BolideThread; без volatile JVM вправе кэшировать значение на CPU
    volatile Weather currentWeather;

    // AtomicBoolean — флаг читается и пишется из нескольких потоков без synchronized
    final AtomicBoolean raceRunning = new AtomicBoolean(false);

    // устанавливается до старта потоков, далее только читается — volatile не нужен
    long raceStartTime;

    // CopyOnWriteArrayList — писателей мало (финиш/инцидент), читателей много;
    // итерация безопасна без блокировок, запись создаёт копию массива
    final List<BolideResult> results = new CopyOnWriteArrayList<>();

    public RaceState(Weather initialWeather) {
        this.currentWeather = initialWeather;
    }

    // synchronized — чтобы строки из разных потоков не перемешивались в консоли
    public synchronized void log(String message) {
        long elapsed = System.currentTimeMillis() - raceStartTime;
        long mins = elapsed / 60000;
        long secs = (elapsed % 60000) / 1000;
        System.out.printf("[%02d:%02d] %s%n", mins, secs, message);
    }
}
