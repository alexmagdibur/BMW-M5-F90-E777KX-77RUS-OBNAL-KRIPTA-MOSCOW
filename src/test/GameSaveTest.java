import domain.*;
import org.junit.jupiter.api.Test;
import saving.GameSave;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class GameSaveTest {

    private static Team buildTeam() {
        Team team = new Team("Ferrari", 1_000_000);

        Bolid bolid = new Bolid("SF-24");
        bolid.installComponent(new Component("Двигатель",     ComponentType.ENGINE,       0, 80));
        bolid.installComponent(new Component("Трансмиссия",  ComponentType.TRANSMISSION, 0, 75));
        bolid.installComponent(new Component("Подвеска",     ComponentType.SUSPENSION,   0, 70));
        bolid.installComponent(new Component("Шасси",        ComponentType.CHASSIS,      0, 70));
        bolid.installComponent(new Component("Обвесы",       ComponentType.AERO_PACKAGE, 0, 65));
        bolid.installComponent(new Component("Шины",         ComponentType.TIRES,        0, 60));
        team.addBolid(bolid);

        team.addPilot(new Pilot("Леклер", 500_000, 90));
        team.addEngineer(new Engineer("Иванов", 200_000, 85));

        return team;
    }

    @Test
    void gameSaveStoresTeamName() {
        Team team = buildTeam();
        GameSave save = new GameSave(team, List.of());
        assertEquals("Ferrari", save.getTeamName());
    }

    @Test
    void gameSaveStoresTeamReference() {
        Team team = buildTeam();
        GameSave save = new GameSave(team, List.of());
        assertSame(team, save.getTeam());
    }

    @Test
    void gameSaveStoresRaceHistory() {
        Team team = buildTeam();
        RaceResult r1 = new RaceResult("Ferrari",  90.5, true);
        RaceResult r2 = new RaceResult("RedBull",  91.2, false);
        r1.setPosition(1);
        r2.setPosition(2);

        GameSave save = new GameSave(team, List.of(r1, r2));

        assertEquals(2, save.getRaceHistory().size());
        assertSame(r1, save.getRaceHistory().get(0));
    }

    @Test
    void gameSaveRaceHistoryIsCopy() {
        Team team = buildTeam();
        java.util.List<RaceResult> original = new java.util.ArrayList<>();
        GameSave save = new GameSave(team, original);

        original.add(new RaceResult("Extra", 99.0, false));

        assertEquals(0, save.getRaceHistory().size(), "Изменение оригинального списка не должно влиять на сохранение");
    }

    @Test
    void gameSaveTimestampHasCorrectFormat() {
        Team team = buildTeam();
        GameSave save = new GameSave(team, List.of());

        // Формат: DD-MM-YYYY_HH-mm-ss  →  23 символа
        String ts = save.getTimeStamp();
        assertNotNull(ts);
        assertTrue(ts.matches("\\d{2}-\\d{2}-\\d{4}_\\d{2}-\\d{2}-\\d{2}"),
                "Временная метка должна соответствовать формату DD-MM-YYYY_HH-mm-ss, получено: " + ts);
    }

    @Test
    void gameSaveToStringContainsKeyFields() {
        Team team = buildTeam();
        GameSave save = new GameSave(team, List.of(new RaceResult("Ferrari", 90.0, true)));

        String str = save.toString();
        assertTrue(str.contains("Ferrari"), "toString должен содержать название команды");
        assertTrue(str.contains("1"),       "toString должен содержать количество гонок");
    }
}
