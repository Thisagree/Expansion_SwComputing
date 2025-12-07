package test.main.entity.Player;

import main.entity.Player.PlayerShipStats;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlayerShipStatsTest {

    @Test
    void testConstructorAndGetters() {
        PlayerShipStats stats = new PlayerShipStats(
                26, 40,
                100, 20f,
                5f, 10f, 300,
                4, 8
        );

        assertEquals(26, stats.getShipWidth());
        assertEquals(40, stats.getShipHeight());
        assertEquals(100, stats.getMaxHP());
        assertEquals(100, stats.getCurHP());
        assertEquals(20f, stats.getATK());
        assertEquals(5f, stats.getMoveSpeed());
        assertEquals(10f, stats.getBulletSpeed());
        assertEquals(300, stats.getShootingInterval());
        assertEquals(4, stats.getBulletWidth());
        assertEquals(8, stats.getBulletHeight());
        assertEquals(0, stats.getExp());
    }

    @Test
    void testSettersAndAdders() {
        PlayerShipStats stats = new PlayerShipStats(
                10, 20,
                50, 5f,
                3f, 7f, 200,
                2, 3
        );

        stats.setCurHP(40);
        stats.setATK(12f);
        stats.addSpeed(2f);
        stats.addBulletSpeed(3f);
        stats.addShootingInterval(-50);

        assertEquals(40, stats.getCurHP());
        assertEquals(12f, stats.getATK());
        assertEquals(5f, stats.getMoveSpeed());
        assertEquals(10f, stats.getBulletSpeed());
        assertEquals(150, stats.getShootingInterval());
    }

    @Test
    void testExpSystem() {
        PlayerShipStats stats = new PlayerShipStats(
                10, 20,
                50, 5f,
                3f, 7f, 200,
                2, 3
        );

        stats.addExp(30);
        assertEquals(30, stats.getExp());

        stats.addExp(80);
        assertEquals(110, stats.getExp());

        stats.resetExp();  // exp -= 100
        assertEquals(10, stats.getExp());
    }
}
