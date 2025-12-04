package main.screen;

import main.engine.Cooldown;
import main.engine.Core;
import main.engine.ShipUpgradeManager;
import main.engine.ShipUpgradeType;
import main.engine.SoundManager;
import main.engine.DrawManager.SpriteType;

import java.awt.event.KeyEvent;
import java.util.List;

/**
 * Screen that allows players to upgrade ships using coins.
 */
public class UpgradeScreen extends Screen {

    private static final int SELECTION_TIME = 200;
    private final ShipUpgradeManager upgradeManager;
    private final List<SpriteType> shipTypes;
    private int selectionIndex;
    private int shipIndex;
    private final Cooldown selectionCooldown;

    public UpgradeScreen(final int width, final int height, final int fps) {
        super(width, height, fps);
        this.upgradeManager = ShipUpgradeManager.getInstance();
        this.shipTypes = upgradeManager.getSupportedShips();
        this.selectionCooldown = Core.getCooldown(SELECTION_TIME);
        this.selectionCooldown.reset();
        this.selectionIndex = 0;
        this.shipIndex = 0;
    }

    private SpriteType getCurrentShip() {
        return shipTypes.get(shipIndex);
    }

    @Override
    public int run() {
        super.run();
        upgradeManager.saveToFile();
        return this.returnCode;
    }

    @Override
    protected void update() {
        super.update();
        draw();

        if (this.selectionCooldown.checkFinished() && this.inputDelay.checkFinished()) {
            handleInput();
        }
    }

    private void handleInput() {
        if (inputManager.isKeyDown(KeyEvent.VK_ESCAPE)) {
            SoundManager.playOnce("sound/select.wav");
            this.returnCode = 1;
            this.isRunning = false;
            return;
        }

        if (inputManager.isKeyDown(KeyEvent.VK_UP) || inputManager.isKeyDown(KeyEvent.VK_W)) {
            SoundManager.playOnce("sound/hover.wav");
            selectionIndex = (selectionIndex + 5) % 6;
            this.selectionCooldown.reset();
        }
        if (inputManager.isKeyDown(KeyEvent.VK_DOWN) || inputManager.isKeyDown(KeyEvent.VK_S)) {
            SoundManager.playOnce("sound/hover.wav");
            selectionIndex = (selectionIndex + 1) % 6;
            this.selectionCooldown.reset();
        }

        if (selectionIndex == 0) {
            if (inputManager.isKeyDown(KeyEvent.VK_RIGHT) || inputManager.isKeyDown(KeyEvent.VK_D)) {
                shipIndex = (shipIndex + 1) % shipTypes.size();
                SoundManager.playOnce("sound/select.wav");
                this.selectionCooldown.reset();
            }
            if (inputManager.isKeyDown(KeyEvent.VK_LEFT) || inputManager.isKeyDown(KeyEvent.VK_A)) {
                shipIndex = (shipIndex + shipTypes.size() - 1) % shipTypes.size();
                SoundManager.playOnce("sound/select.wav");
                this.selectionCooldown.reset();
            }
            return;
        }

        if (selectionIndex >= 1 && selectionIndex <= 4) {
            if (inputManager.isKeyDown(KeyEvent.VK_RIGHT) || inputManager.isKeyDown(KeyEvent.VK_D)) {
                ShipUpgradeType type = ShipUpgradeType.values()[selectionIndex - 1];
                if (upgradeManager.upgradeStat(getCurrentShip(), type)) {
                    SoundManager.playOnce("sound/select.wav");
                } else {
                    SoundManager.playOnce("sound/hover.wav");
                }
                this.selectionCooldown.reset();
            }
        } else if (selectionIndex == 5) {
            if (inputManager.isKeyDown(KeyEvent.VK_RIGHT) || inputManager.isKeyDown(KeyEvent.VK_D)) {
                upgradeManager.resetShip(getCurrentShip());
                SoundManager.playOnce("sound/select.wav");
                this.selectionCooldown.reset();
            }
        }
    }

    private void draw() {
        drawManager.initDrawing(this);
        // hover highlight
        int mx = inputManager.getMouseX();
        int my = inputManager.getMouseY();
        java.awt.Rectangle backBox = drawManager.getBackButtonHitbox(this);
        if (backBox.contains(mx, my)) {
            drawManager.drawBackButton(this, true);
        }
        drawManager.drawUpgradeScreen(this, shipTypes, shipIndex, selectionIndex, upgradeManager);
        drawManager.completeDrawing(this);
    }
}