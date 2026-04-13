import domain.SurvivalParticipant;
import domain.SurvivalRaceState;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.SurvivalRaceService;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class AttackTest {

    // seed=0: nextDouble() ≈ 0.7309 < 0.85 (шанс попадания ур.3) → гарантированное попадание
    private static final long SEED_HIT = 0L;

    private final SurvivalRaceService service = new SurvivalRaceService();

    @BeforeEach
    void setDeterministicRandom() {
        SurvivalRaceService.setRandom(new Random(SEED_HIT));
    }

    @AfterEach
    void restoreRandom() {
        SurvivalRaceService.setRandom(new Random());
    }

    private static SurvivalParticipant player(int meleeLevel, int rangedLevel) {
        return new SurvivalParticipant("Вы", true, 300, meleeLevel, rangedLevel);
    }

    private static SurvivalParticipant bot(String name) {
        return new SurvivalParticipant(name, false, 300, 0, 0);
    }

    // ближнее оружие по соседу → попадание, цель выбывает

    @Test
    void meleeAttack_onAdjacentAhead_targetEliminated() {
        SurvivalParticipant leader = bot("Лидер");
        SurvivalParticipant player = player(3, 0);
        // [Лидер(1), Игрок(2)] — лидер сосед спереди
        SurvivalRaceState state = new SurvivalRaceState(List.of(leader, player), 10);

        boolean hit = service.tryAttack(state, player, leader, 3);

        assertTrue(hit, "при seed=0 и ур.3 должно быть попадание");
        assertTrue(leader.isEliminated());
    }

    @Test
    void meleeAttack_onAdjacentBehind_targetEliminated() {
        SurvivalParticipant player = player(3, 0);
        SurvivalParticipant follower = bot("Следующий");
        SurvivalRaceState state = new SurvivalRaceState(List.of(player, follower), 10);

        assertTrue(service.tryAttack(state, player, follower, 3));
        assertTrue(follower.isEliminated());
    }

    @Test
    void meleeAttack_onAdjacentTarget_removedFromActiveParticipants() {
        SurvivalParticipant target = bot("Цель");
        SurvivalParticipant player = player(3, 0);
        SurvivalRaceState state = new SurvivalRaceState(List.of(target, player), 10);

        service.tryAttack(state, player, target, 3);

        assertFalse(state.getActiveParticipants().contains(target));
        assertEquals(1, state.getActiveParticipants().size());
    }

    @Test
    void meleeValidTargets_containOnlyAdjacentParticipants() {
        SurvivalParticipant first = bot("1-й");
        SurvivalParticipant second = bot("2-й");
        SurvivalParticipant player = player(3, 0);
        SurvivalParticipant fourth = bot("4-й");
        // [1-й, 2-й, Игрок(3), 4-й]
        SurvivalRaceState state = new SurvivalRaceState(List.of(first, second, player, fourth), 10);

        List<SurvivalParticipant> targets = service.getValidTargets(state, player, true);

        assertTrue(targets.contains(second), "сосед спереди");
        assertTrue(targets.contains(fourth), "сосед сзади");
        assertEquals(2, targets.size());
    }

    // ближнее оружие по дальнему участнику → отказ

    @Test
    void meleeValidTargets_doNotContainFarParticipant() {
        SurvivalParticipant far = bot("Далёкий"); // 2 позиции от игрока
        SurvivalParticipant mid = bot("Средний");
        SurvivalParticipant player = player(3, 0);
        // [Далёкий(1), Средний(2), Игрок(3)]
        SurvivalRaceState state = new SurvivalRaceState(List.of(far, mid, player), 10);

        List<SurvivalParticipant> targets = service.getValidTargets(state, player, true);

        assertFalse(targets.contains(far), "2+ позиций — недоступен");
        assertTrue(targets.contains(mid), "сосед доступен");
    }

    @Test
    void meleeValidTargets_excludesNonNeighbors_5participants() {
        SurvivalParticipant p1 = bot("Бот-1");
        SurvivalParticipant p2 = bot("Бот-2");
        SurvivalParticipant p3 = bot("Бот-3");
        SurvivalParticipant p4 = bot("Бот-4");
        SurvivalParticipant player = player(2, 0);
        // [Бот-1, Бот-2, Игрок, Бот-3, Бот-4]
        SurvivalRaceState state = new SurvivalRaceState(List.of(p1, p2, player, p3, p4), 10);

        List<SurvivalParticipant> targets = service.getValidTargets(state, player, true);

        assertFalse(targets.contains(p1), "слишком далеко");
        assertFalse(targets.contains(p4), "слишком далеко");
        assertTrue(targets.contains(p2));
        assertTrue(targets.contains(p3));
        assertEquals(2, targets.size());
    }

    // дальнее оружие по любому участнику → работает

    @Test
    void rangedValidTargets_containAllOtherParticipants() {
        SurvivalParticipant far = bot("Далёкий");
        SurvivalParticipant mid = bot("Средний");
        SurvivalParticipant player = player(0, 3);
        // [Далёкий(1), Средний(2), Игрок(3)]
        SurvivalRaceState state = new SurvivalRaceState(List.of(far, mid, player), 10);

        List<SurvivalParticipant> targets = service.getValidTargets(state, player, false);

        assertTrue(targets.contains(far));
        assertTrue(targets.contains(mid));
        assertEquals(2, targets.size());
    }

    @Test
    void rangedAttack_onFarTarget_hitsSuccessfully() {
        SurvivalParticipant far = bot("Далёкий");
        SurvivalParticipant mid = bot("Средний");
        SurvivalParticipant player = player(0, 3);
        SurvivalRaceState state = new SurvivalRaceState(List.of(far, mid, player), 10);

        assertTrue(service.tryAttack(state, player, far, 3));
        assertTrue(far.isEliminated());
    }

    @Test
    void rangedAttack_onLeader_hitsSuccessfully() {
        SurvivalParticipant leader = bot("Лидер");
        SurvivalParticipant second = bot("2-й");
        SurvivalParticipant third = bot("3-й");
        SurvivalParticipant player = player(0, 3); // 4-я позиция
        SurvivalRaceState state = new SurvivalRaceState(List.of(leader, second, third, player), 10);

        // лидер в 3 позициях — для ближнего недоступен, для дальнего нет
        assertFalse(service.getValidTargets(state, player, true).contains(leader),  "меле не достаёт");
        assertTrue(service.getValidTargets(state, player, false).contains(leader), "дальний достаёт");

        assertTrue(service.tryAttack(state, player, leader, 3));
    }

    // атака выбывшего → отказ

    @Test
    void attackEliminated_returnsFalse() {
        SurvivalParticipant eliminated = bot("Выбывший");
        eliminated.eliminate();
        SurvivalParticipant player = player(3, 0);
        SurvivalRaceState state = new SurvivalRaceState(List.of(player, eliminated), 10);

        assertFalse(service.tryAttack(state, player, eliminated, 3));
    }

    @Test
    void attackEliminated_doesNotChangeEliminatedState() {
        SurvivalParticipant eliminated = bot("Выбывший");
        eliminated.eliminate();
        SurvivalParticipant player = player(3, 0);
        SurvivalRaceState state = new SurvivalRaceState(List.of(player, eliminated), 10);

        service.tryAttack(state, player, eliminated, 3);

        assertTrue(eliminated.isEliminated(), "остаётся выбывшим");
    }

    @Test
    void attackEliminated_notPresentInActiveList() {
        SurvivalParticipant eliminated = bot("Выбывший");
        eliminated.eliminate();
        SurvivalParticipant player = player(3, 0);
        SurvivalRaceState state = new SurvivalRaceState(List.of(player, eliminated), 10);

        service.tryAttack(state, player, eliminated, 3);

        assertFalse(state.getActiveParticipants().contains(eliminated));
    }

    @Test
    void attackWithZeroWeaponLevel_returnsFalse() {
        SurvivalParticipant target = bot("Цель");
        SurvivalParticipant player = player(0, 0); // нет оружия
        SurvivalRaceState state = new SurvivalRaceState(List.of(target, player), 10);

        assertFalse(service.tryAttack(state, player, target, 0));
        assertFalse(target.isEliminated());
    }
}
