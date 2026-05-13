package domain;

public class PitStop {

    // длительность пит-стопа в миллисекундах (для Thread.sleep в BolideThread)
    public static final int DURATION_MS = 2000;
    // снижение износа шин за один пит-стоп
    public static final double WEAR_REDUCTION = 30.0;

    // применяет бонус пит-стопа: снижает износ шин болида
    public static void applyBonus(Bolid bolid) {
        Component tires = bolid.getComponent(ComponentType.TIRES);
        if (tires == null) return;
        int newWear = (int) Math.max(0, tires.getWear() - WEAR_REDUCTION);
        tires.setWear(newWear);
    }

    @Override
    public String toString() {
        return String.format("PitStop | Длительность: %d мс | Снижение износа шин: %.0f%%",
            DURATION_MS, WEAR_REDUCTION);
    }
}
