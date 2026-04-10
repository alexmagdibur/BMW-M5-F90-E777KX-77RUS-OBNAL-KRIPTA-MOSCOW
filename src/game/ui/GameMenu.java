package ui;

import data.TrackCatalog;
import data.WeaponCatalog;
import domain.Bolid;
import domain.Component;
import domain.ComponentType;
import domain.Engineer;
import domain.Pilot;
import domain.Race;
import domain.RaceResult;
import domain.Team;
import domain.Track;
import domain.Weapon;
import domain.WeaponType;
import domain.Weather;
import domain.PlayerChoice;
import domain.SurvivalParticipant;
import domain.SurvivalRaceState;
import service.AssemblyService;
import service.BotGenerator;
import service.HireService;
import service.RaceService;
import service.SurvivalRaceService;
import service.WearService;
import service.WerewolfService;

import service.SaveService;
import service.ShopService;
import util.Ansi;

import java.util.ArrayList;
import java.util.List;

public class GameMenu {

    private final Team playerTeam;
    private final String playerName;
    private final List<Track> customTracks;
    private boolean running;
    private final ShopService      shopService;
    private final HireService      hireService;
    private final AssemblyService  assemblyService;
    private final RaceService      raceService;
    private final WerewolfService  werewolfService;
    private final SaveService      saveService;
    private final List<Race>       raceHistory   = new ArrayList<>();
    /** Плоская история результатов игрока — используется для сохранения. */
    private final List<RaceResult> raceResults;
    private Weather                currentWeather;

