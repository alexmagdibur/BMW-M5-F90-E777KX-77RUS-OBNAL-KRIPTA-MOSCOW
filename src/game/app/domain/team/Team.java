package game.app.domain.team;

import game.app.domain.car.Car;
import game.app.domain.component.Component;
import game.app.domain.person.Engineer;
import game.app.domain.person.Pilot;
import game.app.domain.race.RaceResult;
import java.util.ArrayList;
import java.util.List;

public class Team {

    private final String name;
    private int budget;

    private final List<Car> cars;
    private final List<Pilot> pilots;
    private final List<Engineer> engineers;
    private final List<Component> components;
    private final List<RaceResult> raceResults;

    public Team(String name, int budget) {
        this.name = name;
        this.budget = budget;
        this.cars = new ArrayList<>();
        this.pilots = new ArrayList<>();
        this.engineers = new ArrayList<>();
        this.components = new ArrayList<>();
        this.raceResults = new ArrayList<>();
    }

    public void addRaceResult(RaceResult raceResult) {
        raceResults.add(raceResult);
    }

    public List<RaceResult> getRaceResults() {
        return raceResults;
    }

    public boolean canAfford(int price) {
        return budget >= price;
    }

    public void spend(int price) {
        if (canAfford(price)) {
            budget -= price;
        }
    }

    public void earn(int amount) {
        if (amount > 0) {
            budget += amount;
        }
    }

    public void addCar(Car car) {
        cars.add(car);
    }

    public void addPilot(Pilot pilot) {
        pilots.add(pilot);
    }

    public void addEngineer(Engineer engineer) {
        engineers.add(engineer);
    }

    public List<Pilot> getPilots() {
        return pilots;
    }

    public List<Engineer> getEngineers() {
        return engineers;
    }

    public void addComponent(Component component) {
        components.add(component);
    }

    public void removeComponent(Component component) {
        components.remove(component);
    }

    public List<Component> getComponents() {
        return components;
    }

    public int getBudget() {
        return budget;
    }

    public String getName() {
        return name;
    }
}