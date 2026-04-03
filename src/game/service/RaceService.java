package game.service;

import game.domain.Bolid;
import game.domain.Engineer;
import game.domain.Pilot;
import game.domain.Race;
import game.domain.RaceResult;
import game.domain.Team;
import game.domain.Track;
import game.domain.Weather;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class RaceService {

    private static final long PRIZE_1ST = 2_000_000;
    private static final long PRIZE_2ND = 1_000_000;
    private static final long PRIZE_3RD =   500_000;

    /**
     * Runs a full race:
     *   1. Calculates deterministic player lap time.
     *   2. Generates 3 random bot competitors.
     *   3. Sorts all results by time, assigns positions 1..N.
     *   4. Awards prize money to the team for top-3 finishes.
     *
     * @return Race object containing sorted results and prize info.
     */
    public Race runRace(Team team, Bolid bolid, Pilot pilot, Engineer engineer,
                        Track track, Weather weather) {
        double playerTime = RaceCalculator.calculateTime(bolid, pilot, engineer, track, weather);
        RaceResult playerResult = new RaceResult(team.getName(), playerTime, true);

        List<RaceResult> all = new ArrayList<>(BotGenerator.generate(track, weather));
        all.add(playerResult);

        all.sort(Comparator.comparingDouble(RaceResult::getTime));

        for (int i = 0; i < all.size(); i++) {
            all.get(i).setPosition(i + 1);
        }

        long prize = switch (playerResult.getPosition()) {
            case 1 -> PRIZE_1ST;
            case 2 -> PRIZE_2ND;
            case 3 -> PRIZE_3RD;
            default -> 0L;
        };

        if (prize > 0) {
            team.earn(prize);
        }

        return new Race(track, all, playerResult.getPosition(), prize, weather);
    }
}
