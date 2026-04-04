package domain;

public class Engineer extends TeamMember {

    private int qualification;
    private boolean isWerewolf;

    public Engineer(String name, int salary, int qualification) {
        super(name, salary);
        this.qualification = qualification;
        this.isWerewolf = false;
    }

    public int getQualification() { return qualification; }
    public boolean isWerewolf() { return isWerewolf; }
    public void setWerewolf(boolean werewolf) { isWerewolf = werewolf; }

    @Override
    public String toString() {
        return String.format("Инженер: %s | Скилл: %d | Зарплата: %d", getName(), qualification, getSalary());
    }
}
