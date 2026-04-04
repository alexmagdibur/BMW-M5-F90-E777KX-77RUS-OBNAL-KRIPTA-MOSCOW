package game.ui;

import game.data.TrackCatalog;
import game.domain.Bolid;
import game.domain.Component;
import game.domain.ComponentType;
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
import game.service.WearService;

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
        System.out.println("13. Обслуживание болида");
        if (currentWeather == Weather.SOLAR_ECLIPSE) {
            System.out.println("14. Вычислить оборотней");
            System.out.println("15. Выход");
        } else {
            System.out.println("14. Выход");
        }
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
            case 13 -> maintenanceBolid();
            case 14 -> {
                if (currentWeather == Weather.SOLAR_ECLIPSE) {
                    werewolfHunt();
                } else {
                    System.out.println("До встречи!");
                    running = false;
                }
            }
            case 15 -> {
                if (currentWeather == Weather.SOLAR_ECLIPSE) {
                    System.out.println("До встречи!");
                    running = false;
                } else {
                    System.out.println("Неверный выбор");
                }
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

        List<Component> maxWorn = bolid.getAllComponents().stream()
            .filter(c -> c.getType() != ComponentType.EXTRA && c.getWear() >= 100).toList();
        if (!maxWorn.isEmpty()) {
            System.out.println(Ansi.bold("СТАРТ НЕВОЗМОЖЕН: следующие компоненты полностью изношены (100%):"));
            for (Component c : maxWorn)
                System.out.printf("  — %s%n", c.getName());
            System.out.println("Замените или почините их в пункте «Обслуживание болида».");
            return;
        }

        if (bolid.hasWornComponents()) {
            System.out.println(Ansi.bold("ВНИМАНИЕ: болид имеет компоненты с износом > 50%. Возможен инцидент!"));
        }

        System.out.println(Ansi.bold("\nЗапускаем гонку на трассе: " + track.getName() + "..."));

        Race race = raceService.runRace(playerTeam, bolid, pilot, engineer, track, currentWeather);
        raceHistory.add(race);
        System.out.println(race);

        WearService.applyWear(bolid, track);
        printWearReport(bolid);

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
    // Износ
    // ─────────────────────────────────────────────────────────────────────────

    private void printWearReport(Bolid bolid) {
        System.out.println(Ansi.bold("\nИзнос компонентов после гонки:"));
        boolean anyWorn = false;
        for (Component c : bolid.getAllComponents()) {
            if (c.getType() == ComponentType.EXTRA) continue;
            String warn = c.isWornOut() ? " ⚠ ВЫСОКИЙ ИЗНОС" : "";
            System.out.printf("  %-25s %3d%%%s%n", c.getName(), c.getWear(), warn);
            if (c.isWornOut()) anyWorn = true;
        }
        if (anyWorn) {
            System.out.println(Ansi.bold("ПРЕДУПРЕЖДЕНИЕ: компоненты с износом > 50% снижают надёжность."));
            System.out.println("Рекомендуется починить или заменить их (пункт «Обслуживание болида»).");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Обслуживание болида
    // ─────────────────────────────────────────────────────────────────────────

    private void maintenanceBolid() {
        System.out.println(Ansi.bold("\n———————— ОБСЛУЖИВАНИЕ БОЛИДА ————————"));

        List<Bolid> ready = playerTeam.getBolids().stream()
            .filter(Bolid::isComplete).toList();
        if (ready.isEmpty()) {
            System.out.println("Нет собранных болидов.");
            return;
        }

        Bolid bolid;
        if (ready.size() == 1) {
            bolid = ready.get(0);
        } else {
            System.out.println("Выберите болид:");
            for (int i = 0; i < ready.size(); i++)
                System.out.printf("  %d. %s%n", i + 1, ready.get(i).getName());
            int idx = ConsoleInput.readInt("Ваш выбор: ") - 1;
            if (idx < 0 || idx >= ready.size()) { System.out.println("Неверный выбор."); return; }
            bolid = ready.get(idx);
        }

        boolean loop = true;
        while (loop) {
            System.out.println(Ansi.bold("\nБолид: " + bolid.getName()));
            List<Component> components = bolid.getAllComponents().stream()
                .filter(c -> c.getType() != ComponentType.EXTRA).toList();
            for (int i = 0; i < components.size(); i++) {
                Component c = components.get(i);
                String flag = c.isWornOut() ? " ⚠" : "";
                System.out.printf("  %d. %-25s %3d%%%s%n", i + 1, c.getName(), c.getWear(), flag);
            }
            System.out.println("\n  R. Починить компонент");
            System.out.println("  Z. Заменить компонент");
            System.out.println("  0. Назад");

            String choice = ConsoleInput.readLine("Ваш выбор: ").trim().toUpperCase();
            switch (choice) {
                case "R" -> repairComponent(bolid, components);
                case "Z" -> replaceComponent(bolid, components);
                case "0" -> loop = false;
                default  -> System.out.println("Неверный выбор.");
            }
        }
    }

    private void repairComponent(Bolid bolid, List<Component> components) {
        if (playerTeam.getEngineers().isEmpty()) {
            System.out.println("Нет инженера для ремонта.");
            return;
        }

        System.out.println("Выберите компонент для ремонта:");
        for (int i = 0; i < components.size(); i++) {
            Component c = components.get(i);
            System.out.printf("  %d. %-25s %3d%%%n", i + 1, c.getName(), c.getWear());
        }
        System.out.println("  0. Отмена");
        int idx = ConsoleInput.readInt("Ваш выбор: ") - 1;
        if (idx < 0 || idx >= components.size()) return;

        Engineer engineer = playerTeam.getEngineers().get(0);
        if (playerTeam.getEngineers().size() > 1) {
            System.out.println("Выберите инженера:");
            List<Engineer> engineers = playerTeam.getEngineers();
            for (int i = 0; i < engineers.size(); i++) {
                Engineer e = engineers.get(i);
                long cost = WearService.repairCost(e);
                int amount = WearService.repairAmount(e);
                System.out.printf("  %d. %-25s | Скилл: %2d | Снизит износ на %d%% | Стоимость: %,d руб.%n",
                    i + 1, e.getName(), e.getQualification(), amount, cost);
            }
            int eIdx = ConsoleInput.readInt("Ваш выбор: ") - 1;
            if (eIdx < 0 || eIdx >= engineers.size()) return;
            engineer = engineers.get(eIdx);
        }

        Component c = components.get(idx);
        long cost = WearService.repairCost(engineer);
        int amount = WearService.repairAmount(engineer);

        System.out.printf("Ремонт «%s»: снизит износ на %d%%, стоимость %,d руб. Подтвердить? (1-да / 0-нет): ",
            c.getName(), amount, cost);
        int confirm = ConsoleInput.readInt("");
        if (confirm != 1) return;

        if (!playerTeam.canAfford(cost)) {
            System.out.printf("Недостаточно средств. Нужно %,d руб., есть %,d руб.%n",
                cost, playerTeam.getBudget());
            return;
        }

        playerTeam.spend(cost);
        c.setWear(Math.max(0, c.getWear() - amount));
        System.out.printf("Отремонтировано. Износ «%s»: %d%%%n", c.getName(), c.getWear());
    }

    private void replaceComponent(Bolid bolid, List<Component> components) {
        System.out.println("Выберите слот для замены:");
        for (int i = 0; i < components.size(); i++) {
            Component c = components.get(i);
            System.out.printf("  %d. %-25s %3d%%  [%s]%n",
                i + 1, c.getName(), c.getWear(), c.getType());
        }
        System.out.println("  0. Отмена");
        int idx = ConsoleInput.readInt("Ваш выбор: ") - 1;
        if (idx < 0 || idx >= components.size()) return;

        Component old = components.get(idx);
        ComponentType slotType = old.getType();

        List<Component> candidates = playerTeam.getInventory().stream()
            .filter(c -> c.getType() == slotType).toList();
        if (candidates.isEmpty()) {
            System.out.printf("В инвентаре нет компонентов типа «%s».%n", slotType);
            return;
        }

        System.out.println("Выберите замену из инвентаря:");
        for (int i = 0; i < candidates.size(); i++) {
            Component c = candidates.get(i);
            System.out.printf("  %d. %-25s | Перфоманс: %3d | Износ: %3d%%%n",
                i + 1, c.getName(), c.getPerformanceValue(), c.getWear());
        }
        System.out.println("  0. Отмена");
        int cIdx = ConsoleInput.readInt("Ваш выбор: ") - 1;
        if (cIdx < 0 || cIdx >= candidates.size()) return;

        Component replacement = candidates.get(cIdx);
        if (old.getType() == ComponentType.EXTRA) {
            bolid.getExtras().remove(old);
            bolid.addExtra(replacement);
        } else {
            bolid.installComponent(replacement);
        }
        playerTeam.removeComponent(replacement);
        playerTeam.addComponent(old);
        System.out.printf("Заменено: «%s» → «%s». Старый компонент отправлен в инвентарь.%n",
            old.getName(), replacement.getName());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Охота на оборотней
    // ─────────────────────────────────────────────────────────────────────────

    private static final long VAN_HELSING_COST = 100_000L;
    private static final long BUFFY_COST        =  75_000L;

    private void werewolfHunt() {
        System.out.println(Ansi.bold("\n———————— ВЫЧИСЛЕНИЕ ОБОРОТНЕЙ ————————"));
        System.out.printf("Бюджет: %,d руб.%n", playerTeam.getBudget());
        System.out.println("1. Нанять Ван Хельсинга (" + String.format("%,d", VAN_HELSING_COST) + " руб.) — уничтожить оборотня");
        System.out.println("2. Нанять Баффи (" + String.format("%,d", BUFFY_COST) + " руб.) — вылечить оборотня");
        System.out.println("0. Назад");

        int choice = ConsoleInput.readInt("Ваш выбор: ");
        switch (choice) {
            case 1 -> hireVanHelsing();
            case 2 -> hireBuffy();
            case 0 -> {}
            default -> System.out.println("Неверный выбор.");
        }
    }

    private void hireVanHelsing() {
        if (!playerTeam.canAfford(VAN_HELSING_COST)) {
            System.out.printf("Недостаточно средств. Нужно %,d руб., есть %,d руб.%n",
                VAN_HELSING_COST, playerTeam.getBudget());
            return;
        }
        Pilot target = selectPilotForWerewolfCheck("Ван Хельсинг");
        if (target == null) return;

        playerTeam.spend(VAN_HELSING_COST);
        if (target.isWerewolf()) {
            playerTeam.removePilot(target);
            System.out.printf("Ван Хельсинг уничтожил оборотня %s! Пилот удалён из команды.%n", target.getName());
        } else {
            System.out.printf("Пилот %s не является оборотнем. Ван Хельсинг ушёл ни с чем.%n", target.getName());
        }
    }

    private void hireBuffy() {
        if (!playerTeam.canAfford(BUFFY_COST)) {
            System.out.printf("Недостаточно средств. Нужно %,d руб., есть %,d руб.%n",
                BUFFY_COST, playerTeam.getBudget());
            return;
        }
        Pilot target = selectPilotForWerewolfCheck("Баффи");
        if (target == null) return;

        playerTeam.spend(BUFFY_COST);
        if (target.isWerewolf()) {
            target.setWerewolf(false);
            System.out.printf("Баффи вылечила оборотня %s! Пилот снова человек.%n", target.getName());
        } else {
            System.out.printf("Пилот %s не является оборотнем. Баффи ушла ни с чем.%n", target.getName());
        }
    }

    private Pilot selectPilotForWerewolfCheck(String hunter) {
        List<Pilot> pilots = playerTeam.getPilots();
        if (pilots.isEmpty()) {
            System.out.println("В команде нет пилотов.");
            return null;
        }
        System.out.printf("%nВыберите пилота для проверки (%s):%n", hunter);
        for (int i = 0; i < pilots.size(); i++) {
            System.out.printf("  %d. %s%n", i + 1, pilots.get(i).getName());
        }
        System.out.println("  0. Отмена");
        int idx = ConsoleInput.readInt("Ваш выбор: ") - 1;
        if (idx < 0 || idx >= pilots.size()) return null;
        return pilots.get(idx);
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
        int    dnfs        = 0;
        long   totalPrize  = 0;
        int    bestPos     = Integer.MAX_VALUE;
        int    posSum      = 0;
        int    finished    = 0;

        for (Race r : raceHistory) {
            totalPrize += r.getPrizeAwarded();
            if (r.isPlayerDNF()) {
                dnfs++;
            } else {
                int pos = r.getPlayerPosition();
                posSum += pos;
                finished++;
                if (pos == 1) wins++;
                if (pos <= 3) podiums++;
                if (pos < bestPos) bestPos = pos;
            }
        }

        System.out.printf("Гонок сыграно:       %d%n", total);
        System.out.printf("Побед (1-е место):   %d%n", wins);
        System.out.printf("Подиумов (топ-3):    %d%n", podiums);
        System.out.printf("DNF (инциденты):     %d%n", dnfs);
        if (finished > 0) {
            System.out.printf("Лучшая позиция:      %d%n",   bestPos);
            System.out.printf("Средняя позиция:     %.1f%n", (double) posSum / finished);
        }
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
