package domain;

public class PitStop {

    // длительность пит-стопа в миллисекундах (для Thread.sleep в BolideThread).
    // Значение 800мс согласовано с множителем BolideThread.MS_PER_SIM_SECOND=50:
    // 800 / 50 = 16 симуляционных секунд — реалистичная цена пит-стопа
    // относительно длины гонки (80–110 с).
    public static final int DURATION_MS = 800;
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
