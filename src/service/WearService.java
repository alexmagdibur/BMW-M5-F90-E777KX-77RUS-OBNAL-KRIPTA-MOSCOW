package service;

import domain.car.Car;
import domain.component.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WearService {

    private final Random random;

    public WearService() {
        this.random = new Random();
    }

    public void applyRaceWear(Car car) {
        applyWearToComponent(car.getEngine(), 8, 15);
        applyWearToComponent(car.getTransmission(), 6, 12);
        applyWearToComponent(car.getSuspension(), 5, 10);
        applyWearToComponent(car.getAerokit(), 3, 7);
        applyWearToComponent(car.getTires(), 10, 18);
    }

    private void applyWearToComponent(Component component, int minWear, int maxWear) {
        if (component == null) {
            return;
        }

        double wearValue = random.nextInt(maxWear - minWear + 1) + minWear;
        component.addWear(wearValue);
    }

    public boolean hasBrokenComponents(Car car) {
        for (Component component : getInstalledComponents(car)) {
            if (component.isBroken()) {
                return true;
            }
        }
        return false;
    }

    public boolean hasCriticalWear(Car car) {
        for (Component component : getInstalledComponents(car)) {
            if (component.getWear() >= 70 && !component.isBroken()) {
                return true;
            }
        }
        return false;
    }

    public void printWearReport(Car car) {
        System.out.println("\n=== СОСТОЯНИЕ КОМПЛЕКТУЮЩИХ ===");
        printComponentWear("Двигатель", car.getEngine());
        printComponentWear("Трансмиссия", car.getTransmission());
        printComponentWear("Подвеска", car.getSuspension());
        printComponentWear("Аэрокит", car.getAerokit());
        printComponentWear("Шины", car.getTires());
    }

    private void printComponentWear(String title, Component component) {
        if (component == null) {
            System.out.println(title + ": не установлен");
            return;
        }

        System.out.println(title + ": " + component.getName()
                + " | Износ: " + String.format("%.1f", component.getWear()) + "%");

        if (component.isBroken()) {
            System.out.println("  -> Деталь сломана!");
        } else if (component.getWear() >= 70) {
            System.out.println("  -> Критический износ!");
        } else if (component.getWear() >= 40) {
            System.out.println("  -> Повышенный износ");
        }
    }

    private List<Component> getInstalledComponents(Car car) {
        List<Component> components = new ArrayList<>();
        addIfNotNull(components, car.getEngine());
        addIfNotNull(components, car.getTransmission());
        addIfNotNull(components, car.getSuspension());
        addIfNotNull(components, car.getAerokit());
        addIfNotNull(components, car.getTires());
        return components;
    }

    private void addIfNotNull(List<Component> components, Component component) {
        if (component != null) {
            components.add(component);
        }
    }
}