import domain.Bolid;
import domain.Component;
import domain.ComponentType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BolidReadyTest {

    private static Component make(ComponentType type) {
        return new Component("Компонент", type, 0, 50);
    }

    @Test
    void emptyBolidIsNotComplete() {
        assertFalse(new Bolid("Пустой").isComplete(), "Болид без компонентов не готов к гонке");
    }

    @Test
    void partialBolidIsNotComplete() {
        Bolid bolid = new Bolid("Неполный");
        bolid.installComponent(make(ComponentType.ENGINE));
        bolid.installComponent(make(ComponentType.TRANSMISSION));
        bolid.installComponent(make(ComponentType.SUSPENSION));
        // CHASSIS, AERO_PACKAGE, TIRES отсутствуют
        assertFalse(bolid.isComplete(), "Болид с частичным набором компонентов не готов к гонке");
    }

    @Test
    void bolidWithAllRequiredIsComplete() {
        Bolid bolid = new Bolid("Полный");
        bolid.installComponent(make(ComponentType.ENGINE));
        bolid.installComponent(make(ComponentType.TRANSMISSION));
        bolid.installComponent(make(ComponentType.SUSPENSION));
        bolid.installComponent(make(ComponentType.CHASSIS));
        bolid.installComponent(make(ComponentType.AERO_PACKAGE));
        bolid.installComponent(make(ComponentType.TIRES));
        assertTrue(bolid.isComplete(), "Болид со всеми обязательными компонентами готов к гонке");
    }

    @Test
    void bolidMissingOnlyTiresIsNotComplete() {
        Bolid bolid = new Bolid("Без шин");
        bolid.installComponent(make(ComponentType.ENGINE));
        bolid.installComponent(make(ComponentType.TRANSMISSION));
        bolid.installComponent(make(ComponentType.SUSPENSION));
        bolid.installComponent(make(ComponentType.CHASSIS));
        bolid.installComponent(make(ComponentType.AERO_PACKAGE));
        assertFalse(bolid.isComplete(), "Болид без шин не готов к гонке");
    }
}
