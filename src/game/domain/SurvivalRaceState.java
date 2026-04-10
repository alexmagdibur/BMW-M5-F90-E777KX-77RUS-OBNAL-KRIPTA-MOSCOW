package domain;

import java.util.ArrayList;
import java.util.List;

public class SurvivalRaceState {

    private final List<SurvivalParticipant> order; // index 0 = 1-е место
    private int currentStep;
    private final int totalSteps;

    public SurvivalRaceState(List<SurvivalParticipant> order, int totalSteps) {
        this.order       = new ArrayList<>(order);
        this.totalSteps  = totalSteps;
        this.currentStep = 0;
    }

    // все участники, включая выбывших
    public List<SurvivalParticipant> getOrder() { return order; }

    public int getCurrentStep() { return currentStep; }
    public int getTotalSteps()  { return totalSteps; }
    public void advanceStep()   { currentStep++; }

    // только активные (не выбывшие) в порядке позиций
    public List<SurvivalParticipant> getActiveParticipants() {
        return order.stream().filter(p -> !p.isEliminated()).toList();
    }

    public SurvivalParticipant getPlayer() {
        return order.stream().filter(SurvivalParticipant::isPlayer).findFirst().orElse(null);
    }

    // 1-based позиция среди активных; -1 если выбыл или не найден
    public int getActivePosition(SurvivalParticipant p) {
        List<SurvivalParticipant> active = getActiveParticipants();
        int idx = active.indexOf(p);
        return idx >= 0 ? idx + 1 : -1;
    }

    public boolean isRaceOver() {
        SurvivalParticipant player = getPlayer();
        if (player != null && player.isEliminated()) return true;
        if (getActiveParticipants().size() <= 1) return true;
        return currentStep >= totalSteps;
    }

    // победа: игрок не выбыл и на 1-м месте
    public boolean isPlayerVictory() {
        SurvivalParticipant player = getPlayer();
        if (player == null || player.isEliminated()) return false;
        return getActivePosition(player) == 1;
    }

    // поражение: выбыл или не первый
    public boolean isPlayerDefeat() {
        SurvivalParticipant player = getPlayer();
        if (player == null || player.isEliminated()) return true;
        return getActivePosition(player) != 1;
    }
}
