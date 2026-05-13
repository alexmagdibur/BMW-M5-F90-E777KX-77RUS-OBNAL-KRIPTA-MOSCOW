package data;

import domain.Weather;
import util.RandomUtil;

public class CommentatorPhrases {

    private static final String[] SPONSOR_PHRASES = {
        "Гонку спонсирует ZovAuto: можно, но зачем?",
        "Кубок Аркхема: гонки на грани безумия!",
        "АвтоВАЗ: мы тоже иногда финишируем.",
        "ЖигулиРейсинг: скорость — это не наш конёк, но мы стараемся."
    };

    public static String getWeatherPhrase(Weather w) {
        return switch (w) {
            case DRY -> "Трасса сухая, болиды на максимальной скорости!";
            case WET -> "Влажная трасса — пилоты держат нервы в кулаке.";
            case RAIN -> "Дождь усиливается! Видимость ухудшается.";
            case SOLAR_ECLIPSE -> "СОЛНЕЧНОЕ ЗАТМЕНИЕ! Что-то пошло не так...";
        };
    }

    public static String getPositionPhrase(String leader, String last) {
        return "Лидирует " + leader + "! " + last + " замыкает пелотон.";
    }

    public static String getSponsorPhrase() {
        return SPONSOR_PHRASES[RandomUtil.nextInt(0, SPONSOR_PHRASES.length - 1)];
    }

    public static String getIncidentPhrase(String name) {
        return name + " сходит с дистанции! Драма на трассе!";
    }
}
