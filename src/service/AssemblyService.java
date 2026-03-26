package service;

import domain.car.Car;
import domain.component.Component;
import domain.component.ComponentType;

public class AssemblyService {

    public boolean canInstall(Car car, Component component) {
        ComponentType type = component.getType();

        switch (type) {
            case ENGINE:
                if (car.getEngine() != null) {
                    System.out.println("Двигатель уже установлен!");
                    return false;
                }
                break;

            case TRANSMISSION:
                if (car.getTransmission() != null) {
                    System.out.println("Трансмиссия уже установлена!");
                    return false;
                }
                break;

            case SUSPENSION:
                if (car.getSuspension() != null) {
                    System.out.println("Подвеска уже установлена!");
                    return false;
                }
                break;

            case AEROKIT:
                if (car.getAerokit() != null) {
                    System.out.println("Аэрокит уже установлен!");
                    return false;
                }
                break;

            case TIRES:
                if (car.getTires() != null) {
                    System.out.println("Шины уже установлены!");
                    return false;
                }
                break;

            default:
                return false;
        }

        return true;
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

            default:
                System.out.println("Неизвестный тип компонента.");
                break;
        }
    }
}