package main.engine;

import java.io.IOException;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import main.engine.DrawManager.SpriteType;
import main.engine.upgrade.ShipUpgradeManager;
import main.screen.GameScreen;
import main.screen.ScoreScreen;

/**
 * Entry point for quickly starting a boss battle using the Normal ship with
 * applied upgrades from {@code ShipUpgrade.csv}.
 */
public final class TestBoss {

    private static final int WIDTH = 448;
    private static final int HEIGHT = 520;
    private static final int FPS = 60;

    private static final Logger LOGGER = Core.getLogger();
    private static Handler fileHandler;
    private static ConsoleHandler consoleHandler;

    private TestBoss() {
    }

    public static void main(final String[] args) throws IOException {
        configureLogger();

        // Load and apply ship upgrades from ShipUpgrade.csv before creating the player ship.
        ShipUpgradeManager.getInstance();

        Frame frame = new Frame(WIDTH, HEIGHT);
        InputManager input = InputManager.getInstance();
        frame.addKeyListener(input);
        DrawManager.getInstance().setFrame(frame);

        List<GameSettings> gameSettings = GameSettings.getGameSettings();
        int bossLevelIndex = Math.min(4, gameSettings.size() - 1); // Level 5 settings or fallback to last
        GameSettings bossSettings = gameSettings.get(bossLevelIndex);

        AchievementManager achievementManager = new AchievementManager();
        GameState gameState = new GameState(SpriteType.Normal, 5, 0);
        GameScreen gameScreen = new GameScreen(
                gameState,
                bossSettings,
                false,
                frame.getWidth(),
                frame.getHeight(),
                FPS,
                achievementManager);

        LOGGER.info("Launching boss battle test.");
        frame.setScreen(gameScreen);


        ScoreScreen scoreScreen = new ScoreScreen(
                frame.getWidth(),
                frame.getHeight(),
                FPS,
                gameScreen.getGameState(),
                achievementManager);
        LOGGER.info("Launching score screen after boss battle.");
        frame.setScreen(scoreScreen);

        closeLogger();
        System.exit(0);
    }

    private static void configureLogger() {
        try {
            LOGGER.setUseParentHandlers(false);
            fileHandler = new FileHandler("log");
            fileHandler.setFormatter(new MinimalFormatter());
            consoleHandler = new ConsoleHandler();
            consoleHandler.setFormatter(new MinimalFormatter());
            LOGGER.addHandler(fileHandler);
            LOGGER.addHandler(consoleHandler);
            LOGGER.setLevel(Level.ALL);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void closeLogger() {
        try {
            if (fileHandler != null) {
                fileHandler.flush();
                fileHandler.close();
            }
            if (consoleHandler != null) {
                consoleHandler.flush();
                consoleHandler.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}