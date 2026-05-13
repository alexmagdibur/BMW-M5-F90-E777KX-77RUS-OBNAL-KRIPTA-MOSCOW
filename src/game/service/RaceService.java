package service;

import domain.Bolid;
import domain.Component;
import domain.Engineer;
import domain.Pilot;
import domain.Race;
import domain.RaceResult;
import domain.Team;
import domain.Track;
import domain.Weather;
import service.threads.RaceThread;
import util.RandomUtil;

import java.util.List;

public class RaceService {

    // вынесено в static чтобы тесты могли переопределять через setWerewolfChance()
    private static double WEREWOLF_CHANCE = 0.4;
    // pre-race проверка: 100% гарантирует детерминированность в тестах
    private static final double INCIDENT_CHANCE = 1.0;

    private final SaveService saveService;
    private final String playerName;
    private final List<RaceResult> raceResults;

    // конструктор без автосохранения — используется в тестах
    public RaceService() {
        this(null, null, null);
    }

    // конструктор с автосохранением
    public RaceService(SaveService saveService, String playerName, List<RaceResult> raceResults) {
        this.saveService  = saveService;
        this.playerName   = playerName;
        this.raceResults  = raceResults;
    }

    // только для тестов: позволяет переопределить вероятность оборотня
    public static void setWerewolfChance(double value) {
        WEREWOLF_CHANCE = value;
    }

    // overload для обратной совместимости с существующими тестами
    public Race runRace(Team team, Bolid bolid, Pilot pilot, Engineer engineer,
                        Track track, Weather weather) {
        return runRace(team, bolid, pilot, engineer, track, weather, 1);
    }

    public Race runRace(Team team, Bolid bolid, Pilot pilot, Engineer engineer,
                        Track track, Weather weather, int plannedPitStops) {
        // проверка оборотней до старта гонки
        String presetDnf = null;
        if (weather == Weather.SOLAR_ECLIPSE) {
            pilot.setWerewolf(RandomUtil.nextDouble(0.0, 1.0) < WEREWOLF_CHANCE);
            engineer.setWerewolf(RandomUtil.nextDouble(0.0, 1.0) < WEREWOLF_CHANCE);
            if (pilot.isWerewolf()) {
                presetDnf = "Пилот стал оборотнем, у него лапки! DNF";
            } else if (engineer.isWerewolf()) {
                presetDnf = "Инженер стал оборотнем, у него лапки! DNF";
            }
        } else {
            pilot.setWerewolf(false);
            engineer.setWerewolf(false);
        }

        // pre-race проверка инцидентов (100% шанс на изношенном компоненте)
        if (presetDnf == null) {
            presetDnf = checkIncident(bolid);
        }

        RaceThread raceThread = new RaceThread(
            team, bolid, pilot, engineer, track, plannedPitStops, weather, presetDnf
        );

        // run() а не start() — гонка блокирует меню до полного завершения
        new Thread(raceThread).run();

        Race race = raceThread.getRace();

        // добавляем результат игрока в историю — autoSave вызывается снаружи
        if (raceResults != null && race != null) {
            race.getResults().stream()
                .filter(RaceResult::isPlayer)
                .findFirst()
                .ifPresent(raceResults::add);
        }

        return race;
    }

    private static String checkIncident(Bolid bolid) {
        for (Component c : bolid.getComponents().values()) {
            if (c.isWornOut() && RandomUtil.nextDouble(0.0, 1.0) < INCIDENT_CHANCE) {
                c.setWear(100);
                return "отказ компонента «" + c.getName() + "»";
            }
        }
        return null;
    }
}
