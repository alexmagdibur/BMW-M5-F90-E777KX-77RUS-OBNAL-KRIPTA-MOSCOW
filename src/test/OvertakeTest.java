import domain.SurvivalParticipant;
import domain.SurvivalRaceState;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.SurvivalRaceService;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты обгона в режиме выживания.
 *
 * Используется фиксированный seed (0), при котором
 * первый вызов nextDouble() ≈ 0.7309 < 0.75 = максимальный шанс обгона игрока,
 * что гарантирует успех обгона без зависимости от случайности.
 *
 * Покрываемые сценарии:
 *   — Игрок на 2-м месте обгоняет 1-е → позиции меняются
 *   — Игрок уже на 1-м месте → tryOvertake возвращает false, порядок не меняется
 */
public class OvertakeTest {

    // nextDouble() ≈ 0.7309 при seed=0 — меньше максимального шанса обгона 0.75
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

    // ── Вспомогательные фабрики ───────────────────────────────────────────────

    /** Игрок с перф.300: шанс обгона = min(0.3 + 300/600, 0.75) = 0.75. */
    private static SurvivalParticipant player() {
        return new SurvivalParticipant("Вы", true, 300, 0, 0);
    }

    private static SurvivalParticipant bot(String name) {
        return new SurvivalParticipant(name, false, 300, 0, 0);
    }

    // ════════════════════════════════════════════════════════════════════════════
    // Сценарий 1: игрок на 2-м месте обгоняет 1-е — позиции поменялись
    // ════════════════════════════════════════════════════════════════════════════

    @Test
    void playerAt2nd_overtakes1st_positionsSwapped() {
        SurvivalParticipant leader = bot("Лидер");
        SurvivalParticipant player = player();
        // Порядок: [Лидер(1), Игрок(2)]
        SurvivalRaceState state = new SurvivalRaceState(List.of(leader, player), 10);

        assertEquals(2, state.getActivePosition(player), "Перед обгоном игрок на 2-м месте");
        assertEquals(1, state.getActivePosition(leader), "Перед обгоном бот на 1-м месте");

        boolean success = service.tryOvertake(state, player);

        assertTrue(success, "Обгон должен был удасться при seed=" + SEED_SUCCESS);
        assertEquals(1, state.getActivePosition(player), "После обгона игрок должен быть 1-м");
        assertEquals(2, state.getActivePosition(leader), "После обгона бот должен стать 2-м");
    }

    @Test
    void playerAt2nd_afterOvertake_totalCountUnchanged() {
        SurvivalParticipant leader = bot("Лидер");
        SurvivalParticipant player = player();
        SurvivalRaceState state = new SurvivalRaceState(List.of(leader, player), 10);

        service.tryOvertake(state, player);

        assertEquals(2, state.getActiveParticipants().size(),
            "Количество активных участников не должно изменяться при обгоне");
    }

    @Test
    void playerAt2nd_afterOvertake_orderListContainsBothParticipants() {
        SurvivalParticipant leader = bot("Лидер");
        SurvivalParticipant player = player();
        SurvivalRaceState state = new SurvivalRaceState(List.of(leader, player), 10);

        service.tryOvertake(state, player);

        List<SurvivalParticipant> active = state.getActiveParticipants();
        assertSame(player, active.get(0), "Первый в активном списке — игрок");
        assertSame(leader, active.get(1), "Второй в активном списке — бывший лидер");
    }

    @Test
    void playerAt2nd_in3ParticipantRace_overtakes1stOnly() {
        SurvivalParticipant first  = bot("1-й");
        SurvivalParticipant player = player();
        SurvivalParticipant third  = bot("3-й");
        // Порядок: [1-й, Игрок(2), 3-й]
        SurvivalRaceState state = new SurvivalRaceState(List.of(first, player, third), 10);

        assertEquals(2, state.getActivePosition(player));

        service.tryOvertake(state, player);

        // Игрок обгоняет только непосредственно впередистоящего (1-й)
        assertEquals(1, state.getActivePosition(player), "Игрок должен стать 1-м");
        assertEquals(2, state.getActivePosition(first),  "Бывший 1-й должен стать 2-м");
        assertEquals(3, state.getActivePosition(third),  "3-й не затронут обгоном");
    }

    // ════════════════════════════════════════════════════════════════════════════
    // Сценарий 2: игрок уже на 1-м месте — нечего обгонять, ничего не происходит
    // ════════════════════════════════════════════════════════════════════════════

    @Test
    void playerAt1st_tryOvertake_returnsFalse() {
        SurvivalParticipant player   = player();
        SurvivalParticipant follower = bot("Следующий");
        // Порядок: [Игрок(1), Следующий(2)]
        SurvivalRaceState state = new SurvivalRaceState(List.of(player, follower), 10);

        boolean result = service.tryOvertake(state, player);

        assertFalse(result, "Обгон с 1-й позиции невозможен — некого обгонять");
    }

    @Test
    void playerAt1st_tryOvertake_positionsUnchanged() {
        SurvivalParticipant player   = player();
        SurvivalParticipant follower = bot("Следующий");
        SurvivalRaceState state = new SurvivalRaceState(List.of(player, follower), 10);

        service.tryOvertake(state, player);

        assertEquals(1, state.getActivePosition(player),
            "Игрок должен остаться на 1-м месте");
        assertEquals(2, state.getActivePosition(follower),
            "Следующий должен остаться на 2-м месте");
    }

    @Test
    void playerAt1st_aloneInRace_tryOvertake_returnsFalse() {
        SurvivalParticipant player = player();
        SurvivalRaceState state = new SurvivalRaceState(List.of(player), 10);

        boolean result = service.tryOvertake(state, player);

        assertFalse(result, "В гонке один участник — обгон невозможен");
    }

    @Test
    void playerAt1st_tryOvertake_orderListUnchanged() {
        SurvivalParticipant player   = player();
        SurvivalParticipant follower = bot("Следующий");
        SurvivalRaceState state = new SurvivalRaceState(List.of(player, follower), 10);

        service.tryOvertake(state, player);

        List<SurvivalParticipant> active = state.getActiveParticipants();
        assertSame(player,   active.get(0), "Игрок первым в списке");
        assertSame(follower, active.get(1), "Бот вторым в списке");
    }
}