    public GameMenu(Team playerTeam, String playerName, List<RaceResult> history, List<Track> customTracks) {
        this.playerTeam      = playerTeam;
        this.playerName      = playerName;
        this.customTracks    = new ArrayList<>(customTracks);
        this.raceResults     = new ArrayList<>(history);
        this.running         = true;
        this.saveService     = new SaveService();
        this.shopService     = new ShopService(playerTeam);
        this.hireService     = new HireService(playerTeam);
        this.assemblyService = new AssemblyService(playerTeam, saveService, playerName, raceResults);
        this.raceService     = new RaceService(saveService, playerName, raceResults);
        this.werewolfService = new WerewolfService(playerTeam);
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
        System.out.println("14. Сохранить игру");
        if (currentWeather == Weather.SOLAR_ECLIPSE) {
            System.out.println("15. Вычислить оборотней");
            System.out.println("16. Выход");
        } else {
            System.out.println("15. Выход");
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
            case 14 -> saveGame();
            case 15 -> {
                if (currentWeather == Weather.SOLAR_ECLIPSE) {
                    werewolfService.werewolfHunt();
                } else {
                    System.out.println("До встречи!");
                    running = false;
                }
            }
            case 16 -> {
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

        System.out.println(Ansi.bold("\nВыберите режим гонки:"));
        System.out.println("  1. Обычный");
        System.out.println("  2. Выживание");
        System.out.println("  0. Отмена");
        int mode = ConsoleInput.readInt("Ваш выбор: ");

        switch (mode) {
            case 1 -> startNormalRace();
            case 2 -> startSurvivalMode();
            default -> System.out.println("Отмена.");
        }
    }

    private void startNormalRace() {
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

        saveService.autoSave(playerTeam, raceResults, playerName);

        Weather nextWeather = Weather.random();
        if (nextWeather != currentWeather) {
            System.out.println("Погода изменилась: " + currentWeather + " → " + nextWeather);
        }
        currentWeather = nextWeather;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Режим выживания
    // ─────────────────────────────────────────────────────────────────────────

    private void startSurvivalMode() {
        System.out.println(Ansi.bold("\n———————— РЕЖИМ ВЫЖИВАНИЯ ————————"));

        Track track = selectTrack();
        if (track == null) return;

        Bolid bolid = selectBolid();
        if (bolid == null) return;

        survivalBuyWeapons();
        survivalInstallWeapons(bolid);

        System.out.println(Ansi.bold("\nОружие установлено. Боевая готовность!"));
        ConsoleInput.readLine("Нажмите Enter для старта...");

        SurvivalRaceService svc = new SurvivalRaceService();
        SurvivalRaceState state = svc.createRace(bolid, track.getTotalLength());
        runSurvivalRaceLoop(state, svc);
    }

    // ── Пошаговый цикл гонки выживания ───────────────────────────────────────

    private void runSurvivalRaceLoop(SurvivalRaceState state, SurvivalRaceService svc) {
        while (!state.isRaceOver()) {
            state.advanceStep();
            System.out.printf("%n%s (Шаг %d / %d — каждый шаг = 1 000 м)%n",
                Ansi.bold("═══ ВЫЖИВАНИЕ"), state.getCurrentStep(), state.getTotalSteps());

            printSurvivalStandings(state);

            SurvivalParticipant player = state.getPlayer();
            if (player != null && !player.isEliminated()) {
                survivalPlayerTurn(state, svc, player);
            }

            if (state.isRaceOver()) break;

            List<String> botEvents = svc.processBotActions(state);
            if (!botEvents.isEmpty()) {
                System.out.println(Ansi.bold("\nДействия соперников:"));
                botEvents.forEach(System.out::println);
            }
        }

        printSurvivalResult(state);
    }

    private void printSurvivalStandings(SurvivalRaceState state) {
        System.out.println(Ansi.bold("\nТаблица позиций:"));
        List<SurvivalParticipant> active = state.getActiveParticipants();
        for (int i = 0; i < active.size(); i++) {
            SurvivalParticipant p = active.get(i);
            String weapons = "";
            if (p.getMeleeWeaponLevel() > 0)   weapons += " [Б.ур." + p.getMeleeWeaponLevel() + "]";
            if (p.getRangedWeaponLevel() > 0)   weapons += " [Д.ур." + p.getRangedWeaponLevel() + "]";
            String marker = p.isPlayer() ? " ◄ ВЫ" : "";
            System.out.printf("  %d. %-20s | Перф: %3d%s%s%n",
                i + 1, p.getName(), p.getPerformanceScore(), weapons, marker);
        }

        // Выбывшие
        List<SurvivalParticipant> out = state.getOrder().stream()
            .filter(SurvivalParticipant::isEliminated).toList();
        if (!out.isEmpty()) {
            System.out.print("  Выбыли: ");
            System.out.println(out.stream().map(SurvivalParticipant::getName)
                .reduce((a, b) -> a + ", " + b).orElse(""));
        }
    }

    private void survivalPlayerTurn(SurvivalRaceState state, SurvivalRaceService svc,
                                     SurvivalParticipant player) {
        List<SurvivalParticipant> active = state.getActiveParticipants();
        int playerIdx = active.indexOf(player);

        System.out.println(Ansi.bold("\nВаше действие:"));
        System.out.printf("  1. Обогнать    (~%d%% успех)%n",
            svc.overtakeChancePct(player.getPerformanceScore()));

        boolean hasMelee  = player.getMeleeWeaponLevel()  > 0;
        boolean hasRanged = player.getRangedWeaponLevel()  > 0;
        if (hasMelee || hasRanged) {
            if (hasMelee)  System.out.printf("  2. Атаковать ближним боем  [Ур.%d, ~%d%% попадание]%n",
                player.getMeleeWeaponLevel(),
                svc.attackChancePct(player.getMeleeWeaponLevel()));
            if (hasRanged) System.out.printf("  3. Атаковать дальним боем  [Ур.%d, ~%d%% попадание]%n",
                player.getRangedWeaponLevel(),
                svc.attackChancePct(player.getRangedWeaponLevel()));
        }
        System.out.println("  0. Пропустить ход");

        int choice = ConsoleInput.readInt("Ваш выбор: ");

        switch (choice) {
            case 1 -> {
                if (playerIdx == 0) {
                    System.out.println("Вы уже на первом месте — некого обгонять.");
                } else {
                    boolean ok = svc.applyPlayerChoice(state, player, PlayerChoice.OVERTAKE, null);
                    System.out.println(ok
                        ? Ansi.bold("Обгон удался! Вы переместились вперёд.")
                        : "Обгон не удался.");
                }
            }
            case 2 -> {
                if (!hasMelee) { System.out.println("Нет оружия ближнего боя."); break; }
                survivalAttack(state, svc, player, PlayerChoice.MELEE_ATTACK);
            }
            case 3 -> {
                if (!hasRanged) { System.out.println("Нет оружия дальнего боя."); break; }
                survivalAttack(state, svc, player, PlayerChoice.RANGED_ATTACK);
            }
            default -> System.out.println("Пропускаете ход.");
        }
    }

    private void survivalAttack(SurvivalRaceState state, SurvivalRaceService svc,
                                 SurvivalParticipant player, PlayerChoice choice) {
        boolean melee = (choice == PlayerChoice.MELEE_ATTACK);
        List<SurvivalParticipant> targets = svc.getValidTargets(state, player, melee);

        if (targets.isEmpty()) {
            System.out.println("Нет доступных целей.");
            return;
        }

        System.out.println("Выберите цель:");
        for (int i = 0; i < targets.size(); i++) {
            SurvivalParticipant t = targets.get(i);
            System.out.printf("  %d. %s (позиция %d, перф: %d)%n",
                i + 1, t.getName(), state.getActivePosition(t), t.getPerformanceScore());
        }
        System.out.println("  0. Отмена");

        int idx = ConsoleInput.readInt("Ваш выбор: ") - 1;
        if (idx < 0 || idx >= targets.size()) {
            System.out.println("Отмена атаки.");
            return;
        }

        SurvivalParticipant target = targets.get(idx);
        boolean hit = svc.applyPlayerChoice(state, player, choice, target);

        if (hit) {
            System.out.println(Ansi.bold("Попадание! " + target.getName() + " выбывает из гонки!"));
        } else {
            System.out.println("Промах — " + target.getName() + " уклонился.");
        }
    }

    private void printSurvivalResult(SurvivalRaceState state) {
        System.out.println(Ansi.bold("\n═══ ГОНКА ЗАВЕРШЕНА ═══"));
        SurvivalParticipant player = state.getPlayer();

        if (player == null) return;

        if (state.isPlayerVictory()) {
            int survived = state.getActiveParticipants().size();
            System.out.printf("Вы финишировали на 1-м месте из %d оставшихся участников.%n",
                survived);
            System.out.println(Ansi.bold("ПОБЕДА!"));
        } else if (player.isEliminated()) {
            System.out.println(Ansi.bold("Вы выбыли из гонки. Лучше в следующий раз!"));
        } else {
            int pos = state.getActivePosition(player);
            int survived = state.getActiveParticipants().size();
            System.out.printf("Вы финишировали на %d-м месте из %d оставшихся участников.%n",
                pos, survived);
        }

        System.out.println("\nИтоговая таблица:");
        printSurvivalStandings(state);
    }

    /** Покупка оружия из каталога — только в режиме выживания. */
    private void survivalBuyWeapons() {
        System.out.println(Ansi.bold("\n— Купить оружие —"));
        System.out.printf("Бюджет: %,d руб.%n", playerTeam.getBudget());
        System.out.println("Ур.1 и Ур.3 несовместимы между собой.");

        List<Weapon> catalog = WeaponCatalog.getAll();
        while (true) {
            System.out.printf("%nБюджет: %,d руб.%n", playerTeam.getBudget());
            for (int i = 0; i < catalog.size(); i++) {
                Weapon w = catalog.get(i);
                System.out.printf(" %d. [Ур.%d][%s] %-27s | Урон: %2d | %,d руб.%n",
                    i + 1, w.getLevel(), w.getType(), w.getName(), w.getDamage(), w.getPrice());
            }
            System.out.println(" 0. Продолжить");

            int choice = ConsoleInput.readInt("Ваш выбор: ");
            if (choice == 0) break;
            if (choice < 1 || choice > catalog.size()) { System.out.println("Неверный выбор."); continue; }

            Weapon selected = catalog.get(choice - 1);
            if (!playerTeam.canAfford(selected.getPrice())) {
                System.out.printf("Недостаточно средств. Нужно %,d руб., есть %,d руб.%n",
                    selected.getPrice(), playerTeam.getBudget());
                continue;
            }
            playerTeam.spend(selected.getPrice());
            playerTeam.addWeapon(selected.copy());
            System.out.printf("Куплено: %s за %,d руб.%n", selected.getName(), selected.getPrice());
        }
    }

    /** Установка оружия в болид из инвентаря — только в режиме выживания. */
    private void survivalInstallWeapons(Bolid bolid) {
        System.out.println(Ansi.bold("\n— Установить оружие в болид —"));

        List<Weapon> installed = new ArrayList<>();

        for (WeaponType wt : WeaponType.values()) {
            List<Weapon> available = playerTeam.getWeaponInventory().stream()
                .filter(w -> w.getType() == wt)
                .toList();

            if (available.isEmpty()) {
                System.out.printf("Нет оружия типа «%s» в инвентаре.%n", wt.getDisplayName());
                continue;
            }

            System.out.printf("%n  %s:%n", Ansi.bold(wt.getDisplayName()));
            for (int i = 0; i < available.size(); i++) {
                Weapon w = available.get(i);
                System.out.printf("   %d. [Ур.%d] %-27s | Урон: %d%n",
                    i + 1, w.getLevel(), w.getName(), w.getDamage());
            }
            System.out.println("   0. Не устанавливать");

            int choice = ConsoleInput.readInt("Ваш выбор: ");
            if (choice < 1 || choice > available.size()) continue;

            Weapon picked = available.get(choice - 1);

            boolean compatible = true;
            for (Weapon already : installed) {
                if (!picked.isCompatibleWith(already)) {
                    System.out.printf("«%s» (ур.%d) несовместимо с «%s» (ур.%d). Пропущено.%n",
                        picked.getName(), picked.getLevel(),
                        already.getName(), already.getLevel());
                    compatible = false;
                    break;
                }
            }
            if (compatible) {
                bolid.installWeapon(picked);
                playerTeam.removeWeapon(picked);
                installed.add(picked);
                System.out.printf("Установлено: %s%n", picked.getName());
            }
        }
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
        List<Track> all = new ArrayList<>(TrackCatalog.getAll());
        all.addAll(customTracks);
        System.out.println(Ansi.bold("\nВыберите трассу:"));
        for (int i = 0; i < all.size(); i++) {
            Track t = all.get(i);
            System.out.printf("  %d. %-20s | %,d м | %d секций%n",
                i + 1, t.getName(), t.getTotalLength(), t.getSections().size());
        }
        int idx = ConsoleInput.readInt("Ваш выбор: ") - 1;
        if (idx < 0 || idx >= all.size()) {
            System.out.println("Неверный выбор трассы.");
            return null;
        }
        return all.get(idx);
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
    // Сохранение
    // ─────────────────────────────────────────────────────────────────────────

    private void saveGame() {
        saveService.saveGame(playerTeam, raceResults, playerName);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Статистика гонок
    // ─────────────────────────────────────────────────────────────────────────

    private void showRaceStats() {
        System.out.println(Ansi.bold("\n———————— СТАТИСТИКА ГОНОК ————————"));

        if (raceResults.isEmpty()) {
            System.out.println("Гонок ещё не было.");
            return;
        }

        // Позиции и результаты — из raceResults (включает историю из сохранения)
        int total    = raceResults.size();
        int wins     = 0;
        int podiums  = 0;
        int dnfs     = 0;
        int bestPos  = Integer.MAX_VALUE;
        int posSum   = 0;
        int finished = 0;

        for (RaceResult r : raceResults) {
            if (r.isIncident()) {
                dnfs++;
            } else {
                int pos = r.getPosition();
                posSum += pos;
                finished++;
                if (pos == 1) wins++;
                if (pos <= 3) podiums++;
                if (pos < bestPos) bestPos = pos;
            }
        }

        // Призовые — только из Race объектов текущей сессии (RaceResult не хранит приз)
        long sessionPrize = 0;
        for (Race r : raceHistory) {
            sessionPrize += r.getPrizeAwarded();
        }

        System.out.printf("Гонок сыграно:       %d%n", total);
        System.out.printf("Побед (1-е место):   %d%n", wins);
        System.out.printf("Подиумов (топ-3):    %d%n", podiums);
        System.out.printf("DNF (инциденты):     %d%n", dnfs);
        if (finished > 0) {
            System.out.printf("Лучшая позиция:      %d%n",   bestPos);
            System.out.printf("Средняя позиция:     %.1f%n", (double) posSum / finished);
        }
        if (sessionPrize > 0) {
            System.out.printf("Призовые за сессию:  %,d руб.%n", sessionPrize);
        }
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
