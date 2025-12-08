package main.screen;

import main.engine.Achievement;
import main.engine.AchievementManager;
import main.engine.Core;
import main.engine.FileManager;

import java.awt.*;
import java.awt.event.KeyEvent;
import main.engine.SoundManager;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class AchievementScreen extends Screen {
    private static final int ROWS_PER_COLUMN = 7;
    private static final int PAGE = ROWS_PER_COLUMN * 2;
    private final FileManager fileManager;
    private final List<Achievement> achievements;
    private List<String> completer;
    private List<String> completer1P;
    private int currentIdx = 0;
    private int currentPage = 0;
    private int maxPage;
    private String numOfPages;

    public AchievementScreen(final int width, final int height, final int fps) {
        super(width, height, fps);
        AchievementManager achievementManager = Core.getAchievementManager();
        achievements = achievementManager.getAchievements();
        fileManager = Core.getFileManager();
        this.completer = Core.getFileManager().getAchievementCompleter(achievements.get(currentIdx));
        this.returnCode = 3;
        updateCompleterData();
        // Start menu music loop when the achievement main.screen is created
        SoundManager.playLoop("sound/menu_sound.wav");
    }

    public final int run() {
        super.run();
        // Stop menu music when leaving the achievement main.screen
        SoundManager.stop();

        return this.returnCode;
    }

    protected final void update() {

        // [2025-10-17] feat: Added key input logic to navigate achievements
        // When the right or left arrow key is pressed, update the current achievement index
        // and reload the completer list for the newly selected achievement.
        if (inputManager.isKeyDown(KeyEvent.VK_RIGHT) && inputDelay.checkFinished()) {
            moveAchievement(1);
            inputDelay.reset();
        }
        if (inputManager.isKeyDown(KeyEvent.VK_LEFT) && inputDelay.checkFinished()) {
            moveAchievement(-1);
            inputDelay.reset();
        }

        // [2025-10-20] feat: Added key input logic to navigate achievement pages
        // When the up or down arrow key is pressed, switch between pages and update page display text.
        if (inputManager.isKeyDown(KeyEvent.VK_UP) && inputDelay.checkFinished()) {
            if (currentPage > 0) currentPage--;
            inputDelay.reset();
        }

        if (inputManager.isKeyDown(KeyEvent.VK_DOWN) && inputDelay.checkFinished()) {
            if (currentPage < maxPage) currentPage++;
            inputDelay.reset();
        }

        this.numOfPages = (currentPage+1) + " / " + (maxPage+1);

        super.update();
        draw();

        if (inputManager.isKeyDown(KeyEvent.VK_ESCAPE) && this.inputDelay.checkFinished()) {
            this.returnCode = 1;
            this.isRunning = false;
        }

        // button click event
        if (inputManager.isMouseClicked()) {
            int mx = inputManager.getMouseX();
            int my = inputManager.getMouseY();
            Rectangle backBox = drawManager.getBackButtonHitbox(this);
            if (backBox.contains(mx, my)) {
                this.returnCode = 1;
                this.isRunning = false;
            }
        }
    }

    private void draw() {
        drawManager.initDrawing(this);

        int start = currentPage * PAGE;

        int end = Math.min(start + PAGE, completer1P.size());
        List<String> page1P = (start < completer1P.size()) ?
                completer1P.subList(start, end) : Collections.emptyList();

        drawManager.drawAchievementMenu(this, achievements.get(currentIdx), page1P, numOfPages);

        // hover highlight
        int mx = inputManager.getMouseX();
        int my = inputManager.getMouseY();
        Rectangle backBox = drawManager.getBackButtonHitbox(this);

        if (backBox.contains(mx, my)) {
            drawManager.drawBackButton(this, true);
        }

        drawManager.completeDrawing(this);
    }

    private void updateCompleterData(){
        this.completer1P = completer.stream().filter(s -> s.startsWith("1:")).collect(Collectors.toList());
        this.currentPage = 0;
        this.maxPage = Math.max(0, (int) Math.ceil((double) completer1P.size() / PAGE) - 1);
    }
    // reload the completer list for the newly selected achievement.
    private void moveAchievement(int delta) {
        currentIdx = (currentIdx + delta + achievements.size()) % achievements.size();
        completer = fileManager.getAchievementCompleter(achievements.get(currentIdx));
        updateCompleterData();
    }
}
