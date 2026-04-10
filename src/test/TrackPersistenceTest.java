import domain.SectionType;
import domain.Track;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import saving.TrackFileManager;
import ui.ConsoleInput;
import ui.TrackEditor;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// Тест: создать трек в редакторе, «перезапустить» (новый TrackEditor), открыть редактор — трек на месте.
public class TrackPersistenceTest {

    private static final String TEST_PLAYER = "__test_persistence_player__";

    private InputStream  originalIn;
    private PrintStream  originalOut;

    @BeforeEach
    void setUp() {
        originalIn  = System.in;
        originalOut = System.out;
        System.setOut(new PrintStream(new ByteArrayOutputStream()));
        cleanupTestFiles();
    }

    @AfterEach
    void tearDown() {
        System.setIn(originalIn);
        System.setOut(originalOut);
        ConsoleInput.resetScanner();
        cleanupTestFiles();
    }

    @Test
    void trackSurvivesEditorRestart() {
        // Шаг 1: создаём трек через редактор с playerName
        String createInput = String.join("\n",
            "1", // меню: создать трек
            "Трасса Ужаса",  // название
            "3", // количество секций
            "1", // секция 1 тип: STRAIGHT
            "500", // секция 1 длина
            "2", // секция 2 тип: TURN
            "200", // секция 2 длина
            "3", // секция 3 тип: CLIMB
            "300", // секция 3 длина
            "5" // меню: выйти
        ) + "\n";

        System.setIn(new ByteArrayInputStream(createInput.getBytes()));
        ConsoleInput.resetScanner();

        TrackEditor editor1 = new TrackEditor(TEST_PLAYER);
        editor1.open();

        assertEquals(1, editor1.getCustomTracks().size(), "Трек должен быть создан в первом редакторе");

        // Шаг 2: «перезапуск» — новый экземпляр TrackEditor с тем же playerName
        ConsoleInput.resetScanner();
        System.setIn(originalIn); // пустой ввод не нужен — только конструктор

        TrackEditor editor2 = new TrackEditor(TEST_PLAYER);
        List<Track> loaded = editor2.getCustomTracks();

        assertEquals(1, loaded.size(), "После перезапуска трек должен загрузиться из файла");

        Track track = loaded.get(0);
        assertEquals("Трасса Ужаса", track.getName());
        assertEquals(3, track.getSections().size());
        assertEquals(SectionType.STRAIGHT, track.getSections().get(0).getType());
        assertEquals(500, track.getSections().get(0).getLength());
        assertEquals(SectionType.TURN, track.getSections().get(1).getType());
        assertEquals(200, track.getSections().get(1).getLength());
        assertEquals(SectionType.CLIMB, track.getSections().get(2).getType());
        assertEquals(300, track.getSections().get(2).getLength());
    }

    private void cleanupTestFiles() {
        File dir = new File("tracks" + File.separator + TEST_PLAYER);
        if (dir.exists()) {
            for (File f : dir.listFiles()) f.delete();
            dir.delete();
        }
    }
}
