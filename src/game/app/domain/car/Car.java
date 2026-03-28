package game.app.domain.car;

import game.app.domain.component.Component;
import game.app.domain.component.ComponentType;

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

    public String getName() {
        return name;
    }

    public boolean isComplete() {
        return engine != null
                && transmission != null
                && suspension != null
                && aerokit != null
                && tires != null;
    }

    public ComponentType getNextRequiredType() {
        if (engine == null) {
            return ComponentType.ENGINE;
        }
        if (transmission == null) {
            return ComponentType.TRANSMISSION;
        }
        if (suspension == null) {
            return ComponentType.SUSPENSION;
        }
        if (aerokit == null) {
            return ComponentType.AEROKIT;
        }
        if (tires == null) {
            return ComponentType.TIRES;
        }
        return null;
    }

    public boolean hasMainComponent(ComponentType type) {
        switch (type) {
            case ENGINE:
                return engine != null;
            case TRANSMISSION:
                return transmission != null;
            case SUSPENSION:
                return suspension != null;
            case AEROKIT:
                return aerokit != null;
            case TIRES:
                return tires != null;
            default:
                return false;
        }
    }

    public Component getComponentByType(ComponentType type) {
        switch (type) {
            case ENGINE:
                return engine;
            case TRANSMISSION:
                return transmission;
            case SUSPENSION:
                return suspension;
            case AEROKIT:
                return aerokit;
            case TIRES:
                return tires;
            default:
                return null;
        }
    }

    public void setComponentByType(Component component) {
        if (component == null) {
            return;
        }

        switch (component.getType()) {
            case ENGINE:
                this.engine = component;
                break;
            case TRANSMISSION:
                this.transmission = component;
                break;
            case SUSPENSION:
                this.suspension = component;
                break;
            case AEROKIT:
                this.aerokit = component;
                break;
            case TIRES:
                this.tires = component;
                break;
            case EXTRA:
                addExtra(component);
                break;
            default:
                break;
        }
    }

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
        if (extra == null) {
            return;
        }

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

    public int getInstalledMainComponentsCount() {
        int count = 0;

        if (engine != null) count++;
        if (transmission != null) count++;
        if (suspension != null) count++;
        if (aerokit != null) count++;
        if (tires != null) count++;

        return count;
    }

    private String componentInfo(Component component) {
        if (component == null) {
            return "не установлен";
        }

        return component.getName()
                + " [" + component.getSeries()
                + ", lvl " + component.getPowerLevel()
                + ", износ " + String.format("%.1f", component.getWear()) + "%]";
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

        String nextStep = getNextRequiredType() == null
                ? "основная сборка завершена"
                : "следующий обязательный компонент: " + getNextRequiredType();

        return "Болид: " + name
                + "\nДвигатель: " + componentInfo(engine)
                + "\nТрансмиссия: " + componentInfo(transmission)
                + "\nПодвеска: " + componentInfo(suspension)
                + "\nАэрокит: " + componentInfo(aerokit)
                + "\nШины: " + componentInfo(tires)
                + "\nДопы: " + extrasInfo
                + "\nОсновных компонентов установлено: " + getInstalledMainComponentsCount() + "/5"
                + "\nСтатус сборки: " + nextStep;
    }
}