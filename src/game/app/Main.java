package app;

import domain.RaceResult;
import domain.Team;
import saving.GameSave;
import service.SaveService;
import ui.ConsoleInput;
import ui.GameMenu;
import ui.TrackEditor;
import util.Ansi;

import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        System.out.println(Ansi.bold("———————— ZOV AUTO: МОЖНО, А ЗАЧЕМ ————————"));

        String name = ConsoleInput.readLine("Введите имя вашей команды: ");

        SaveService saveService = new SaveService();
        Team team;
        List<RaceResult> history = new ArrayList<>();

        List<String> saves = saveService.getAvailableSaves(name);
        if (!saves.isEmpty()) {
            System.out.println(Ansi.bold("\nНайдены сохранения для команды «" + name + "»:"));
            System.out.println("  1. Новая игра");
            System.out.println("  2. Загрузить сохранение");
            int startChoice = ConsoleInput.readInt("Ваш выбор: ");

            if (startChoice == 2) {
                team = loadSave(saveService, name, saves, history);
            } else {
                team = createPlayerTeam(name);
            }
        } else {
            team = createPlayerTeam(name);
        }

        // Редактор треков
        String editorAnswer = ConsoleInput.readLine("Запустить редактор треков? (да/нет): ").trim().toLowerCase();
        TrackEditor editor = new TrackEditor(name);
        if (editorAnswer.equals("да")) {
            editor.open();
        }

        GameMenu menu = new GameMenu(team, name, history, editor.getCustomTracks());
        menu.run();

        ConsoleInput.close();
    }

    private static Team loadSave(SaveService saveService, String name,
                                 List<String> saves, List<RaceResult> history) {
        System.out.println(Ansi.bold("\nДоступные сохранения:"));
        for (int i = 0; i < saves.size(); i++) {
            System.out.printf("  %d. %s%n", i + 1, saves.get(i));
        }
        int idx = ConsoleInput.readInt("Выберите сохранение: ") - 1;

        if (idx < 0 || idx >= saves.size()) {
            System.out.println("Неверный выбор, начинаем новую игру.");
            return createPlayerTeam(name);
        }

        GameSave save = saveService.loadGame(name, saves.get(idx));
        history.addAll(save.getRaceHistory());
        System.out.println("Загружено: " + save);
        return save.getTeam();
    }

    public static Team createPlayerTeam(String teamName) {
        return new Team(teamName, 10_000_000);
    }
}
