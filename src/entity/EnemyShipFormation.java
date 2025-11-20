package entity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import engine.*;
import screen.Screen;
import engine.DrawManager.SpriteType;

/**
 * Groups enemy ships into a formation that moves together.
 *
 * @author <a href="mailto:RobertoIA1987@gmail.com">Roberto Izquierdo Amo</a>
 *
 */
public class EnemyShipFormation implements Iterable<EnemyShip> {

    /** Initial position in the x-axis. */
    private static final int INIT_POS_X = 20;
    /** Initial position in the y-axis. */
    private static final int INIT_POS_Y = 100;
    /** Distance between ships. */
    private static final int SEPARATION_DISTANCE = 40;
    /** Proportion of C-type ships. */
    private static final double PROPORTION_C = 0.2;
    /** Proportion of B-type ships. */
    private static final double PROPORTION_B = 0.4;
    /** Lateral speed of the formation. */
    private static final int X_SPEED = 8;
    /** Downwards speed of the formation. */
    private static final int Y_SPEED = 4;
    /** Speed of the bullets shot by the members. */
    private static final int BULLET_SPEED = 4;
    /** Proportion of differences between shooting times. */
    private static final double SHOOTING_VARIANCE = .2;
    /** Margin on the sides of the screen. */
    private static final int SIDE_MARGIN = 20;
    /** Margin on the bottom of the screen. */
    private static final int BOTTOM_MARGIN = 80;
    /** Distance to go down each pass. */
    private static final int DESCENT_DISTANCE = 20;
    /** Minimum speed allowed. */
    private static final int MINIMUM_SPEED = 10;

    /** DrawManager instance. */
    private final DrawManager drawManager;
    /** Application logger. */
    private final Logger logger;
    /** Screen to draw ships on. */
    private Screen screen;

    /** List of enemy ships forming the formation. */
    private final List<List<EnemyShip>> enemyShips;
    /** Minimum time between shots. */
    private Cooldown shootingCooldown;
    /** Number of ships in the formation - horizontally. */
    private final int nShipsWide;
    /** Number of ships in the formation - vertically. */
    private final int nShipsHigh;
    /** Time between shots. */
    private final int shootingInterval;
    /** Variance in the time between shots. */
    private final int shootingVariance;
    /** Initial ship speed. */
    private final int baseSpeed;
    /** Speed of the ships. */
    private int movementSpeed;
    /** Current direction the formation is moving on. */
    private Direction currentDirection;
    /** Direction the formation was moving previously. */
    private Direction previousDirection;
    /** Interval between movements, in frames. */
    private int movementInterval;
    /** Total width of the formation. */
    private int width;
    /** Total height of the formation. */
    private int height;
    /** Position in the x-axis of the upper left corner of the formation. */
    private int positionX;
    /** Position in the y-axis of the upper left corner of the formation. */
    private int positionY;
    /** Width of one ship. */
    private final int shipWidth;
    /** Height of one ship. */
    private final int shipHeight;
    /** List of ships that are able to shoot. */
    private final List<EnemyShip> shooters;
    /** Number of not destroyed ships. */
    private int shipCount;
    /** Current level (for selecting attack pattern). */
    private final int level;

    // Pattern state
    /** Timestamp when patterns are allowed to start (ms). */
    private long patternStartTime = 0L;
    /** True after the first pattern has been activated. */
    private boolean patternActivated = false;
    /** Currently active attack pattern. */
    private PatternType currentPattern = PatternType.NONE;

    // Level 1
    /** Current column index for the wave pattern. */
    private int waveIndex = 0;
    /** Frame counter between wave shots. */
    private int waveFrameCounter = 0;
    /** Frames between wave shots (lower = faster). */
    private static final int WAVE_STEP_INTERVAL = 20;

    // Level 2
    /** Pair index for side wave (left / right column pair). */
    private int sidewavePairIndex = 0;

