package service;

import domain.Bolid;
import domain.Component;
import domain.Engineer;
import domain.Pilot;
import domain.Race;
import domain.RaceResult;
import domain.Team;
import domain.Track;
import domain.Weather;
import util.RandomUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class RaceService {

    private static final long PRIZE_1ST = 2_000_000;
    private static final long PRIZE_2ND = 1_000_000;
    private static final long PRIZE_3RD = 500_000;

    private static final double WEREWOLF_CHANCE = 0.4;

    public Race runRace(Team team, Bolid bolid, Pilot pilot, Engineer engineer,
                        Track track, Weather weather) {
        if (weather == Weather.SOLAR_ECLIPSE) {
            pilot.setWerewolf(RandomUtil.nextDouble(0.0, 1.0) < WEREWOLF_CHANCE);
        } else {
            pilot.setWerewolf(false);
        }

        String dnfReason = null;
        if (pilot.isWerewolf()) {
            dnfReason = "Пилот стал оборотнем, у него лапки! DNF";
        } else {
            dnfReason = checkIncident(bolid);
        }

        RaceResult playerResult;
        if (dnfReason != null) {
            playerResult = RaceResult.dnf(team.getName(), true);
        } else {
            double playerTime = RaceCalculator.calculateTime(bolid, pilot, engineer, track, weather);
            playerResult = new RaceResult(team.getName(), playerTime, true);
        }

        List<RaceResult> all = new ArrayList<>(BotGenerator.generate(track, weather));
        all.add(playerResult);

        all.sort(Comparator.comparingDouble(RaceResult::getTime));

        for (int i = 0; i < all.size(); i++) {
            all.get(i).setPosition(i + 1);
        }

        long prize = 0;
        if (dnfReason == null) {
            prize = switch (playerResult.getPosition()) {
                case 1 -> PRIZE_1ST;
                case 2 -> PRIZE_2ND;
                case 3 -> PRIZE_3RD;
                default -> 0L;
            };
        }

        if (prize > 0) {
            team.earn(prize);
        }

        return new Race(track, all, playerResult.getPosition(), prize, weather, dnfReason);
    }

    private static final double INCIDENT_CHANCE = 1;


    private String checkIncident(Bolid bolid) {
        for (Component c : bolid.getComponents().values()) {
            if (c.isWornOut() && RandomUtil.nextDouble(0.0, 1.0) < INCIDENT_CHANCE) {
                c.setWear(100);
                return "отказ компонента «" + c.getName() + "»";
            }
        }
        return null;
    }
}
