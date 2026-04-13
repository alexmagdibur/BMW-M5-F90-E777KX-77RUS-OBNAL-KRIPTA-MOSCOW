import domain.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ui.ConsoleInput;
import ui.GameMenu;
import ui.TrackEditor;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


public class TrackSelectionTest {

    private static final String TEST_PLAYER = "__test_track_selection__";

    private InputStream originalIn;
    private PrintStream originalOut;
    private ByteArrayOutputStream captured;

    @BeforeEach
    void setUp() {
        originalIn = System.in;
        originalOut = System.out;
        captured = new ByteArrayOutputStream();
        System.setOut(new PrintStream(captured));
        System.setIn(new ByteArrayInputStream(new byte[0]));
        ConsoleInput.resetScanner();
        cleanupTestDirs();
    }

    @AfterEach
    void tearDown() {
        System.setIn(originalIn);
        System.setOut(originalOut);
        ConsoleInput.resetScanner();
        cleanupTestDirs();
    }

    // helpers

    private static Team readyTeam() {
        Team team = new Team(TEST_PLAYER, 10_000_000L);
        Bolid bolid = new Bolid("Болид");
        bolid.installComponent(new Component("Двигатель", ComponentType.ENGINE, 0, 60));
        bolid.installComponent(new Component("Трансмиссия", ComponentType.TRANSMISSION, 0, 55));
        bolid.installComponent(new Component("Подвеска", ComponentType.SUSPENSION, 0, 50));
        bolid.installComponent(new Component("Шасси", ComponentType.CHASSIS, 0, 50));
        bolid.installComponent(new Component("Обвесы", ComponentType.AERO_PACKAGE, 0, 50));
        bolid.installComponent(new Component("Шины", ComponentType.TIRES, 0, 60));
        team.addBolid(bolid);
        team.addPilot(new Pilot("Пилот", 0, 75));
        team.addEngineer(new Engineer("Инженер", 0, 60));
        return team;
    }

    private void cleanupTestDirs() {
        deleteDir(new File("tracks" + File.separator + TEST_PLAYER));
        deleteDir(new File("saves" + File.separator + TEST_PLAYER));
    }

    private void deleteDir(File dir) {
        if (!dir.exists()) return;
        File[] files = dir.listFiles();
        if (files != null) for (File f : files) f.delete();
        dir.delete();
    }

    private void feedInput(String input) {
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        ConsoleInput.resetScanner();
    }

    // сценарий 1: трасса спрашивается перед каждой гонкой

    @Test
    void trackIsAskedBeforeEachRace() {
        feedInput("1\n1\n1\n" + // гонка 1: меню -> гонка, режим -> Обычный, трасса 1
                  "1\n1\n1\n" + // гонка 2: меню -> гонка, режим -> Обычный, трасса 1
                  "16\n15\n"); // выход при любой погоде

        GameMenu menu = new GameMenu(readyTeam(), TEST_PLAYER, new ArrayList<>(), new ArrayList<>());
        menu.run();

        String out = captured.toString();
        // «выберите трассу:» должно появиться дважды — по разу на гонку
        long asks = out.lines().filter(l -> l.contains("Выберите трассу:")).count();
        assertTrue(asks >= 2, "Трасса должна запрашиваться перед каждой гонкой, нашлось запросов: " + asks);
    }

    // сценарий 2: пользовательский трек виден в списке трасс

    @Test
    void customTrackAppearsInTrackList() {
        // Создаём трек через редактор
        feedInput(String.join("\n",
            "1", // создать трек
            "Моя Трасса", // название
            "1", // 1 секция
            "1", "1000", // STRAIGHT 1000
            "5" // выйти
        ) + "\n");

        TrackEditor editor = new TrackEditor(TEST_PLAYER);
        editor.open();

        // запускаем GameMenu с пользовательским треком
        // нажимаем «1» (гонка), затем выбираем последний трек в списке
        // (каталог + 1 пользовательский — он последний)
        List<Track> custom = editor.getCustomTracks();
        assertEquals(1, custom.size(), "Должен быть один пользовательский трек");

        // используем каталожный трек (пункт 1) чтобы не считать индекс
        feedInput("1\n1\n1\n16\n15\n"); // гонка, режим→Обычный, трасса 1, выход

        GameMenu menu = new GameMenu(readyTeam(), TEST_PLAYER, new ArrayList<>(), custom);
        menu.run();

        String out = captured.toString();
        // пользовательский трек должен быть в списке (упоминается при выводе выбора)
        assertTrue(out.contains("Моя Трасса"),
            "Пользовательский трек должен быть виден в списке трасс");
    }

    // сценарий 3: трек из редактора сохраняется и загружается

    @Test
    void customTrackCreatedInEditorSurvivesRestart() {
        feedInput(String.join("\n",
            "1", // создать трек
            "Трасса из редактора", // название
            "2", // 2 секции
            "1", "800", // STRAIGHT 800
            "3", "400", // CLIMB 400
            "5" // выйти
        ) + "\n");

        TrackEditor editor1 = new TrackEditor(TEST_PLAYER);
        editor1.open();
        assertEquals(1, editor1.getCustomTracks().size());

        // «перезапуск»: новый TrackEditor только читает файл, console не нужна
        feedInput("");
        TrackEditor editor2 = new TrackEditor(TEST_PLAYER);
        List<Track> loaded = editor2.getCustomTracks();

        assertEquals(1, loaded.size(), "Трек должен загрузиться из файла");
        assertEquals("Трасса из редактора", loaded.get(0).getName());
        assertEquals(SectionType.STRAIGHT, loaded.get(0).getSections().get(0).getType());
        assertEquals(800, loaded.get(0).getSections().get(0).getLength());
        assertEquals(SectionType.CLIMB, loaded.get(0).getSections().get(1).getType());
        assertEquals(400, loaded.get(0).getSections().get(1).getLength());
    }
}
