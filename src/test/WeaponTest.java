import domain.Bolid;
import domain.Weapon;
import domain.WeaponType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты системы оружия в режиме выживания.
 *
 * Покрываемые сценарии:
 *  — установка 1 ближнего + 1 дальнего → успех
 *  — попытка добавить второе оружие того же типа → слот занят, первое остаётся
 *  — установка оружий, несовместимых по уровню (1 + 3) → отказ isCompatibleWith
 *  — попытка установить оружие на болид, где уже есть несовместимое → отказ
 */
public class WeaponTest {

    // ── Фикстуры ──────────────────────────────────────────────────────────────

    // Уровень 1
    private static final Weapon MELEE_1  = new Weapon("Кастет ВАЗ",       WeaponType.MELEE,  50_000,  25, 1);
    private static final Weapon RANGED_1 = new Weapon("Рогатка-М",         WeaponType.RANGED, 40_000,  20, 1);
    // Уровень 2
    private static final Weapon MELEE_2  = new Weapon("Цепь BMW",          WeaponType.MELEE,  180_000, 55, 2);
    private static final Weapon RANGED_2 = new Weapon("Пистолет Макарова", WeaponType.RANGED, 150_000, 48, 2);
    // Уровень 3
    private static final Weapon MELEE_3  = new Weapon("Плазменный кулак",   WeaponType.MELEE,  600_000, 95, 3);
    private static final Weapon RANGED_3 = new Weapon("Рельсотрон Porsche", WeaponType.RANGED, 550_000, 90, 3);

    private Bolid bolid;

    @BeforeEach
    void setUp() {
        bolid = new Bolid("Тестовый болид");
    }

    // ════════════════════════════════════════════════════════════════════════════
    // 1. Установка 1 ближнее + 1 дальнее → успех
    // ════════════════════════════════════════════════════════════════════════════

    @Test
    void installMeleeAndRanged_bothSlotsFilledSuccessfully() {
        bolid.installWeapon(MELEE_1);
        bolid.installWeapon(RANGED_1);

        assertEquals(2, bolid.getWeapons().size(),
            "Должно быть установлено ровно 2 оружия");
        assertSame(MELEE_1, bolid.getWeapons().get(WeaponType.MELEE),
            "Ближнее оружие должно быть в слоте MELEE");
        assertSame(RANGED_1, bolid.getWeapons().get(WeaponType.RANGED),
            "Дальнее оружие должно быть в слоте RANGED");
    }

    @Test
    void freshBolidHasNoWeapons() {
        assertTrue(bolid.getWeapons().isEmpty(),
            "Новый болид не должен иметь оружия");
    }

    @Test
    void installMeleeOnly_onlyMeleeSlotOccupied() {
        bolid.installWeapon(MELEE_2);

        assertTrue(bolid.getWeapons().containsKey(WeaponType.MELEE));
        assertFalse(bolid.getWeapons().containsKey(WeaponType.RANGED));
    }

    @Test
    void installRangedOnly_onlyRangedSlotOccupied() {
        bolid.installWeapon(RANGED_2);

        assertTrue(bolid.getWeapons().containsKey(WeaponType.RANGED));
        assertFalse(bolid.getWeapons().containsKey(WeaponType.MELEE));
    }

    // ════════════════════════════════════════════════════════════════════════════
    // 2. Попытка добавить ещё одно ближнее или дальнее → слот занят, отказ
    //
    // Болид хранит Map<WeaponType, Weapon> — по одному слоту на тип.
    // Если слот занят, правильная логика установки отклоняет новое оружие.
    // Ниже реализуем ту же проверку, что GameMenu.survivalInstallWeapons().
    // ════════════════════════════════════════════════════════════════════════════

    @Test
    void slotIsOccupiedAfterFirstMeleeInstall() {
        bolid.installWeapon(MELEE_1);

        assertTrue(bolid.getWeapons().containsKey(WeaponType.MELEE),
            "После установки ближнего слот MELEE должен быть занят");
    }

    @Test
    void slotIsOccupiedAfterFirstRangedInstall() {
        bolid.installWeapon(RANGED_1);

        assertTrue(bolid.getWeapons().containsKey(WeaponType.RANGED),
            "После установки дальнего слот RANGED должен быть занят");
    }

