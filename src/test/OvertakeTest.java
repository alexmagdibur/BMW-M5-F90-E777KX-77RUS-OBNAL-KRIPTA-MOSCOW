import domain.SurvivalParticipant;
import domain.SurvivalRaceState;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.SurvivalRaceService;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class OvertakeTest {

    // seed=0: nextDouble() = 0.7309 < 0.75 (макс. шанс игрока) - обгон гарантирован
    private static final long SEED_SUCCESS = 0L;

    private final SurvivalRaceService service = new SurvivalRaceService();

    @BeforeEach
    void setDeterministicRandom() {
        SurvivalRaceService.setRandom(new Random(SEED_SUCCESS));
    }

    @AfterEach
    void restoreRandom() {
        SurvivalRaceService.setRandom(new Random());
    }

    // перф.300: шанс обгона = min(0.3 + 300/600, 0.75) = 0.75
    private static SurvivalParticipant player() {
        return new SurvivalParticipant("Вы", true, 300, 0, 0);
    }

    private static SurvivalParticipant bot(String name) {
        return new SurvivalParticipant(name, false, 300, 0, 0);
    }

    // игрок на 2-м месте обгоняет 1-е - позиции поменялись

    @Test
    void playerAt2nd_overtakes1st_positionsSwapped() {
        SurvivalParticipant leader = bot("Лидер");
        SurvivalParticipant player = player();
        SurvivalRaceState state = new SurvivalRaceState(List.of(leader, player), 10);

        assertEquals(2, state.getActivePosition(player));
        assertEquals(1, state.getActivePosition(leader));

        boolean success = service.tryOvertake(state, player);

        assertTrue(success, "обгон должен удасться при seed=" + SEED_SUCCESS);
        assertEquals(1, state.getActivePosition(player));
        assertEquals(2, state.getActivePosition(leader));
    }

    @Test
    void playerAt2nd_afterOvertake_totalCountUnchanged() {
        SurvivalRaceState state = new SurvivalRaceState(List.of(bot("Лидер"), player()), 10);
        service.tryOvertake(state, state.getPlayer());
        assertEquals(2, state.getActiveParticipants().size());
    }

    @Test
    void playerAt2nd_afterOvertake_orderListContainsBothParticipants() {
        SurvivalParticipant leader = bot("Лидер");
        SurvivalParticipant player = player();
        SurvivalRaceState state = new SurvivalRaceState(List.of(leader, player), 10);

        service.tryOvertake(state, player);

        List<SurvivalParticipant> active = state.getActiveParticipants();
        assertSame(player, active.get(0));
        assertSame(leader, active.get(1));
    }

    @Test
    void playerAt2nd_in3ParticipantRace_overtakes1stOnly() {
        SurvivalParticipant first  = bot("1-й");
        SurvivalParticipant player = player();
        SurvivalParticipant third  = bot("3-й");
        // [1-й, Игрок(2), 3-й]
        SurvivalRaceState state = new SurvivalRaceState(List.of(first, player, third), 10);

        service.tryOvertake(state, player);

        assertEquals(1, state.getActivePosition(player));
        assertEquals(2, state.getActivePosition(first));
        assertEquals(3, state.getActivePosition(third), "3-й не затронут");
    }

    // игрок уже на 1-м месте - нечего обгонять, ничего не происходит

    @Test
    void playerAt1st_tryOvertake_returnsFalse() {
        SurvivalParticipant player = player();
        SurvivalRaceState state = new SurvivalRaceState(List.of(player, bot("Следующий")), 10);
        assertFalse(service.tryOvertake(state, player));
    }

    @Test
    void playerAt1st_tryOvertake_positionsUnchanged() {
        SurvivalParticipant player   = player();
        SurvivalParticipant follower = bot("Следующий");
        SurvivalRaceState state = new SurvivalRaceState(List.of(player, follower), 10);

        service.tryOvertake(state, player);

        assertEquals(1, state.getActivePosition(player));
        assertEquals(2, state.getActivePosition(follower));
    }

    @Test
    void playerAt1st_aloneInRace_tryOvertake_returnsFalse() {
        SurvivalParticipant player = player();
        SurvivalRaceState state = new SurvivalRaceState(List.of(player), 10);
        assertFalse(service.tryOvertake(state, player));
    }

    @Test
    void playerAt1st_tryOvertake_orderListUnchanged() {
        SurvivalParticipant player   = player();
        SurvivalParticipant follower = bot("Следующий");
        SurvivalRaceState state = new SurvivalRaceState(List.of(player, follower), 10);

        service.tryOvertake(state, player);

        List<SurvivalParticipant> active = state.getActiveParticipants();
        assertSame(player,   active.get(0));
        assertSame(follower, active.get(1));
    }
}
