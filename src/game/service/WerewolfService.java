package service;

import domain.Pilot;
import domain.Team;
import ui.ConsoleInput;
import util.Ansi;

import java.util.List;

public class WerewolfService {

    private static final long VAN_HELSING_COST = 100_000L;
    private static final long BUFFY_COST        =  75_000L;

    private final Team team;

    public WerewolfService(Team team) {
        this.team = team;
    }

    public void werewolfHunt() {
        System.out.println(Ansi.bold("\n———————— ВЫЧИСЛЕНИЕ ОБОРОТНЕЙ ————————"));
        System.out.printf("Бюджет: %,d руб.%n", team.getBudget());
        System.out.println("1. Нанять Ван Хельсинга (" + String.format("%,d", VAN_HELSING_COST) + " руб.) — уничтожить оборотня");
        System.out.println("2. Нанять Баффи (" + String.format("%,d", BUFFY_COST) + " руб.) — вылечить оборотня");
        System.out.println("0. Назад");

        int choice = ConsoleInput.readInt("Ваш выбор: ");
        switch (choice) {
            case 1 -> hireVanHelsing();
            case 2 -> hireBuffy();
            case 0 -> {}
            default -> System.out.println("Неверный выбор.");
        }
    }

    private void hireVanHelsing() {
        if (!team.canAfford(VAN_HELSING_COST)) {
            System.out.printf("Недостаточно средств. Нужно %,d руб., есть %,d руб.%n",
                VAN_HELSING_COST, team.getBudget());
            return;
        }
        Pilot target = selectPilotForWerewolfCheck("Ван Хельсинг");
        if (target == null) return;

        team.spend(VAN_HELSING_COST);
        if (target.isWerewolf()) {
            team.removePilot(target);
            System.out.printf("Ван Хельсинг уничтожил оборотня %s! Пилот удалён из команды.%n", target.getName());
        } else {
            System.out.printf("Пилот %s не является оборотнем. Ван Хельсинг ушёл ни с чем.%n", target.getName());
        }
    }

    private void hireBuffy() {
        if (!team.canAfford(BUFFY_COST)) {
            System.out.printf("Недостаточно средств. Нужно %,d руб., есть %,d руб.%n",
                BUFFY_COST, team.getBudget());
            return;
        }
        Pilot target = selectPilotForWerewolfCheck("Баффи");
        if (target == null) return;

        team.spend(BUFFY_COST);
        if (target.isWerewolf()) {
            target.setWerewolf(false);
            System.out.printf("Баффи вылечила оборотня %s! Пилот снова человек.%n", target.getName());
        } else {
            System.out.printf("Пилот %s не является оборотнем. Баффи ушла ни с чем.%n", target.getName());
        }
    }

    private Pilot selectPilotForWerewolfCheck(String hunter) {
        List<Pilot> pilots = team.getPilots();
        if (pilots.isEmpty()) {
            System.out.println("В команде нет пилотов.");
            return null;
        }
        System.out.printf("%nВыберите пилота для проверки (%s):%n", hunter);
        for (int i = 0; i < pilots.size(); i++) {
            System.out.printf("  %d. %s%n", i + 1, pilots.get(i).getName());
        }
        System.out.println("  0. Отмена");
        int idx = ConsoleInput.readInt("Ваш выбор: ") - 1;
        if (idx < 0 || idx >= pilots.size()) return null;
        return pilots.get(idx);
    }
}
