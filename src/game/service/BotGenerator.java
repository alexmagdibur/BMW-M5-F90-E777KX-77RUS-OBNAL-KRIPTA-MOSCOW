package game.service;

import game.domain.RaceResult;
import game.domain.SectionType;
import game.domain.Track;
import game.domain.TrackSection;
import game.domain.Weather;
import game.util.RandomUtil;

import java.util.ArrayList;
import java.util.List;

public class BotGenerator {

    private static final String[] BOT_NAMES = {
        "Чуваки на АвтоВАЗе",
        "M5 Asphalt 8",
        "Распил Владивосток",
        "JDM Racing Club",
        "Клуб дядек на Крузаках",
        "Можно, а зачем?"
    };

    private static final int BOT_COUNT = 3;

    /**
     * Generates BOT_COUNT bots with random times distributed around the
     * reference time for the given track (all-average crew, factor = 1.0).
     * Weather slows bots the same way it slows the player.
     * Multiplier range: [0.80, 1.25] × weatherAdjustedReferenceTime.
     */
    public static List<RaceResult> generate(Track track, Weather weather) {
        double ref = referenceTime(track, weather);

        List<String> usedNames = new ArrayList<>();
        List<RaceResult> bots = new ArrayList<>();

        for (int i = 0; i < BOT_COUNT; i++) {
            String name = pickName(usedNames);
            double time = ref * RandomUtil.nextDouble(0.80, 1.25);
            bots.add(new RaceResult(name, time, false));
        }
        return bots;
    }

    /**
     * Reference time = sum of (sectionLength / (baseSpeed * weatherMultiplier)) per section,
     * assuming the average performance factor of 1.0.
     */
    private static double referenceTime(Track track, Weather weather) {
        double time = 0.0;
        for (TrackSection s : track.getSections()) {
            double baseSpeed = switch (s.getType()) {
                case STRAIGHT -> 70.0;
                case TURN     -> 28.0;
                case CLIMB    -> 40.0;
                case DESCENT  -> 55.0;
            };
            time += s.getLength() / (baseSpeed * weather.getMultiplier(s.getType()));
        }
        return time;
    }

    private static String pickName(List<String> used) {
        String name;
        do {
            name = BOT_NAMES[RandomUtil.nextInt(0, BOT_NAMES.length - 1)];
        } while (used.contains(name));
        used.add(name);
        return name;
    }

    /**
     * Fixed league profiles for display (informational only).
     * Bolid / pilot / engineer skill levels reflect how competitive each team is.
     */
    public static String getLeagueTable() {
        Object[][] teams = {
            {"Чуваки на АвтоВАЗе",    312, 42, 35},
            {"M5 Asphalt 8",           445, 78, 71},
            {"Распил Владивосток",     380, 55, 60},
            {"JDM Racing Club",        510, 88, 82},
            {"Клуб дядек на Крузаках", 290, 30, 25},
            {"Можно, а зачем?",        460, 72, 68},
        };
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-26s | Болид | Пилот | Инженер%n", "Команда"));
        sb.append("─".repeat(52)).append("\n");
        for (Object[] t : teams) {
            sb.append(String.format("%-26s |  %3d  |  %3d  |    %3d%n",
                t[0], t[1], t[2], t[3]));
        }
        return sb.toString().stripTrailing();
    }
}
