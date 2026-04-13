import domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import saving.EntitySerializer;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class EntitySerializerTest {

    private EntitySerializer s;

    @BeforeEach
    void setUp() {
        s = new EntitySerializer();
    }

    // Team

    @Test
    void teamRoundTrip() {
        Team original = new Team("Ferrari", 5_000_000L);
        String line = s.serializeTeam(original);
        Team result = s.deserializeTeam(line);

        assertEquals(original.getName(), result.getName());
        assertEquals(original.getBudget(), result.getBudget());
    }

    @Test
    void teamWithSpecialCharactersInName() {
        Team team = new Team("Red Bull Racing", 9_999_999L);
        assertEquals("Red Bull Racing", s.deserializeTeam(s.serializeTeam(team)).getName());
    }

    // Component

    @Test
    void componentRoundTrip() {
        Component original = new Component("V8 Двигатель", ComponentType.ENGINE, 150_000, 85);
        original.setWear(30);

        String line = s.serializeComponent(original);
        Component result = s.deserializeComponent(line);

        assertEquals(original.getName(), result.getName());
        assertEquals(original.getType(), result.getType());
        assertEquals(original.getPrice(), result.getPrice());
        assertEquals(original.getPerformanceValue(), result.getPerformanceValue());
        assertEquals(original.getWear(), result.getWear());
    }

    @Test
    void componentWithZeroWear() {
        Component c = new Component("Шины", ComponentType.TIRES, 50_000, 60);
        assertEquals(0, s.deserializeComponent(s.serializeComponent(c)).getWear());
    }

    @Test
    void componentWithMaxWear() {
        Component c = new Component("Мотор", ComponentType.ENGINE, 0, 70);
        c.setWear(100);
        assertEquals(100, s.deserializeComponent(s.serializeComponent(c)).getWear());
    }

    // Bolid

    @Test
    void bolidRoundTripMainComponents() {
        Component engine = new Component("Двигатель", ComponentType.ENGINE, 0, 80);
        Component tires = new Component("Шины", ComponentType.TIRES, 0, 60);
        Component trans = new Component("Трансмиссия", ComponentType.TRANSMISSION, 0, 70);

        Bolid original = new Bolid("SF-24");
        original.installComponent(engine);
        original.installComponent(tires);
        original.installComponent(trans);

        List<Component> pool = List.of(engine, tires, trans);
        String line = s.serializeBolid(original);
        Bolid result = s.deserializeBolid(line, pool);

        assertEquals("SF-24", result.getName());
        assertNotNull(result.getComponent(ComponentType.ENGINE), "ENGINE должен присутствовать");
        assertNotNull(result.getComponent(ComponentType.TIRES), "TIRES должен присутствовать");
        assertNotNull(result.getComponent(ComponentType.TRANSMISSION), "TRANSMISSION должен присутствовать");
        assertEquals("Двигатель", result.getComponent(ComponentType.ENGINE).getName());
    }

    @Test
    void bolidRoundTripWithExtras() {
        Component engine = new Component("Двигатель", ComponentType.ENGINE, 0, 80);
        Component spoiler = new Component("Спойлер", ComponentType.EXTRA, 0, 10);

        Bolid original = new Bolid("RB20");
        original.installComponent(engine);
        original.addExtra(spoiler);

        List<Component> pool = List.of(engine, spoiler);
        Bolid result = s.deserializeBolid(s.serializeBolid(original), pool);

        assertEquals(1, result.getComponents().size());
        assertEquals(1, result.getExtras().size());
        assertEquals("Спойлер", result.getExtras().get(0).getName());
    }

    @Test
    void bolidEmptyComponents() {
        Bolid empty = new Bolid("Пустой болид");
        Bolid result = s.deserializeBolid(s.serializeBolid(empty), List.of());
        assertEquals("Пустой болид", result.getName());
        assertTrue(result.getComponents().isEmpty());
        assertTrue(result.getExtras().isEmpty());
    }

    // Pilot

    @Test
    void pilotRoundTrip() {
        Pilot original = new Pilot("Леклер", 500_000, 90);
        String line = s.serializePilot(original);
        Pilot result = s.deserializePilot(line);

        assertEquals(original.getName(), result.getName());
        assertEquals(original.getSalary(), result.getSalary());
        assertEquals(original.getSkill(), result.getSkill());
        assertFalse(result.isWerewolf(), "По умолчанию не оборотень");
    }

    @Test
    void pilotWerewolfFlagPreserved() {
        Pilot pilot = new Pilot("Хэмилтон", 1_000_000, 95);
        pilot.setWerewolf(true);

        Pilot result = s.deserializePilot(s.serializePilot(pilot));
        assertTrue(result.isWerewolf(), "Флаг isWerewolf должен сохраняться");
    }

    // Engineer

    @Test
    void engineerRoundTrip() {
        Engineer original = new Engineer("Браун", 300_000, 85);
        Engineer result = s.deserializeEngineer(s.serializeEngineer(original));

        assertEquals(original.getName(), result.getName());
        assertEquals(original.getSalary(), result.getSalary());
        assertEquals(original.getQualification(), result.getQualification());
        assertFalse(result.isWerewolf());
    }

    @Test
    void engineerWerewolfFlagPreserved() {
        Engineer eng = new Engineer("Тайпс", 200_000, 70);
        eng.setWerewolf(true);

        assertTrue(s.deserializeEngineer(s.serializeEngineer(eng)).isWerewolf());
    }

    // RaceResult

    @Test
    void raceResultNormalRoundTrip() {
        RaceResult original = new RaceResult("Ferrari", 95.347, true);
        original.setPosition(2);

        String line = s.serializeRaceResult(original);
        RaceResult result = s.deserializeRaceResult(line);

        assertEquals("Ferrari", result.getTeamName());
        assertEquals(95.347, result.getTime(), 1e-9);
        assertTrue(result.isPlayer());
        assertFalse(result.isIncident());
        assertEquals(2, result.getPosition());
    }

    @Test
    void raceResultDnfRoundTrip() {
        RaceResult dnf = RaceResult.dnf("RedBull", false);
        dnf.setPosition(10);

        RaceResult result = s.deserializeRaceResult(s.serializeRaceResult(dnf));

        assertEquals("RedBull", result.getTeamName());
        assertTrue(result.isIncident(), "DNF должен восстанавливаться как инцидент");
        assertFalse(result.isPlayer());
        assertEquals(10, result.getPosition());
    }

    // serialized string format smoke-tests

    @Test
    void teamSerializedLineContainsSeparator() {
        String line = s.serializeTeam(new Team("McLaren", 3_000_000L));
        assertTrue(line.contains(";"), "Строка должна содержать разделитель ;");
        assertEquals(2, line.split(";", -1).length);
    }

    @Test
    void componentSerializedLineHasFiveFields() {
        String line = s.serializeComponent(new Component("Шасси", ComponentType.CHASSIS, 80_000, 65));
        assertEquals(5, line.split(";", -1).length);
    }
}
