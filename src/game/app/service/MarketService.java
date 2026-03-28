package game.app.service;

import game.app.domain.component.Component;
import game.app.domain.component.ComponentType;
import game.app.domain.team.Team;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MarketService {
    private final List<Component> availableComponents;

    public MarketService() {
        this.availableComponents = new ArrayList<>();

        // ДВИГАТЕЛИ
        availableComponents.add(new Component(
                1,
                "BMW B48 Street",
                1000,
                ComponentType.ENGINE,
                "street",
                1,
                Set.of("street", "sport")
        ));

        availableComponents.add(new Component(
                2,
                "BMW B58 Sport",
                1800,
                ComponentType.ENGINE,
                "sport",
                2,
                Set.of("street", "sport", "race")
        ));

        availableComponents.add(new Component(
                3,
                "BMW S63 Race",
                3000,
                ComponentType.ENGINE,
                "race",
                3,
                Set.of("sport", "race")
        ));

        // ТРАНСМИССИИ
        availableComponents.add(new Component(
                4,
                "Street МКПП",
                800,
                ComponentType.TRANSMISSION,
                "street",
                1,
                Set.of("street", "sport")
        ));

        availableComponents.add(new Component(
                5,
                "Sport DCT",
                1500,
                ComponentType.TRANSMISSION,
                "sport",
                2,
                Set.of("street", "sport", "race")
        ));

        availableComponents.add(new Component(
                6,
                "Race Sequential",
                2500,
                ComponentType.TRANSMISSION,
                "race",
                3,
                Set.of("sport", "race")
        ));

        // ПОДВЕСКА
        availableComponents.add(new Component(
                7,
                "Street Suspension",
                600,
                ComponentType.SUSPENSION,
                "street",
                1,
                Set.of("street", "sport")
        ));

        availableComponents.add(new Component(
                8,
                "Sport Suspension",
                1200,
                ComponentType.SUSPENSION,
                "sport",
                2,
                Set.of("street", "sport", "race")
        ));

        availableComponents.add(new Component(
                9,
                "Race Suspension",
                2000,
                ComponentType.SUSPENSION,
                "race",
                3,
                Set.of("sport", "race")
        ));

        // АЭРОКИТЫ
        availableComponents.add(new Component(
                10,
                "Street Aero",
                500,
                ComponentType.AEROKIT,
                "street",
                1,
                Set.of("street", "sport")
        ));

        availableComponents.add(new Component(
                11,
                "Sport Aero",
                1000,
                ComponentType.AEROKIT,
                "sport",
                2,
                Set.of("street", "sport", "race")
        ));

        availableComponents.add(new Component(
                12,
                "Race Aero",
                1700,
                ComponentType.AEROKIT,
                "race",
                3,
                Set.of("sport", "race")
        ));

        // ШИНЫ
        availableComponents.add(new Component(
                13,
                "Street Tires",
                300,
                ComponentType.TIRES,
                "street",
                1,
                Set.of("street", "sport")
        ));

        availableComponents.add(new Component(
                14,
                "Sport Tires",
                700,
                ComponentType.TIRES,
                "sport",
                2,
                Set.of("street", "sport", "race")
        ));

        availableComponents.add(new Component(
                15,
                "Slick Race Tires",
                1300,
                ComponentType.TIRES,
                "race",
                3,
                Set.of("sport", "race")
        ));

        // ДОПОЛНИТЕЛЬНЫЕ КОМПОНЕНТЫ
        availableComponents.add(new Component(
                16,
                "Тонировка в круг",
                200,
                ComponentType.EXTRA,
                "street",
                1,
                Set.of("street", "sport", "race")
        ));

        availableComponents.add(new Component(
                17,
                "Блатные номера Е777КХ 77RUS",
                350,
                ComponentType.EXTRA,
                "street",
                1,
                Set.of("street", "sport", "race")
        ));

        availableComponents.add(new Component(
                18,
                "Автозвук Pride Car Audio",
                350,
                ComponentType.EXTRA,
                "sport",
                2,
                Set.of("street", "sport", "race")
        ));
    }

    public void listComponents() {
        System.out.println("Доступные компоненты для покупки:");
        for (int i = 0; i < availableComponents.size(); i++) {
            System.out.println((i + 1) + ". " + availableComponents.get(i));
        }
    }

    public void listComponentsByType(ComponentType type) {
        System.out.println("Доступные компоненты типа " + type + ":");

        List<Component> filtered = getComponentsByType(type);
        for (int i = 0; i < filtered.size(); i++) {
            System.out.println((i + 1) + ". " + filtered.get(i));
        }

        if (filtered.isEmpty()) {
            System.out.println("Компоненты этого типа отсутствуют.");
        }
    }

    public List<Component> getComponentsByType(ComponentType type) {
        List<Component> result = new ArrayList<>();

        for (Component component : availableComponents) {
            if (component.getType() == type) {
                result.add(component);
            }
        }

        return result;
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

    public void buyComponentByTypeAndIndex(Team team, ComponentType type, int choice) {
        List<Component> filtered = getComponentsByType(type);

        if (choice < 1 || choice > filtered.size()) {
            System.out.println("Неверный выбор комплектующего.");
            return;
        }

        Component component = filtered.get(choice - 1);

        if (!team.canAfford(component.getPrice())) {
            System.out.println("Недостаточно средств для покупки компонента " + component.getName());
            return;
        }

        team.spend(component.getPrice());
        team.addComponent(component);

        System.out.println("Компонент " + component.getName() + " успешно куплен!");
        System.out.println("Оставшийся бюджет: " + team.getBudget());
    }

    public List<Component> getAvailableComponents() {
        return availableComponents;
    }
}