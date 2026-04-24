import domain.EmergencyKit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import saving.EntitySerializer;

import static org.junit.jupiter.api.Assertions.*;

public class EmergencyKitSerializerTest {

    private EntitySerializer s;

    @BeforeEach
    void setUp() {
        s = new EntitySerializer();
    }

    // round-trip

    @Test
    void allTrueRoundTrip() {
        EmergencyKit kit = new EmergencyKit(true, true, true);
        EmergencyKit result = s.deserializeEmergencyKit(s.serializeEmergencyKit(kit));

        assertTrue(result.hasFirstAidKit());
        assertTrue(result.hasFireExtinguisher());
        assertTrue(result.hasWarningTriangle());
    }

    @Test
    void allFalseRoundTrip() {
        EmergencyKit kit = new EmergencyKit(false, false, false);
        EmergencyKit result = s.deserializeEmergencyKit(s.serializeEmergencyKit(kit));

        assertFalse(result.hasFirstAidKit());
        assertFalse(result.hasFireExtinguisher());
        assertFalse(result.hasWarningTriangle());
    }

    @Test
    void partialKitRoundTrip() {
        EmergencyKit kit = new EmergencyKit(true, false, true);
        EmergencyKit result = s.deserializeEmergencyKit(s.serializeEmergencyKit(kit));

        assertTrue(result.hasFirstAidKit());
        assertFalse(result.hasFireExtinguisher());
        assertTrue(result.hasWarningTriangle());
    }

    @Test
    void eachItemPreservedIndependently() {
        // аптечка есть, остальных нет
        EmergencyKit onlyAid = new EmergencyKit(true, false, false);
        EmergencyKit r1 = s.deserializeEmergencyKit(s.serializeEmergencyKit(onlyAid));
        assertTrue(r1.hasFirstAidKit());
        assertFalse(r1.hasFireExtinguisher());
        assertFalse(r1.hasWarningTriangle());

        // только огнетушитель
        EmergencyKit onlyFire = new EmergencyKit(false, true, false);
        EmergencyKit r2 = s.deserializeEmergencyKit(s.serializeEmergencyKit(onlyFire));
        assertFalse(r2.hasFirstAidKit());
        assertTrue(r2.hasFireExtinguisher());
        assertFalse(r2.hasWarningTriangle());

        // только знак
        EmergencyKit onlyTriangle = new EmergencyKit(false, false, true);
        EmergencyKit r3 = s.deserializeEmergencyKit(s.serializeEmergencyKit(onlyTriangle));
        assertFalse(r3.hasFirstAidKit());
        assertFalse(r3.hasFireExtinguisher());
        assertTrue(r3.hasWarningTriangle());
    }

    // формат строки

    @Test
    void serializedLineHasThreeFields() {
        String line = s.serializeEmergencyKit(new EmergencyKit(true, false, true));
        assertEquals(3, line.split(";", -1).length, "Строка должна содержать ровно 3 поля");
    }

    @Test
    void serializedLineContainsSeparator() {
        String line = s.serializeEmergencyKit(new EmergencyKit(false, false, false));
        assertTrue(line.contains(";"), "Строка должна содержать разделитель ;");
    }

    @Test
    void serializedCompleteKitContainsOnlyTrue() {
        String line = s.serializeEmergencyKit(new EmergencyKit(true, true, true));
        assertFalse(line.contains("false"), "Полный набор не должен содержать false");
    }

    @Test
    void serializedEmptyKitContainsOnlyFalse() {
        String line = s.serializeEmergencyKit(new EmergencyKit(false, false, false));
        assertFalse(line.contains("true"), "Пустой набор не должен содержать true");
    }
}
