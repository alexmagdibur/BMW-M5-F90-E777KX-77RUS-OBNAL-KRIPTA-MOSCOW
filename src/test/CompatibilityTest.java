import domain.Component;
import domain.ComponentType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CompatibilityTest {

    private static Component make(int level) {
        return new Component("Компонент", ComponentType.ENGINE, 0, 50, level);
    }

    // ─── несовместимые пары ───────────────────────────────────────────────────

    @Test
    void level1AndLevel3AreIncompatible() {
        assertFalse(make(1).isCompatibleWith(make(3)),
            "Бюджетный (1) и высокий (3) несовместимы");
    }

    @Test
    void level3AndLevel1AreIncompatible() {
        assertFalse(make(3).isCompatibleWith(make(1)),
            "Несовместимость симметрична");
    }

    // ─── совместимые пары ────────────────────────────────────────────────────

    @Test
    void level1AndLevel2AreCompatible() {
        assertTrue(make(1).isCompatibleWith(make(2)));
    }

    @Test
    void level2AndLevel3AreCompatible() {
        assertTrue(make(2).isCompatibleWith(make(3)));
    }

    @Test
    void sameLevel1AreCompatible() {
        assertTrue(make(1).isCompatibleWith(make(1)));
    }

    @Test
    void sameLevel3AreCompatible() {
        assertTrue(make(3).isCompatibleWith(make(3)));
    }
}
