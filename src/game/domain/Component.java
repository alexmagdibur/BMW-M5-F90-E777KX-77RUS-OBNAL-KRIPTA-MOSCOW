package domain;

public class Component {

    private String name;
    private ComponentType type;
    private int price;
    private int performanceValue;
    private int wear; // 0–100, percentage
    private int level; // 1=budget, 2=mid, 3=high — hidden from display

    /** Constructor for components without compatibility levels (AERO, TIRES, EXTRA). */
    public Component(String name, ComponentType type, int price, int performanceValue) {
        this(name, type, price, performanceValue, 2);
    }

    /** Constructor for components with an explicit compatibility level (1–3). */
    public Component(String name, ComponentType type, int price, int performanceValue, int level) {
        this.name = name;
        this.type = type;
        this.price = price;
        this.performanceValue = performanceValue;
        this.wear = 0;
        this.level = level;
    }

    public String getName() { return name; }
    public ComponentType getType() { return type; }
    public int getPrice() { return price; }
    public int getPerformanceValue() { return performanceValue; }
    public int getWear() { return wear; }

    /** Returns a fresh copy with the same level but wear reset to 0. */
    public Component copy() {
        return new Component(name, type, price, performanceValue, level);
    }

    /**
     * Compatibility applies only to ENGINE, TRANSMISSION, SUSPENSION, CHASSIS.
     * Level 1 ↔ 1, 2 | Level 2 ↔ any | Level 3 ↔ 2, 3
     * Incompatible pair: (1, 3) or (3, 1).
     */
    public boolean isCompatibleWith(Component other) {
        if ((this.level == 1 && other.level == 3) ||
            (this.level == 3 && other.level == 1)) {
            return false;
        }
        return true;
    }

    public void setWear(int wear) {
        this.wear = Math.min(100, Math.max(0, wear));
    }

    public void applyWear(int amount) {
        setWear(this.wear + amount);
    }

    public boolean isWornOut() {
        return wear > 50;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s | Перфоманс: %d | Цена: %d | Износ: %d%%",
                type, name, performanceValue, price, wear);
    }
}
