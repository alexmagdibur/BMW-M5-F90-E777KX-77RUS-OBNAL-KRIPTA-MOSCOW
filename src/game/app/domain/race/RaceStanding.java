package game.app.domain.race;

public class RaceStanding {

    private final String teamName;
    private final RaceResult result;

    public RaceStanding(String teamName, RaceResult result) {
        this.teamName = teamName;
        this.result = result;
    }

    public String getTeamName() {
        return teamName;
    }

    public RaceResult getResult() {
        return result;
    }
}