    // Level 3
    /** Frames between shots inside one focus group. */
    private static final int FOCUS_STEP_INTERVAL = 10;
    /** Frame counter for the focus pattern. */
    private int focusFrameCounter = 0;
    /** 0: left, 1: center, 2: right, -1: idle. */
    private int focusGroupIndex = -1;
    /** Index inside the currently selected group. */
    private int focusStepInGroup = 0;
    /** How many groups have already attacked (max 10). */
    private int focusGroupUsed = 0;
    /** Next time (ms) when a new group may start. */
    private long focusDelayUntil = 0L;

    // Level 4
    /** 0: step1 (normal columns), 1: step2 (normal again), 2: step3 (single columns fake) */
    private int randomBurstPhase = 0;
    /** How many full sets (0→1→2) have been completed (max 2). */
    private int randomBurstCycle = 0;
    /** Extra waits (number of cooldowns) before the next phase. */
    private int randomBurstWait = 0;
    /** Select attack pattern by level. */
    private PatternType selectPatternByLevel(int level) {
        switch (level) {
            case 1: return PatternType.WAVE;
            case 2: return PatternType.SIDEWAVE;
            case 3: return PatternType.FOCUS;
            case 4: return PatternType.RANDOMBURST;
            default: return PatternType.NONE;
        }
    }
    /** Enemy attack pattern types. */
    private enum PatternType {
        NONE,
        WAVE,
        SIDEWAVE,
        FOCUS,
        RANDOMBURST
    }
    /** Directions the formation can move. */
    private enum Direction {
        /** Movement to the right side of the screen. */
        RIGHT,
        /** Movement to the left side of the screen. */
        LEFT,
        /** Movement to the bottom of the screen. */
        DOWN
    };

    /**
     * Constructor, sets the initial conditions.
     *
     * @param gameSettings
     *            Current game settings.
     */
    public EnemyShipFormation(final GameSettings gameSettings, final int level) {
        this.level = level;
        this.drawManager = Core.getDrawManager();
        this.logger = Core.getLogger();
        this.enemyShips = new ArrayList<List<EnemyShip>>();
        this.currentDirection = Direction.RIGHT;
        this.movementInterval = 0;
        this.nShipsWide = gameSettings.getFormationWidth();
        this.nShipsHigh = gameSettings.getFormationHeight();
        this.shootingInterval = gameSettings.getShootingFrequency();
        this.shootingVariance = (int) (gameSettings.getShootingFrequency()
                * SHOOTING_VARIANCE);
        this.baseSpeed = gameSettings.getBaseSpeed();
        this.movementSpeed = this.baseSpeed;
        this.positionX = INIT_POS_X;
        this.positionY = INIT_POS_Y;
        this.shooters = new ArrayList<EnemyShip>();
        SpriteType spriteType;

        this.logger.info("Initializing " + nShipsWide + "x" + nShipsHigh
                + " ship formation in (" + positionX + "," + positionY + ")");

        // Each sub-list is a column on the formation.
        for (int i = 0; i < this.nShipsWide; i++)
            this.enemyShips.add(new ArrayList<EnemyShip>());

        for (List<EnemyShip> column : this.enemyShips) {
            for (int i = 0; i < this.nShipsHigh; i++) {
                if (i / (float) this.nShipsHigh < PROPORTION_C)
                    spriteType = SpriteType.EnemyShipC1;
                else if (i / (float) this.nShipsHigh < PROPORTION_B
                        + PROPORTION_C)
                    spriteType = SpriteType.EnemyShipB1;
                else
                    spriteType = SpriteType.EnemyShipA1;

                column.add(new EnemyShip((SEPARATION_DISTANCE
                        * this.enemyShips.indexOf(column))
                        + positionX, (SEPARATION_DISTANCE * i)
                        + positionY, spriteType));
                this.shipCount++;
            }
        }

        this.shipWidth = this.enemyShips.getFirst().getFirst().getWidth();
        this.shipHeight = this.enemyShips.getFirst().getFirst().getHeight();

        this.width = (this.nShipsWide - 1) * SEPARATION_DISTANCE
                + this.shipWidth;
        this.height = (this.nShipsHigh - 1) * SEPARATION_DISTANCE
                + this.shipHeight;

        for (List<EnemyShip> column : this.enemyShips)
            this.shooters.add(column.get(column.size() - 1));

        for (GameSettings.ChangeData changeData : gameSettings.getChangeDataList()){
            EnemyShip ship = this.enemyShips.get(changeData.x).get(changeData.y);

            if(changeData.hp == 0){
                destroy(ship);
            }
            else {
                ship.changeShip(changeData);
            }
        }

        List<EnemyShip> destroyed;
        for (List<EnemyShip> column : this.enemyShips) {
            destroyed = new ArrayList<EnemyShip>();
            for (EnemyShip ship : column) {
                if (ship != null && ship.isDestroyed()) {
                    destroyed.add(ship);
                    this.logger.info("Removed enemy "
                            + column.indexOf(ship) + " from column "
                            + this.enemyShips.indexOf(column));
                }
            }
            column.removeAll(destroyed);
        }
        this.patternStartTime = System.currentTimeMillis();
    }

