package main.screen;

import java.awt.event.KeyEvent;
import main.engine.Cooldown;
import main.engine.Core;
import main.engine.DrawManager;
import main.entity.Player.PlayerShip;

public class ShipSelectionScreen extends Screen {

    private static final int SELECTION_TIME = 200;
    private final Cooldown selectionCooldown;
    private int selectedShipIndex = 0; // 0: NORMAL, 1: BIG_SHOT, 2: DOUBLE_SHOT, 3: MOVE_FAST
    private final PlayerShip[] playerShipExamples = new PlayerShip[4];

    private boolean backSelected = false; // If current state is on the back button, can't select ship

    public ShipSelectionScreen(final int width, final int height, final int fps) {
        super(width, height, fps);
        this.selectionCooldown = Core.getCooldown(SELECTION_TIME);
        this.selectionCooldown.reset();

        playerShipExamples[0] = new PlayerShip(width / 2 - 100, height / 2, DrawManager.SpriteType.Normal, null);
        playerShipExamples[1] = new PlayerShip(width / 2 - 35, height / 2, DrawManager.SpriteType.BigShot, null);
        playerShipExamples[2] = new PlayerShip(width / 2 + 35, height / 2, DrawManager.SpriteType.DoubleShot, null);
        playerShipExamples[3] = new PlayerShip(width / 2 + 100, height / 2, DrawManager.SpriteType.MoveFast, null);
    }

    /**
     * Returns the selected ship type to Core.
     *
     * @return The selected ShipType enum.
     */
    public DrawManager.SpriteType getSelectedShipType() {
        return switch (this.selectedShipIndex) {
            case 1 -> DrawManager.SpriteType.BigShot;
            case 2 -> DrawManager.SpriteType.DoubleShot;
            case 3 -> DrawManager.SpriteType.MoveFast;
            default -> DrawManager.SpriteType.Normal;
        };
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
                backSelected = true;
                selectionCooldown.reset();
            }
            if (inputManager.isKeyDown(KeyEvent.VK_DOWN) ||  inputManager.isKeyDown(KeyEvent.VK_S)) {
                backSelected = false;
                selectionCooldown.reset();
            }
            if (!backSelected) {
                if (inputManager.isKeyDown(KeyEvent.VK_LEFT) || inputManager.isKeyDown(KeyEvent.VK_A)) {
                    this.selectedShipIndex = this.selectedShipIndex - 1;
                    if (this.selectedShipIndex < 0) {
                        this.selectedShipIndex += 4;
                    }
                    this.selectedShipIndex = this.selectedShipIndex % 4;
                    this.selectionCooldown.reset();
                }
                if (inputManager.isKeyDown(KeyEvent.VK_RIGHT) || inputManager.isKeyDown(KeyEvent.VK_D)) {
                    this.selectedShipIndex = (this.selectedShipIndex + 1) % 4;
                    this.selectionCooldown.reset();
                }
            }
            if (inputManager.isKeyDown(KeyEvent.VK_SPACE)) {
                this.returnCode = backSelected ? 1 : 2;
                this.isRunning = false;
            }
            int mx = inputManager.getMouseX();
            int my = inputManager.getMouseY();
            boolean clicked = inputManager.isMouseClicked();

            java.awt.Rectangle backBox = drawManager.getBackButtonHitbox(this);

            if (clicked && backBox.contains(mx, my)) {
                this.returnCode = 1;
                this.isRunning = false;

            }
        }
    }

    private void draw() {
        drawManager.initDrawing(this);

        drawManager.drawShipSelectionMenu(this, playerShipExamples, this.selectedShipIndex);

        // hover highlight
        int mx = inputManager.getMouseX();
        int my = inputManager.getMouseY();
        java.awt.Rectangle backBox = drawManager.getBackButtonHitbox(this);
        boolean backHover = backBox.contains(mx, my);
        drawManager.drawBackButton(this, backHover || backSelected);

        drawManager.completeDrawing(this);
    }
}