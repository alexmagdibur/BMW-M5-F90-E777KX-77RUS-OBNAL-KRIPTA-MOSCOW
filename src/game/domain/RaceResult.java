package game.domain;

public class RaceResult {

    private final String teamName;
    private final double time;   // seconds
    private final boolean isPlayer;
    private int position;

    public RaceResult(String teamName, double time, boolean isPlayer) {
        this.teamName  = teamName;
        this.time      = time;
        this.isPlayer  = isPlayer;
    }

    public String  getTeamName() { return teamName; }
    public double  getTime()     { return time; }
    public boolean isPlayer()    { return isPlayer; }
    public int     getPosition() { return position; }

    public void setPosition(int position) { this.position = position; }

    /** Formats seconds as M:SS.mmm */
    public static String formatTime(double seconds) {
        int    mins  = (int) seconds / 60;
        double secs  = seconds - mins * 60.0;
        return String.format("%d:%06.3f", mins, secs);
    }

    @Override
    public String toString() {
        String marker = isPlayer ? "► " : "  ";
        return String.format("%2d | %s%-25s | %s",
            position, marker, teamName, formatTime(time));
    }
}
