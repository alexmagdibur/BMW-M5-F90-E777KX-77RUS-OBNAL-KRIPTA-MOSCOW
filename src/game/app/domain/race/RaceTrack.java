package game.app.domain.race;

public class RaceTrack {

    private final String name;
    private final int difficulty;
    private final double baseTime;

    public RaceTrack(String name, int difficulty, double baseTime) {
        this.name = name;
        this.difficulty = difficulty;
        this.baseTime = baseTime;
    }

    public String getName() {
        return name;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public double getBaseTime() {
        return baseTime;
    }

    @Override
    public String toString() {
        return name + " | Сложность: " + difficulty + " | Базовое время: " + baseTime;
    }
}