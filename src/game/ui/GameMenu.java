package game.ui;

import game.data.TrackCatalog;
import game.domain.Bolid;
import game.domain.Engineer;
import game.domain.Pilot;
import game.domain.Race;
import game.domain.Team;
import game.domain.Track;
import game.domain.Weather;
import game.service.AssemblyService;
import game.service.BotGenerator;
import game.service.HireService;
import game.service.RaceService;
import game.service.ShopService;
import game.util.Ansi;

import java.util.ArrayList;
import java.util.List;

public class GameMenu {

    private final Team playerTeam;
    private boolean running;
    private final ShopService     shopService;
    private final HireService     hireService;
    private final AssemblyService assemblyService;
    private final RaceService     raceService;
    private final List<Race>      raceHistory = new ArrayList<>();
    private Weather               currentWeather;

    public GameMenu(Team playerTeam) {
        this.playerTeam      = playerTeam;
        this.running         = true;
        this.shopService     = new ShopService(playerTeam);
        this.hireService     = new HireService(playerTeam);
        this.assemblyService = new AssemblyService(playerTeam);
        this.raceService     = new RaceService();
        this.currentWeather  = Weather.random();
    }

    public void run() {
        while (running) {
            printMenu();
            int choice = ConsoleInput.readInt("Введите ваш выбор: ");
            handleChoice(choice);
        }
    }

    private void printMenu() {
        System.out.println(Ansi.bold("\n———————— ГЛАВНОЕ МЕНЮ ————————"));
        System.out.println(playerTeam);
        System.out.println("Погода: " + currentWeather);
        System.out.println(" ");
        System.out.println("1. Начать гонку");
        System.out.println("2. Купить компоненты");
        System.out.println("3. Собрать болид");
        System.out.println("4. Нанять инженера");
        System.out.println("5. Нанять пилота");
        System.out.println("6. Посмотреть болиды");
        System.out.println("7. Посмотреть пилотов");
        System.out.println("8. Посмотреть инженеров");
        System.out.println("9. Посмотреть инвентарь");
        System.out.println("10. Статистика гонок");
        System.out.println("11. Посмотреть другие команды");
        System.out.println("12. Посмотреть другие результаты");
        System.out.println("13. Выход");
    }

