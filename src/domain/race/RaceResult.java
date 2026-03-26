package domain.race;

public class RaceResult {

    private final String trackName;
    private final String pilotName;
    private final double finalTime;
    private final boolean finished;
    private final boolean incidentOccurred;
    private final String status;

    public RaceResult(String trackName, String pilotName, double finalTime,
                      boolean finished, boolean incidentOccurred, String status) {
        this.trackName = trackName;
        this.pilotName = pilotName;
        this.finalTime = finalTime;
        this.finished = finished;
        this.incidentOccurred = incidentOccurred;
        this.status = status;
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

    public boolean isFinished() {
        return finished;
    }

    public boolean isIncidentOccurred() {
        return incidentOccurred;
    }

    public String getStatus() {
        return status;
    }

    @Override
    public String toString() {
        if (!finished) {
            return "Трасса: " + trackName +
                    " | Пилот: " + pilotName +
                    " | Статус: " + status;
        }

        return "Трасса: " + trackName +
                " | Пилот: " + pilotName +
                " | Время: " + String.format("%.2f", finalTime) +
                " | Статус: " + status;
    }
}