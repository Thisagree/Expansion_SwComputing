package engine;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.Rectangle;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.*;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import Animations.BasicGameSpace;
import Animations.Explosion;
import Animations.MenuSpace;
import entity.Enemy.EnemyShip;
import entity.Player.PlayerShip;
import engine.augment.Augment;
import screen.Screen;
import entity.Entity;
import entity.Bullet;

/**
 * Manages screen drawing.
 *
 * @author <a href="mailto:RobertoIA1987@gmail.com">Roberto Izquierdo Amo</a>
 *
 */
public final class DrawManager {

    /** Singleton instance of the class. */
    private static DrawManager instance;
    /** Current frame. */
    private static Frame frame;
    /** Application logger. */
    private static Logger logger;
    /** Graphics context. */
    private static Graphics graphics;
    /** Buffer Graphics. */
    private static Graphics backBufferGraphics;
    /** Buffer image. */
    private static BufferedImage backBuffer;
    /** Normal sized font. */
    private static Font fontRegular;
    /** Normal sized font properties. */
    private static FontMetrics fontRegularMetrics;
    /** Big sized font. */
    private static Font fontBig;
    /** Big sized font properties. */
    private static FontMetrics fontBigMetrics;

    /** Sprite types mapped to their images. */
    private static Map<SpriteType, boolean[][]> spriteMap;

    private final List<Explosion> explosions = new ArrayList<>();

    /**
     * Stars background animations for both game and main menu
     * Star density specified as argument.
     * */
    BasicGameSpace basicGameSpace = new BasicGameSpace(100);
    MenuSpace menuSpace = new MenuSpace(50);
    int explosion_size = 2;


    // Label for back button
    private static final String BACK_LABEL = "< Back";

    /** Sprite types. */
    public enum SpriteType {
        /** Player ship. */
        Normal,
        BigShot,
        DoubleShot,
        MoveFast,
        /** Destroyed player ship. */
        ShipDestroyedNormal,
        ShipDestroyedBigShot,
        ShipDestroyedDoubleShot,
        ShipDestroyedMoveFast,
        /** Player bullet. */
        Bullet,
        /** Enemy bullet. */
        EnemyBullet,
        /** First enemy ship - first form. */
        EnemyShipA1,
        /** First enemy ship - second form. */
        EnemyShipA2,
        /** Second enemy ship - first form. */
        EnemyShipB1,
        /** Second enemy ship - second form. */
        EnemyShipB2,
        /** Third enemy ship - first form. */
        EnemyShipC1,
        /** Third enemy ship - second form. */
        EnemyShipC2,
        /** Bonus ship. */
        EnemyShipSpecial,
        /** Destroyed enemy ship. */
        Explosion,
        /** Heart for lives display. */
        Heart, //추가
        /** Item Graphics Temp */
        ItemScore,
        ItemCoin,
        ItemHeal,
        ItemTripleShot,
        ItemScoreBooster,
        ItemBulletSpeedUp
    }

    /**
     * Private constructor.
     */
    private DrawManager() {
        FileManager fileManager = Core.getFileManager();
        logger = Core.getLogger();
        logger.info("Started loading resources.");

        try {
            spriteMap = new LinkedHashMap<>();

            spriteMap.put(SpriteType.Normal, new boolean[13][8]);
            spriteMap.put(SpriteType.BigShot, new boolean[13][8]);
            spriteMap.put(SpriteType.DoubleShot, new boolean[13][8]);
            spriteMap.put(SpriteType.MoveFast, new boolean[13][8]);
            spriteMap.put(SpriteType.ShipDestroyedNormal, new boolean[13][8]);
            spriteMap.put(SpriteType.ShipDestroyedBigShot, new boolean[13][8]);
            spriteMap.put(SpriteType.ShipDestroyedDoubleShot, new boolean[13][8]);
            spriteMap.put(SpriteType.ShipDestroyedMoveFast, new boolean[13][8]);
            spriteMap.put(SpriteType.Bullet, new boolean[3][5]);
            spriteMap.put(SpriteType.EnemyBullet, new boolean[3][5]);
            spriteMap.put(SpriteType.EnemyShipA1, new boolean[12][8]);
            spriteMap.put(SpriteType.EnemyShipA2, new boolean[12][8]);
            spriteMap.put(SpriteType.EnemyShipB1, new boolean[12][8]);
            spriteMap.put(SpriteType.EnemyShipB2, new boolean[12][8]);
            spriteMap.put(SpriteType.EnemyShipC1, new boolean[12][8]);
            spriteMap.put(SpriteType.EnemyShipC2, new boolean[12][8]);
            spriteMap.put(SpriteType.EnemyShipSpecial, new boolean[16][7]);
            spriteMap.put(SpriteType.Explosion, new boolean[13][7]);
            spriteMap.put(SpriteType.Heart, new boolean[11][10]);

            // Item sprite placeholder
            spriteMap.put(SpriteType.ItemScore, new boolean[5][5]);
            spriteMap.put(SpriteType.ItemCoin, new boolean[5][5]);
            spriteMap.put(SpriteType.ItemHeal, new boolean[5][5]);
            spriteMap.put(SpriteType.ItemTripleShot, new boolean[5][5]);
            spriteMap.put(SpriteType.ItemScoreBooster, new boolean[5][5]);
            spriteMap.put(SpriteType.ItemBulletSpeedUp, new boolean[5][5]);

            fileManager.loadSprite(spriteMap);
            logger.info("Finished loading the sprites.");

            // Font loading.
            fontRegular = fileManager.loadFont(14f);
            fontBig = fileManager.loadFont(24f);
            logger.info("Finished loading the fonts.");

        } catch (IOException e) {
            logger.warning("Loading failed.");
        } catch (FontFormatException e) {
            logger.warning("Font formating failed.");
        }
    }

    /**
     * Returns shared instance of DrawManager.
     *
     * @return Shared instance of DrawManager.
     */
    static DrawManager getInstance() {
        if (instance == null)
            instance = new DrawManager();
        return instance;
    }

    /**
     * Sets the frame to draw the image on.
     *
     * @param currentFrame
     *                     Frame to draw on.
     */
    public void setFrame(final Frame currentFrame) {
        frame = currentFrame;
    }

