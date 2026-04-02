package game.app;

import game.domain.Team;
import game.ui.ConsoleInput;
import game.ui.GameMenu;

public class Main {

    public static void main(String[] args) {
        System.out.println("ZovAuto — Кубок Аркхема");
        System.out.println("1. New game");
        System.out.println("2. Quick test mode (pre-built team)");

        int choice = ConsoleInput.readInt("Choose: ");

        Team playerTeam;
        if (choice == 2) {
            playerTeam = GameInitializer.createTestTeam();
            System.out.println("Test team loaded: " + playerTeam);
        } else {
            String name = ConsoleInput.readLine("Enter your team name: ");
            playerTeam = GameInitializer.createPlayerTeam(name);
            System.out.println("Team created: " + playerTeam);
        }

        GameMenu menu = new GameMenu(playerTeam);
        menu.run();

        ConsoleInput.close();
    }
}
