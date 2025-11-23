package screen;

import java.awt.event.KeyEvent;
import java.util.*;

import engine.Cooldown;
import engine.Core;
import engine.GameSettings;
import engine.GameState;
import engine.*;
import engine.SoundManager;
import engine.augment.Augment;
import engine.augment.AugmentPool;
import entity.*;
import entity.Enemy.EnemyShip;
import entity.Enemy.EnemyShipFormation;
import entity.Enemy.EnemyShipStats;
import entity.Player.PlayerShip;
import entity.Player.PlayerShipStats;

// NEW Item code


/**
 * Implements the game screen, where the action happens.(supports co-op with
 * shared team lives)
 *
 * @author <a href="mailto:RobertoIA1987@gmail.com">Roberto Izquierdo Amo</a>
 *
 */
public class GameScreen extends Screen {

    /** Milliseconds until the screen accepts user input. */
    private static final int INPUT_DELAY = 6000;
    /** Bonus score for each life remaining at the end of the level. */
    private static final int LIFE_SCORE = 100;
    /** Minimum time between bonus ship's appearances. */
    private static final int BONUS_SHIP_INTERVAL = 20000;
    /** Maximum variance in the time between bonus ship's appearances. */
    private static final int BONUS_SHIP_VARIANCE = 10000;
    /** Time until bonus ship explosion disappears. */
    private static final int BONUS_SHIP_EXPLOSION = 500;
    /** Time from finishing the level to screen change. */
    private static final int SCREEN_CHANGE_INTERVAL = 1500;
    /** Height of the interface separation line. */
    private static final int SEPARATION_LINE_HEIGHT = 68;
    private static final int HIGH_SCORE_NOTICE_DURATION = 2000;
    private static final int MAX_EXP = 100;
    private static final int AUGMENT_OPTION_COUNT = 3;
    private static boolean sessionHighScoreNotified = false;

    /** For Check Achievement
     * 2015-10-02 add new */
    private AchievementManager achievementManager;
    /** Current game difficulty settings. */
    private GameSettings gameSettings;
    /** Current difficulty level number. */
    private int level;
    /** Formation of enemy ships. */
    private EnemyShipFormation enemyShipFormation;
    private EnemyShip enemyShipSpecial;
    /** Formation of player ships. */
    private final PlayerShip playerShip;
    /** Stat of player ships. */
    private final PlayerShipStats playerStats;
    /** Minimum time between bonus ship appearances. */
    private Cooldown enemyShipSpecialCooldown;
    /** Time until bonus ship explosion disappears. */
    private Cooldown enemyShipSpecialExplosionCooldown;
    /** Time from finishing the level to screen change. */
    private Cooldown screenFinishedCooldown;
    /** Set of all bullets fired by on screen ships. */
    private Set<Bullet> bullets;
    /** Set of all items spawned. */
    private Set<Item> items;
    private long gameStartTime;
    /** Checks if the level is finished. */
    private boolean levelFinished;
    /** Checks if a bonus life is received. */
    private final boolean bonusLife;
    private int topScore;
    private boolean highScoreNotified;
    private long highScoreNoticeStartTime;

    private boolean isPaused;
    private Cooldown pauseCooldown;
    private Cooldown returnMenuCooldown;

    /** checks if player took damage
     * 2025-10-02 add new variable
     * */
    private boolean tookDamageThisLevel;
    private boolean countdownSoundPlayed = false;

    private final GameState state;
    DrawManager.SpriteType shipType;

    /** control augment screen and player level up toast screen
     * 2025-11-16 add new variable
     * */
    private List<Augment> augOption;
    private boolean isAugSelect = false;
    private int augmentIndex = 0;
    private Cooldown augmentCooldown;

    private boolean isLevelUpToast = false;
    private long levelUpToastStart;

