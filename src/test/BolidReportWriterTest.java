import domain.*;
import org.junit.jupiter.api.*;
import saving.BolidReportWriter;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

public class BolidReportWriterTest {

    private static final String PLAYER = "test_report_player";

    private BolidReportWriter writer;

    @BeforeEach
    void setUp() {
        writer = new BolidReportWriter();
    }

    @AfterEach
    void tearDown() {
        deleteDir(new File("reports" + File.separator + PLAYER));
    }

    // файл создаётся

    @Test
    void reportFileIsCreated() throws IOException {
        File file = writer.writeReport(new Bolid("Тест"), PLAYER);
        assertTrue(file.exists(), "JSON-файл должен быть создан");
    }

    @Test
    void reportFileIsNotEmpty() throws IOException {
        File file = writer.writeReport(new Bolid("Тест"), PLAYER);
        assertTrue(file.length() > 0, "JSON-файл не должен быть пустым");
    }

    @Test
    void reportFileHasJsonExtension() throws IOException {
        File file = writer.writeReport(new Bolid("Тест"), PLAYER);
        assertTrue(file.getName().endsWith(".json"));
    }

    @Test
    void reportFileNameContainsBolidName() throws IOException {
        File file = writer.writeReport(new Bolid("SF-24"), PLAYER);
        assertTrue(file.getName().contains("SF-24"));
    }

    @Test
    void specialCharsInBolidNameAreSanitized() throws IOException {
        File file = writer.writeReport(new Bolid("Болид 2024!"), PLAYER);
        assertFalse(file.getName().contains(" "), "Пробел должен быть заменён");
        assertFalse(file.getName().contains("!"), "! должен быть заменён");
        assertTrue(file.exists());
    }

    // корень JSON

    @Test
    void rootContainsBolidName() throws IOException {
        File file = writer.writeReport(new Bolid("Ракета"), PLAYER);
        assertTrue(read(file).contains("\"name\": \"Ракета\""));
    }

    @Test
    void incompleteFlagFalseForEmptyBolid() throws IOException {
        File file = writer.writeReport(new Bolid("Пустой"), PLAYER);
        assertTrue(read(file).contains("\"complete\": false"));
    }

    @Test
    void completeFlagTrueForFullBolid() throws IOException {
        File file = writer.writeReport(fullBolid("Полный"), PLAYER);
        assertTrue(read(file).contains("\"complete\": true"));
    }

    @Test
    void performanceScoreWritten() throws IOException {
        Bolid bolid = new Bolid("Болид");
        bolid.installComponent(new Component("Двигатель", ComponentType.ENGINE, 0, 70));
        bolid.installComponent(new Component("Шины", ComponentType.TIRES, 0, 50));

        int expected = bolid.getPerformanceScore();
        assertTrue(read(writer.writeReport(bolid, PLAYER))
                       .contains("\"performanceScore\": " + expected));
    }

    @Test
    void hasWornComponentsFalseWhenFresh() throws IOException {
        File file = writer.writeReport(fullBolid("Новый"), PLAYER);
        assertTrue(read(file).contains("\"hasWornComponents\": false"));
    }

    @Test
    void hasWornComponentsTrueWhenWorn() throws IOException {
        Bolid bolid = new Bolid("Изношенный");
        Component engine = new Component("Двигатель", ComponentType.ENGINE, 0, 80);
        engine.setWear(75);
        bolid.installComponent(engine);

        assertTrue(read(writer.writeReport(bolid, PLAYER))
                       .contains("\"hasWornComponents\": true"));
    }

    // компоненты

    @Test
    void componentsKeyPresentInJson() throws IOException {
        assertTrue(read(writer.writeReport(new Bolid("Тест"), PLAYER))
                       .contains("\"components\""));
    }

    @Test
    void componentNameWritten() throws IOException {
        Bolid bolid = new Bolid("Болид");
        bolid.installComponent(new Component("Мотор V8", ComponentType.ENGINE, 0, 80));

        assertTrue(read(writer.writeReport(bolid, PLAYER))
                       .contains("\"name\": \"Мотор V8\""));
    }

    @Test
    void componentTypeWritten() throws IOException {
        Bolid bolid = new Bolid("Болид");
        bolid.installComponent(new Component("Мотор", ComponentType.ENGINE, 200_000, 95));

        assertTrue(read(writer.writeReport(bolid, PLAYER))
                       .contains("\"type\": \"ENGINE\""));
    }

    @Test
    void componentPriceWritten() throws IOException {
        Bolid bolid = new Bolid("Болид");
        bolid.installComponent(new Component("Мотор", ComponentType.ENGINE, 200_000, 95));

        assertTrue(read(writer.writeReport(bolid, PLAYER))
                       .contains("\"price\": 200000"));
    }

