import data.TacticCatalog;
import domain.RaceTactic;
import domain.Weather;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class TacticTest {

    @Test
    void testRainTacticModifier() {
        RaceTactic rain = TacticCatalog.getAvailableTactics().get(1); // Дождевая
        assertEquals(0.93, rain.getModifier(Weather.RAIN), 0.001,
            "Дождевая тактика на RAIN должна давать коэффициент 0.93");
    }

    @Test
    void testMissingWeatherReturnsNeutral() {
        // Агрессивная не имеет модификатора для SOLAR_ECLIPSE
        RaceTactic aggressive = TacticCatalog.getAvailableTactics().get(0);
        assertEquals(1.0, aggressive.getModifier(Weather.SOLAR_ECLIPSE), 0.001,
            "Отсутствующая погода в map должна возвращать нейтральный коэффициент 1.0");
    }

    @Test
    void testAggressiveFasterOnDrySlowerOnRain() {
        RaceTactic aggressive = TacticCatalog.getAvailableTactics().get(0); // Агрессивная
        double dryMod  = aggressive.getModifier(Weather.DRY);
        double rainMod = aggressive.getModifier(Weather.RAIN);
        assertTrue(dryMod < 1.0,  "Агрессивная на DRY должна ускорять (< 1.0)");
        assertTrue(rainMod > 1.0, "Агрессивная на RAIN должна замедлять (> 1.0)");
    }

    @Test
    void testCustomTacticGetModifier() {
        RaceTactic t = new RaceTactic("Тест", Map.of(Weather.WET, 0.88));
        assertEquals(0.88, t.getModifier(Weather.WET), 0.001);
        assertEquals(1.0,  t.getModifier(Weather.DRY), 0.001,
            "Погода не в map должна вернуть 1.0");
    }
}
