import domain.*;
import org.junit.jupiter.api.Test;
import service.RaceService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class RaceThreadIntegrationTest {

    // трасса из двух секций — гонка завершается за ~200мс реального времени
    private static Track simpleTrack() {
        return new Track("Интег-тест", List.of(
            new TrackSection(SectionType.STRAIGHT, 1000),
            new TrackSection(SectionType.TURN, 500)
        ));
    }

    private static Bolid freshBolid() {
        Bolid b = new Bolid("Тест-болид");
        b.installComponent(new Component("Двигатель", ComponentType.ENGINE, 0, 70));
        b.installComponent(new Component("КПП", ComponentType.TRANSMISSION, 0, 60));
        b.installComponent(new Component("Подвеска", ComponentType.SUSPENSION, 0, 55));
        b.installComponent(new Component("Шасси", ComponentType.CHASSIS, 0, 55));
        b.installComponent(new Component("Аэропакет", ComponentType.AERO_PACKAGE, 0, 55));
        b.installComponent(new Component("Шины", ComponentType.TIRES, 0, 60));
        return b;
    }

    private static Bolid wornBolid() {
        Bolid b = new Bolid("Изношенный болид");
        // wear=75 > 50 → isWornOut() = true → pre-race incident с вероятностью 100%
        b.installComponent(wornComponent("Двигатель", ComponentType.ENGINE));
        b.installComponent(wornComponent("КПП", ComponentType.TRANSMISSION));
        b.installComponent(wornComponent("Подвеска", ComponentType.SUSPENSION));
        b.installComponent(wornComponent("Шасси", ComponentType.CHASSIS));
        b.installComponent(wornComponent("Аэропакет", ComponentType.AERO_PACKAGE));
        b.installComponent(wornComponent("Шины", ComponentType.TIRES));
        return b;
    }

    private static Component wornComponent(String name, ComponentType type) {
        Component c = new Component(name, type, 0, 60);
        c.setWear(75);
        return c;
    }

    @Test
    void testRaceCompletesWithinTimeout() throws InterruptedException {
        RaceService svc = new RaceService();
        Thread runner = new Thread(() ->
            svc.runRace(new Team("Тест", 0), freshBolid(),
                new Pilot("Пилот", 0, 75), new Engineer("Инженер", 0, 60),
                simpleTrack(), Weather.DRY, 0)
        );
        runner.start();
        runner.join(15_000);
        assertFalse(runner.isAlive(), "гонка должна завершиться менее чем за 15 секунд");
    }

    @Test
    void testResultsContainAtLeastOneEntry() {
        Race race = new RaceService().runRace(
            new Team("Тест", 0), freshBolid(),
            new Pilot("Пилот", 0, 75), new Engineer("Инженер", 0, 60),
            simpleTrack(), Weather.DRY, 0
        );
        assertNotNull(race, "объект Race не должен быть null");
        assertFalse(race.getResults().isEmpty(), "список результатов не должен быть пустым");
    }

    @Test
    void testMaxWornBolidProducesDNF() {
        // pre-race checkIncident с шансом 100% гарантирует DNF на изношенном болиде
        RaceService.setWerewolfChance(0.0); // исключаем оборотней
        Race race = new RaceService().runRace(
            new Team("Тест", 0), wornBolid(),
            new Pilot("Пилот", 0, 75), new Engineer("Инженер", 0, 60),
            simpleTrack(), Weather.DRY, 0
        );
        assertTrue(race.isPlayerDNF(), "болид с изношенными компонентами должен получить DNF");
        RaceService.setWerewolfChance(0.4); // восстанавливаем дефолт
    }
}
