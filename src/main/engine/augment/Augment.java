package main.engine.augment;

import main.engine.augment.effectDB.AugmentEffect;
import main.entity.Player.PlayerShip;

public record Augment(String name, String description, AugmentEffect effect) {


    public String getAugmentName(){ return this.name; }
    public String getAugmentDesc(){ return this.description; }

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