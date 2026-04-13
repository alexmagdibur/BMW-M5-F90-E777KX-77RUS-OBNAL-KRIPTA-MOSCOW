package domain;

public enum WeaponType {
    MELEE ("Ближний бой"),
    RANGED ("Дальний бой");

    private final String displayName;

    WeaponType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() { return displayName; }

    @Override
    public String toString() { return displayName; }
}
