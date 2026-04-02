package game.data;

import game.domain.Pilot;

import java.util.List;

public class PilotCatalog {

    public static List<Pilot> getAll() {
        return List.of(
            new Pilot("Святослав Новиков",     200_000, 55),
            new Pilot("Вадим Горемыкин",     350_000, 70),
            new Pilot("Кирилл Куреев",     500_000, 82),
            new Pilot("Руслан Ахтямов",       750_000, 91)
        );
    }
}