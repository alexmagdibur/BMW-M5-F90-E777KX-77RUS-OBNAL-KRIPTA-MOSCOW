package service.threads;

import domain.Bolid;
import domain.Engineer;
import domain.Pilot;
import domain.PitLane;
import domain.PitStop;
import domain.Track;
import domain.TrackSection;
import domain.Weather;
import service.RaceCalculator;

import java.util.List;

public class BolideThread implements Runnable {

    // 1 симуляционная секунда = 50 мс реального времени.
    // Используется и для сна на секцию, и для конвертации длительности питстопа
    // в симуляционные секунды — чтобы totalTime отражал реальный порядок финиша.
    static final double MS_PER_SIM_SECOND = 50.0;

    private final RaceState state;
    private final Bolid bolid;
    private final Pilot pilot;
    private final Engineer engineer;
    private final Track track;
    private final int plannedPitStops;
    private final String participantName;
    private final boolean isPlayer;

    // volatile — пишется из IncidentThread, читается в своём run()
    private volatile boolean dnf = false;

    public BolideThread(RaceState state, Bolid bolid, Pilot pilot, Engineer engineer,
                        Track track, int plannedPitStops, String participantName, boolean isPlayer) {
        this.state = state;
        this.bolid = bolid;
        this.pilot = pilot;
        this.engineer = engineer;
        this.track = track;
        this.plannedPitStops = plannedPitStops;
        this.participantName = participantName;
        this.isPlayer = isPlayer;
    }

    public void setDnf() { this.dnf = true; }
    public boolean isDnf() { return dnf; }
    public Bolid getBolid() { return bolid; }
    public String getPilotName() { return pilot.getName(); }
    public String getParticipantName() { return participantName; }
    public boolean isPlayer() { return isPlayer; }

    @Override
    public void run() {
        List<TrackSection> sections = track.getSections();
        int sectionsCount = sections.size();
        int sectionsPerPitStop = (plannedPitStops > 0)
            ? sectionsCount / plannedPitStops
            : Integer.MAX_VALUE;
        int pitsDone = 0;
        double totalTime = 0.0;

        for (int i = 0; i < sectionsCount; i++) {
            if (dnf || !state.raceRunning.get()) break;

            // volatile чтение — берём актуальную погоду на момент прохождения секции
            Weather weather = state.currentWeather;
            TrackSection section = sections.get(i);

            // создаём однострочную трассу из одной секции — RaceCalculator не трогаем
            Track sectionTrack = new Track("_", List.of(section));
            double sectionTime = RaceCalculator.calculateTime(bolid, pilot, engineer, sectionTrack, weather);

            // применяем тактику пилота если задана
            if (pilot.getTactic() != null) {
                sectionTime *= pilot.getTactic().getModifier(weather);
            }

            totalTime += sectionTime;
            // обновляем прогресс — CommentatorThread использует для определения реального лидера
            state.sectionProgress.put(participantName, i + 1);

            try {
                Thread.sleep((long)(sectionTime * MS_PER_SIM_SECOND));
            } catch (InterruptedException e) {
                break;
            }

            // пит-стоп каждые sectionsPerPitStop секций;
            // tryPitStop() возвращает стоимость питстопа в симуляционных секундах —
            // добавляем к totalTime, чтобы порядок в таблице совпадал с порядком в логах
            if ((i + 1) % sectionsPerPitStop == 0 && pitsDone < plannedPitStops) {
                totalTime += tryPitStop();
                pitsDone++;
            }
        }

        if (!dnf) {
            state.results.add(new BolideResult(participantName, totalTime, false, isPlayer));
            state.log("🏁 " + participantName + " финишировал: " + String.format("%.3f с", totalTime));
        }
    }

    // Возвращает стоимость питстопа в симуляционных секундах:
    //   успешный въезд → DURATION_MS / MS_PER_SIM_SECOND (= 16 с при текущих константах)
    //   боксы заняты  → 0.0 (пит-стоп пропущен, время не тратится)
    // Благодаря этому totalTime = секции + питстопы, и сортировка результатов
    // всегда совпадает с реальным порядком финиша в логах.
    private double tryPitStop() {
        PitLane pit = track.getPitLane();
        if (pit == null) return 0.0;

        if (pit.tryEnter()) {
            state.log("🔧 " + participantName + " заехал в боксы");
            try {
                Thread.sleep(PitStop.DURATION_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            PitStop.applyBonus(bolid);
            pit.leave();
            state.log("✅ " + participantName + " выехал из боксов");
            return PitStop.DURATION_MS / MS_PER_SIM_SECOND;
        } else {
            state.log("⛔ " + participantName + " — боксы заняты, пит-стоп пропущен");
            return 0.0;
        }
    }
}
