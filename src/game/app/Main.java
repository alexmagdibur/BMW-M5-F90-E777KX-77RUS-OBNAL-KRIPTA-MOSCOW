package game.app;

import game.domain.Team;
import game.ui.ConsoleInput;
import game.ui.GameMenu;
import game.util.Ansi;

public class Main {

    public static void main(String[] args) {
        System.out.println(Ansi.bold("———————— ZOV AUTO: МОЖНО, А ЗАЧЕМ ————————"));

        String name = ConsoleInput.readLine("Введите имя вашей команды: ");
        Team playerTeam = GameInitializer.createPlayerTeam(name);

        GameMenu menu = new GameMenu(playerTeam);
        menu.run();

        ConsoleInput.close();
    }
}