    /**
     * Constructor, establishes the properties of the screen.
     *
     * @param gameState
     *                     Current game state.
     * @param gameSettings
     *                     Current game settings.
     * @param bonusLife
     *                     Checks if a bonus life is awarded this level.
     * @param width
     *                     Screen width.
     * @param height
     *                     Screen height.
     * @param fps
     *                     Frames per second, frame rate at which the game is run.
     * @param achievementManager
     * 			               Achievement manager instance used to track and save player achievements.
     * 			  2025-10-03 add generator parameter and comment
     */
    public GameScreen(final GameState gameState,
                      final GameSettings gameSettings, final boolean bonusLife,
                      final int width, final int height, final int fps, final AchievementManager achievementManager) {
        super(width, height, fps);

        this.state = gameState;
        this.gameSettings = gameSettings;
        this.bonusLife = bonusLife;
        this.level = gameState.getLevel();
        this.playerShip = gameState.getPlayerShip();
        this.playerStats = playerShip.getStats();

        // for check Achievement 2025-10-02 add
        this.achievementManager = achievementManager;
        this.tookDamageThisLevel = false;
        this.highScoreNotified = false;
        this.highScoreNoticeStartTime = 0;

        if (this.bonusLife)
            playerShip.getStats().setCurHP(playerShip.getStats().getCurHP() + 1);
      // [ADD] ensure achievementManager is not null for popup system
		if (this.achievementManager == null) this.achievementManager = new AchievementManager();
    }

      /**
     * Resets the session high score notification flag.
     * Should be called when a new game starts from the main menu.
     */
    public static void resetSessionHighScoreNotified() {
        sessionHighScoreNotified = false;
    }

    /**
     * Initializes basic screen properties, and adds necessary elements.
     */
    public final void initialize() {
        super.initialize();

        state.clearAllEffects();
        playerShip.resetPosition(this.getWidth() / 2, this.getHeight() /10 * 9);
        // Start background music for gameplay
        SoundManager.startBackgroundMusic("sound/SpaceInvader-GameTheme.wav");

        enemyShipFormation = new EnemyShipFormation(this.gameSettings);
        enemyShipFormation.attach(this);

        this.enemyShipSpecialCooldown = Core.getVariableCooldown(BONUS_SHIP_INTERVAL, BONUS_SHIP_VARIANCE);
        this.enemyShipSpecialCooldown.reset();
        this.enemyShipSpecialExplosionCooldown = Core.getCooldown(BONUS_SHIP_EXPLOSION);
        this.screenFinishedCooldown = Core.getCooldown(SCREEN_CHANGE_INTERVAL);
        this.bullets = new HashSet<Bullet>();

        // New Item Code
        this.items = new HashSet<Item>();

		// Special input delay / countdown.
		this.gameStartTime = System.currentTimeMillis();
		this.inputDelay = Core.getCooldown(INPUT_DELAY);
		this.inputDelay.reset();
        drawManager.setDeath(false);

        this.isPaused = false;
        this.pauseCooldown = Core.getCooldown(300);
        this.returnMenuCooldown = Core.getCooldown(300);

        augmentCooldown = Core.getCooldown(300);  // 0.2초 디바운스
        augmentCooldown.reset();
    }

    /**
     * Starts the action.
     *
     * @return Next screen code.
     */
    public final int run() {
        super.run();

        state.addScore(LIFE_SCORE * playerShip.getStats().getCurHP());

        // Stop all music on exiting this screen
        SoundManager.stopAllMusic();

        this.logger.info("Screen cleared with a score of " + state.getScore());
        return this.returnCode;
    }