    /**
     * Associates the formation to a given screen.
     *
     * @param newScreen
     *            Screen to attach.
     */
    public final void attach(final Screen newScreen) {
        screen = newScreen;
    }

    /**
     * Draws every individual component of the formation.
     */
    public final void draw() {
        for (List<EnemyShip> column : this.enemyShips)
            for (EnemyShip enemyShip : column)
                drawManager.drawEntity(enemyShip, enemyShip.getPositionX(),
                        enemyShip.getPositionY());
    }

    /**
     * Updates the position of the ships.
     */
    public final void update() {
        if(this.shootingCooldown == null) {
            this.shootingCooldown = Core.getVariableCooldown(shootingInterval,
                    shootingVariance);
            this.shootingCooldown.reset();
        }

        cleanUp();

        int movementX = 0;
        int movementY = 0;
        double remainingProportion = (double) this.shipCount
                / (this.nShipsHigh * this.nShipsWide);
        this.movementSpeed = (int) (Math.pow(remainingProportion, 2)
                * this.baseSpeed);
        this.movementSpeed += MINIMUM_SPEED;

        movementInterval++;
        if (movementInterval >= this.movementSpeed) {
            movementInterval = 0;

            boolean isAtBottom = positionY
                    + this.height > screen.getHeight() - BOTTOM_MARGIN;
            boolean isAtRightSide = positionX
                    + this.width >= screen.getWidth() - SIDE_MARGIN;
            boolean isAtLeftSide = positionX <= SIDE_MARGIN;
            boolean isAtHorizontalAltitude = positionY % DESCENT_DISTANCE == 0;

            if (currentDirection == Direction.DOWN) {
                if (isAtHorizontalAltitude)
                    if (previousDirection == Direction.RIGHT) {
                        currentDirection = Direction.LEFT;
                        this.logger.info("Formation now moving left 1");
                    } else {
                        currentDirection = Direction.RIGHT;
                        this.logger.info("Formation now moving right 2");
                    }
            } else if (currentDirection == Direction.LEFT) {
                if (isAtLeftSide)
                    if (!isAtBottom) {
                        previousDirection = currentDirection;
                        currentDirection = Direction.DOWN;
                        this.logger.info("Formation now moving down 3");
                    } else {
                        currentDirection = Direction.RIGHT;
                        this.logger.info("Formation now moving right 4");
                    }
            } else {
                if (isAtRightSide)
                    if (!isAtBottom) {
                        previousDirection = currentDirection;
                        currentDirection = Direction.DOWN;
                        this.logger.info("Formation now moving down 5");
                    } else {
                        currentDirection = Direction.LEFT;
                        this.logger.info("Formation now moving left 6");
                    }
            }

            if (currentDirection == Direction.RIGHT)
                movementX = X_SPEED;
            else if (currentDirection == Direction.LEFT)
                movementX = -X_SPEED;
            else
                movementY = Y_SPEED;

            positionX += movementX;
            positionY += movementY;

            // Cleans explosions.
            List<EnemyShip> destroyed;
            for (List<EnemyShip> column : this.enemyShips) {
                destroyed = new ArrayList<EnemyShip>();
                for (EnemyShip ship : column) {
                    if (ship != null && ship.isDestroyed()) {
                        destroyed.add(ship);
                        this.logger.info("Removed enemy "
                                + column.indexOf(ship) + " from column "
                                + this.enemyShips.indexOf(column));
                    }
                }
                column.removeAll(destroyed);
            }

            for (List<EnemyShip> column : this.enemyShips)
                for (EnemyShip enemyShip : column) {
                    enemyShip.move(movementX, movementY);
                    enemyShip.update();
                }
            // Start the special attack pattern once, 7 seconds after the level begins
            if (!patternActivated && System.currentTimeMillis() - patternStartTime > 7000) {
                currentPattern = selectPatternByLevel(level);
                patternActivated = true;
            }
        }
    }

