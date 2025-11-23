package entity.Player;

import java.awt.Color;
import java.util.Set;

import engine.Cooldown;
import engine.Core;
import engine.GameState;
import engine.DrawManager.SpriteType;
import entity.Bullet;
import entity.BulletPool;
import entity.Entity;

import static engine.ItemEffect.ItemEffectType.*;

/**
 * Implements a ship, to be controlled by the player.
 *
 * @author <a href="mailto:RobertoIA1987@gmail.com">Roberto Izquierdo Amo</a>
 *
 */
public class PlayerShip extends Entity {
    // special bullet variables
    private static final int DOUBLE_SHOT_OFFSET = 6;

    /** Ship Variables **/
    private static final int DESTRUCTION_COOLDOWN = 1000;
    private PlayerShipStats stats;

    private final GameState gameState;

    /** Cooldowns */
    private final Cooldown shootingCooldown;
    private final Cooldown destructionCooldown;
    private int Y;
    private int hits;

    /**
     * Constructor, establishes the ship's properties.
     *
     * @param positionX
     *                  Initial position of the ship in the X axis.
     * @param positionY
     *                  Initial position of the ship in the Y axis.
     * @param type
     *                  Ship type (null defaults to NORMAL)
     * @param gameState
     *                  Game state reference (can be null)
     */
    public PlayerShip(final int positionX, final int positionY, final SpriteType type, final GameState gameState) {
        super(positionX, positionY, 26, 16, Color.GREEN);
        this.stats = PlayerShipLibrary.getShipList().get(type);
        this.spriteType = type;

        if(this.stats == null) {
            this.stats = PlayerShipLibrary.getShipList().get(SpriteType.Normal);
            this.spriteType = SpriteType.Normal;
        }

        this.gameState = gameState;
        this.shootingCooldown = Core.getCooldown(this.stats.getShootingInterval());
        this.destructionCooldown = Core.getCooldown(DESTRUCTION_COOLDOWN);
        this.Y = positionY;
        this.hits = 0;
    }

    public PlayerShipStats getStats() { return stats; }

    /**
     * Moves the ship speed uni ts right, or until the right screen border is reached.
     */
    public final void moveRight() { this.positionX += stats.getMoveSpeed(); }

    /**
     * Moves the ship speed units left, or until the left screen border is reached.
     */
    public final void moveLeft() { this.positionX -= stats.getMoveSpeed(); }
    public final void moveUp() { this.positionY -= stats.getMoveSpeed(); }
    public final void moveDown() { this.positionY += stats.getMoveSpeed(); }

    /**
     * Shoots a bullet based on ship type and active effects.
     *
     * @param bullets
     *                List of bullets on screen, to add the new bullet.
     * @return
     *                True if shooting was successful, false if on cooldown
     */
    public final boolean shoot(final Set<Bullet> bullets) {

        if (!this.shootingCooldown.checkFinished()) { return false; }

        this.shootingCooldown.reset();
        Core.getLogger().info("[Ship] Shooting :" + this.spriteType);

        int bulletX = positionX + this.width / 2;
        int bulletY = this.positionY - this.stats.getBulletHeight();

        if (hasTripleShotEffect()) {
            shootTripleShot(bullets, bulletX, bulletY);
            return true;
        }

        // Default shooting based on ship type
        shootBasedOnType(bullets, bulletX, bulletY);
        return true;
    }

    /**
     * Updates status of the ship.
     */
    public final void update() {
        if (!this.destructionCooldown.checkFinished())
            switch (this.spriteType) {
                case Normal -> this.spriteType = SpriteType.ShipDestroyedNormal;
                case BigShot -> this.spriteType = SpriteType.ShipDestroyedBigShot;
                case DoubleShot -> this.spriteType = SpriteType.ShipDestroyedDoubleShot;
                case MoveFast -> this.spriteType = SpriteType.ShipDestroyedMoveFast;
            }
        else
            switch (this.spriteType) {
                case ShipDestroyedNormal -> this.spriteType = SpriteType.Normal;
                case ShipDestroyedBigShot -> this.spriteType = SpriteType.BigShot;
                case ShipDestroyedDoubleShot -> this.spriteType = SpriteType.DoubleShot;
                case ShipDestroyedMoveFast -> this.spriteType = SpriteType.MoveFast;
        }
    }

    /**
     * Switches the ship to its destroyed state.
     */
    public final void destroy() {
        this.destructionCooldown.reset();
    }

    /**
     * Checks if the ship is destroyed.
     *
     * @return True if the ship is currently destroyed.
     */
    public final boolean isDestroyed() {
        return !this.destructionCooldown.checkFinished();
    }

    /**
     * Fires bullets based on ship type.
     */
    private void shootBasedOnType(final Set<Bullet> bullets, final int centerX, final int bulletY) {
        switch (this.spriteType) {
            case DoubleShot:
                addBullet(bullets, centerX - DOUBLE_SHOT_OFFSET, bulletY);
                addBullet(bullets, centerX + DOUBLE_SHOT_OFFSET, bulletY);
                break;
            case BigShot:
            case MoveFast:
            case Normal:
            default:
                addBullet(bullets, centerX, bulletY);
                break;
        }
    }

    /**
     * Creates and adds a bullet to the game.
     */
    private void addBullet(final Set<Bullet> bullets, final int x, final int y) {
        int speedMultiplier = getBulletSpeedMultiplier();
        int currentBulletSpeed = this.stats.getBulletSpeed() * speedMultiplier;

        Bullet bullet = BulletPool.getBullet(x, y, currentBulletSpeed, stats.getBulletWidth(), stats.getBulletHeight(), this.getTeam());
        bullets.add(bullet);
    }

    /** ========================= Item Effect check ========================= **/

    /**
     * Checks if player has effect active
     *
     * @return
     *                              list of active effects
     */
    private boolean hasTripleShotEffect() {
        return gameState != null && gameState.hasEffect(TRIPLESHOT);
    }

    private int getBulletSpeedMultiplier() {
        if (gameState == null) return 1;

        Integer effectValue = gameState.getEffectValue(BULLETSPEEDUP);
        if (effectValue != null) {
            Core.getLogger().info("[Ship] Item effect: Faster Bullets");
            return effectValue;
        }
        return 1;
    }

    public void addHit(){
        this.hits++;
    }

    public void resetPosition(int width, int height) {
        this.positionX = width;
        this.positionY = height;
    }

    /**
     * TRIPLESHOT effect
     */
    private void shootTripleShot(final Set<Bullet> bullets, final int centerX, final int bulletY) {
        Core.getLogger().info("[Ship] Item effect: TRIPLESHOT");
        Integer TRIPLE_SHOT_OFFSET = gameState.getEffectValue(TRIPLESHOT);

        addBullet(bullets, centerX, bulletY);
        addBullet(bullets, centerX - TRIPLE_SHOT_OFFSET, bulletY);
        addBullet(bullets, centerX + TRIPLE_SHOT_OFFSET, bulletY);
    }
}