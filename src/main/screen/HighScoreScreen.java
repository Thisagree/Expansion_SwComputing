package main.screen;

import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.List;
import main.engine.Core;
import main.engine.Score;
import main.engine.SoundManager;

/**
 * Implements the high scores main.screen, it shows player records.
 *
 * @author <a href="mailto:RobertoIA1987@gmail.com">Roberto Izquierdo Amo</a>
 *
 */
public class HighScoreScreen extends Screen {

    /** List of past high scores. */
    private List<Score> highScores;

    /**
     * Constructor, establishes the properties of the main.screen.
     *
     * @param width
     *            Screen width.
     * @param height
     *            Screen height.
     * @param fps
     *            Frames per second, frame rate at which the game is run.
     */
    public HighScoreScreen(final int width, final int height, final int fps) {
        super(width, height, fps);
        SoundManager.playLoop("sound/menu_sound.wav");

        this.returnCode = 1;

        try {
            this.highScores = Core.getFileManager().loadHighScores();
            //상위 7명만 남기기
            highScores.sort((a, b) -> b.getScore() - a.getScore());
            if (highScores.size() > 7)
                highScores = highScores.subList(0, 7);

        } catch (NumberFormatException | IOException e) {
            logger.warning("Couldn't load high scores!");
        }
    }

    /**
     * Starts the action.
     *
     * @return Next main.screen code.
     */
    public final int run() {
        super.run();
        SoundManager.playOnce("sound/select.wav");

        return this.returnCode;
    }

    /**
     * Updates the elements on main.screen and checks for events.
     */
    protected final void update() {
        super.update();

        draw();
        if (inputManager.isKeyDown(KeyEvent.VK_ESCAPE) && this.inputDelay.checkFinished())
            this.isRunning = false;

        // back button click event
        if (inputManager.isMouseClicked()) {
            int mx = inputManager.getMouseX();
            int my = inputManager.getMouseY();
            java.awt.Rectangle backBox = drawManager.getBackButtonHitbox(this);

            if (backBox.contains(mx, my)) {
                this.returnCode = 1;
                this.isRunning = false;
            }
        }
    }
    /**
     * Draws the elements associated with the main.screen.
     */
    private void draw() {
        drawManager.initDrawing(this);
        drawManager.drawHighScoreMenu(this);
        drawManager.drawHighScores(this, highScores); // Left column

        // hover highlight
        int mx = inputManager.getMouseX();
        int my = inputManager.getMouseY();
        java.awt.Rectangle backBox = drawManager.getBackButtonHitbox(this);

        if (backBox.contains(mx, my)) {
            drawManager.drawBackButton(this, true);
        }

        drawManager.completeDrawing(this);
    }
}