    /**
     * Cleans empty columns, adjusts the width and height of the formation.
     */
    private void cleanUp() {
        Set<Integer> emptyColumns = new HashSet<Integer>();
        int maxColumn = 0;
        int minPositionY = Integer.MAX_VALUE;
        for (List<EnemyShip> column : this.enemyShips) {
            if (!column.isEmpty()) {
                // Height of this column
                int columnSize = column.getLast().positionY
                        - this.positionY + this.shipHeight;
                maxColumn = Math.max(maxColumn, columnSize);
                minPositionY = Math.min(minPositionY, column.getFirst()
                        .getPositionY());
            } else {
                // Empty column, we remove it.
                emptyColumns.add(this.enemyShips.indexOf(column));
            }
        }
        for (int index : emptyColumns) {
            this.enemyShips.remove(index);
            logger.info("Removed column " + index);
        }

        int leftMostPoint = 0;
        int rightMostPoint = 0;

        for (List<EnemyShip> column : this.enemyShips) {
            if (!column.isEmpty()) {
                if (leftMostPoint == 0)
                    leftMostPoint = column.getFirst().getPositionX();
                rightMostPoint = column.getFirst().getPositionX();
            }
        }

        this.width = rightMostPoint - leftMostPoint + this.shipWidth;
        this.height = maxColumn;

        this.positionX = leftMostPoint;
        this.positionY = minPositionY;
    }

