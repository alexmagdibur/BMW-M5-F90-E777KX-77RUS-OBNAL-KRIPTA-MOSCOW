package domain;

public enum SectionType {
    STRAIGHT("Прямая"),
    TURN("Поворот"),
    CLIMB("Подъём"),
    DESCENT("Спуск");

    private final String displayName;

    SectionType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() { return displayName; }

    @Override
    public String toString() { return displayName; }
}
