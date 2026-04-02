package game.service;

import game.data.EngineerCatalog;
import game.data.PilotCatalog;
import game.domain.Engineer;
import game.domain.Pilot;
import game.domain.Team;
import game.ui.ConsoleInput;
import game.util.Ansi;

import java.util.List;

public class HireService {

    private final Team team;

    public HireService(Team team) {
        this.team = team;
    }

    public void hirePilot() {
        System.out.println(Ansi.bold("\n———————— НАНЯТЬ ПИЛОТА ————————"));
        System.out.printf("Ваш бюджет: %,d руб.%n", team.getBudget());

        List<Pilot> options = PilotCatalog.getAll();
        for (int i = 0; i < options.size(); i++) {
            Pilot p = options.get(i);
            System.out.printf(" %d. %-25s | Скилл: %2d | Зарплата: %,d руб.%n",
                i + 1, p.getName(), p.getSkill(), p.getSalary());
        }
        System.out.println(" 0. Отмена");

        int choice = ConsoleInput.readInt("Ваш выбор: ");
        if (choice == 0) return;

        if (choice < 1 || choice > options.size()) {
            System.out.println("Неверный выбор.");
            return;
        }

        Pilot selected = options.get(choice - 1);
        if (!team.canAfford(selected.getSalary())) {
            System.out.printf("Недостаточно средств. Нужно %,d руб., есть %,d руб.%n",
                selected.getSalary(), team.getBudget());
            return;
        }

        team.deductBudget(selected.getSalary());
        team.hirePilot(selected);
        System.out.printf("Нанят пилот: %s (зарплата %,d руб.)%n", selected.getName(), selected.getSalary());
    }

    public void hireEngineer() {
        System.out.println(Ansi.bold("\n———————— НАНЯТЬ ИНЖЕНЕРА ————————"));
        System.out.printf("Ваш бюджет: %,d руб.%n", team.getBudget());

        List<Engineer> options = EngineerCatalog.getAll();
        for (int i = 0; i < options.size(); i++) {
            Engineer e = options.get(i);
            System.out.printf(" %d. %-25s | Скилл: %2d | Зарплата: %,d руб.%n",
                i + 1, e.getName(), e.getQualification(), e.getSalary());
        }
        System.out.println(" 0. Отмена");

        int choice = ConsoleInput.readInt("Ваш выбор: ");
        if (choice == 0) return;

        if (choice < 1 || choice > options.size()) {
            System.out.println("Неверный выбор.");
            return;
        }

        Engineer selected = options.get(choice - 1);
        if (!team.canAfford(selected.getSalary())) {
            System.out.printf("Недостаточно средств. Нужно %,d руб., есть %,d руб.%n",
                selected.getSalary(), team.getBudget());
            return;
        }

        team.deductBudget(selected.getSalary());
        team.hireEngineer(selected);
        System.out.printf("Нанят инженер: %s (зарплата %,d руб.)%n", selected.getName(), selected.getSalary());
    }
}
