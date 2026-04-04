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

    // ─── поле isWerewolf ─────────────────────────────────────────────────────

    @Test
    void pilotIsNotWerewolfByDefault() {
        assertFalse(new Pilot("Пилот", 0, 50).isWerewolf());
    }

    @Test
    void werewolfFlagCanBeSetAndCleared() {
        Pilot pilot = new Pilot("Пилот", 0, 50);
        pilot.setWerewolf(true);
        assertTrue(pilot.isWerewolf());
        pilot.setWerewolf(false);
        assertFalse(pilot.isWerewolf());
    }

    // ─── гонка ───────────────────────────────────────────────────────────────

    @Test
    void solarEclipseWithChance1AlwaysMakesWerewolf()  {
        setWerewolfChance(1.0); // гарантируем оборотня при солнечном затмении

        Race race = new RaceService().runRace(
            new Team("Тест", 0),
            buildBolid(),
            new Pilot("Пилот", 0, 50),
            new Engineer("Инженер", 0, 50),
            simpleTrack(),
            Weather.SOLAR_ECLIPSE
        );

        assertTrue(race.isPlayerDNF(), "Пилот-оборотень должен получить DNF");
    }

    @Test
    void nonEclipseWeatherResetsWerewolfFlag()  {
        setWerewolfChance(1.0); // даже при chance=1 без затмения флаг должен сброситься

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

        assertFalse(pilot.isWerewolf(), "При обычной погоде флаг оборотня должен сброситься");
        assertFalse(race.isPlayerDNF(), "При обычной погоде пилот не должен получить DNF из-за флага оборотня");
    }

    // ─── Ван Хельсинг ────────────────────────────────────────────────────────

    @Test
    void vanHelsingRemovesWerewolfPilotFromTeam() {
        Team team = new Team("Тест", 500_000);
        Pilot pilot = new Pilot("Оборотень", 0, 50);
        pilot.setWerewolf(true);
        team.addPilot(pilot);

        mockInput("1\n1\n"); // выбор Ван Хельсинга (1), затем пилот #1

        new WerewolfService(team).werewolfHunt();

        assertFalse(team.getPilots().contains(pilot), "Ван Хельсинг должен удалить пилота-оборотня из команды");
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

    // ─── Баффи ───────────────────────────────────────────────────────────────

    @Test
    void buffyHealsWerewolfPilot() {
        Team team = new Team("Тест", 500_000);
        Pilot pilot = new Pilot("Оборотень", 0, 50);
        pilot.setWerewolf(true);
        team.addPilot(pilot);

        mockInput("2\n1\n"); // выбор Баффи (2), затем пилот #1

        new WerewolfService(team).werewolfHunt();

        assertFalse(pilot.isWerewolf(), "Баффи должна вылечить пилота-оборотня");
        assertTrue(team.getPilots().contains(pilot), "Баффи не должна удалять пилота из команды");
    }

    // ─── вспомогательные методы ───────────────────────────────────────────────

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
