import domain.*;
import org.junit.jupiter.api.Test;
import service.RaceCalculator;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class StaffBonusTest {

    private static final Track TRACK = new Track("Тест", List.of(
        new TrackSection(SectionType.STRAIGHT, 2000),
        new TrackSection(SectionType.TURN,      800),
        new TrackSection(SectionType.CLIMB,     500),
        new TrackSection(SectionType.DESCENT,   700)
    ));

    private static Bolid buildBolid() {
        Bolid b = new Bolid("Болид");
        b.installComponent(new Component("Двигатель",   ComponentType.ENGINE,       0, 60));
        b.installComponent(new Component("Трансмиссия", ComponentType.TRANSMISSION, 0, 55));
        b.installComponent(new Component("Подвеска",    ComponentType.SUSPENSION,   0, 50));
        b.installComponent(new Component("Шасси",       ComponentType.CHASSIS,       0, 50));
        b.installComponent(new Component("Обвесы",      ComponentType.AERO_PACKAGE, 0, 50));
        b.installComponent(new Component("Шины",        ComponentType.TIRES,         0, 60));
        return b;
    }

    @Test
    void higherSkillPilotFinishesFaster() {
        Engineer engineer = new Engineer("Инженер", 0, 50);

        double timeLow  = RaceCalculator.calculateTime(buildBolid(), new Pilot("Пилот", 0,  1), engineer, TRACK, Weather.DRY);
        double timeHigh = RaceCalculator.calculateTime(buildBolid(), new Pilot("Пилот", 0, 100), engineer, TRACK, Weather.DRY);

        assertTrue(timeHigh < timeLow, "Пилот с навыком 100 должен финишировать быстрее пилота с навыком 1");
    }

    @Test
    void higherQualificationEngineerFinishesFaster() {
        Pilot pilot = new Pilot("Пилот", 0, 50);

        double timeLow  = RaceCalculator.calculateTime(buildBolid(), pilot, new Engineer("Инженер", 0,  1), TRACK, Weather.DRY);
        double timeHigh = RaceCalculator.calculateTime(buildBolid(), pilot, new Engineer("Инженер", 0, 100), TRACK, Weather.DRY);

        assertTrue(timeHigh < timeLow, "Инженер с квалификацией 100 должен давать меньшее время, чем с квалификацией 1");
    }
}
