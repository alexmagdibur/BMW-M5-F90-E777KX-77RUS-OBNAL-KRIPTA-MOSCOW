import domain.SurvivalParticipant;
import domain.SurvivalRaceState;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты условий победы и поражения в режиме выживания.
 *
 * Победа:    игрок не выбыл И занимает 1-е место среди активных.
 * Поражение: игрок выбыл ИЛИ не занимает 1-е место по окончании гонки.
 */
public class BattleEndTest {

    // ── Фабрики ───────────────────────────────────────────────────────────────

    private static SurvivalParticipant player() {
        return new SurvivalParticipant("Вы", true, 300, 0, 0);
    }

    private static SurvivalParticipant bot(String name) {
        return new SurvivalParticipant(name, false, 300, 0, 0);
    }

    // ════════════════════════════════════════════════════════════════════════════
    // 1. ПОБЕДА — игрок пришёл первым
    // ════════════════════════════════════════════════════════════════════════════

    @Test
    void playerAtPosition1_isVictory() {
        SurvivalParticipant player = player();
        SurvivalParticipant bot    = bot("Бот");
        // Игрок на 1-м месте
        SurvivalRaceState state = new SurvivalRaceState(List.of(player, bot), 5);

        assertTrue(state.isPlayerVictory(), "Игрок на 1-м месте — победа");
        assertFalse(state.isPlayerDefeat(), "Не поражение");
    }

    @Test
    void playerAloneAfterAllBotsEliminated_isVictory() {
        SurvivalParticipant player = player();
        SurvivalParticipant bot1   = bot("Бот-1");
        SurvivalParticipant bot2   = bot("Бот-2");
        SurvivalRaceState state = new SurvivalRaceState(List.of(player, bot1, bot2), 5);

        bot1.eliminate();
        bot2.eliminate();

        assertTrue(state.isPlayerVictory(),
            "Все боты выбыли — игрок единственный оставшийся, победа");
        assertFalse(state.isPlayerDefeat());
    }

    @Test
    void playerAt1stAfterLeaderEliminated_isVictory() {
        SurvivalParticipant leader = bot("Лидер");
        SurvivalParticipant player = player();
        SurvivalParticipant third  = bot("3-й");
        // Изначально [Лидер(1), Игрок(2), 3-й(3)]
        SurvivalRaceState state = new SurvivalRaceState(
            List.of(leader, player, third), 5);

        leader.eliminate(); // лидер выбыл — игрок поднялся на 1-е

        assertEquals(1, state.getActivePosition(player), "Игрок теперь 1-й");
        assertTrue(state.isPlayerVictory(), "Победа после выбывания лидера");
        assertFalse(state.isPlayerDefeat());
    }

    @Test
    void playerAloneFromStart_isVictory() {
        SurvivalParticipant player = player();
        SurvivalRaceState state = new SurvivalRaceState(List.of(player), 5);

        assertTrue(state.isPlayerVictory(), "Игрок единственный участник — победа");
    }

    // ════════════════════════════════════════════════════════════════════════════
    // 2. ПОРАЖЕНИЕ — игрок выбыл
    // ════════════════════════════════════════════════════════════════════════════

    @Test
    void playerEliminated_isDefeat() {
        SurvivalParticipant player = player();
        SurvivalParticipant bot    = bot("Бот");
        SurvivalRaceState state = new SurvivalRaceState(List.of(bot, player), 5);

        player.eliminate();

        assertTrue(state.isPlayerDefeat(), "Игрок выбыл — поражение");
        assertFalse(state.isPlayerVictory(), "Не победа");
    }

    @Test
    void playerEliminated_whenAt1st_isStillDefeat() {
        SurvivalParticipant player = player();
        SurvivalParticipant bot    = bot("Бот");
        // Игрок был на 1-м месте, но выбыл (атака в спину)
        SurvivalRaceState state = new SurvivalRaceState(List.of(player, bot), 5);

        player.eliminate();

        assertTrue(state.isPlayerDefeat(), "Выбывший игрок проиграл, даже если был 1-м");
        assertFalse(state.isPlayerVictory());
    }

    @Test
    void playerEliminated_lastInRace_isDefeat() {
        SurvivalParticipant winner = bot("Победитель");
        SurvivalParticipant player = player();
        SurvivalRaceState state = new SurvivalRaceState(List.of(winner, player), 5);

        player.eliminate();

        assertTrue(state.isPlayerDefeat());
        assertFalse(state.isPlayerVictory());
    }

    // ════════════════════════════════════════════════════════════════════════════
    // 3. ПОРАЖЕНИЕ — игрок не первый по окончании
    // ════════════════════════════════════════════════════════════════════════════

    @Test
    void playerAtPosition2_isDefeat() {
        SurvivalParticipant leader = bot("Лидер");
        SurvivalParticipant player = player();
        SurvivalRaceState state = new SurvivalRaceState(List.of(leader, player), 5);

        assertTrue(state.isPlayerDefeat(), "Игрок на 2-м месте — поражение");
        assertFalse(state.isPlayerVictory());
    }

