
package main.screen;

import java.awt.event.KeyEvent;

public class UpgradeScreen extends Screen {
    public UpgradeScreen(final int width, final int height, final int fps) {
        super(width, height, fps);
        this.returnCode = 7;
    }
    public final int run() {
        super.run();
        return this.returnCode;
    }
    protected final void update() {
        super.update();
        draw();

        if (inputManager.isKeyDown(KeyEvent.VK_BACK_SPACE) && this.inputDelay.checkFinished()) {
            this.returnCode = 1;
            this.isRunning = false;
        }
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
    private void draw() {
        drawManager.initDrawing(this);
        drawManager.drawUpgradeMenu(this);
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