package domain;

public class Pilot extends TeamMember {

    private int skill;

    public Pilot(String name, int salary, int skill) {
        super(name, salary);
        this.skill = skill;
    }

    public int getSkill() { return skill; }

    @Override
    public String toString() {
        return String.format("Пилот: %s | Скилл: %d | Зарплата: %d", getName(), skill, getSalary());
    }
}
