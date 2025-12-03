package main.entity.Player;


import main.engine.DrawManager.SpriteType;

import java.util.HashMap;
import java.util.Map;

public class PlayerShipLibrary {
    private final static Map<SpriteType, PlayerShipStats> shipList = new HashMap<>();
    static {
        shipList.put(SpriteType.Normal,
                new PlayerShipStats(26, 16, 5, 1, 2, -6, 750, 6, 10));
        shipList.put(SpriteType.BigShot,
                new PlayerShipStats(26, 16, 3, 1, 1, -6, 750, 9, 15));
        shipList.put(SpriteType.DoubleShot,
                new PlayerShipStats(26, 16, 5, 1, 1, -6, 750, 6, 10));
        shipList.put(SpriteType.MoveFast,
                new PlayerShipStats(26, 16, 3, 1, 3, -6, 900, 6, 10));
    }

    public static Map<SpriteType, PlayerShipStats> getShipList() {
        return shipList;
    }
}