    /**
     * First part of the drawing process. Initialises buffers, draws the
     * background and prepares the images.
     *
     * @param screen
     *               Screen to draw in.
     */
    public void initDrawing(final Screen screen) {
        backBuffer = new BufferedImage(screen.getWidth(), screen.getHeight(),
                BufferedImage.TYPE_INT_RGB);

        graphics = frame.getGraphics();
        backBufferGraphics = backBuffer.getGraphics();

        backBufferGraphics.setColor(Color.BLACK);
        backBufferGraphics
                .fillRect(0, 0, screen.getWidth(), screen.getHeight());

        fontRegularMetrics = backBufferGraphics.getFontMetrics(fontRegular);
        fontBigMetrics = backBufferGraphics.getFontMetrics(fontBig);
    }

    /**
     * Draws the completed drawing on screen.
     *
     * @param screen
     *               Screen to draw on.
     */
    public void completeDrawing(final Screen screen) {
        graphics.drawImage(backBuffer, frame.getInsets().left,
                frame.getInsets().top, frame);
    }

    /**
     * Draws an entity, using the appropriate image.
     *
     * @param entity
     *                  Entity to be drawn.
     * @param positionX
     *                  Coordinates for the left side of the image.
     * @param positionY
     *                  Coordinates for the upper side of the image.
     */
    public void drawEntity(final Entity entity, final int positionX, final int positionY) {
        boolean[][] image = spriteMap.get(entity.getSpriteType());

        // 2P mode: start with the entity's own color
        Color color = getColor(entity);

        // --- Scaling logic ---
        // Original sprite dimensions
        int spriteWidth = image.length;
        int spriteHeight = image[0].length;

        // Entity dimensions (modified via Bullet constructor or other entities)
        int entityWidth = entity.getWidth();
        int entityHeight = entity.getHeight();

        // Calculate scaling ratios compared to original sprite
        float widthRatio = (float) entityWidth / (spriteWidth * 2);
        float heightRatio = (float) entityHeight / (spriteHeight * 2);
        // --- End of scaling logic ---

        // Set drawing color again
        backBufferGraphics.setColor(color);
        // Draw the sprite with scaling applied
        for (int i = 0; i < spriteWidth; i++) {
            for (int j = 0; j < spriteHeight; j++) {
                if (image[i][j]) {
                    // Apply calculated scaling ratio to pixel positions and size
                    backBufferGraphics.fillRect(
                            positionX + (int)(i * 2 * widthRatio),
                            positionY + (int)(j * 2 * heightRatio),
                            (int)Math.ceil(widthRatio * 2), // Adjust the width of the pixel
                            (int)Math.ceil(heightRatio * 2) // Adjust the height of the pixel
                    );
                }
            }
        }
    }

    private static Color getColor(Entity entity) {
        Color color = entity.getColor();

        // Color-code by player when applicable
        if (entity instanceof PlayerShip playerShip) {
            color = Color.BLUE;
            // else leave default (e.g., green) for legacy/unknown
        }
        else if (entity instanceof Bullet bullet) {
            color = Color.CYAN; // P1 bullet
            // enemy bullets will keep their default color from the entity
        }

        /*
          Makes A-type enemies semi-transparent when their health is 1.
          Checks if the entity is an EnemyShip of type A (EnemyShipA1 or A2),
          and sets its color alpha to 32 to indicate critical damage.
         */
        if (entity instanceof EnemyShip enemy) {
            if((enemy.getSpriteType() == SpriteType.EnemyShipA1 || enemy.getSpriteType() == SpriteType.EnemyShipA2) && enemy.getStats().getHp() == 1)
                color = new Color(color.getRed(), color.getGreen(), color.getBlue(), 32);
        }
        return color;
    }


    public void menuHover(final int state){
        menuSpace.setColor(state);
        menuSpace.setSpeed(state == 4);
    }

    public void triggerExplosion(int x, int y, boolean enemy, boolean finalExplosion) {
        logger.info("Enemy: "+enemy);
        logger.info("final: "+finalExplosion);
        explosions.add(new Explosion(x, y, enemy, finalExplosion));
    }

    public void drawExplosions(){

        Graphics2D g2d = (Graphics2D) backBufferGraphics;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setColor(Color.WHITE);


        Iterator<Explosion> iterator = explosions.iterator();

        while(iterator.hasNext()){
            Explosion e = iterator.next();
            e.update();

            if (!e.isActive()) {
                iterator.remove();
                continue;
            }

            for(Explosion.Particle p : e.getParticles()){
                if(!p.active){
                    continue;
                }

                int baseSize;

                Random random = new Random();
                if (e.getSize() == 4)
                    baseSize = random.nextInt(5) + 2;
                else
                    baseSize = random.nextInt(6)+18;

                int flickerAlpha = Math.max(0, Math.min(255, p.color.getAlpha() - (int)(Math.random() * 50)));


                float[] dist = {0.0f, 0.3f, 0.7f, 1.0f};
                Color[] colors;
                if(e.enemy()){
                    colors = new Color[]{
                            new Color(255, 255, 250, flickerAlpha),
                            new Color(255, 250, 180, flickerAlpha),
                            new Color(255, 200, 220, flickerAlpha / 2),
                            new Color(0, 0, 0, 0)
                    };
                }
                else{
                    colors = new Color[]{
                            new Color(255, 255, 180, flickerAlpha),
                            new Color(255, 200, 0, flickerAlpha),
                            new Color(255, 80, 0, flickerAlpha / 2),
                            new Color(0, 0, 0, 0)
                    };
                }

                RadialGradientPaint paint = new RadialGradientPaint(
                        new Point((int) p.x, (int) p.y),
                        baseSize,
                        dist,
                        colors
                );

                g2d.setPaint(paint);

                int offsetX = (int) (Math.random() * 4 - 2);
                int offsetY = (int) (Math.random() * 4 - 2);

                g2d.fillOval(
                        (int) (p.x - (double) baseSize / 2 + offsetX),
                        (int) (p.y - (double) baseSize / 2 + offsetY),
                        baseSize,
                        baseSize
                );
            }

        }
    }



