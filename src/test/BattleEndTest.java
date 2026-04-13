import domain.SurvivalParticipant;
import domain.SurvivalRaceState;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// победа: не выбыл и 1-е место; поражение: выбыл или не первый
public class BattleEndTest {

    private static SurvivalParticipant player() {
        return new SurvivalParticipant("Вы", true, 300, 0, 0);
    }

    private static SurvivalParticipant bot(String name) {
        return new SurvivalParticipant(name, false, 300, 0, 0);
    }

    // победа — игрок пришёл первым

    @Test
    void playerAtPosition1_isVictory() {
        SurvivalParticipant player = player();
        SurvivalRaceState state = new SurvivalRaceState(List.of(player, bot("Бот")), 5);

        assertTrue(state.isPlayerVictory());
        assertFalse(state.isPlayerDefeat());
    }

    @Test
    void playerAloneAfterAllBotsEliminated_isVictory() {
        SurvivalParticipant player = player();
        SurvivalParticipant bot1 = bot("Бот-1");
        SurvivalParticipant bot2 = bot("Бот-2");
        SurvivalRaceState state = new SurvivalRaceState(List.of(player, bot1, bot2), 5);

        bot1.eliminate();
        bot2.eliminate();

        assertTrue(state.isPlayerVictory());
        assertFalse(state.isPlayerDefeat());
    }

    @Test
    void playerAt1stAfterLeaderEliminated_isVictory() {
        SurvivalParticipant leader = bot("Лидер");
        SurvivalParticipant player = player();
        SurvivalParticipant third = bot("3-й");
        // [Лидер(1), Игрок(2), 3-й]
        SurvivalRaceState state = new SurvivalRaceState(List.of(leader, player, third), 5);

        leader.eliminate(); // игрок поднялся на 1-е

        assertEquals(1, state.getActivePosition(player));
        assertTrue(state.isPlayerVictory());
    }

    @Test
    void playerAloneFromStart_isVictory() {
        SurvivalRaceState state = new SurvivalRaceState(List.of(player()), 5);
        assertTrue(state.isPlayerVictory());
    }

    // поражение — игрок выбыл

    @Test
    void playerEliminated_isDefeat() {
        SurvivalParticipant player = player();
        SurvivalRaceState state = new SurvivalRaceState(List.of(bot("Бот"), player), 5);

        player.eliminate();

        assertTrue(state.isPlayerDefeat());
        assertFalse(state.isPlayerVictory());
    }

    @Test
    void playerEliminated_whenAt1st_isStillDefeat() {
        SurvivalParticipant player = player();
        // был на 1-м месте, но получил атаку в спину
        SurvivalRaceState state = new SurvivalRaceState(List.of(player, bot("Бот")), 5);

        player.eliminate();

        assertTrue(state.isPlayerDefeat(), "выбывший проигрывает даже с 1-го места");
        assertFalse(state.isPlayerVictory());
    }

    @Test
    void playerEliminated_lastInRace_isDefeat() {
        SurvivalParticipant player = player();
        SurvivalRaceState state = new SurvivalRaceState(List.of(bot("Победитель"), player), 5);

        player.eliminate();

        assertTrue(state.isPlayerDefeat());
    }

    // поражение — не первый по окончании

    @Test
    void playerAtPosition2_isDefeat() {
        SurvivalRaceState state = new SurvivalRaceState(List.of(bot("Лидер"), player()), 5);
        assertTrue(state.isPlayerDefeat());
        assertFalse(state.isPlayerVictory());
    }

    @Test
    void playerAtPosition3_isDefeat() {
        SurvivalParticipant player = player();
        SurvivalRaceState state = new SurvivalRaceState(
            List.of(bot("1-й"), bot("2-й"), player), 5);

        assertEquals(3, state.getActivePosition(player));
        assertTrue(state.isPlayerDefeat());
    }

    @Test
    void playerAtLastPosition_isDefeat() {
        SurvivalParticipant player = player();
        SurvivalRaceState state = new SurvivalRaceState(
            List.of(bot("1-й"), bot("2-й"), bot("3-й"), bot("4-й"), player), 5);

        assertEquals(5, state.getActivePosition(player));
        assertTrue(state.isPlayerDefeat());
    }

    // isVictory и isDefeat всегда противоположны

    @Test
    void victoryAndDefeat_mutuallyExclusive_whenPlayerFirst() {
        SurvivalRaceState state = new SurvivalRaceState(List.of(player(), bot("Бот")), 5);
        assertNotEquals(state.isPlayerVictory(), state.isPlayerDefeat());
    }

    @Test
    void victoryAndDefeat_mutuallyExclusive_whenPlayerSecond() {
        SurvivalRaceState state = new SurvivalRaceState(List.of(bot("Лидер"), player()), 5);
        assertNotEquals(state.isPlayerVictory(), state.isPlayerDefeat());
    }

    @Test
    void victoryAndDefeat_mutuallyExclusive_whenPlayerEliminated() {
        SurvivalParticipant player = player();
        SurvivalRaceState state = new SurvivalRaceState(List.of(bot("Бот"), player), 5);
        player.eliminate();
        assertNotEquals(state.isPlayerVictory(), state.isPlayerDefeat());
    }

    // согласованность с isRaceOver

    @Test
    void raceIsOver_whenPlayerEliminated() {
        SurvivalParticipant player = player();
        SurvivalRaceState state = new SurvivalRaceState(List.of(bot("Бот"), player), 5);

        player.eliminate();

        assertTrue(state.isRaceOver());
        assertTrue(state.isPlayerDefeat());
    }

    @Test
    void raceIsOver_whenOnlyPlayerRemains_andHeWins() {
        SurvivalParticipant opponent = bot("Бот");
        SurvivalParticipant player = player();
        SurvivalRaceState state = new SurvivalRaceState(List.of(player, opponent), 5);

        opponent.eliminate();

        assertTrue(state.isRaceOver());
        assertTrue(state.isPlayerVictory());
    }

    @Test
    void raceNotOver_whenMultipleActiveAndStepsRemain() {
        SurvivalRaceState state = new SurvivalRaceState(List.of(bot("Бот"), player()), 5);
        assertFalse(state.isRaceOver());
    }

    @Test
    void raceOver_stepsExhausted_playerNotFirst_isDefeat() {
        SurvivalParticipant leader = bot("Лидер");
        SurvivalParticipant player = player();
        SurvivalRaceState state = new SurvivalRaceState(List.of(leader, player), 3);

        state.advanceStep();
        state.advanceStep();
        state.advanceStep();

        assertTrue(state.isRaceOver());
        assertTrue(state.isPlayerDefeat(), "на 2-м месте после финиша — поражение");
    }

    @Test
    void raceOver_stepsExhausted_playerFirst_isVictory() {
        SurvivalParticipant player = player();
        SurvivalRaceState state = new SurvivalRaceState(List.of(player, bot("Бот")), 2);

        state.advanceStep();
        state.advanceStep();

        assertTrue(state.isRaceOver());
        assertTrue(state.isPlayerVictory(), "на 1-м месте после финиша — победа");
    }
}
