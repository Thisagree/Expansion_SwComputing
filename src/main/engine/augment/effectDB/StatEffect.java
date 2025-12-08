package main.engine.augment.effectDB;

import main.entity.Player.PlayerShip;
import main.entity.Player.PlayerShipStats;

public class StatEffect implements AugmentEffect {
    public enum StatType { ATTACK, SPEED, BULLET_SPEED, INTERVAL }

    private final StatType type;
    private final int delta;

    public StatEffect(StatType type, int delta) {
        this.type = type;
        this.delta = delta;
    }

    /**
     *  11.28.2025. Added in commit : feat : Add stat Augment effect
     *  Make stat change to the player
     *
     *  @param player
     *       the player to apply the effect to
     *
     * */
    @Override
    public void apply(PlayerShip player) {
        PlayerShipStats stats = player.getStats();
        switch (type) {
            case ATTACK -> stats.setATK((stats.getATK() + delta));

            case SPEED -> stats.addSpeed(delta);

            case BULLET_SPEED -> {
                stats.addBulletSpeed(-delta);
            }

            case INTERVAL -> {
                int predictInterval = stats.getShootingInterval() - delta;
                int amount = predictInterval <= 50 ? -(stats.getShootingInterval() - 50) : -delta;
                stats.addShootingInterval(amount);
            }
        }
    }
}
