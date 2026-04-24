package service;

import data.ComponentCatalog;
import domain.Component;
import domain.ComponentType;
import domain.EmergencyKit;
import domain.Team;
import ui.ConsoleInput;
import util.Ansi;

import java.util.List;

public class ShopService {

    private static final ComponentType[] ORDER = {
        ComponentType.ENGINE, ComponentType.TRANSMISSION, ComponentType.SUSPENSION,
        ComponentType.CHASSIS, ComponentType.AERO_PACKAGE, ComponentType.TIRES, ComponentType.EXTRA
    };

    private static final String[] TYPE_NAMES = {
        "Двигатель", "Трансмиссия", "Подвеска",
        "Шасси", "Обвесы", "Шины", "Дополнително"
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

        buyEmergencyKit();

        System.out.println("Покупка завершена. Компоненты добавлены в инвентарь.");
    }

    private void buyEmergencyKit() {
        EmergencyKit kit = team.getEmergencyKit();
        if (kit.isComplete()) {
            System.out.println(Ansi.bold("\n———— Набор экстренной помощи ————"));
            System.out.println("Набор укомплектован полностью.");
            return;
        }

        System.out.println(Ansi.bold("\n———— Набор экстренной помощи ————"));
        System.out.println("Обязателен для допуска к гонке!");
        System.out.printf("Бюджет: %,d руб.%n", team.getBudget());

        while (true) {
            System.out.println();
            printKitItem(1, "Аптечка",            5_000,  kit.hasFirstAidKit());
            printKitItem(2, "Огнетушитель",        8_000,  kit.hasFireExtinguisher());
            printKitItem(3, "Знак аварийной остановки", 2_000, kit.hasWarningTriangle());
            System.out.println(" 0. Продолжить");

            int choice = ConsoleInput.readInt("Ваш выбор: ");
            if (choice == 0) break;

            switch (choice) {
                case 1 -> buyKitItem("Аптечка",                 5_000,  kit.hasFirstAidKit(),
                                     () -> kit.setFirstAidKit(true));
                case 2 -> buyKitItem("Огнетушитель",             8_000,  kit.hasFireExtinguisher(),
                                     () -> kit.setFireExtinguisher(true));
                case 3 -> buyKitItem("Знак аварийной остановки", 2_000,  kit.hasWarningTriangle(),
                                     () -> kit.setWarningTriangle(true));
                default -> System.out.println("Неверный выбор.");
            }

            if (kit.isComplete()) {
                System.out.println("Набор экстренной помощи укомплектован!");
                break;
            }
        }
    }

    private void printKitItem(int num, String name, long price, boolean owned) {
        String status = owned ? "[КУПЛЕНО]" : String.format("%,d руб.", price);
        System.out.printf(" %d. %-35s | %s%n", num, name, status);
    }

    private void buyKitItem(String name, long price, boolean alreadyOwned, Runnable applyPurchase) {
        if (alreadyOwned) {
            System.out.println("«" + name + "» уже куплена.");
            return;
        }
        if (!team.canAfford(price)) {
            System.out.printf("Недостаточно средств. Нужно %,d руб., есть %,d руб.%n",
                    price, team.getBudget());
            return;
        }
        team.spend(price);
        applyPurchase.run();
        System.out.printf("Куплено: %s за %,d руб. | Бюджет: %,d руб.%n",
                name, price, team.getBudget());
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