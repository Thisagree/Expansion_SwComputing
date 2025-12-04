package main.entity.Enemy;

public class EnemyShipStats {
    /** Current health of the enemy ship */
    private int HP;
    /** Attack point of enemy ship */
    private int ATK;
    private float totalDamage = 0;
    /** Values of the ship, in points, when destroyed. */
    private int pointValue;
    /** Values of the ship, in coins, when destroyed. */
    private int coinValue;
    /** Values of the ship, in EXP, when destroyed. */
    private int expValue;

    public EnemyShipStats(int HP, int ATK, int totalDamage, int pointValue, int coinValue, int exp) {
        this.HP = HP;
        this.ATK = ATK;
        this.totalDamage = totalDamage;
        this.pointValue = pointValue;
        this.coinValue = coinValue;
        this.expValue = exp;
    }

    public EnemyShipStats(EnemyShipStats other) {
        this.HP = other.HP;
        this.ATK = other.ATK;
        this.totalDamage = 0;
        this.pointValue = other.pointValue;
        this.coinValue = other.coinValue;
        this.expValue = other.expValue;
    }

    public int getHp() { return HP; };
    public void setHp(int hp) { this.HP = hp; }

    public int getATK() { return ATK; };
    public void setATK(int atk) { this.ATK = atk; };

    public float getTotalDamage() { return totalDamage; }
    public void setTotalDamage(float totalDamage) { this.totalDamage = totalDamage; }

    public int getPointValue() { return pointValue; }
    public void setPointValue(int pointValue) { this.pointValue = pointValue; }

    public int getCoinValue() { return coinValue; }
    public void setCoinValue(int coinValue) { this.coinValue = coinValue; }

    public int getExpValue() { return expValue; }
    public void setExpValue(int expValue) { this.expValue = expValue; }
}