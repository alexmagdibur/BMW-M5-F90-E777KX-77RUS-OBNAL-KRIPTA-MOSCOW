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
 * Тесты атаки в режиме выживания.
 *
 * Фиксированный seed=0: nextDouble() ≈ 0.7309 < 0.85 (шанс попадания ур.3)
 * → гарантированное попадание при использовании оружия 3-го уровня.
 *
 * Покрываемые сценарии:
 *   — Ближнее оружие по соседнему участнику → попадание, цель выбывает
 *   — Ближнее оружие по неближнему участнику → отказ (не в списке целей)
 *   — Дальнее оружие по любому участнику → работает, цель выбывает
 *   — Атака уже выбывшего → tryAttack возвращает false
 */
public class AttackTest {

    private static final long SEED_HIT = 0L; // 0.7309 < 0.85 → попадание ур.3

    private final SurvivalRaceService service = new SurvivalRaceService();

    @BeforeEach
    void setDeterministicRandom() {
        SurvivalRaceService.setRandom(new Random(SEED_HIT));
    }

    @AfterEach
    void restoreRandom() {
        SurvivalRaceService.setRandom(new Random());
    }

    // ── Фабрики участников ────────────────────────────────────────────────────

    private static SurvivalParticipant player(int meleeLevel, int rangedLevel) {
        return new SurvivalParticipant("Вы", true, 300, meleeLevel, rangedLevel);
    }

    private static SurvivalParticipant bot(String name) {
        return new SurvivalParticipant(name, false, 300, 0, 0);
    }

    // ════════════════════════════════════════════════════════════════════════════
    // 1. Ближнее оружие по соседнему участнику → попадание, цель выбывает
    // ════════════════════════════════════════════════════════════════════════════

    @Test
    void meleeAttack_onAdjacentAhead_targetEliminated() {
        SurvivalParticipant leader = bot("Лидер");
        SurvivalParticipant player = player(3, 0); // ближнее ур.3
        // Порядок: [Лидер(1), Игрок(2)] — Лидер — сосед спереди
        SurvivalRaceState state = new SurvivalRaceState(List.of(leader, player), 10);

        boolean hit = service.tryAttack(state, player, leader, player(3,0).getMeleeWeaponLevel());

        assertTrue(hit, "При seed=0 и ур.3 должно быть попадание");
        assertTrue(leader.isEliminated(), "Цель должна выбыть после попадания");
    }

    @Test
    void meleeAttack_onAdjacentBehind_targetEliminated() {
        SurvivalParticipant player   = player(3, 0);
        SurvivalParticipant follower = bot("Следующий");
        // Порядок: [Игрок(1), Следующий(2)] — Следующий — сосед сзади
        SurvivalRaceState state = new SurvivalRaceState(List.of(player, follower), 10);

        boolean hit = service.tryAttack(state, player, follower, 3);

        assertTrue(hit, "Атака соседа сзади тоже должна попасть");
        assertTrue(follower.isEliminated());
    }

    @Test
    void meleeAttack_onAdjacentTarget_removedFromActiveParticipants() {
        SurvivalParticipant target = bot("Цель");
        SurvivalParticipant player = player(3, 0);
        SurvivalRaceState state = new SurvivalRaceState(List.of(target, player), 10);

        service.tryAttack(state, player, target, 3);

        assertFalse(state.getActiveParticipants().contains(target),
            "Выбывший участник не должен быть в активном списке");
        assertEquals(1, state.getActiveParticipants().size());
    }

    @Test
    void meleeValidTargets_containOnlyAdjacentParticipants() {
        SurvivalParticipant first  = bot("1-й");
        SurvivalParticipant second = bot("2-й");
        SurvivalParticipant player = player(3, 0);
        SurvivalParticipant fourth = bot("4-й");
        // Порядок: [1-й, 2-й, Игрок(3), 4-й]
        SurvivalRaceState state = new SurvivalRaceState(
            List.of(first, second, player, fourth), 10);

        List<SurvivalParticipant> targets = service.getValidTargets(state, player, true);

        assertTrue(targets.contains(second), "Сосед спереди должен быть доступен");
        assertTrue(targets.contains(fourth), "Сосед сзади должен быть доступен");
        assertEquals(2, targets.size(), "Ближний бой — ровно два соседа");
    }

    // ════════════════════════════════════════════════════════════════════════════
    // 2. Ближнее оружие по дальнему участнику → отказ (не в списке целей)
    // ════════════════════════════════════════════════════════════════════════════

    @Test
    void meleeValidTargets_doNotContainFarParticipant() {
        SurvivalParticipant far    = bot("Далёкий"); // 2 позиции от игрока
        SurvivalParticipant mid    = bot("Средний");
        SurvivalParticipant player = player(3, 0);
        // Порядок: [Далёкий(1), Средний(2), Игрок(3)]
        SurvivalRaceState state = new SurvivalRaceState(List.of(far, mid, player), 10);

        List<SurvivalParticipant> targets = service.getValidTargets(state, player, true);

        assertFalse(targets.contains(far),
            "Дальний участник (2+ позиций) недоступен для ближнего боя");
        assertTrue(targets.contains(mid),
            "Непосредственный сосед должен быть в списке");
    }