    private void handleChoice(int choice) {
        switch (choice) {
            case 1  -> startRace();
            case 2  -> shopService.openShop();
            case 3  -> assemblyService.assembleBolid();
            case 4  -> hireService.hireEngineer();
            case 5  -> hireService.hirePilot();
            case 6  -> System.out.println(playerTeam.getBolidsInfo());
            case 7  -> System.out.println(playerTeam.getPilotsInfo());
            case 8  -> System.out.println(playerTeam.getEngineersInfo());
            case 9  -> System.out.println(playerTeam.getInventoryInfo());
            case 10 -> showRaceStats();
            case 11 -> showOtherTeams();
            case 12 -> showOtherResults();
            case 13 -> {
                System.out.println("До встречи!");
                running = false;
            }
            default -> System.out.println("Неверный выбор");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Гонка
    // ─────────────────────────────────────────────────────────────────────────

    private void startRace() {
        if (!playerTeam.isReadyToRace()) {
            printNotReadyReason();
            return;
        }

        Track    track    = selectTrack();    if (track    == null) return;
        Bolid    bolid    = selectBolid();    if (bolid    == null) return;
        Pilot    pilot    = selectPilot();    if (pilot    == null) return;
        Engineer engineer = selectEngineer(); if (engineer == null) return;

        System.out.println(Ansi.bold("\nЗапускаем гонку на трассе: " + track.getName() + "..."));

        Race race = raceService.runRace(playerTeam, bolid, pilot, engineer, track, currentWeather);
        raceHistory.add(race);
        System.out.println(race);

        Weather nextWeather = Weather.random();
        if (nextWeather != currentWeather) {
            System.out.println("Погода изменилась: " + currentWeather + " → " + nextWeather);
        }
        currentWeather = nextWeather;
    }

    private void printNotReadyReason() {
        System.out.println(Ansi.bold("\nКоманда не готова к гонке:"));
        List<Bolid> ready = playerTeam.getBolids().stream()
            .filter(Bolid::isComplete).toList();
        if (ready.isEmpty())
            System.out.println("  — нет собранного болида (используйте «Собрать болид»)");
        if (playerTeam.getPilots().isEmpty())
            System.out.println("  — нет пилота (используйте «Нанять пилота»)");
        if (playerTeam.getEngineers().isEmpty())
            System.out.println("  — нет инженера (используйте «Нанять инженера»)");
    }

    private Track selectTrack() {
        List<Track> tracks = TrackCatalog.getAll();
        System.out.println(Ansi.bold("\nВыберите трассу:"));
        for (int i = 0; i < tracks.size(); i++) {
            Track t = tracks.get(i);
            System.out.printf("  %d. %-20s | %,d м | %d секций%n",
                i + 1, t.getName(), t.getTotalLength(), t.getSections().size());
        }
        int idx = ConsoleInput.readInt("Ваш выбор: ") - 1;
        if (idx < 0 || idx >= tracks.size()) {
            System.out.println("Неверный выбор трассы.");
            return null;
        }
        return tracks.get(idx);
    }

    private Bolid selectBolid() {
        List<Bolid> ready = playerTeam.getBolids().stream()
            .filter(Bolid::isComplete).toList();
        if (ready.size() == 1) return ready.get(0);

        System.out.println(Ansi.bold("\nВыберите болид:"));
        for (int i = 0; i < ready.size(); i++) {
            Bolid b = ready.get(i);
            System.out.printf("  %d. %-27s | Перфоманс: %d%n",
                i + 1, b.getName(), b.getPerformanceScore());
        }
        int idx = ConsoleInput.readInt("Ваш выбор: ") - 1;
        if (idx < 0 || idx >= ready.size()) {
            System.out.println("Неверный выбор болида.");
            return null;
        }
        return ready.get(idx);
    }

    private Pilot selectPilot() {
        List<Pilot> pilots = playerTeam.getPilots();
        if (pilots.size() == 1) return pilots.get(0);

        System.out.println(Ansi.bold("\nВыберите пилота:"));
        for (int i = 0; i < pilots.size(); i++) {
            Pilot p = pilots.get(i);
            System.out.printf("  %d. %-27s | Скилл: %d%n",
                i + 1, p.getName(), p.getSkill());
        }
        int idx = ConsoleInput.readInt("Ваш выбор: ") - 1;
        if (idx < 0 || idx >= pilots.size()) {
            System.out.println("Неверный выбор пилота.");
            return null;
        }
        return pilots.get(idx);
    }

    private Engineer selectEngineer() {
        List<Engineer> engineers = playerTeam.getEngineers();
        if (engineers.size() == 1) return engineers.get(0);

        System.out.println(Ansi.bold("\nВыберите инженера:"));
        for (int i = 0; i < engineers.size(); i++) {
            Engineer e = engineers.get(i);
            System.out.printf("  %d. %-27s | Квалификация: %d%n",
                i + 1, e.getName(), e.getQualification());
        }
        int idx = ConsoleInput.readInt("Ваш выбор: ") - 1;
        if (idx < 0 || idx >= engineers.size()) {
            System.out.println("Неверный выбор инженера.");
            return null;
        }
        return engineers.get(idx);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Статистика гонок
    // ─────────────────────────────────────────────────────────────────────────

    private void showRaceStats() {
        System.out.println(Ansi.bold("\n———————— СТАТИСТИКА ГОНОК ————————"));

        if (raceHistory.isEmpty()) {
            System.out.println("Гонок ещё не было.");
            return;
        }

        int    total       = raceHistory.size();
        int    wins        = 0;
        int    podiums     = 0;
        long   totalPrize  = 0;
        int    bestPos     = Integer.MAX_VALUE;
        int    posSum      = 0;

        for (Race r : raceHistory) {
            int pos = r.getPlayerPosition();
            posSum    += pos;
            totalPrize += r.getPrizeAwarded();
            if (pos == 1) wins++;
            if (pos <= 3) podiums++;
            if (pos < bestPos) bestPos = pos;
        }

        double avgPos = (double) posSum / total;

        System.out.printf("Гонок сыграно:       %d%n",   total);
        System.out.printf("Побед (1-е место):   %d%n",   wins);
        System.out.printf("Подиумов (топ-3):    %d%n",   podiums);
        System.out.printf("Лучшая позиция:      %d%n",   bestPos);
        System.out.printf("Средняя позиция:     %.1f%n", avgPos);
        System.out.printf("Заработано призовых: %,d руб.%n", totalPrize);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Другие команды
    // ─────────────────────────────────────────────────────────────────────────

    private void showOtherTeams() {
        System.out.println(Ansi.bold("\n———————— ДРУГИЕ КОМАНДЫ ————————"));
        System.out.println(BotGenerator.getLeagueTable());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // История результатов
    // ─────────────────────────────────────────────────────────────────────────

    private void showOtherResults() {
        System.out.println(Ansi.bold("\n———————— ИСТОРИЯ ГОНОК ————————"));

        if (raceHistory.isEmpty()) {
            System.out.println("Гонок ещё не было.");
            return;
        }

        // Brief list
        for (int i = 0; i < raceHistory.size(); i++) {
            Race r = raceHistory.get(i);
            String prize = r.getPrizeAwarded() > 0
                ? String.format("  +%,d руб.", r.getPrizeAwarded())
                : "";
            System.out.printf("  %2d. %-20s | %d-е место%s%n",
                i + 1, r.getTrack().getName(), r.getPlayerPosition(), prize);
        }

        System.out.println("\n  0. Назад");
        System.out.println("  N. Подробнее о гонке N");
        int choice = ConsoleInput.readInt("Ваш выбор: ");

        if (choice >= 1 && choice <= raceHistory.size()) {
            System.out.println(raceHistory.get(choice - 1));
        }
    }
}
