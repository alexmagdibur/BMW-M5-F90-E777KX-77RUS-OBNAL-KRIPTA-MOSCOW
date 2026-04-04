package domain;

public class Engineer extends TeamMember {

    private int qualification;

    public Engineer(String name, int salary, int qualification) {
        super(name, salary);
        this.qualification = qualification;
    }

    public int getQualification() { return qualification; }

    @Override
    public String toString() {
        return String.format("Инженер: %s | Скилл: %d | Зарплата: %d", getName(), qualification, getSalary());
    }
}
