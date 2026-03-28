package domain.race;

import domain.component.Component;

public class RaceIncident {

    private final String title;
    private final Component damagedComponent;

    public RaceIncident(String title, Component damagedComponent) {
        this.title = title;
        this.damagedComponent = damagedComponent;
    }

    public String buildStatus() {
        if (damagedComponent == null) {
            return title;
        }

        return title +  " (поврежден компонент \"" +
                damagedComponent.getName() + "\")";
    }
}