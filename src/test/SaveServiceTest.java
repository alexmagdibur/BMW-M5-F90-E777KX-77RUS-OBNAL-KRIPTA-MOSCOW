import domain.*;
import org.junit.jupiter.api.*;
import saving.GameSave;
import service.SaveService;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


// создаём команду с полным набором данных, сохраняем, загружаем, сравниваем.
public class SaveServiceTest {

    private static final String PLAYER = "test_save_service_player";

    private SaveService service;

    // инициализация тестовой команды

    private static Team buildTeam() {
        Team team = new Team("Scuderia Ferrari", 8_000_000L);

        // Болид с 6 основными компонентами
        Bolid sf24 = new Bolid("SF-24");
        sf24.installComponent(new Component("Двигатель Ferrari", ComponentType.ENGINE, 180_000, 88));
        sf24.installComponent(new Component("Трансмиссия F8", ComponentType.TRANSMISSION, 90_000, 75));
        sf24.installComponent(new Component("Подвеска Pro", ComponentType.SUSPENSION, 60_000, 70));
        sf24.installComponent(new Component("Шасси Carbon", ComponentType.CHASSIS, 120_000, 80));
        sf24.installComponent(new Component("Обвес Aero-X", ComponentType.AERO_PACKAGE, 75_000, 72));
        sf24.installComponent(new Component("Шины Soft", ComponentType.TIRES, 40_000, 65));
        team.addBolid(sf24);

        // Компонент в инвентаре (не установлен в болид)
        Component spare = new Component("Запасной двигатель", ComponentType.ENGINE, 150_000, 82);
        spare.setWear(15);
        team.addComponent(spare);

        // Персонал
        Pilot leclercq = new Pilot("Шарль Леклер", 500_000, 92);
        Pilot sainz    = new Pilot("Карлос Сайнс", 450_000, 88);
        team.addPilot(leclercq);
        team.addPilot(sainz);

        Engineer ross   = new Engineer("Росс Браун",   300_000, 90);
        Engineer nikita = new Engineer("Никита Петров", 200_000, 78);
        team.addEngineer(ross);
        team.addEngineer(nikita);

        return team;
    }

    private static List<RaceResult> buildHistory() {
        RaceResult r1 = new RaceResult("Scuderia Ferrari", 87.342, true);
        r1.setPosition(1);
        RaceResult r2 = new RaceResult("Red Bull", 88.015, false);
        r2.setPosition(2);
        RaceResult r3 = RaceResult.dnf("Alpine", false);
        r3.setPosition(10);
        return List.of(r1, r2, r3);
    }

    // setup / teardown

    @BeforeEach
    void setUp() {
        service = new SaveService();
    }

    @AfterEach
    void tearDown() {
        deleteDir(new File("saves" + File.separator + PLAYER));
    }

    // autoSave round-trip

    @Test
    void autoSaveAndLoadPreservesTeamName() {
        service.autoSave(buildTeam(), buildHistory(), PLAYER);
        GameSave save = service.loadGame(PLAYER, "autosave.csv");
        System.out.println(save); // вывод через toString
        assertEquals("Scuderia Ferrari", save.getTeamName());
    }

    @Test
    void autoSaveAndLoadPreservesBudget() {
        service.autoSave(buildTeam(), buildHistory(), PLAYER);
        GameSave save = service.loadGame(PLAYER, "autosave.csv");
        assertEquals(8_000_000L, save.getTeam().getBudget());
    }

    @Test
    void autoSaveAndLoadPreservesBolidCount() {
        service.autoSave(buildTeam(), buildHistory(), PLAYER);
        GameSave save = service.loadGame(PLAYER, "autosave.csv");
        assertEquals(1, save.getTeam().getBolids().size());
    }

    @Test
    void autoSaveAndLoadPreservesBolidComponents() {
        service.autoSave(buildTeam(), buildHistory(), PLAYER);
        GameSave save = service.loadGame(PLAYER, "autosave.csv");
        Bolid loadedBolid = save.getTeam().getBolids().get(0);
        assertEquals("SF-24", loadedBolid.getName());
        assertNotNull(loadedBolid.getComponent(ComponentType.ENGINE), "ENGINE должен присутствовать");
        assertNotNull(loadedBolid.getComponent(ComponentType.TIRES), "TIRES должен присутствовать");
        assertNotNull(loadedBolid.getComponent(ComponentType.TRANSMISSION), "TRANSMISSION должен присутствовать");
        assertTrue(loadedBolid.isComplete(), "Болид должен быть полностью собран");
    }

    @Test
    void autoSaveAndLoadPreservesInventory() {
        service.autoSave(buildTeam(), buildHistory(), PLAYER);
        GameSave save = service.loadGame(PLAYER, "autosave.csv");
        List<Component> inventory = save.getTeam().getInventory();
        assertEquals(1, inventory.size(), "В инвентаре должен быть 1 запасной компонент");
        assertEquals("Запасной двигатель", inventory.get(0).getName());
        assertEquals(15, inventory.get(0).getWear(), "Износ запасного компонента должен сохраниться");
    }