    @Test
    void componentPerformanceValueWritten() throws IOException {
        Bolid bolid = new Bolid("Болид");
        bolid.installComponent(new Component("Мотор", ComponentType.ENGINE, 0, 95));

        assertTrue(read(writer.writeReport(bolid, PLAYER))
                       .contains("\"performanceValue\": 95"));
    }

    @Test
    void componentWearWritten() throws IOException {
        Bolid bolid = new Bolid("Болид");
        Component tires = new Component("Шины", ComponentType.TIRES, 0, 50);
        tires.setWear(42);
        bolid.installComponent(tires);

        assertTrue(read(writer.writeReport(bolid, PLAYER))
                       .contains("\"wear\": 42"));
    }

    @Test
    void wornOutFalseWhenWearAtBoundary() throws IOException {
        Bolid bolid = new Bolid("Болид");
        Component c = new Component("Шины", ComponentType.TIRES, 0, 50);
        c.setWear(50);
        bolid.installComponent(c);

        assertTrue(read(writer.writeReport(bolid, PLAYER))
                       .contains("\"wornOut\": false"));
    }

    @Test
    void wornOutTrueWhenWearOver50() throws IOException {
        Bolid bolid = new Bolid("Болид");
        Component c = new Component("Шины", ComponentType.TIRES, 0, 50);
        c.setWear(51);
        bolid.installComponent(c);

        assertTrue(read(writer.writeReport(bolid, PLAYER))
                       .contains("\"wornOut\": true"));
    }

    @Test
    void emptyBolidHasEmptyComponentsArray() throws IOException {
        String json = read(writer.writeReport(new Bolid("Пустой"), PLAYER));
        assertTrue(json.contains("\"components\": []"));
    }

    // extras

    @Test
    void extrasKeyPresentInJson() throws IOException {
        assertTrue(read(writer.writeReport(new Bolid("Тест"), PLAYER))
                       .contains("\"extras\""));
    }

    @Test
    void extraNameWritten() throws IOException {
        Bolid bolid = new Bolid("Болид");
        bolid.addExtra(new Component("Спойлер", ComponentType.EXTRA, 5_000, 3));

        assertTrue(read(writer.writeReport(bolid, PLAYER))
                       .contains("\"name\": \"Спойлер\""));
    }

    @Test
    void twoExtrasWritten() throws IOException {
        Bolid bolid = new Bolid("Болид");
        bolid.addExtra(new Component("Спойлер", ComponentType.EXTRA, 5_000, 3));
        bolid.addExtra(new Component("Диффузор", ComponentType.EXTRA, 8_000, 5));

        String json = read(writer.writeReport(bolid, PLAYER));
        assertTrue(json.contains("\"name\": \"Спойлер\""));
        assertTrue(json.contains("\"name\": \"Диффузор\""));
    }

    // оружие

    @Test
    void weaponsKeyPresentInJson() throws IOException {
        assertTrue(read(writer.writeReport(new Bolid("Тест"), PLAYER))
                       .contains("\"weapons\""));
    }

    @Test
    void weaponFieldsWritten() throws IOException {
        Bolid bolid = new Bolid("Болид");
        bolid.installWeapon(new Weapon("Ракетница", WeaponType.RANGED, 70_000, 40, 2));

        String json = read(writer.writeReport(bolid, PLAYER));
        assertTrue(json.contains("\"name\": \"Ракетница\""));
        assertTrue(json.contains("\"type\": \"RANGED\""));
        assertTrue(json.contains("\"damage\": 40"));
        assertTrue(json.contains("\"price\": 70000"));
        assertTrue(json.contains("\"level\": 2"));
    }

    // кодировка и структура

    @Test
    void jsonFileIsUtf8WithCyrillic() throws IOException {
        File file = writer.writeReport(new Bolid("Кириллица"), PLAYER);
        String content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
        assertTrue(content.contains("Кириллица"));
    }

    @Test
    void jsonStartsWithCurlyBrace() throws IOException {
        String json = read(writer.writeReport(new Bolid("Тест"), PLAYER)).trim();
        assertTrue(json.startsWith("{"), "JSON должен начинаться с {");
        assertTrue(json.endsWith("}"), "JSON должен заканчиваться на }");
    }

    @Test
    void quotesInBolidNameAreEscaped() throws IOException {
        Bolid bolid = new Bolid("Болид \"Ракета\"");
        String json = read(writer.writeReport(bolid, PLAYER));
        assertTrue(json.contains("\\\"Ракета\\\""), "Кавычки внутри строки должны быть экранированы");
    }

    // игровая опция: Экспорт отчёта (.json)

