package service.threads;

import domain.Bolid;
import domain.Component;

import java.util.List;

public class IncidentThread implements Runnable {

    private static final double INCIDENT_CHANCE = 0.4;

    private final RaceState state;
    private final List<BolideThread> participants;
    private final int checkIntervalMs;

    public IncidentThread(RaceState state, List<BolideThread> participants, int checkIntervalMs) {
        this.state = state;
        this.participants = participants;
        this.checkIntervalMs = checkIntervalMs;
    }

    @Override
    public void run() {
        while (state.raceRunning.get()) {
            try {
                Thread.sleep(checkIntervalMs);
            } catch (InterruptedException e) {
                break;
            }
            if (!state.raceRunning.get()) break;

            for (BolideThread participant : participants) {
                if (participant.isDnf()) continue;

                Bolid bolid = participant.getBolid();
                String failedComponent = checkIncident(bolid);
                if (failedComponent != null) {
                    participant.setDnf();
                    state.results.add(new BolideResult(
                        participant.getParticipantName(), 0, true, participant.isPlayer()
                    ));
                    state.log("💥 ИНЦИДЕНТ: " + participant.getParticipantName()
                        + " сходит с трассы! (отказ: " + failedComponent + ")");
                }
            }
        }
    }

    // возвращает имя отказавшего компонента или null если инцидента нет
    private static String checkIncident(Bolid bolid) {
        for (Component c : bolid.getComponents().values()) {
            if (c.isWornOut() && Math.random() < INCIDENT_CHANCE) {
                c.setWear(100);
                return c.getName();
            }
        }
        return null;
    }
}
