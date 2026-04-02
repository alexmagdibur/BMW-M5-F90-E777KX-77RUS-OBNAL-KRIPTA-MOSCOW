package game.data;

import game.domain.Engineer;

import java.util.List;

public class EngineerCatalog {

    public static List<Engineer> getAll() {
        return List.of(
            new Engineer("Дмитрий Левиев",   180_000, 20),
            new Engineer("Илия Родионов",         300_000, 68),
            new Engineer("Ирина Снитько",   450_000, 80),
            new Engineer("Елизавета Тихомирова",   700_000, 90)
        );
    }
}