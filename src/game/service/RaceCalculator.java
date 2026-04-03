package game.service;

import game.domain.Bolid;
import game.domain.Engineer;
import game.domain.Pilot;
import game.domain.SectionType;
import game.domain.Track;
import game.domain.TrackSection;
import game.domain.Weather;

/**
 * Deterministic lap-time calculator.
 *
 * Per-section effective speed = baseSpeed(type) * combinedFactor
 * combinedFactor = 0.70 + 0.60 * (wBolid*bolidNorm + wPilot*pilotNorm + wEng*engNorm)
 *
 * Weights by section type reflect what matters most on each section:
 *   STRAIGHT — bolid power dominates (engine, aero)
 *   TURN     — pilot skill dominates (braking, steering)
 *   CLIMB    — engine + engineer setup (gear ratios, downforce)
 *   DESCENT  — pilot bravery + bolid brakes/tires
 *
 * combinedFactor range: [0.70 .. 1.30]
 * → best team ≈1.30× base speed, worst team ≈0.70× base speed
 */
public class RaceCalculator {

    // Base speeds by section type, m/s
    private static final double SPEED_STRAIGHT = 70.0; // ~252 km/h
    private static final double SPEED_TURN     = 28.0; // ~100 km/h
    private static final double SPEED_CLIMB    = 40.0; // ~144 km/h
    private static final double SPEED_DESCENT  = 55.0; // ~198 km/h

    // Normalization bounds for bolid performance score
    private static final double BOLID_MIN = 300.0;
    private static final double BOLID_MAX = 540.0;

    // Normalization bounds for pilot/engineer skill (1–100)
    private static final double SKILL_MIN = 1.0;
    private static final double SKILL_MAX = 100.0;

    /**
     * Returns deterministic lap time in seconds.
     * A better bolid, pilot, or engineer always yields a shorter time.
     * Weather applies a per-section speed multiplier on top of performance.
     */
    public static double calculateTime(Bolid bolid, Pilot pilot, Engineer engineer,
                                       Track track, Weather weather) {
        double bolidNorm = normalize(bolid.getPerformanceScore(), BOLID_MIN, BOLID_MAX);
        double pilotNorm = normalize(pilot.getSkill(),            SKILL_MIN, SKILL_MAX);
        double engNorm   = normalize(engineer.getQualification(), SKILL_MIN, SKILL_MAX);

        double totalTime = 0.0;
        for (TrackSection section : track.getSections()) {
            double speed = effectiveSpeed(section.getType(), bolidNorm, pilotNorm, engNorm, weather);
            totalTime += section.getLength() / speed;
        }
        return totalTime;
    }

    private static double effectiveSpeed(SectionType type,
                                         double bolidNorm,
                                         double pilotNorm,
                                         double engNorm,
                                         Weather weather) {
        double baseSpeed;
        double wBolid, wPilot, wEng;

        switch (type) {
            case STRAIGHT -> { baseSpeed = SPEED_STRAIGHT; wBolid = 0.70; wPilot = 0.20; wEng = 0.10; }
            case TURN     -> { baseSpeed = SPEED_TURN;     wBolid = 0.25; wPilot = 0.60; wEng = 0.15; }
            case CLIMB    -> { baseSpeed = SPEED_CLIMB;    wBolid = 0.50; wPilot = 0.20; wEng = 0.30; }
            case DESCENT  -> { baseSpeed = SPEED_DESCENT;  wBolid = 0.35; wPilot = 0.50; wEng = 0.15; }
            default       -> { baseSpeed = SPEED_STRAIGHT; wBolid = 0.50; wPilot = 0.30; wEng = 0.20; }
        }

        // combinedFactor ∈ [0.70, 1.30]; weights sum to 1.0 per type
        double combinedFactor = 0.70 + 0.60 * (wBolid * bolidNorm + wPilot * pilotNorm + wEng * engNorm);
        return baseSpeed * combinedFactor * weather.getMultiplier(type);
    }

    /** Clamps value to [0, 1] linear range between min and max. */
    private static double normalize(double value, double min, double max) {
        return Math.max(0.0, Math.min(1.0, (value - min) / (max - min)));
    }
}
