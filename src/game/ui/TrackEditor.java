package ui;

import data.TrackCatalog;
import domain.SectionType;
import domain.Track;
import domain.TrackSection;

import java.util.ArrayList;
import java.util.List;

public class TrackEditor {

    private final List<Track> customTracks = new ArrayList<>();

    public void open() {
        System.out.println("\n=== Редактор трасс ===");
        while (true) {
            System.out.println("\n1. Создать новый трек");
            System.out.println("2. Просмотреть все треки");
            System.out.println("3. Редактировать трек");
            System.out.println("4. Удалить трек");
            System.out.println("5. Выйти из редактора");
            int choice = ConsoleInput.readInt("Ваш выбор: ");
            switch (choice) {
                case 1 -> createTrack();
                case 2 -> showAllTracks(TrackCatalog.getAll(), customTracks);
                case 3 -> editTrack();
                case 4 -> deleteTrack();
                case 5 -> { return; }
                default -> System.out.println("Неверный выбор.");
            }
        }
    }

    public void createTrack() {
        String name = ConsoleInput.readLine("Название трека: ");
        int count = ConsoleInput.readInt("Количество секций: ");
        List<TrackSection> sections = readSections(count);
        customTracks.add(new Track(name, sections));
        System.out.println("Трек «" + name + "» создан.");
    }

    public void showAllTracks(List<Track> catalog, List<Track> custom) {
        System.out.println("\n--- Каталожные треки ---");
        for (Track t : catalog) {
            System.out.println(t);
            System.out.println();
        }
        System.out.println("--- Пользовательские треки ---");
        if (custom.isEmpty()) {
            System.out.println("  (нет пользовательских треков)");
        } else {
            for (Track t : custom) {
                System.out.println(t);
                System.out.println();
            }
        }
    }

    public void editTrack() {
        if (customTracks.isEmpty()) {
            System.out.println("Нет пользовательских треков для редактирования.");
            return;
        }
        int idx = selectCustomTrack("редактирования");
        if (idx < 0) return;
        int count = ConsoleInput.readInt("Количество секций: ");
        List<TrackSection> sections = readSections(count);
        Track old = customTracks.get(idx);
        customTracks.set(idx, new Track(old.getName(), sections));
        System.out.println("Трек «" + old.getName() + "» обновлён.");
    }

    public void deleteTrack() {
        if (customTracks.isEmpty()) {
            System.out.println("Нет пользовательских треков для удаления.");
            return;
        }
        int idx = selectCustomTrack("удаления");
        if (idx < 0) return;
        Track removed = customTracks.remove(idx);
        System.out.println("Трек «" + removed.getName() + "» удалён.");
    }

    public List<Track> getCustomTracks() {
        return customTracks;
    }

    private int selectCustomTrack(String action) {
        System.out.println("\nПользовательские треки:");
        for (int i = 0; i < customTracks.size(); i++) {
            System.out.printf("  %d. %s%n", i + 1, customTracks.get(i).getName());
        }
        int choice = ConsoleInput.readInt("Выберите трек для " + action + ": ") - 1;
        if (choice < 0 || choice >= customTracks.size()) {
            System.out.println("Неверный выбор.");
            return -1;
        }
        return choice;
    }

    private List<TrackSection> readSections(int count) {
        SectionType[] types = SectionType.values();
        List<TrackSection> sections = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            System.out.printf("Секция %d:%n", i);
            System.out.println("  Тип: 1) STRAIGHT  2) TURN  3) CLIMB  4) DESCENT");
            int typeIdx = ConsoleInput.readInt("  Выберите тип: ") - 1;
            if (typeIdx < 0 || typeIdx >= types.length) typeIdx = 0;
            int length = ConsoleInput.readInt("  Длина (м): ");
            sections.add(new TrackSection(types[typeIdx], length));
        }
        return sections;
    }
}
