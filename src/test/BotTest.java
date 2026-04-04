import domain.*;
import org.junit.jupiter.api.Test;
import service.BotGenerator;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BotTest {

    private static final Track TRACK = new Track("Тест", List.of(
        new TrackSection(SectionType.STRAIGHT, 2000),
        new TrackSection(SectionType.TURN,      500)
    ));

    @Test
    void generateReturnsCorrectNumberOfBots() {
        List<RaceResult> bots = BotGenerator.generate(TRACK, Weather.DRY);
        assertEquals(3, bots.size(), "Должно быть сгенерировано 3 бота");
    }

    @Test
    void eachBotHasPositiveTime() {
        List<RaceResult> bots = BotGenerator.generate(TRACK, Weather.DRY);
        for (RaceResult bot : bots) {
            assertTrue(bot.getTime() > 0, "Время бота должно быть больше нуля");
            assertTrue(bot.getTime() < Double.MAX_VALUE, "Бот не должен иметь DNF-время");
        }
    }

    @Test
    void eachBotHasNonEmptyName() {
        List<RaceResult> bots = BotGenerator.generate(TRACK, Weather.DRY);
        for (RaceResult bot : bots) {
            assertNotNull(bot.getTeamName());
            assertFalse(bot.getTeamName().isBlank(), "Имя команды бота не должно быть пустым");
        }
    }

    @Test
    void botsAreNotMarkedAsPlayer() {
        List<RaceResult> bots = BotGenerator.generate(TRACK, Weather.DRY);
        for (RaceResult bot : bots) {
            assertFalse(bot.isPlayer(), "Боты не должны быть помечены как игрок");
        }
    }

    @Test
    void botNamesAreUnique() {
        List<RaceResult> bots = BotGenerator.generate(TRACK, Weather.DRY);
        long uniqueNames = bots.stream().map(RaceResult::getTeamName).distinct().count();
        assertEquals(bots.size(), uniqueNames, "Имена ботов должны быть уникальными");
    }
}