    @Test
    void playerAtPosition3_isDefeat() {
        SurvivalParticipant first  = bot("1-й");
        SurvivalParticipant second = bot("2-й");
        SurvivalParticipant player = player();
        SurvivalRaceState state = new SurvivalRaceState(
            List.of(first, second, player), 5);

        assertEquals(3, state.getActivePosition(player));
        assertTrue(state.isPlayerDefeat(), "Игрок на 3-м месте — поражение");
        assertFalse(state.isPlayerVictory());
    }

    @Test
    void playerAtLastPosition_isDefeat() {
        SurvivalParticipant p1 = bot("1-й");
        SurvivalParticipant p2 = bot("2-й");
        SurvivalParticipant p3 = bot("3-й");
        SurvivalParticipant p4 = bot("4-й");
        SurvivalParticipant player = player();
        SurvivalRaceState state = new SurvivalRaceState(
            List.of(p1, p2, p3, p4, player), 5);

        assertEquals(5, state.getActivePosition(player));
        assertTrue(state.isPlayerDefeat(), "Игрок на последнем месте — поражение");
    }

    // ════════════════════════════════════════════════════════════════════════════
    // 4. isVictory и isDefeat взаимоисключающи при любом состоянии
    // ════════════════════════════════════════════════════════════════════════════

    @Test
    void victoryAndDefeat_mutuallyExclusive_whenPlayerFirst() {
        SurvivalParticipant player = player();
        SurvivalRaceState state = new SurvivalRaceState(List.of(player, bot("Бот")), 5);

        assertNotEquals(state.isPlayerVictory(), state.isPlayerDefeat(),
            "Победа и поражение не могут быть одновременно истинны или ложны");
    }

    @Test
    void victoryAndDefeat_mutuallyExclusive_whenPlayerSecond() {
        SurvivalParticipant player = player();
        SurvivalRaceState state = new SurvivalRaceState(
            List.of(bot("Лидер"), player), 5);

        assertNotEquals(state.isPlayerVictory(), state.isPlayerDefeat());
    }

    @Test
    void victoryAndDefeat_mutuallyExclusive_whenPlayerEliminated() {
        SurvivalParticipant player = player();
        SurvivalRaceState state = new SurvivalRaceState(
            List.of(bot("Бот"), player), 5);
        player.eliminate();

        assertNotEquals(state.isPlayerVictory(), state.isPlayerDefeat());
    }

    // ════════════════════════════════════════════════════════════════════════════
    // 5. isRaceOver согласован с результатом
    // ════════════════════════════════════════════════════════════════════════════

    @Test
    void raceIsOver_whenPlayerEliminated() {
        SurvivalParticipant player = player();
        SurvivalRaceState state = new SurvivalRaceState(
            List.of(bot("Бот"), player), 5);

        player.eliminate();

        assertTrue(state.isRaceOver(),
            "Гонка завершается после выбывания игрока");
        assertTrue(state.isPlayerDefeat());
    }

    @Test
    void raceIsOver_whenOnlyPlayerRemains_andHeWins() {
        SurvivalParticipant bot    = bot("Бот");
        SurvivalParticipant player = player();
        SurvivalRaceState state = new SurvivalRaceState(List.of(player, bot), 5);

        bot.eliminate();

        assertTrue(state.isRaceOver(),
            "Гонка завершается, если остался один активный участник");
        assertTrue(state.isPlayerVictory(),
            "Единственный оставшийся игрок — победитель");
    }

    @Test
    void raceNotOver_whenMultipleActiveParticipants_andStepsRemain() {
        SurvivalParticipant player = player();
        SurvivalRaceState state = new SurvivalRaceState(
            List.of(bot("Бот"), player), 5);

        // Шаги не исчерпаны, никто не выбыл
        assertFalse(state.isRaceOver(), "Гонка ещё идёт");
    }

    @Test
    void raceOver_whenAllStepsExhausted_playerNotFirst_isDefeat() {
        SurvivalParticipant leader = bot("Лидер");
        SurvivalParticipant player = player();
        SurvivalRaceState state = new SurvivalRaceState(List.of(leader, player), 3);

        // Исчерпываем все шаги
        state.advanceStep();
        state.advanceStep();
        state.advanceStep();

        assertTrue(state.isRaceOver(), "Шаги исчерпаны — гонка окончена");
        assertTrue(state.isPlayerDefeat(),
            "Игрок на 2-м месте по истечении шагов — поражение");
    }

    @Test
    void raceOver_whenAllStepsExhausted_playerFirst_isVictory() {
        SurvivalParticipant player = player();
        SurvivalParticipant bot    = bot("Бот");
        SurvivalRaceState state = new SurvivalRaceState(List.of(player, bot), 2);

        state.advanceStep();
        state.advanceStep();

        assertTrue(state.isRaceOver());
        assertTrue(state.isPlayerVictory(),
            "Игрок на 1-м месте по истечении шагов — победа");
    }
}
