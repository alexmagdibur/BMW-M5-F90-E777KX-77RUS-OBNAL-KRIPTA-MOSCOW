import domain.*;
import org.junit.jupiter.api.Test;
import service.RaceCalculator;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class WeatherTest {

    private static final Bolid BOLID = buildBolid();
    private static final Pilot PILOT = new Pilot("Пилот", 0, 75);
    private static final Engineer ENGINEER = new Engineer("Инженер", 0, 60);
    private static final Track TRACK = new Track("Тест", List.of(
        new TrackSection(SectionType.STRAIGHT, 2000),
        new TrackSection(SectionType.TURN, 800),
        new TrackSection(SectionType.CLIMB, 500),
        new TrackSection(SectionType.DESCENT, 700)
    ));

    private static Bolid buildBolid() {
        Bolid b = new Bolid("Болид");
        b.installComponent(new Component("Двигатель", ComponentType.ENGINE, 0, 60));
        b.installComponent(new Component("Трансмиссия", ComponentType.TRANSMISSION, 0, 55));
        b.installComponent(new Component("Подвеска", ComponentType.SUSPENSION, 0, 50));
        b.installComponent(new Component("Шасси", ComponentType.CHASSIS, 0, 50));
        b.installComponent(new Component("Обвесы", ComponentType.AERO_PACKAGE, 0, 50));
        b.installComponent(new Component("Шины", ComponentType.TIRES, 0, 60));
        return b;
    }

    @Test
    void dryIsFasterThanWet() {
        double dry = RaceCalculator.calculateTime(BOLID, PILOT, ENGINEER, TRACK, Weather.DRY);
        double wet = RaceCalculator.calculateTime(BOLID, PILOT, ENGINEER, TRACK, Weather.WET);
        assertTrue(dry < wet, "Сухая трасса должна давать меньшее время, чем влажная");
    }

    @Test
    void dryIsFasterThanRain() {
        double dry = RaceCalculator.calculateTime(BOLID, PILOT, ENGINEER, TRACK, Weather.DRY);
        double rain = RaceCalculator.calculateTime(BOLID, PILOT, ENGINEER, TRACK, Weather.RAIN);
        assertTrue(dry < rain, "Сухая трасса должна давать меньшее время, чем дождь");
    }

    @Test
    void wetIsFasterThanRain() {
        double wet = RaceCalculator.calculateTime(BOLID, PILOT, ENGINEER, TRACK, Weather.WET);
        double rain = RaceCalculator.calculateTime(BOLID, PILOT, ENGINEER, TRACK, Weather.RAIN);
        assertTrue(wet < rain, "Влажная трасса должна давать меньшее время, чем дождь");
    }

    @Test
    void allWeatherConditionsProduceDifferentTimes() {
        double dry = RaceCalculator.calculateTime(BOLID, PILOT, ENGINEER, TRACK, Weather.DRY);
        double wet = RaceCalculator.calculateTime(BOLID, PILOT, ENGINEER, TRACK, Weather.WET);
        double rain = RaceCalculator.calculateTime(BOLID, PILOT, ENGINEER, TRACK, Weather.RAIN);
        double eclipse = RaceCalculator.calculateTime(BOLID, PILOT, ENGINEER, TRACK, Weather.SOLAR_ECLIPSE);

        assertNotEquals(dry, wet, 0.001);
        assertNotEquals(dry, rain, 0.001);
        assertNotEquals(wet, rain, 0.001);
        assertNotEquals(dry, eclipse,0.001);
    }
}
