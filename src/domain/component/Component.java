package domain.component;

public class Component {

    private final int id;
    private final String name;
    private final int price;
    private final ComponentType type;
    private double wear;

    public Component(int id, String name, int price, ComponentType type) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.type = type;
        this.wear = 0.0;
    }



    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getPrice() {
        return price;
    }

    public ComponentType getType() {
        return type;
    }

    public double getWear() {
        return wear;
    }

    public void addWear(double percent) {
        wear += percent;

        if (wear > 100) {
            wear = 100;
        }

        if (wear < 0) {
            wear = 0;
        }
    }

    public boolean isBroken() {
        return wear >= 100;
    }

    @Override
    public String toString() {
        return name + " | Цена: " + price + " | Износ: " + wear + "% | Тип: " + type;
    }


}