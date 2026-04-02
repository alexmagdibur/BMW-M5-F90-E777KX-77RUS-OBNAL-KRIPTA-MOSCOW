package game.domain;

public class Component {

    private String name;
    private ComponentType type;
    private int price;
    private int performanceValue;
    private int wear; // 0–100, percentage

    public Component(String name, ComponentType type, int price, int performanceValue) {
        this.name = name;
        this.type = type;
        this.price = price;
        this.performanceValue = performanceValue;
        this.wear = 0;
    }

    public String getName() { return name; }
    public ComponentType getType() { return type; }
    public int getPrice() { return price; }
    public int getPerformanceValue() { return performanceValue; }
    public int getWear() { return wear; }

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
        return String.format("[%s] %s | Perf: %d | Price: %d | Wear: %d%%",
                type, name, performanceValue, price, wear);
    }
}