    @Test
    void cannotAddSecondMeleeWhenSlotOccupied() {
        bolid.installWeapon(MELEE_1);

        // Имитируем проверку GameMenu: занят слот → не устанавливаем
        boolean slotOccupied = bolid.getWeapons().containsKey(MELEE_2.getType());
        assertTrue(slotOccupied, "Слот ближнего оружия уже занят");

        if (!slotOccupied) {
            bolid.installWeapon(MELEE_2); // этой строки не должно быть достигнуто
        }

        // Слот по-прежнему содержит первое оружие
        assertSame(MELEE_1, bolid.getWeapons().get(WeaponType.MELEE),
            "В слоте должно остаться первое ближнее оружие");
    }

    @Test
    void cannotAddSecondRangedWhenSlotOccupied() {
        bolid.installWeapon(RANGED_3);

        boolean slotOccupied = bolid.getWeapons().containsKey(RANGED_1.getType());
        assertTrue(slotOccupied, "Слот дальнего оружия уже занят");

        if (!slotOccupied) {
            bolid.installWeapon(RANGED_1);
        }

        assertSame(RANGED_3, bolid.getWeapons().get(WeaponType.RANGED),
            "В слоте должно остаться первое дальнее оружие");
    }

    @Test
    void totalWeaponCountNeverExceedsTwo() {
        bolid.installWeapon(MELEE_2);
        bolid.installWeapon(RANGED_2);

        // Попытка установить третье оружие (второй MELEE) — слот занят
        boolean meleeOccupied = bolid.getWeapons().containsKey(WeaponType.MELEE);
        if (!meleeOccupied) {
            bolid.installWeapon(MELEE_3);
        }

        assertEquals(2, bolid.getWeapons().size(),
            "Болид не должен содержать более 2 оружий одновременно");
    }

    // ════════════════════════════════════════════════════════════════════════════
    // 3. Совместимость оружий между собой (Weapon.isCompatibleWith)
    // ════════════════════════════════════════════════════════════════════════════

    // Совместимые пары ─────────────────────────────────────────────────────────

    @Test
    void level1Melee_level1Ranged_areCompatible() {
        assertTrue(MELEE_1.isCompatibleWith(RANGED_1),
            "Оружия одного уровня (1+1) совместимы");
    }

    @Test
    void level2Melee_level2Ranged_areCompatible() {
        assertTrue(MELEE_2.isCompatibleWith(RANGED_2),
            "Оружия одного уровня (2+2) совместимы");
    }

    @Test
    void level3Melee_level3Ranged_areCompatible() {
        assertTrue(MELEE_3.isCompatibleWith(RANGED_3),
            "Оружия одного уровня (3+3) совместимы");
    }

    @Test
    void level1Melee_level2Ranged_areCompatible() {
        assertTrue(MELEE_1.isCompatibleWith(RANGED_2),
            "Уровни 1 и 2 совместимы");
    }

    @Test
    void level2Ranged_level1Melee_areCompatibleSymmetric() {
        assertTrue(RANGED_2.isCompatibleWith(MELEE_1),
            "Совместимость симметрична: 2+1 = 1+2");
    }

    @Test
    void level2Melee_level3Ranged_areCompatible() {
        assertTrue(MELEE_2.isCompatibleWith(RANGED_3),
            "Уровни 2 и 3 совместимы");
    }

    @Test
    void level3Ranged_level2Melee_areCompatibleSymmetric() {
        assertTrue(RANGED_3.isCompatibleWith(MELEE_2),
            "Совместимость симметрична: 3+2 = 2+3");
    }

    // Несовместимые пары (уровень 1 + уровень 3) ──────────────────────────────

    @Test
    void level1Melee_level3Ranged_areIncompatible() {
        assertFalse(MELEE_1.isCompatibleWith(RANGED_3),
            "Бюджетное (1) + топовое (3) оружие несовместимы");
    }

    @Test
    void level3Ranged_level1Melee_areIncompatibleSymmetric() {
        assertFalse(RANGED_3.isCompatibleWith(MELEE_1),
            "Несовместимость симметрична: 3+1 = 1+3");
    }

    @Test
    void level3Melee_level1Ranged_areIncompatible() {
        assertFalse(MELEE_3.isCompatibleWith(RANGED_1),
            "Топовое (3) + бюджетное (1) оружие несовместимы");
    }

    @Test
    void level1Ranged_level3Melee_areIncompatibleSymmetric() {
        assertFalse(RANGED_1.isCompatibleWith(MELEE_3),
            "Несовместимость симметрична: 1+3 = 3+1");
    }

    // Оружие совместимо само с собой ──────────────────────────────────────────

