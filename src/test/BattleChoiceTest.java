import domain.PlayerChoice;
import domain.SurvivalParticipant;
import domain.SurvivalRaceState;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.SurvivalRaceService;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

// обгон и атака взаимоисключающи — за ход выполняется ровно одно действие
public class BattleChoiceTest {

    // seed=0: nextDouble() ≈ 0.7309 → гарантирует обгон (< 0.75) и попадание ур.3 (< 0.85)
    private static final long SEED = 0L;

    private final SurvivalRaceService service = new SurvivalRaceService();

    @BeforeEach
    void setDeterministicRandom() {
        SurvivalRaceService.setRandom(new Random(SEED));
    }

    @AfterEach
    void restoreRandom() {
        SurvivalRaceService.setRandom(new Random());
    }

    // игрок с ближним и дальним ур.3, перф.300 — обгон и попадание гарантированы
    private static SurvivalParticipant armedPlayer() {
        return new SurvivalParticipant("Вы", true, 300, 3, 3);
    }

    private static SurvivalParticipant bot(String name) {
        return new SurvivalParticipant(name, false, 300, 0, 0);
    }

    // ─── OVERTAKE: атаки не происходит ───────────────────────────────────────

    @Test
    void chooseOvertake_playerMovesForward_noTargetEliminated() {
        SurvivalParticipant leader   = bot("Лидер");
        SurvivalParticipant player   = armedPlayer();
        SurvivalParticipant follower = bot("Следующий");
        // [Лидер(1), Игрок(2), Следующий(3)]
        SurvivalRaceState state = new SurvivalRaceState(List.of(leader, player, follower), 10);

        service.applyPlayerChoice(state, player, PlayerChoice.OVERTAKE, null);

        assertEquals(1, state.getActivePosition(player), "обгон сработал");
        assertFalse(leader.isEliminated(),   "атаки не было");
        assertFalse(follower.isEliminated(), "атаки не было");
        assertEquals(3, state.getActiveParticipants().size());
    }

    @Test
    void chooseOvertake_onlyPositionChanges_noneEliminated() {
        SurvivalParticipant leader = bot("Лидер");
        SurvivalParticipant player = armedPlayer();
        SurvivalRaceState state = new SurvivalRaceState(List.of(leader, player), 10);

        int before = state.getActivePosition(player);
        service.applyPlayerChoice(state, player, PlayerChoice.OVERTAKE, null);

        assertTrue(state.getActivePosition(player) < before, "позиция улучшилась");
        assertFalse(leader.isEliminated(), "обгон не атакует");
    }

    @Test
    void chooseOvertake_targetArgumentIgnored() {
        // даже если передать цель — OVERTAKE её игнорирует
        SurvivalParticipant leader = bot("Лидер");
        SurvivalParticipant player = armedPlayer();
        SurvivalRaceState state = new SurvivalRaceState(List.of(leader, player), 10);

        service.applyPlayerChoice(state, player, PlayerChoice.OVERTAKE, leader);

        assertFalse(leader.isEliminated());
    }

    // ─── MELEE_ATTACK: обгона не происходит ──────────────────────────────────

    @Test
    void chooseMeleeAttack_targetEliminated_onlyOneEffect() {
        SurvivalParticipant leader = bot("Лидер");
        SurvivalParticipant player = armedPlayer();
        // [Лидер(1), Игрок(2)] — лидер сосед
        SurvivalRaceState state = new SurvivalRaceState(List.of(leader, player), 10);

        service.applyPlayerChoice(state, player, PlayerChoice.MELEE_ATTACK, leader);

        assertTrue(leader.isEliminated(), "атака сработала");
        // игрок стал 1-м из-за выбывания лидера, а не из-за обгона
        assertEquals(1, state.getActiveParticipants().size());
        assertSame(player, state.getActiveParticipants().get(0));
    }