    /**
     * Shoots from a random enemy ship after cooldown.
     * Bullet creation handled in spawnBulletFromShooter().
     */
    public final void shoot(final Set<Bullet> bullets) {
        if (this.shooters.isEmpty()) return;

        // Level 1/2 patterns: frame-based firing, ignore cooldown
        if (currentPattern == PatternType.WAVE || currentPattern == PatternType.SIDEWAVE) {
            waveFrameCounter++;

            if (waveFrameCounter >= WAVE_STEP_INTERVAL) {
                waveFrameCounter = 0;

                if (currentPattern == PatternType.WAVE)
                    fireWavePattern(bullets);
                else
                    fireSideWavePattern(bullets);
            }
            return;
        }
        // Level 3 pattern: fast group-based firing (ends after 10 groups)
        if (currentPattern == PatternType.FOCUS) {
            fireFocusPattern(bullets);
            return;
        }
        // Other patterns use normal cooldown
        if (!this.shootingCooldown.checkFinished())
            return;

        this.shootingCooldown.reset();

        switch (currentPattern) {
            case RANDOMBURST:
                // Level 4 pattern: 3-phase burst + 2 cycles
                fireRandomBurstPattern(bullets);
                break;
            default:
                // Default random shooter
                fireNormalRandom(bullets);
                break;
        }
    }
    // Pattern functions
    private void fireNormalRandom(Set<Bullet> bullets) {
        int index = (int) (Math.random() * this.shooters.size());
        EnemyShip shooter = this.shooters.get(index);
        spawnBulletFromShooter(shooter, bullets);
    }
    /**
     * Level 1 : fire one column per step, left → right
     */
    private void fireWavePattern(Set<Bullet> bullets) {
        if (this.shooters.isEmpty()) return;

        // Wave finished one full pass → back to default pattern
        if (waveIndex >= this.shooters.size()) {
            currentPattern = PatternType.NONE;
            waveIndex = 0;
            waveFrameCounter = 0; // ★ 다음 wave를 위해 초기화

            // Avoid an immediate extra shot right after the wave ends
            if (this.shootingCooldown != null) {
                this.shootingCooldown.reset();
            }
            return;
        }
        EnemyShip shooter = this.shooters.get(waveIndex);
        spawnBulletFromShooter(shooter, bullets);
        waveIndex++;
    }
    /**
     * Level 2 : shoot from left/right pairs toward the center
     */
    private void fireSideWavePattern(Set<Bullet> bullets) {
        int n = shooters.size();

        // Finished all pairs → return to default pattern
        if (sidewavePairIndex >= (n + 1) / 2) {
            currentPattern = PatternType.NONE;
            sidewavePairIndex = 0;
            waveFrameCounter = 0;
            shootingCooldown.reset();
            return;
        }

        int left = sidewavePairIndex;
        int right = n - 1 - sidewavePairIndex;

        spawnBulletFromShooter(shooters.get(left), bullets);

        if (right != left)
            spawnBulletFromShooter(shooters.get(right), bullets);

        sidewavePairIndex++;
    }
    /**
     * Level 3 : rapid sequential shots by random left/center/right groups
     */
    private void fireFocusPattern(Set<Bullet> bullets) {
        long now = System.currentTimeMillis();

        // Finished all pairs → back to normal pattern
        if (focusGroupUsed >= 10) {
            currentPattern = PatternType.NONE;
            focusGroupIndex = -1;
            focusStepInGroup = 0;
            focusFrameCounter = 0;
            focusDelayUntil = 0L;
            if (shootingCooldown != null) shootingCooldown.reset();
            return;
        }

        // No active group → try to select a new one
        if (focusGroupIndex == -1) {
            if (focusDelayUntil > 0 && now < focusDelayUntil)
                return;

            // Randomly pick left / center / right, skipping empty groups
            for (int tries = 0; tries < 3 && focusGroupIndex == -1; tries++) {
                int candidate = (int) (Math.random() * 3); // 0~2
                if (!getFocusGroup(candidate).isEmpty()) {
                    focusGroupIndex = candidate;
                    focusStepInGroup = 0;
                    focusFrameCounter = 0;
                }
            }

            // All groups are empty → fall back to normal pattern
            if (focusGroupIndex == -1) {
                currentPattern = PatternType.NONE;
                if (shootingCooldown != null) shootingCooldown.reset();
            }
            return;
        }

        // Active group: fire columns in that group with a fast tempo
        focusFrameCounter++;
        if (focusFrameCounter < FOCUS_STEP_INTERVAL)
            return;
        focusFrameCounter = 0;

        List<EnemyShip> group = getFocusGroup(focusGroupIndex);
        if (group.isEmpty()) {
            // Group became empty → count as used and schedule next one
            focusGroupIndex = -1;
            focusGroupUsed++;
            focusDelayUntil = now + 500;
            return;
        }

        if (focusStepInGroup >= group.size()) {
            // Fired all columns in this group → mark as used and move on
            focusGroupIndex = -1;
            focusGroupUsed++;
            focusDelayUntil = now + 500;
            return;
        }

        EnemyShip shooter = group.get(focusStepInGroup);
        if (shooter != null && !shooter.isDestroyed())
            spawnBulletFromShooter(shooter, bullets);

        focusStepInGroup++;
    }
    /**
     * Return the shooters that belong to the selected focus group.
     * Groups are divided by X-position into left / center / right.
     */
    private List<EnemyShip> getFocusGroup(int group) {
        List<EnemyShip> result = new ArrayList<>();
        if (shooters.isEmpty()) return result;

        // Find minX / maxX among existing shooters
        int minX = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;

        for (EnemyShip s : shooters) {
            int x = s.getPositionX();
            if (x < minX) minX = x;
            if (x > maxX) maxX = x;
        }

        // Split X-range into 3 groups
        int range = maxX - minX;
        int leftMax   = minX + range / 3;
        int centerMax = minX + (range * 2) / 3;

        // Collect ships based on the selected group
        for (EnemyShip s : shooters) {
            int x = s.getPositionX();
            if (group == 0 && x <= leftMax)
                result.add(s);
            else if (group == 1 && x > leftMax && x <= centerMax)
                result.add(s);
            else if (group == 2 && x > centerMax)
                result.add(s);
        }

        return result;
    }
    /**
     * Level 4: repeated 3-phase burst (0→1→2) done twice.
     */
    private void fireRandomBurstPattern(Set<Bullet> bullets) {

        // End pattern after two full cycles
        if (randomBurstCycle >= 2) {
            endRandomBurst();
            return;
        }

        // Waiting period (cooldown only)
        if (randomBurstWait > 0) {
            randomBurstWait--;
            return;
        }

        // Classify columns into single / normal
        List<EnemyShip> singleCols = new ArrayList<>();
        List<EnemyShip> normalCols = new ArrayList<>();

        for (List<EnemyShip> col : enemyShips) {
            if (col.isEmpty()) continue;
            EnemyShip s = col.get(col.size() - 1);
            if (col.size() == 1) singleCols.add(s);
            else normalCols.add(s);
        }

        // Phase processing
        switch (randomBurstPhase) {

            case 0:
            case 1:
                fireColumns(normalCols, bullets);

                if (randomBurstPhase == 0) {
                    randomBurstPhase = 1;
                    randomBurstWait = 2;
                } else {
                    randomBurstPhase = 2;
                }
                break;

            case 2:
                fireTriple(singleCols, bullets);
                randomBurstCycle++;

                if (randomBurstCycle >= 2) {
                    endRandomBurst();
                } else {
                    randomBurstPhase = 0;
                    randomBurstWait = 2;
                }
                break;
        }
    }
    /** fall-back to normal random shooting */
    private void endRandomBurst() {
        currentPattern = PatternType.NONE;
        randomBurstPhase = 0;
        randomBurstCycle = 0;
        randomBurstWait = 0;
        if (shootingCooldown != null) shootingCooldown.reset();
    }

