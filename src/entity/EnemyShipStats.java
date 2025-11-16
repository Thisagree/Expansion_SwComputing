package entity;

public class EnemyShipStats {
    /** Current health of the enemy ship */
    private int HP;
    /** Attack point of enemy ship */
    private int ATK;
    private int totalDamage = 0;
    /** Values of the ship, in points, when destroyed. */
    private int pointValue;
    /** Values of the ship, in coins, when destroyed. */
    private int coinValue;

    public EnemyShipStats(int HP, int ATK, int totalDamage, int pointValue, int coinValue) {
        this.HP = HP;
        this.ATK = ATK;
        this.totalDamage = totalDamage;
        this.pointValue = pointValue;
        this.coinValue = coinValue;
    }

    public int getHp() { return HP; };
    void setHp(int hp) { this.HP = hp; }

    public int getATK() { return ATK; };
    public void setATK(int atk) { this.ATK = atk; };

    public int getTotalDamage() { return totalDamage; }
    public void setTotalDamage(int totalDamage) { this.totalDamage = totalDamage; }

    public int getPointValue() { return pointValue; }
    public void setPointValue(int pointValue) { this.pointValue = pointValue; }

    public int getCoinValue() { return coinValue; }
    public void setCoinValue(int coinValue) { this.coinValue = coinValue; }
}
