package domain;

public class RaceResult {

    private final String  teamName;
    private final double  time;           // seconds; Double.MAX_VALUE when DNF
    private final boolean isPlayer;
    private final boolean incidentOccurred;
    private int position;

    public RaceResult(String teamName, double time, boolean isPlayer) {
        this.teamName         = teamName;
        this.time             = time;
        this.isPlayer         = isPlayer;
        this.incidentOccurred = false;
    }

    private RaceResult(String teamName, boolean isPlayer) {
        this.teamName         = teamName;
        this.time             = Double.MAX_VALUE;
        this.isPlayer         = isPlayer;
        this.incidentOccurred = true;
    }

    /** Factory for a Did-Not-Finish result. Time is MAX_VALUE so DNF sorts last. */
    public static RaceResult dnf(String teamName, boolean isPlayer) {
        return new RaceResult(teamName, isPlayer);
    }

    public String  getTeamName()        { return teamName; }
    public double  getTime()            { return time; }
    public boolean isPlayer()           { return isPlayer; }
    public boolean isIncident()         { return incidentOccurred; }
    public int     getPosition()        { return position; }

    public void setPosition(int position) { this.position = position; }

    /** Formats seconds as M:SS.mmm */
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
