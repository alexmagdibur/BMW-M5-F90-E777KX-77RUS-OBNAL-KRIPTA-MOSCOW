package app;

import domain.Team;
import ui.ConsoleInput;
import ui.GameMenu;
import util.Ansi;

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
