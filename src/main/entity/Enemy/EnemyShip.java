package main.entity.Enemy;

import java.awt.Color;
import main.engine.Cooldown;
import main.engine.Core;
import main.engine.DrawManager.SpriteType;
import main.engine.GameSettings;
import main.entity.Entity;


/**
 * Implements an enemy ship, to be destroyed by the player.
 *
 * @author <a href="mailto:RobertoIA1987@gmail.com">Roberto Izquierdo Amo</a>
 *
 */
public class EnemyShip extends Entity {
    private EnemyShipStats stats;
    /** Point value of a bonus enemy. */
    private static final int BONUS_TYPE_POINTS = 100;

    /** Number of Coins an enemy per type */
    private static final int BONUS_TYPE_COINS = 10;

    /** EXP value of a bonus enemy. */
    private static final int BONUS_TYPE_EXP = 100;

    /** Cooldown between sprite changes. */
    private Cooldown animationCooldown;
    /** Checks if the ship has been hit by a bullet. */
    private boolean isDestroyed;


    /**
     * Constructor, establishes the ship's properties.
     *
     * @param positionX
     *            Initial position of the ship in the X axis.
     * @param positionY
     *            Initial position of the ship in the Y axis.
     * @param spriteType
     *            Sprite type, image corresponding to the ship.
     */
    public EnemyShip(final int positionX, final int positionY, final SpriteType spriteType) {
        super(positionX, positionY, 12 * 2, 8 * 2, Color.WHITE);

        this.spriteType = spriteType;
        this.animationCooldown = Core.getCooldown(500);
        this.isDestroyed = false;
        EnemyShipStats template = EnemyShipLibrary.getShipList().get(this.spriteType);
        if(this.stats == null)
            this.stats = new EnemyShipStats(3,0,0,10,1, 10);
        else
            this.stats = new EnemyShipStats(template);
    }

    public void changeShip(GameSettings.ChangeData changeData) {
        stats.setHp(this.stats.getHp() * changeData.hp);
        setColor(changeData.color);
        stats.setPointValue(stats.getPointValue() * changeData.multiplier);
        stats.setExpValue(stats.getExpValue() * changeData.multiplier);
        stats.setCoinValue(stats.getCoinValue() * changeData.multiplier);
    }

    /**
     * Constructor, establishes the ship's properties for a special ship, with
     * known starting properties.
     */
    public EnemyShip() {
        super(-32, 80, 16 * 2, 7 * 2, Color.RED);
        this.spriteType = SpriteType.EnemyShipSpecial;
        this.isDestroyed = false;
        this.stats = new EnemyShipStats(1, 1, 0, BONUS_TYPE_POINTS, BONUS_TYPE_COINS, BONUS_TYPE_EXP);
    }

    /**
     * Moves the ship the specified distance.
     *
     * @param distanceX
     *            Distance to move in the X axis.
     * @param distanceY
     *            Distance to move in the Y axis.
     */
    public final void move(final int distanceX, final int distanceY) {
        this.positionX += distanceX;
        this.positionY += distanceY;
    }

    /**
     * Updates attributes, mainly used for animation purposes.
     */
    public final void update() {
        if (this.animationCooldown.checkFinished()) {
            this.animationCooldown.reset();
            switch (this.spriteType) {
                case EnemyShipA1:
                    this.spriteType = SpriteType.EnemyShipA2;
                    break;
                case EnemyShipA2:
                    this.spriteType = SpriteType.EnemyShipA1;
                    break;
                case EnemyShipB1:
                    this.spriteType = SpriteType.EnemyShipB2;
                    break;
                case EnemyShipB2:
                    this.spriteType = SpriteType.EnemyShipB1;
                    break;
                case EnemyShipC1:
                    this.spriteType = SpriteType.EnemyShipC2;
                    break;
                case EnemyShipC2:
                    this.spriteType = SpriteType.EnemyShipC1;
                    break;
                default:
                    break;
            }
        }
    }

    /** Reduces enemy health by damage and handles destruction or damage animation if health drops to 0 */
    public final void hit(float damage) {
        this.stats.setTotalDamage(this.stats.getTotalDamage() + damage);
        if (stats.getTotalDamage() >= stats.getHp()) {
            destroy();
            Color color = this.getColor();
            color = new Color(color.getRed(), color.getGreen(), color.getBlue(), 255);
            setColor(color);
        }

        else {
            switch (this.spriteType) {
                case EnemyShipA1:
                case EnemyShipA2:
                    this.spriteType = SpriteType.EnemyShipA2;
                    break;
                case EnemyShipB1:
                case EnemyShipB2:
                    this.spriteType = SpriteType.EnemyShipB2;
                    break;
                case EnemyShipC1:
                case EnemyShipC2:
                    this.spriteType = SpriteType.EnemyShipC2;
                    break;
                default:
                    break;
            }
            Color color = this.getColor();
            if(stats.getTotalDamage() != 0) {
                int alpha = (int)Math.clamp(70 + 150 * (float)stats.getHp() / stats.getTotalDamage(), 0, 255);
                color = new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
                setColor(color);
            }
        }
    }

    public final void destroy() {
        this.isDestroyed = true;
        this.spriteType = SpriteType.Explosion;
    }

    public final boolean isDestroyed() {
        return this.isDestroyed;
    }

    public EnemyShipStats getStats() { return stats; }
}