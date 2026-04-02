package game.app;

import game.domain.Team;
import game.ui.ConsoleInput;
import game.ui.GameMenu;
import game.util.Ansi;

public class Main {

    public static void main(String[] args) {
        System.out.println(Ansi.bold("———————— ZOV AUTO: МОЖНО, А ЗАЧЕМ ————————"));
        System.out.println("1. Новая игра");
        System.out.println("2. Тестовый режим");

        int choice = ConsoleInput.readInt("Введите ваш выбор: ");

        Team playerTeam;
        if (choice == 2) {
            playerTeam = GameInitializer.createTestTeam();
            System.out.println("Выбран тестовый режим");
        } else {
            String name = ConsoleInput.readLine("Введите имя вашей команды: ");
            playerTeam = GameInitializer.createPlayerTeam(name);
        }

        GameMenu menu = new GameMenu(playerTeam);
        menu.run();

        ConsoleInput.close();
    }
}
