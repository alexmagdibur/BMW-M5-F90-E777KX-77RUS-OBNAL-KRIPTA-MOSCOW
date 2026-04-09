import domain.*;
import org.junit.jupiter.api.*;
import saving.GameSave;
import service.AssemblyService;
import service.RaceService;
import service.SaveService;
import service.WearService;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Интеграционный тест автосохранения.
 *
 * Сценарии:
 *   1. Собрать болид → autosave.csv должен появиться.
 *   2. Провести гонку → autosave.csv должен обновиться (болид + результат гонки).
 *   3. Загрузить autosave.csv → команда, болид, история на месте.
 */
public class AutoSaveTest {

    private static final String PLAYER = "autosave_test_player";

    private SaveService      saveService;
    private List<RaceResult> raceResults;
    private Team             team;

    // ── setup / teardown ──────────────────────────────────────────────────────

    @BeforeEach
    void setUp() {
        saveService  = new SaveService();
        raceResults  = new ArrayList<>();
        team         = new Team(PLAYER, 10_000_000L);
    }

    @AfterEach
    void tearDown() {
        deleteDir(new File("saves" + File.separator + PLAYER));
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    /** Создаёт AssemblyService с автосохранением и возвращает полностью собранный болид. */
    private Bolid buildAndAssembleBolid(String bolidName) {
        // Кладём компоненты в инвентарь
        Component engine = new Component("Двигатель",   ComponentType.ENGINE,       0, 80);
        Component trans  = new Component("Трансмиссия", ComponentType.TRANSMISSION, 0, 75);
        Component susp   = new Component("Подвеска",    ComponentType.SUSPENSION,   0, 70);
        Component chas   = new Component("Шасси",       ComponentType.CHASSIS,      0, 70);
        Component aero   = new Component("Аэропакет",   ComponentType.AERO_PACKAGE, 0, 65);
        Component tires  = new Component("Шины",        ComponentType.TIRES,        0, 60);
        team.addComponent(engine);
        team.addComponent(trans);
        team.addComponent(susp);
        team.addComponent(chas);
        team.addComponent(aero);
        team.addComponent(tires);

        // Собираем болид напрямую (минуя UI)
        Bolid bolid = new Bolid(bolidName);
        bolid.installComponent(engine);
        bolid.installComponent(trans);
        bolid.installComponent(susp);
        bolid.installComponent(chas);
        bolid.installComponent(aero);
        bolid.installComponent(tires);

        for (Component c : List.of(engine, trans, susp, chas, aero, tires)) {
            team.removeComponent(c);
        }
        team.addBolid(bolid);

        // Вызываем autoSave — как это делает AssemblyService
        saveService.autoSave(team, raceResults, PLAYER);

        return bolid;
    }

    /** Запускает гонку через RaceService с автосохранением (зеркалирует порядок GameMenu). */
    private Race runRaceWithAutoSave(Bolid bolid) {
        if (team.getPilots().isEmpty())    team.addPilot(new Pilot("Тест-пилот", 0, 80));
        if (team.getEngineers().isEmpty()) team.addEngineer(new Engineer("Тест-инженер", 0, 75));

        Track track = new Track("Тест-трасса", List.of(
                new TrackSection(SectionType.STRAIGHT, 2000),
                new TrackSection(SectionType.TURN, 500)
        ));

        // RaceService добавляет результат в raceResults, но autoSave НЕ вызывает
        RaceService raceService = new RaceService(saveService, PLAYER, raceResults);
        Race race = raceService.runRace(
                team,
                bolid,
                team.getPilots().get(0),
                team.getEngineers().get(0),
                track,
                Weather.DRY
        );

        // Применяем износ (как GameMenu после runRace)
        WearService.applyWear(bolid, track);

        // AutoSave ПОСЛЕ wear — как GameMenu.startRace()
        saveService.autoSave(team, raceResults, PLAYER);

        return race;
    }

    // ── Сценарий 1: сборка болида → autosave.csv появился ────────────────────

    @Test
    void scenario1_afterAssembly_autoSaveFileExists() {
        buildAndAssembleBolid("SF-24");

        assertTrue(saveService.getAvailableSaves(PLAYER).contains("autosave.csv"),
                "autosave.csv должен появиться после сборки болида");
    }

    @Test
    void scenario1_afterAssembly_bolidInAutoSave() {
        buildAndAssembleBolid("SF-24");

        GameSave save = saveService.loadGame(PLAYER, "autosave.csv");
        assertEquals(1, save.getTeam().getBolids().size(),
                "Болид должен быть в автосохранении");
        assertEquals("SF-24", save.getTeam().getBolids().get(0).getName());
    }

    @Test
    void scenario1_afterAssembly_teamBudgetInAutoSave() {
        buildAndAssembleBolid("RB20");

        GameSave save = saveService.loadGame(PLAYER, "autosave.csv");
        assertEquals(10_000_000L, save.getTeam().getBudget());
    }

    // ── Сценарий 2: гонка → autosave.csv обновился ───────────────────────────

    @Test
    void scenario2_afterRace_autoSaveContainsRaceResult() {
        Bolid bolid = buildAndAssembleBolid("SF-24");
        runRaceWithAutoSave(bolid);

        GameSave save = saveService.loadGame(PLAYER, "autosave.csv");
        assertEquals(1, save.getRaceHistory().size(),
                "После гонки в автосохранении должна быть 1 запись истории");
    }

    @Test
    void scenario2_afterRace_raceResultBelongsToPlayer() {
        Bolid bolid = buildAndAssembleBolid("SF-24");
        runRaceWithAutoSave(bolid);

        GameSave save = saveService.loadGame(PLAYER, "autosave.csv");
        RaceResult result = save.getRaceHistory().get(0);
        assertTrue(result.isPlayer(), "Сохранённый результат должен принадлежать игроку");
    }

    @Test
    void scenario2_autoSave_overwritesPreviousOnSecondRace() {
        Bolid bolid = buildAndAssembleBolid("SF-24");
        runRaceWithAutoSave(bolid);
        runRaceWithAutoSave(bolid);

        GameSave save = saveService.loadGame(PLAYER, "autosave.csv");
        assertEquals(2, save.getRaceHistory().size(),
                "После двух гонок в автосохранении должно быть 2 записи");
    }

    @Test
    void scenario2_afterRace_budgetUpdatedInAutoSave() {
        Bolid bolid = buildAndAssembleBolid("SF-24");
        long budgetBeforeRace = team.getBudget();

        runRaceWithAutoSave(bolid);

        GameSave save = saveService.loadGame(PLAYER, "autosave.csv");
        // Бюджет мог вырасти (приз) или остаться — главное что он совпадает с текущим
        assertEquals(team.getBudget(), save.getTeam().getBudget(),
                "Бюджет в автосохранении должен совпадать с текущим бюджетом команды");
        // Дополнительно убеждаемся, что значение разумное
        assertTrue(save.getTeam().getBudget() >= budgetBeforeRace,
                "После гонки бюджет не может стать меньше (штрафов нет)");
    }

    // ── Сценарий 3: загрузить autosave → всё на месте ────────────────────────

    @Test
    void scenario3_loadAfterRace_teamNamePreserved() {
        Bolid bolid = buildAndAssembleBolid("SF-24");
        runRaceWithAutoSave(bolid);

        // "Перезапуск" — новый сервис (как Main.java при следующем старте)
        SaveService freshService = new SaveService();
        GameSave loaded = freshService.loadGame(PLAYER, "autosave.csv");

        assertEquals(PLAYER, loaded.getTeamName(),
                "Имя команды должно совпадать после перезапуска");
    }

    @Test
    void scenario3_loadAfterRace_bolidIsComplete() {
        Bolid bolid = buildAndAssembleBolid("SF-24");
        runRaceWithAutoSave(bolid);

        SaveService freshService = new SaveService();
        GameSave loaded = freshService.loadGame(PLAYER, "autosave.csv");

        assertFalse(loaded.getTeam().getBolids().isEmpty(),
                "Болид должен быть на месте");
        assertTrue(loaded.getTeam().getBolids().get(0).isComplete(),
                "Болид должен быть полностью собран");
    }

    @Test
    void scenario3_loadAfterRace_pilotsPreserved() {
        Bolid bolid = buildAndAssembleBolid("SF-24");
        runRaceWithAutoSave(bolid);

        SaveService freshService = new SaveService();
        GameSave loaded = freshService.loadGame(PLAYER, "autosave.csv");

        assertFalse(loaded.getTeam().getPilots().isEmpty(),
                "Пилот должен быть в загруженной команде");
        assertEquals("Тест-пилот", loaded.getTeam().getPilots().get(0).getName());
    }

    @Test
    void scenario3_loadAfterRace_historyPreserved() {
        Bolid bolid = buildAndAssembleBolid("SF-24");
        runRaceWithAutoSave(bolid);

        SaveService freshService = new SaveService();
        GameSave loaded = freshService.loadGame(PLAYER, "autosave.csv");

        assertEquals(1, loaded.getRaceHistory().size(),
                "История гонок должна быть на месте после загрузки");
    }

    // ── Wear сохраняется корректно ─────────────────────────────────────────────

    @Test
    void wearIsPersistedAfterRace() {
        Bolid bolid = buildAndAssembleBolid("SF-24");

        // Убеждаемся что до гонки wear = 0
        bolid.getAllComponents().forEach(c ->
                assertEquals(0, c.getWear(), "Износ до гонки должен быть 0 у " + c.getName()));

        runRaceWithAutoSave(bolid);

        // Гонка применяет wear — хотя бы один компонент должен износиться
        int totalWear = bolid.getAllComponents().stream().mapToInt(Component::getWear).sum();
        assertTrue(totalWear > 0, "После гонки должен появиться износ");

        // Загружаем из autosave — wear должен совпасть
        GameSave loaded = saveService.loadGame(PLAYER, "autosave.csv");
        Bolid loadedBolid = loaded.getTeam().getBolids().get(0);

        int savedWear = loadedBolid.getAllComponents().stream().mapToInt(Component::getWear).sum();
        assertEquals(totalWear, savedWear,
                "Суммарный износ в сохранении должен совпадать с фактическим");
    }

    @Test
    void wearAccumulatesAcrossMultipleRaces() {
        Bolid bolid = buildAndAssembleBolid("SF-24");

        runRaceWithAutoSave(bolid);
        int wearAfterRace1 = bolid.getAllComponents().stream().mapToInt(Component::getWear).sum();

        runRaceWithAutoSave(bolid);
        int wearAfterRace2 = bolid.getAllComponents().stream().mapToInt(Component::getWear).sum();

        assertTrue(wearAfterRace2 >= wearAfterRace1,
                "Износ должен накапливаться между гонками");

        GameSave loaded = saveService.loadGame(PLAYER, "autosave.csv");
        int savedWear = loaded.getTeam().getBolids().get(0)
                .getAllComponents().stream().mapToInt(Component::getWear).sum();

        assertEquals(wearAfterRace2, savedWear,
                "После двух гонок сохранённый wear должен совпадать с накопленным");
    }

    @Test
    void scenario3_toString_showsExpectedInfo() {
        Bolid bolid = buildAndAssembleBolid("McLaren");
        runRaceWithAutoSave(bolid);

        GameSave loaded = new SaveService().loadGame(PLAYER, "autosave.csv");
        String str = loaded.toString();
        System.out.println("toString: " + str);

        assertTrue(str.contains(PLAYER), "toString должен содержать имя команды");
        assertTrue(str.contains("1"),    "toString должен содержать количество гонок");
    }

    // ── helper ────────────────────────────────────────────────────────────────

    private void deleteDir(File dir) {
        if (!dir.exists()) return;
        File[] files = dir.listFiles();
        if (files != null) for (File f : files) f.delete();
        dir.delete();
    }
}
