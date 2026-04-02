package game.domain;

public class Pilot extends TeamMember {

    private int skill; // 1–100
    private boolean isWerewolf; // hidden flag, used during solar eclipse

    public Pilot(String name, int salary, int skill) {
        super(name, salary);
        this.skill = skill;
        this.isWerewolf = false;
    }

    public int getSkill() { return skill; }
    public boolean isWerewolf() { return isWerewolf; }
    public void setWerewolf(boolean werewolf) { isWerewolf = werewolf; }

    @Override
    public String toString() {
        return String.format("Пилот: %s | Скилл: %d | Зарплата: %d", getName(), skill, getSalary());
    }
}
