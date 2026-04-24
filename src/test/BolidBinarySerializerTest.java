import domain.*;
import org.junit.jupiter.api.*;
import saving.BolidBinarySerializer;
import service.SaveService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BolidBinarySerializerTest {

    private BolidBinarySerializer serializer;
    private File tempFile;

    @BeforeEach
    void setUp() throws IOException {
        serializer = new BolidBinarySerializer();
        tempFile = File.createTempFile("bolid_test_", ".bin");
        tempFile.deleteOnExit();
    }

    @AfterEach
    void tearDown() {
        tempFile.delete();
    }

    // round-trip

    @Test
    void bolidNamePreserved() throws IOException {
        Bolid original = new Bolid("Ракета-2000");
        serializer.save(original, tempFile);
        assertEquals("Ракета-2000", serializer.load(tempFile).getName());
    }

    @Test
    void emptyBolidRoundTrip() throws IOException {
        Bolid original = new Bolid("Пустой");
        serializer.save(original, tempFile);

        Bolid loaded = serializer.load(tempFile);
        assertEquals("Пустой", loaded.getName());
        assertTrue(loaded.getComponents().isEmpty());
        assertTrue(loaded.getExtras().isEmpty());
        assertTrue(loaded.getWeapons().isEmpty());
    }

    @Test
    void mainComponentsRoundTrip() throws IOException {
        Bolid original = new Bolid("Болид-1");
        original.installComponent(new Component("V8 Двигатель", ComponentType.ENGINE, 180_000, 88));
        original.installComponent(new Component("Трансмиссия", ComponentType.TRANSMISSION, 90_000, 75));
        original.installComponent(new Component("Подвеска", ComponentType.SUSPENSION, 60_000, 70));
        original.installComponent(new Component("Шасси", ComponentType.CHASSIS, 120_000, 80));
        original.installComponent(new Component("Обвес", ComponentType.AERO_PACKAGE, 75_000, 72));
        original.installComponent(new Component("Шины", ComponentType.TIRES, 40_000, 65));

        serializer.save(original, tempFile);
        Bolid loaded = serializer.load(tempFile);

        assertEquals(6, loaded.getComponents().size());
        assertNotNull(loaded.getComponent(ComponentType.ENGINE));
        assertNotNull(loaded.getComponent(ComponentType.TIRES));
        assertEquals("V8 Двигатель", loaded.getComponent(ComponentType.ENGINE).getName());
    }

    @Test
    void componentFieldsPreserved() throws IOException {
        Component engine = new Component("Мотор Pro", ComponentType.ENGINE, 200_000, 95, 3);
        engine.setWear(42);

        Bolid original = new Bolid("Тест");
        original.installComponent(engine);
        serializer.save(original, tempFile);

        Component loaded = serializer.load(tempFile).getComponent(ComponentType.ENGINE);
        assertEquals("Мотор Pro", loaded.getName());
        assertEquals(ComponentType.ENGINE, loaded.getType());
        assertEquals(200_000, loaded.getPrice());
        assertEquals(95, loaded.getPerformanceValue());
        assertEquals(42, loaded.getWear());
        assertEquals(3, loaded.getLevel());
    }

    @Test
    void extrasRoundTrip() throws IOException {
        Bolid original = new Bolid("С экстрой");
        original.addExtra(new Component("Спойлер", ComponentType.EXTRA, 10_000, 5));
        original.addExtra(new Component("Диффузор", ComponentType.EXTRA, 15_000, 8));

        serializer.save(original, tempFile);
        Bolid loaded = serializer.load(tempFile);

        assertEquals(2, loaded.getExtras().size());
        assertEquals("Спойлер", loaded.getExtras().get(0).getName());
        assertEquals("Диффузор", loaded.getExtras().get(1).getName());
    }

    @Test
    void wearPreservedAfterRoundTrip() throws IOException {
        Component tires = new Component("Изношенные шины", ComponentType.TIRES, 0, 50);
        tires.setWear(77);

        Bolid original = new Bolid("Worn");
        original.installComponent(tires);
        serializer.save(original, tempFile);

        int loadedWear = serializer.load(tempFile).getComponent(ComponentType.TIRES).getWear();
        assertEquals(77, loadedWear);
    }

    @Test
    void weaponsRoundTrip() throws IOException {
        Bolid original = new Bolid("Вооружённый");
        original.installWeapon(new Weapon("Пушка Mk1", WeaponType.RANGED, 50_000, 30, 1));

        serializer.save(original, tempFile);
        Bolid loaded = serializer.load(tempFile);

        assertEquals(1, loaded.getWeapons().size());
        Weapon w = loaded.getWeapons().get(WeaponType.RANGED);
        assertNotNull(w);
        assertEquals("Пушка Mk1", w.getName());
        assertEquals(30, w.getDamage());
        assertEquals(1, w.getLevel());
    }

    @Test
    void fullBolidRoundTrip() throws IOException {
        Bolid original = new Bolid("Полный болид");
        original.installComponent(new Component("Двигатель", ComponentType.ENGINE, 180_000, 88));
        original.installComponent(new Component("Трансмиссия", ComponentType.TRANSMISSION, 90_000, 75));
        original.installComponent(new Component("Подвеска", ComponentType.SUSPENSION, 60_000, 70));
        original.installComponent(new Component("Шасси", ComponentType.CHASSIS, 120_000, 80));
        original.installComponent(new Component("Обвес", ComponentType.AERO_PACKAGE, 75_000, 72));
        original.installComponent(new Component("Шины", ComponentType.TIRES, 40_000, 65));
        original.addExtra(new Component("Спойлер", ComponentType.EXTRA, 5_000, 3));
        original.installWeapon(new Weapon("Ракетница", WeaponType.RANGED, 70_000, 40, 2));

        serializer.save(original, tempFile);
        Bolid loaded = serializer.load(tempFile);

        assertEquals("Полный болид", loaded.getName());
        assertEquals(6, loaded.getComponents().size());
        assertEquals(1, loaded.getExtras().size());
        assertEquals(1, loaded.getWeapons().size());
        assertTrue(loaded.isComplete());
    }

    // файловый формат

    @Test
    void savedFileIsNotEmpty() throws IOException {
        serializer.save(new Bolid("Тест"), tempFile);
        assertTrue(tempFile.length() > 0, "Бинарный файл не должен быть пустым");
    }

    @Test
    void savedFileStartsWithMagicBytes() throws IOException {
        serializer.save(new Bolid("Тест"), tempFile);
        byte[] bytes = Files.readAllBytes(tempFile.toPath());

        assertEquals((byte) 0xB0, bytes[0], "Первый байт — магическое число 0xB0");
        assertEquals((byte) 0x11, bytes[1], "Второй байт — магическое число 0x11");
        assertEquals((byte) 1, bytes[2], "Третий байт — версия формата 1");
    }

    @Test
    void binaryIsSmallerThanEquivalentCsvEstimate() throws IOException {
        Bolid bolid = new Bolid("SF-24");
        bolid.installComponent(new Component("Двигатель Ferrari", ComponentType.ENGINE, 180_000, 88));
        bolid.installComponent(new Component("Трансмиссия F8", ComponentType.TRANSMISSION, 90_000, 75));
        bolid.installComponent(new Component("Подвеска Pro", ComponentType.SUSPENSION, 60_000, 70));
        bolid.installComponent(new Component("Шасси Carbon", ComponentType.CHASSIS, 120_000, 80));
        bolid.installComponent(new Component("Обвес Aero-X", ComponentType.AERO_PACKAGE, 75_000, 72));
        bolid.installComponent(new Component("Шины Soft", ComponentType.TIRES, 40_000, 65));

        serializer.save(bolid, tempFile);

        assertTrue(tempFile.length() > 0);
        assertTrue(tempFile.length() < 1024, "Файл болида не должен быть огромным");
    }

    // игровая опция: Сохранить болид (.bin)

    private static final String PLAYER = "test_bin_game_player";

    @AfterEach
    void tearDownSaveDir() {
        deleteDir(new File("saves" + File.separator + PLAYER));
    }

    @Test
    void saveBolidCreatesFileInCorrectDirectory() {
        Bolid bolid = new Bolid("SF-24");
        new SaveService().saveBolid(bolid, PLAYER);

        File expected = new File("saves" + File.separator + PLAYER
                + File.separator + "bolids" + File.separator + "SF-24.bin");
        assertTrue(expected.exists(), "Файл должен быть создан по пути saves/{player}/bolids/{name}.bin");
    }

    @Test
    void saveBolidCreatesBolidsSubdirectory() {
        new SaveService().saveBolid(new Bolid("Тест"), PLAYER);

        File bolidsDir = new File("saves" + File.separator + PLAYER + File.separator + "bolids");
        assertTrue(bolidsDir.isDirectory(), "Папка bolids/ должна создаваться автоматически");
    }

    @Test
    void saveBolidProducesLoadableFile() throws IOException {
        Bolid original = new Bolid("Ракета");
        new SaveService().saveBolid(original, PLAYER);

        File binFile = new File("saves" + File.separator + PLAYER
                + File.separator + "bolids" + File.separator + "Ракета.bin");
        Bolid loaded = new BolidBinarySerializer().load(binFile);
        assertEquals("Ракета", loaded.getName());
    }

    @Test
    void saveBolidPreservesAllComponents() throws IOException {
        Bolid original = new Bolid("Болид");
        original.installComponent(new Component("Двигатель", ComponentType.ENGINE, 180_000, 88));
        original.installComponent(new Component("Трансмиссия", ComponentType.TRANSMISSION, 90_000, 75));
        original.installComponent(new Component("Подвеска", ComponentType.SUSPENSION, 60_000, 70));
        original.installComponent(new Component("Шасси", ComponentType.CHASSIS, 120_000, 80));
        original.installComponent(new Component("Обвес", ComponentType.AERO_PACKAGE, 75_000, 72));
        original.installComponent(new Component("Шины", ComponentType.TIRES, 40_000, 65));
        new SaveService().saveBolid(original, PLAYER);

        File binFile = new File("saves" + File.separator + PLAYER
                + File.separator + "bolids" + File.separator + "Болид.bin");
        Bolid loaded = new BolidBinarySerializer().load(binFile);

        assertEquals(6, loaded.getComponents().size());
        assertTrue(loaded.isComplete());
        assertEquals("Двигатель", loaded.getComponent(ComponentType.ENGINE).getName());
    }

    @Test
    void saveBolidPreservesWear() throws IOException {
        Component tires = new Component("Шины", ComponentType.TIRES, 0, 60);
        tires.setWear(63);
        Bolid original = new Bolid("Worn");
        original.installComponent(tires);
        new SaveService().saveBolid(original, PLAYER);

        File binFile = new File("saves" + File.separator + PLAYER
                + File.separator + "bolids" + File.separator + "Worn.bin");
        int loadedWear = new BolidBinarySerializer().load(binFile)
                .getComponent(ComponentType.TIRES).getWear();
        assertEquals(63, loadedWear);
    }

    @Test
    void saveBolidOverwritesPreviousFile() throws IOException {
        SaveService service = new SaveService();
        File binFile = new File("saves" + File.separator + PLAYER
                + File.separator + "bolids" + File.separator + "Болид.bin");

        Bolid v1 = new Bolid("Болид");
        v1.installComponent(new Component("Мотор А", ComponentType.ENGINE, 0, 50));
        service.saveBolid(v1, PLAYER);

        Bolid v2 = new Bolid("Болид");
        v2.installComponent(new Component("Мотор Б", ComponentType.ENGINE, 0, 99));
        service.saveBolid(v2, PLAYER);

        Bolid loaded = new BolidBinarySerializer().load(binFile);
        assertEquals("Мотор Б", loaded.getComponent(ComponentType.ENGINE).getName(),
                "Второе сохранение должно перезаписать первое");
    }

    @Test
    void saveBolidWithSpacesInNameSanitizesFileName() {
        new SaveService().saveBolid(new Bolid("Red Bull 2024"), PLAYER);

        File bolidsDir = new File("saves" + File.separator + PLAYER + File.separator + "bolids");
        File[] files = bolidsDir.listFiles();
        assertNotNull(files);
        assertEquals(1, files.length);
        assertFalse(files[0].getName().contains(" "), "Пробелы в имени файла должны быть заменены");
    }

    // ошибки

    @Test
    void loadFromCorruptedFileThrows() throws IOException {
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(new byte[]{0x00, 0x00, 0x00});
        }
        assertThrows(IOException.class, () -> serializer.load(tempFile),
                "Загрузка повреждённого файла должна выбрасывать IOException");
    }

    @Test
    void loadFromWrongMagicThrows() throws IOException {
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(new byte[]{0x12, 0x34, 0x01});
        }
        assertThrows(IOException.class, () -> serializer.load(tempFile));
    }

    @Test
    void loadFromUnsupportedVersionThrows() throws IOException {
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(new byte[]{(byte) 0xB0, 0x11, 0x7F});
        }
        assertThrows(IOException.class, () -> serializer.load(tempFile));
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
