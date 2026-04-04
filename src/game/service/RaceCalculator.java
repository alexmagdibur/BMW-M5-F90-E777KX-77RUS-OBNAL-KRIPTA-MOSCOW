package service;

import domain.Bolid;
import domain.Engineer;
import domain.Pilot;
import domain.SectionType;
import domain.Track;
import domain.TrackSection;
import domain.Weather;

public class RaceCalculator {

    private static final double SPEED_STRAIGHT = 70.0; // ~252 km/h
    private static final double SPEED_TURN = 28.0; // ~100 km/h
    private static final double SPEED_CLIMB = 40.0; // ~144 km/h
    private static final double SPEED_DESCENT = 55.0; // ~198 km/h

    private static final double BOLID_MIN = 300.0;
    private static final double BOLID_MAX = 540.0;

    private static final double SKILL_MIN = 1.0;
    private static final double SKILL_MAX = 100.0;


    public static double calculateTime(Bolid bolid, Pilot pilot, Engineer engineer,
                                       Track track, Weather weather) {
        double bolidNorm = normalize(bolid.getPerformanceScore(), BOLID_MIN, BOLID_MAX);
        double pilotNorm = normalize(pilot.getSkill(), SKILL_MIN, SKILL_MAX);
        double engNorm   = normalize(engineer.getQualification(), SKILL_MIN, SKILL_MAX);

        double totalTime = 0.0;
        for (TrackSection section : track.getSections()) {
            double speed = effectiveSpeed(section.getType(), bolidNorm, pilotNorm, engNorm, weather);
            totalTime += section.getLength() / speed;
        }
        return totalTime;
    }

    private static double effectiveSpeed(SectionType type, double bolidNorm, double pilotNorm, double engNorm,
                                         Weather weather) {
        double baseSpeed;
        double wBolid, wPilot, wEng;

        switch (type) {
            case STRAIGHT -> { baseSpeed = SPEED_STRAIGHT; wBolid = 0.70; wPilot = 0.20; wEng = 0.10; }
            case TURN -> { baseSpeed = SPEED_TURN; wBolid = 0.25; wPilot = 0.60; wEng = 0.15; }
            case CLIMB -> { baseSpeed = SPEED_CLIMB; wBolid = 0.50; wPilot = 0.20; wEng = 0.30; }
            case DESCENT -> { baseSpeed = SPEED_DESCENT; wBolid = 0.35; wPilot = 0.50; wEng = 0.15; }
            default -> { baseSpeed = SPEED_STRAIGHT; wBolid = 0.50; wPilot = 0.30; wEng = 0.20; }
        }

        double combinedFactor = 0.70 + 0.60 * (wBolid * bolidNorm + wPilot * pilotNorm + wEng * engNorm);
        return baseSpeed * combinedFactor * weather.getMultiplier(type);
    }

    private static double normalize(double value, double min, double max) {
        return Math.max(0.0, Math.min(1.0, (value - min) / (max - min)));
    }
}
