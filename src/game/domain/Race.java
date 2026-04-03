package game.domain;

import java.util.List;

public class Race {

    private final Track            track;
    private final List<RaceResult> results;
    private final int              playerPosition;
    private final long             prizeAwarded;
    private final Weather          weather;

    public Race(Track track, List<RaceResult> results, int playerPosition, long prizeAwarded,
                Weather weather) {
        this.track          = track;
        this.results        = List.copyOf(results);
        this.playerPosition = playerPosition;
        this.prizeAwarded   = prizeAwarded;
        this.weather        = weather;
    }

    public Track            getTrack()          { return track; }
    public List<RaceResult> getResults()        { return results; }
    public int              getPlayerPosition() { return playerPosition; }
    public long             getPrizeAwarded()   { return prizeAwarded; }
    public Weather          getWeather()        { return weather; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%nГонка: %s  |  Погода: %s%n", track.getName(), weather));

        for (RaceResult r : results) {
            sb.append(" ").append(r).append("\n");
        }

        sb.append(String.format("%nМесто: %d", playerPosition));
        if (prizeAwarded > 0) {
            sb.append(String.format("  |  +%,d руб.", prizeAwarded));
        }

        return sb.toString();
    }
}
