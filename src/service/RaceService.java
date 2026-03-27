package service;

import domain.car.Car;
import domain.component.Component;
import domain.person.Engineer;
import domain.person.Pilot;
import domain.race.RaceResult;
import domain.race.RaceTrack;
import domain.race.RaceIncident;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RaceService {

    private final List<RaceTrack> tracks;
    private final Random random;
    private final WearService wearService;

    public RaceService() {
        this.tracks = new ArrayList<>();
        this.random = new Random();
        this.wearService = new WearService();
        fillTracks();
    }

    private void fillTracks() {
        tracks.add(new RaceTrack("Садовое кольцо", 70, 120.0));
        tracks.add(new RaceTrack("Монако", 85, 140.0));
        tracks.add(new RaceTrack("Формула-1", 95, 160.0));
    }

    public void listTracks() {
        System.out.println("Доступные трассы:");
        for (int i = 0; i < tracks.size(); i++) {
            System.out.println((i + 1) + ". " + tracks.get(i));
        }
    }

    public boolean isValidTrackChoice(int choice) {
        return choice >= 1 && choice <= tracks.size();
    }

    public RaceTrack getTrackByChoice(int choice) {
        return tracks.get(choice - 1);
    }

    public RaceResult simulateRace(Car car, Pilot pilot, Engineer engineer, RaceTrack track, boolean acceptedRisk) {
        double incidentChance = 0.0;

        if (acceptedRisk) {
            incidentChance = wearService.calculateIncidentChance(car);

            double engineerProtection = engineer.getQualification() * 0.2;
            incidentChance -= engineerProtection;

            if (incidentChance < 5) {
                incidentChance = 5;
            }
        }

        boolean incidentOccurred = acceptedRisk && random.nextDouble() * 100 < incidentChance;

        if (incidentOccurred) {
            RaceIncident incident = wearService.getRandomIncident(car);

            if (incident != null) {
                incident.applyDamage();

                return new RaceResult(
                        track.getName(),
                        pilot.getName(),
                        0,
                        false,
                        true,
                        incident.buildStatus()
                );
            }

            Component brokenComponent = wearService.breakRandomCriticalComponent(car);
            String brokenName = brokenComponent == null ? "неизвестный компонент" : brokenComponent.getName();

            return new RaceResult(
                    track.getName(),
                    pilot.getName(),
                    0,
                    false,
                    true,
                    "Произошел инцидент: разрушен компонент \"" + brokenName + "\""
            );
        }

        int carScore = calculateCarScore(car);
        int pilotScore = pilot.getSkill();
        int engineerScore = engineer.getQualification();

        double randomFactor = random.nextDouble() * 5.0;
        double bonus = (carScore * 0.35) + (pilotScore * 0.25) + (engineerScore * 0.15);
        double difficultyPenalty = track.getDifficulty() * 0.2;
        double wearPenalty = calculateWearPenalty(car);

        double finalTime = track.getBaseTime() + difficultyPenalty + wearPenalty - bonus + randomFactor;

        if (finalTime < 20) {
            finalTime = 20;
        }

        return new RaceResult(
                track.getName(),
                pilot.getName(),
                finalTime,
                true,
                false,
                "Финиш"
        );
    }

    private double calculateWearPenalty(Car car) {
        double penalty = 0.0;
        penalty += getWearPenalty(car.getEngine(), 0.12);
        penalty += getWearPenalty(car.getTransmission(), 0.08);
        penalty += getWearPenalty(car.getSuspension(), 0.07);
        penalty += getWearPenalty(car.getAerokit(), 0.05);
        penalty += getWearPenalty(car.getTires(), 0.15);
        return penalty;
    }

    private double getWearPenalty(Component component, double coefficient) {
        if (component == null) {
            return 0;
        }
        return component.getWear() * coefficient;
    }

    private int calculateCarScore(Car car) {
        int score = 0;

        score += getComponentScore(car.getEngine());
        score += getComponentScore(car.getTransmission());
        score += getComponentScore(car.getSuspension());
        score += getComponentScore(car.getAerokit());
        score += getComponentScore(car.getTires());
        score += getExtraScore(car);

        return score;
    }

    private int getExtraScore(Car car) {
        int bonus = 0;

        for (Component extra : car.getExtras()) {
            if (extra.getName().equalsIgnoreCase("Тонировка в круг")) {
                bonus += 5;
            } else if (extra.getName().equalsIgnoreCase("Блатные номера Е777КХ 77RUS")) {
                bonus += 7;
            } else {
                bonus += 3;
            }
        }

        return bonus;
    }

    private int getComponentScore(Component component) {
        if (component == null) {
            return 0;
        }

        return switch (component.getType()) {
            case ENGINE -> 90;
            case TRANSMISSION -> 70;
            case SUSPENSION -> 60;
            case AEROKIT -> 50;
            case TIRES -> 40;
            case EXTRA -> 10;
        };
    }
}