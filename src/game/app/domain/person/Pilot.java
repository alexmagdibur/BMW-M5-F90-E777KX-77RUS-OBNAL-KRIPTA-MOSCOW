package game.app.domain.person;

public class Pilot {

    private final String name;
    private final int skill;
    private final int price;

    public Pilot(String name, int skill, int price) {
        this.name = name;
        this.skill = skill;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public int getSkill() {
        return skill;
    }

    public int getPrice() {
        return price;
    }

    @Override
    public String toString() {
        return name + " | Навык: " + skill + " | Цена: " + price;
    }
}