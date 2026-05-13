package service.threads;

import data.CommentatorPhrases;
import util.RandomUtil;

import java.util.List;

public class CommentatorThread implements Runnable {

    private final RaceState state;
    private final List<BolideThread> participants;
    private final int commentIntervalMs;

    public CommentatorThread(RaceState state, List<BolideThread> participants, int commentIntervalMs) {
        this.state = state;
        this.participants = participants;
        this.commentIntervalMs = commentIntervalMs;
    }

    @Override
    public void run() {
        while (state.raceRunning.get()) {
            try {
                Thread.sleep(commentIntervalMs);
            } catch (InterruptedException e) {
                break;
            }
            if (!state.raceRunning.get()) break;

            // рандомно выбираем тип комментария: 0 — погода, 1 — позиции, 2 — спонсор
            int type = RandomUtil.nextInt(0, 2);
            switch (type) {
                case 0 -> state.log("🎙 " + CommentatorPhrases.getWeatherPhrase(state.currentWeather));
                case 1 -> commentPositions();
                case 2 -> state.log("📢 " + CommentatorPhrases.getSponsorPhrase());
            }
        }
    }

    private void commentPositions() {
        // читаем снапшот CopyOnWriteArrayList — безопасно без synchronized
        List<BolideResult> results = List.copyOf(state.results);

        if (!results.isEmpty()) {
            // среди финишировавших берём первого как лидера, последнего как замыкающего
            String leader = results.get(0).getParticipantName();
            String last   = results.get(results.size() - 1).getParticipantName();
            state.log("🎙 " + CommentatorPhrases.getPositionPhrase(leader, last));
        } else {
            // никто ещё не финишировал — комментируем по активным участникам
            String first = participants.isEmpty() ? "?" : participants.get(0).getParticipantName();
            state.log("🎙 " + first + " всё ещё на трассе, итоги скоро!");
        }
    }
}