    @Test
    void chooseMeleeAttack_in3ParticipantRace_onlyTargetEliminated() {
        SurvivalParticipant leader   = bot("Лидер");
        SurvivalParticipant player   = armedPlayer();
        SurvivalParticipant follower = bot("Следующий");
        SurvivalRaceState state = new SurvivalRaceState(List.of(leader, player, follower), 10);

        service.applyPlayerChoice(state, player, PlayerChoice.MELEE_ATTACK, leader);

        assertTrue(leader.isEliminated());
        assertFalse(follower.isEliminated(), "третий не задет");
        assertEquals(2, state.getActiveParticipants().size());
    }

    // ─── RANGED_ATTACK: обгона не происходит ─────────────────────────────────

    @Test
    void chooseRangedAttack_farTargetEliminated_othersUntouched() {
        SurvivalParticipant far    = bot("Далёкий");
        SurvivalParticipant mid    = bot("Средний");
        SurvivalParticipant player = armedPlayer();
        // [Далёкий(1), Средний(2), Игрок(3)]
        SurvivalRaceState state = new SurvivalRaceState(List.of(far, mid, player), 10);

        service.applyPlayerChoice(state, player, PlayerChoice.RANGED_ATTACK, far);

        assertTrue(far.isEliminated());
        assertFalse(mid.isEliminated(), "средний не атакован");
        assertEquals(2, state.getActiveParticipants().size());
    }

    @Test
    void chooseRangedAttack_onlyTargetGone_othersStayActive() {
        SurvivalParticipant p1 = bot("1-й");
        SurvivalParticipant p2 = bot("2-й");
        SurvivalParticipant p3 = bot("3-й");
        SurvivalParticipant player = armedPlayer(); // 4-я позиция
        SurvivalRaceState state = new SurvivalRaceState(List.of(p1, p2, p3, player), 10);

        service.applyPlayerChoice(state, player, PlayerChoice.RANGED_ATTACK, p1);

        assertTrue(p1.isEliminated());
        assertFalse(p2.isEliminated());
        assertFalse(p3.isEliminated());
        assertEquals(3, state.getActiveParticipants().size());
    }

    // ─── PASS: ничего не происходит ──────────────────────────────────────────

    @Test
    void choosePass_nothingHappens() {
        SurvivalParticipant leader = bot("Лидер");
        SurvivalParticipant player = armedPlayer();
        SurvivalRaceState state = new SurvivalRaceState(List.of(leader, player), 10);

        boolean result = service.applyPlayerChoice(state, player, PlayerChoice.PASS, null);

        assertFalse(result);
        assertFalse(leader.isEliminated());
        assertEquals(2, state.getActivePosition(player));
    }

    // ─── одно действие = ровно один эффект ───────────────────────────────────

    @Test
    void singleChoice_overtake_noEliminationsOccur() {
        SurvivalParticipant leader   = bot("Лидер");
        SurvivalParticipant player   = armedPlayer();
        SurvivalParticipant follower = bot("Следующий");
        SurvivalRaceState state = new SurvivalRaceState(List.of(leader, player, follower), 10);

        service.applyPlayerChoice(state, player, PlayerChoice.OVERTAKE, null);

        assertEquals(1, state.getActivePosition(player), "обгон произошёл");
        assertFalse(leader.isEliminated(),   "никто не выбыл");
        assertFalse(follower.isEliminated(), "никто не выбыл");
    }

    @Test
    void singleChoice_meleeHit_noSimultaneousOvertake() {
        SurvivalParticipant leader   = bot("Лидер");
        SurvivalParticipant player   = armedPlayer();
        SurvivalParticipant follower = bot("Следующий");
        SurvivalRaceState state = new SurvivalRaceState(List.of(leader, player, follower), 10);

        int posBefore = state.getActivePosition(player); // 2

        service.applyPlayerChoice(state, player, PlayerChoice.MELEE_ATTACK, leader);

        assertTrue(leader.isEliminated(), "атака сработала");
        // позиция сдвинулась на 1 только из-за выбывания лидера, обгона не было
        assertEquals(posBefore - 1, state.getActivePosition(player));
        assertFalse(follower.isEliminated(), "второй противник не атакован");
    }
}
