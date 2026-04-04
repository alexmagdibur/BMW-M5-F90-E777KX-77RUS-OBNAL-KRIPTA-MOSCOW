package game.domain;

/**
 * Weather conditions that affect race lap times.
 *
 * Each constant stores per-section-type speed multipliers:
 *   STRAIGHT — hydroplaning risk at high speed
 *   TURN     — grip loss, most affected in wet/rain
 *   CLIMB    — moderate penalty (lower speeds, traction issues)
 *   DESCENT  — braking distance increases in wet conditions
 */
public enum Weather {

    DRY ("Солнечно",   1.00, 1.00, 1.00, 1.00),
    WET ("Высокая влажность"  ,  0.88, 0.80, 0.91, 0.85),
    RAIN("Дождь",  0.76, 0.68, 0.83, 0.74);

    private final String displayName;
    private final double straightMult;
    private final double turnMult;
    private final double climbMult;
    private final double descentMult;

    Weather(String displayName,
            double straightMult, double turnMult,
            double climbMult, double descentMult) {
        this.displayName  = displayName;
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

    /** Random weather condition. */
    public static Weather random() {
        Weather[] values = values();
        return values[(int) (Math.random() * values.length)];
    }
}
