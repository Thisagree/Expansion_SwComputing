package main.entity;

import java.awt.Color;

import main.engine.DrawManager.SpriteType;

/**
 * Implements a bullet that moves vertically up or down.
 *
 * @author <a href="mailto:RobertoIA1987@gmail.com">Roberto Izquierdo Amo</a>
 *
 */
public class Bullet extends Entity {
    /**
     * Speed of the bullet, positive or negative depending on direction -
     * positive is down.
     */
    private int speed;

    private int speedX = 0;
    private int speedY = 0;

    /**
     * Constructor, establishes the bullet's properties.
     *
     * @param positionX
     *                  Initial position of the bullet in the X axis.
     * @param positionY
     *                  Initial position of the bullet in the Y axis.
     * @param speed
     *                  Speed of the bullet, positive or negative depending on
     *                  direction - positive is down.
     */
    // change the constructor to receive width and height
    public Bullet(final int positionX, final int positionY, final int width, final int height, final int speed) {
        super(positionX, positionY,0,0 , Color.WHITE);
        this.speed = speed;
    }

    public Bullet(final int positionX, final int positionY,
                  final int width, final int height,
                  final int speedX, final int speedY) {
        super(positionX, positionY, width, height, Color.WHITE);
        this.speedX = speedX;
        this.speedY = speedY;
    }

    // reset the size when recycling bullets
    public final void setSize(final int width, final int height) {
        this.width = width;
        this.height = height;
    }

    /**
     * Sets correct sprite for the bullet, based on speed.
     */

    public final void setSprite() {
        if (this.speedY < 0) {
            this.spriteType = SpriteType.Bullet; // player bullet fired, team remains NEUTRAL
        } else {
            this.spriteType = SpriteType.EnemyBullet; // enemy fired bullet
        }
    }

    /**
     * Updates the bullet's position.
     */
    public final void update() {
        this.positionY += this.speed;
        this.positionX += this.speedX;
        this.positionY += this.speedY;
    }

    /**
     * Setter of the speed of the bullet.
     *
     * @param speed
     *              New speed of the bullet.
     */
    public final void setSpeed(final int speed) {
        this.speed = speed;
    }

    /**
     * Getter for the speed of the bullet.
     *
     * @return Speed of the bullet.
     */
    public final int getSpeed() {
        return this.speed;
    }

    public void setSpeedX(int sx) { this.speedX = sx; }
    public void setSpeedY(int sy) { this.speedY = sy; }

    public int getSpeedX() { return speedX; }
    public int getSpeedY() { return speedY; }
}