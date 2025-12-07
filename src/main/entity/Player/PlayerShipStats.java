package main.entity.Player;


public class PlayerShipStats {
    /** Ship Variables **/
    private final int shipWidth;  // 13 * 2
    private final int shipHeight;
    /** Ship properties **/
    private final int maxHP;
    private int curHP;
    private float ATK;
    private int exp = 0;
    private float moveSpeed;
    private float bulletSpeed;
    private int shootingInterval;
    private final int bulletWidth;
    private final int bulletHeight;

    public PlayerShipStats(int shipWidth, int shipHeight,
                           int HP, float ATK, float moveSpeed, float bulletSpeed, int shootingInterval, int bulletWidth, int bulletHeight) {
        this.shipWidth = shipWidth;
        this.shipHeight = shipHeight;
        this.maxHP = HP;
        this.curHP = HP;
        this.ATK = ATK;
        this.moveSpeed = moveSpeed;
        this.bulletSpeed = bulletSpeed;
        this.shootingInterval = shootingInterval;
        this.bulletWidth = bulletWidth;
        this.bulletHeight = bulletHeight;
    }

    public int getShipWidth() { return shipWidth; }
    public int getShipHeight() { return shipHeight; }

    public int getMaxHP() { return maxHP; }
    public int getCurHP() { return curHP; }
    public void setCurHP(int HP) { this.curHP = HP; }

    public float getATK() { return ATK; }
    public void setATK(float ATK) { this.ATK = ATK; }

    public float getMoveSpeed() { return moveSpeed; }
    public void addSpeed(float delta) { moveSpeed += delta; }

    public float getBulletSpeed() { return bulletSpeed; }
    public void addBulletSpeed(float delta) { this.bulletSpeed += delta; }

    public int getShootingInterval() { return this.shootingInterval; }
    public void addShootingInterval(int delta) { this.shootingInterval += delta; }

    public int getBulletWidth() { return bulletWidth; }
    public int getBulletHeight() { return bulletHeight; }

    public void addExp(int delta) { exp += delta; }
    public int getExp() { return exp; }
    public void resetExp() { exp -= 100; }
}