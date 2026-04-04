import domain.Team;
import service.ShopService;
import ui.ConsoleInput;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Poryadok kategoriy v openShop():
 *   ENGINE -> TRANSMISSION -> SUSPENSION -> CHASSIS -> AERO_PACKAGE -> TIRES -> EXTRA
 *
 * Vvod dlya "kupit ENGINE #1, propustit ostalnoye":
 *   "1\n0\n0\n0\n0\n0\n0\n0\n"
 *    1=vybrat item1  0=ne pokupat eshcho  0=TRANS  0=SUSP  0=CHASSIS  0=AERO  0=TIRES  0=EXTRA
 *
 * Vvod dlya "popytka kupit ENGINE (ne hvatayet), propustit ostalnoye":
 *   "1\n0\n0\n0\n0\n0\n0\n"
 *    1=vybrat item1(otkaz)  0=TRANS  0=SUSP  0=CHASSIS  0=AERO  0=TIRES  0=EXTRA
 */
class ShopServiceTest {

    private static final long INITIAL_BUDGET  = 1_000_000L;
    private static final long ENGINE_V6_PRICE = 300_000L;
    private static final long BROKE_BUDGET    = 100L;

    private static final String BUY_ENGINE_1_SKIP_REST =
            "1\n0\n0\n0\n0\n0\n0\n0\n";
    private static final String TRY_ENGINE_1_SKIP_REST =
            "1\n0\n0\n0\n0\n0\n0\n";

    private Team team;
    private ShopService shopService;
    private InputStream originalIn;

    @BeforeEach
    void setUp() {
        team        = new Team("Test", INITIAL_BUDGET);
        shopService = new ShopService(team);
        originalIn  = System.in;
    }

    @AfterEach
    void tearDown() throws Exception {
        replaceScanner(originalIn);
    }

    @Test
    void budgetIsDeductedAfterPurchase() throws Exception {
        simulateInput(BUY_ENGINE_1_SKIP_REST);
        shopService.openShop();
        assertEquals(INITIAL_BUDGET - ENGINE_V6_PRICE, team.getBudget());
    }

    @Test
    void componentGoesToInventoryAfterPurchase() throws Exception {
        simulateInput(BUY_ENGINE_1_SKIP_REST);
        shopService.openShop();

        assertEquals(1, team.getInventory().size());
        String inventoryInfo = team.getInventoryInfo();
        assertTrue(inventoryInfo.contains("V6"));
    }

    @Test
    void purchaseFailsWhenInsufficientFunds() throws Exception {
        team        = new Team("Broke", BROKE_BUDGET);
        shopService = new ShopService(team);

        simulateInput(TRY_ENGINE_1_SKIP_REST);
        shopService.openShop();

        assertEquals(BROKE_BUDGET, team.getBudget());
        assertTrue(team.getInventory().isEmpty());
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private void simulateInput(String input) throws Exception {
        ByteArrayInputStream stream =
                new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        System.setIn(stream);
        replaceScanner(stream);
    }

    private void replaceScanner(InputStream source) throws Exception {
        Field f = ConsoleInput.class.getDeclaredField("scanner");
        f.setAccessible(true);
        f.set(null, new Scanner(source));
    }
}