    @Test
    void weaponIsCompatibleWithItself_level1() {
        assertTrue(MELEE_1.isCompatibleWith(MELEE_1));
    }

    @Test
    void weaponIsCompatibleWithItself_level3() {
        assertTrue(RANGED_3.isCompatibleWith(RANGED_3));
    }

    // ════════════════════════════════════════════════════════════════════════════
    // 4. Установка оружия, несовместимого с уже установленным на болиде → отказ
    //
    // Реализуем ту же логику, что в GameMenu.survivalInstallWeapons():
    //   проверяем isCompatibleWith для всех ранее установленных оружий,
    //   и только при совместимости вызываем bolid.installWeapon().
    // ════════════════════════════════════════════════════════════════════════════

    /**
     * Вспомогательный метод установки: возвращает false, если оружие
     * несовместимо с уже установленными (логика из GameMenu).
     */
    private boolean tryInstall(Bolid b, Weapon candidate, List<Weapon> alreadyInstalled) {
        for (Weapon existing : alreadyInstalled) {
            if (!candidate.isCompatibleWith(existing)) {
                return false; // отказ — несовместимо
            }
        }
        b.installWeapon(candidate);
        alreadyInstalled.add(candidate);
        return true;
    }

    @Test
    void installIncompatibleWeapon_rejectedWhenBolidHasLevel1() {
        List<Weapon> installed = new ArrayList<>();
        tryInstall(bolid, MELEE_1, installed); // ур. 1 ближнее — успех

        // Пытаемся установить дальнее ур. 3 — несовместимо с ближним ур. 1
        boolean result = tryInstall(bolid, RANGED_3, installed);

        assertFalse(result, "Оружие ур.3 должно быть отклонено при наличии ур.1 в болиде");
        assertFalse(bolid.getWeapons().containsKey(WeaponType.RANGED),
            "Дальнее оружие не должно попасть в болид");
    }

    @Test
    void installIncompatibleWeapon_rejectedWhenBolidHasLevel3() {
        List<Weapon> installed = new ArrayList<>();
        tryInstall(bolid, RANGED_3, installed); // ур. 3 дальнее — успех

        // Пытаемся установить ближнее ур. 1 — несовместимо с дальним ур. 3
        boolean result = tryInstall(bolid, MELEE_1, installed);

        assertFalse(result, "Оружие ур.1 должно быть отклонено при наличии ур.3 в болиде");
        assertFalse(bolid.getWeapons().containsKey(WeaponType.MELEE),
            "Ближнее оружие не должно попасть в болид");
    }

    @Test
    void installCompatibleWeapons_bothAccepted_level2Bridge() {
        List<Weapon> installed = new ArrayList<>();
        boolean r1 = tryInstall(bolid, MELEE_2, installed); // ур. 2
        boolean r2 = tryInstall(bolid, RANGED_2, installed); // ур. 2

        assertTrue(r1, "Первое оружие ур.2 должно быть принято");
        assertTrue(r2, "Второе оружие ур.2 должно быть принято");
        assertEquals(2, bolid.getWeapons().size(),
            "Оба оружия должны быть установлены на болид");
    }

    @Test
    void installCompatibleWeapons_level1AndLevel2_bothAccepted() {
        List<Weapon> installed = new ArrayList<>();
        boolean r1 = tryInstall(bolid, MELEE_1, installed);
        boolean r2 = tryInstall(bolid, RANGED_2, installed);

        assertTrue(r1);
        assertTrue(r2);
        assertEquals(2, bolid.getWeapons().size());
    }

    @Test
    void installCompatibleWeapons_level2AndLevel3_bothAccepted() {
        List<Weapon> installed = new ArrayList<>();
        boolean r1 = tryInstall(bolid, MELEE_3, installed);
        boolean r2 = tryInstall(bolid, RANGED_2, installed);

        assertTrue(r1);
        assertTrue(r2);
        assertEquals(2, bolid.getWeapons().size());
    }

    @Test
    void incompatiblePair_secondWeaponNotPresentOnBolid_afterRejection() {
        List<Weapon> installed = new ArrayList<>();
        tryInstall(bolid, MELEE_3, installed);
        tryInstall(bolid, RANGED_1, installed); // отклонено

        assertFalse(bolid.getWeapons().containsKey(WeaponType.RANGED),
            "Отклонённое оружие не должно появиться в болиде");
        assertEquals(1, bolid.getWeapons().size(),
            "После отказа в болиде должно быть только одно оружие");
    }
}
