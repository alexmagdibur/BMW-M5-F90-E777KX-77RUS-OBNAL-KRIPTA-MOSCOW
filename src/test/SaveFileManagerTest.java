import org.junit.jupiter.api.*;
import saving.SaveFileManager;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SaveFileManagerTest {

    private SaveFileManager manager;

    // Уникальный игрок для каждого запуска — изолируем тесты от остатков на диске
    private static final String PLAYER = "test_player_junit";

    @BeforeEach
    void setUp() {
        manager = new SaveFileManager();
    }

    @AfterEach
    void tearDown() {
        // Удаляем созданные тестом файлы и папку, чтобы не засорять проект
        deleteDir(new File("saves" + File.separator + PLAYER));
    }

    // ── write + read ──────────────────────────────────────────────────────────

    @Test
    void writeAndReadRoundTrip() {
        String content = "Ferrari;5000000";
        manager.writeToFile(PLAYER, "team.csv", content);
        assertEquals(content, manager.readFromFile(PLAYER, "team.csv"));
    }

    @Test
    void writeCreatesPlayerDirectory() {
        manager.writeToFile(PLAYER, "data.txt", "test");
        File dir = new File("saves" + File.separator + PLAYER);
        assertTrue(dir.isDirectory(), "Папка saves/{player}/ должна быть создана");
    }

    @Test
    void multilineContentPreserved() {
        String content = "line1\nline2\nline3";
        manager.writeToFile(PLAYER, "multi.txt", content);
        assertEquals(content, manager.readFromFile(PLAYER, "multi.txt"));
    }

    @Test
    void overwriteFileWithNewContent() {
        manager.writeToFile(PLAYER, "save.csv", "first");
        manager.writeToFile(PLAYER, "save.csv", "second");
        assertEquals("second", manager.readFromFile(PLAYER, "save.csv"));
    }

    // ── read edge cases ───────────────────────────────────────────────────────

    @Test
    void readNonExistentFileReturnsEmpty() {
        String result = manager.readFromFile(PLAYER, "ghost.txt");
        assertEquals("", result, "Несуществующий файл должен вернуть пустую строку");
    }

    // ── listSaveFiles ─────────────────────────────────────────────────────────

    @Test
    void listSaveFilesReturnsWrittenFiles() {
        manager.writeToFile(PLAYER, "save1.csv", "a");
        manager.writeToFile(PLAYER, "save2.csv", "b");
        manager.writeToFile(PLAYER, "save3.csv", "c");

        List<String> files = manager.listSaveFiles(PLAYER);

        assertEquals(3, files.size(), "Должно быть 3 файла");
        assertTrue(files.contains("save1.csv"));
        assertTrue(files.contains("save2.csv"));
        assertTrue(files.contains("save3.csv"));
    }

    @Test
    void listSaveFilesForUnknownPlayerReturnsEmptyList() {
        List<String> files = manager.listSaveFiles("nobody_ever_created_this_player");
        assertNotNull(files);
        assertTrue(files.isEmpty(), "Несуществующий игрок — пустой список");
    }

    @Test
    void listSaveFilesCountMatchesWritten() {
        manager.writeToFile(PLAYER, "a.txt", "1");
        manager.writeToFile(PLAYER, "b.txt", "2");

        assertEquals(2, manager.listSaveFiles(PLAYER).size());
    }

    // ── saveExists ────────────────────────────────────────────────────────────

    @Test
    void saveExistsReturnsTrueAfterWrite() {
        manager.writeToFile(PLAYER, "check.csv", "data");
        assertTrue(manager.saveExists(PLAYER, "check.csv"));
    }

    @Test
    void saveExistsReturnsFalseForMissingFile() {
        assertFalse(manager.saveExists(PLAYER, "missing.csv"));
    }

    @Test
    void saveExistsReturnsFalseForMissingPlayer() {
        assertFalse(manager.saveExists("no_such_player_xyz", "file.csv"));
    }

    // ── helper ────────────────────────────────────────────────────────────────

    private void deleteDir(File dir) {
        if (!dir.exists()) return;
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) f.delete();
        }
        dir.delete();
    }
}
