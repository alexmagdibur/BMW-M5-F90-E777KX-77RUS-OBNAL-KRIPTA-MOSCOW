package domain;

public abstract class TeamMember {

    private String name;
    private int salary;
    private boolean isWerewolf;

    public TeamMember(String name, int salary) {
        this.name = name;
        this.salary = salary;
        this.isWerewolf = false;
    }

    public String getName() { return name; }
    public int getSalary() { return salary; }
    public boolean isWerewolf() { return isWerewolf; }
    public void setWerewolf(boolean werewolf) { isWerewolf = werewolf; }

    @Override
    public String toString() {
        return String.format("%s | Salary: %d", name, salary);
    }
}
