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

/**
 * Тесты взаимного исключения действий за ход в режиме выживания.
 *
 * Правило: за один ход игрок выполняет ровно одно действие — обгон ИЛИ атаку.
 * Выбрал обгон → атака в этом ходу не происходит (цели не выбывают).
 * Выбрал атаку → обгон не происходит (позиции из-за атаки не меняются).
 *
 * Фиксированный seed=0: nextDouble() ≈ 0.7309 — гарантирует попадание при ур.3 (порог 0.85)
 * и обгон при перф.≥270 (шанс 0.75).
 */
public class BattleChoiceTest {

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

    // ── Фабрики участников ────────────────────────────────────────────────────

    /** Игрок с ближним и дальним оружием ур.3, перф.300 — гарантирован обгон и попадание. */
    private static SurvivalParticipant armedPlayer() {
        return new SurvivalParticipant("Вы", true, 300, 3, 3);
    }

    private static SurvivalParticipant bot(String name) {
        return new SurvivalParticipant(name, false, 300, 0, 0);
    }

    // ════════════════════════════════════════════════════════════════════════════
    // 1. Выбрал OVERTAKE — атака в этом ходу не происходит
    // ════════════════════════════════════════════════════════════════════════════

    @Test
    void chooseOvertake_playerMovesForward_noTargetEliminated() {
        SurvivalParticipant leader   = bot("Лидер");
        SurvivalParticipant player   = armedPlayer();
        SurvivalParticipant follower = bot("Следующий");
        // Порядок: [Лидер(1), Игрок(2), Следующий(3)]
        SurvivalRaceState state = new SurvivalRaceState(
            List.of(leader, player, follower), 10);

        service.applyPlayerChoice(state, player, PlayerChoice.OVERTAKE, null);

        // Позиция изменилась — обгон сработал
        assertEquals(1, state.getActivePosition(player), "После обгона игрок на 1-м месте");

        // Атака не происходила — никто не выбыл
        assertFalse(leader.isEliminated(),   "Лидер не должен выбыть — атаки не было");
        assertFalse(follower.isEliminated(), "Следующий не должен выбыть — атаки не было");
        assertEquals(3, state.getActiveParticipants().size(),
            "Все участники остаются активными");
    }

    @Test
    void chooseOvertake_onlyPositionEffectOccurs_attackEffectAbsent() {
        SurvivalParticipant leader = bot("Лидер");
        SurvivalParticipant player = armedPlayer();
        SurvivalRaceState state = new SurvivalRaceState(List.of(leader, player), 10);

        int positionBefore = state.getActivePosition(player);
        service.applyPlayerChoice(state, player, PlayerChoice.OVERTAKE, null);
        int positionAfter = state.getActivePosition(player);

        // Единственный эффект — смена позиции
        assertTrue(positionAfter < positionBefore, "Позиция должна улучшиться");
        assertFalse(leader.isEliminated(),
            "Обгон не является атакой — цель не выбывает");
    }

    @Test
    void chooseOvertake_withTargetArgument_targetStillNotEliminated() {
        // Даже если передать цель вместе с OVERTAKE — атака не выполняется
        SurvivalParticipant leader = bot("Лидер");
        SurvivalParticipant player = armedPlayer();
        SurvivalRaceState state = new SurvivalRaceState(List.of(leader, player), 10);

        // Передаём leader как «цель» — но OVERTAKE её игнорирует
        service.applyPlayerChoice(state, player, PlayerChoice.OVERTAKE, leader);

        assertFalse(leader.isEliminated(),
            "OVERTAKE не должен атаковать цель, даже если она передана");
    }

    // ════════════════════════════════════════════════════════════════════════════
    // 2. Выбрал MELEE_ATTACK — обгон в этом ходу не происходит
    // ════════════════════════════════════════════════════════════════════════════

    @Test
    void chooseMeleeAttack_targetEliminated_positionUnchanged() {
        SurvivalParticipant leader = bot("Лидер");
        SurvivalParticipant player = armedPlayer();
        // Порядок: [Лидер(1), Игрок(2)] — Лидер сосед, допустимая цель ближнего
        SurvivalRaceState state = new SurvivalRaceState(List.of(leader, player), 10);

        service.applyPlayerChoice(state, player, PlayerChoice.MELEE_ATTACK, leader);

        // Атака сработала — цель выбыла
        assertTrue(leader.isEliminated(), "Цель должна выбыть после ближней атаки");

        // Обгона не было — позиция изменилась только из-за выбывания лидера, а не из-за обгона
        // (игрок стал 1-м, потому что соперник выбыл, а не потому что было два действия)
        assertEquals(1, state.getActiveParticipants().size(),
            "В активных остался только игрок — лидер выбыл");
        assertSame(player, state.getActiveParticipants().get(0));
    }

    @Test
    void chooseMeleeAttack_in3ParticipantRace_onlyTargetEliminated_otherPositionsIntact() {
        SurvivalParticipant leader   = bot("Лидер");
        SurvivalParticipant player   = armedPlayer();
        SurvivalParticipant follower = bot("Следующий");
        // Порядок: [Лидер(1), Игрок(2), Следующий(3)]
        SurvivalRaceState state = new SurvivalRaceState(
            List.of(leader, player, follower), 10);

        service.applyPlayerChoice(state, player, PlayerChoice.MELEE_ATTACK, leader);

        // Цель выбыла
        assertTrue(leader.isEliminated(), "Лидер выбыл после атаки");

        // Обгона не было: до атаки игрок был 2-м; лидер выбыл и игрок стал 1-м,
        // но это эффект выбывания, а не дополнительного обгона
        assertFalse(follower.isEliminated(),
            "Третий участник не должен пострадать");
        assertEquals(2, state.getActiveParticipants().size(),
            "Активных двое: игрок и следующий");
    }

