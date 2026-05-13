package domain;

public class RaceResult {

    private final String  teamName;
    private final double  time;
    private final boolean isPlayer;
    private final boolean incidentOccurred;
    private int position;
    private RaceTactic tactic;

    public RaceResult(String teamName, double time, boolean isPlayer) {
        this.teamName = teamName;
        this.time = time;
        this.isPlayer = isPlayer;
        this.incidentOccurred = false;
    }

    // конструктор для бота с тактикой
    public RaceResult(String teamName, double time, boolean isPlayer, RaceTactic tactic) {
        this(teamName, time, isPlayer);
        this.tactic = tactic;
    }

    private RaceResult(String teamName, boolean isPlayer) {
        this.teamName = teamName;
        this.time = Double.MAX_VALUE;
        this.isPlayer = isPlayer;
        this.incidentOccurred = true;
    }

    public static RaceResult dnf(String teamName, boolean isPlayer) {
        return new RaceResult(teamName, isPlayer);
    }

    public String getTeamName() { return teamName; }
    public double getTime() { return time; }
    public boolean isPlayer() { return isPlayer; }
    public boolean isIncident() { return incidentOccurred; }
    public int getPosition() { return position; }
    public RaceTactic getTactic() { return tactic; }

    public void setPosition(int position) { this.position = position; }
    public void setTactic(RaceTactic tactic) { this.tactic = tactic; }


    public static String formatTime(double seconds) {
        int    mins = (int) seconds / 60;
        double secs = seconds - mins * 60.0;
        return String.format("%d:%06.3f", mins, secs);
    }

    @Override
    public String toString() {
        String marker    = isPlayer ? "► " : "  ";
        String timeField = incidentOccurred ? "DNF" : formatTime(time);
        return String.format("%2d | %s%-25s | %s",
            position, marker, teamName, timeField);
    }
}
