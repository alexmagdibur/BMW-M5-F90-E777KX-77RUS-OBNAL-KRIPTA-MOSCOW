package service.threads;

import data.CommentatorPhrases;
import util.RandomUtil;

import java.util.Set;
import java.util.stream.Collectors;

public class CommentatorThread implements Runnable {

    private final RaceState state;
    private final int commentIntervalMs;
    // последний выведенный комментарий — пропускаем повтор той же фразы
    private String lastComment = "";

    public CommentatorThread(RaceState state, int commentIntervalMs) {
        this.state = state;
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

            String comment = buildComment();
            // null — нечего говорить; совпадение — не повторяем одно и то же
            if (comment != null && !comment.equals(lastComment)) {
                state.log(comment);
                lastComment = comment;
            }
        }
    }

    private String buildComment() {
        int type = RandomUtil.nextInt(0, 2);
        return switch (type) {
            case 0 -> "🎙 " + CommentatorPhrases.getWeatherPhrase(state.currentWeather);
            case 1 -> buildPositionComment();
            default -> "📢 " + CommentatorPhrases.getSponsorPhrase();
        };
    }

    private String buildPositionComment() {
        // имена уже финишировавших / DNF — исключаем их из «активных»
        Set<String> finishedNames = state.results.stream()
            .map(BolideResult::getParticipantName)
            .collect(Collectors.toSet());

        // лидер и замыкающий среди тех, кто ещё едет (по числу пройденных секций)
        String leader  = state.getLeaderName(finishedNames);
        String laggard = state.getLaggardName(finishedNames);

        if (leader == null) return null; // все финишировали — не спамим

        if (leader.equals(laggard)) {
            return "🎙 " + leader + " продолжает борьбу на трассе!";
        }
        return "🎙 " + CommentatorPhrases.getPositionPhrase(leader, laggard);
    }
}