    /**
     * Updates the elements on screen and checks for events.
     */
    protected final void update() {
        super.update();

        // Countdown beep once during pre-start
        if (!this.inputDelay.checkFinished() && !countdownSoundPlayed) {
            long elapsed = System.currentTimeMillis() - this.gameStartTime;
            if (elapsed > 1750) {
                SoundManager.playOnce("sound/CountDownSound.wav");
                countdownSoundPlayed = true;
            }
        }

        if (this.inputDelay.checkFinished() && inputManager.isKeyDown(KeyEvent.VK_ESCAPE) && this.pauseCooldown.checkFinished()) {
            this.isPaused = !this.isPaused;
            this.pauseCooldown.reset();

            if (this.isPaused) {
                // Pause game music when pausing - no sound during pause
                SoundManager.stopBackgroundMusic();
            } else {
                // Resume game music when unpausing
                SoundManager.startBackgroundMusic("sound/SpaceInvader-GameTheme.wav");
            }
        }
        if (this.isPaused && inputManager.isKeyDown(KeyEvent.VK_BACK_SPACE) && this.returnMenuCooldown.checkFinished()) {
            SoundManager.playOnce("sound/select.wav");
            SoundManager.stopAllMusic(); // Stop all music before returning to menu
            returnCode = 1;
            this.isRunning = false;
        }
        // Pause 상태에서 Enter → 즉시 종료하고 점수 화면으로
        if (this.isPaused && inputManager.isKeyDown(KeyEvent.VK_ENTER)) {
            earlyExitToScore();
            return;
        }

        checkLevelUp();
        if (isLevelUpToast) {
            if (System.currentTimeMillis() - levelUpToastStart >= 1000) {
                cleanBullets();
                isLevelUpToast = false;
            }
        }
        if (isAugSelect && !isLevelUpToast) {
            selectAugment();
        }

        if (!this.isPaused && !this.isAugSelect) {
            if (this.inputDelay.checkFinished() && !this.levelFinished) {
                boolean moveRight, moveLeft, moveUp, moveDown, fire;
                moveRight = inputManager.isP1RightPressed();
                moveLeft = inputManager.isP1LeftPressed();
                moveUp = inputManager.isP1UpPressed();
                moveDown = inputManager.isP1DownPressed();
                boolean isRightBorder = playerShip.getPositionX() + playerShip.getWidth() + playerShip.getStats().getMoveSpeed() > this.width - 1;

                boolean isLeftBorder = playerShip.getPositionX() - playerShip.getStats().getMoveSpeed() < 1;

                boolean isUpBorder = playerShip.getPositionY() + playerShip.getHeight() + playerShip.getStats().getMoveSpeed() > this.height - 1;

                boolean isDownBorder = playerShip.getPositionY() - playerShip.getStats().getMoveSpeed() < 1;

                if (moveRight && !isRightBorder)
                    playerShip.moveRight();
                if (moveLeft && !isLeftBorder)
                    playerShip.moveLeft();
                if (moveUp && !isUpBorder)
                    playerShip.moveUp();
                if (moveDown && !isDownBorder)
                    playerShip.moveDown();

                fire = inputManager.isKeyDown(KeyEvent.VK_SPACE);

                if (fire && playerShip.shoot(this.bullets)) {
                    SoundManager.playOnce("sound/shoot.wav");
                    state.incBulletsShot(); // 2P mode: increments per-player bullet shots
                }
                // Special ship lifecycle
                if (this.enemyShipSpecial != null) {
                    if (!this.enemyShipSpecial.isDestroyed())
                        this.enemyShipSpecial.move(2, 0);
                    else if (this.enemyShipSpecialExplosionCooldown.checkFinished())
                        this.enemyShipSpecial = null;
                }
                if (this.enemyShipSpecial == null && this.enemyShipSpecialCooldown.checkFinished()) {
                    this.enemyShipSpecial = new EnemyShip();
                    this.enemyShipSpecialCooldown.reset();
                    SoundManager.playLoop("sound/special_ship_sound.wav");
                    this.logger.info("A special ship appears");
                }
                if (this.enemyShipSpecial != null && this.enemyShipSpecial.getPositionX() > this.width) {
                    this.enemyShipSpecial = null;
                    SoundManager.stop();
                    this.logger.info("The special ship has escaped");
                }
                // Update ships & enemies
                playerShip.update();

                this.enemyShipFormation.update();
                int bulletsBefore = this.bullets.size();
                this.enemyShipFormation.shoot(this.bullets);
                if (this.bullets.size() > bulletsBefore) {
                    // At least one enemy bullet added
                    SoundManager.playOnce("sound/shoot_enemies.wav");
                }
            }

        }

        manageCollisions();
        cleanBullets();

        // Item Entity Code
        cleanItems();
        manageItemPickups();

        // check active item affects
        state.updateEffects();
        drawManager.setLastLife(playerShip.getStats().getCurHP() == 1);
        draw();

        if (!sessionHighScoreNotified && this.state.getScore() > this.topScore) {
            sessionHighScoreNotified = true;
            this.highScoreNotified = true;
            this.highScoreNoticeStartTime = System.currentTimeMillis();
        }
        // End condition: formation cleared or TEAM lives exhausted.
        if ((this.enemyShipFormation.isEmpty() || playerShip.getStats().getCurHP() == 0) && !this.levelFinished) {
            // The object managed by the object pool pattern must be recycled at the end of the level.
            BulletPool.recycle(this.bullets);
            this.bullets.removeAll(this.bullets);
            ItemPool.recycle(items);
            this.items.removeAll(this.items);
            this.levelFinished = true;
            this.screenFinishedCooldown.reset();
        }

        if (this.levelFinished && this.screenFinishedCooldown.checkFinished() && !this.isAugSelect) {
            if (!achievementManager.hasPendingToasts()) {
                this.isRunning = false;
            }
        }
        if (this.achievementManager != null) this.achievementManager.update();
        checkAchievement();
        draw();
    }

