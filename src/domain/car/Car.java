package domain.car;

import domain.component.Component;
import java.util.ArrayList;
import java.util.List;

public class Car {

    private final String name;
    private Component engine;
    private Component transmission;
    private Component suspension;
    private Component aerokit;
    private Component tires;
    private final List<Component> extras;

    public Car(String name) {
        this.name = name;
        this.extras = new ArrayList<>();
    }

    public boolean isComplete() {
        return engine != null &&
                transmission != null &&
                suspension != null &&
                aerokit != null &&
                tires != null;
    }

    // Геттеры и сеттеры для компонентов
    public Component getEngine() {
        return engine;
    }

    public void setEngine(Component engine) {
        this.engine = engine;
    }

    public Component getTransmission() {
        return transmission;
    }

    public void setTransmission(Component transmission) {
        this.transmission = transmission;
    }

    public Component getSuspension() {
        return suspension;
    }

    public void setSuspension(Component suspension) {
        this.suspension = suspension;
    }

    public Component getAerokit() {
        return aerokit;
    }

    public void setAerokit(Component aerokit) {
        this.aerokit = aerokit;
    }

    public Component getTires() {
        return tires;
    }

    public void setTires(Component tires) {
        this.tires = tires;
    }

    public List<Component> getExtras() {
        return extras;
    }

    public void addExtra(Component extra) {
        extras.add(extra);
    }

    public boolean hasExtra(String extraName) {
        for (Component extra : extras) {
            if (extra.getName().equalsIgnoreCase(extraName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        String extrasInfo;

        if (extras.isEmpty()) {
            extrasInfo = "нет";
        } else {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < extras.size(); i++) {
                builder.append(extras.get(i).getName());
                if (i < extras.size() - 1) {
                    builder.append(", ");
                }
            }
            extrasInfo = builder.toString();
        }

        return "Болид: " + name +
                "\nДвигатель: " + (engine == null ? "не установлен" : engine.getName()) +
                "\nТрансмиссия: " + (transmission == null ? "не установлена" : transmission.getName()) +
                "\nПодвеска: " + (suspension == null ? "не установлена" : suspension.getName()) +
                "\nАэрокит: " + (aerokit == null ? "не установлен" : aerokit.getName()) +
                "\nШины: " + (tires == null ? "не установлены" : tires.getName()) +
                "\nДопы: " + extrasInfo;
    }
}