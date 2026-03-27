package service;

import domain.car.Car;
import domain.component.Component;
import domain.component.ComponentType;
import domain.person.Engineer;
import domain.person.Pilot;
import domain.race.RaceResult;
import domain.race.RaceTrack;
import domain.team.Team;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BotService {

    private final Random random;
    private final AssemblyService assemblyService;

    public BotService() {
        this.random = new Random();
        this.assemblyService = new AssemblyService();
    }

    public List<Team> createBotTeams(int count) {
        List<Team> bots = new ArrayList<>();

        for (int i = 1; i <= count; i++) {
            int budget = 8000 + random.nextInt(5001); // 8000..13000
            Team bot = new Team("Бот-команда " + i, budget);
            bots.add(bot);
        }

        return bots;
    }

    public Car prepareBotCar(Team bot) {
        Car car = new Car("Болид " + bot.getName());

        Pilot pilot = generatePilot();
        Engineer engineer = generateEngineer();

        bot.addPilot(pilot);
        bot.addEngineer(engineer);

        List<Component> components = generateComponents();

        int totalCost = pilot.getPrice() + engineer.getPrice();
        for (Component component : components) {
            totalCost += component.getPrice();
        }

        if (bot.getBudget() < totalCost) {
            bot.earn(totalCost - bot.getBudget());
        }

        bot.spend(pilot.getPrice());
        bot.spend(engineer.getPrice());

        for (Component component : components) {
            bot.spend(component.getPrice());
            if (assemblyService.canInstall(car, component)) {
                assemblyService.installComponent(car, component);
            }
        }

        bot.addCar(car);
        return car;
    }

    public RaceResult runBotRace(Team bot, Car car, RaceTrack track, RaceService raceService) {
        Pilot pilot = bot.getPilots().get(0);
        Engineer engineer = bot.getEngineers().get(0);

        boolean acceptedRisk = false;
        return raceService.simulateRace(car, pilot, engineer, track, acceptedRisk);
    }

    private Pilot generatePilot() {
        String[] names = {"Макс", "Леон", "Артем", "Никита", "Роман"};
        String name = names[random.nextInt(names.length)] + " #" + (100 + random.nextInt(900));
        int skill = 70 + random.nextInt(16); // 70..86
        int price = 900 + random.nextInt(301); // 900..1200
        return new Pilot(name, skill, price);
    }

    private Engineer generateEngineer() {
        String[] names = {"Игорь", "Олег", "Марк", "Денис", "Тимур"};
        String name = names[random.nextInt(names.length)] + " #" + (100 + random.nextInt(900));
        int qualification = 70 + random.nextInt(16); // 70..86
        int price = 800 + random.nextInt(401); // 800..1200
        return new Engineer(name, qualification, price);
    }

    private List<Component> generateComponents() {
        List<Component> components = new ArrayList<>();

        components.add(new Component(101, "Bot Engine", 1000, ComponentType.ENGINE));
        components.add(new Component(102, "Bot Transmission", 800, ComponentType.TRANSMISSION));
        components.add(new Component(103, "Bot Suspension", 600, ComponentType.SUSPENSION));
        components.add(new Component(104, "Bot Aerokit", 500, ComponentType.AEROKIT));
        components.add(new Component(105, "Bot Tires", 300, ComponentType.TIRES));

        return components;
    }
}