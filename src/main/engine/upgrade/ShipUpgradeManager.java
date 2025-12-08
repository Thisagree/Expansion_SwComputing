package main.engine.upgrade;

import main.engine.Core;
import main.engine.DrawManager.SpriteType;
import main.entity.Player.PlayerShipLibrary;
import main.entity.Player.PlayerShipStats;

import java.io.IOException;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Manages persistent upgrade levels and coins for player ships.
 */
public class ShipUpgradeManager {
    private static final int MAX_LEVEL = 10;
    private static final int MIN_LEVEL = 1;
    private static ShipUpgradeManager instance;

    private final Logger logger = Core.getLogger();
    /** Stores original base stats (before upgrades) for every ship type */
    private final Map<SpriteType, PlayerShipStats> baseStats = new EnumMap<>(SpriteType.class);
    /** Stores upgrade levels for each ship type and each upgrade attribute */
    private final Map<SpriteType, EnumMap<ShipUpgradeType, Integer>> upgradeLevels = new EnumMap<>(SpriteType.class);
    /** Player’s total available coins */
    private int coins;

    private ShipUpgradeManager() {
        cacheBaseStats();
        loadFromFile();
        applyUpgradesToLibrary();
    }

    /**
     * Returns the singleton instance of ShipUpgradeManager.
     *
     * @return instance
     */
    public static ShipUpgradeManager getInstance() {
        if (instance == null) {
            instance = new ShipUpgradeManager();
        }
        return instance;
    }

    /**
     * Copies all current PlayerShipLibrary base stats into a snapshot
     * so upgrades can be applied relative to the original values.
     */
    private void cacheBaseStats() {
        for (Map.Entry<SpriteType, PlayerShipStats> entry : PlayerShipLibrary.getShipList().entrySet()) {
            SpriteType type = entry.getKey();
            PlayerShipStats stats = entry.getValue();
            PlayerShipStats snapshot = new PlayerShipStats(
                    stats.getShipWidth(),
                    stats.getShipHeight(),
                    stats.getMaxHP(),
                    stats.getATK(),
                    stats.getMoveSpeed(),
                    stats.getBulletSpeed(),
                    stats.getShootingInterval(),
                    stats.getBulletWidth(),
                    stats.getBulletHeight()
            );
            baseStats.put(type, snapshot);
        }
    }

    /**
     * Loads upgrade levels and coins from files.
     * - ship_upgrades file (upgrade levels + coins)
     * - coins file
     */
    public void loadFromFile() {
        try {
            ShipUpgradeData data = Core.getFileManager().loadShipUpgrades();
            if (data != null) {
                coins = data.getCoins();
                upgradeLevels.clear();
                for (Map.Entry<SpriteType, EnumMap<ShipUpgradeType, Integer>> entry : data.getUpgradeLevels().entrySet()) {
                    upgradeLevels.put(entry.getKey(), new EnumMap<>(entry.getValue()));
                }
            }
        } catch (IOException e) {
            logger.warning("Failed to load ship upgrades. Using defaults.");
        }

        try {
            coins = Core.getFileManager().loadCoins();
        } catch (IOException e) {
            logger.warning("Failed to load saved coins. Using defaults.");
        }
        ensureDefaultLevels();
    }

    /**
     * Saves upgrade levels and coins to file.
     */
    public void saveToFile() {
        try {
            Core.getFileManager().saveShipUpgrades(new ShipUpgradeData(getLevelsSnapshot(), coins));
            Core.getFileManager().saveCoins(coins);
        } catch (IOException e) {
            logger.warning("Failed to save ship upgrades: " + e.getMessage());
        }
    }

    /**
     * Ensures all upgrade types for all ships exist and have at least MIN_LEVEL.
     */
    private void ensureDefaultLevels() {
        for (SpriteType type : PlayerShipLibrary.getShipList().keySet()) {
            EnumMap<ShipUpgradeType, Integer> levels = upgradeLevels.computeIfAbsent(type, k -> new EnumMap<>(ShipUpgradeType.class));
            for (ShipUpgradeType upgradeType : ShipUpgradeType.values()) {
                levels.putIfAbsent(upgradeType, MIN_LEVEL);
            }
        }
    }

    /**
     * @return total available coins
     */
    public int getCoins() {
        return coins;
    }

    /**
     * Sets total coins (cannot go below zero) and saves to file.
     *
     * @param coins new coin value
     */
    public void setCoins(int coins) {
        this.coins = Math.max(0, coins);
        saveToFile();
    }

    /**
     * Adds (or subtracts) coins by delta (minimum 0) and saves.
     *
     * @param delta amount to add or subtract
     */
    public void addCoins(int delta) {
        coins = Math.max(0, coins + delta);
        saveToFile();
    }

    /**
     * Returns current upgrade level for a given ship and upgrade type.
     * Defaults to MIN_LEVEL(1) if missing.
     *
     * @return upgrade level
     */
    public int getLevel(SpriteType type, ShipUpgradeType upgradeType) {
        return upgradeLevels.getOrDefault(type, new EnumMap<>(ShipUpgradeType.class))
                .getOrDefault(upgradeType, MIN_LEVEL);
    }

    /**
     * Calculates the cost to upgrade from the current level to the next level.
     * Formula: 100 * currentLevel
     * Returns 0 if at MAX_LEVEL.
     */
    public int getUpgradeCost(SpriteType type, ShipUpgradeType upgradeType) {
        int currentLevel = getLevel(type, upgradeType);
        if (currentLevel >= MAX_LEVEL) {
            return 0;
        }
        return 100 * currentLevel;
    }

