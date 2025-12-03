package main.engine.augment.effectDB;

import main.engine.DrawManager;
import main.engine.GameState;
import main.entity.Player.PlayerShip;
import main.entity.Player.PlayerShipStats;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class StatEffectTest {

    private PlayerShip player;
    private PlayerShipStats stats;

    @BeforeEach
    void setUp() {
        player = new GameState(DrawManager.SpriteType.Normal, 1, 0).getPlayerShip();
        stats = player.getStats();
    }

    @Test
    void attackIncrease() {
        float originalAtk = stats.getATK();

        StatEffect effect = new StatEffect(StatEffect.StatType.ATTACK, 2);

        effect.apply(player);

        assertEquals(originalAtk + 2, stats.getATK(),
                "Attack stat should increase by +2 after effect");
    }

    @Test
    void moveSpeedIncrease() {
        int originalSpeed = stats.getMoveSpeed();

        StatEffect effect = new StatEffect(StatEffect.StatType.SPEED, 1);
        effect.apply(player);

        assertEquals(originalSpeed + 1, stats.getMoveSpeed(),
                "Move speed stat should increase by +1 after effect");
    }

    @Test
    void bulletSpeedDecrease() {
        int originalBulletSpeed = stats.getBulletSpeed();

        StatEffect effect = new StatEffect(StatEffect.StatType.BULLET_SPEED, 3);
        effect.apply(player);

        assertEquals(originalBulletSpeed - 3, stats.getBulletSpeed(),
                "Bullet speed stat should decrease by 3 after effect");
    }


    @Test
    void shootingIntervalDecrease() {
        float originalInterval = stats.getShootingInterval();

        StatEffect effect = new StatEffect(StatEffect.StatType.INTERVAL, 200);
        effect.apply(player);

        assertTrue(stats.getShootingInterval() < originalInterval,
                "Shooting interval should decrease after interval effect");
    }

    @Test
    void shootingIntervalNotBelowZero() {
        stats.addShootingInterval(-(int) stats.getShootingInterval());
        int originalInterval = (int) stats.getShootingInterval();

        StatEffect effect = new StatEffect(StatEffect.StatType.INTERVAL, 9999);
        effect.apply(player);

        assertEquals(50, stats.getShootingInterval(),
                "When result <= 50, it should only reduce by 50");
    }

}