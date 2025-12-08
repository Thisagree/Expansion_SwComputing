// main.engine/GameState.java
package main.engine;

import java.util.HashMap;
import java.util.Map;
import main.engine.ItemEffect.ItemEffectType;
import main.entity.Player.PlayerShip;

/**
 * Implements an object that stores the state of the game between levels -
 * supports 2-player co-op with shared lives.
 *
 * @author <a href="mailto:RobertoIA1987@gmail.com">Roberto Izquierdo Amo</a>
 *
 *
 */
public class GameState {

    private static final java.util.logging.Logger logger = Core.getLogger();

	/** Current game level. */
	private int level;

	/** life */
    private int score;
    private int bulletsShot;
    private int shipsDestroyed;
	/** Current coin count. */ // ADD THIS LINE
    private static int coins = 0;
    private PlayerShip playerShip;
    private int itemsCollected = 0;

    private static class EffectState {
        Cooldown cooldown;
        boolean active;
        Integer effectValue;

        EffectState() {
            this.cooldown = null;
            this.active = false;
            this.effectValue = null;
        }
    }

    /** Each player has all effect types always initialized (inactive at start). */
    private final Map<ItemEffectType, EffectState> playerEffects = new HashMap<>();

	public GameState(main.engine.DrawManager.SpriteType shipType, final int level, final int coin) {
        this.playerShip = new PlayerShip(260, 420, shipType, this);
		this.level = level;
        coins = coin;
        initializeEffectStates();
    }

	/* ---------- Constructors ---------- */

	/* Legacy 6-arg - kept for old call sites */
	/**
	 * Constructor.
	 *
	 * @param level
	 *                       Current game level.
	 * @param score
	 *                       Current score.
	 * @param bulletsShot
	 *                       Bullets shot until now.
	 * @param shipsDestroyed
	 *                       Ships destroyed until now.
	 * @param coins          // ADD THIS LINE
	 *                       Current coin count. // ADD THIS LINE
	 */
	public GameState(final PlayerShip playerShip, final int level, final int score, final int bulletsShot,
                     final int shipsDestroyed, final int coins) {
        this.playerShip = playerShip;
		this.level = level;
		this.score = score;
        this.bulletsShot = bulletsShot;
		this.shipsDestroyed = shipsDestroyed;
		GameState.coins = coins;
        this.itemsCollected = 0;
        initializeEffectStates();
    }

    public PlayerShip getPlayerShip() { return playerShip; }

    public int getScore() { return score; }

	public int getBulletsShot() {
		return bulletsShot;
	}

	public int getShipsDestroyed() {
		return shipsDestroyed;
	}
    public int getItemsCollected() {
        return itemsCollected;
    }
    public void incrementItemsCollected() {
        this.itemsCollected++;
    }

	public void addScore(final int delta) {
		int realDelta = delta;
		// If ScoreBoost item active, score gain is doubled.
        Integer multiplier = getEffectValue(ItemEffect.ItemEffectType.SCOREBOOST);
        if (multiplier != null) {
            realDelta = delta * multiplier;
            logger.info("[GameState] Player " + " ScoreBoost active (x" + multiplier + "). Score changed from " + delta + " to " + realDelta);
        }
		score += realDelta;
	}

	public void incBulletsShot() {
		bulletsShot++;
	}

	public void incShipsDestroyed() {
		shipsDestroyed++;
	}

	// 2P mode: per-player coin tracking
    public int getCoins() { return coins; } // legacy total for ScoreScreen

    public void addCoins(final int delta) {
        coins = Math.max(0, coins + delta);
    }

    public boolean spendCoins(final int amount) {
        if (coins < amount) return false;
        coins -= amount;
        return true;
    }

    public int getLevel() {
		return level;
	}

	public void nextLevel() {
		level++;
	}

    /* ---------- Item effects status methods ---------- **/

    /** Initialize all possible effects for every player (inactive). */
    private void initializeEffectStates() {
        for (ItemEffectType type : ItemEffectType.values())
            playerEffects.put(type, new EffectState());
    }

    public void addEffect(ItemEffectType type, Integer effectValue, int durationSeconds) {
        EffectState state = playerEffects.get(type);
        if (state == null) return;

        String valueStr = (effectValue != null) ? " (value: " + effectValue + ")" : "";

        if (state.active && state.cooldown != null) {
            // Extend existing effect
            state.cooldown.addTime(durationSeconds * 1000);

            state.effectValue = effectValue;

            logger.info("[GameState] Player " + " extended " + type
                    + valueStr + ") by " + durationSeconds + "s to " + state.cooldown.getDuration() );
        } else {
            // Start new effect
            state.cooldown = Core.getCooldown(durationSeconds * 1000);
            state.cooldown.reset();
            state.active = true;

            state.effectValue = effectValue;

            logger.info("[GameState] Player " + " started " + type
                    + valueStr + ") for " + durationSeconds + "s");
        }
    }

    public boolean hasEffect(ItemEffectType type) {
        EffectState state = playerEffects.get(type);
        if (state == null || !state.active) return false;

        return !state.cooldown.checkFinished();
    }

    /**
     * Gets the effect value for a specific player and effect type
     *
     * @param type
     *            Type of effect to check
     * @return
     *            Effect value if active, null otherwise
     */
    public Integer getEffectValue(ItemEffectType type) {
        EffectState state = playerEffects.get(type);
        if (state == null || !state.active) return null;

        // Check if effect is still valid (not expired)
        if (state.cooldown != null && state.cooldown.checkFinished()) {
            return null;
        }

        return state.effectValue;
    }

    /** Call this each frame to clean up expired effects */
    public void updateEffects() {
        for (Map.Entry<ItemEffectType, EffectState> entry : playerEffects.entrySet()) {
            EffectState state = entry.getValue();
            if (state.active && state.cooldown != null && state.cooldown.checkFinished()) {
                logger.info("[GameState] Player " + " effect " + entry.getKey() + " expired.");
                state.active = false;
                state.cooldown = null;  // Release reference
                state.effectValue = null;
            }
        }
    }

    /** Clear all active effects for a specific player */
    public void clearEffects() {
        // for - all effect types for this player
        for (Map.Entry<ItemEffectType, EffectState> entry : playerEffects.entrySet()) {
            // get effect state
            EffectState state = entry.getValue();
            // if state active then false
            if (state.active) {
                state.active = false;
                state.cooldown = null;
                state.effectValue = null;
            }
        }
        logger.info("[GameState] Player " + ": All effects cleared.");
    }

    /** Clear all active effects for all players */
    public void clearAllEffects() {
        clearEffects();
    }
}
