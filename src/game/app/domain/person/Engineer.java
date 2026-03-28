package game.app.domain.person;

public class Engineer {

    private final String name;
    private final int qualification;
    private final int price;

    public Engineer(String name, int qualification, int price) {
        this.name = name;
        this.qualification = qualification;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public int getQualification() {
        return qualification;
    }

    public int getPrice() {
        return price;
    }

    @Override
    public String toString() {
        return name + " | Квалификация: " + qualification + " | Цена: " + price;
    }
}