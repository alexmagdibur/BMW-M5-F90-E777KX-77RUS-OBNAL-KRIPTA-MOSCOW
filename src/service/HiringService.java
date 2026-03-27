package service;

import domain.person.Engineer;
import domain.person.Pilot;
import domain.team.Team;

import java.util.ArrayList;
import java.util.List;

public class HiringService {

    private final List<Pilot> availablePilots;
    private final List<Engineer> availableEngineers;

    public HiringService() {
        this.availablePilots = new ArrayList<>();
        this.availableEngineers = new ArrayList<>();

        fillPilots();
        fillEngineers();
    }

    private void fillPilots() {
        availablePilots.add(new Pilot("Руслан Ахтямов", 90, 1200));
        availablePilots.add(new Pilot("Кирилл Куреев", 95, 1400));
        availablePilots.add(new Pilot("Святослав Новиков", 85, 1000));
    }

    private void fillEngineers() {
        availableEngineers.add(new Engineer("Елизавета Тихомирова", 95, 1300));
        availableEngineers.add(new Engineer("Илия Родионов", 94, 900));
        availableEngineers.add(new Engineer("Дмитрий Левиев", 10, 1100));
    }

    public void listPilots() {
        System.out.println("Доступные пилоты:");
        for (int i = 0; i < availablePilots.size(); i++) {
            System.out.println((i + 1) + ". " + availablePilots.get(i));
        }
    }

    public void listEngineers() {
        System.out.println("Доступные инженеры:");
        for (int i = 0; i < availableEngineers.size(); i++) {
            System.out.println((i + 1) + ". " + availableEngineers.get(i));
        }
    }

    public void hirePilotByIndex(Team team, int choice) {
        if (choice < 1 || choice > availablePilots.size()) {
            System.out.println("Неверный выбор пилота.");
            return;
        }

        Pilot pilot = (Pilot) availablePilots.get(choice - 1);

        if (hasPilot(team, pilot)) {
            System.out.println("Пилот " + pilot.getName() + " уже нанят в команду.");
            return;
        }

        if (!team.canAfford(pilot.getPrice())) {
            System.out.println("Недостаточно средств для найма пилота " + pilot.getName());
            return;
        }

        team.spend(pilot.getPrice());
        team.addPilot(pilot);

        System.out.println("Пилот " + pilot.getName() + " успешно нанят.");
        System.out.println("Оставшийся бюджет: " + team.getBudget());
    }

    public void hireEngineerByIndex(Team team, int choice) {
        if (choice < 1 || choice > availableEngineers.size()) {
            System.out.println("Неверный выбор инженера.");
            return;
        }

        Engineer engineer = (Engineer) availableEngineers.get(choice - 1);

        if (hasEngineer(team, engineer)) {
            System.out.println("Инженер " + engineer.getName() + " уже нанят в команду.");
            return;
        }

        if (!team.canAfford(engineer.getPrice())) {
            System.out.println("Недостаточно средств для найма инженера " + engineer.getName());
            return;
        }

        team.spend(engineer.getPrice());
        team.addEngineer(engineer);

        System.out.println("Инженер " + engineer.getName() + " успешно нанят.");
        System.out.println("Оставшийся бюджет: " + team.getBudget());
    }

    private boolean hasPilot(Team team, Pilot pilot) {
        for (Object obj : team.getPilots()) {
            Pilot teamPilot = (Pilot) obj;
            if (teamPilot.getName().equalsIgnoreCase(pilot.getName())) {
                return true;
            }
        }
        return false;
    }

    private boolean hasEngineer(Team team, Engineer engineer) {
        for (Object obj : team.getEngineers()) {
            Engineer teamEngineer = (Engineer) obj;
            if (teamEngineer.getName().equalsIgnoreCase(engineer.getName())) {
                return true;
            }
        }
        return false;
    }

    public List<Pilot> getAvailablePilots() {
        return availablePilots;
    }

    public List<Engineer> getAvailableEngineers() {
        return availableEngineers;
    }
}