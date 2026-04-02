package game.ui;

import game.domain.Team;
import game.service.HireService;
import game.service.ShopService;
import game.util.Ansi;

public class GameMenu {

    private final Team playerTeam;
    private boolean running;
    private final ShopService shopService;
    private final HireService hireService;

    public GameMenu(Team playerTeam) {
        this.playerTeam = playerTeam;
        this.running = true;
        this.shopService = new ShopService(playerTeam);
        this.hireService = new HireService(playerTeam);
    }

    public void run() {
        while (running) {
            printMenu();
            int choice = ConsoleInput.readInt("Введите ваш выбор: ");
            handleChoice(choice);
        }
    }

    private void printMenu() {
        System.out.println(Ansi.bold("\n———————— ГЛАВНОЕ МЕНЮ ————————"));
        System.out.println(playerTeam);
        System.out.println(" ");
        System.out.println(" 1. Начать гонку");
        System.out.println(" 2. Купить компоненты");
        System.out.println(" 3. Собрать болид");
        System.out.println(" 4. Нанять инженера");
        System.out.println(" 5. Нанять пилота");
        System.out.println(" 6. Посмотреть болиды");
        System.out.println(" 7. Посмотреть пилотов");
        System.out.println(" 8. Статистика гонок");
        System.out.println(" 9. Посмотреть другие команды");
        System.out.println("10. Посмотреть результаты");
        System.out.println("11. Выход");
    }

    private void handleChoice(int choice) {
        switch (choice) {
            case 1 -> System.out.println("[Not implemented] Start race");
            case 2 -> shopService.openShop();
            case 3 -> System.out.println("[Not implemented] Assemble bolid");
            case 4 -> hireService.hireEngineer();
            case 5 -> hireService.hirePilot();
            case 6 -> System.out.println("[Not implemented] View bolids");
            case 7 -> System.out.println("[Not implemented] View pilots");
            case 8 -> System.out.println("[Not implemented] Race statistics");
            case 9 -> System.out.println("[Not implemented] View other teams");
            case 10 -> System.out.println("[Not implemented] View other results");
            case 11 -> {
                System.out.println("До встречи!");
                running = false;
            }
            default -> System.out.println("Неверный выбор");
        }
    }
}
