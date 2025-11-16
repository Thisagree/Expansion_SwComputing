package entity;

import engine.DrawManager.SpriteType;


public class PlayerShipStats {
    /** Ship Variables **/
    private final int shipWidth;  // 13 * 2
    private final int shipHeight;
    /** Ship properties **/
    private int HP;
    private int ATK;
    private final int moveSpeed;
    private final int bulletSpeed;
    private final int shootingInterval;
    private final int bulletWidth;
    private final int bulletHeight;

    public PlayerShipStats(int shipWidth, int shipHeight,
                           int HP, int ATK, int moveSpeed, int bulletSpeed, int shootingInterval, int bulletWidth, int bulletHeight) {
        this.shipWidth = shipWidth;
        this.shipHeight = shipHeight;
        this.HP = HP;
        this.ATK = ATK;
        this.moveSpeed = moveSpeed;
        this.bulletSpeed = bulletSpeed;
        this.shootingInterval = shootingInterval;
        this.bulletWidth = bulletWidth;
        this.bulletHeight = bulletHeight;
    }

    public int getShipWidth() { return shipWidth; }
    public int getShipHeight() { return shipHeight; }

    public int getHP() { return HP; }
    public void setHP(int HP) { this.HP = HP; }
    public int getATK() { return ATK; }
    public void setATK(int ATK) { this.ATK = ATK; }

    public int getMoveSpeed() { return moveSpeed; }
    public int getBulletSpeed() { return bulletSpeed; }
    public int getShootingInterval() { return this.shootingInterval; }
    public int getBulletWidth() { return bulletWidth; }
    public int getBulletHeight() { return bulletHeight; }
}
