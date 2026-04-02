package game.domain;

import java.util.EnumMap;
import java.util.Map;

public class Bolid {

    private String name;
    private Map<ComponentType, Component> components;

    public Bolid(String name) {
        this.name = name;
        this.components = new EnumMap<>(ComponentType.class);
    }

    public String getName() { return name; }

    public void installComponent(Component component) {
        components.put(component.getType(), component);
    }

    public Component getComponent(ComponentType type) {
        return components.get(type);
    }

    public Map<ComponentType, Component> getComponents() {
        return components;
    }

    public boolean isComplete() {
        for (ComponentType type : ComponentType.values()) {
            if (!components.containsKey(type)) {
                return false;
            }
        }
        return true;
    }

    public int getPerformanceScore() {
        int total = 0;
        for (Component c : components.values()) {
            total += c.getPerformanceValue();
        }
        return total;
    }

    public boolean hasWornComponents() {
        for (Component c : components.values()) {
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
