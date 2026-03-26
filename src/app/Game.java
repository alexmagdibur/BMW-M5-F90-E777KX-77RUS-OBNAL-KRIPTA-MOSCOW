package app;

import domain.car.Car;
import domain.component.Component;
import domain.person.Engineer;
import domain.person.Pilot;
import domain.team.Team;
import service.AssemblyService;
import service.HiringService;
import service.MarketService;
import ui.ConsoleMenu;
import ui.InputReader;
import domain.race.RaceResult;
import domain.race.RaceTrack;
import service.RaceService;
import service.WearService;

public class Game {

    private final ConsoleMenu menu;
    private final InputReader inputReader;
    private final AssemblyService assemblyService;
    private final MarketService marketService;
    private final HiringService hiringService;
    private final RaceService raceService;
    private boolean running;
    private final Team team;
    private final Car car;
    private final WearService wearService;

    public Game() {
        this.menu = new ConsoleMenu();
        this.assemblyService = new AssemblyService();
        this.inputReader = new InputReader();
        this.marketService = new MarketService();
        this.hiringService = new HiringService();
        this.raceService = new RaceService();
        this.running = true;
        this.team = new Team("Команда 1", 10000);
        this.car = new Car("Болид 1");
        this.wearService = new WearService();
    }

    public void run() {
        while (running) {
            menu.printMainMenu(team.getBudget());
            int choice = inputReader.readInt();
            handleChoice(choice);
        }
    }

    private void handleChoice(int choice) {
        switch (choice) {
            case 1 -> startRace();
            case 2 -> buyComponents();
            case 3 -> assembleCar();
            case 4 -> hireEngineer();
            case 5 -> hirePilot();
            case 6 -> showCars();
            case 7 -> showPilots();
            case 8 -> showEngineers();
            case 9 -> showRaceStatistics();
            case 0 -> stopGame();
            default -> System.out.println("Неверный ввод");
        }
    }

    private void startRace() {
        if (!canStartRace()) {
            return;
        }

        System.out.println("\n=== ВЫБОР ТРАССЫ ===");
        raceService.listTracks();
        System.out.println("0 - Вернуться в главное меню");


        int choice = inputReader.readInt();

        if (choice == 0) {
            System.out.println("Вы вернулись в главное меню.");
            return;
        }

        if (!raceService.isValidTrackChoice(choice)) {
            System.out.println("Неверный выбор трассы.");
            return;
        }

        RaceTrack selectedTrack = raceService.getTrackByChoice(choice);
        Pilot pilot = team.getPilots().get(0);
        Engineer engineer = team.getEngineers().get(0);

        RaceResult result = raceService.simulateRace(car, pilot, engineer, selectedTrack);
        team.addRaceResult(result);

        System.out.println("\n=== РЕЗУЛЬТАТ ГОНКИ ===");
        System.out.println("Трасса: " + selectedTrack.getName());
        System.out.println("Пилот: " + pilot.getName());
        System.out.println("Инженер: " + engineer.getName());
        System.out.println("Итоговое время: " + String.format("%.2f", result.getFinalTime()));
    }

    private void showRaceStatistics() {
        System.out.println("\n=== СТАТИСТИКА ГОНОК ===");

        if (team.getRaceResults().isEmpty()) {
            System.out.println("Гонок пока не было.");
            return;
        }

        for (RaceResult raceResult : team.getRaceResults()) {
            System.out.println(raceResult);
        }
    }

    private boolean canStartRace() {
        if (!isCarReady()) {
            System.out.println("\nНельзя начать гонку: болид собран не полностью.");
            return false;
        }

        if (!hasPilot()) {
            System.out.println("\nНельзя начать гонку: у команды нет пилота.");
            return false;
        }

        if (!hasEngineer()) {
            System.out.println("\nНельзя начать гонку: у команды нет инженера.");
            return false;
        }
        if (hasBrokenComponents()) {
            System.out.println("\nНельзя начать гонку: на болиде есть сломанные детали.");
            wearService.printWearReport(car);
            return false;
        }
        if (wearService.hasCriticalWear(car)) {
            System.out.println("\nВнимание: у некоторых деталей критический износ.");
            wearService.printWearReport(car);
        }

        return true;
    }

    private boolean isCarReady() {
        return car.isComplete();
    }

    private boolean hasPilot() {
        return !team.getPilots().isEmpty();
    }

    private boolean hasEngineer() {
        return !team.getEngineers().isEmpty();
    }

    private boolean hasBrokenComponents() {
        return wearService.hasBrokenComponents(car);
    }