    /**
     * Draws the main menu stars background animation
     */
    public void updateMenuSpace(){
        menuSpace.updateStars();

        Graphics2D g2d = (Graphics2D) backBufferGraphics;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        backBufferGraphics.setColor(Color.WHITE);
        int[][] positions = menuSpace.getStarLocations();

        for(int i = 0; i < menuSpace.getNumStars(); i++){

            int size = 1;
            int radius = size * 2;

            float[] dist = {0.0f, 1.0f};
            Color[] colors = {
                    menuSpace.getColor(),
                    new Color(255, 255, 200, 0)
            };

            RadialGradientPaint paint = new RadialGradientPaint(
                    new Point(positions[i][0], positions[i][1]),
                    radius,
                    dist,
                    colors
            );
            g2d.setPaint(paint);
            g2d.fillOval(positions[i][0] - radius / 2, positions[i][1] - radius / 2, radius, radius);


            backBufferGraphics.fillOval(positions[i][0], positions[i][1], size, size);
        }
    }

    public void setLastLife(boolean status){
        basicGameSpace.setLastLife(status);
    }

    public void setDeath(boolean status){
        if(status)
            explosion_size = 20;
        else
            explosion_size = 2;
    }


    /**
     * Draws the stars background animation during the game
     */
    public void updateGameSpace(){
        basicGameSpace.update();

        Graphics2D g2d = (Graphics2D) backBufferGraphics;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        backBufferGraphics.setColor(Color.WHITE);
        int[][] positions = basicGameSpace.getStarLocations();
        for(int i = 0; i < basicGameSpace.getNumStars(); i++){

            int size = (positions[i][2] < 2) ? 2 : 1;
            int radius = size * 2;

            float[] dist = {0.0f, 1.0f};
            Color[] colors = new Color[2];
            if(basicGameSpace.isLastLife()){
                colors[0] = new Color(255, 0, 0, 100);
                colors[1] = new Color(255, 0, 0, 50);
            }
            else{
                colors[0] = new Color(255, 255, 200, 50);
                colors[1] = new Color(255, 255, 200, 50);
            }

            RadialGradientPaint paint = new RadialGradientPaint(
                    new Point(positions[i][0] + size / 2, positions[i][1] + size / 2),
                    radius,
                    dist,
                    colors
            );
            g2d.setPaint(paint);
            g2d.fillOval(positions[i][0] - radius / 2, positions[i][1] - radius / 2, radius, radius);


            backBufferGraphics.fillOval(positions[i][0], positions[i][1], size, size);
        }
    }
    /**
     * For debugging purposes, draws the canvas borders.
     *
     * @param screen
     *               Screen to draw in.
     */
    @SuppressWarnings("unused")
    private void drawBorders(final Screen screen) {
        backBufferGraphics.setColor(Color.GREEN);
        backBufferGraphics.drawLine(0, 0, screen.getWidth() - 1, 0);
        backBufferGraphics.drawLine(0, 0, 0, screen.getHeight() - 1);
        backBufferGraphics.drawLine(screen.getWidth() - 1, 0,
                screen.getWidth() - 1, screen.getHeight() - 1);
        backBufferGraphics.drawLine(0, screen.getHeight() - 1,
                screen.getWidth() - 1, screen.getHeight() - 1);
    }

    /**
     * For debugging purposes, draws a grid over the canvas.
     *
     * @param screen
     *               Screen to draw in.
     */
    @SuppressWarnings("unused")
    private void drawGrid(final Screen screen) {
        backBufferGraphics.setColor(Color.DARK_GRAY);
        for (int i = 0; i < screen.getHeight() - 1; i += 2)
            backBufferGraphics.drawLine(0, i, screen.getWidth() - 1, i);
        for (int j = 0; j < screen.getWidth() - 1; j += 2)
            backBufferGraphics.drawLine(j, 0, j, screen.getHeight() - 1);
    }

    /**
     * Draws current score on screen.
     *
     * @param screen
     *               Screen to draw on.
     * @param score
     *               Current score.
     */
    public void drawScore(final Screen screen, final int score) {
        backBufferGraphics.setFont(fontRegular);
        backBufferGraphics.setColor(Color.WHITE);
        String scoreString = String.format("%04d", score);
        backBufferGraphics.drawString(scoreString, screen.getWidth() - 60, 25);
    }

    /**
     * Draws current score on screen.
     *
     * @param screen
     *               Screen to draw on.
     * @param exp
     *               Current exp.
     */
    public void drawExp(final Screen screen, final int exp) {
        backBufferGraphics.setFont(fontRegular);
        backBufferGraphics.setColor(Color.decode("#AD19EC"));
        String scoreString = String.format("%03d", exp);
        backBufferGraphics.drawString(scoreString, screen.getWidth() - 60, 84);
        backBufferGraphics.drawString("EXP :            %", screen.getWidth()-115, 84);
    }

	/**
	 * Draws number of remaining lives on screen.
	 *
	 * @param screen    Screen to draw on.
     * @param lives    Whether the game is in co-op mode.
	 */


    public void drawLives(final Screen screen, final int lives) {
        backBufferGraphics.setFont(fontRegular);
        backBufferGraphics.setColor(Color.WHITE);
        Entity heart = new Entity(0, 0, 11*2, 10*2, Color.RED) {
            { this.spriteType = SpriteType.Heart; }
        };

        backBufferGraphics.drawString(Integer.toString(lives), 20, 40);
        for (int i = 0; i<lives; i++)
            drawEntity(heart, 40 + 35 * i, 23);
    }
	/**
	 * Draws current coin count on screen.
	 *
	 * @param screen
	 *               Screen to draw on.
	 * @param coins
	 *               Current coin count.
	 */ // ADD THIS METHOD
	public void drawCoins(final Screen screen, final int coins) { // ADD THIS METHOD
		backBufferGraphics.setFont(fontRegular); // ADD THIS METHOD
		backBufferGraphics.setColor(Color.YELLOW); // ADD THIS METHOD
		String coinString = String.format("%04d", coins); // ADD THIS METHOD
		backBufferGraphics.drawString(coinString, screen.getWidth() - 60, 52); // ADD THIS METHOD
        backBufferGraphics.drawString("COIN : ", screen.getWidth()-115, 52);
	} // ADD THIS METHOD

    /**
     * Draws a thick line from side to side of the screen.
     *
     * @param screen
     *                  Screen to draw on.
     * @param positionY
     *                  Y coordinate of the line.
     */
    public void drawHorizontalLine(final Screen screen, final int positionY) {
        backBufferGraphics.setColor(Color.GREEN);
        backBufferGraphics.drawLine(0, positionY, screen.getWidth(), positionY);
        backBufferGraphics.drawLine(0, positionY + 1, screen.getWidth(),
                positionY + 1);
    }

