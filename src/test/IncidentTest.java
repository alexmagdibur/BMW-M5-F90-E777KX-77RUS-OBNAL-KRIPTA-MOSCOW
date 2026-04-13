import domain.*;
import org.junit.jupiter.api.Test;
import service.RaceService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class IncidentTest {

    // собирает болид со всеми обязательными компонентами.
    // возвращает компонент ENGINE, чтобы можно было проверить его износ после гонки.
    private static Component buildBolid(Bolid bolid) {
        Component engine = new Component("Двигатель", ComponentType.ENGINE, 0, 50);
        bolid.installComponent(engine);
        bolid.installComponent(new Component("Трансмиссия", ComponentType.TRANSMISSION, 0, 50));
        bolid.installComponent(new Component("Подвеска", ComponentType.SUSPENSION, 0, 50));
        bolid.installComponent(new Component("Шасси", ComponentType.CHASSIS, 0, 50));
        bolid.installComponent(new Component("Обвесы", ComponentType.AERO_PACKAGE, 0, 50));
        bolid.installComponent(new Component("Шины", ComponentType.TIRES, 0, 50));
        return engine;
    }

    private static Track simpleTrack() {
        return new Track("Тест", List.of(new TrackSection(SectionType.STRAIGHT, 1000)));
    }

    @Test
    void wornComponentCausesDNF() {
        Bolid bolid = new Bolid("Болид");
        Component engine = buildBolid(bolid);
        engine.setWear(75);

        Race race = new RaceService().runRace(
            new Team("Тест", 0), bolid, new Pilot("Пилот", 0, 50),
            new Engineer("Инженер", 0, 50), simpleTrack(), Weather.DRY
        );

        assertTrue(race.isPlayerDNF(), "Изношенный компонент должен вызвать DNF");
    }

    @Test
    void incidentSetsComponentWearTo100() {
        Bolid bolid = new Bolid("Болид");
        Component engine = buildBolid(bolid);
        engine.setWear(75);

        new RaceService().runRace(
            new Team("Тест", 0), bolid, new Pilot("Пилот", 0, 50),
            new Engineer("Инженер", 0, 50), simpleTrack(), Weather.DRY
        );

        assertEquals(100, engine.getWear(), "После инцидента компонент должен иметь износ 100%");
    }

    @Test
    void freshComponentDoesNotCauseDNF() {
        Bolid bolid = new Bolid("Болид");
        buildBolid(bolid); // все компоненты с wear=0

        Race race = new RaceService().runRace(
            new Team("Тест", 0), bolid, new Pilot("Пилот", 0, 50),
            new Engineer("Инженер", 0, 50), simpleTrack(), Weather.DRY
        );

        assertFalse(race.isPlayerDNF(), "Новые компоненты не должны вызывать DNF");
    }
}
