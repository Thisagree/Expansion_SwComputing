package main.screen;

import java.awt.event.KeyEvent;
import main.engine.Cooldown;
import main.engine.Core;

/**
 *
 * Implements the PlayScreen
 *
 */

public class PlayScreen extends Screen {
    private static final int SELECTION_TIME = 200;
    private final Cooldown selectionCooldown;
    private int menuIndex = 0; // 0 = 1P, 1 = 2P, 2 = Back

/**
 * Constructor, establishes the properties of the main.screen.
 *
 * @param width  Screen width.
 * @param height Screen height.
 * @param fps    Frames per second, frame rate at which the game is run.
 *  **/

    public PlayScreen(final int width, final int height, final int fps) {
        super(width, height, fps);
        this.returnCode = 2; // default 1P
        this.selectionCooldown = Core.getCooldown(SELECTION_TIME);
        this.selectionCooldown.reset();
    }

    public final int run() {
        super.run();
        return this.returnCode;
    }

    protected final void update() {
        super.update();
        draw();

        if (this.selectionCooldown.checkFinished() && this.inputDelay.checkFinished()) {
            if (inputManager.isKeyDown(KeyEvent.VK_UP) || inputManager.isKeyDown(KeyEvent.VK_W)) {
                this.menuIndex = (this.menuIndex + 2) % 3; // UP
                this.selectionCooldown.reset();
            }
            if (inputManager.isKeyDown(KeyEvent.VK_DOWN) || inputManager.isKeyDown(KeyEvent.VK_S)) {
                this.menuIndex = (this.menuIndex + 1) % 3; // DOWN
                this.selectionCooldown.reset();
            }

            // back button click event & 1P, 2P button click event
            if (inputManager.isKeyDown(KeyEvent.VK_SPACE)) {
                switch (this.menuIndex) {
                    case 0: // "1 Player"
                        this.returnCode = 2; // go to GameScreen
                        break;

                    case 1: // "Back"
                        this.returnCode = 1; // go back to TitleScreen
                        break;
                }
                this.isRunning = false;
            }
            if (inputManager.isMouseClicked()) {
                int mx = inputManager.getMouseX();
                int my = inputManager.getMouseY();

                java.awt.Rectangle backBox = drawManager.getBackButtonHitbox(this);
                java.awt.Rectangle modeBox = drawManager.getPlayMenuHitboxes(this);
                java.awt.Rectangle[] allBoxes = { modeBox, backBox };

                for  (int i = 0; i < allBoxes.length; i++) {
                    if (allBoxes[i].contains(mx, my)) {
                        this.menuIndex = i;
                        this.returnCode = 2;
                        this.isRunning = false;
                        return;
                    }
                }

            }
        }
    }

    private void draw() {
        drawManager.initDrawing(this);

        // hover highlight
        int mx = inputManager.getMouseX();
        int my = inputManager.getMouseY();

        java.awt.Rectangle modeBox = drawManager.getPlayMenuHitboxes(this);
        java.awt.Rectangle backBox = drawManager.getBackButtonHitbox(this);
        java.awt.Rectangle[] allBoxes = { modeBox, backBox };

        for (int i = 0; i < allBoxes.length; i++) {
            if (allBoxes[i].contains(mx, my)) {
                this.menuIndex = i;
                break;
            }
        }

        drawManager.drawPlayMenu(this, this.menuIndex==2 ? -1 : this.menuIndex, this.menuIndex);
        drawManager.drawBackButton(this, this.menuIndex==2);
        drawManager.completeDrawing(this);
    }

}


