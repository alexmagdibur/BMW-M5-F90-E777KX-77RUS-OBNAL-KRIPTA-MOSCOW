import domain.*;
import org.junit.jupiter.api.Test;
import service.RaceService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class RaceTest {

    private static Bolid buildCompleteBolid() {
        Bolid bolid = new Bolid("Тест-болид");
        bolid.installComponent(new Component("Двигатель", ComponentType.ENGINE, 0, 60));
        bolid.installComponent(new Component("Трансмиссия", ComponentType.TRANSMISSION, 0, 55));
        bolid.installComponent(new Component("Подвеска", ComponentType.SUSPENSION, 0, 50));
        bolid.installComponent(new Component("Шасси", ComponentType.CHASSIS, 0, 50));
        bolid.installComponent(new Component("Обвесы", ComponentType.AERO_PACKAGE, 0, 50));
        bolid.installComponent(new Component("Шины", ComponentType.TIRES, 0, 60));
        return bolid;
    }

    private static Track simpleTrack() {
        return new Track("Тест-трасса", List.of(
            new TrackSection(SectionType.STRAIGHT, 2000),
            new TrackSection(SectionType.TURN, 500)
        ));
    }

    @Test
    void raceReturnsResultWithPositiveTime() {
        Race race = new RaceService().runRace(
            new Team("Тест", 0),
            buildCompleteBolid(),
            new Pilot("Пилот", 0, 75),
            new Engineer("Инженер", 0, 60),
            simpleTrack(),
            Weather.DRY
        );

        assertFalse(race.isPlayerDNF(), "Гонка без износа не должна давать DNF");

        RaceResult playerResult = race.getResults().stream()
            .filter(RaceResult::isPlayer)
            .findFirst()
            .orElseThrow();

        assertTrue(playerResult.getTime() > 0, "Время должно быть больше нуля");
        assertTrue(playerResult.getTime() < Double.MAX_VALUE, "Время не должно быть DNF-значением");
    }

    @Test
    void playerIsAssignedAPosition() {
        Race race = new RaceService().runRace(
            new Team("Тест", 0),
            buildCompleteBolid(),
            new Pilot("Пилот", 0, 75),
            new Engineer("Инженер", 0, 60),
            simpleTrack(),
            Weather.DRY
        );

        assertTrue(race.getPlayerPosition() >= 1, "Позиция должна быть >= 1");
    }
}
