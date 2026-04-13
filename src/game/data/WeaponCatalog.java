package data;

import domain.Weapon;
import domain.WeaponType;

import java.util.List;

public class WeaponCatalog {

    // Уровень 1
    public static List<Weapon> getLevel1() {
        return List.of(
            new Weapon("Кастет ВАЗ", WeaponType.MELEE, 50_000, 25, 1),
            new Weapon("Рогатка-М", WeaponType.RANGED, 40_000, 20, 1)
        );
    }

    // Уровень 2
    public static List<Weapon> getLevel2() {
        return List.of(
            new Weapon("Цепь BMW", WeaponType.MELEE, 180_000, 55, 2),
            new Weapon("Пистолет Макарова", WeaponType.RANGED, 150_000, 48, 2)
        );
    }

    // Уровень 3
    public static List<Weapon> getLevel3() {
        return List.of(
            new Weapon("Плазменный кулак", WeaponType.MELEE, 600_000, 95, 3),
            new Weapon("Рельсотрон Porsche", WeaponType.RANGED, 550_000, 90, 3)
        );
    }

    public static List<Weapon> getAll() {
        return List.of(
            new Weapon("Кастет ВАЗ", WeaponType.MELEE, 50_000, 25, 1),
            new Weapon("Рогатка-М", WeaponType.RANGED, 40_000, 20, 1),
            new Weapon("Цепь BMW", WeaponType.MELEE, 180_000, 55, 2),
            new Weapon("Пистолет Макарова", WeaponType.RANGED, 150_000, 48, 2),
            new Weapon("Плазменный кулак", WeaponType.MELEE, 600_000, 95, 3),
            new Weapon("Рельсотрон Porsche", WeaponType.RANGED, 550_000, 90, 3)
        );
    }

    public static List<Weapon> getByType(WeaponType type) {
        return getAll().stream().filter(w -> w.getType() == type).toList();
    }
}
