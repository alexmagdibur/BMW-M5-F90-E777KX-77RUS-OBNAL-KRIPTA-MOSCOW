package domain.race;

import domain.component.Component;

public class RaceIncident {

    private final String title;
    private final String description;
    private final Component damagedComponent;

    public RaceIncident(String title, String description, Component damagedComponent) {
        this.title = title;
        this.description = description;
        this.damagedComponent = damagedComponent;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Component getDamagedComponent() {
        return damagedComponent;
    }

    public void applyDamage() {
        if (damagedComponent != null) {
            damagedComponent.breakCompletely();
        }
    }

    public String buildStatus() {
        if (damagedComponent == null) {
            return title + ": " + description;
        }

        return title + ": " + description + " (поврежден компонент \"" +
                damagedComponent.getName() + "\")";
    }
}