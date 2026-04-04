package service;

import domain.Bolid;
import domain.Component;
import domain.ComponentType;
import domain.Team;
import ui.ConsoleInput;
import util.Ansi;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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

        for (int i = 0; i < REQUIRED.length; i++) {
            Component choice = pickFromInventory(REQUIRED[i], REQUIRED_NAMES[i], false);
            if (choice == null) {
                System.out.println("Сборка отменена: не выбран обязательный компонент.");
                return;
            }
            selected.add(choice);
        }

        String incompatibility = checkCompatibility(selected);
        if (incompatibility != null) {
            System.out.println(Ansi.bold("\nСборка отменена: несовместимые компоненты."));
            System.out.println("  " + incompatibility);
            return;
        }

        System.out.println(Ansi.bold("\n———— Экстра (необязательно, можно несколько) ————"));
        List<Component> chosenExtras = new ArrayList<>();
        while (true) {
            List<Component> available = team.getInventory().stream()
                .filter(c -> c.getType() == ComponentType.EXTRA && !chosenExtras.contains(c))
                .toList();
            if (available.isEmpty()) {
                System.out.println("Больше нет экстра-компонентов в инвентаре.");
                break;
            }
            System.out.println("  Доступные экстра-компоненты:");
            for (int i = 0; i < available.size(); i++) {
                Component c = available.get(i);
                System.out.printf("   %d. %-27s | Перфоманс: %3d%n",
                    i + 1, c.getName(), c.getPerformanceValue());
            }
            System.out.println("   0. Готово");
            int choice = ConsoleInput.readInt("Ваш выбор: ");
            if (choice == 0) break;
            if (choice < 1 || choice > available.size()) { System.out.println("Неверный выбор."); continue; }
            Component picked = available.get(choice - 1);
            chosenExtras.add(picked);
            System.out.printf("Добавлено: %s%n", picked.getName());
        }

        Bolid bolid = new Bolid(bolidName);
        for (Component c : selected) {
            bolid.installComponent(c);
            team.removeComponent(c);
        }
        for (Component c : chosenExtras) {
            bolid.addExtra(c);
            team.removeComponent(c);
        }
        team.addBolid(bolid);

        System.out.println(Ansi.bold("\nБолид собран!"));
        System.out.printf("  %-27s | Перфоманс: %3d%n", bolid.getName(), bolid.getPerformanceScore());
        if (!chosenExtras.isEmpty()) {
            int extraBonus = chosenExtras.stream().mapToInt(Component::getPerformanceValue).sum();
            System.out.printf("  Бонус от экстры: +%d (%d компонент(а))%n", extraBonus, chosenExtras.size());
        }
    }

    private static final Set<ComponentType> COMPATIBILITY_TYPES = Set.of(
        ComponentType.ENGINE,
        ComponentType.TRANSMISSION,
        ComponentType.SUSPENSION,
        ComponentType.CHASSIS
    );


    private String checkCompatibility(List<Component> components) {
        List<Component> relevant = components.stream()
            .filter(c -> COMPATIBILITY_TYPES.contains(c.getType()))
            .toList();

        for (int i = 0; i < relevant.size(); i++) {
            for (int j = i + 1; j < relevant.size(); j++) {
                Component a = relevant.get(i);
                Component b = relevant.get(j);
                if (!a.isCompatibleWith(b)) {
                    return String.format("«%s» несовместим с «%s».", a.getName(), b.getName());
                }
            }
        }
        return null;
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