    public void drawLevel (final Screen screen, final int level) {
        backBufferGraphics.setColor(Color.WHITE);
        String levelString = "Level " + level;
        backBufferGraphics.drawString(levelString, screen.getWidth()-250, 25);
    }

    public void drawShipCount (final Screen screen, final int shipCount) {
        backBufferGraphics.setColor(Color.GREEN);
        Entity enemyIcon = new Entity(0, 0, 12*2, 8*2, Color.GREEN) {
            { this.spriteType = SpriteType.EnemyShipB2; }
        };
        int iconX = screen.getWidth() - 252;
        int iconY = 37;
        drawEntity(enemyIcon, iconX, iconY);
        String shipString = ": " + shipCount;
        backBufferGraphics.drawString(shipString, iconX + 30, 52);
    }


    /**
     * Draws game title.
     *
     * @param screen
     *               Screen to draw on.
     */
    public void drawTitle(final Screen screen) {
        String titleString = "Invaders";
        String instructionsString = "select with w+s / arrows, confirm with space";

        backBufferGraphics.setColor(Color.GRAY);
        drawCenteredRegularString(screen, instructionsString,
                screen.getHeight() / 2);

        backBufferGraphics.setColor(Color.GREEN);
        drawCenteredBigString(screen, titleString, screen.getHeight() / 3);
    }

    /**
     *
     * @param screen
     *               Screen to draw on.
     * @param selectedIndex
     *               Option selected.
     */
    public void drawMenu(final Screen screen, final Integer hoverOption, final int selectedIndex) {
        String[] items = {"Play", "Achievements", "High scores","Settings", "Exit"};

        int baseY = screen.getHeight() / 3 * 2 - 20; // Adjust spacing due to high society button addition
        int spacing = (int) (fontRegularMetrics.getHeight() * 1.5);
        for (int i = 0; i < items.length; i++) {
            boolean highlight = (hoverOption != null) ? (i == hoverOption) : (i == selectedIndex);
            backBufferGraphics.setColor(highlight ? Color.GREEN : Color.WHITE);
            drawCenteredRegularString(screen, items[i], baseY + spacing * i);
        }
    }

	/**
	 * Draws game results.
	 *
	 * @param screen
	 *                       Screen to draw on.
	 * @param score
	 *                       Score obtained.
	 * @param coins
	 *                       Coins obtained.
	 * @param shipsDestroyed
	 *                       Total ships destroyed.
	 * @param accuracy
	 *                       Total accuracy.
	 * @param isNewRecord
	 *                       If the score is a new high score.
	 */
	public void drawResults(final Screen screen, final int score,
							final int coins, final int livesRemaining , final int shipsDestroyed,
							final float accuracy, final boolean isNewRecord, final boolean accuracy1P) {
		String scoreString = String.format("score %04d", score);
		String coinString = String.format("coins %04d", coins);
		String livesRemainingString = String.format("lives remaining %d", livesRemaining);
		String shipsDestroyedString = "enemies destroyed " + shipsDestroyed;
		String accuracyString = String.format("accuracy %.2f%%", Float.isNaN(accuracy) ? 0.0 : accuracy * 100);

        int height = 4;

		if (isNewRecord) {
			backBufferGraphics.setColor(Color.RED);
		} else {
			backBufferGraphics.setColor(Color.WHITE);
		}

		drawCenteredRegularString(screen, scoreString, screen.getHeight()
				/ height);
		drawCenteredRegularString(screen, coinString,
				screen.getHeight() / height + fontRegularMetrics.getHeight()
						* 2);
		drawCenteredRegularString(screen, livesRemainingString,
				screen.getHeight() / height + fontRegularMetrics.getHeight()
						* 4);
		drawCenteredRegularString(screen, shipsDestroyedString,
				screen.getHeight() / height + fontRegularMetrics.getHeight()
						* 6);
		// Draw accuracy for player in 1P mode
		if (accuracy1P) {
			drawCenteredRegularString(screen, accuracyString, screen.getHeight()
					/ height + fontRegularMetrics.getHeight() * 8);
		}
	}

	/**
	 * Draws interactive characters for name input.
	 *
	 * @param screen
	 *                         Screen to draw on.
	 * @param name
	 *                         Current name inserted.
	 */
	public void drawNameInput(final Screen screen, final StringBuilder name, boolean isNewRecord) {
		String newRecordString = "New Record!";
		String introduceNameString = "Name: ";
		String nameStr = name.toString();

		if (isNewRecord) {
			backBufferGraphics.setColor(Color.GREEN);
			drawCenteredRegularString(screen, newRecordString, screen.getHeight()
					/ 4 + fontRegularMetrics.getHeight() * 11);
		}

		// Draw the current name with blinking cursor
		String displayName = name.isEmpty() ? "" : nameStr;

		// Cursor blinks every 500ms
		boolean showCursor = (System.currentTimeMillis() / 500) % 2 == 0;
		String cursor = showCursor ? "|" : " ";

		String displayText = introduceNameString + displayName + cursor;

		backBufferGraphics.setColor(Color.WHITE);
		drawCenteredRegularString(screen, displayText,
				screen.getHeight() / 4 + fontRegularMetrics.getHeight() * 12);

	}

	public void drawNameInputError(Screen screen) {
		String alert = "Enter at least 3 chars!" ; // "Name too short!"

		backBufferGraphics.setColor(Color.YELLOW);
		drawCenteredRegularString(screen, alert, screen.getHeight()
				/ 4 + fontRegularMetrics.getHeight() * 13 );
	}

    /**
     * Draws basic content of game over screen.
     *
     * @param screen
     *                     Screen to draw on.
     * @param acceptsInput
     *                     If the screen accepts input.
     */
    public void drawGameOver(final Screen screen, final boolean acceptsInput) {
        String gameOverString = "Game Over";
        String continueOrExitString = "Press Space to play again, Escape to exit";

        int height = 4;

        backBufferGraphics.setColor(Color.GREEN);
        drawCenteredBigString(screen, gameOverString, screen.getHeight()
                / height - fontBigMetrics.getHeight() * 2);

        if (acceptsInput)
            backBufferGraphics.setColor(Color.GREEN);
        else
            backBufferGraphics.setColor(Color.GRAY);
        drawCenteredRegularString(screen, continueOrExitString,
                screen.getHeight() / 2 + fontRegularMetrics.getHeight() * 10);
    }
    public void drawPauseOverlay(final Screen screen){
        backBufferGraphics.setColor(new Color(0,0,0,200));
        backBufferGraphics.fillRect(0, 0, screen.getWidth(), screen.getHeight());

        String pauseString = "PAUSED";
        backBufferGraphics.setFont(fontBig);
        backBufferGraphics.setColor(Color.WHITE);
        drawCenteredBigString(screen, pauseString, screen.getHeight()/2);

        String returnMenu = "PRESS BACKSPACE TO RETURN TO TITLE";
        backBufferGraphics.setFont(fontRegular);
        backBufferGraphics.setColor(Color.WHITE);
        drawCenteredRegularString(screen, returnMenu, screen.getHeight()-50);
    }//ADD This Screen

