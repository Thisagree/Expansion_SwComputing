package main.entity.Enemy;

import main.engine.DrawManager.SpriteType;
import java.util.HashMap;
import java.util.Map;


public class EnemyShipLibrary {
    private final static Map<SpriteType, EnemyShipStats> shipList = new HashMap<>();
    public EnemyShipLibrary() {
        shipList.put(SpriteType.EnemyShipA1,
                new EnemyShipStats(2, 1, 0, 10, 2, 10));
        shipList.put(SpriteType.EnemyShipA2,
                new EnemyShipStats(2, 1, 0, 10, 2, 10));
        shipList.put(SpriteType.EnemyShipB1,
                new EnemyShipStats(1, 1, 0, 20, 3, 20));
        shipList.put(SpriteType.EnemyShipB2,
                new EnemyShipStats(1, 1, 0, 20, 3, 20));
        shipList.put(SpriteType.EnemyShipC1,
                new EnemyShipStats(1, 1, 0, 30, 5, 30));
        shipList.put(SpriteType.EnemyShipC2,
                new EnemyShipStats(1, 1, 0, 30, 5, 30));
    }

    public static Map<SpriteType, EnemyShipStats> getShipList() {
        return shipList;
    }
}
