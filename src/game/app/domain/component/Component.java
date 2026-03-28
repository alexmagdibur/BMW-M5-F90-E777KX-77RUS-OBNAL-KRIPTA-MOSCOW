package game.app.domain.component;

import java.util.Set;

public class Component {
    private final int id;
    private final String name;
    private final int price;
    private final ComponentType type;
    private double wear;

    // Параметры совместимости
    private final String series;              // street / sport / race
    private final int powerLevel;             // 1 / 2 / 3
    private final Set<String> compatibleWith; // с какими series совместим компонент

    public Component(int id,
                     String name,
                     int price,
                     ComponentType type,
                     String series,
                     int powerLevel,
                     Set<String> compatibleWith) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.type = type;
        this.series = series;
        this.powerLevel = powerLevel;
        this.compatibleWith = compatibleWith;
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

    public String getSeries() {
        return series;
    }

    public int getPowerLevel() {
        return powerLevel;
    }

    public Set<String> getCompatibleWith() {
        return compatibleWith;
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

    public void breakCompletely() {
        wear = 100;
    }

    public boolean isBroken() {
        return wear >= 100;
    }

    public boolean isCompatibleWith(Component other) {
        if (other == null) {
            return true;
        }

        boolean seriesMatch =
                compatibleWith.contains(other.getSeries()) &&
                        other.getCompatibleWith().contains(this.series);

        boolean powerMatch =
                Math.abs(this.powerLevel - other.getPowerLevel()) <= 1;

        return seriesMatch && powerMatch;
    }

    @Override
    public String toString() {
        return name
                + " | Цена: " + price
                + " | Износ: " + String.format("%.1f", wear) + "%"
                + " | Тип: " + type
                + " | Серия: " + series
                + " | Уровень: " + powerLevel;
    }
}