    @Test
    void reportCreatedInPlayerSubdirectory() throws IOException {
        File file = writer.writeReport(new Bolid("Тест"), PLAYER);
        assertTrue(file.getParentFile().getName().equals(PLAYER),
                "Файл должен лежать в папке reports/{playerName}/");
    }

    @Test
    void reportRootDirectoryIsReports() throws IOException {
        File file = writer.writeReport(new Bolid("Тест"), PLAYER);
        assertEquals("reports", file.getParentFile().getParentFile().getName());
    }

    @Test
    void writingReportTwiceOverwritesFile() throws IOException {
        Bolid bolid = new Bolid("Болид");
        bolid.installComponent(new Component("Мотор А", ComponentType.ENGINE, 0, 50));
        File file1 = writer.writeReport(bolid, PLAYER);

        bolid.installComponent(new Component("Шины", ComponentType.TIRES, 0, 99));
        File file2 = writer.writeReport(bolid, PLAYER);

        assertEquals(file1.getAbsolutePath(), file2.getAbsolutePath(),
                "Повторный экспорт должен перезаписывать тот же файл");
        assertTrue(read(file2).contains("\"performanceValue\": 99"),
                "Файл должен содержать актуальные данные после перезаписи");
    }

    @Test
    void differentBolidsGetSeparateFiles() throws IOException {
        File f1 = writer.writeReport(new Bolid("Alpha"), PLAYER);
        File f2 = writer.writeReport(new Bolid("Beta"), PLAYER);

        assertNotEquals(f1.getAbsolutePath(), f2.getAbsolutePath(),
                "Каждый болид должен получить отдельный файл");
        assertTrue(f1.exists());
        assertTrue(f2.exists());
    }

    @Test
    void reportReflectsUpdatedWear() throws IOException {
        Bolid bolid = new Bolid("Болид");
        Component engine = new Component("Двигатель", ComponentType.ENGINE, 0, 80);
        engine.setWear(10);
        bolid.installComponent(engine);

        writer.writeReport(bolid, PLAYER);

        engine.setWear(75);
        File updated = writer.writeReport(bolid, PLAYER);

        String json = read(updated);
        assertTrue(json.contains("\"wear\": 75"), "Актуальный износ должен быть в файле");
        assertTrue(json.contains("\"wornOut\": true"), "wornOut должен обновиться");
        assertTrue(json.contains("\"hasWornComponents\": true"));
    }

    @Test
    void reportForBolidWithAllSectionsHasAllKeys() throws IOException {
        Bolid bolid = fullBolid("Полный");
        bolid.addExtra(new Component("Спойлер", ComponentType.EXTRA, 5_000, 3));
        bolid.installWeapon(new Weapon("Пушка", WeaponType.RANGED, 50_000, 30, 1));

        String json = read(writer.writeReport(bolid, PLAYER));
        assertTrue(json.contains("\"components\""));
        assertTrue(json.contains("\"extras\""));
        assertTrue(json.contains("\"weapons\""));
        assertTrue(json.contains("\"complete\": true"));
        assertTrue(json.contains("\"name\": \"Полный\""));
    }

    // escapeJson

    @Test
    void escapeJsonHandlesQuotes() {
        assertEquals("he said \\\"hi\\\"", BolidReportWriter.escapeJson("he said \"hi\""));
    }

    @Test
    void escapeJsonHandlesBackslash() {
        assertEquals("a\\\\b", BolidReportWriter.escapeJson("a\\b"));
    }

    @Test
    void escapeJsonHandlesNewline() {
        assertEquals("line1\\nline2", BolidReportWriter.escapeJson("line1\nline2"));
    }

    @Test
    void escapeJsonPlainStringUnchanged() {
        assertEquals("SF-24", BolidReportWriter.escapeJson("SF-24"));
    }

    // helpers

    private static Bolid fullBolid(String name) {
        Bolid b = new Bolid(name);
        b.installComponent(new Component("Двигатель", ComponentType.ENGINE, 180_000, 88));
        b.installComponent(new Component("Трансмиссия", ComponentType.TRANSMISSION, 90_000, 75));
        b.installComponent(new Component("Подвеска", ComponentType.SUSPENSION, 60_000, 70));
        b.installComponent(new Component("Шасси", ComponentType.CHASSIS, 120_000, 80));
        b.installComponent(new Component("Обвес", ComponentType.AERO_PACKAGE, 75_000, 72));
        b.installComponent(new Component("Шины", ComponentType.TIRES, 40_000, 65));
        return b;
    }

    private static String read(File file) throws IOException {
        return Files.readString(file.toPath(), StandardCharsets.UTF_8);
    }

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
