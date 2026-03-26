package service;

import domain.car.Car;
import domain.component.Component;
import domain.person.Engineer;
import domain.person.Pilot;
import domain.race.RaceResult;
import domain.race.RaceTrack;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RaceService {

    private final List<RaceTrack> tracks;
    private final Random random;

    public RaceService() {
        this.tracks = new ArrayList<>();
        this.random = new Random();
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

    public RaceResult simulateRace(Car car, Pilot pilot, Engineer engineer, RaceTrack track) {
        int carScore = calculateCarScore(car);
        int pilotScore = pilot.getSkill();
        int engineerScore = engineer.getQualification();

        double randomFactor = random.nextDouble() * 5.0;
        double bonus = (carScore * 0.35) + (pilotScore * 0.25) + (engineerScore * 0.15);
        double difficultyPenalty = track.getDifficulty() * 0.2;

        double finalTime = track.getBaseTime() + difficultyPenalty - bonus + randomFactor;

        if (finalTime < 20) {
            finalTime = 20;
        }

        return new RaceResult(track.getName(), pilot.getName(), finalTime);
    }

    private int calculateCarScore(Car car) {
        int score = 0;

        score += getComponentScore(car.getEngine());
        score += getComponentScore(car.getTransmission());
        score += getComponentScore(car.getSuspension());
        score += getComponentScore(car.getAerokit());
        score += getComponentScore(car.getTires());

        return score;
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
        };
    }
}