    /**
     * Attempts to upgrade the given ship’s stat:
     * - Fails if already MAX_LEVEL
     * - Fails if not enough coins
     * - On success: deduct cost, increase level, re-apply upgraded stats
     *
     * @return true if upgrade succeeded
     */
    public boolean upgradeStat(SpriteType type, ShipUpgradeType upgradeType) {
        EnumMap<ShipUpgradeType, Integer> levels = upgradeLevels.get(type);
        if (levels == null) {
            return false;
        }
        int currentLevel = levels.get(upgradeType);
        if (currentLevel >= MAX_LEVEL) {
            return false;
        }
        int cost = getUpgradeCost(type, upgradeType);
        if (coins < cost) {
            return false;
        }
        coins -= cost;
        levels.put(upgradeType, currentLevel + 1);
        applyUpgradesToLibrary(type);
        return true;
    }

    /**
     * Resets all upgrade levels for the given ship to MIN_LEVEL
     * and refunds all coins previously spent.
     *
     * @return total refunded coins
     */
    public int resetShip(SpriteType type) {
        EnumMap<ShipUpgradeType, Integer> levels = upgradeLevels.get(type);
        if (levels == null) {
            return 0;
        }
        int refund = 0;
        for (Map.Entry<ShipUpgradeType, Integer> entry : levels.entrySet()) {
            int level = entry.getValue();
            refund += totalCostForLevel(level);
            entry.setValue(MIN_LEVEL);
        }
        coins += refund;
        applyUpgradesToLibrary(type);
        return refund;
    }

    /**
     * Returns a deep copy of the upgrade level map
     * (used when saving to ShipUpgradeData).
     */
    public Map<SpriteType, EnumMap<ShipUpgradeType, Integer>> getLevelsSnapshot() {
        Map<SpriteType, EnumMap<ShipUpgradeType, Integer>> snapshot = new EnumMap<>(SpriteType.class);
        for (Map.Entry<SpriteType, EnumMap<ShipUpgradeType, Integer>> entry : upgradeLevels.entrySet()) {
            snapshot.put(entry.getKey(), new EnumMap<>(entry.getValue()));
        }
        return snapshot;
    }

    /**
     * Returns a copy of the upgrade levels for a specific ship type.
     *
     * @return copied map of upgrade levels
     */
    public EnumMap<ShipUpgradeType, Integer> getLevels(SpriteType type) {
        EnumMap<ShipUpgradeType, Integer> levels = upgradeLevels.get(type);
        return levels == null ? new EnumMap<>(ShipUpgradeType.class) : new EnumMap<>(levels);
    }

    /**
     * Calculates the total refund amount if the ship were reset now.
     *
     * @return refund total
     */
    public int getRefundAmount(SpriteType type) {
        EnumMap<ShipUpgradeType, Integer> levels = upgradeLevels.get(type);
        if (levels == null) {
            return 0;
        }
        int refund = 0;
        for (int level : levels.values()) {
            refund += totalCostForLevel(level);
        }
        return refund;
    }

    /**
     * Returns the upgraded stats currently applied to the ship
     * via PlayerShipLibrary.
     */
    public PlayerShipStats getUpgradedStats(SpriteType type) {
        return PlayerShipLibrary.getShipList().get(type);
    }

    /**
     * Helper method: returns total coins spent from level 1 to the given level.
     * Sums all upgrade costs up to that level.
     */
    private int totalCostForLevel(int level) {
        int total = 0;
        for (int current = MIN_LEVEL; current < level; current++) {
            total += current * 100;
        }
        return total;
    }

    /**
     * Applies upgrade levels for ALL ship types to PlayerShipLibrary.
     * Recalculates their stats from base values.
     */
    private void applyUpgradesToLibrary() {
        for (SpriteType type : PlayerShipLibrary.getShipList().keySet()) {
            applyUpgradesToLibrary(type);
        }
    }

    /**
     * Applies upgrade levels for ONE specific ship to PlayerShipLibrary.
     * Stat adjustments:
     * - ATTACK: +0.3 per level
     * - MOVE_SPEED: +0.5 per level
     * - FIRE_RATE: shooting interval -25 per level (minimum 200)
     * - MAX_HP: +1 per level
     */
    private void applyUpgradesToLibrary(SpriteType type) {
        PlayerShipStats base = baseStats.get(type);
        if (base == null) {
            return;
        }
        EnumMap<ShipUpgradeType, Integer> levels = upgradeLevels.get(type);
        if (levels == null) {
            return;
        }

        int attackLevel = levels.get(ShipUpgradeType.ATTACK);
        int moveLevel = levels.get(ShipUpgradeType.MOVE_SPEED);
        int rateLevel = levels.get(ShipUpgradeType.FIRE_RATE);
        int hpLevel = levels.get(ShipUpgradeType.MAX_HP);

        float attack = base.getATK() + 0.3f * (attackLevel - 1);
        float moveSpeed = base.getMoveSpeed() + 0.5f * (moveLevel - 1);
        int shootingInterval = Math.max(200, base.getShootingInterval() - 25 * (rateLevel - 1));
        int maxHp = base.getMaxHP() + (hpLevel - 1);

        PlayerShipStats upgraded = new PlayerShipStats(
                base.getShipWidth(),
                base.getShipHeight(),
                maxHp,
                attack,
                moveSpeed,
                base.getBulletSpeed(),
                shootingInterval,
                base.getBulletWidth(),
                base.getBulletHeight());
        PlayerShipLibrary.getShipList().put(type, upgraded);
    }

    /**
     * Returns all ship types that support upgrades.
     */
    public List<SpriteType> getSupportedShips() {
        return List.of(SpriteType.Normal, SpriteType.BigShot, SpriteType.DoubleShot, SpriteType.MoveFast);
    }
}
