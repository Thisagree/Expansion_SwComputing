package main.entity.Enemy;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EnemyShipStatsTest {
    @Test
    void testConstructorAndGetters() {
        EnemyShipStats stats = new EnemyShipStats(100, 20, 5, 10, 15, 50);

        assertEquals(100, stats.getHp());
        assertEquals(20, stats.getATK());
        assertEquals(5f, stats.getTotalDamage());
        assertEquals(10, stats.getPointValue());
        assertEquals(15, stats.getCoinValue());
        assertEquals(50, stats.getExpValue());
    }

    @Test
    void testCopyConstructor() {
        EnemyShipStats original = new EnemyShipStats(100, 20, 7, 10, 15, 50);
        EnemyShipStats copy = new EnemyShipStats(original);

        assertEquals(original.getHp(), copy.getHp());
        assertEquals(original.getATK(), copy.getATK());
        assertEquals(0f, copy.getTotalDamage()); // reset
        assertEquals(original.getPointValue(), copy.getPointValue());
        assertEquals(original.getCoinValue(), copy.getCoinValue());
        assertEquals(original.getExpValue(), copy.getExpValue());
    }

    @Test
    void testSetters() {
        EnemyShipStats stats = new EnemyShipStats(100, 20, 0, 10, 15, 50);

        stats.setHp(120);
        stats.setATK(30);
        stats.setTotalDamage(12.5f);
        stats.setPointValue(40);
        stats.setCoinValue(60);
        stats.setExpValue(99);

        assertEquals(120, stats.getHp());
        assertEquals(30, stats.getATK());
        assertEquals(12.5f, stats.getTotalDamage());
        assertEquals(40, stats.getPointValue());
        assertEquals(60, stats.getCoinValue());
        assertEquals(99, stats.getExpValue());
    }
}