    /** util: fire one bullet from each ship */
    private void fireColumns(List<EnemyShip> list, Set<Bullet> bullets) {
        for (EnemyShip s : list)
            if (s != null && !s.isDestroyed())
                spawnBulletFromShooter(s, bullets);
    }

    /** util: fire triple from each ship */
    private void fireTriple(List<EnemyShip> list, Set<Bullet> bullets) {
        for (EnemyShip s : list)
            if (s != null && !s.isDestroyed())
                for (int i = 0; i < 3; i++)
                    spawnBulletFromShooter(s, bullets);
    }
    /**
     * Bullet spawn utility.
     * - Applies A/B/C-type firing rules (single / fast / double shot).
     * - Computes bullet size, speed, and spawn position.
     * - Extracted to keep shoot() simple and maintainable.
     */
    private void spawnBulletFromShooter(final EnemyShip shooter,
                                        final Set<Bullet> bullets) {

        int bulletWidth = 3 * 2;
        int bulletHeight = 5 * 2;
        int spawnY = shooter.getPositionY() + shooter.getHeight();

        int bulletSpeed = BULLET_SPEED;

        if (shooter.getSpriteType() == SpriteType.EnemyShipB1 ||
                shooter.getSpriteType() == SpriteType.EnemyShipB2) {
            bulletSpeed = BULLET_SPEED * 2;
        }

        if (shooter.getSpriteType() == SpriteType.EnemyShipC1 ||
                shooter.getSpriteType() == SpriteType.EnemyShipC2) {

            int offset = 6;

            Bullet b1 = BulletPool.getBullet(
                    shooter.getPositionX() + shooter.getWidth() / 2 - offset,
                    spawnY, bulletSpeed, bulletWidth, bulletHeight, Entity.Team.ENEMY);
            bullets.add(b1);

            Bullet b2 = BulletPool.getBullet(
                    shooter.getPositionX() + shooter.getWidth() / 2 + offset,
                    spawnY, bulletSpeed, bulletWidth, bulletHeight, Entity.Team.ENEMY);
            bullets.add(b2);

        } else {
            Bullet b = BulletPool.getBullet(
                    shooter.getPositionX() + shooter.getWidth() / 2,
                    spawnY, bulletSpeed, bulletWidth, bulletHeight, Entity.Team.ENEMY);
            bullets.add(b);
        }
    }
    /**
     * Destroys a ship.
     *
     * @param destroyedShip
     *            Ship to be destroyed.
     */
    public final void destroy(final EnemyShip destroyedShip) {
        for (List<EnemyShip> column : this.enemyShips)
            for (int i = 0; i < column.size(); i++)
                if (column.get(i).equals(destroyedShip)) {
                    column.get(i).destroy();
                    this.logger.info("Destroyed ship in ("
                            + this.enemyShips.indexOf(column) + "," + i + ")");
                }

        // Updates the list of ships that can shoot the player.
        if (this.shooters.contains(destroyedShip)) {
            int destroyedShipIndex = this.shooters.indexOf(destroyedShip);
            int destroyedShipColumnIndex = -1;

            for (List<EnemyShip> column : this.enemyShips)
                if (column.contains(destroyedShip)) {
                    destroyedShipColumnIndex = this.enemyShips.indexOf(column);
                    break;
                }

            EnemyShip nextShooter = getNextShooter(this.enemyShips
                    .get(destroyedShipColumnIndex));

            if (nextShooter != null)
                this.shooters.set(destroyedShipIndex, nextShooter);
            else {
                this.shooters.remove(destroyedShipIndex);
                this.logger.info("Shooters list reduced to "
                        + this.shooters.size() + " members.");
            }
        }

        this.shipCount--;
    }

    /**
     * Gets the ship on a given column that will be in charge of shooting.
     *
     * @param column
     *            Column to search.
     * @return New shooter ship.
     */
    public final EnemyShip getNextShooter(final List<EnemyShip> column) {
        Iterator<EnemyShip> iterator = column.iterator();
        EnemyShip nextShooter = null;
        while (iterator.hasNext()) {
            EnemyShip checkShip = iterator.next();
            if (checkShip != null && !checkShip.isDestroyed())
                nextShooter = checkShip;
        }

        return nextShooter;
    }

    /**
     * Returns an iterator over the ships in the formation.
     *
     * @return Iterator over the enemy ships.
     */
    @Override
    public final Iterator<EnemyShip> iterator() {
        Set<EnemyShip> enemyShipsList = new HashSet<EnemyShip>();

        for (List<EnemyShip> column : this.enemyShips)
            enemyShipsList.addAll(column);

        return enemyShipsList.iterator();
    }


    public boolean lastShip(){
        return this.shipCount == 1;
    }
    /**
     * Checks if there are any ships remaining.
     *
     * @return True when all ships have been destroyed.
     */
    public final boolean isEmpty() {
        return this.shipCount <= 0;
    }

    public int getShipCount() {
        return this.shipCount;
    }
}


