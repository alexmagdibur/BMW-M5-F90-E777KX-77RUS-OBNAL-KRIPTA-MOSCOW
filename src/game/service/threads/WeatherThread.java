package service.threads;

import domain.Weather;

public class WeatherThread implements Runnable {

    private final RaceState state;
    private final int changeIntervalMs;

    public WeatherThread(RaceState state, int changeIntervalMs) {
        this.state = state;
        this.changeIntervalMs = changeIntervalMs;
    }

    @Override
    public void run() {
        while (state.raceRunning.get()) {
            try {
                Thread.sleep(changeIntervalMs);
            } catch (InterruptedException e) {
                break;
            }
            if (!state.raceRunning.get()) break;

            Weather next = randomWeather();
            if (next != state.currentWeather) {
                Weather prev = state.currentWeather;
                state.currentWeather = next;
                state.log("🌦 Погода изменилась: " + prev + " → " + next);
            }
        }
    }

    // SOLAR_ECLIPSE — 10%, остальные три по ~30%
    private static Weather randomWeather() {
        double r = Math.random();
        if (r < 0.10) return Weather.SOLAR_ECLIPSE;
        if (r < 0.40) return Weather.DRY;
        if (r < 0.70) return Weather.WET;
        return Weather.RAIN;
    }
}
