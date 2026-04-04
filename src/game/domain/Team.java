package domain;

import java.util.ArrayList;
import java.util.List;
import util.Ansi;

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

    public void spend(long amount) {
        if (!canAfford(amount)) {
            throw new IllegalStateException("Недостаточно бюджета.");
        }
        budget -= amount;
    }

    public void earn(long amount) {
        budget += amount;
    }

    public void addComponent(Component component) {
        inventory.add(component);
    }

    public void removeComponent(Component component) {
        inventory.remove(component);
    }

    public void addBolid(Bolid bolid) {
        bolids.add(bolid);
    }

    public void addPilot(Pilot pilot) {
        pilots.add(pilot);
    }

    public void removePilot(Pilot pilot) {
        pilots.remove(pilot);
    }

    public void addEngineer(Engineer engineer) {
        engineers.add(engineer);
    }

    public void removeEngineer(Engineer engineer) {
        engineers.remove(engineer);
    }

    public boolean isReadyToRace() {
        boolean hasBolid = bolids.stream().anyMatch(Bolid::isComplete);
        return hasBolid && !pilots.isEmpty() && !engineers.isEmpty();
    }

    public String getBolidsInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append(Ansi.bold(String.format("\nБолиды (%d):%n", bolids.size())));
        if (bolids.isEmpty()) {
            sb.append("  —");
        } else {
            for (Bolid b : bolids)
                sb.append(String.format("%-27s | Перфоманс: %3d | Собран: %s%n",
                    b.getName(), b.getPerformanceScore(), b.isComplete() ? "Да" : "Нет"));
        }
        return sb.toString().stripTrailing();
    }

    public String getPilotsInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append(Ansi.bold(String.format("\nПилоты (%d):%n", pilots.size())));
        if (pilots.isEmpty()) {
            sb.append("  —");
        } else {
            for (Pilot p : pilots)
                sb.append(String.format("%-27s | Скилл: %3d | Зарплата: %,d руб.%n",
                    p.getName(), p.getSkill(), p.getSalary()));
        }
        return sb.toString().stripTrailing();
    }

    public String getEngineersInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append(Ansi.bold(String.format("\nИнженеры (%d):%n", engineers.size())));
        if (engineers.isEmpty()) {
            sb.append("  —");
        } else {
            for (Engineer e : engineers)
                sb.append(String.format("%-27s | Скилл: %3d | Зарплата: %,d руб.%n",
                    e.getName(), e.getQualification(), e.getSalary()));
        }
        return sb.toString().stripTrailing();
    }

    public String getInventoryInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append(Ansi.bold(String.format("\nИнвентарь (%d):%n", inventory.size())));
        if (inventory.isEmpty()) {
            sb.append("  —");
        } else {
            for (Component c : inventory)
                sb.append(String.format("%-27s | Перфоманс: %3d | Цена: %,8d руб. | Износ: %d%%%n",
                    c.getName(), c.getPerformanceValue(), c.getPrice(), c.getWear()));
        }
        return sb.toString().stripTrailing();
    }

    @Override
    public String toString() {
        return String.format("Команда: %s | Бюджет: %,d руб.", name, budget);
    }
}
