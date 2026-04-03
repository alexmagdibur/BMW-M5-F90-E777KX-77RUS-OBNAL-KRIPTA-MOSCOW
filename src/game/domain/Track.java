package game.domain;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Track {

    private final String name;
    private final List<TrackSection> sections;

    public Track(String name, List<TrackSection> sections) {
        this.name = name;
        this.sections = List.copyOf(sections);
    }

    public String getName() { return name; }
    public List<TrackSection> getSections() { return sections; }

    public int getTotalLength() {
        return sections.stream().mapToInt(TrackSection::getLength).sum();
    }

    /** Суммарная длина секций каждого типа. */
    public Map<SectionType, Integer> getLengthByType() {
        return sections.stream().collect(
            Collectors.groupingBy(TrackSection::getType,
                Collectors.summingInt(TrackSection::getLength))
        );
    }

    /** Количество секций каждого типа. */
    public Map<SectionType, Long> getCountByType() {
        return sections.stream().collect(
            Collectors.groupingBy(TrackSection::getType, Collectors.counting())
        );
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Трасса: %s | Длина: %,d м | Секций: %d%n",
            name, getTotalLength(), sections.size()));

        Map<SectionType, Integer> byType = getLengthByType();
        for (SectionType t : SectionType.values()) {
            int len = byType.getOrDefault(t, 0);
            if (len > 0) {
                sb.append(String.format("  %-10s %,d м%n", t + ":", len));
            }
        }

        sb.append("  Секции: ");
        sb.append(sections.stream()
            .map(TrackSection::toString)
            .collect(Collectors.joining(", ")));

        return sb.toString().stripTrailing();
    }
}