    @Test
    void meleeValidTargets_farParticipant_listExcludesNonNeighbors_3participants() {
        SurvivalParticipant p1 = bot("Бот-1");
        SurvivalParticipant p2 = bot("Бот-2");
        SurvivalParticipant p3 = bot("Бот-3");
        SurvivalParticipant p4 = bot("Бот-4");
        SurvivalParticipant player = player(2, 0);
        // Порядок: [Бот-1, Бот-2, Игрок, Бот-3, Бот-4]
        SurvivalRaceState state = new SurvivalRaceState(
            List.of(p1, p2, player, p3, p4), 10);

        List<SurvivalParticipant> targets = service.getValidTargets(state, player, true);

        assertFalse(targets.contains(p1), "Бот-1 (2 позиции) недоступен для ближнего боя");
        assertFalse(targets.contains(p4), "Бот-4 (2 позиции) недоступен для ближнего боя");
        assertTrue(targets.contains(p2),  "Бот-2 (сосед) доступен");
        assertTrue(targets.contains(p3),  "Бот-3 (сосед) доступен");
        assertEquals(2, targets.size());
    }

    // ════════════════════════════════════════════════════════════════════════════
    // 3. Дальнее оружие по любому участнику → работает
    // ════════════════════════════════════════════════════════════════════════════

    @Test
    void rangedValidTargets_containAllOtherParticipants() {
        SurvivalParticipant far    = bot("Далёкий");
        SurvivalParticipant mid    = bot("Средний");
        SurvivalParticipant player = player(0, 3); // дальнее ур.3
        // Порядок: [Далёкий(1), Средний(2), Игрок(3)]
        SurvivalRaceState state = new SurvivalRaceState(List.of(far, mid, player), 10);

        List<SurvivalParticipant> targets = service.getValidTargets(state, player, false);

        assertTrue(targets.contains(far),
            "Дальнее оружие должно доставать до любого участника");
        assertTrue(targets.contains(mid),
            "Дальнее оружие должно доставать до соседа");
        assertEquals(2, targets.size(), "Список целей — все кроме атакующего");
    }

    @Test
    void rangedAttack_onFarTarget_hitsSuccessfully() {
        SurvivalParticipant far    = bot("Далёкий");
        SurvivalParticipant mid    = bot("Средний");
        SurvivalParticipant player = player(0, 3);
        SurvivalRaceState state = new SurvivalRaceState(List.of(far, mid, player), 10);

        boolean hit = service.tryAttack(state, player, far, 3);

        assertTrue(hit, "Дальняя атака по удалённой цели должна попасть при seed=0 и ур.3");
        assertTrue(far.isEliminated(), "Удалённая цель должна выбыть");
    }

    @Test
    void rangedAttack_onLeader_hitsSuccessfully() {
        SurvivalParticipant leader  = bot("Лидер");
        SurvivalParticipant second  = bot("2-й");
        SurvivalParticipant third   = bot("3-й");
        SurvivalParticipant player  = player(0, 3); // позиция 4-я
        SurvivalRaceState state = new SurvivalRaceState(
            List.of(leader, second, third, player), 10);

        // Лидер в 3 позициях от игрока — недоступен ближнему, доступен дальнему
        boolean inMelee  = service.getValidTargets(state, player, true).contains(leader);
        boolean inRanged = service.getValidTargets(state, player, false).contains(leader);

        assertFalse(inMelee,  "Лидер вне досягаемости ближнего оружия");
        assertTrue(inRanged, "Лидер доступен для дальнего оружия");

        boolean hit = service.tryAttack(state, player, leader, 3);
        assertTrue(hit, "Дальняя атака по лидеру должна попасть");
    }

    // ════════════════════════════════════════════════════════════════════════════
    // 4. Атака уже выбывшего → tryAttack возвращает false
    // ════════════════════════════════════════════════════════════════════════════

    @Test
    void attackEliminated_returnsFalse() {
        SurvivalParticipant eliminated = bot("Выбывший");
        eliminated.eliminate();
        SurvivalParticipant player = player(3, 0);
        SurvivalRaceState state = new SurvivalRaceState(List.of(player, eliminated), 10);

        boolean result = service.tryAttack(state, player, eliminated, 3);

        assertFalse(result, "Атака уже выбывшего участника должна вернуть false");
    }

    @Test
    void attackEliminated_doesNotChangeEliminatedState() {
        SurvivalParticipant eliminated = bot("Выбывший");
        eliminated.eliminate();
        SurvivalParticipant player = player(3, 0);
        SurvivalRaceState state = new SurvivalRaceState(List.of(player, eliminated), 10);

        service.tryAttack(state, player, eliminated, 3);

        assertTrue(eliminated.isEliminated(),
            "Участник должен оставаться выбывшим после повторной атаки");
    }

    @Test
    void attackEliminated_notPresentInActiveList() {
        SurvivalParticipant eliminated = bot("Выбывший");
        eliminated.eliminate();
        SurvivalParticipant player = player(3, 0);
        SurvivalRaceState state = new SurvivalRaceState(List.of(player, eliminated), 10);

        service.tryAttack(state, player, eliminated, 3);

        assertFalse(state.getActiveParticipants().contains(eliminated),
            "Выбывший не должен попасть в активный список");
    }

    @Test
    void attackWithZeroWeaponLevel_returnsFalse() {
        SurvivalParticipant target = bot("Цель");
        SurvivalParticipant player = player(0, 0); // нет оружия
        SurvivalRaceState state = new SurvivalRaceState(List.of(target, player), 10);

        boolean result = service.tryAttack(state, player, target, 0);

        assertFalse(result, "Атака без оружия (уровень 0) должна вернуть false");
        assertFalse(target.isEliminated(), "Цель без оружия не должна выбыть");
    }
}
