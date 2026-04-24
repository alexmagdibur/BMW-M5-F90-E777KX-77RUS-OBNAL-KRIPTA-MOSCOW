import domain.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import saving.GameSave;
import service.SaveService;
import service.ShopService;
import ui.ConsoleInput;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class EmergencyKitRaceTest {

    private static final String PLAYER = "test_emergency_kit_player";

    private final InputStream originalIn = System.in;

    // helpers

    // Команда с болидом, пилотом, инженером — без набора
    private static Team readyTeamWithoutKit() {
        Team team = new Team("TestTeam", 5_000_000L);

        Bolid bolid = new Bolid("TestBolid");
        bolid.installComponent(new Component("Двигатель",    ComponentType.ENGINE,       0, 80));
        bolid.installComponent(new Component("Трансмиссия",  ComponentType.TRANSMISSION, 0, 70));
        bolid.installComponent(new Component("Подвеска",     ComponentType.SUSPENSION,   0, 70));
        bolid.installComponent(new Component("Шасси",        ComponentType.CHASSIS,      0, 75));
        bolid.installComponent(new Component("Обвес",        ComponentType.AERO_PACKAGE, 0, 65));
        bolid.installComponent(new Component("Шины",         ComponentType.TIRES,        0, 60));
        team.addBolid(bolid);

        team.addPilot(new Pilot("Пилот", 100_000, 80));
        team.addEngineer(new Engineer("Инженер", 100_000, 75));

        return team;
    }

    private void mockInput(String input) {
        System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
        ConsoleInput.resetScanner();
    }

    @AfterEach
    void tearDown() {
        System.setIn(originalIn);
        ConsoleInput.resetScanner();
        deleteDir(new File("saves" + File.separator + PLAYER));
    }

    // race readiness

    @Test
    void teamWithoutKitCannotRace() {
        Team team = readyTeamWithoutKit();
        // набор пустой по умолчанию
        assertFalse(team.isReadyToRace(), "Без набора экстренной помощи гонка недоступна");
    }

    @Test
    void teamWithCompleteKitCanRace() {
        Team team = readyTeamWithoutKit();
        team.getEmergencyKit().setFirstAidKit(true);
        team.getEmergencyKit().setFireExtinguisher(true);
        team.getEmergencyKit().setWarningTriangle(true);

        assertTrue(team.isReadyToRace(), "Полный набор + болид + персонал = готов к гонке");
    }

    @Test
    void missingFirstAidKitBlocksRace() {
        Team team = readyTeamWithoutKit();
        team.getEmergencyKit().setFireExtinguisher(true);
        team.getEmergencyKit().setWarningTriangle(true);
        // аптечки нет

        assertFalse(team.isReadyToRace(), "Без аптечки гонка недоступна");
    }

    @Test
    void missingFireExtinguisherBlocksRace() {
        Team team = readyTeamWithoutKit();
        team.getEmergencyKit().setFirstAidKit(true);
        team.getEmergencyKit().setWarningTriangle(true);
        // огнетушителя нет

        assertFalse(team.isReadyToRace(), "Без огнетушителя гонка недоступна");
    }

    @Test
    void missingWarningTriangleBlocksRace() {
        Team team = readyTeamWithoutKit();
        team.getEmergencyKit().setFirstAidKit(true);
        team.getEmergencyKit().setFireExtinguisher(true);
        // знака нет

        assertFalse(team.isReadyToRace(), "Без знака аварийной остановки гонка недоступна");
    }

    @Test
    void emptyKitIsNotComplete() {
        EmergencyKit kit = new EmergencyKit(false, false, false);
        assertFalse(kit.isComplete());
    }

    @Test
    void kitWithAllItemsIsComplete() {
        EmergencyKit kit = new EmergencyKit(true, true, true);
        assertTrue(kit.isComplete());
    }

    @Test
    void kitWithTwoItemsIsNotComplete() {
        assertFalse(new EmergencyKit(true, true, false).isComplete());
        assertFalse(new EmergencyKit(true, false, true).isComplete());
        assertFalse(new EmergencyKit(false, true, true).isComplete());
    }

    // CSV save / load

    @Test
    void completeKitSavedAndLoadedCorrectly() {
        Team team = readyTeamWithoutKit();
        team.getEmergencyKit().setFirstAidKit(true);
        team.getEmergencyKit().setFireExtinguisher(true);
        team.getEmergencyKit().setWarningTriangle(true);

        SaveService service = new SaveService();
        service.autoSave(team, List.of(), PLAYER);

        GameSave loaded = service.loadGame(PLAYER, "autosave.csv");
        EmergencyKit kit = loaded.getTeam().getEmergencyKit();

        assertTrue(kit.hasFirstAidKit(),      "Аптечка должна сохраниться");
        assertTrue(kit.hasFireExtinguisher(),  "Огнетушитель должен сохраниться");
        assertTrue(kit.hasWarningTriangle(),   "Знак должен сохраниться");
        assertTrue(kit.isComplete(),           "Набор должен быть полным после загрузки");
    }

    @Test
    void emptyKitSavedAndLoadedCorrectly() {
        Team team = readyTeamWithoutKit();
        // набор не куплен

        SaveService service = new SaveService();
        service.autoSave(team, List.of(), PLAYER);

        GameSave loaded = service.loadGame(PLAYER, "autosave.csv");
        EmergencyKit kit = loaded.getTeam().getEmergencyKit();

        assertFalse(kit.hasFirstAidKit());
        assertFalse(kit.hasFireExtinguisher());
        assertFalse(kit.hasWarningTriangle());
        assertFalse(kit.isComplete());
    }

    @Test
    void partialKitSavedAndLoadedCorrectly() {
        Team team = readyTeamWithoutKit();
        team.getEmergencyKit().setFirstAidKit(true);
        team.getEmergencyKit().setWarningTriangle(true);
        // огнетушителя нет

        SaveService service = new SaveService();
        service.autoSave(team, List.of(), PLAYER);

        GameSave loaded = service.loadGame(PLAYER, "autosave.csv");
        EmergencyKit kit = loaded.getTeam().getEmergencyKit();

        assertTrue(kit.hasFirstAidKit());
        assertFalse(kit.hasFireExtinguisher(), "Огнетушитель не должен появиться после загрузки");
        assertTrue(kit.hasWarningTriangle());
    }

    @Test
    void loadedTeamWithoutKitStillCannotRace() {
        Team team = readyTeamWithoutKit();
        // набор не куплен

        SaveService service = new SaveService();
        service.autoSave(team, List.of(), PLAYER);

        GameSave loaded = service.loadGame(PLAYER, "autosave.csv");
        assertFalse(loaded.getTeam().isReadyToRace(),
                "После загрузки без набора гонка должна быть недоступна");
    }

    @Test
    void loadedTeamWithCompleteKitCanRace() {
        Team team = readyTeamWithoutKit();
        team.getEmergencyKit().setFirstAidKit(true);
        team.getEmergencyKit().setFireExtinguisher(true);
        team.getEmergencyKit().setWarningTriangle(true);

        SaveService service = new SaveService();
        service.autoSave(team, List.of(), PLAYER);

        GameSave loaded = service.loadGame(PLAYER, "autosave.csv");
        assertTrue(loaded.getTeam().isReadyToRace(),
                "После загрузки с полным набором гонка должна быть доступна");
    }

    // shop purchase

    @Test
    void buyingFirstAidKitDeductsBudget() {
        // 7x "0" — пропуск всех категорий компонентов, "1" — купить аптечку, "0" — выход из набора
        mockInput("0\n0\n0\n0\n0\n0\n0\n1\n0\n");

        Team team = new Team("Тест", 1_000_000L);
        new ShopService(team).openShop();

        assertEquals(995_000L, team.getBudget(), "Аптечка стоит 5 000 руб.");
        assertTrue(team.getEmergencyKit().hasFirstAidKit());
    }

    @Test
    void buyingFireExtinguisherDeductsBudget() {
        // "2" — купить огнетушитель
        mockInput("0\n0\n0\n0\n0\n0\n0\n2\n0\n");

        Team team = new Team("Тест", 1_000_000L);
        new ShopService(team).openShop();

        assertEquals(992_000L, team.getBudget(), "Огнетушитель стоит 8 000 руб.");
        assertTrue(team.getEmergencyKit().hasFireExtinguisher());
    }

    @Test
    void buyingWarningTriangleDeductsBudget() {
        // "3" — купить знак
        mockInput("0\n0\n0\n0\n0\n0\n0\n3\n0\n");

        Team team = new Team("Тест", 1_000_000L);
        new ShopService(team).openShop();

        assertEquals(998_000L, team.getBudget(), "Знак стоит 2 000 руб.");
        assertTrue(team.getEmergencyKit().hasWarningTriangle());
    }

    @Test
    void buyingAlreadyOwnedItemDoesNotDeductAgain() {
        // аптечка уже куплена заранее
        Team team = new Team("Тест", 1_000_000L);
        team.getEmergencyKit().setFirstAidKit(true);

        // "1" — попытка купить аптечку повторно, "0" — выход
        mockInput("0\n0\n0\n0\n0\n0\n0\n1\n0\n");
        new ShopService(team).openShop();

        assertEquals(1_000_000L, team.getBudget(), "Повторная покупка не должна списывать деньги");
    }

    @Test
    void buyingAllItemsCompletesKit() {
        // купить аптечку, потом огнетушитель, потом знак
        mockInput("0\n0\n0\n0\n0\n0\n0\n1\n2\n3\n");

        Team team = new Team("Тест", 1_000_000L);
        new ShopService(team).openShop();

        assertTrue(team.getEmergencyKit().isComplete(), "После покупки всех предметов набор полный");
        assertEquals(985_000L, team.getBudget(), "Итого списано 5000+8000+2000=15000 руб.");
    }

    @Test
    void cannotBuyKitItemWithInsufficientBudget() {
        // "2" — попытка купить огнетушитель (8 000), но денег не хватает
        mockInput("0\n0\n0\n0\n0\n0\n0\n2\n0\n");

        Team team = new Team("Тест", 5_000L); // хватает только на знак, но не на огнетушитель
        new ShopService(team).openShop();

        assertFalse(team.getEmergencyKit().hasFireExtinguisher(),
                "При нехватке денег предмет не должен покупаться");
        assertEquals(5_000L, team.getBudget(), "Бюджет не должен измениться");
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
