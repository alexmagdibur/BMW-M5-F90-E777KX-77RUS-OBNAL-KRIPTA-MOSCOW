package domain;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class Bolid {

    private String name;
    private Map<ComponentType, Component> components;
    private List<Component> extras;

    public Bolid(String name) {
        this.name = name;
        this.components = new EnumMap<>(ComponentType.class);
        this.extras = new ArrayList<>();
    }

    public String getName() { return name; }

    public void installComponent(Component component) {
        components.put(component.getType(), component);
    }

    public void addExtra(Component component) {
        extras.add(component);
    }

    public Component getComponent(ComponentType type) {
        return components.get(type);
    }

    public Map<ComponentType, Component> getComponents() {
        return components;
    }

    public List<Component> getExtras() {
        return extras;
    }

    public List<Component> getAllComponents() {
        List<Component> all = new ArrayList<>(components.values());
        all.addAll(extras);
        return all;
    }

    private static final ComponentType[] REQUIRED = {
        ComponentType.ENGINE,
        ComponentType.TRANSMISSION,
        ComponentType.SUSPENSION,
        ComponentType.CHASSIS,
        ComponentType.AERO_PACKAGE,
        ComponentType.TIRES
    };

    public boolean isComplete() {
        for (ComponentType type : REQUIRED) {
            if (!components.containsKey(type)) return false;
        }
        return true;
    }

    public int getPerformanceScore() {
        int total = 0;
        for (Component c : getAllComponents()) {
            total += c.getPerformanceValue();
        }
        return total;
    }

    public boolean hasWornComponents() {
        for (Component c : getAllComponents()) {
            if (c.isWornOut()) return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return String.format("Болид: %s | Перфоманс: %d | Сборка: %s",
                name, getPerformanceScore(), isComplete() ? "Да" : "Нет");
    }
}
