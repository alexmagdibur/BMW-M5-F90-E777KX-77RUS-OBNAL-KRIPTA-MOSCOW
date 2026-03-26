package domain.race;

public class RaceResult {

    private final String trackName;
    private final String pilotName;
    private final double finalTime;

    public RaceResult(String trackName, String pilotName, double finalTime) {
        this.trackName = trackName;
        this.pilotName = pilotName;
        this.finalTime = finalTime;
    }

    public String getTrackName() {
        return trackName;
    }

    public String getPilotName() {
        return pilotName;
    }

    public double getFinalTime() {
        return finalTime;
    }

    @Override
    public String toString() {
        return "Трасса: " + trackName +
                " | Пилот: " + pilotName +
                " | Время: " + String.format("%.2f", finalTime);
    }
}