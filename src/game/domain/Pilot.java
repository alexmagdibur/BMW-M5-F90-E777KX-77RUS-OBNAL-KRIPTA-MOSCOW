package domain;

public class Pilot extends TeamMember {

    private int skill;
    private RaceTactic tactic;

    public Pilot(String name, int salary, int skill) {
        super(name, salary);
        this.skill = skill;
    }

    public int getSkill() { return skill; }
    public RaceTactic getTactic() { return tactic; }
    public void setTactic(RaceTactic tactic) { this.tactic = tactic; }

    @Override
    public String toString() {
        String tacticName = tactic != null ? tactic.getName() : "Нет тактики";
        return String.format("Пилот: %s | Скилл: %d | Зарплата: %d | Тактика: %s",
            getName(), skill, getSalary(), tacticName);
    }
}
