package game.domain;

public abstract class TeamMember {

    private String name;
    private int salary;

    public TeamMember(String name, int salary) {
        this.name = name;
        this.salary = salary;
    }

    public String getName() { return name; }
    public int getSalary() { return salary; }

    @Override
    public String toString() {
        return String.format("%s | Salary: %d", name, salary);
    }
}
