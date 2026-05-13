package data;

import domain.RaceTactic;
import domain.Weather;

import java.util.List;
import java.util.Map;

public class TacticCatalog {

    // List сохраняет порядок — важно для показа игроку в меню (этап 5)
    public static List<RaceTactic> getAvailableTactics() {
        return List.of(
            // быстрее на сухом, теряет на дожде
            new RaceTactic("Агрессивная", Map.of(
                Weather.DRY,   0.97,
                Weather.RAIN,  1.05
            )),
            // заточена под мокрые условия, медленнее на сухом
            new RaceTactic("Дождевая", Map.of(
                Weather.RAIN,  0.93,
                Weather.WET,   0.95,
                Weather.DRY,   1.03
            )),
            // стабильно чуть медленнее при любой погоде, без риска
            new RaceTactic("Осторожная", Map.of(
                Weather.DRY,           1.02,
                Weather.WET,           1.02,
                Weather.RAIN,          1.02,
                Weather.SOLAR_ECLIPSE, 1.02
            )),
            // нейтральная — модификаторы не заданы, getModifier вернёт 1.0
            new RaceTactic("Универсальная", Map.of())
        );
    }
}
