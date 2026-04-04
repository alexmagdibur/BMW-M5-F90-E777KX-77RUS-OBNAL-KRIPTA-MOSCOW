package data;

import domain.SectionType;
import domain.Track;
import domain.TrackSection;

import java.util.List;

public class TrackCatalog {

    public static List<Track> getAll() {
        return List.of(monza(), nurburgring(), sochiAutodrom());
    }

    private static Track monza() {
        return new Track("Монца", List.of(
            new TrackSection(SectionType.STRAIGHT, 1100),
            new TrackSection(SectionType.TURN,300),
            new TrackSection(SectionType.STRAIGHT, 750),
            new TrackSection(SectionType.TURN, 200),
            new TrackSection(SectionType.STRAIGHT, 600),
            new TrackSection(SectionType.TURN, 250),
            new TrackSection(SectionType.STRAIGHT, 900),
            new TrackSection(SectionType.TURN, 200)
        ));
    }

    private static Track nurburgring() {
        return new Track("Нюрбургринг", List.of(
            new TrackSection(SectionType.STRAIGHT, 650),
            new TrackSection(SectionType.CLIMB, 400),
            new TrackSection(SectionType.TURN, 350),
            new TrackSection(SectionType.DESCENT, 500),
            new TrackSection(SectionType.TURN, 300),
            new TrackSection(SectionType.CLIMB, 350),
            new TrackSection(SectionType.STRAIGHT, 500),
            new TrackSection(SectionType.DESCENT, 450),
            new TrackSection(SectionType.TURN, 400),
            new TrackSection(SectionType.STRAIGHT, 700)
        ));
    }

    private static Track sochiAutodrom() {
        return new Track("Сочи Автодром", List.of(
            new TrackSection(SectionType.STRAIGHT, 850),
            new TrackSection(SectionType.TURN, 400),
            new TrackSection(SectionType.STRAIGHT, 500),
            new TrackSection(SectionType.TURN, 350),
            new TrackSection(SectionType.STRAIGHT, 600),
            new TrackSection(SectionType.CLIMB, 250),
            new TrackSection(SectionType.DESCENT, 300),
            new TrackSection(SectionType.TURN, 300),
            new TrackSection(SectionType.STRAIGHT, 550)
        ));
    }
}
