package main.engine;

import java.util.Arrays;

/**
 * Implements a high score record.
 *
 * @author <a href="mailto:RobertoIA1987@gmail.com">Roberto Izquierdo Amo</a>
 *
 */
public class Score implements Comparable<Score> {

    /** Player's name. */
    private final String name;
    /** Score points. */
    private final int score;

    /** per-player breakdown */
    private int playerScores;
    private int playerBullets;
    private int playerKills;


    /**level reached and lives left */
    private int levelReached;
    private int livesRemaining;

    /**
     * Constructor.
     *
     * @param name  Player name, three letters.
     * @param score Player score.
     */
    public Score(final String name, final int score) {
        this.name = name;
        this.score = score;
    }

    /**
     * NEW Constructor: (team co-op)
     */
    public Score(final String name, final GameState gs) {
        this.name = name;
        this.score = gs.getScore();
        this.levelReached = gs.getLevel();
        this.livesRemaining = gs.getPlayerShip().getStats().getCurHP();
        this.playerScores = gs.getScore();
        this.playerBullets = gs.getBulletsShot();
        this.playerKills = gs.getShipsDestroyed();
    }

    /**
     * Getter for the player's name.
     *
     * @return Name of the player.
     */
    public final String getName() {
        return this.name;
    }

    /**
     * Getter for the player's score.
     *
     * @return High score.
     */
    public final int getScore() {
        return this.score;
    }

    /**
     * Getter for:
     * level reached
     * lives left
     *
     * per-player breakdown
     */
    public final int getLevelReached() {
        return this.levelReached;
    }

    public final int getLivesRemaining() {
        return this.livesRemaining;
    }

    // Per-player (null-safe for legacy scores)
    public final int getPlayerScore() {
        return playerScores;
    }

    public final int getPlayerBullets() { return playerBullets; }

    public final int getPlayerKills() {
        return playerKills;
    }

    /**
     * Orders the scores descending by score.
     *
     * @param other
     *              Score to compare the current one with.
     * @return Comparison between the two scores. Positive if the current one is
     *         smaller, positive if its bigger, zero if it's the same.
     */

    @Override
    public final int compareTo(final Score other) {
        return Integer.compare(other.getScore(), this.score); // descending
    }

    @Override
    public String toString() {
        return "Score{name='" + name + "', score=" + score +
                ", perPlayer=" + playerScores + "}";
    }

}