    /**
     * Draws the elements associated with the screen.
     */
    private void draw() {
        drawManager.initDrawing(this);

        drawManager.drawExplosions();
        drawManager.updateGameSpace();

        drawManager.drawEntity(playerShip, playerShip.getPositionX(), playerShip.getPositionY());
        drawManager.drawHpBar(playerShip.getPositionX(),  playerShip.getPositionY() + playerShip.getHeight() + 2, playerShip.getWidth(), 4, playerStats.getCurHP(),playerStats.getMaxHP(), false);

        if (this.enemyShipSpecial != null)
            drawManager.drawEntity(this.enemyShipSpecial, this.enemyShipSpecial.getPositionX(), this.enemyShipSpecial.getPositionY());
        enemyShipFormation.draw();
        for (EnemyShip enemyShip : this.enemyShipFormation) {
            if (enemyShip.isDestroyed()) continue;

            EnemyShipStats stats = enemyShip.getStats();
            drawManager.drawHpBar(enemyShip.getPositionX(),enemyShip.getPositionY() + 16,enemyShip.getWidth(),3,
                    stats.getHp() - stats.getTotalDamage(),stats.getHp(),true);
        }

        for (Bullet bullet : this.bullets)
            drawManager.drawEntity(bullet, bullet.getPositionX(),bullet.getPositionY());

        // draw items
        for (Item item : this.items)
            drawManager.drawEntity(item, item.getPositionX(),item.getPositionY());

		// Aggregate UI (team score & team lives)
		drawManager.drawScore(this, state.getScore());
        drawManager.drawExp(this, playerStats.getExp());
        drawManager.drawHpBar(40,25,100,10,playerStats.getCurHP(),playerStats.getMaxHP(),false);
        drawManager.drawCoins(this,  state.getCoins());
        drawManager.drawLevel(this, this.state.getLevel());
		drawManager.drawHorizontalLine(this, SEPARATION_LINE_HEIGHT - 1);
        drawManager.drawShipCount(this, enemyShipFormation.getShipCount());
		if (!this.inputDelay.checkFinished()) {
			int countdown = (int) ((INPUT_DELAY - (System.currentTimeMillis() - this.gameStartTime)) / 1000);
			drawManager.drawCountDown(this, this.state.getLevel(), countdown, this.bonusLife);
			drawManager.drawHorizontalLine(this, this.height / 2 - this.height / 12);
			drawManager.drawHorizontalLine(this, this.height / 2 + this.height / 12);
		}
        if (this.highScoreNotified &&
                System.currentTimeMillis() - this.highScoreNoticeStartTime < HIGH_SCORE_NOTICE_DURATION) {
            drawManager.drawNewHighScoreNotice(this);
        }

		// [ADD] draw achievement popups right before completing the frame
		drawManager.drawAchievementToasts(
				this,
				(this.achievementManager != null)
						? this.achievementManager.getActiveToasts()
						: java.util.Collections.emptyList()
		);
		if(this.isPaused){
			drawManager.drawPauseOverlay(this);
            // pause 화면에서 표시
            drawManager.drawCenteredRegularString(
                    this,
                    "[ENTER] Finish and Show Score  |  [ESC] Resume",
                    this.height / 2 + 60
            );
		}
        if (this.isLevelUpToast) {
            drawManager.drawLevelUpToast(this);
        }

        if(this.isAugSelect && !isLevelUpToast){
            drawManager.drawAugmentOverlay(this, augOption, augmentIndex);
        }
        drawManager.completeDrawing(this);
    }

    /**
     * Checks whether the player has enough EXP to level up.
     * 2025-11-16 Added in commit : feat : Add augment select system.
     */
    private void checkLevelUp(){
        if(playerStats.getExp() >= MAX_EXP){
            playerStats.resetExp();
            List<Augment> list = new ArrayList<>(AugmentPool.pool);
            Collections.shuffle(list);
            augOption = list.subList(0, Math.min(AUGMENT_OPTION_COUNT, list.size()));
            isAugSelect = true;
            isLevelUpToast = true;
            cleanBullets();
            SoundManager.playOnce("sound/win.wav");
            levelUpToastStart = System.currentTimeMillis();
        }
    }

