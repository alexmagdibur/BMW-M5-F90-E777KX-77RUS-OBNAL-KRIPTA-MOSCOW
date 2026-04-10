import domain.SectionType;
import domain.Track;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ui.ConsoleInput;
import ui.TrackEditor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TrackEditorTest {

    private InputStream originalIn;
    private PrintStream originalOut;
    private ByteArrayOutputStream captured;

    @BeforeEach
    void setUp() {
        originalIn  = System.in;
        originalOut = System.out;
        captured = new ByteArrayOutputStream();
        System.setOut(new PrintStream(captured));
    }

    @AfterEach
    void tearDown() {
        System.setIn(originalIn);
        System.setOut(originalOut);
        ConsoleInput.resetScanner();
    }


    // создать трек «Тест-Трек» (3 секции: STRAIGHT/1000, TURN/500, CLIMB/300)
    // просмотреть все треки
    // выйти

    @Test
    void createTrackAndVerifyItAppearsInList() {
        String input = String.join("\n",
            "1", // меню: создать трек
            "Тест-Трек",  // название
            "3", // количество секций
            "1", // секция 1 тип: STRAIGHT
            "1000", // секция 1 длина
            "2", // секция 2 тип: TURN
            "500", // секция 2 длина
            "3", // секция 3 тип: CLIMB
            "300", // секция 3 длина
            "2", // меню: просмотреть все треки
            "5" // меню: выйти
        ) + "\n";

        System.setIn(new ByteArrayInputStream(input.getBytes()));
        ConsoleInput.resetScanner();

        TrackEditor editor = new TrackEditor();
        editor.open();

        // Проверяем customTracks через геттер
        List<Track> custom = editor.getCustomTracks();
        assertEquals(1, custom.size(), "Должен быть ровно один пользовательский трек");

        Track track = custom.get(0);
        assertEquals("Тест-Трек", track.getName());
        assertEquals(3, track.getSections().size());
        assertEquals(SectionType.STRAIGHT, track.getSections().get(0).getType());
        assertEquals(1000, track.getSections().get(0).getLength());
        assertEquals(SectionType.TURN, track.getSections().get(1).getType());
        assertEquals(500, track.getSections().get(1).getLength());
        assertEquals(SectionType.CLIMB, track.getSections().get(2).getType());
        assertEquals(300, track.getSections().get(2).getLength());

        // Проверяем, что вывод содержит название трека (пункт 2 — просмотр)
        String output = captured.toString();
        assertTrue(output.contains("Тест-Трек"), "Вывод должен содержать название нового трека");
        assertTrue(output.contains("Пользовательские треки"), "Вывод должен содержать секцию пользовательских треков");
    }
}
