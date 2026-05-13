import domain.Bolid;
import domain.Component;
import domain.ComponentType;
import domain.PitStop;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PitStopTest {

    private static Bolid bolidWithTires(int tireWear) {
        Bolid b = new Bolid("Тест");
        Component tires = new Component("Шины", ComponentType.TIRES, 0, 60);
        tires.setWear(tireWear);
        b.installComponent(tires);
        return b;
    }

    @Test
    void testApplyBonusReducesTireWear() {
        Bolid bolid = bolidWithTires(60);
        PitStop.applyBonus(bolid);
        int expected = (int)(60 - PitStop.WEAR_REDUCTION);
        assertEquals(expected, bolid.getComponent(ComponentType.TIRES).getWear(),
            "Износ шин должен снизиться на WEAR_REDUCTION");
    }

    @Test
    void testApplyBonusDoesNotGoBelowZero() {
        Bolid bolid = bolidWithTires(10);
        PitStop.applyBonus(bolid);
        assertEquals(0, bolid.getComponent(ComponentType.TIRES).getWear(),
            "Износ не должен уйти ниже нуля");
    }
}