    /**
     * Handles input for the augment selection menu.
     * 2025-11-16 Added in commit : feat : Add augment select system.
     */
    private void selectAugment(){
        if(isAugSelect){
            if((inputManager.isKeyDown(KeyEvent.VK_UP) ||  inputManager.isKeyDown(KeyEvent.VK_W))
                    && augmentCooldown.checkFinished()){
                augmentIndex = (augmentIndex - 1 + AUGMENT_OPTION_COUNT) % AUGMENT_OPTION_COUNT;
                augmentCooldown.reset();
            }else if((inputManager.isKeyDown(KeyEvent.VK_DOWN) ||  inputManager.isKeyDown(KeyEvent.VK_S))
                    && augmentCooldown.checkFinished()){
                augmentIndex = (augmentIndex + 1 + AUGMENT_OPTION_COUNT) % AUGMENT_OPTION_COUNT;
                augmentCooldown.reset();
            }else if((inputManager.isKeyDown(KeyEvent.VK_SPACE) && augmentCooldown.checkFinished())) {
                isAugSelect = false;
                augmentCooldown.reset();
            }
        }
    }

    /**
     * Cleans bullets that go off screen.
     */
    private void cleanBullets() {
        Set<Bullet> recyclable = new HashSet<Bullet>();
        for (Bullet bullet : this.bullets) {
            bullet.update();
            if (bullet.getPositionY() < SEPARATION_LINE_HEIGHT
                    || bullet.getPositionY() > this.height)
                recyclable.add(bullet);
        }
        this.bullets.removeAll(recyclable);
        BulletPool.recycle(recyclable);
    }

    /**
     * Cleans items that go off screen.
     */
    private void cleanItems() {
        Set<Item> recyclableItems = new HashSet<Item>();
        for (Item item : this.items) {
            item.update();
            if (item.getPositionY() > this.height)
                recyclableItems.add(item);
        }
        this.items.removeAll(recyclableItems);
        ItemPool.recycle(recyclableItems);
    }

    /**
     * Manages pickups between player and items.
     */
    private void manageItemPickups() {
        Set<Item> collected = new HashSet<Item>();
        for (Item item : this.items) {
            if (checkCollision(item, playerShip) && !collected.contains(item)) {
                collected.add(item);
                this.logger.info("Player " + " picked up item: " + item.getType());
                SoundManager.playOnce("sound/hover.wav");
                item.applyEffect(getGameState());
            }
        }
        this.items.removeAll(collected);
        ItemPool.recycle(collected);
    }

