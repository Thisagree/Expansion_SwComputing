package main.entity;

import java.awt.Color;
import java.util.Set;

import main.engine.Cooldown;
import main.engine.Core;
import main.engine.DrawManager.SpriteType;
import main.screen.Screen;
import main.entity.Enemy.*;

/**
 * Boss enemy with multi-phase pattern based on HP.
 */
public class Boss extends Entity {

    private EnemyShipStats stats;
    private Screen screen;

    private boolean isDestroyed = false;

    private Cooldown animationCooldown;
    private Cooldown shootCooldown;

    private double movePhase = 0;


    public Boss(int positionX, int positionY) {
        super(positionX, positionY, 12 * 6, 8 * 6, Color.RED);

        this.spriteType = SpriteType.EnemyShipSpecial;

        this.stats = new EnemyShipStats(
                100,    // HP
                2,      // Attack
                0,      // totalDamage
                500,    // pointValue
                20,     // coinValue
                50      // exp
        );

        this.animationCooldown = Core.getCooldown(400);
        this.shootCooldown     = Core.getCooldown(600);
    }

    /** Update boss based on HP phases */
    public void update(Set<Bullet> bullets) {

        int hp = stats.getHp();

        if (hp > 60) {
            // PHASE 1: Linear movement + straight shot
            patternPhase1(bullets);
        }
        else if (hp > 30) {
            // PHASE 2: Infinity movement + 5-way
            patternPhase2(bullets);
        }
        else {
            // PHASE 3: Infinity movement + 5-way + danger zone straight shot
            patternPhase3(bullets);
        }

        animateColor();
    }

    /** Color animation */
    private void animateColor() {
        if (animationCooldown.checkFinished()) {
            animationCooldown.reset();
            this.setColor(new Color(
                    (int)(155 + Math.random() * 100),
                    (int)(20 + Math.random() * 30),
                    (int)(20 + Math.random() * 30)
            ));
        }
    }

    /* -------------------------------
     *         PHASE 1
     *   Straight move + straight bullets
     * ------------------------------- */
    private int phase1Speed = 2;
    private void patternPhase1(Set<Bullet> bullets) {
        this.positionX += phase1Speed;

        if (this.positionX < 20) {
            this.positionX = 20;
            phase1Speed = -phase1Speed;
        } else if (this.positionX > 350) {
            this.positionX = 350;
            phase1Speed = -phase1Speed;
        }

        shootStraight(bullets);
    }

    private void shootStraight(Set<Bullet> bullets) {
        if (!shootCooldown.checkFinished()) return;
        shootCooldown.reset();

        int cx = this.getPositionX() + this.getWidth() / 2;
        int cy = this.getPositionY() + this.getHeight();

        Bullet b = BulletPool.getBullet(
                cx, cy,
                0, 6,     // straight downward
                4, 10,
                Entity.Team.ENEMY
        );
        bullets.add(b);
    }

    /* -------------------------------
     *         PHASE 2
     *    Infinity move + 5-way bullets
     * ------------------------------- */
    private void patternPhase2(Set<Bullet> bullets) {
        movePattern();
        shootSpread(bullets);
    }

    private void shootSpread(Set<Bullet> bullets) {
        if (!shootCooldown.checkFinished()) return;
        shootCooldown.reset();

        int cx = this.getPositionX() + this.getWidth() / 2;
        int cy = this.getPositionY() + this.getHeight();

        int[] dx = { -3, -1, 0, 1, 3 };

        for (int sx : dx) {
            bullets.add(BulletPool.getBullet(
                    cx, cy,
                    sx, 5,
                    4, 8,
                    Entity.Team.ENEMY
            ));
        }
    }

    /* -------------------------------
     *         PHASE 3
     *   Infinity + 5-way
     *   + Danger zone mark + straight bullets
     * ------------------------------- */
    private void patternPhase3(Set<Bullet> bullets) {
        movePattern();

        if (!shootCooldown.checkFinished()) return;
        shootCooldown.reset();

        int cx = this.getPositionX() + this.getWidth() / 2;
        int cy = this.getPositionY() + this.getHeight();

        // 5-way bullets
        int[] dx = { -3, -1, 0, 1, 3 };
        for (int sx : dx) {
            bullets.add(BulletPool.getBullet(cx, cy, sx, 5, 4, 8, Entity.Team.ENEMY));
        }

        int verticalSpacing = 15;
        for (int i = 0; i < 5; i++) {
            bullets.add(BulletPool.getBullet(cx, cy + i * verticalSpacing, 0, 6, 4, 10, Entity.Team.ENEMY));
        }
    }

    /** Infinity movement */
    private void movePattern() {
        movePhase += 0.03;
        double t = movePhase;

        int centerX = 240;
        int centerY = 80;

        int ampX = 140;
        int ampY = 50;

        double offsetX = ampX * Math.sin(t);
        double offsetY = ampY * Math.sin(t) * Math.cos(t);

        this.positionX = centerX + (int)offsetX - this.width / 2;
        this.positionY = centerY + (int)offsetY;
    }

    /* -------------------------------
     * Hit & Destroy
     * ------------------------------- */

    public void hit(int damage) {
        stats.setHp(stats.getHp() - damage);
        stats.setTotalDamage(stats.getTotalDamage() + damage);

        if (stats.getHp() <= 0) {
            destroy();
        } else {
            float ratio = (float)stats.getHp() / 100f;
            this.setColor(new Color((int)(255 * ratio), 60, 60));
        }
    }

    public void destroy() {
        this.isDestroyed = true;
        this.spriteType = SpriteType.Explosion;
        this.setColor(new Color(255, 50, 50));
    }

    public boolean isDestroyed() {
        return this.isDestroyed;
    }

    public EnemyShipStats getStats() {
        return stats;
    }
}