package domain;

/**
 * Weather conditions that affect race lap times.
 *
 * Each constant stores per-section-type speed multipliers:
 *   STRAIGHT — hydroplaning risk at high speed
 *   TURN     — grip loss, most affected in wet/rain
 *   CLIMB    — moderate penalty (lower speeds, traction issues)
 *   DESCENT  — braking distance increases in wet conditions
 *
 * weight — relative probability in random(); higher = more frequent.
 */
public enum Weather {

    DRY             ("Солнечно",          15, 1.00, 1.00, 1.00, 1.00),
    WET             ("Высокая влажность", 25, 0.88, 0.80, 0.91, 0.85),
    RAIN            ("Дождь",             15, 0.76, 0.68, 0.83, 0.74),
    SOLAR_ECLIPSE   ("Солнечное затмение",45, 0.88, 0.80, 0.91, 0.85);

    private final String displayName;
    private final int    weight;
    private final double straightMult;
    private final double turnMult;
    private final double climbMult;
    private final double descentMult;

    Weather(String displayName, int weight,
            double straightMult, double turnMult,
            double climbMult, double descentMult) {
        this.displayName  = displayName;
        this.weight       = weight;
        this.straightMult = straightMult;
        this.turnMult     = turnMult;
        this.climbMult    = climbMult;
        this.descentMult  = descentMult;
    }

    /** Speed multiplier for the given section type under this weather. */
    public double getMultiplier(SectionType type) {
        return switch (type) {
            case STRAIGHT -> straightMult;
            case TURN     -> turnMult;
            case CLIMB    -> climbMult;
            case DESCENT  -> descentMult;
        };
    }

    public String getDisplayName() { return displayName; }

    @Override
    public String toString() { return displayName; }

    /** Weighted random: SOLAR_ECLIPSE appears ~45% of the time. */
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
