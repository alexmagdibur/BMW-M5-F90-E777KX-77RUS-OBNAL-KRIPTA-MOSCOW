package service;

import domain.component.Component;
import domain.component.ComponentType;
import domain.team.Team;

import java.util.ArrayList;
import java.util.List;

public class MarketService {

    private final List<Component> availableComponents;

    public MarketService() {
        this.availableComponents = new ArrayList<>();

        availableComponents.add(new Component(1, "BMW B48 4-цилиндровый", 1000, ComponentType.ENGINE));
        availableComponents.add(new Component(2, "Пятиступенчатая МКПП ВАЗ 2115", 800, ComponentType.TRANSMISSION));
        availableComponents.add(new Component(3, "Подвеска от Toyota Land Cruiser 200", 600, ComponentType.SUSPENSION));
        availableComponents.add(new Component(4, "Обвесы BMW M5 F90 M-Sport", 500, ComponentType.AEROKIT));
        availableComponents.add(new Component(5, "Шины Michelin Pilot Sport", 300, ComponentType.TIRES));
        availableComponents.add(new Component(6, "Тонировка в круг", 200, ComponentType.EXTRA));
        availableComponents.add(new Component(7, "Блатные номера Е777КХ 77RUS", 350, ComponentType.EXTRA));
        availableComponents.add(new Component(8, "Автозвук Pride Car Audio", 350, ComponentType.EXTRA));
    }

    public void listComponents() {
        System.out.println("Доступные компоненты для покупки:");
        for (int i = 0; i < availableComponents.size(); i++) {
            System.out.println((i + 1) + ". " + availableComponents.get(i));
        }
    }

    public void buyComponentByIndex(Team team, int choice) {
        if (choice < 1 || choice > availableComponents.size()) {
            System.out.println("Неверный выбор комплектующего.");
            return;
        }

        Component component = availableComponents.get(choice - 1);

        if (!team.canAfford(component.getPrice())) {
            System.out.println("Недостаточно средств для покупки компонента " + component.getName());
            return;
        }

        team.spend(component.getPrice());
        team.addComponent(component);

        System.out.println("Компонент " + component.getName() + " успешно куплен!");
        System.out.println("Оставшийся бюджет: " + team.getBudget());
    }

}