package service.threads;

import domain.RaceResult;

public class BolideResult {

    private final String participantName;
    private final double time;
    private final boolean dnf;
    private final boolean isPlayer;

    public BolideResult(String participantName, double time, boolean dnf, boolean isPlayer) {
        this.participantName = participantName;
        this.time = time;
        this.dnf = dnf;
        this.isPlayer = isPlayer;
    }

    public String getParticipantName() { return participantName; }
    public double getTime() { return time; }
    public boolean isDnf() { return dnf; }
    public boolean isPlayer() { return isPlayer; }

    // конвертация в RaceResult для совместимости с Race и GameMenu
    public RaceResult toRaceResult() {
        if (dnf) return RaceResult.dnf(participantName, isPlayer);
        return new RaceResult(participantName, time, isPlayer);
    }

    @Override
    public String toString() {
        String status = dnf ? "DNF" : String.format("%.3f с", time);
        return String.format("%s | %s | игрок: %s", participantName, status, isPlayer);
    }
}
