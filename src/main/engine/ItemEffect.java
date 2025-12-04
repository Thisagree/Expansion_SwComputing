package main.engine;

import java.util.logging.Logger;

public class ItemEffect {

    private static final Logger logger = Core.getLogger();

    public enum ItemEffectType {
        TRIPLESHOT,
        SCOREBOOST,
        BULLETSPEEDUP
    }

    /*
     * When a player picks up a duration item,
     * attempt to spend the corresponding amount of coins. If the
     * player doesn't have enough coins the effect is not applied.
     */
    private static final int COST_TRIPLESHOT = 100;
    private static final int COST_SCOREBOOST = 0;
    private static final int COST_BULLETSPEEDUP = 75;

    /*=========================SINGLE USE=================================**/

    /**
     * Applies the coin item effect to the specified player.
     *
     * @param gameState
     *            current game state instance.
     * @param coinAmount
     *            amount of coins to add.
     */
    public static void applyCoinItem(final GameState gameState, int coinAmount) {
        if (gameState == null) return;
        final int beforeCoin = gameState.getCoins();

        gameState.addCoins(coinAmount);

        logger.info("Player " + " added " + coinAmount + " coins. before : " + beforeCoin + ", after : " + gameState.getCoins());
    }

    /**
     * Applies the heal item effect to the specified player.
     *
     * @param gameState
     *            current game state instance.
     * @param lifeAmount
     *            amount of lives to add.
     */
    public static void applyHealItem(final GameState gameState, int lifeAmount) {
        if (gameState == null) return;
        final int beforeLife = gameState.getPlayerShip().getStats().getCurHP();


        if (gameState.getPlayerShip().getStats().getCurHP() + lifeAmount > 3) {
            // if adding life exceeds max, add score and coin instead
            gameState.addScore(lifeAmount * 20);
            gameState.addCoins(lifeAmount * 20);
        }
        else
            gameState.getPlayerShip().getStats().setCurHP(gameState.getPlayerShip().getStats().getCurHP() + lifeAmount);

        logger.info("Player added " + lifeAmount + " lives. before : " + beforeLife + ", after : " +
                gameState.getPlayerShip().getStats().getCurHP());
    }

    /**
     * Applies the score item effect to the specified player.
     *
     * @param gameState
     *            current game state instance.
     * @param scoreAmount
     *            amount of score to add.
     */
    public static void applyScoreItem(final GameState gameState, int scoreAmount) {
        if (gameState == null) return;
        final int beforeScore = gameState.getScore();

        gameState.addScore(scoreAmount);

        logger.info("[ItemEffect - SCORE] Player " + " : " + beforeScore + " + " + scoreAmount + " -> " + gameState.getScore());
    }

    /*========================= DURATION ITEM =================================**/

    /**
     * Attempts to spend coins for the purchase; returns true if the spend succeeded.
     * NOTE: This helper uses the existing GameState API (getCoins and addCoins).
     * It subtracts coins by calling addCoins(playerIndex, -cost). If your GameState
     * provides a dedicated "spend" or "removeCoins" method you can replace the call.
     */
    private static boolean trySpendCoins(final GameState gameState, final int cost) {
        if (gameState == null) return true;
        if (cost <= 0) return false; // free or invalid cost treated as free

        final int current = gameState.getCoins();

        // Use the dedicated spend method implemented in GameState
        if (gameState.spendCoins(cost)) {
            logger.info("Player " + " spent " + cost + " coins. before: " + current + ", after: " + gameState.getCoins());
            return false;
        } else {
            logger.info("Player " + " cannot afford cost " + cost + ". current coins: " + current);
            return true;
        }
    }
    /**
     * Applies the TripleShot timed effect to the specified player.
     * Returns true if purchase succeeded and effect applied, false if insufficient coins.
     */
    public static boolean applyTripleShot(final GameState gameState, int effectValue, int duration, Integer overrideCost) {
        if (gameState == null) return false;
        final int cost = (overrideCost != null) ? overrideCost : COST_TRIPLESHOT;

        if (trySpendCoins(gameState, cost)) {
            return false;
        }
        // apply duration
        gameState.addEffect(ItemEffectType.TRIPLESHOT, effectValue, duration);
        logger.info("[ItemEffect - TRIPLESHOT] Player "  + " applied for " + duration + "s.");
        return true;
    }

    public static boolean applyScoreBoost(final GameState gameState, int effectValue, int duration, Integer overrideCost) {
        if (gameState == null) return false;
        final int cost = (overrideCost != null) ? overrideCost : COST_SCOREBOOST;

        if (trySpendCoins(gameState, cost)) {
            return false;
        }
        // apply duration
        gameState.addEffect(ItemEffectType.SCOREBOOST, effectValue, duration);
        logger.info("[ItemEffect - SCOREBOOST] Player " + " applied for " + duration + "s. Score gain will be multiplied by " + effectValue + ".");
        return true;
    }

    /**
     * Applies the BulletSpeedUp timed effect to the specified player.
     */
    public static boolean applyBulletSpeedUp(final GameState gameState, int effectValue, int duration, Integer overrideCost) {
        if (gameState == null) return false;
        final int cost = (overrideCost != null) ? overrideCost : COST_BULLETSPEEDUP;

        if (trySpendCoins(gameState, cost))
            return false;

        // apply duration
        gameState.addEffect(ItemEffectType.BULLETSPEEDUP, effectValue, duration);
        logger.info("[ItemEffect - BULLETSPEEDUP] Player " + " applied for " + duration + "s.");
        return true;
    }
}