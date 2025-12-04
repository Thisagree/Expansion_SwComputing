package main.entity.Enemy;

import java.util.*;
import java.util.logging.Logger;

import main.engine.*;
import main.entity.Bullet;
import main.entity.BulletPool;
import main.entity.Entity;
import main.screen.Screen;
import main.engine.DrawManager.SpriteType;

/**
 * Groups enemy ships into a formation that moves together.
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
    /** Margin on the sides of the main.screen. */
    private static final int SIDE_MARGIN = 20;
    /** Margin on the bottom of the main.screen. */
    private static final int BOTTOM_MARGIN = 80;
    /** Distance to go down each pass. */
    private static final int DESCENT_DISTANCE = 20;
    /** Minimum speed allowed. */
    private static final int MINIMUM_SPEED = 10;

    private static final int PATTERN_DELAY_MS = 7000;
    private static final int FOCUS_MAX = 10;
    private static final int FOCUS_STEP = 10;
    private static final int FOCUS_DELAY_MS = 500;
    private static final int BURST_MAX = 2;
    private static final int BURST_WAIT = 2;
    private static final int WAVE_STEP = 20;

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

    private final int level;

    private long patternStartTime = 0L;
    private PatternType currentPattern = PatternType.NONE;

    private int waveIndex = 0;
    private int waveFrameCounter = 0;

    private int sideWavePairIndex = 0;

    private int focusFrameCounter = 0;
    private int focusGroupIndex = -1;
    private int focusStepInGroup = 0;
    private int focusGroupUsed = 0;
    private long focusDelayUntil = 0L;

    private int randomBurstPhase = 0;
    private int randomBurstCycle = 0;
    private int randomBurstWait = 0;

    private enum PatternType {
        NONE,
        WAVE,
        SIDE_WAVE,
        FOCUS,
        RANDOM_BURST
    }

    private enum Direction {
        /** Movement to the right side of the main.screen. */
        RIGHT,
        /** Movement to the left side of the main.screen. */
        LEFT,
        /** Movement to the bottom of the main.screen. */
        DOWN
    }

    public EnemyShipFormation(final GameSettings gameSettings, final int level) {
        this.level = level;
        this.drawManager = Core.getDrawManager();
        this.logger = Core.getLogger();
        this.enemyShips = new ArrayList<>();
        this.currentDirection = Direction.RIGHT;
        this.movementInterval = 0;
        this.nShipsWide = gameSettings.getFormationWidth();
        this.nShipsHigh = gameSettings.getFormationHeight();
        this.shootingInterval = gameSettings.getShootingFrequency();
        this.shootingVariance = (int) (gameSettings.getShootingFrequency() * SHOOTING_VARIANCE);
        this.baseSpeed = gameSettings.getBaseSpeed();
        this.movementSpeed = this.baseSpeed;
        this.positionX = INIT_POS_X;
        this.positionY = INIT_POS_Y;
        this.shooters = new ArrayList<>();
        SpriteType spriteType;

        this.logger.info("Initializing " + nShipsWide + "x" + nShipsHigh
                + " ship formation in (" + positionX + "," + positionY + ")");

        for (int i = 0; i < this.nShipsWide; i++) {
            this.enemyShips.add(new ArrayList<>());
        }

        for (List<EnemyShip> column : this.enemyShips) {
            for (int i = 0; i < this.nShipsHigh; i++) {
                if (i / (float) this.nShipsHigh < PROPORTION_C)
                    spriteType = SpriteType.EnemyShipC1;
                else if (i / (float) this.nShipsHigh < PROPORTION_B + PROPORTION_C)
                    spriteType = SpriteType.EnemyShipB1;
                else
                    spriteType = SpriteType.EnemyShipA1;

                column.add(new EnemyShip(
                        (SEPARATION_DISTANCE * this.enemyShips.indexOf(column)) + positionX,
                        (SEPARATION_DISTANCE * i) + positionY,
                        spriteType));
                this.shipCount++;
            }
        }

        this.shipWidth = this.enemyShips.getFirst().getFirst().getWidth();
        this.shipHeight = this.enemyShips.getFirst().getFirst().getHeight();

        this.width = (this.nShipsWide - 1) * SEPARATION_DISTANCE + this.shipWidth;
        this.height = (this.nShipsHigh - 1) * SEPARATION_DISTANCE + this.shipHeight;

        for (List<EnemyShip> column : this.enemyShips) {
            this.shooters.add(column.getLast());
        }

        for (GameSettings.ChangeData changeData : gameSettings.getChangeDataList()) {
            EnemyShip ship = this.enemyShips.get(changeData.x).get(changeData.y);

            if (changeData.hp == 0) {
                destroy(ship);
            } else {
                ship.changeShip(changeData);
            }
        }

        List<EnemyShip> destroyed;
        for (List<EnemyShip> column : this.enemyShips) {
            destroyed = new ArrayList<>();
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
     * Associates the formation to a given main.screen.
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
        for (List<EnemyShip> column : this.enemyShips) {
            for (EnemyShip enemyShip : column) {
                drawManager.drawEntity(enemyShip, enemyShip.getPositionX(),
                        enemyShip.getPositionY());
            }
        }
    }

    /**
     * Updates the position of the ships.
     */
    public final void update() {
        if (this.shootingCooldown == null) {
            this.shootingCooldown = Core.getVariableCooldown(shootingInterval, shootingVariance);
            this.shootingCooldown.reset();
        }

        cleanUp();

        int movementX = 0;
        int movementY = 0;
        double remainingProportion = (double) this.shipCount
                / (this.nShipsHigh * this.nShipsWide);
        this.movementSpeed = (int) (Math.pow(remainingProportion, 2) * this.baseSpeed);
        this.movementSpeed += MINIMUM_SPEED;

        movementInterval++;
        if (movementInterval >= this.movementSpeed) {
            movementInterval = 0;

            boolean isAtBottom = positionY + this.height > screen.getHeight() - BOTTOM_MARGIN;
            boolean isAtRightSide = positionX + this.width >= screen.getWidth() - SIDE_MARGIN;
            boolean isAtLeftSide = positionX <= SIDE_MARGIN;
            boolean isAtHorizontalAltitude = positionY % DESCENT_DISTANCE == 0;

            if (currentDirection == Direction.DOWN) {
                if (isAtHorizontalAltitude) {
                    if (previousDirection == Direction.RIGHT) {
                        currentDirection = Direction.LEFT;
                        this.logger.info("Formation now moving left 1");
                    } else {
                        currentDirection = Direction.RIGHT;
                        this.logger.info("Formation now moving right 2");
                    }
                }
            } else if (currentDirection == Direction.LEFT) {
                if (isAtLeftSide) {
                    if (!isAtBottom) {
                        previousDirection = currentDirection;
                        currentDirection = Direction.DOWN;
                        this.logger.info("Formation now moving down 3");
                    } else {
                        currentDirection = Direction.RIGHT;
                        this.logger.info("Formation now moving right 4");
                    }
                }
            } else {
                if (isAtRightSide) {
                    if (!isAtBottom) {
                        previousDirection = currentDirection;
                        currentDirection = Direction.DOWN;
                        this.logger.info("Formation now moving down 5");
                    } else {
                        currentDirection = Direction.LEFT;
                        this.logger.info("Formation now moving left 6");
                    }
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
                destroyed = new ArrayList<>();
                for (EnemyShip enemyShip : column) {
                    if (enemyShip != null && enemyShip.isDestroyed()) {
                        destroyed.add(enemyShip);
                        this.logger.info("Removed enemy "
                                + column.indexOf(enemyShip) + " from column "
                                + this.enemyShips.indexOf(column));
                    } else {
                        Objects.requireNonNull(enemyShip).move(movementX, movementY);
                        enemyShip.update();
                    }
                }
                column.removeAll(destroyed);
            }

            long now = System.currentTimeMillis();
            if (currentPattern == PatternType.NONE &&
                    now - patternStartTime >= PATTERN_DELAY_MS) {
                currentPattern = selectPatternByLevel(level);
                initPatternState(currentPattern);
            }
        }
    }

    /**
     * Cleans empty columns, adjusts the width and height of the formation.
     */
    private void cleanUp() {
        Set<Integer> emptyColumns = new HashSet<>();
        int maxColumn = 0;
        int minPositionY = Integer.MAX_VALUE;

        for (List<EnemyShip> column : this.enemyShips) {
            if (!column.isEmpty()) {
                // Height of this column
                int columnSize = column.getLast().getPositionY()
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

    public final void shoot(final Set<Bullet> bullets) {
        if (this.shooters.isEmpty()) {
            return;
        }

        switch (currentPattern) {
            case WAVE:
                handleWaveShoot(bullets);
                return;
            case SIDE_WAVE:
                handleSideWaveShoot(bullets);
                return;
            case FOCUS:
                fireFocusPattern(bullets);
                return;
            case RANDOM_BURST:
                if (!this.shootingCooldown.checkFinished()) {
                    return;
                }
                this.shootingCooldown.reset();
                fireRandomBurstPattern(bullets);
                return;
            case NONE:
            default:
                if (!this.shootingCooldown.checkFinished()) {
                    return;
                }
                this.shootingCooldown.reset();
                fireNormalRandom(bullets);
        }
    }

    private void handleWaveShoot(Set<Bullet> bullets) {
        waveFrameCounter++;
        if (waveFrameCounter >= WAVE_STEP) {
            waveFrameCounter = 0;
            fireWavePattern(bullets);
        }
    }

    private void handleSideWaveShoot(Set<Bullet> bullets) {
        waveFrameCounter++;
        if (waveFrameCounter >= WAVE_STEP) {
            waveFrameCounter = 0;
            fireSideWavePattern(bullets);
        }
    }

    private void fireNormalRandom(Set<Bullet> bullets) {
        int index = (int) (Math.random() * this.shooters.size());
        EnemyShip shooter = this.shooters.get(index);
        spawnBulletFromShooter(shooter, bullets);
    }

    private void fireWavePattern(Set<Bullet> bullets) {
        if (this.shooters.isEmpty()) return;

        if (waveIndex >= this.shooters.size()) {
            currentPattern = PatternType.NONE;
            waveIndex = 0;
            waveFrameCounter = 0;
            if (this.shootingCooldown != null) {
                this.shootingCooldown.reset();
            }
            patternStartTime = System.currentTimeMillis();
            return;
        }
        EnemyShip shooter = this.shooters.get(waveIndex);
        spawnBulletFromShooter(shooter, bullets);
        waveIndex++;
    }

    private void fireSideWavePattern(Set<Bullet> bullets) {
        int n = shooters.size();

        if (sideWavePairIndex >= (n + 1) / 2) {
            currentPattern = PatternType.NONE;
            sideWavePairIndex = 0;
            waveFrameCounter = 0;
            shootingCooldown.reset();
            patternStartTime = System.currentTimeMillis();
            return;
        }

        int left = sideWavePairIndex;
        int right = n - 1 - sideWavePairIndex;

        spawnBulletFromShooter(shooters.get(left), bullets);

        if (right != left) {
            spawnBulletFromShooter(shooters.get(right), bullets);
        }

        sideWavePairIndex++;
    }

    private void fireFocusPattern(Set<Bullet> bullets) {
        long now = System.currentTimeMillis();

        if (focusGroupUsed >= FOCUS_MAX) {
            resetFocusPattern();
            return;
        }

        if (focusGroupIndex == -1) {
            if (focusDelayUntil > 0 && now < focusDelayUntil)
                return;

            List<Integer> availableGroups = new ArrayList<>();
            for (int i = 0; i < 3; i++) {
                if (!getFocusGroup(i).isEmpty()) {
                    availableGroups.add(i);
                }
            }

            if (availableGroups.isEmpty()) {
                resetFocusPattern();
                return;
            }

            int randomIndex = (int) (Math.random() * availableGroups.size());
            focusGroupIndex = availableGroups.get(randomIndex);
            focusStepInGroup = 0;
            focusFrameCounter = 0;
        }

        focusFrameCounter++;
        if (focusFrameCounter < FOCUS_STEP)
            return;
        focusFrameCounter = 0;

        List<EnemyShip> group = getFocusGroup(focusGroupIndex);

        if (group.isEmpty() || focusStepInGroup >= group.size()) {
            endFocusGroup(now);
            return;
        }

        EnemyShip shooter = group.get(focusStepInGroup);
        if (shooter != null && !shooter.isDestroyed())
            spawnBulletFromShooter(shooter, bullets);

        focusStepInGroup++;
    }

    private void resetFocusPattern() {
        currentPattern = PatternType.NONE;
        focusGroupIndex = -1;
        focusStepInGroup = 0;
        focusFrameCounter = 0;
        focusDelayUntil = 0L;
        focusGroupUsed = 0;
        if (shootingCooldown != null) {
            shootingCooldown.reset();
        }
        patternStartTime = System.currentTimeMillis();
    }

    private void endFocusGroup(long now) {
        focusGroupIndex = -1;
        focusGroupUsed++;
        focusDelayUntil = now + FOCUS_DELAY_MS;
    }

    private List<EnemyShip> getFocusGroup(int group) {
        List<EnemyShip> result = new ArrayList<>();
        if (shooters.isEmpty()) {
            return result;
        }

        int n = shooters.size();
        int third = n / 3;

        int start = group * third;
        int end = (group == 2) ? n : start + third;

        if (start >= n) {
            return result;
        }
        if (end > n) {
            end = n;
        }

        result.addAll(shooters.subList(start, end));
        return result;
    }

    private void fireRandomBurstPattern(Set<Bullet> bullets) {
        if (randomBurstWait > 0) {
            randomBurstWait--;
            return;
        }

        List<EnemyShip> singleCols = new ArrayList<>();
        List<EnemyShip> normalCols = new ArrayList<>();

        for (List<EnemyShip> col : enemyShips) {
            if (col.isEmpty()) continue;
            EnemyShip s = col.getLast();
            if (col.size() == 1) singleCols.add(s);
            else normalCols.add(s);
        }

        if (randomBurstPhase == 0 || randomBurstPhase == 1) {

            fireColumns(normalCols, bullets);

            if (randomBurstPhase == 0) {
                randomBurstPhase = 1;
                randomBurstWait = BURST_WAIT;
            } else {
                randomBurstPhase = 2;
            }

        } else if (randomBurstPhase == 2) {

            fireTriple(singleCols, bullets);
            randomBurstCycle++;

            if (randomBurstCycle >= BURST_MAX) {
                endRandomBurst();
            } else {
                randomBurstPhase = 0;
                randomBurstWait = BURST_WAIT;
            }
        }
    }

    private void endRandomBurst() {
        currentPattern = PatternType.NONE;
        randomBurstPhase = 0;
        randomBurstCycle = 0;
        randomBurstWait = 0;
        if (shootingCooldown != null) shootingCooldown.reset();
        patternStartTime = System.currentTimeMillis();
    }

    private void fireColumns(List<EnemyShip> list, Set<Bullet> bullets) {
        for (EnemyShip s : list) {
            if (s != null && !s.isDestroyed()) {
                spawnBulletFromShooter(s, bullets);
            }
        }
    }

    private void fireTriple(List<EnemyShip> list, Set<Bullet> bullets) {
        for (EnemyShip s : list) {
            if (s != null && !s.isDestroyed()) {
                for (int i = 0; i < 3; i++) {
                    spawnBulletFromShooter(s, bullets);
                }
            }
        }
    }

    private void spawnBulletFromShooter(final EnemyShip shooter,
                                        final Set<Bullet> bullets) {

        SpriteType type = shooter.getSpriteType();

        boolean isTypeB = (type == SpriteType.EnemyShipB1 || type == SpriteType.EnemyShipB2);
        boolean isTypeC = (type == SpriteType.EnemyShipC1 || type == SpriteType.EnemyShipC2);

        int bulletSpeed = BULLET_SPEED * (isTypeB ? 2 : 1);

        int bulletWidth = 3 * 2;
        int bulletHeight = 5 * 2;
        int spawnY = shooter.getPositionY() + shooter.getHeight();

        if (isTypeC) {
            spawnDoubleShot(shooter, bullets, bulletSpeed, bulletWidth, bulletHeight, spawnY);
        } else {
            spawnSingleShot(shooter, bullets, bulletSpeed, bulletWidth, bulletHeight, spawnY);
        }
    }

    private void spawnSingleShot(EnemyShip shooter,
                                 Set<Bullet> bullets,
                                 int bulletSpeed,
                                 int bulletWidth,
                                 int bulletHeight,
                                 int spawnY) {

        Bullet b = BulletPool.getBullet(
                shooter.getPositionX() + shooter.getWidth() / 2,
                spawnY,
                0,
                bulletSpeed,
                bulletWidth,
                bulletHeight,
                Entity.Team.ENEMY);
        bullets.add(b);
    }

    private void spawnDoubleShot(EnemyShip shooter,
                                 Set<Bullet> bullets,
                                 int bulletSpeed,
                                 int bulletWidth,
                                 int bulletHeight,
                                 int spawnY) {

        int offset = 6;

        Bullet b1 = BulletPool.getBullet(
                shooter.getPositionX() + shooter.getWidth() / 2 - offset,
                spawnY,
                0,
                bulletSpeed,
                bulletWidth,
                bulletHeight,
                Entity.Team.ENEMY);
        Bullet b2 = BulletPool.getBullet(
                shooter.getPositionX() + shooter.getWidth() / 2 + offset,
                spawnY,
                0,
                bulletSpeed,
                bulletWidth,
                bulletHeight,
                Entity.Team.ENEMY);

        bullets.add(b1);
        bullets.add(b2);
    }

    private PatternType selectPatternByLevel(int level) {
        return switch (level) {
            case 1 -> PatternType.WAVE;
            case 2 -> PatternType.SIDE_WAVE;
            case 3 -> PatternType.FOCUS;
            case 4 -> PatternType.RANDOM_BURST;
            default -> PatternType.NONE;
        };
    }

    private void initPatternState(PatternType pattern) {
        switch (pattern) {
            case WAVE -> {
                waveIndex = 0;
                waveFrameCounter = 0;
            }
            case SIDE_WAVE -> {
                sideWavePairIndex = 0;
                waveFrameCounter = 0;
            }
            case FOCUS -> {
                focusFrameCounter = 0;
                focusGroupIndex = -1;
                focusStepInGroup = 0;
                focusGroupUsed = 0;
                focusDelayUntil = 0L;
            }
            case RANDOM_BURST -> {
                randomBurstPhase = 0;
                randomBurstCycle = 0;
                randomBurstWait = 0;
            }
            case NONE -> {
            }
        }
    }

    /**
     * Destroys a ship.
     *
     * @param destroyedShip
     *            Ship to be destroyed.
     */
    public final void destroy(final EnemyShip destroyedShip) {
        for (List<EnemyShip> column : this.enemyShips) {
            for (int i = 0; i < column.size(); i++) {
                if (column.get(i).equals(destroyedShip)) {
                    column.get(i).destroy();
                    this.logger.info("Destroyed ship in ("
                            + this.enemyShips.indexOf(column) + "," + i + ")");
                }
            }
        }

        // Updates the list of ships that can shoot the player.
        if (this.shooters.contains(destroyedShip)) {
            int destroyedShipIndex = this.shooters.indexOf(destroyedShip);
            int destroyedShipColumnIndex = -1;

            for (List<EnemyShip> column : this.enemyShips) {
                if (column.contains(destroyedShip)) {
                    destroyedShipColumnIndex = this.enemyShips.indexOf(column);
                    break;
                }
            }

            EnemyShip nextShooter = getNextShooter(this.enemyShips.get(destroyedShipColumnIndex));

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
        Set<EnemyShip> enemyShipsList = new HashSet<>();

        for (List<EnemyShip> column : this.enemyShips)
            enemyShipsList.addAll(column);

        return enemyShipsList.iterator();
    }

    public boolean lastShip() {
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


