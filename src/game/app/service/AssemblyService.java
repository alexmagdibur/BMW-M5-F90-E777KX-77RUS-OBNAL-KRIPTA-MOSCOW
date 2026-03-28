package game.app.service;

import game.app.domain.car.Car;
import game.app.domain.component.Component;
import game.app.domain.component.ComponentType;

import java.util.ArrayList;
import java.util.List;

public class AssemblyService {

    public boolean canInstall(Car car, Component component) {
        if (component == null) {
            System.out.println("Компонент не найден.");
            return false;
        }

        if (isSlotOccupied(car, component)) {
            return false;
        }

        String compatibilityError = getCompatibilityError(car, component);
        if (compatibilityError != null) {
            System.out.println(compatibilityError);
            return false;
        }

        return true;
    }

    private boolean isSlotOccupied(Car car, Component component) {
        ComponentType type = component.getType();

        switch (type) {
            case ENGINE:
                if (car.getEngine() != null) {
                    System.out.println("Двигатель уже установлен!");
                    return true;
                }
                break;

            case TRANSMISSION:
                if (car.getTransmission() != null) {
                    System.out.println("Трансмиссия уже установлена!");
                    return true;
                }
                break;

            case SUSPENSION:
                if (car.getSuspension() != null) {
                    System.out.println("Подвеска уже установлена!");
                    return true;
                }
                break;

            case AEROKIT:
                if (car.getAerokit() != null) {
                    System.out.println("Аэрокит уже установлен!");
                    return true;
                }
                break;

            case TIRES:
                if (car.getTires() != null) {
                    System.out.println("Шины уже установлены!");
                    return true;
                }
                break;

            case EXTRA:
                for (Component extra : car.getExtras()) {
                    if (extra.getName().equalsIgnoreCase(component.getName())) {
                        System.out.println("Такой доп уже установлен!");
                        return true;
                    }
                }
                break;

            default:
                System.out.println("Неизвестный тип компонента.");
                return true;
        }

        return false;
    }

    public String getCompatibilityError(Car car, Component newComponent) {
        if (newComponent.getType() == ComponentType.EXTRA) {
            return null;
        }

        List<Component> installedComponents = getInstalledMainComponents(car);

        for (Component installed : installedComponents) {
            if (!newComponent.isCompatibleWith(installed)) {
                return "Компонент " + newComponent.getName()
                        + " несовместим с уже установленным компонентом "
                        + installed.getName() + "!";
            }
        }

        return null;
    }

    private List<Component> getInstalledMainComponents(Car car) {
        List<Component> components = new ArrayList<>();

        if (car.getEngine() != null) {
            components.add(car.getEngine());
        }

        if (car.getTransmission() != null) {
            components.add(car.getTransmission());
        }

        if (car.getSuspension() != null) {
            components.add(car.getSuspension());
        }

        if (car.getAerokit() != null) {
            components.add(car.getAerokit());
        }

        if (car.getTires() != null) {
            components.add(car.getTires());
        }

        return components;
    }

    public void installComponent(Car car, Component component) {
        ComponentType type = component.getType();

        switch (type) {
            case ENGINE:
                car.setEngine(component);
                break;

            case TRANSMISSION:
                car.setTransmission(component);
                break;

            case SUSPENSION:
                car.setSuspension(component);
                break;

            case AEROKIT:
                car.setAerokit(component);
                break;

            case TIRES:
                car.setTires(component);
                break;

            case EXTRA:
                car.addExtra(component);
                break;

            default:
                System.out.println("Неизвестный тип компонента.");
                break;
        }
    }

    public ComponentType getNextRequiredType(Car car) {
        if (car.getEngine() == null) {
            return ComponentType.ENGINE;
        }

        if (car.getTransmission() == null) {
            return ComponentType.TRANSMISSION;
        }

        if (car.getSuspension() == null) {
            return ComponentType.SUSPENSION;
        }

        if (car.getAerokit() == null) {
            return ComponentType.AEROKIT;
        }

        if (car.getTires() == null) {
            return ComponentType.TIRES;
        }

        return null;
    }

    public boolean isMainBuildComplete(Car car) {
        return getNextRequiredType(car) == null;
    }
}