    @Test
    void autoSaveAndLoadPreservesPilots() {
        service.autoSave(buildTeam(), buildHistory(), PLAYER);
        GameSave save = service.loadGame(PLAYER, "autosave.csv");
        List<Pilot> pilots = save.getTeam().getPilots();
        assertEquals(2, pilots.size());
        assertTrue(pilots.stream().anyMatch(p -> p.getName().equals("Шарль Леклер")));
        assertTrue(pilots.stream().anyMatch(p -> p.getName().equals("Карлос Сайнс")));
    }

    @Test
    void autoSaveAndLoadPreservesPilotStats() {
        service.autoSave(buildTeam(), buildHistory(), PLAYER);
        GameSave save = service.loadGame(PLAYER, "autosave.csv");
        Pilot leclercq = save.getTeam().getPilots().stream()
                .filter(p -> p.getName().equals("Шарль Леклер"))
                .findFirst().orElseThrow();
        assertEquals(500_000, leclercq.getSalary());
        assertEquals(92,      leclercq.getSkill());
    }

    @Test
    void autoSaveAndLoadPreservesEngineers() {
        service.autoSave(buildTeam(), buildHistory(), PLAYER);
        GameSave save = service.loadGame(PLAYER, "autosave.csv");
        List<Engineer> engineers = save.getTeam().getEngineers();
        assertEquals(2, engineers.size());
        assertTrue(engineers.stream().anyMatch(e -> e.getName().equals("Росс Браун")));
    }

    @Test
    void autoSaveAndLoadPreservesRaceHistory() {
        service.autoSave(buildTeam(), buildHistory(), PLAYER);
        GameSave save = service.loadGame(PLAYER, "autosave.csv");

        List<RaceResult> history = save.getRaceHistory();
        assertEquals(3, history.size(), "Должно быть 3 записи в истории");
    }

    @Test
    void autoSaveAndLoadPreservesDnfResult() {
        service.autoSave(buildTeam(), buildHistory(), PLAYER);
        GameSave save = service.loadGame(PLAYER, "autosave.csv");

        RaceResult dnf = save.getRaceHistory().stream()
                .filter(RaceResult::isIncident)
                .findFirst().orElseThrow(() -> new AssertionError("DNF запись не найдена"));

        assertEquals("Alpine", dnf.getTeamName());
        assertEquals(10, dnf.getPosition());
    }

    @Test
    void autoSaveAndLoadPreservesRaceTime() {
        service.autoSave(buildTeam(), buildHistory(), PLAYER);
        GameSave save = service.loadGame(PLAYER, "autosave.csv");

        RaceResult player = save.getRaceHistory().stream()
                .filter(RaceResult::isPlayer)
                .findFirst().orElseThrow();

        assertEquals(87.342, player.getTime(), 1e-9);
        assertEquals(1, player.getPosition());
    }

    // saveGame (с timestamp) round-trip

    @Test
    void saveGameCreatesFileListedInAvailableSaves() {
        service.saveGame(buildTeam(), buildHistory(), PLAYER);
        List<String> saves = service.getAvailableSaves(PLAYER);

        assertFalse(saves.isEmpty(), "После saveGame должен появиться хотя бы один файл");
        assertTrue(saves.stream().anyMatch(f -> f.startsWith("save_") && f.endsWith(".csv")),
                "Файл должен иметь формат save_{timestamp}.csv");
    }

    @Test
    void saveGameCanBeLoadedBack() {
        service.saveGame(buildTeam(), buildHistory(), PLAYER);

        String fileName = service.getAvailableSaves(PLAYER).stream()
                .filter(f -> f.startsWith("save_"))
                .findFirst().orElseThrow();

        GameSave save = service.loadGame(PLAYER, fileName);
        System.out.println(save);   // toString

        assertEquals("Scuderia Ferrari", save.getTeamName());
        assertEquals(3, save.getRaceHistory().size());
    }

    // autoSave overwrite

    @Test
    void autoSaveOverwritesPreviousFile() {
        service.autoSave(buildTeam(), buildHistory(), PLAYER);

        Team updatedTeam = new Team("Scuderia Ferrari", 9_999_999L); // изменили бюджет
        service.autoSave(updatedTeam, List.of(), PLAYER);

        GameSave save = service.loadGame(PLAYER, "autosave.csv");
        assertEquals(9_999_999L, save.getTeam().getBudget(), "Должен загрузиться актуальный бюджет");
        assertEquals(0, save.getRaceHistory().size(), "История должна быть пустой после перезаписи");
    }

    // toString smoke-test

    @Test
    void toStringContainsExpectedFields() {
        service.autoSave(buildTeam(), buildHistory(), PLAYER);
        GameSave save = service.loadGame(PLAYER, "autosave.csv");

        String str = save.toString();
        System.out.println("toString: " + str);

        assertTrue(str.contains("Scuderia Ferrari"), "toString должен содержать имя команды");
        assertTrue(str.contains("3"),                "toString должен содержать количество гонок");
    }

    // helper

    private void deleteDir(File dir) {
        if (!dir.exists()) return;
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) deleteDir(f);
                else f.delete();
            }
        }
        dir.delete();
    }
}
