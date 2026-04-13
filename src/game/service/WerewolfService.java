package service;

import domain.Engineer;
import domain.Pilot;
import domain.Team;
import domain.TeamMember;
import ui.ConsoleInput;
import util.Ansi;

import java.util.ArrayList;
import java.util.List;

public class WerewolfService {

    private static final long VAN_HELSING_COST = 100_000L;
    private static final long BUFFY_COST = 75_000L;

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
        TeamMember target = selectStaffForWerewolfCheck("Ван Хельсинг");
        if (target == null) return;

        team.spend(VAN_HELSING_COST);
        if (target.isWerewolf()) {
            removeMember(target);
            System.out.printf("Ван Хельсинг уничтожил оборотня %s! %s удалён из команды.%n",
                target.getName(), roleLabel(target));
        } else {
            System.out.printf("%s %s не является оборотнем. Ван Хельсинг ушёл ни с чем.%n",
                roleLabel(target), target.getName());
        }
    }

    private void hireBuffy() {
        if (!team.canAfford(BUFFY_COST)) {
            System.out.printf("Недостаточно средств. Нужно %,d руб., есть %,d руб.%n",
                BUFFY_COST, team.getBudget());
            return;
        }
        TeamMember target = selectStaffForWerewolfCheck("Баффи");
        if (target == null) return;

        team.spend(BUFFY_COST);
        if (target.isWerewolf()) {
            target.setWerewolf(false);
            System.out.printf("Баффи вылечила оборотня %s! %s снова человек.%n",
                target.getName(), roleLabel(target));
        } else {
            System.out.printf("%s %s не является оборотнем. Баффи ушла ни с чем.%n",
                roleLabel(target), target.getName());
        }
    }

    private TeamMember selectStaffForWerewolfCheck(String hunter) {
        List<TeamMember> staff = new ArrayList<>();
        staff.addAll(team.getPilots());
        staff.addAll(team.getEngineers());

        if (staff.isEmpty()) {
            System.out.println("В команде нет персонала.");
            return null;
        }
        System.out.printf("%nВыберите члена команды для проверки (%s):%n", hunter);
        for (int i = 0; i < staff.size(); i++) {
            TeamMember m = staff.get(i);
            System.out.printf("  %d. [%s] %s%n", i + 1, roleLabel(m), m.getName());
        }
        System.out.println("  0. Отмена");
        int idx = ConsoleInput.readInt("Ваш выбор: ") - 1;
        if (idx < 0 || idx >= staff.size()) return null;
        return staff.get(idx);
    }

    private void removeMember(TeamMember member) {
        if (member instanceof Pilot p) {
            team.removePilot(p);
        } else if (member instanceof Engineer e) {
            team.removeEngineer(e);
        }
    }

    private static String roleLabel(TeamMember member) {
        if (member instanceof Pilot) return "Пилот";
        if (member instanceof Engineer) return "Инженер";
        return "Сотрудник";
    }
}
