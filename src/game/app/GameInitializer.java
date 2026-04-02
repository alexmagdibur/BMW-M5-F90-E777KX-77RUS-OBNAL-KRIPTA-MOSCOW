package game.app;

import game.domain.*;

public class GameInitializer {

    /**
     * Creates a normal player team with a standard starting budget.
     */
    public static Team createPlayerTeam(String teamName) {
        return new Team(teamName, 10_000_000);
    }

    /**
     * Creates a player team pre-loaded with components, a pilot and an engineer.
     * Used for quick testing as required by the assignment.
     */
    public static Team createTestTeam() {
        Team team = new Team("Test Team", 999_999_999);

        // Components
        Component engine = new Component("V8 Turbo", ComponentType.ENGINE, 500_000, 85);
        Component transmission = new Component("6-Speed Auto", ComponentType.TRANSMISSION, 200_000, 70);
        Component suspension = new Component("Sport Suspension", ComponentType.SUSPENSION, 150_000, 75);
        Component chassis = new Component("Carbon Chassis", ComponentType.CHASSIS, 400_000, 90);
        Component aero = new Component("Standard Aero", ComponentType.AERO_PACKAGE, 100_000, 65);
        Component tires = new Component("Soft Tires", ComponentType.TIRES, 80_000, 80);

        team.addComponent(engine);
        team.addComponent(transmission);
        team.addComponent(suspension);
        team.addComponent(chassis);
        team.addComponent(aero);
        team.addComponent(tires);

        // Assemble a ready bolid
        Bolid bolid = new Bolid("Test Bolid #1");
        bolid.installComponent(engine);
        bolid.installComponent(transmission);
        bolid.installComponent(suspension);
        bolid.installComponent(chassis);
        bolid.installComponent(aero);
        bolid.installComponent(tires);
        team.addBolid(bolid);

        // Staff
        team.addPilot(new Pilot("Lewis Testov", 300_000, 90));
        team.addEngineer(new Engineer("Adrian Debugov", 250_000, 88));

        System.out.println(team);

        return team;
    }
}
