package domain;

public class TrackSection {

    private final SectionType type;
    private final int length;

    public TrackSection(SectionType type, int length) {
        this.type = type;
        this.length = length;
    }

    public SectionType getType() { return type; }
    public int getLength() { return length; }

    @Override
    public String toString() {
        return String.format("[%s] %d м", type, length);
    }
}
