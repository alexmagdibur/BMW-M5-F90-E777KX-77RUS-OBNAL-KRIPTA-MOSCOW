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

    /** Полный список участников (включая выбывших). */
    public List<SurvivalParticipant> getOrder() { return order; }

    public int getCurrentStep() { return currentStep; }
    public int getTotalSteps()  { return totalSteps; }
    public void advanceStep()   { currentStep++; }

    /** Только активные (не выбывшие) в порядке позиций. */
    public List<SurvivalParticipant> getActiveParticipants() {
        return order.stream().filter(p -> !p.isEliminated()).toList();
    }

    public SurvivalParticipant getPlayer() {
        return order.stream().filter(SurvivalParticipant::isPlayer).findFirst().orElse(null);
    }

    /** 1-based позиция среди активных, −1 если не найден / выбыл. */
    public int getActivePosition(SurvivalParticipant p) {
        List<SurvivalParticipant> active = getActiveParticipants();
        int idx = active.indexOf(p);
        return idx >= 0 ? idx + 1 : -1;
    }

    public boolean isRaceOver() {
        SurvivalParticipant player = getPlayer();
        if (player != null && player.isEliminated()) return true;
        List<SurvivalParticipant> active = getActiveParticipants();
        // Гонка кончается, если остался только игрок или все выбыли
        if (active.size() <= 1) return true;
        return currentStep >= totalSteps;
    }

    /**
     * Победа: игрок не выбыл и занимает 1-е место среди активных участников.
     */
    public boolean isPlayerVictory() {
        SurvivalParticipant player = getPlayer();
        if (player == null || player.isEliminated()) return false;
        return getActivePosition(player) == 1;
    }

    /**
     * Поражение: игрок выбыл или не занимает 1-е место.
     */
    public boolean isPlayerDefeat() {
        SurvivalParticipant player = getPlayer();
        if (player == null || player.isEliminated()) return true;
        return getActivePosition(player) != 1;
    }
}
