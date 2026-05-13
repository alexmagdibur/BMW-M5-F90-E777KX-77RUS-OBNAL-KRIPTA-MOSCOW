package service;

import domain.Bolid;
import domain.Engineer;
import domain.Pilot;
import domain.Race;
import domain.RaceResult;
import domain.Team;
import domain.Track;
import domain.Weather;
import service.threads.RaceThread;

import java.util.List;

public class RaceService {

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

    public Race runRace(Team team, Bolid bolid, Pilot pilot, Engineer engineer,
                        Track track, Weather weather) {
        RaceThread raceThread = new RaceThread(team, bolid, pilot, engineer, track, 1, weather);

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
}
