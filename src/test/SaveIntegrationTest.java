import domain.*;
import org.junit.jupiter.api.*;
import saving.GameSave;
import service.SaveService;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


// Сценарии
    // 1. новая игра - купить компоненты - сохранить - выйти
    // 2. то же имя - загрузить - проверить бюджет и инвентврб
    // 3. другое имя - убедиться, что чужих сохранений не видео
    // ну хз как то так
public class SaveIntegrationTest {

    private static final String PLAYER_A = "integration_player_a";
    private static final String PLAYER_B = "integration_player_b";

    private SaveService saveService;

    @BeforeEach
    void setUp() {
        saveService = new SaveService();
    }

    @AfterEach
    void tearDown() {
        deleteDir(new File("saves" + File.separator + PLAYER_A));
        deleteDir(new File("saves" + File.separator + PLAYER_B));
    }

    // сценарий 1 + 2: сохранить - загрузить - сверить состояние

    @Test
    void scenario_newGame_buyComponents_save_thenLoad_budgetPreserved() {
        // сессия 1: новая игра, покупаем компоненты, сохраняем
        Team team = new Team(PLAYER_A, 10_000_000L);

        // симулируем покупку двух компонентов (ShopService тратит деньги)
        Component engine = new Component("Двигатель RS", ComponentType.ENGINE, 500_000, 85);
        Component tires  = new Component("Шины Pirelli", ComponentType.TIRES, 200_000, 65);
        team.spend(500_000 + 200_000);    // как это делает ShopService
        team.addComponent(engine);
        team.addComponent(tires);

        // добавляем пилота и инженера
        team.addPilot(new Pilot("Петров", 300_000, 80));
        team.addEngineer(new Engineer("Иванов", 150_000, 75));

        saveService.saveGame(team, List.of(), PLAYER_A);

        // сессия 2: находим сохранение и загружаем
        List<String> saves = saveService.getAvailableSaves(PLAYER_A);
        assertFalse(saves.isEmpty(), "Список сохранений не должен быть пустым");

        String fileName = saves.stream()
                .filter(f -> f.startsWith("save_"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Файл save_*.csv не найден"));

        GameSave loaded = saveService.loadGame(PLAYER_A, fileName);
        System.out.println("Загружено: " + loaded);

        assertEquals(10_000_000L - 700_000L, loaded.getTeam().getBudget(),
                "Бюджет должен совпадать после загрузки");
    }

    @Test
    void scenario_newGame_save_thenLoad_inventoryPreserved() {
        Team team = new Team(PLAYER_A, 10_000_000L);

        Component engine = new Component("Двигатель V8", ComponentType.ENGINE, 400_000, 88);
        Component trans  = new Component("Коробка GT", ComponentType.TRANSMISSION, 200_000, 72);
        engine.setWear(20);
        team.spend(600_000);
        team.addComponent(engine);
        team.addComponent(trans);

        saveService.autoSave(team, List.of(), PLAYER_A);

        GameSave loaded = saveService.loadGame(PLAYER_A, "autosave.csv");

        List<Component> inv = loaded.getTeam().getInventory();
        assertEquals(2, inv.size(), "В инвентаре должно быть 2 компонента");

        Component loadedEngine = inv.stream()
                .filter(c -> c.getName().equals("Двигатель V8"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Двигатель не найден в инвентаре"));
        assertEquals(20, loadedEngine.getWear(), "Износ компонента должен сохраняться");
    }

    @Test
    void scenario_newGame_save_thenLoad_pilotsPreserved() {
        Team team = new Team(PLAYER_A, 10_000_000L);
        team.addPilot(new Pilot("Алонсо", 600_000, 92));
        team.addPilot(new Pilot("Перес",  400_000, 87));

        saveService.autoSave(team, List.of(), PLAYER_A);

        GameSave loaded = saveService.loadGame(PLAYER_A, "autosave.csv");

        assertEquals(2, loaded.getTeam().getPilots().size(), "Оба пилота должны загрузиться");
        assertTrue(loaded.getTeam().getPilots().stream()
                .anyMatch(p -> p.getName().equals("Алонсо")));
    }

    @Test
    void scenario_newGame_save_thenLoad_bolidPreserved() {
        Team team = new Team(PLAYER_A, 10_000_000L);

        Bolid bolid = new Bolid("McLaren MCL38");
        bolid.installComponent(new Component("Двигатель", ComponentType.ENGINE, 0, 80));
        bolid.installComponent(new Component("Трансмиссия", ComponentType.TRANSMISSION, 0, 75));
        bolid.installComponent(new Component("Подвеска", ComponentType.SUSPENSION, 0, 70));
        bolid.installComponent(new Component("Шасси", ComponentType.CHASSIS, 0, 70));
        bolid.installComponent(new Component("Аэропакет", ComponentType.AERO_PACKAGE, 0, 65));
        bolid.installComponent(new Component("Шины Hard", ComponentType.TIRES,        0, 60));
        team.addBolid(bolid);
        team.addEngineer(new Engineer("Браун", 200_000, 85));

        saveService.autoSave(team, List.of(), PLAYER_A);

        GameSave loaded = saveService.loadGame(PLAYER_A, "autosave.csv");

        assertEquals(1, loaded.getTeam().getBolids().size());
        Bolid lb = loaded.getTeam().getBolids().get(0);
        assertEquals("McLaren MCL38", lb.getName());
        assertTrue(lb.isComplete(), "Болид должен быть собран после загрузки");
    }

    @Test
    void scenario_newGame_race_save_thenLoad_raceHistoryPreserved() {
        Team team = new Team(PLAYER_A, 10_000_000L);

        // Симулируем результат гонки
        RaceResult result = new RaceResult(PLAYER_A, 91.5, true);
        result.setPosition(3);
        List<RaceResult> history = List.of(result);

        saveService.autoSave(team, history, PLAYER_A);

        GameSave loaded = saveService.loadGame(PLAYER_A, "autosave.csv");

        assertEquals(1, loaded.getRaceHistory().size(), "История гонок должна содержать 1 запись");
        RaceResult lr = loaded.getRaceHistory().get(0);
        assertEquals(PLAYER_A, lr.getTeamName());
        assertEquals(3, lr.getPosition());
        assertEquals(91.5, lr.getTime(), 1e-9);
    }


    // сценарий 3: другой игрок не видит чужих сохранений

    @Test
    void scenario_differentPlayer_seesNoSaves() {
        // PLAYER_A сохраняет игру
        Team teamA = new Team(PLAYER_A, 5_000_000L);
        saveService.autoSave(teamA, List.of(), PLAYER_A);

        // PLAYER_B проверяет свои сохранения — должен видеть пустой список
        List<String> savesB = saveService.getAvailableSaves(PLAYER_B);
        assertTrue(savesB.isEmpty(),
                "Игрок B не должен видеть сохранения игрока A");
    }

    @Test
    void scenario_differentPlayer_ownSavesAreIsolated() {
        // оба игрока сохраняют свои игры
        saveService.autoSave(new Team(PLAYER_A, 1_000_000L), List.of(), PLAYER_A);
        saveService.autoSave(new Team(PLAYER_B, 9_000_000L), List.of(), PLAYER_B);

        // каждый видит только своё
        GameSave loadedA = saveService.loadGame(PLAYER_A, "autosave.csv");
        GameSave loadedB = saveService.loadGame(PLAYER_B, "autosave.csv");

        assertEquals(1_000_000L, loadedA.getTeam().getBudget(), "Бюджет A должен совпасть");
        assertEquals(9_000_000L, loadedB.getTeam().getBudget(), "Бюджет B должен совпасть");
    }

    @Test
    void scenario_multipleManualSaves_allListed() {
        Team team = new Team(PLAYER_A, 10_000_000L);

        // три ручных сохранения в разные моменты
        saveService.saveGame(team, List.of(), PLAYER_A);
        try { Thread.sleep(1100); } catch (InterruptedException ignored) {}  // разные временные метки
        saveService.saveGame(team, List.of(), PLAYER_A);
        try { Thread.sleep(1100); } catch (InterruptedException ignored) {}
        saveService.saveGame(team, List.of(), PLAYER_A);

        List<String> saves = saveService.getAvailableSaves(PLAYER_A);
        long manualCount = saves.stream().filter(f -> f.startsWith("save_")).count();
        assertEquals(3, manualCount, "Должно быть 3 ручных сохранения");
    }

    // toString итогового GameSave

    @Test
    void loadedGameSave_toStringShowsCorrectData() {
        Team team = new Team("Команда Альфа", 7_777_000L);
        RaceResult r = new RaceResult("Команда Альфа", 88.0, true);
        r.setPosition(1);

        saveService.autoSave(team, List.of(r), PLAYER_A);

        GameSave save = saveService.loadGame(PLAYER_A, "autosave.csv");
        String str = save.toString();
        System.out.println("toString: " + str);

        assertTrue(str.contains("Команда Альфа"), "toString должен содержать название команды");
        assertTrue(str.contains("1"), "toString должен содержать количество гонок");
    }

    // helper

    private void deleteDir(File dir) {
        if (!dir.exists()) return;
        File[] files = dir.listFiles();
        if (files != null) for (File f : files) f.delete();
        dir.delete();
    }
}
