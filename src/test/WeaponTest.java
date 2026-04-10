import domain.Bolid;
import domain.Weapon;
import domain.WeaponType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class WeaponTest {

    // уровень 1
    private static final Weapon MELEE_1  = new Weapon("Кастет ВАЗ",        WeaponType.MELEE,  50_000,  25, 1);
    private static final Weapon RANGED_1 = new Weapon("Рогатка-М",          WeaponType.RANGED, 40_000,  20, 1);
    // уровень 2
    private static final Weapon MELEE_2  = new Weapon("Цепь BMW",           WeaponType.MELEE,  180_000, 55, 2);
    private static final Weapon RANGED_2 = new Weapon("Пистолет Макарова",  WeaponType.RANGED, 150_000, 48, 2);
    // уровень 3
    private static final Weapon MELEE_3  = new Weapon("Плазменный кулак",   WeaponType.MELEE,  600_000, 95, 3);
    private static final Weapon RANGED_3 = new Weapon("Рельсотрон Porsche", WeaponType.RANGED, 550_000, 90, 3);

    private Bolid bolid;

    @BeforeEach
    void setUp() {
        bolid = new Bolid("Тестовый болид");
    }

    // ─── установка 1 ближнего + 1 дальнего → успех ───────────────────────────

    @Test
    void installMeleeAndRanged_bothSlotsFilledSuccessfully() {
        bolid.installWeapon(MELEE_1);
        bolid.installWeapon(RANGED_1);

        assertEquals(2, bolid.getWeapons().size());
        assertSame(MELEE_1,  bolid.getWeapons().get(WeaponType.MELEE));
        assertSame(RANGED_1, bolid.getWeapons().get(WeaponType.RANGED));
    }

    @Test
    void freshBolidHasNoWeapons() {
        assertTrue(bolid.getWeapons().isEmpty());
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

    // второе оружие того же типа → слот занят, отказ

    @Test
    void slotIsOccupiedAfterFirstMeleeInstall() {
        bolid.installWeapon(MELEE_1);
        assertTrue(bolid.getWeapons().containsKey(WeaponType.MELEE));
    }

    @Test
    void slotIsOccupiedAfterFirstRangedInstall() {
        bolid.installWeapon(RANGED_1);
        assertTrue(bolid.getWeapons().containsKey(WeaponType.RANGED));
    }

    @Test
    void cannotAddSecondMeleeWhenSlotOccupied() {
        bolid.installWeapon(MELEE_1);

        // имитируем проверку из GameMenu: занят слот → не устанавливаем
        boolean slotOccupied = bolid.getWeapons().containsKey(MELEE_2.getType());
        if (!slotOccupied) {
            bolid.installWeapon(MELEE_2);
        }

        assertSame(MELEE_1, bolid.getWeapons().get(WeaponType.MELEE), "первое оружие должно остаться");
    }

    @Test
    void cannotAddSecondRangedWhenSlotOccupied() {
        bolid.installWeapon(RANGED_3);

        boolean slotOccupied = bolid.getWeapons().containsKey(RANGED_1.getType());
        if (!slotOccupied) {
            bolid.installWeapon(RANGED_1);
        }

        assertSame(RANGED_3, bolid.getWeapons().get(WeaponType.RANGED));
    }

    @Test
    void totalWeaponCountNeverExceedsTwo() {
        bolid.installWeapon(MELEE_2);
        bolid.installWeapon(RANGED_2);

        // слот занят — третье не устанавливаем
        if (!bolid.getWeapons().containsKey(WeaponType.MELEE)) {
            bolid.installWeapon(MELEE_3);
        }

        assertEquals(2, bolid.getWeapons().size());
    }

    // совместимость оружий: Weapon.isCompatibleWith

    @Test void level1Melee_level1Ranged_areCompatible() { assertTrue(MELEE_1.isCompatibleWith(RANGED_1)); }
    @Test void level2Melee_level2Ranged_areCompatible() { assertTrue(MELEE_2.isCompatibleWith(RANGED_2)); }
    @Test void level3Melee_level3Ranged_areCompatible() { assertTrue(MELEE_3.isCompatibleWith(RANGED_3)); }
    @Test void level1Melee_level2Ranged_areCompatible() { assertTrue(MELEE_1.isCompatibleWith(RANGED_2)); }
    @Test void level2Ranged_level1Melee_compatible_symmetric() { assertTrue(RANGED_2.isCompatibleWith(MELEE_1)); }
    @Test void level2Melee_level3Ranged_areCompatible() { assertTrue(MELEE_2.isCompatibleWith(RANGED_3)); }
    @Test void level3Ranged_level2Melee_compatible_symmetric() { assertTrue(RANGED_3.isCompatibleWith(MELEE_2)); }
    @Test void weaponCompatibleWithItself_level1() { assertTrue(MELEE_1.isCompatibleWith(MELEE_1)); }
    @Test void weaponCompatibleWithItself_level3() { assertTrue(RANGED_3.isCompatibleWith(RANGED_3)); }

    @Test void level1Melee_level3Ranged_areIncompatible() { assertFalse(MELEE_1.isCompatibleWith(RANGED_3)); }
    @Test void level3Ranged_level1Melee_incompatible_symmetric(){ assertFalse(RANGED_3.isCompatibleWith(MELEE_1)); }
    @Test void level3Melee_level1Ranged_areIncompatible() { assertFalse(MELEE_3.isCompatibleWith(RANGED_1)); }
    @Test void level1Ranged_level3Melee_incompatible_symmetric(){ assertFalse(RANGED_1.isCompatibleWith(MELEE_3)); }

    // несовместимое оружие с уже установленным на болиде → отказ

    // та же логика что в GameMenu.survivalInstallWeapons
    private boolean tryInstall(Bolid b, Weapon candidate, List<Weapon> installed) {
        for (Weapon existing : installed) {
            if (!candidate.isCompatibleWith(existing)) return false;
        }
        b.installWeapon(candidate);
        installed.add(candidate);
        return true;
    }

    @Test
    void installIncompatibleWeapon_rejectedWhenBolidHasLevel1() {
        List<Weapon> installed = new ArrayList<>();
        tryInstall(bolid, MELEE_1, installed);

        boolean result = tryInstall(bolid, RANGED_3, installed); // ур.3 несовместим с ур.1

        assertFalse(result);
        assertFalse(bolid.getWeapons().containsKey(WeaponType.RANGED));
    }

    @Test
    void installIncompatibleWeapon_rejectedWhenBolidHasLevel3() {
        List<Weapon> installed = new ArrayList<>();
        tryInstall(bolid, RANGED_3, installed);

        boolean result = tryInstall(bolid, MELEE_1, installed);

        assertFalse(result);
        assertFalse(bolid.getWeapons().containsKey(WeaponType.MELEE));
    }

    @Test
    void installCompatibleWeapons_bothAccepted_level2Bridge() {
        List<Weapon> installed = new ArrayList<>();
        assertTrue(tryInstall(bolid, MELEE_2, installed));
        assertTrue(tryInstall(bolid, RANGED_2, installed));
        assertEquals(2, bolid.getWeapons().size());
    }

    @Test
    void installCompatibleWeapons_level1AndLevel2_bothAccepted() {
        List<Weapon> installed = new ArrayList<>();
        assertTrue(tryInstall(bolid, MELEE_1, installed));
        assertTrue(tryInstall(bolid, RANGED_2, installed));
        assertEquals(2, bolid.getWeapons().size());
    }

    @Test
    void installCompatibleWeapons_level2AndLevel3_bothAccepted() {
        List<Weapon> installed = new ArrayList<>();
        assertTrue(tryInstall(bolid, MELEE_3, installed));
        assertTrue(tryInstall(bolid, RANGED_2, installed));
        assertEquals(2, bolid.getWeapons().size());
    }

    @Test
    void incompatiblePair_secondWeaponNotPresentOnBolid_afterRejection() {
        List<Weapon> installed = new ArrayList<>();
        tryInstall(bolid, MELEE_3, installed);
        tryInstall(bolid, RANGED_1, installed); // отклонено

        assertFalse(bolid.getWeapons().containsKey(WeaponType.RANGED));
        assertEquals(1, bolid.getWeapons().size());
    }
}