    /**
     * Draws basic content of Augment select screen.
     *
     * @param screen
     *                     Screen to draw on.
     * @param augOption
     *                     Augments to show.
     *
     * 2025-11-16 Added in commit : feat : Add augment select system.
     */
    public void drawAugmentOverlay(final Screen screen, List<Augment> augOption, int toggle){
        backBufferGraphics.setColor(new Color(0,0,0,200));
        backBufferGraphics.fillRect(0, 0, screen.getWidth(), screen.getHeight());

        int height = screen.getHeight()/3;
        int gap = 100;

        // Show 3 augment options
        for (int i = 0; i < augOption.size(); i++) {
            Augment aug = augOption.get(i);
            backBufferGraphics.setColor(Color.GRAY);
            if(toggle == i) backBufferGraphics.setColor(Color.GREEN);
            drawCenteredBigString(screen, aug.name, height + i * gap);
            drawCenteredRegularString(screen, aug.description, height + i * gap+ 30);
        }
    }

    /**
     * Draw player's level up toast
     *
     * @param screen
     * Screen to draw on.
     *
     * 2025-11-16 Added in commit : feat : Add augment select system.
     */
    public void drawLevelUpToast(final Screen screen) {
        Graphics2D g2d = (Graphics2D) backBufferGraphics.create();

        try {
            int boxWidth = 350;
            int boxHeight = 110;
            int x = (screen.getWidth() - boxWidth) / 2;
            int y = (screen.getHeight() - boxHeight) / 2;

            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
            g2d.setColor(Color.BLACK);
            g2d.fillRoundRect(x, y, boxWidth, boxHeight, 15, 15);

            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));

            g2d.setColor(Color.GREEN);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRoundRect(x, y, boxWidth, boxHeight, 15, 15);

            g2d.setFont(fontRegular);
            g2d.setColor(Color.YELLOW);

            String msg = "PLAYER LEVEL UP!";
            FontMetrics fm = g2d.getFontMetrics();
            int w = fm.stringWidth(msg);

