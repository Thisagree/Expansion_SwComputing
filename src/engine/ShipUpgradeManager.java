package engine;

import engine.DrawManager.SpriteType;
import entity.PlayerShipLibrary;
import entity.PlayerShipStats;

import java.io.IOException;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Manages persistent player ship upgrades and available coins.
 */
public class ShipUpgradeManager {
    private static final int MAX_LEVEL = 10;
    private static final int MIN_LEVEL = 1;
    private static ShipUpgradeManager instance;

    private final Logger logger = Core.getLogger();
    private final Map<SpriteType, PlayerShipStats> baseStats = new EnumMap<>(SpriteType.class);
    private final Map<SpriteType, EnumMap<ShipUpgradeType, Integer>> upgradeLevels = new EnumMap<>(SpriteType.class);
    private int coins;

    private ShipUpgradeManager() {
        cacheBaseStats();
        loadFromFile();
        applyUpgradesToLibrary();
    }

    public static ShipUpgradeManager getInstance() {
        if (instance == null) {
            instance = new ShipUpgradeManager();
        }
        return instance;
    }

    private void cacheBaseStats() {
        for (Map.Entry<SpriteType, PlayerShipStats> entry : PlayerShipLibrary.getShipList().entrySet()) {
            SpriteType type = entry.getKey();
            PlayerShipStats stats = entry.getValue();
            PlayerShipStats snapshot = new PlayerShipStats(
                    stats.getShipWidth(),
                    stats.getShipHeight(),
                    stats.getHP(),
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
        ensureDefaultLevels();
    }

    public void saveToFile() {
        try {
            Core.getFileManager().saveShipUpgrades(new ShipUpgradeData(getLevelsSnapshot(), coins));
        } catch (IOException e) {
            logger.warning("Failed to save ship upgrades: " + e.getMessage());
        }
    }

    private void ensureDefaultLevels() {
        for (SpriteType type : PlayerShipLibrary.getShipList().keySet()) {
            EnumMap<ShipUpgradeType, Integer> levels = upgradeLevels.computeIfAbsent(type, k -> new EnumMap<>(ShipUpgradeType.class));
            for (ShipUpgradeType upgradeType : ShipUpgradeType.values()) {
                levels.putIfAbsent(upgradeType, MIN_LEVEL);
            }
        }
    }

    public int getCoins() {
        return coins;
    }

    public void addCoins(int delta) {
        coins = Math.max(0, coins + delta);
        saveToFile();
    }

    public int getLevel(SpriteType type, ShipUpgradeType upgradeType) {
        return upgradeLevels.getOrDefault(type, new EnumMap<>(ShipUpgradeType.class))
                .getOrDefault(upgradeType, MIN_LEVEL);
    }

    public int getUpgradeCost(SpriteType type, ShipUpgradeType upgradeType) {
        int currentLevel = getLevel(type, upgradeType);
        if (currentLevel >= MAX_LEVEL) {
            return 0;
        }
        return 50 + (currentLevel - 1) * 25;
    }

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

    public Map<SpriteType, EnumMap<ShipUpgradeType, Integer>> getLevelsSnapshot() {
        Map<SpriteType, EnumMap<ShipUpgradeType, Integer>> snapshot = new EnumMap<>(SpriteType.class);
        for (Map.Entry<SpriteType, EnumMap<ShipUpgradeType, Integer>> entry : upgradeLevels.entrySet()) {
            snapshot.put(entry.getKey(), new EnumMap<>(entry.getValue()));
        }
        return snapshot;
    }

    public EnumMap<ShipUpgradeType, Integer> getLevels(SpriteType type) {
        EnumMap<ShipUpgradeType, Integer> levels = upgradeLevels.get(type);
        return levels == null ? new EnumMap<>(ShipUpgradeType.class) : new EnumMap<>(levels);
    }

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

    public PlayerShipStats getUpgradedStats(SpriteType type) {
        return PlayerShipLibrary.getShipList().get(type);
    }

    private int totalCostForLevel(int level) {
        int total = 0;
        for (int current = MIN_LEVEL; current < level; current++) {
            total += 50 + (current - 1) * 25;
        }
        return total;
    }

    private void applyUpgradesToLibrary() {
        for (SpriteType type : PlayerShipLibrary.getShipList().keySet()) {
            applyUpgradesToLibrary(type);
        }
    }

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

        int attack = base.getATK() + (attackLevel - 1);
        int moveSpeed = base.getMoveSpeed() + (moveLevel - 1);
        int shootingInterval = Math.max(200, base.getShootingInterval() - 50 * (rateLevel - 1));
        int maxHp = base.getHP() + (hpLevel - 1);

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

    public List<SpriteType> getSupportedShips() {
        return List.of(SpriteType.Normal, SpriteType.BigShot, SpriteType.DoubleShot, SpriteType.MoveFast);
    }
}