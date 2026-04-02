package game.service;

import game.domain.Bolid;
import game.domain.Component;
import game.domain.ComponentType;
import game.domain.Team;
import game.ui.ConsoleInput;
import game.util.Ansi;

import java.util.ArrayList;
import java.util.List;

public class AssemblyService {

    private static final ComponentType[] REQUIRED = {
        ComponentType.ENGINE,
        ComponentType.TRANSMISSION,
        ComponentType.SUSPENSION,
        ComponentType.CHASSIS,
        ComponentType.AERO_PACKAGE,
        ComponentType.TIRES
    };

    private static final String[] REQUIRED_NAMES = {
        "Двигатель",
        "Трансмиссия",
        "Подвеска",
        "Шасси",
        "Обвесы",
        "Шины"
    };

    private final Team team;

    public AssemblyService(Team team) {
        this.team = team;
    }

    public void assembleBolid() {
        System.out.println(Ansi.bold("\n———————— СБОРКА БОЛИДА ————————"));

        if (team.getInventory().isEmpty()) {
            System.out.println("Инвентарь пуст. Сначала купите компоненты.");
            return;
        }

        String bolidName = ConsoleInput.readLine("Название болида: ");
        if (bolidName.isBlank()) {
            System.out.println("Отмена.");
            return;
        }

        List<Component> selected = new ArrayList<>();

        // Required slots
        for (int i = 0; i < REQUIRED.length; i++) {
            Component choice = pickFromInventory(REQUIRED[i], REQUIRED_NAMES[i], false);
            if (choice == null) {
                System.out.println("Сборка отменена: не выбран обязательный компонент.");
                return;
            }
            selected.add(choice);
        }

        // Optional EXTRA slot
        System.out.println(Ansi.bold("\n———— Экстра (необязательно) ————"));
        System.out.println("Компоненты типа EXTRA дают дополнительный перфоманс.");
        Component extra = pickFromInventory(ComponentType.EXTRA, "Экстра", true);
        if (extra != null) selected.add(extra);

        // Assemble
        Bolid bolid = new Bolid(bolidName);
        for (Component c : selected) {
            bolid.installComponent(c);
            team.removeComponent(c);
        }
        team.addBolid(bolid);

        System.out.println(Ansi.bold("\nБолид собран!"));
        System.out.printf("  %-27s | Перфоманс: %3d%n", bolid.getName(), bolid.getPerformanceScore());
        if (extra != null) {
            System.out.printf("  Бонус от экстры: +%d (компонент: %s)%n",
                extra.getPerformanceValue(), extra.getName());
        }
    }

    private Component pickFromInventory(ComponentType type, String typeName, boolean skippable) {
        List<Component> options = new ArrayList<>();
        for (Component c : team.getInventory()) {
            if (c.getType() == type) options.add(c);
        }

        if (options.isEmpty()) {
            if (skippable) {
                System.out.printf("Нет компонентов типа «%s» в инвентаре. Пропущено.%n", typeName);
                return null;
            }
            System.out.printf("Нет компонентов типа «%s» в инвентаре.%n", typeName);
            return null;
        }

        System.out.printf("%n  %s:%n", Ansi.bold(typeName));
        for (int i = 0; i < options.size(); i++) {
            Component c = options.get(i);
            System.out.printf("   %d. %-27s | Перфоманс: %3d | Износ: %d%%%n",
                i + 1, c.getName(), c.getPerformanceValue(), c.getWear());
        }
        if (skippable) System.out.println("   0. Пропустить");

        int choice = ConsoleInput.readInt("Ваш выбор: ");

        if (skippable && choice == 0) return null;

        if (choice < 1 || choice > options.size()) {
            System.out.println("Неверный выбор.");
            return null;
        }

        return options.get(choice - 1);
    }
}