            g2d.drawString(msg, (screen.getWidth() - w) / 2, y + boxHeight/2 + fm.getAscent()/2);

        } finally {
            g2d.dispose();
        }
    }

    /**
     * Draws high score screen title and instructions.
     *
     * @param screen
     *               Screen to draw on.
     */
    public void drawHighScoreMenu(final Screen screen) {
        String highScoreString = "High Scores";
        String instructionsString = "Press ESC to return";

        int midX = screen.getWidth() / 2;
        int startY = screen.getHeight() / 3;

        backBufferGraphics.setColor(Color.GREEN);
        drawCenteredBigString(screen, highScoreString, screen.getHeight() / 8);

        backBufferGraphics.setColor(Color.GRAY);
        drawCenteredRegularString(screen, instructionsString,
                screen.getHeight() / 5);

        backBufferGraphics.setColor(Color.GREEN);
        backBufferGraphics.drawString("HighScore", midX - fontBigMetrics.stringWidth("HighScore") / 2 + 40, startY);

        // draw back button at top-left
        drawBackButton(screen, false);
    }

    /**
     * Draws high scores.
     *
     * @param screen
     *                   Screen to draw on.
     * @param highScores
     *                   List of high scores.
     */
    public void drawHighScores(final Screen screen, final List<Score> highScores) { // add mode to parameter
        backBufferGraphics.setColor(Color.WHITE);
        int i = 0;
        String scoreString;

        int midX = screen.getWidth() / 2;
        int startY = screen.getHeight() / 3 + fontBigMetrics.getHeight() + 20;
        int lineHeight = fontRegularMetrics.getHeight() + 5;

        for (Score score : highScores) {
            scoreString = String.format("%s        %04d", score.getName(), score.getScore());
            int x = midX - fontRegularMetrics.stringWidth(scoreString) / 2;
            backBufferGraphics.drawString(scoreString, x, startY + lineHeight * i);
            i++;
        }
    }

    /**
     * Draws high scores.
     *
     * @param screen
     *                   Screen to draw on.
     * [2025-10-09] Added in commit: feat: complete drawAchievementMenu method in DrawManager
     */
    public void drawAchievementMenu(final Screen screen,
                                    Achievement achievement, List<String> completer, String numOfPages) {
        String achievementsTitle = "Achievements";
        String instructionsString = "Press ESC to return";
        String playerModeString = "              1P                                      2P              ";
        String prevNextString = "PREV                                                              NEXT";
        String achievementName = achievement.getName();
        String descriptionString = achievement.getDescription();

        // Draw the title, achievement name, and description
        backBufferGraphics.setColor(Color.GREEN);
        drawCenteredBigString(screen, achievementsTitle, screen.getHeight() / 10);
        drawCenteredRegularString(screen, achievementName, screen.getHeight() / 7);
        backBufferGraphics.setColor(Color.GRAY);
        drawCenteredRegularString(screen, descriptionString, screen.getHeight() / 5);
        backBufferGraphics.setColor(Color.GREEN);
        drawCenteredRegularString(screen, playerModeString, (screen.getHeight() / 4));
        backBufferGraphics.setColor(Color.GRAY);
        drawCenteredRegularString(screen, instructionsString, (int) (screen.getHeight() * 0.9));

        // Starting Y position for player names
        int startY = (int) (screen.getHeight() * 0.3);
        int lineHeight = 25;

        // X positions for the 1P and 2P columns
        int leftX = screen.getWidth() / 4;      // 1P column

        // Separate completer into 1P and 2P teams based on the mode prefix
        if (completer != null && !completer.isEmpty()) {

            List<String> team = completer.stream().map(s -> s.substring(2)).toList();

            // Draw names in each column, up to the max number of lines
            int maxLines = team.size();
            for (int i = 0; i < maxLines; i++) {
                int y = startY + i * lineHeight;
                backBufferGraphics.setColor(Color.WHITE);
                backBufferGraphics.drawString(team.get(i), leftX, y);
            }
        } else {
            // Display placeholder text if no achievers were found
            backBufferGraphics.setColor(Color.GREEN);
            drawCenteredBigString(screen, "No achievers found.", (int) (screen.getHeight() * 0.5));
        }
        backBufferGraphics.setColor(Color.GREEN);
        drawCenteredBigString(screen, numOfPages, (int) (screen.getHeight() * 0.8));
        // Draw prev/next navigation buttons at the bottom
        backBufferGraphics.setColor(Color.GREEN);
        drawCenteredRegularString(screen, prevNextString, (int) (screen.getHeight() * 0.8));

        // Draw back button at the top-left corner
        drawBackButton(screen, false);
    }



    public void drawSettingMenu(final Screen screen) {
        String settingsString = "Settings";
        String instructionsString = "Press ESC to return";

        backBufferGraphics.setColor(Color.GREEN);
        drawCenteredBigString(screen, settingsString, screen.getHeight() / 8);
        backBufferGraphics.setFont(fontRegular);
        backBufferGraphics.setColor(Color.GRAY);
        drawCenteredRegularString(screen, instructionsString, screen.getHeight() / 6);
    }

    public void drawKeysettings(final Screen screen, int selectedSection, int selectedKeyIndex, boolean[] keySelected,int[] currentKeys) {
        int panelWidth = 220;
        int panelHeight = 180;
        int x = screen.getWidth() - panelWidth - 50;
        int y = screen.getHeight() / 4;

        String[] labels = {"MOVE LEFT :", "MOVE RIGHT:", "ATTACK :"};
        String[] keys = new String[3];

        for (int i = 0; i < labels.length; i++) {
            int textY = y + 70 + (i * 50);
            keys[i] = KeyEvent.getKeyText(currentKeys[i]); // Convert set key codes to characters
            // draw the dividing line
            if (i < labels.length - 1) {
                backBufferGraphics.setColor(Color.DARK_GRAY);
                backBufferGraphics.drawLine(x + 20, textY + 20, x + panelWidth - 20, textY + 20);
            }

            // Verify that the current item is in key selection (waiting) status and select color
            if (keySelected[i]) {
                backBufferGraphics.setColor(Color.YELLOW);
            } else if (selectedSection == 1 && selectedKeyIndex == i) {
                backBufferGraphics.setColor(Color.GREEN);
            } else {
                backBufferGraphics.setColor(Color.LIGHT_GRAY);
            }
            // draw key
            backBufferGraphics.drawString(labels[i], x + 30, textY);
            backBufferGraphics.setColor(Color.WHITE);
            backBufferGraphics.drawString(keys[i], x + 150, textY);
        }

    }

    /**
     * Draws a centered string on regular font.
     *
     * @param screen
     *               Screen to draw on.
     * @param string
     *               String to draw.
     * @param height
     *               Height of the drawing.
     */
    public void drawCenteredRegularString(final Screen screen,
                                          final String string, final int height) {
        backBufferGraphics.setFont(fontRegular);
        backBufferGraphics.drawString(string, screen.getWidth() / 2
                - fontRegularMetrics.stringWidth(string) / 2, height);
    }

    /**
     * Draws a centered string on regular font at a specific coordinate.
     *
     * @param string
     *              String to draw.
     * @param x
     *              X coordinate to center the string on.
     * @param y
     *              Y coordinate of the drawing.
     */
    public void drawCenteredRegularString(final String string, final int x, final int y) {
        backBufferGraphics.setFont(fontRegular);
        backBufferGraphics.drawString(string, x - fontRegularMetrics.stringWidth(string) / 2, y);
    }

    /**
     * Draws a centered string on big font.
     *
     * @param screen
     *               Screen to draw on.
     * @param string
     *               String to draw.
     * @param height
     *               Height of the drawing.
     */
    public void drawCenteredBigString(final Screen screen, final String string,
                                      final int height) {
        backBufferGraphics.setFont(fontBig);
        backBufferGraphics.drawString(string, screen.getWidth() / 2
                - fontBigMetrics.stringWidth(string) / 2, height);
    }

    /**
     * Countdown to game start.
     *
     * @param screen
     *                  Screen to draw on.
     * @param level
     *                  Game difficulty level.
     * @param number
     *                  Countdown number.
     * @param bonusLife
     *                  Checks if a bonus life is received.
     */
    public void drawCountDown(final Screen screen, final int level,
                              final int number, final boolean bonusLife) {
        int rectWidth = screen.getWidth();
        int rectHeight = screen.getHeight() / 6;
        backBufferGraphics.setColor(Color.BLACK);
        backBufferGraphics.fillRect(0, screen.getHeight() / 2 - rectHeight / 2,
                rectWidth, rectHeight);
        backBufferGraphics.setColor(Color.GREEN);
        if (number >= 4)
            if (!bonusLife) {
                drawCenteredBigString(screen, "Level " + level,
                        screen.getHeight() / 2
                                + fontBigMetrics.getHeight() / 3);
            } else {
                drawCenteredBigString(screen, "Level " + level
                                + " - Bonus life!",
                        screen.getHeight() / 2
                                + fontBigMetrics.getHeight() / 3);
            }
        else if (number != 0)
            drawCenteredBigString(screen, Integer.toString(number),
                    screen.getHeight() / 2 + fontBigMetrics.getHeight() / 3);
        else
            drawCenteredBigString(screen, "GO!", screen.getHeight() / 2
                    + fontBigMetrics.getHeight() / 3);
    }

    public void drawNewHighScoreNotice(final Screen screen) {
//        String message = "NEW HIGH SCORE!";
//        backBufferGraphics.setColor(Color.YELLOW);
//        drawCenteredBigString(screen, message, screen.getHeight() / 4);
    }


	/**
	 * Draws achievement toasts.
	 *
	 * @param screen
	 * Screen to draw on.
	 * @param toasts
	 * List of toasts to draw.
	 */
	public void drawAchievementToasts(final Screen screen, final List<Achievement> toasts) {
		if (toasts == null || toasts.isEmpty()) {
			return;
		}

		Achievement achievement = toasts.getLast();

		Graphics2D g2d = (Graphics2D) backBufferGraphics.create();

		try {
			int boxWidth = 350;
			int boxHeight = 110;
			int cornerRadius = 15;

			int x = (screen.getWidth() - boxWidth) / 2;
			int y = (screen.getHeight() - boxHeight) / 2;

			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
			g2d.setColor(Color.BLACK);
			g2d.fillRoundRect(x, y, boxWidth, boxHeight, cornerRadius, cornerRadius);

			g2d.setColor(Color.GREEN);
			g2d.setStroke(new BasicStroke(2));
			g2d.drawRoundRect(x, y, boxWidth, boxHeight, cornerRadius, cornerRadius);

			g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));

			g2d.setFont(fontBig);
			g2d.setColor(Color.YELLOW);
			FontMetrics bigMetrics = g2d.getFontMetrics(fontBig);
			int titleWidth = bigMetrics.stringWidth("Achievement Clear!");
			g2d.drawString("Achievement Clear!", (screen.getWidth() - titleWidth) / 2, y + 35);

			g2d.setFont(fontRegular);
			g2d.setColor(Color.WHITE);
			FontMetrics regularMetrics = g2d.getFontMetrics(fontRegular);
			int nameWidth = regularMetrics.stringWidth(achievement.getName());
			g2d.drawString(achievement.getName(), (screen.getWidth() - nameWidth) / 2, y + 60);

			g2d.setColor(Color.LIGHT_GRAY);

			if (achievement.getDescription().length() < 30) {
				int descWidth = regularMetrics.stringWidth(achievement.getDescription());
				g2d.drawString(achievement.getDescription(), (screen.getWidth() - descWidth) / 2, y + 80 + regularMetrics.getHeight()/2);
			} else {
				// 30 characters or more to handle the wrap
				String line1 = achievement.getDescription().substring(0, achievement.getDescription().length()/2);
				String line2 = achievement.getDescription().substring(achievement.getDescription().length()/2);

				// first line
				int line1Widgh = regularMetrics.stringWidth(line1);
				g2d.drawString(line1, (screen.getWidth() - line1Widgh) / 2, y + 80);

				// second line
				int line2Widgh = regularMetrics.stringWidth(line2);
				g2d.drawString(line2, (screen.getWidth() - line2Widgh) / 2, y + 80 + regularMetrics.getHeight());
			}
		} finally {
			g2d.dispose();
		}
	}

    /**
     * Draws the play mode selection menu (1P / 2P / Back).
     *
     * @param screen
     *                  Screen to draw on.
     * @param selectedIndex
     *                  Currently selected option (0 = 1P, 1 = 2P, 2 = Back).
     */
    // Modify to accept hoverIndex for highlighting
    public void drawPlayMenu(final Screen screen, final Integer hoverOption, final int selectedIndex) {
        String[] items = {"1 Player", "2 Players"};
        // Removed center back button

        // draw back button at top-left corner\, Set the selectedIndex to Highlight the Back Button
        drawBackButton(screen, selectedIndex == 2);

        int baseY = screen.getHeight() / 2 - 20; // Modified the position with the choice reduced to two
        for (int i = 0; i < items.length; i++) {
            boolean highlight = (hoverOption != null) ? (i == hoverOption) : (i == selectedIndex);
            backBufferGraphics.setColor(highlight ? Color.GREEN : Color.WHITE);
            drawCenteredRegularString(screen, items[i],
                    baseY + fontRegularMetrics.getHeight() * 3 * i);
        }
    }

    // Draw a "BACK_LABEL" button at the top-left corner.
    public void drawBackButton(final Screen screen, final boolean highlighted) {
        backBufferGraphics.setFont(fontRegular);
        backBufferGraphics.setColor(highlighted ? Color.GREEN : Color.WHITE);

        int margin = 12;
        int ascent = fontRegularMetrics.getAscent();
        backBufferGraphics.drawString(BACK_LABEL, margin, margin + ascent);
    }

    // add this line
    // hitbox coordinate function
    // [Refactor] unified hitbox logic to match drawMenu() for consistency
    public Rectangle[] getMenuHitboxes (final Screen screen) {
        if (fontRegularMetrics == null) {
            backBufferGraphics.setFont(fontRegular);
            fontRegularMetrics = backBufferGraphics.getFontMetrics(fontRegular);
        }

        final String[] buttons = {"Play", "Achievement", "High scores", "Settings", "Exit"};

        int baseY = screen.getHeight() / 3 * 2 - 20;
        int spacing = (int) (fontRegularMetrics.getHeight() * 1.5);
        Rectangle[] boxes= new Rectangle[buttons.length];

        for (int i = 0; i < buttons.length; i++) {
            int baseline = baseY + spacing * i;
            boxes[i] = centeredStringBounds(screen, buttons[i], baseline);
        }

        return boxes;
    }

    // hitbox for Back button
    public Rectangle getBackButtonHitbox (final Screen screen) {
        if  (fontRegularMetrics == null) {
            backBufferGraphics.setFont(fontRegular);
            fontRegularMetrics = backBufferGraphics.getFontMetrics(fontRegular);
        }

        int margin = 12;
        int ascent = fontRegularMetrics.getAscent();
        int descent = fontRegularMetrics.getDescent();
        int padTop = 2;

        int y = margin - padTop;
        int w = fontRegularMetrics.stringWidth(BACK_LABEL);
        int h = ascent + descent + 25;

        return new Rectangle(margin, y, w, h);
    }

    public Rectangle getPlayMenuHitboxes(final Screen screen) {
        if (fontRegularMetrics == null) {
            backBufferGraphics.setFont(fontRegular);
            fontRegularMetrics = backBufferGraphics.getFontMetrics(fontRegular);
        }

        final String items = "1 Player";
        int baseY = screen.getHeight() / 2 - 20;
        int baselineY = baseY + fontRegularMetrics.getHeight() * 3;
        return centeredStringBounds(screen, items, baselineY);
    }

    public void drawShipSelectionMenu(final Screen screen, final PlayerShip[] playerShipExamples, final int selectedShipIndex) {
        PlayerShip playerShip = playerShipExamples[selectedShipIndex];

        String screenTitle = "PLAYER " + " : CHOOSE YOUR SHIP";

        // Ship Type Info
        String[] shipNames = {"Normal Type", "Big Shot Type", "Double Shot Type", "Speed Type"};
        String[] shipSpeeds = {"SPEED: NORMAL", "SPEED: SLOW", "SPEED: SLOW", "SPEED: FAST"};
        String[] shipFireRates = {"FIRE RATE: NORMAL", "FIRE RATE: NORMAL", "FIRE RATE: NORMAL", "FIRE RATE: SLOW"};

        drawEntity(playerShip, playerShip.getPositionX() - playerShip.getWidth()/2, playerShip.getPositionY());
//        for (int i = 0; i < 4; i++) {
//            // Draw Player Ship
//            drawManager.drawEntity(ship, ship.getPositionX() - ship.getWidth()/2, ship.getPositionY());
//        }

        // Draw Selected Player Page Title
        backBufferGraphics.setColor(Color.GREEN);
        drawCenteredBigString(screen, screenTitle, screen.getHeight() / 4);
        // Draw Selected Player Ship Type
        backBufferGraphics.setColor(Color.white);
        drawCenteredRegularString(screen, " > " + shipNames[selectedShipIndex] + " < ", screen.getHeight() / 2 - 40);
        // Draw Selected Player Ship Info
        backBufferGraphics.setColor(Color.WHITE);
//        drawCenteredRegularString(shipSpeeds[selectedShipIndex], centerX, screen.getHeight() / 2 + 60);
//        drawCenteredRegularString(shipFireRates[selectedShipIndex], centerX, screen.getHeight() / 2 + 80);
        drawCenteredRegularString(screen, shipSpeeds[selectedShipIndex], screen.getHeight() / 2 + 60);
        drawCenteredRegularString(screen, shipFireRates[selectedShipIndex], screen.getHeight() / 2 + 80);

        backBufferGraphics.setColor(Color.GRAY);
        drawCenteredRegularString(screen, "Press SPACE to Select", screen.getHeight() - 50);
    }

    /*
    When a given string is aligned in the middle of the screen,
    the pixel area occupied by the string is calculated as Rectangle and returned
     */
    private Rectangle centeredStringBounds(final Screen screen, final String string, final int baselineY) {
        backBufferGraphics.setFont(fontRegular);
        final int pad = 4;

        int textWidth = fontRegularMetrics.stringWidth(string);
        int ascent = fontRegularMetrics.getAscent();
        int descent = fontRegularMetrics.getDescent();

        int x = screen.getWidth() / 2 - textWidth / 2;
        int y = baselineY - ascent + 20 - pad / 2;
        int h = ascent + descent + pad;

        return new Rectangle(x, y, textWidth, h);
    }

    public void drawVolumeBar(final Screen screen, final int volumlevel, final boolean dragging){
        int bar_startWidth = screen.getWidth() / 2;
        int bar_endWidth = screen.getWidth()-40;
        int barHeight = screen.getHeight()*3/10;

        String volumelabel = "Volume";
        backBufferGraphics.setFont(fontRegular);
        backBufferGraphics.setColor(Color.WHITE);
        backBufferGraphics.drawLine(bar_startWidth, barHeight, bar_endWidth, barHeight);

        backBufferGraphics.setColor(Color.WHITE);
        backBufferGraphics.drawString(volumelabel, bar_startWidth-80, barHeight+7);

//		change this line to get indicator center position
        int size = 14;
        double ratio = volumlevel / 100.0;
        int centerX = bar_startWidth + (int) ((bar_endWidth - bar_startWidth) * ratio);
        int indicatorX = centerX - size / 2 - 3;
        int indicatorY = barHeight - size / 2 ;

        int rawX = Core.getInputManager().getMouseX();
        int rawY = Core.getInputManager().getMouseY();
        Insets insets = frame.getInsets();
        int mouseX = rawX - insets.left;
        int mouseY = rawY - insets.top;

        boolean hoverIndicator = mouseX >= indicatorX && mouseX <= indicatorX + size &&
                mouseY >= indicatorY && mouseY <= indicatorY + size;

        if (hoverIndicator || dragging) {
            backBufferGraphics.setColor(Color.GREEN);
        } else {
            backBufferGraphics.setColor(Color.WHITE);
        }

        backBufferGraphics.fillRect(indicatorX, indicatorY, size, size);

        backBufferGraphics.setColor(Color.WHITE);
        String volumeText = Integer.toString(volumlevel);
        backBufferGraphics.drawString(volumeText, bar_endWidth+10, barHeight +7);

    }

    public void drawSettingLayout(final Screen screen, final String[] menuItems, final int selectedmenuItems) {
        int splitPointX = screen.getWidth() *3/10;
        backBufferGraphics.setFont(fontRegular);
        int menuY = screen.getHeight()*3/10;
        for (int i = 0; i < menuItems.length; i++) {
            if (i == selectedmenuItems) {
                backBufferGraphics.setColor(Color.GREEN);
            }
            else {
                backBufferGraphics.setColor(Color.WHITE);
            }
            backBufferGraphics.drawString(menuItems[i], 30, menuY+(i*60));
            backBufferGraphics.setColor(Color.GREEN);
        }
        backBufferGraphics.drawLine(splitPointX, screen.getHeight()/4, splitPointX,(menuY+menuItems.length*60));
    }

    public Rectangle getVolumeBarHitbox(final Screen screen){
        int bar_startWidth = screen.getWidth() / 2;
        int bar_endWidth = screen.getWidth() - 40;
        int barHeight = screen.getHeight() * 3 / 10;

        int barThickness = 20;

        //	int for adjust volume hitbox
        int volumeHitBoxOffset = 20;
        int centerY = barHeight + volumeHitBoxOffset;

        int y = centerY - barThickness;
        int width = bar_endWidth - bar_startWidth;
        int height = barThickness * 2;

        return new Rectangle(bar_startWidth, y, width, height);
    }

    public void drawHpBar(int x, int y, int width, int height, int currentHp, int maxHp, boolean isEnemy) {

        if (maxHp <= 0) return;

        float ratio = currentHp / (float) maxHp;
        ratio = Math.max(0f, Math.min(1f, ratio)); // 0~1 사이로 클램프

        backBufferGraphics.setColor(Color.DARK_GRAY);
        backBufferGraphics.fillRect(x, y, width, height);

        if (isEnemy)
            backBufferGraphics.setColor(Color.RED);
        else
            backBufferGraphics.setColor(Color.GREEN);

        int filledWidth = (int) (width * ratio);
        backBufferGraphics.fillRect(x, y, filledWidth, height);

        backBufferGraphics.setColor(Color.BLACK);
        backBufferGraphics.drawRect(x, y, width, height);
    }
}