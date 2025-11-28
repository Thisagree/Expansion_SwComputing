package engine.augment;

import engine.augment.effectDB.AugmentEffect;
import entity.Player.PlayerShip;

public record Augment(String name, String description, AugmentEffect effect) {

    /**
     * 11.28.2025. Added in commit : feat : Add stat Augment effect
     * Apply augment effect
     *
     * @param player
     *      the player to apply the effect to
     *
     * */
    public void apply(PlayerShip player) {
        effect.apply(player);

    }
}