package domain;

public enum Weather {

    DRY ("Солнечно", 15, 1.00, 1.00, 1.00, 1.00),
    WET ("Высокая влажность", 25, 0.88, 0.80, 0.91, 0.85),
    RAIN ("Дождь", 15, 0.76, 0.68, 0.83, 0.74),
    SOLAR_ECLIPSE ("Солнечное затмение",45, 0.88, 0.80, 0.91, 0.85);

    private final String displayName;
    private final int weight;
    private final double straightMult;
    private final double turnMult;
    private final double climbMult;
    private final double descentMult;

    Weather(String displayName, int weight, double straightMult, double turnMult,
            double climbMult, double descentMult) {
        this.displayName = displayName;
        this.weight = weight;
        this.straightMult = straightMult;
        this.turnMult = turnMult;
        this.climbMult = climbMult;
        this.descentMult = descentMult;
    }

    public double getMultiplier(SectionType type) {
        return switch (type) {
            case STRAIGHT -> straightMult;
            case TURN -> turnMult;
            case CLIMB -> climbMult;
            case DESCENT -> descentMult;
        };
    }

    public String getDisplayName() { return displayName; }

    @Override
    public String toString() { return displayName; }

    public static Weather random() {
        Weather[] values = values();
        int totalWeight = 0;
        for (Weather w : values) totalWeight += w.weight;

        int roll = (int) (Math.random() * totalWeight);
        int cumulative = 0;
        for (Weather w : values) {
            cumulative += w.weight;
            if (roll < cumulative) return w;
        }
        return values[values.length - 1];
    }
}
