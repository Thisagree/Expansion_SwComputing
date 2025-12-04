package main.engine;

import main.engine.DrawManager.SpriteType;
import java.util.EnumMap;
import java.util.Map;

/**
 * Serializable structure holding ship upgrade data and available coins.
 */
public class ShipUpgradeData {
    private final Map<SpriteType, EnumMap<ShipUpgradeType, Integer>> upgradeLevels;
    private final int coins;

    public ShipUpgradeData(Map<SpriteType, EnumMap<ShipUpgradeType, Integer>> upgradeLevels, int coins) {
        this.upgradeLevels = upgradeLevels;
        this.coins = coins;
    }

    public Map<SpriteType, EnumMap<ShipUpgradeType, Integer>> getUpgradeLevels() {
        return upgradeLevels;
    }

    public int getCoins() {
        return coins;
    }
}