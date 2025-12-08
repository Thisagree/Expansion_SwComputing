package main.entity;

import java.awt.Color;

import main.engine.DrawManager.SpriteType;

/**
 * Implements a generic game main.entity.
 *
 * @author <a href="mailto:RobertoIA1987@gmail.com">Roberto Izquierdo Amo</a>
 *
 */
public class Entity{

    /** Position in the x-axis of the upper left corner of the main.entity. */
    protected int positionX;
    /** Position in the y-axis of the upper left corner of the main.entity. */
    protected int positionY;
    /** Width of the main.entity. */
    protected int width;
    /** Height of the main.entity. */
    protected int height;
    /** Color of the main.entity. */
    private Color color;
    /** Sprite type assigned to the main.entity. */
    protected SpriteType spriteType;

    public enum Team {
        PLAYER, ENEMY, NEUTRAL
    }

    // every main.entity knows their team - possibility for friendly-fire?
    protected Team team = Team.NEUTRAL;

    /**
     * Constructor, establishes the main.entity's generic properties.
     *
     * @param positionX
     *                  Initial position of the main.entity in the X axis.
     * @param positionY
     *                  Initial position of the main.entity in the Y axis.
     * @param width
     *                  Width of the main.entity.
     * @param height
     *                  Height of the main.entity.
     * @param color
     *                  Color of the main.entity.
     */
    public Entity(final int positionX, final int positionY, final int width, final int height, final Color color) {
        this.positionX = positionX;
        this.positionY = positionY;
        this.width = width;
        this.height = height;
        this.color = color;
    }

    public final Color getColor() {
        return color;
    }
    public final void setColor(Color color) { this.color = color; }

    public final int getPositionX() {
        return this.positionX;
    }
    public final void setPositionX(final int positionX) {
        this.positionX = positionX;
    }

    public final int getPositionY() {
        return this.positionY;
    }
    public final void setPositionY(final int positionY) {
        this.positionY = positionY;
    }

    public final SpriteType getSpriteType() {
        return this.spriteType;
    }

    public final int getWidth() {
        return this.width;
    }

    public final int getHeight() {
        return this.height;
    }

    public Team getTeam() { return team; }

    public void setTeam(Team t) { this.team = (t == null ? Team.NEUTRAL : t); }
}