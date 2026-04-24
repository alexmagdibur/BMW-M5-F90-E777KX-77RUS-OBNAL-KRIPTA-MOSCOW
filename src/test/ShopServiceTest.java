import domain.Team;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import service.ShopService;
import ui.ConsoleInput;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ShopServiceTest {

    private final InputStream originalIn = System.in;

    private void mockInput(String input) {
        System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
        ConsoleInput.resetScanner();
    }

    @AfterEach
    void tearDown() {
        System.setIn(originalIn);
        ConsoleInput.resetScanner();
    }

    @Test
    void budgetDeductedAfterPurchase() {
        // 7 категорий: купить 1й ENGINE (1,0), пропустить остальные 6 (0x6), пропустить набор (0)
        mockInput("1\n0\n0\n0\n0\n0\n0\n0\n0\n");

        Team team = new Team("Тест", 1_000_000);
        new ShopService(team).openShop();

        assertEquals(700_000, team.getBudget());
    }

    @Test
    void componentAddedToInventoryAfterPurchase() {
        mockInput("1\n0\n0\n0\n0\n0\n0\n0\n0\n");

        Team team = new Team("Тест", 1_000_000);
        int inventoryBefore = team.getInventory().size();

        new ShopService(team).openShop();

        assertEquals(inventoryBefore + 1, team.getInventory().size());
    }

    @Test
    void purchaseFailsWhenNotEnoughMoney() {
        mockInput("1\n0\n0\n0\n0\n0\n0\n0\n");

        Team team = new Team("Тест", 100_000);
        int budgetBefore = Math.toIntExact(team.getBudget());
        int inventoryBefore = team.getInventory().size();

        new ShopService(team).openShop();

        assertEquals(budgetBefore, team.getBudget());
        assertEquals(inventoryBefore, team.getInventory().size());
    }
}