package game.service;

import game.data.ComponentCatalog;
import game.domain.Component;
import game.domain.ComponentType;
import game.domain.Team;
import game.ui.ConsoleInput;
import game.util.Ansi;

import java.util.List;

public class ShopService {

    private static final ComponentType[] ORDER = {
        ComponentType.ENGINE,
        ComponentType.TRANSMISSION,
        ComponentType.SUSPENSION,
        ComponentType.CHASSIS,
        ComponentType.AERO_PACKAGE,
        ComponentType.TIRES,
        ComponentType.EXTRA
    };

    private static final String[] TYPE_NAMES = {
        "Двигатель",
        "Трансмиссия",
        "Подвеска",
        "Шасси",
        "Обвесы",
        "Шины",
        "Дополнително"
    };

    private final Team team;

    public ShopService(Team team) {
        this.team = team;
    }

    public void openShop() {
        System.out.println(Ansi.bold("\n———————— МАГАЗИН КОМПОНЕНТОВ ————————"));
        System.out.printf("Ваш бюджет: %,d руб.%n", team.getBudget());
        System.out.println("Выбирайте компоненты по одному. 0 — пропустить категорию.");

        for (int i = 0; i < ORDER.length; i++) {
            ComponentType type = ORDER[i];
            System.out.println(Ansi.bold("\n———— " + TYPE_NAMES[i] + " ————"));
            buyCategory(type);
            System.out.printf("Бюджет: %,d руб.%n", team.getBudget());
        }

        System.out.println("Покупка завершена. Компоненты добавлены в инвентарь.");
    }

    private void buyCategory(ComponentType type) {
        List<Component> options = ComponentCatalog.getByType(type);

        while (true) {
            for (int i = 0; i < options.size(); i++) {
                Component c = options.get(i);
                System.out.printf(" %d. %-27s | Перфоманс: %2d | Цена: %,d руб.%n",
                    i + 1, c.getName(), c.getPerformanceValue(), c.getPrice());
            }
            System.out.println(" 0. Пропустить / Продолжить");

            int choice = ConsoleInput.readInt("Ваш выбор: ");

            if (choice == 0) return;

            if (choice < 1 || choice > options.size()) {
                System.out.println("Неверный выбор.");
                continue;
            }

            Component selected = options.get(choice - 1);

            if (!team.canAfford(selected.getPrice())) {
                System.out.printf("Недостаточно средств. Нужно %,d руб., есть %,d руб.%n",
                    selected.getPrice(), team.getBudget());
                return;
            }

            team.spend(selected.getPrice());
            team.addComponent(selected.copy());
            System.out.printf("Куплено: %s за %,d руб. | Бюджет: %,d руб.%n",
                selected.getName(), selected.getPrice(), team.getBudget());
            System.out.println(" 1. Купить ещё один");
            System.out.println(" 0. Продолжить");

            int next = ConsoleInput.readInt("Ваш выбор: ");
            if (next != 1) return;
        }
    }
}