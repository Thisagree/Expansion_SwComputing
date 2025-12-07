package test.main.engine.ShipUpgradeManagerTest;

import main.engine.DrawManager.SpriteType;
import main.engine.ShipUpgradeManager;
import main.engine.ShipUpgradeType;
import main.entity.Player.PlayerShipLibrary;
import main.entity.Player.PlayerShipStats;
import org.junit.jupiter.api.*;

import java.io.*;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

public class ShipUpgradeManagerTest {

    private static final File RES_DIR = new File("res");
    private static final File COINS_FILE = new File("res/coins.csv");
    private static final File UPGRADE_FILE = new File("res/ShipUpgrade.csv");

    @BeforeEach
    public void setup() throws Exception {

        // Create res/ directory if it does not exist
        if (!RES_DIR.exists()) RES_DIR.mkdir();

        // Create initial coins.csv for testing
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(COINS_FILE), StandardCharsets.UTF_8))) {
            writer.write("coins,0");
        }

        // Create default ShipUpgrade.csv for testing
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(UPGRADE_FILE), StandardCharsets.UTF_8))) {
            writer.write("ShipType,ATTACK,MOVE_SPEED,FIRE_RATE,MAX_HP\n");
            writer.write("Normal,1,1,1,1\n");
            writer.write("BigShot,1,1,1,1\n");
            writer.write("DoubleShot,1,1,1,1\n");
            writer.write("MoveFast,1,1,1,1\n");
        }

        // Initialize ship library (clear previous test data)
        PlayerShipLibrary.getShipList().clear();
        PlayerShipLibrary.getShipList().put(
                SpriteType.Normal,
                new PlayerShipStats(32, 32, 10, 3, 5, 7, 500, 4, 4)
        );

        // Reset singleton instance of ShipUpgradeManager
        var instanceField = ShipUpgradeManager.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, null);
    }

    @AfterEach
    public void cleanup() {
        // Delete temporary test files
        COINS_FILE.delete();
        UPGRADE_FILE.delete();
    }


    // ---------------------------------------------------
    //                    TEST CASES
    // ---------------------------------------------------

    @Test
    public void testDefaultLevelLoad() {
        // The default upgrade level for all types should be 1
        ShipUpgradeManager manager = ShipUpgradeManager.getInstance();
        assertEquals(1, manager.getLevel(SpriteType.Normal, ShipUpgradeType.ATTACK));
    }

    @Test
    public void testAddCoins() {
        // Adding coins should properly update the balance
        ShipUpgradeManager manager = ShipUpgradeManager.getInstance();
        manager.addCoins(150);
        assertEquals(150, manager.getCoins());
    }

    @Test
    public void testUpgradeStat() {
        // Upgrading a ship stat should increase the level and modify the PlayerShipStats
        ShipUpgradeManager manager = ShipUpgradeManager.getInstance();
        manager.setCoins(200);

        boolean ok = manager.upgradeStat(SpriteType.Normal, ShipUpgradeType.ATTACK);
        assertTrue(ok);

        // Level should increase
        assertEquals(2, manager.getLevel(SpriteType.Normal, ShipUpgradeType.ATTACK));

        // ATK stat should increase
        PlayerShipStats upgraded = manager.getUpgradedStats(SpriteType.Normal);
        assertEquals(4, upgraded.getATK()); // base attack 3 → level 2 gives +1
    }

    @Test
    public void testUpgradeFail_NoCoins() {
        // Upgrade should fail when there are no coins
        ShipUpgradeManager manager = ShipUpgradeManager.getInstance();
        manager.setCoins(0);

        assertFalse(manager.upgradeStat(SpriteType.Normal, ShipUpgradeType.ATTACK));
        assertEquals(1, manager.getLevel(SpriteType.Normal, ShipUpgradeType.ATTACK));
    }

    @Test
    public void testResetRefund() {
        // Resetting upgrades should refund coins based on accumulated upgrade cost
        ShipUpgradeManager manager = ShipUpgradeManager.getInstance();
        manager.setCoins(500);

        // Perform two upgrades
        manager.upgradeStat(SpriteType.Normal, ShipUpgradeType.ATTACK);
        manager.upgradeStat(SpriteType.Normal, ShipUpgradeType.ATTACK);

        int before = manager.getCoins();
        int refund = manager.resetShip(SpriteType.Normal);
        int after = manager.getCoins();

        // After reset: coins should increase by refunded value
        assertEquals(before + refund, after);

        // Level should return to 1
        assertEquals(1, manager.getLevel(SpriteType.Normal, ShipUpgradeType.ATTACK));
    }
}
