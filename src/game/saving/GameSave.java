package saving;

import domain.RaceResult;
import domain.Team;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class GameSave {

    private static final DateTimeFormatter TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("dd-MM-yyyy_HH-mm-ss");

    private final String teamName;
    private final Team team;
    private final List<RaceResult> raceHistory;
    private final String timeStamp;

    public GameSave(Team team, List<RaceResult> raceHistory) {
        this.teamName    = team.getName();
        this.team        = team;
        this.raceHistory = new ArrayList<>(raceHistory);
        this.timeStamp   = LocalDateTime.now().format(TIMESTAMP_FORMAT);
    }

    public String getTeamName()              { return teamName; }
    public Team getTeam()                    { return team; }
    public List<RaceResult> getRaceHistory() { return raceHistory; }
    public String getTimeStamp()             { return timeStamp; }

    @Override
    public String toString() {
        return String.format("GameSave[команда='%s', гонок=%d, сохранено=%s]",
                teamName, raceHistory.size(), timeStamp);
    }
}
