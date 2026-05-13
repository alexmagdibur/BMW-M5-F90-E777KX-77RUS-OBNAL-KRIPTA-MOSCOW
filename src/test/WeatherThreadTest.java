import domain.Weather;
import org.junit.jupiter.api.Test;
import service.threads.RaceState;
import service.threads.WeatherThread;

import static org.junit.jupiter.api.Assertions.*;

public class WeatherThreadTest {

    private static RaceState startedState(Weather initial) {
        RaceState state = new RaceState(initial);
        state.setRaceStartTime(System.currentTimeMillis());
        state.getRaceRunning().set(true);
        return state;
    }

    @Test
    void testWeatherChangesOverTime() throws InterruptedException {
        // запускаем 10 попыток; хотя бы в одной погода должна измениться
        boolean changed = false;
        for (int attempt = 0; attempt < 10 && !changed; attempt++) {
            RaceState state = startedState(Weather.DRY);
            // интервал 50мс — за 500мс сделает ~10 попыток сменить погоду
            Thread t = new Thread(new WeatherThread(state, 50));
            t.start();
            Thread.sleep(500);
            state.getRaceRunning().set(false);
            t.interrupt();
            t.join(3000);
            if (state.getCurrentWeather() != Weather.DRY) changed = true;
        }
        assertTrue(changed, "за 10 попыток погода должна хотя бы раз измениться");
    }

    @Test
    void testWeatherThreadStopsWhenRaceEnds() throws InterruptedException {
        RaceState state = startedState(Weather.DRY);
        Thread t = new Thread(new WeatherThread(state, 100));
        t.start();
        state.getRaceRunning().set(false);
        t.interrupt();
        t.join(3000);
        assertFalse(t.isAlive(), "поток WeatherThread должен завершиться после raceRunning=false");
    }
}