    // ════════════════════════════════════════════════════════════════════════════
    // 3. Выбрал RANGED_ATTACK — обгон в этом ходу не происходит
    // ════════════════════════════════════════════════════════════════════════════

    @Test
    void chooseRangedAttack_farTargetEliminated_positionEffectIsOnlyFromElimination() {
        SurvivalParticipant far    = bot("Далёкий");
        SurvivalParticipant mid    = bot("Средний");
        SurvivalParticipant player = armedPlayer();
        // Порядок: [Далёкий(1), Средний(2), Игрок(3)]
        SurvivalRaceState state = new SurvivalRaceState(
            List.of(far, mid, player), 10);

        service.applyPlayerChoice(state, player, PlayerChoice.RANGED_ATTACK, far);

        // Дальняя цель выбыла
        assertTrue(far.isEliminated(), "Далёкая цель должна выбыть от дальней атаки");

        // Игрок не обгонял — Средний впереди, не затронут
        assertFalse(mid.isEliminated(), "Средний не атакован и не должен выбыть");
        assertEquals(2, state.getActiveParticipants().size());
    }

    @Test
    void chooseRangedAttack_doesNotMovePlayerPosition_beyondEliminationEffect() {
        SurvivalParticipant p1 = bot("1-й");
        SurvivalParticipant p2 = bot("2-й");
        SurvivalParticipant p3 = bot("3-й");
        SurvivalParticipant player = armedPlayer(); // 4-я позиция
        SurvivalRaceState state = new SurvivalRaceState(
            List.of(p1, p2, p3, player), 10);

        // Атакуем p1 (далёкого лидера) дальним оружием
        service.applyPlayerChoice(state, player, PlayerChoice.RANGED_ATTACK, p1);

        assertTrue(p1.isEliminated(), "Лидер выбыл");
        // p2 и p3 не затронуты
        assertFalse(p2.isEliminated());
        assertFalse(p3.isEliminated());
        assertEquals(3, state.getActiveParticipants().size(),
            "Три участника остаются активными");
    }

    // ════════════════════════════════════════════════════════════════════════════
    // 4. PASS — ни обгона, ни атаки
    // ════════════════════════════════════════════════════════════════════════════

    @Test
    void choosePass_nothingHappens() {
        SurvivalParticipant leader = bot("Лидер");
        SurvivalParticipant player = armedPlayer();
        SurvivalRaceState state = new SurvivalRaceState(List.of(leader, player), 10);

        boolean result = service.applyPlayerChoice(state, player, PlayerChoice.PASS, null);

        assertFalse(result, "Пропуск хода не имеет результата");
        assertFalse(leader.isEliminated(), "При пропуске никто не выбывает");
        assertEquals(2, state.getActivePosition(player), "Позиция не изменилась");
    }

    // ════════════════════════════════════════════════════════════════════════════
    // 5. Одно действие = ровно один эффект (не оба одновременно)
    // ════════════════════════════════════════════════════════════════════════════

    @Test
    void singleCallToApplyPlayerChoice_producesExactlyOneEffect() {
        SurvivalParticipant leader   = bot("Лидер");
        SurvivalParticipant player   = armedPlayer();
        SurvivalParticipant follower = bot("Следующий");
        SurvivalRaceState state = new SurvivalRaceState(
            List.of(leader, player, follower), 10);

        // Выбираем обгон — только один вызов applyPlayerChoice
        service.applyPlayerChoice(state, player, PlayerChoice.OVERTAKE, null);

        // Эффект обгона есть
        assertEquals(1, state.getActivePosition(player), "Обгон произошёл");
        // Эффекта атаки нет
        assertFalse(leader.isEliminated(), "Атаки не было — лидер активен");
        assertFalse(follower.isEliminated(), "Атаки не было — следующий активен");
    }

    @Test
    void singleCallToApplyPlayerChoice_meleeHit_noSimultaneousOvertake() {
        SurvivalParticipant leader   = bot("Лидер");
        SurvivalParticipant player   = armedPlayer();
        SurvivalParticipant follower = bot("Следующий");
        SurvivalRaceState state = new SurvivalRaceState(
            List.of(leader, player, follower), 10);

        // Запоминаем позицию до
        int posBefore = state.getActivePosition(player);

        // Выбираем ближнюю атаку — один вызов applyPlayerChoice
        service.applyPlayerChoice(state, player, PlayerChoice.MELEE_ATTACK, leader);

        // Атака произошла
        assertTrue(leader.isEliminated(), "Атака ближним — лидер выбыл");

        // Обгона НЕ было: позиция игрока до вызова была 2; после выбывания лидера
        // он стал 1-м только потому что лидер выбыл — за ход выполнено ровно 1 действие
        assertEquals(posBefore - 1, state.getActivePosition(player),
            "Позиция сдвинулась ровно на 1 из-за выбывания соперника, а не из-за обгона");
        assertFalse(follower.isEliminated(),
            "Второй противник не был атакован — атака была лишь одна");
    }
}
