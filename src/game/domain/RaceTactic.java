package domain;

import java.util.Map;

public class RaceTactic {

    private String name;
    // коэффициент < 1.0 — быстрее, > 1.0 — медленнее
    private Map<Weather, Double> modifiers;

    public RaceTactic(String name, Map<Weather, Double> modifiers) {
        this.name = name;
        this.modifiers = modifiers;
    }

    public String getName() { return name; }
    public Map<Weather, Double> getModifiers() { return modifiers; }

    public void setName(String name) { this.name = name; }
    public void setModifiers(Map<Weather, Double> modifiers) { this.modifiers = modifiers; }

    // возвращает 1.0 если для данной погоды модификатор не задан
    public double getModifier(Weather weather) {
        return modifiers.getOrDefault(weather, 1.0);
    }

    @Override
    public String toString() {
        return String.format("Тактика: %s | Модификаторы: %s", name, modifiers);
    }
}
