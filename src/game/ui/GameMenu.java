package game.ui;

import game.domain.Team;

public class GameMenu {

    private Team playerTeam;
    private boolean running;

    public GameMenu(Team playerTeam) {
        this.playerTeam = playerTeam;
        this.running = true;
    }

    public void run() {
        while (running) {
            printMenu();
            int choice = ConsoleInput.readInt("Your choice: ");
            handleChoice(choice);
        }
    }

    private void printMenu() {
        System.out.println("\n========== ZovAuto — Кубок Аркхема ==========");
        System.out.println("Team: " + playerTeam.getName() + " | Budget: " + playerTeam.getBudget());
        System.out.println("----------------------------------------------");
        System.out.println(" 1. Start race");
        System.out.println(" 2. Buy components");
        System.out.println(" 3. Assemble bolid");
        System.out.println(" 4. Hire engineer");
        System.out.println(" 5. Hire pilot");
        System.out.println(" 6. View bolids");
        System.out.println(" 7. View pilots");
        System.out.println(" 8. Race statistics");
        System.out.println(" 9. View other teams");
        System.out.println("10. View other results");
        System.out.println("11. Exit");
        System.out.println("==============================================");
    }

    private void handleChoice(int choice) {
        switch (choice) {
            case 1 -> System.out.println("[Not implemented] Start race");
            case 2 -> System.out.println("[Not implemented] Buy components");
            case 3 -> System.out.println("[Not implemented] Assemble bolid");
            case 4 -> System.out.println("[Not implemented] Hire engineer");
            case 5 -> System.out.println("[Not implemented] Hire pilot");
            case 6 -> System.out.println("[Not implemented] View bolids");
            case 7 -> System.out.println("[Not implemented] View pilots");
            case 8 -> System.out.println("[Not implemented] Race statistics");
            case 9 -> System.out.println("[Not implemented] View other teams");
            case 10 -> System.out.println("[Not implemented] View other results");
            case 11 -> {
                System.out.println("Goodbye!");
                running = false;
            }
            default -> System.out.println("Invalid choice. Enter 1–11.");
        }
    }
}
