package game.domain;

import java.util.ArrayList;
import java.util.List;

public class Team {

    private String name;
    private long budget;
    private List<Component> inventory;
    private List<Bolid> bolids;
    private List<Pilot> pilots;
    private List<Engineer> engineers;

    public Team(String name, long budget) {
        this.name = name;
        this.budget = budget;
        this.inventory = new ArrayList<>();
        this.bolids = new ArrayList<>();
        this.pilots = new ArrayList<>();
        this.engineers = new ArrayList<>();
    }

    public String getName() { return name; }
    public long getBudget() { return budget; }
    public List<Component> getInventory() { return inventory; }
    public List<Bolid> getBolids() { return bolids; }
    public List<Pilot> getPilots() { return pilots; }
    public List<Engineer> getEngineers() { return engineers; }

    public boolean canAfford(long amount) {
        return budget >= amount;
    }

    public void deductBudget(long amount) {
        if (!canAfford(amount)) {
            throw new IllegalStateException("Not enough budget.");
        }
        budget -= amount;
    }

    public void addBudget(long amount) {
        budget += amount;
    }

    public void addToInventory(Component component) {
        inventory.add(component);
    }

    public void removeFromInventory(Component component) {
        inventory.remove(component);
    }

    public void addBolid(Bolid bolid) {
        bolids.add(bolid);
    }

    public void hirePilot(Pilot pilot) {
        pilots.add(pilot);
    }

    public void hireEngineer(Engineer engineer) {
        engineers.add(engineer);
    }

    public boolean isReadyToRace() {
        boolean hasBolid = bolids.stream().anyMatch(Bolid::isComplete);
        return hasBolid && !pilots.isEmpty() && !engineers.isEmpty();
    }

    @Override
    public String toString() {
        return String.format("Team: %s | Budget: %d | Bolids: %d | Pilots: %d | Engineers: %d",
                name, budget, bolids.size(), pilots.size(), engineers.size());
    }
}
