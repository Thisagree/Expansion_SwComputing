package screen;

import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.*;

import engine.*;

/**
 * Implements the score screen.
 *
 * @author <a href="mailto:RobertoIA1987@gmail.com">Roberto Izquierdo Amo</a>
 *
 */
public class ScoreScreen extends Screen {

    /** Milliseconds between changes in user selection. */
    private static final int SELECTION_TIME = 200;
    /** Code of max high score. */
    private static final int MAX_HIGH_SCORE_NUM = 7;
    /** Maximum name length. */
    private static final int MAX_NAME_LENGTH = 5;

    // Added for persist per-player breakdown
    private final GameState gameState;

    /** Current score. */
    private final int score;
    /** Player lives left. */
    private final int livesRemaining;
    /** Current coins. */
    private final int coins;
    /** Total bullets shot by the player. */
    private final int bulletsShot;
    /** Total ships destroyed by the player. */
    private final int shipsDestroyed;
    /** List of past high scores. */
    private List<Score> highScores;
    /** Checks if current score is a new high score. */
    private boolean isNewRecord;
    /** Player name for record input. */
    private StringBuilder name;
    /** Make sure the name is less than 3 characters. */
    private boolean showNameError = false;
    /** Time between changes in user selection. */
    private final Cooldown selectionCooldown;
    /** manages achievements.*/
    private final AchievementManager achievementManager;
    /** Total coins earned in the game. */
    private final int totalCoin;

    /**
     * Constructor, establishes the properties of the screen.
     *
     * @param width
     *                  Screen width.
     * @param height
     *                  Screen height.
     * @param fps
     *                  Frames per second, frame rate at which the game is run.
     * @param gameState
     *                  Current game state.
     * @param achievementManager
     * 			            Achievement manager instance used to track and save player achievements.
     * 			  2025-10-03  add generator parameter and comment
     */
    public ScoreScreen(final int width, final int height, final int fps,
                       final GameState gameState, final AchievementManager achievementManager) {
        super(width, height, fps);
        this.gameState = gameState; // Added

        this.score = gameState.getScore();
        this.livesRemaining = gameState.getPlayerShip().getStats().getHP();
        this.coins = gameState.getCoins();
        this.name = new StringBuilder();
        this.bulletsShot = gameState.getBulletsShot();
        this.shipsDestroyed = gameState.getShipsDestroyed();
        this.totalCoin = gameState.getCoins(); // ADD THIS LINE
        this.isNewRecord = false;
        this.name = new StringBuilder();
        this.selectionCooldown = Core.getCooldown(SELECTION_TIME);
        this.selectionCooldown.reset();
        this.achievementManager = achievementManager;

        try {
            this.highScores = Core.getFileManager().loadHighScores();
            if (highScores.size() < MAX_HIGH_SCORE_NUM
                    || highScores.getLast().getScore() < this.score)
                this.isNewRecord = true;

        } catch (IOException e) {
            logger.warning("Couldn't load high scores!");
        }
        // clear last key
        inputManager.clearLastKey();
    }

    /**
     * Starts the action.
     *
     * @return Next screen code.
     */
    public final int run() {
        super.run();

        return this.returnCode;
    }

    /**
     * Updates the elements on screen and checks for events.
     */
    protected final void update() {
        super.update();

		draw();
		if (this.inputDelay.checkFinished()) {
			if (inputManager.isKeyDown(KeyEvent.VK_ESCAPE)) {
                // Return to main menu.
                SoundManager.playOnce("sound/select.wav");
				this.returnCode = 1;
				this.isRunning = false;
				if (this.isNewRecord) {
					saveScore();
					saveAchievement(); //2025-10-03 call method for save achievement released
				}
			} else if (inputManager.isKeyDown(KeyEvent.VK_SPACE)) {
                // name too short -> return
                if (this.name.length() < 3) return;
				// Play again.
                SoundManager.playOnce("sound/select.wav");
				this.returnCode = 2;
				this.isRunning = false;
				if (this.isNewRecord) {
					saveScore();
					saveAchievement(); // 2025-10-03 call method for save achievement released
				}
			}

			// Handle backspace
			if (inputManager.isKeyDown(KeyEvent.VK_BACK_SPACE)
					&& this.selectionCooldown.checkFinished()) {
				if (!this.name.isEmpty()) {
					this.name.deleteCharAt(this.name.length() - 1);
					this.selectionCooldown.reset();
				}
			}

			// Handle character input
			char typedChar = inputManager.getLastCharTyped();
			if (typedChar != '\0') {
				// Checks the name is not short when you press the space bar
				if (typedChar == ' ') {
					if (this.name.length() < 3) {
						// System.out.println("too short!!");
						this.showNameError = true;
					}
				}

				// Check if it's a valid character (alphanumeric only)
				else if ((Character.isLetterOrDigit(typedChar))
						&& this.name.length() < MAX_NAME_LENGTH) {
					this.name.append(Character.toUpperCase(typedChar));

				}
			}
		}

    }

    /**
     * Saves the score as a high score.
     * 2025-10-18
     * Add ability that distinguish duplicate names and save higher scores
     */
    private void saveScore() {
        String newName = new String(this.name);
        Score newScore = new Score(newName, this.gameState);
        boolean foundAndReplaced = false;
        for (int i = 0; i < highScores.size(); i++) {
            Score existingScore = highScores.get(i);
            if (existingScore.getName().equals(newName)) {
                if (newScore.getScore() > existingScore.getScore()) {
                    highScores.set(i, newScore);
                }
                foundAndReplaced = true;
                break;
            }
        }
        if (!foundAndReplaced) {
            highScores.add(newScore);
        }
        Collections.sort(highScores);
        if (highScores.size() > MAX_HIGH_SCORE_NUM)
            highScores.removeLast();
        try {
            Core.getFileManager().saveHighScores(highScores);
        } catch (IOException e) {
            logger.warning("Couldn't load high scores!");
        }
    }

    /**
     * Save the achievement released.
     * 2025-10-03
     * add new method
     */
    private void saveAchievement() {
        try {
            this.achievementManager.saveToFile(new String(this.name));
        } catch (IOException e) {
            logger.warning("Couldn't save achievements!");
        }
    }

    /**
     * Draws the elements associated with the screen.
     */
    private void draw() {
        drawManager.initDrawing(this);

		drawManager.drawGameOver(this, this.inputDelay.checkFinished());

        float accuracy = (this.bulletsShot > 0) ? (float) this.shipsDestroyed / this.bulletsShot : 0f;
        float acc = (this.bulletsShot > 0) ? (float) this.shipsDestroyed / this.bulletsShot : 0f;
        drawManager.drawResults(this, this.score, this.coins, this.livesRemaining, this.shipsDestroyed, acc, this.isNewRecord, true); // Draw accuracy for 1P mode

		drawManager.drawNameInput(this, this.name, this.isNewRecord);
		if (showNameError)
			drawManager.drawNameInputError(this);

        drawManager.completeDrawing(this);
    }

    public int getTotalCoin() {
        return totalCoin;
    }
}
