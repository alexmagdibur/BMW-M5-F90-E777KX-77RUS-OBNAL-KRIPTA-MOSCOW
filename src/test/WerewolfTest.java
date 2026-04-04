import domain.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import service.RaceService;
import service.WerewolfService;
import ui.ConsoleInput;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class WerewolfTest {

    private static void setWerewolfChance(double value) {
        RaceService.setWerewolfChance(value);
    }

    private final InputStream originalIn = System.in;

    private void mockInput(String input) {
        System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
        ConsoleInput.resetScanner();
    }

    @AfterEach
    void tearDown()  {
        System.setIn(originalIn);
        ConsoleInput.resetScanner();
        setWerewolfChance(0.4); // восстанавливаем исходное значение
    }


    // ─── Флаги оборотня ───────────────────────────────────────────────────────

    @Test
    void pilotIsNotWerewolfByDefault() {
        assertFalse(new Pilot("Пилот", 0, 50).isWerewolf());
    }

    @Test
    void engineerIsNotWerewolfByDefault() {
        assertFalse(new Engineer("Инженер", 0, 50).isWerewolf());
    }

    @Test
    void werewolfFlagCanBeSetAndClearedForPilot() {
        Pilot pilot = new Pilot("Пилот", 0, 50);
        pilot.setWerewolf(true);
        assertTrue(pilot.isWerewolf());
        pilot.setWerewolf(false);
        assertFalse(pilot.isWerewolf());
    }

    @Test
    void werewolfFlagCanBeSetAndClearedForEngineer() {
        Engineer engineer = new Engineer("Инженер", 0, 50);
        engineer.setWerewolf(true);
        assertTrue(engineer.isWerewolf());
        engineer.setWerewolf(false);
        assertFalse(engineer.isWerewolf());
    }

    // ─── Гонка при солнечном затмении ─────────────────────────────────────────

    @Test
    void solarEclipseWithChance1AlwaysMakesWerewolf() {
        setWerewolfChance(1.0);

        Race race = new RaceService().runRace(
            new Team("Тест", 0),
            buildBolid(),
            new Pilot("Пилот", 0, 50),
            new Engineer("Инженер", 0, 50),
            simpleTrack(),
            Weather.SOLAR_ECLIPSE
        );

        assertTrue(race.isPlayerDNF(), "При chance=1 и затмении должен быть DNF");
    }

    @Test
    void nonEclipseWeatherResetsWerewolfFlagForPilot() {
        setWerewolfChance(1.0);

        Pilot pilot = new Pilot("Пилот", 0, 50);
        pilot.setWerewolf(true);

        Race race = new RaceService().runRace(
            new Team("Тест", 0),
            buildBolid(),
            pilot,
            new Engineer("Инженер", 0, 50),
            simpleTrack(),
            Weather.DRY
        );

        assertFalse(pilot.isWerewolf(), "При обычной погоде флаг пилота должен сброситься");
        assertFalse(race.isPlayerDNF(), "При обычной погоде не должен быть DNF из-за оборотня");
    }

    @Test
    void nonEclipseWeatherResetsWerewolfFlagForEngineer() {
        setWerewolfChance(1.0);

        Engineer engineer = new Engineer("Инженер", 0, 50);
        engineer.setWerewolf(true);

        Race race = new RaceService().runRace(
            new Team("Тест", 0),
            buildBolid(),
            new Pilot("Пилот", 0, 50),
            engineer,
            simpleTrack(),
            Weather.DRY
        );

        assertFalse(engineer.isWerewolf(), "При обычной погоде флаг инженера должен сброситься");
        assertFalse(race.isPlayerDNF(), "При обычной погоде инженер-оборотень не должен давать DNF");
    }

    // ─── Ван Хельсинг ─────────────────────────────────────────────────────────

    @Test
    void vanHelsingRemovesWerewolfPilotFromTeam() {
        Team team = new Team("Тест", 500_000);
        Pilot pilot = new Pilot("Оборотень", 0, 50);
        pilot.setWerewolf(true);
        team.addPilot(pilot);

        mockInput("1\n1\n"); // Ван Хельсинг (1), пилот #1

        new WerewolfService(team).werewolfHunt();

        assertFalse(team.getPilots().contains(pilot), "Ван Хельсинг должен удалить пилота-оборотня");
    }

    @Test
    void vanHelsingDoesNotRemoveNormalPilot() {
        Team team = new Team("Тест", 500_000);
        Pilot pilot = new Pilot("Человек", 0, 50);
        pilot.setWerewolf(false);
        team.addPilot(pilot);

        mockInput("1\n1\n");

        new WerewolfService(team).werewolfHunt();

        assertTrue(team.getPilots().contains(pilot), "Ван Хельсинг не должен удалять обычного пилота");
    }

    @Test
    void vanHelsingRemovesWerewolfEngineerFromTeam() {
        Team team = new Team("Тест", 500_000);
        Engineer engineer = new Engineer("Оборотень", 0, 50);
        engineer.setWerewolf(true);
        team.addEngineer(engineer);

        mockInput("1\n1\n"); // Ван Хельсинг (1), инженер #1

        new WerewolfService(team).werewolfHunt();

        assertFalse(team.getEngineers().contains(engineer), "Ван Хельсинг должен удалить инженера-оборотня");
    }

    @Test
    void vanHelsingDoesNotRemoveNormalEngineer() {
        Team team = new Team("Тест", 500_000);
        Engineer engineer = new Engineer("Человек", 0, 50);
        engineer.setWerewolf(false);
        team.addEngineer(engineer);

        mockInput("1\n1\n");

        new WerewolfService(team).werewolfHunt();

        assertTrue(team.getEngineers().contains(engineer), "Ван Хельсинг не должен удалять обычного инженера");
    }

    // ─── Баффи ────────────────────────────────────────────────────────────────

    @Test
    void buffyHealsWerewolfPilot() {
        Team team = new Team("Тест", 500_000);
        Pilot pilot = new Pilot("Оборотень", 0, 50);
        pilot.setWerewolf(true);
        team.addPilot(pilot);

        mockInput("2\n1\n"); // Баффи (2), пилот #1

        new WerewolfService(team).werewolfHunt();

        assertFalse(pilot.isWerewolf(), "Баффи должна вылечить пилота-оборотня");
        assertTrue(team.getPilots().contains(pilot), "Баффи не должна удалять пилота из команды");
    }

    @Test
    void buffyHealsWerewolfEngineer() {
        Team team = new Team("Тест", 500_000);
        Engineer engineer = new Engineer("Оборотень", 0, 50);
        engineer.setWerewolf(true);
        team.addEngineer(engineer);

        mockInput("2\n1\n"); // Баффи (2), инженер #1

        new WerewolfService(team).werewolfHunt();

        assertFalse(engineer.isWerewolf(), "Баффи должна вылечить инженера-оборотня");
        assertTrue(team.getEngineers().contains(engineer), "Баффи не должна удалять инженера из команды");
    }


    private static Bolid buildBolid() {
        Bolid b = new Bolid("Болид");
        b.installComponent(new Component("Двигатель",   ComponentType.ENGINE,       0, 60));
        b.installComponent(new Component("Трансмиссия", ComponentType.TRANSMISSION, 0, 55));
        b.installComponent(new Component("Подвеска",    ComponentType.SUSPENSION,   0, 50));
        b.installComponent(new Component("Шасси",       ComponentType.CHASSIS,       0, 50));
        b.installComponent(new Component("Обвесы",      ComponentType.AERO_PACKAGE, 0, 50));
        b.installComponent(new Component("Шины",        ComponentType.TIRES,         0, 60));
        return b;
    }

    private static Track simpleTrack() {
        return new Track("Тест", List.of(new TrackSection(SectionType.STRAIGHT, 1000)));
    }
}
