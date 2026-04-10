package domain;

public class Weapon {

    private final String     name;
    private final WeaponType type;
    private final int        price;
    private final int        damage;
    private final int        level;

    public Weapon(String name, WeaponType type, int price, int damage, int level) {
        this.name   = name;
        this.type   = type;
        this.price  = price;
        this.damage = damage;
        this.level  = level;
    }

    public String     getName()   { return name;   }
    public WeaponType getType()   { return type;   }
    public int        getPrice()  { return price;  }
    public int        getDamage() { return damage; }
    public int        getLevel()  { return level;  }

    /** Уровни 1 и 3 несовместимы между собой — аналогично Component. */
    public boolean isCompatibleWith(Weapon other) {
        return !((this.level == 1 && other.level == 3) ||
                 (this.level == 3 && other.level == 1));
    }

    public Weapon copy() {
        return new Weapon(name, type, price, damage, level);
    }

    @Override
    public String toString() {
        return String.format("[%s] %s | Урон: %d | Уровень: %d | Цена: %,d руб.",
            type, name, damage, level, price);
    }
}
