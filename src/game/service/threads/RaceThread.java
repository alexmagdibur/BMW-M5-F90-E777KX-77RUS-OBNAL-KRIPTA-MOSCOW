package service.threads;

import data.TacticCatalog;
import domain.Bolid;
import domain.Component;
import domain.ComponentType;
import domain.Engineer;
import domain.Pilot;
import domain.Race;
import domain.RaceResult;
import domain.RaceTactic;
import domain.Team;
import domain.Track;
import domain.Weather;
import service.WearService;
import util.RandomUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class RaceThread implements Runnable {

    private static final long PRIZE_1ST = 2_000_000;
    private static final long PRIZE_2ND = 1_000_000;
    private static final long PRIZE_3RD = 500_000;

    private static final double WEREWOLF_CHANCE = 0.4;

    private static final String[] BOT_NAMES = {
        "Чуваки на АвтоВАЗе", "M5 Asphalt 8", "Распил Владивосток",
        "JDM Racing Club", "Клуб дядек на Крузаках", "Можно, а зачем?"
    };

    private final Team team;
    private final Bolid bolid;
    private final Pilot pilot;
    private final Engineer engineer;
    private final Track track;
    private final int plannedPitStops;
    private final Weather initialWeather;

    // результат гонки — доступен после run()
    private Race race;

    public RaceThread(Team team, Bolid bolid, Pilot pilot, Engineer engineer,
                      Track track, int plannedPitStops, Weather initialWeather) {
        this.team = team;
        this.bolid = bolid;
        this.pilot = pilot;
        this.engineer = engineer;
        this.track = track;
        this.plannedPitStops = plannedPitStops;
        this.initialWeather = initialWeather;
    }

    public Race getRace() { return race; }

    @Override
    public void run() {
        RaceState state = new RaceState(initialWeather);
        state.raceStartTime = System.currentTimeMillis();
        state.raceRunning.set(true);
        state.log("🏁 СТАРТ! Трасса: " + track.getName() + " | Погода: " + initialWeather);

        // проверка оборотня до старта потоков
        String werewolfDnf = checkWerewolf(state);

        // создаём BolideThread для игрока
        BolideThread playerThread = new BolideThread(
            state, bolid, pilot, engineer, track, plannedPitStops, team.getName(), true
        );
        if (werewolfDnf != null) {
            playerThread.setDnf();
            state.results.add(new BolideResult(team.getName(), 0, true, true));
            state.log("🐺 " + werewolfDnf);
        }

        // создаём BolideThread для ботов
        List<BolideThread> botThreads = createBots(state);

        List<BolideThread> allParticipants = new ArrayList<>();
        allParticipants.add(playerThread);
        allParticipants.addAll(botThreads);

        // запускаем погоду и инциденты
        Thread weatherThread = new Thread(new WeatherThread(state, 5000));
        Thread incidentThread = new Thread(new IncidentThread(state, allParticipants, 3000));
        weatherThread.setDaemon(true);
        incidentThread.setDaemon(true);
        weatherThread.start();
        incidentThread.start();

        // запускаем болиды
        List<Thread> bolideThreads = new ArrayList<>();
        for (BolideThread bt : allParticipants) {
            Thread t = new Thread(bt);
            bolideThreads.add(t);
            t.start();
        }

        // ждём завершения всех болидов
        for (Thread t : bolideThreads) {
            try { t.join(); } catch (InterruptedException ignored) {}
        }

        state.raceRunning.set(false);

        // прерываем вспомогательные потоки (они спят) и ждём
        weatherThread.interrupt();
        incidentThread.interrupt();
        try { weatherThread.join(1000); } catch (InterruptedException ignored) {}
        try { incidentThread.join(1000); } catch (InterruptedException ignored) {}

        // применяем износ к болиду игрока
        WearService.applyWear(bolid, track);

        race = buildRace(state, werewolfDnf);
    }

    // возвращает строку причины dnf если кто-то стал оборотнем, иначе null
    private String checkWerewolf(RaceState state) {
        if (initialWeather != Weather.SOLAR_ECLIPSE) return null;
        if (Math.random() < WEREWOLF_CHANCE) {
            return "Пилот " + pilot.getName() + " стал оборотнем — DNF!";
        }
        if (Math.random() < WEREWOLF_CHANCE) {
            return "Инженер " + engineer.getName() + " стал оборотнем — DNF!";
        }
        return null;
    }

    private List<BolideThread> createBots(RaceState state) {
        List<RaceTactic> tactics = TacticCatalog.getAvailableTactics();
        List<String> usedNames = new ArrayList<>();
        List<BolideThread> bots = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            String name = pickName(usedNames);
            Pilot botPilot = new Pilot(name, 0, RandomUtil.nextInt(40, 90));
            botPilot.setTactic(tactics.get(RandomUtil.nextInt(0, tactics.size() - 1)));
            Engineer botEngineer = new Engineer(name + "_eng", 0, RandomUtil.nextInt(40, 80));
            Bolid botBolid = createBotBolid(name);
            int pits = RandomUtil.nextInt(0, 2);
            bots.add(new BolideThread(state, botBolid, botPilot, botEngineer, track, pits, name, false));
        }
        return bots;
    }

    // болид бота с 6 компонентами — суммарный перфоманс попадает в диапазон RaceCalculator [300, 540]
    private static Bolid createBotBolid(String ownerName) {
        Bolid b = new Bolid(ownerName + "_bolid");
        b.installComponent(new Component("Двигатель",    ComponentType.ENGINE,       0, RandomUtil.nextInt(50, 90)));
        b.installComponent(new Component("КПП",          ComponentType.TRANSMISSION, 0, RandomUtil.nextInt(50, 90)));
        b.installComponent(new Component("Подвеска",     ComponentType.SUSPENSION,   0, RandomUtil.nextInt(50, 90)));
        b.installComponent(new Component("Шасси",        ComponentType.CHASSIS,      0, RandomUtil.nextInt(50, 90)));
        b.installComponent(new Component("Аэропакет",    ComponentType.AERO_PACKAGE, 0, RandomUtil.nextInt(50, 90)));
        b.installComponent(new Component("Шины",         ComponentType.TIRES,        0, RandomUtil.nextInt(50, 90)));
        return b;
    }

    private static String pickName(List<String> used) {
        String name;
        do {
            name = BOT_NAMES[RandomUtil.nextInt(0, BOT_NAMES.length - 1)];
        } while (used.contains(name));
        used.add(name);
        return name;
    }

    private Race buildRace(RaceState state, String werewolfDnf) {
        List<BolideResult> sorted = new ArrayList<>(state.results);
        // финишировавшие сортируются по времени, DNF идут в конец
        sorted.sort(Comparator
            .comparingInt((BolideResult r) -> r.isDnf() ? 1 : 0)
            .thenComparingDouble(BolideResult::getTime));

        List<RaceResult> raceResults = new ArrayList<>();
        int playerPosition = sorted.size();
        long prize = 0;
        String dnfReason = null;

        for (int i = 0; i < sorted.size(); i++) {
            BolideResult br = sorted.get(i);
            RaceResult rr = br.toRaceResult();
            rr.setPosition(i + 1);
            raceResults.add(rr);

            if (br.isPlayer()) {
                playerPosition = i + 1;
                if (br.isDnf()) {
                    dnfReason = werewolfDnf != null ? werewolfDnf : "инцидент во время гонки";
                }
            }
        }

        // начисляем приз игроку если не DNF
        if (dnfReason == null) {
            prize = switch (playerPosition) {
                case 1 -> PRIZE_1ST;
                case 2 -> PRIZE_2ND;
                case 3 -> PRIZE_3RD;
                default -> 0L;
            };
            if (prize > 0) team.earn(prize);
        }

        state.log("═══════════════════ ФИНИШ ═══════════════════");
        for (RaceResult rr : raceResults) {
            state.log(rr.toString());
        }
        if (dnfReason != null) {
            state.log("Ваш результат: DNF — " + dnfReason);
        } else {
            state.log("Ваше место: " + playerPosition
                + (prize > 0 ? "  |  +" + String.format("%,d", prize) + " руб." : ""));
        }

        return new Race(track, raceResults, playerPosition, prize, state.currentWeather, dnfReason);
    }
}