    /**
     * Enemy bullets hit players → decrement TEAM lives; player bullets hit enemies
     * → add score.
     */
    private void manageCollisions() {
        Set<Bullet> recyclable = new HashSet<Bullet>();
        for (Bullet bullet : this.bullets) {
            if (bullet.getSpeed() > 0) {
                // Enemy bullet vs both players
                if (playerShip != null && !playerShip.isDestroyed() && checkCollision(bullet, playerShip) &&
                        !this.levelFinished) {
                    recyclable.add(bullet);
                    drawManager.triggerExplosion(playerShip.getPositionX(), playerShip.getPositionY(), false,
                            playerShip.getStats().getCurHP() == 1);
                    playerShip.addHit();

                    playerShip.destroy(); // explosion/respawn handled by Ship.update()
                    SoundManager.playOnce("sound/explosion.wav");
                    playerShip.getStats().setCurHP(playerShip.getStats().getCurHP() - 1); // decrement shared/team lives by 1

                    // Record damage for Survivor achievement check
                    this.tookDamageThisLevel = true;

                    drawManager.setLastLife(playerShip.getStats().getCurHP() == 1);
                    drawManager.setDeath(playerShip.getStats().getCurHP() == 0);

                    this.logger.info("Hit on player " + ", team lives now: " + playerShip.getStats().getCurHP());
                    break;

				}
			} else {
				// Player bullet vs enemies
                boolean finalShip = this.enemyShipFormation.lastShip();

                // Check collision with formation enemies
                for (EnemyShip enemyShip : this.enemyShipFormation) {
                    if (!enemyShip.isDestroyed() && checkCollision(bullet, enemyShip)) {
                        recyclable.add(bullet);
                        enemyShip.hit(playerShip.getStats().getATK());

                        if (enemyShip.isDestroyed()) {
                            int points = enemyShip.getStats().getPointValue();
                            state.addCoins(enemyShip.getStats().getCoinValue()); // 2P mode: modified to per-player coins
                            int exp = enemyShip.getStats().getExpValue();

                            drawManager.triggerExplosion(enemyShip.getPositionX(), enemyShip.getPositionY(),
                                    true, finalShip);
                            state.addScore(points); // 2P mode: modified to add to P1 score for now
                            playerStats.addExp(exp);
                            state.incShipsDestroyed();

                            // obtain drop from ItemManager (may return null)
                            Item drop = engine.ItemManager.getInstance().obtainDrop(enemyShip);
                            if (drop != null) {
                                this.items.add(drop);
                                this.logger.info("Spawned " + drop.getType() + " at " + drop.getPositionX() + "," + drop.getPositionY());
                            }

                            this.enemyShipFormation.destroy(enemyShip);
                            SoundManager.playOnce("sound/invaderkilled.wav");
                            this.logger.info("Hit on enemy ship.");
                        }
                        break;
                    }
                }

                if (this.enemyShipSpecial != null && !this.enemyShipSpecial.isDestroyed() && checkCollision(bullet, this.enemyShipSpecial)) {
                    int points = this.enemyShipSpecial.getStats().getPointValue();
                    int exp = this.enemyShipSpecial.getStats().getExpValue();

                    state.addCoins(this.enemyShipSpecial.getStats().getCoinValue()); // 2P mode: modified to per-player coins

                    state.addScore(points);
                    playerStats.addExp(exp);
                    state.incShipsDestroyed(); // 2P mode: modified incrementing ships destroyed

					this.enemyShipSpecial.destroy();
                    SoundManager.stop();
                    SoundManager.playOnce("sound/explosion.wav");
                    drawManager.triggerExplosion(this.enemyShipSpecial.getPositionX(), this.enemyShipSpecial.getPositionY(), true, true);
                    this.enemyShipSpecialExplosionCooldown.reset();
                    recyclable.add(bullet);
                }
            }
        }
        this.bullets.removeAll(recyclable);
        BulletPool.recycle(recyclable);
    }

    /**
     * Checks if two entities are colliding.
     *
     * @param a
     *            First entity, the bullet.
     * @param b
     *            Second entity, the ship.
     * @return Result of the collision test.
     */
    private boolean checkCollision(final Entity a, final Entity b) {
        int centerAX = a.getPositionX() + a.getWidth() / 2;
        int centerAY = a.getPositionY() + a.getHeight() / 2;
        int centerBX = b.getPositionX() + b.getWidth() / 2;
        int centerBY = b.getPositionY() + b.getHeight() / 2;
        int maxDistanceX = a.getWidth() / 2 + b.getWidth() / 2;
        int maxDistanceY = a.getHeight() / 2 + b.getHeight() / 2;
        int distanceX = Math.abs(centerAX - centerBX);
        int distanceY = Math.abs(centerAY - centerBY);
        return distanceX < maxDistanceX && distanceY < maxDistanceY;
    }

    /**
     * Returns a GameState object representing the status of the game.
     *
     * @return Current game state.
     */
    public final GameState getGameState() {
        return this.state;
    }

    /**
     * check Achievement released;
     */
    public void checkAchievement(){
        // First Blood
        if(state.getShipsDestroyed() == 1) {
            achievementManager.unlock("First Blood");
        }
        // Clear
        if (levelFinished && this.enemyShipFormation.isEmpty() && state.getLevel()==5) {
            achievementManager.unlock("Clear");
            float Acc = state.getBulletsShot() > 0 ? (float) state.getShipsDestroyed() / state.getBulletsShot()*100 : 0f;
            // Survivor
            if(!this.tookDamageThisLevel){
                achievementManager.unlock("Survivor");
            }
            //Sharpshooter
            if(Acc>=80)
                achievementManager.unlock("Sharpshooter");
        }

        //50 Bullets
        if(state.getBulletsShot() >= 50){
            achievementManager.unlock("50 Bullets");
        }
        //Get 3000 Score
        if(state.getScore()>=3000){
            achievementManager.unlock("Get 3000 Score");
        }
    }
    private void earlyExitToScore() {
        SoundManager.stopBackgroundMusic();
        // 목숨 0으로
        while (playerStats.getCurHP() > 0) playerStats.setCurHP(playerStats.getCurHP() - 1);
    }
}