    private void buyComponents() {
        boolean buying = true;

        while (buying) {
            System.out.println("\n=== МЕНЮ ПОКУПКИ ===");
            System.out.println("Оставшийся бюджет: " + team.getBudget());
            System.out.println("Выберите компоненты для покупки:");
            marketService.listComponents();
            System.out.println("0 - Вернуться в главное меню");

            int componentChoice = inputReader.readInt();

            if (componentChoice == 0) {
                buying = false;
                System.out.println("Вы вернулись в главное меню.");
                continue;
            }

            marketService.buyComponentByIndex(team, componentChoice);

            System.out.println("\n1. Купить еще компоненты");
            System.out.println("0. Вернуться в главное меню");
            int choice = inputReader.readInt();

            if (choice == 0) {
                buying = false;
                System.out.println("Вы вернулись в главное меню.");
            }
        }
    }

    private void assembleCar() {
        boolean assembling = true;

        while (assembling) {
            System.out.println("\n=== СБОРКА БОЛИДА ===");
            System.out.println(car);

            if (team.getComponents().isEmpty()) {
                System.out.println("У команды нет купленных комплектующих.");
                return;
            }

            printOwnedComponents();
            System.out.println("0 - Вернуться в главное меню");

            int choice = inputReader.readInt();

            if (choice == 0) {
                assembling = false;
                System.out.println("Вы вернулись в главное меню.");
                continue;
            }

            if (!isValidOwnedComponentChoice(choice)) {
                System.out.println("Неверный выбор. Попробуйте снова.");
                continue;
            }

            Component selectedComponent = team.getComponents().get(choice - 1);

            if (assemblyService.canInstall(car, selectedComponent)) {
                assemblyService.installComponent(car, selectedComponent);
                team.removeComponent(selectedComponent);

                System.out.println(selectedComponent.getName() + " успешно установлен в болид.");
            } else {
                System.out.println("Этот компонент нельзя установить.");
            }

            if (car.isComplete()) {
                System.out.println("Болид полностью собран!");
                assembling = false;
            }
        }
    }

    private void hirePilot() {
        boolean hiring = true;

        while (hiring) {
            System.out.println("\n=== НАЙМ ПИЛОТА ===");
            System.out.println("Оставшийся бюджет: " + team.getBudget());
            hiringService.listPilots();
            System.out.println("0 - Вернуться в главное меню");

            int choice = inputReader.readInt();

            if (choice == 0) {
                hiring = false;
                System.out.println("Вы вернулись в главное меню.");
                continue;
            }

            hiringService.hirePilotByIndex(team, choice);

            System.out.println("\n1. Нанять еще пилота");
            System.out.println("0. Вернуться в главное меню");
            int nextChoice = inputReader.readInt();

            if (nextChoice == 0) {
                hiring = false;
                System.out.println("Вы вернулись в главное меню.");
            }
        }
    }

    private void hireEngineer() {
        boolean hiring = true;

        while (hiring) {
            System.out.println("\n=== НАЙМ ИНЖЕНЕРА ===");
            System.out.println("Оставшийся бюджет: " + team.getBudget());
            hiringService.listEngineers();
            System.out.println("0 - Вернуться в главное меню");

            int choice = inputReader.readInt();

            if (choice == 0) {
                hiring = false;
                System.out.println("Вы вернулись в главное меню.");
                continue;
            }

            hiringService.hireEngineerByIndex(team, choice);

            System.out.println("\n1. Нанять еще инженера");
            System.out.println("0. Вернуться в главное меню");
            int nextChoice = inputReader.readInt();

            if (nextChoice == 0) {
                hiring = false;
                System.out.println("Вы вернулись в главное меню.");
            }
        }
    }

    private void showCars() {
        System.out.println("\n=== БОЛИДЫ КОМАНДЫ ===");
        System.out.println(car);
    }

    private void showPilots() {
        System.out.println("\n=== ПИЛОТЫ КОМАНДЫ ===");

        if (team.getPilots().isEmpty()) {
            System.out.println("У команды пока нет пилотов.");
            return;
        }

        for (Pilot pilot : team.getPilots()) {
            System.out.println(pilot);
        }
    }

    private void showEngineers() {
        System.out.println("\n=== ИНЖЕНЕРЫ КОМАНДЫ ===");

        if (team.getEngineers().isEmpty()) {
            System.out.println("У команды пока нет инженеров.");
            return;
        }

        for (Engineer engineer : team.getEngineers()) {
            System.out.println(engineer);
        }
    }

    private void printOwnedComponents() {
        System.out.println("\nКупленные комплектующие команды:");
        for (int i = 0; i < team.getComponents().size(); i++) {
            System.out.println((i + 1) + ". " + team.getComponents().get(i));
        }
    }

    private boolean isValidOwnedComponentChoice(int choice) {
        return choice >= 1 && choice <= team.getComponents().size();
    }

    private void stopGame() {
        running = false;
        System.out.println("Игра завершена");
    }
}