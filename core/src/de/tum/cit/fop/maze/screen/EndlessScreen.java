package de.tum.cit.fop.maze.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import de.tum.cit.fop.maze.MazeRunnerGame;
import de.tum.cit.fop.maze.entities.*;
import de.tum.cit.fop.maze.entities.enemy.*;
import de.tum.cit.fop.maze.entities.trap.Trap;
import de.tum.cit.fop.maze.entities.trap.TrapT02_PearlMine;
import de.tum.cit.fop.maze.game.Difficulty;
import de.tum.cit.fop.maze.game.DifficultyConfig;
import de.tum.cit.fop.maze.game.GameConstants;
import de.tum.cit.fop.maze.game.GameManager;
import de.tum.cit.fop.maze.input.PlayerInputHandler;
import de.tum.cit.fop.maze.maze.MazeRenderer;
import de.tum.cit.fop.maze.tools.ButtonFactory;
import de.tum.cit.fop.maze.ui.HUD;
import de.tum.cit.fop.maze.utils.CameraManager;
import de.tum.cit.fop.maze.tools.DeveloperConsole;
import de.tum.cit.fop.maze.input.KeyBindingManager;

// ✅ 新增导入：事件监听相关
import de.tum.cit.fop.maze.game.event.GameListener;
import de.tum.cit.fop.maze.game.score.DamageSource;
import de.tum.cit.fop.maze.game.EnemyTier;

import java.lang.reflect.Method;
import java.util.*;

import static de.tum.cit.fop.maze.maze.MazeGenerator.BORDER_THICKNESS;
import static com.badlogic.gdx.math.MathUtils.random;
/**
 * Gameplay screen implementing the Endless mode.
 *
 * <p>This screen manages an infinite survival gameplay loop with
 * dynamically increasing waves, enemy spawning, adaptive item drops,
 * score calculation, and game-over handling.
 *
 * <p>The EndlessScreen listens to in-game events via {@link GameListener}
 * to track kills, damage taken, and survival progress.
 */

public class EndlessScreen implements Screen, GameListener {

    private final MazeRunnerGame game;
    private final DifficultyConfig difficultyConfig;

    private GameManager gm;
    private MazeRenderer maze;
    private CameraManager cam;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private HUD hud;
    private PlayerInputHandler input;
    private DeveloperConsole console;

    private Texture uiTop, uiBottom, uiLeft, uiRight;

    private boolean paused = false;
    private Stage pauseStage;
    private boolean pauseUIInitialized = false;

    private float endlessSurvivalTime = 0f;
    private int endlessWave = 1;
    private int endlessKills = 0;
    private int endlessScore = 0;

    private int endlessDamageTaken = 0;

    private float endlessSpawnTimer = 0f;
    private float endlessSpawnInterval = 4f;
    private boolean endlessGameOver = false;
    private Stage endlessGameOverStage;
    private boolean endlessGameOverUIInitialized = false;

    private float heartSpawnTimer = 0f;
    private float powerupSpawnTimer = 0f;
    private float minHeartSpawnInterval = 15f;
    private float minPowerupSpawnInterval = 30f;
    private int heartsSpawnedThisWave = 0;
    private int powerupsSpawnedThisWave = 0;
    private Map<String, Long> heartCreationTimes = new HashMap<>();

    private final Random randomGenerator = new Random();

    enum Type { WALL_BEHIND, ENTITY, WALL_FRONT }

    static class Item {
        float y;
        int priority;
        Type type;
        MazeRenderer.WallGroup wall;
        GameObject entity;

        Item(MazeRenderer.WallGroup w, Type t) {
            wall = w;
            y = w.startY;
            type = t;
        }

        Item(GameObject e, int p) {
            entity = e;
            y = e.getY();
            priority = p;
            type = Type.ENTITY;
        }
    }
    /**
     * Strategies used to determine optimal heart spawn positions.
     */
    enum HeartSpawnStrategy {
        NEAR_PLAYER,
        SAFE_ZONE,
        FAR_FROM_ENEMIES,
        STRATEGIC_POINT
    }

    enum PowerupType {
        ATTACK_BOOST,
        SPEED_BOOST,
        DEFENSE_BOOST,
        COOLDOWN_REDUCTION
    }
    /**
     * Creates the Endless mode gameplay screen.
     *
     * @param game the main game instance
     * @param difficultyConfig configuration for maze size and difficulty parameters
     */
    public EndlessScreen(MazeRunnerGame game, DifficultyConfig difficultyConfig) {
        this.game = game;
        this.difficultyConfig = difficultyConfig;
    }
    /**
     * Initializes the Endless mode gameplay, including camera, maze,
     * HUD, event listeners, and initial enemy spawning.
     */
    @Override
    public void show() {
        uiTop = new Texture("Wallpaper/HUD_up.png");
        uiBottom = new Texture("Wallpaper/HUD_down.png");
        uiLeft = new Texture("Wallpaper/HUD_left.png");
        uiRight = new Texture("Wallpaper/HUD_right.png");

        input = new PlayerInputHandler();
        batch = game.getSpriteBatch();
        shapeRenderer = new ShapeRenderer();

        gm = game.getGameManager();

        gm.setGameListener(this);

        cam = new CameraManager(difficultyConfig);
        cam.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        maze = new MazeRenderer(gm, difficultyConfig);
        hud = new HUD(gm);

        if (gm.getPlayer() != null) {
            cam.centerOnPlayerImmediately(gm.getPlayer());
        }

        trySetActiveGameScreen();

        console = new DeveloperConsole(gm, game.getSkin());

        if (isEndlessMode()) {
            initializeEndlessMode();
        }

        if (gm != null && gm.getPlayer() != null) {
            Player player = gm.getPlayer();

            cam.centerOnPlayerImmediately(player);
        }

    }

    private void trySetActiveGameScreen() {
        try {
            Method m = game.getClass().getMethod("setActiveGameScreen", Screen.class);
            m.invoke(game, this);
        } catch (Exception ignored) {}
    }
    /**
     * Resets and initializes all Endless mode variables and spawns
     * the initial set of enemies.
     */
    private void initializeEndlessMode() {

        endlessSurvivalTime = 0f;
        endlessWave = 1;
        endlessKills = 0;
        endlessScore = 0;

        endlessDamageTaken = 0;

        endlessSpawnTimer = 0f;
        heartSpawnTimer = 0f;
        powerupSpawnTimer = 0f;
        endlessGameOver = false;

        heartsSpawnedThisWave = 0;
        powerupsSpawnedThisWave = 0;
        heartCreationTimes.clear();

        gm.getExitDoors().clear();
        gm.getKeys().clear();

        spawnInitialEndlessEnemies();
    }
    /**
     * Called when an enemy is killed during Endless mode.
     *
     * @param tier the tier of the defeated enemy
     * @param isDashKill whether the enemy was killed by a dash attack
     */
    @Override
    public void onEnemyKilled(EnemyTier tier, boolean isDashKill) {
        endlessKills++;
    }
    /**
     * Called when the player takes damage during Endless mode.
     *
     * @param currentHp the player's current health
     * @param source the damage source
     */
    @Override
    public void onPlayerDamage(int currentHp, DamageSource source) {
        endlessDamageTaken++;
    }

    // 接口必须实现的方法（无尽模式暂时不需要具体逻辑，留空即可）
    @Override
    public void onItemCollected(String itemType) {}

    @Override
    public void onLevelFinished(int levelNumber) {}
    /**
     * Updates and renders the Endless mode gameplay each frame.
     *
     * @param delta time elapsed since last frame (in seconds)
     */
    @Override
    public void render(float delta) {

        if (resizing) {
            ScreenUtils.clear(0, 0, 0, 1);
            return;
        }

        gm.setUIConsumesMouse(hud.isMouseOverInteractiveUI());
        Vector3 world = cam.getCamera().unproject(
                new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0)
        );

        gm.setMouseTargetTile(
                (int)(world.x / GameConstants.CELL_SIZE),
                (int)(world.y / GameConstants.CELL_SIZE)
        );

        handleInput(delta);

        float timeScale = console.isVisible() ? 0f : gm.getVariable("time_scale");
        float gameDelta = delta * timeScale;

        if (!paused && !console.isVisible()) {
            gm.update(gameDelta);
            if (isEndlessMode() && !endlessGameOver) {
                updateEndlessMode(gameDelta);
            }
        }

        if (!paused && !console.isVisible()) {
            cam.update(gameDelta, gm);
        }

        ScreenUtils.clear(0.1f, 0.1f, 0.1f, 1f);

        batch.setProjectionMatrix(cam.getCamera().combined);
        batch.begin();

        maze.renderFloor(batch);

        List<ExitDoor> exitDoorsCopy = new ArrayList<>(gm.getExitDoors());
        exitDoorsCopy.forEach(d -> d.renderPortalBack(batch));

        List<Item> items = prepareRenderItems(exitDoorsCopy);
        items.sort(Comparator.comparingDouble((Item i) -> -i.y)
                .thenComparingInt(i -> i.type.ordinal())
                .thenComparingInt(i -> i.priority));

        for (Item it : items) {
            if (it.wall != null) {
                maze.renderWallGroup(batch, it.wall);
            } else {
                it.entity.drawSprite(batch);
            }
        }

        if (gm.getKeyEffectManager() != null) {
            gm.getKeyEffectManager().render(batch);
        }
        if (gm.getBobaBulletEffectManager() != null) {
            gm.getBobaBulletEffectManager().render(batch);
        }

        if (gm.getItemEffectManager() != null) gm.getItemEffectManager().renderSprites(batch);
        if (gm.getTrapEffectManager() != null) gm.getTrapEffectManager().renderSprites(batch);
        if (gm.getCombatEffectManager() != null) gm.getCombatEffectManager().renderSprites(batch);

        batch.end();

        shapeRenderer.setProjectionMatrix(cam.getCamera().combined);

        for (Player p : gm.getPlayers()) {
            if (p.getAbilityManager() != null) {
                p.getAbilityManager().drawAbilities(batch, shapeRenderer, p);
            }
        }
        renderUI();

        if (paused) {
            if (!pauseUIInitialized) {
                initPauseUI();
            }

            Gdx.input.setInputProcessor(pauseStage);
            pauseStage.act(delta);
            pauseStage.draw();
            return;
        }
        if (endlessGameOver && endlessGameOverStage != null) renderGameOverScreen(delta);

    }

    private void renderGameOverScreen(float delta) {
        if (!endlessGameOverUIInitialized) {
            showEndlessGameOverScreen();
        }

        Gdx.input.setInputProcessor(endlessGameOverStage);
        endlessGameOverStage.act(delta);
        endlessGameOverStage.draw();
    }

    private void renderUI() {
        Matrix4 oldProjection = batch.getProjectionMatrix().cpy();
        Color oldColor = batch.getColor().cpy();

        batch.setProjectionMatrix(
                new Matrix4().setToOrtho2D(
                        0, 0,
                        Gdx.graphics.getWidth(),
                        Gdx.graphics.getHeight()
                )
        );

        batch.begin();
        renderMazeBorderDecorations(batch);
        hud.renderInGameUI(batch, !paused && !console.isVisible());
        batch.end();
        if (console != null) {
            console.render();
        }
        batch.setColor(oldColor);
        batch.setProjectionMatrix(oldProjection);
    }

    private void renderMazeBorderDecorations(SpriteBatch batch) {
        int w = Gdx.graphics.getWidth();
        int h = Gdx.graphics.getHeight();
        int thickness = 1000;

        batch.draw(uiTop,    0, h - thickness+860, w, thickness-120);
        batch.draw(uiBottom, 0, 0-800,             w, thickness-120);
        batch.draw(uiLeft,   -600, 0,             thickness-220, h);
        batch.draw(uiRight,  w - thickness+810, 0, thickness-220, h);
    }

    /**
     * Updates Endless mode logic such as wave progression,
     * enemy spawning, and item generation.
     *
     * @param delta scaled game delta time
     */
    private void updateEndlessMode(float delta) {

        if (gm == null || gm.getPlayer() == null) return;

        endlessSurvivalTime += delta;
        endlessSpawnTimer += delta;
        heartSpawnTimer += delta;
        powerupSpawnTimer += delta;

        if (isEndlessGameOver()) {
            endlessGameOver = true;
            showEndlessGameOverScreen();
            return;
        }

        float healthPercent = calculatePlayerHealthPercentage();

        float enemySpawnInterval = getDynamicEnemySpawnInterval(healthPercent);
        if (endlessSpawnTimer >= enemySpawnInterval) {
            endlessSpawnTimer = 0f;
            spawnHealthBasedEnemies(healthPercent);
        }

        updateHeartSpawnLogic(delta, healthPercent);
        updatePowerupSpawnLogic(delta, healthPercent);
        cleanupExpiredItems();

        int newWave = 1 + (int)(endlessSurvivalTime / 60f);
        if (newWave > endlessWave) {
            endlessWave = newWave;
            onEndlessWaveAdvanced();
            resetWaveSpawnCounters();
        }
        int pearlCount = 0;
        int coffeeCount = 0;
        int caramelCount = 0;

        for (Enemy enemy : gm.getEnemies()) {
            if (enemy instanceof EnemyE01_CorruptedPearl) {
                pearlCount++;
            } else if (enemy instanceof EnemyE02_SmallCoffeeBean) {
                coffeeCount++;
            } else if (enemy instanceof EnemyE03_CaramelJuggernaut) {
                caramelCount++;
            }
        }


    }

    private boolean isEndlessGameOver() {
        if (!gm.isTwoPlayerMode()) {
            return gm.getPlayer().isDead();
        }
        for (Player p : gm.getPlayers()) {
            if (!p.isDead()) return false;
        }
        return true;
    }

    private float calculatePlayerHealthPercentage() {
        if (!gm.isTwoPlayerMode()) {
            return gm.getPlayer().getLives() / (float) difficultyConfig.initialLives * 100f;
        }

        int total = 0;
        int alive = 0;
        for (Player p : gm.getPlayers()) {
            if (!p.isDead()) {
                total += p.getLives();
                alive++;
            }
        }
        if (alive == 0) return 0;
        return (total / (float)(alive * difficultyConfig.initialLives)) * 100f;
    }

    private float getDynamicEnemySpawnInterval(float healthPercent) {
        float interval = endlessSpawnInterval;

        if (healthPercent < 30) {
            interval *= 0.5f;
        } else if (healthPercent < 60) {
            interval *= 0.75f;
        }

        interval -= endlessWave * 0.1f;
        return Math.max(1f, interval);
    }

    private void updateHeartSpawnLogic(float delta, float healthPercent) {
        float interval = calculateHeartSpawnInterval(healthPercent);

        if (heartSpawnTimer >= interval) {
            int maxPerWave = getMaxHeartsPerWave();
            if (heartsSpawnedThisWave < maxPerWave && gm.getHearts().size() < 3) {
                spawnSmartHeart(healthPercent);
                heartsSpawnedThisWave++;
                heartSpawnTimer = 0f;
            }
        }
    }

    private float calculateHeartSpawnInterval(float healthPercent) {
        float base = 20f;

        if (healthPercent < 20) base = 8f;
        else if (healthPercent < 40) base = 12f;
        else if (healthPercent < 60) base = 16f;

        float waveMul = Math.max(0.5f, 1f - endlessWave * 0.05f);
        float countMul = 1f + gm.getHearts().size() * 0.3f;

        return Math.max(5f, base * waveMul * countMul);
    }

    private int getMaxHeartsPerWave() {
        return Math.min(5, 2 + endlessWave / 3);
    }
    private boolean resizing = false;

    private void spawnSmartHeart(float healthPercent) {
        Player p = gm.getPlayer();
        if (p == null) return;

        HeartSpawnStrategy strategy = determineHeartSpawnStrategy(healthPercent);
        int[] pos = findOptimalHeartPosition(p.getX(), p.getY(), strategy);

        if (pos == null) return;

        Heart heart = new Heart(pos[0], pos[1]);
        heartCreationTimes.put(pos[0] + "," + pos[1], System.currentTimeMillis());

        if (healthPercent < 30 && randomGenerator.nextFloat() < 0.3f) {
            heart = createEnhancedHeart(pos[0], pos[1]);
        }
        gm.getHearts().add(heart);
    }
    private Heart createEnhancedHeart(int x, int y) {
        Heart heart = new Heart(x, y);
        try {
            Method setHealMethod = Heart.class.getMethod("setHealAmount", int.class);
            setHealMethod.invoke(heart, 2);
        } catch (Exception e) {
            System.out.println("error");
        }
        return heart;
    }
    private HeartSpawnStrategy determineHeartSpawnStrategy(float healthPercent) {
        if (healthPercent < 20) return HeartSpawnStrategy.NEAR_PLAYER;
        if (healthPercent < 40) return HeartSpawnStrategy.SAFE_ZONE;
        if (gm.getEnemies().size() > 5) return HeartSpawnStrategy.FAR_FROM_ENEMIES;
        return HeartSpawnStrategy.STRATEGIC_POINT;
    }

    private int[] findOptimalHeartPosition(int px, int py, HeartSpawnStrategy strategy) {
        int bx = -1, by = -1;
        float best = -9999f;

        for (int i = 0; i < 50; i++) {
            int[] pos = findAnyEmptyCell();
            if (pos == null) continue;

            float score = calculatePositionScore(pos[0], pos[1], px, py, strategy);
            if (score > best) {
                best = score;
                bx = pos[0];
                by = pos[1];
            }
        }
        if (bx == -1) return null;
        return new int[]{bx, by};
    }

    private float calculatePositionScore(int x, int y, int px, int py, HeartSpawnStrategy s) {
        if (!isCellWalkable(x, y) || isCellOccupied(x, y)) return -9999f;

        float score = 0f;

        switch (s) {
            case NEAR_PLAYER:
                float distToPlayer = Math.abs(x - px) + Math.abs(y - py);
                if (distToPlayer >= 3 && distToPlayer <= 8) {
                    score += 100 - distToPlayer;
                }
                break;

            case SAFE_ZONE:
                float minEnemyDist = getMinDistanceToEnemies(x, y);
                score += minEnemyDist * 10;
                break;

            case FAR_FROM_ENEMIES:
                float enemyDist = getMinDistanceToEnemies(x, y);
                score += enemyDist * 20;
                if (enemyDist > 10) score += 50;
                break;

            case STRATEGIC_POINT:
                int openDirections = countOpenDirections(x, y);
                score += openDirections * 30;
                break;
        }

        if (!isInCorner(x, y)) score += 20;
        if (getMinDistanceToHearts(x, y) > 5) score += 30;

        return score;
    }

    private float getMinDistanceToEnemies(int x, int y) {
        float min = Float.MAX_VALUE;
        for (Enemy e : gm.getEnemies()) {
            float d = Math.abs(e.getX() - x) + Math.abs(e.getY() - y);
            min = Math.min(min, d);
        }
        return min == Float.MAX_VALUE ? 10f : min;
    }

    private float getMinDistanceToHearts(int x, int y) {
        float min = Float.MAX_VALUE;
        for (Heart h : gm.getHearts()) {
            float d = Math.abs(h.getX() - x) + Math.abs(h.getY() - y);
            min = Math.min(min, d);
        }
        return min == Float.MAX_VALUE ? 10f : min;
    }

    private int countOpenDirections(int x, int y) {
        int c = 0;
        if (isCellWalkable(x + 1, y)) c++;
        if (isCellWalkable(x - 1, y)) c++;
        if (isCellWalkable(x, y + 1)) c++;
        if (isCellWalkable(x, y - 1)) c++;
        return c;
    }

    private boolean isInCorner(int x, int y) {
        int b = 0;
        if (!isCellWalkable(x + 1, y)) b++;
        if (!isCellWalkable(x - 1, y)) b++;
        if (!isCellWalkable(x, y + 1)) b++;
        if (!isCellWalkable(x, y - 1)) b++;
        return b >= 3;
    }

    private void updatePowerupSpawnLogic(float delta, float healthPercent) {
        if (endlessWave < 3) return;

        float powerupInterval = calculatePowerupSpawnInterval(healthPercent);

        if (powerupSpawnTimer >= powerupInterval) {
            int maxPowerupsPerWave = getMaxPowerupsPerWave();
            if (powerupsSpawnedThisWave < maxPowerupsPerWave) {
                spawnRandomPowerup(healthPercent);
                powerupsSpawnedThisWave++;
                powerupSpawnTimer = 0f;
            }
        }
    }

    private float calculatePowerupSpawnInterval(float healthPercent) {
        float baseInterval = 45f;

        if (healthPercent < 30) {
            baseInterval = 25f; // 低生命值时生成更快
        } else if (healthPercent > 70) {
            baseInterval = 60f; // 高生命值时生成更慢
        }

        float waveMultiplier = Math.max(0.3f, 1.0f - (endlessWave * 0.03f));

        return Math.max(15f, baseInterval * waveMultiplier);
    }

    private int getMaxPowerupsPerWave() {
        return Math.min(3, 1 + (endlessWave / 5));
    }

    private void spawnRandomPowerup(float healthPercent) {
        int[] pos = findEmptyCellForEndlessSpawn();
        if (pos == null) return;

        PowerupType type = selectPowerupType(healthPercent);

        try {
            switch (type) {
                case ATTACK_BOOST:
                    spawnAttackBoost(pos[0], pos[1]);
                    break;
                case SPEED_BOOST:
                    spawnSpeedBoost(pos[0], pos[1]);
                    break;
                case DEFENSE_BOOST:
                    spawnDefenseBoost(pos[0], pos[1]);
                    break;
                case COOLDOWN_REDUCTION:
                    spawnCooldownReduction(pos[0], pos[1]);
                    break;
            }


        } catch (Exception e) {
            System.out.println("error" + e.getMessage());
        }
    }



    private PowerupType selectPowerupType(float healthPercent) {
        float rand = randomGenerator.nextFloat();

        if (healthPercent < 30) {
            if (rand < 0.4) return PowerupType.DEFENSE_BOOST;
            if (rand < 0.7) return PowerupType.COOLDOWN_REDUCTION;
            if (rand < 0.9) return PowerupType.ATTACK_BOOST;
            return PowerupType.SPEED_BOOST;
        } else if (healthPercent > 70) {
            if (rand < 0.4) return PowerupType.ATTACK_BOOST;
            if (rand < 0.7) return PowerupType.SPEED_BOOST;
            if (rand < 0.9) return PowerupType.COOLDOWN_REDUCTION;
            return PowerupType.DEFENSE_BOOST;
        } else {
            if (rand < 0.25) return PowerupType.ATTACK_BOOST;
            if (rand < 0.5) return PowerupType.SPEED_BOOST;
            if (rand < 0.75) return PowerupType.DEFENSE_BOOST;
            return PowerupType.COOLDOWN_REDUCTION;
        }
    }

    private void spawnAttackBoost(int x, int y) {
        System.out.println("生成攻击力提升物品");
    }

    private void spawnSpeedBoost(int x, int y) {
        System.out.println("生成速度提升物品");
    }

    private void spawnDefenseBoost(int x, int y) {
        System.out.println("生成防御提升物品");
    }

    private void spawnCooldownReduction(int x, int y) {
        System.out.println("生成冷却减少物品");
    }


    private void cleanupExpiredItems() {
        long currentTime = System.currentTimeMillis();
        Iterator<Heart> heartIter = gm.getHearts().iterator();

        while (heartIter.hasNext()) {
            Heart heart = heartIter.next();
            String heartKey = heart.getX() + "," + heart.getY();
            Long creationTime = heartCreationTimes.get(heartKey);

            if (creationTime != null) {
                long age = currentTime - creationTime;
                if (age > 60000) {
                    heartIter.remove();
                    heartCreationTimes.remove(heartKey);
                }
            }
        }
    }

    private void onEndlessWaveAdvanced() {
        System.out.println("===  " + endlessWave + "  ===");

        grantWaveRewards();
        int mineCount = Math.min(3, 1 + endlessWave / 3);
        spawnEndlessPearlMine(mineCount);
        if (endlessWave % 5 == 0) {
            spawnSuperHeart();
        }

        if (endlessWave % 10 == 0) {
            spawnRarePowerup();
        }
    }

    private void spawnEndlessPearlMine(int count) {
        for (int i = 0; i < count; i++) {
            int[] pos = findAnyEmptyCell();
            if (pos == null) continue;

            TrapT02_PearlMine mine =
                    new TrapT02_PearlMine(pos[0], pos[1], gm);

            gm.getTraps().add(mine);

            System.out.println("💣 生成 T02 Pearl Mine 于 (" + pos[0] + "," + pos[1] + ")");
        }
    }

    private void grantWaveRewards() {
        Player player = gm.getPlayer();
        if (player == null) return;

        float healthPercent = calculatePlayerHealthPercentage();

        if (healthPercent < 40) {
            player.heal(1);
            System.out.println("🎁 波次奖励：回复1点生命");
        } else if (healthPercent < 70) {
            System.out.println("🎁 波次奖励：获得临时增益");
        } else {
            System.out.println("🎁 波次奖励：属性提升");
        }
    }



    private void spawnSuperHeart() {
        int[] pos = findAnyEmptyCell();
        if (pos != null) {
            System.out.println("🌟 生成超强血包（回3血）");
        }
    }

    private void spawnRarePowerup() {
        int[] pos = findAnyEmptyCell();
        if (pos != null) {
            System.out.println("💎 生成稀有强化物品");
        }
    }

    private void resetWaveSpawnCounters() {
        heartsSpawnedThisWave = 0;
        powerupsSpawnedThisWave = 0;
    }
    private void spawnHealthBasedEnemies(float healthPercent) {
        int spawnCount = calculateSpawnCount(healthPercent);

        for (int i = 0; i < spawnCount; i++) {
            int[] pos = findEmptyCellForEndlessSpawn();
            if (pos == null) continue;

            Enemy enemy = createEnemyBasedOnHealth(healthPercent, pos[0], pos[1]);
            if (enemy != null) {
                gm.getEnemies().add(enemy);
            }
        }

        System.out.println("生成 " + spawnCount + " 个敌人 | 生命值: " + (int)healthPercent + "% | 波次: " + endlessWave);
    }

    private Enemy createEnemyBasedOnHealth(float healthPercent, int x, int y) {
        float randValue = randomGenerator.nextFloat() * 100f;

        try {
            if (healthPercent > 70) {
                if (randValue < 80) {
                    return new EnemyE01_CorruptedPearl(x, y);
                } else {
                    return new EnemyE02_SmallCoffeeBean(x, y);
                }
            }
            else if (healthPercent > 40) {
                if (randValue < 60) {
                    return new EnemyE01_CorruptedPearl(x, y);
                } else if (randValue < 90) {
                    return new EnemyE02_SmallCoffeeBean(x, y);
                } else {
                    return new EnemyE03_CaramelJuggernaut(x, y);
                }
            }
            else if (healthPercent > 20) {
                if (randValue < 40) {
                    return new EnemyE01_CorruptedPearl(x, y);
                } else if (randValue < 80) {
                    return new EnemyE02_SmallCoffeeBean(x, y);
                } else {
                    return new EnemyE03_CaramelJuggernaut(x, y);
                }
            }
            else {
                if (randValue < 20) {
                    return new EnemyE01_CorruptedPearl(x, y);
                } else if (randValue < 60) {
                    return new EnemyE02_SmallCoffeeBean(x, y);
                } else {
                    return new EnemyE03_CaramelJuggernaut(x, y);
                }
            }
        } catch (Exception e) {
            System.out.println("创建敌人失败: " + e.getMessage());
            return new EnemyE01_CorruptedPearl(x, y);
        }
    }

    private int calculateSpawnCount(float healthPercent) {
        int baseCount = 1;

        if (healthPercent > 70) {
            baseCount = 1;
        } else if (healthPercent > 40) {
            baseCount = 2;
        } else if (healthPercent > 20) {
            baseCount = 3;
        } else {
            baseCount = 4;
        }

        return baseCount + (endlessWave / 3);
    }
    private boolean isCellOccupied(int x, int y) {
        Player player = gm.getPlayer();
        if (player != null && player.getX() == x && player.getY() == y) {
            return true;
        }

        List<Enemy> enemiesCopy = new ArrayList<>(gm.getEnemies());
        for (Enemy enemy : enemiesCopy) {
            if (enemy != null && enemy.isActive() && enemy.getX() == x && enemy.getY() == y) {
                if (enemy instanceof EnemyE04_CrystallizedCaramelShell e04) {
                    if (e04.occupiesCell(x, y)) return true;
                }
            }
        }

        List<Heart> heartsCopy = new ArrayList<>(gm.getHearts());
        for (Heart heart : heartsCopy) {
            if (heart != null && heart.isActive() && heart.getX() == x && heart.getY() == y) {
                return true;
            }
        }

        List<Treasure> treasuresCopy = new ArrayList<>(gm.getTreasures());
        for (Treasure treasure : treasuresCopy) {
            if (treasure != null && treasure.isActive() && treasure.getX() == x && treasure.getY() == y) {
                return true;
            }
        }

        List<Key> keysCopy = new ArrayList<>(gm.getKeys());
        for (Key key : keysCopy) {
            if (key != null && key.isActive() && key.getX() == x && key.getY() == y) {
                return true;
            }
        }

        return false;
    }

    private int[] findEmptyCellForEndlessSpawn() {
        Player player = gm.getPlayer();
        if (player == null) return findAnyEmptyCell();

        int playerX = player.getX();
        int playerY = player.getY();

        for (int attempt = 0; attempt < 50; attempt++) {
            int x = BORDER_THICKNESS + random.nextInt(
                    difficultyConfig.mazeWidth - BORDER_THICKNESS * 2
            );
            int y = BORDER_THICKNESS + random.nextInt(
                    difficultyConfig.mazeHeight - BORDER_THICKNESS * 2
            );

            if (isCellWalkable(x, y) && !isCellOccupied(x, y)) {
                if (Math.abs(x - playerX) > 5 || Math.abs(y - playerY) > 5) {
                    return new int[]{x, y};
                }
            }
        }

        return findAnyEmptyCell();
    }

    private boolean isCellWalkable(int x, int y) {
        if (x < 0 || y < 0 || x >= difficultyConfig.mazeWidth || y >= difficultyConfig.mazeHeight) {
            return false;
        }

        int[][] mazeArray = gm.getMaze();
        if (mazeArray == null || y >= mazeArray.length || x >= mazeArray[0].length) {
            return false;
        }

        return mazeArray[y][x] == 1;
    }


    private int[] findAnyEmptyCell() {
        int width = difficultyConfig.mazeWidth;
        int height = difficultyConfig.mazeHeight;

        for (int attempt = 0; attempt < 100; attempt++) {
            int x = BORDER_THICKNESS + random.nextInt(width - BORDER_THICKNESS * 2);
            int y = BORDER_THICKNESS + random.nextInt(height - BORDER_THICKNESS * 2);

            if (isCellWalkable(x, y) && !isCellOccupied(x, y)) {
                return new int[]{x, y};
            }
        }

        return new int[]{BORDER_THICKNESS + 1, BORDER_THICKNESS + 1};
    }
    private void spawnEndlessHealthPack() {
        int[] pos = findAnyEmptyCell();
        if (pos != null) {
            try {
                Heart heart = new Heart(pos[0], pos[1]);
                gm.getHearts().add(heart);
                System.out.println("生成生命包于位置 (" + pos[0] + ", " + pos[1] + ")");
            } catch (Exception e) {
                System.out.println("生成生命包失败: " + e.getMessage());
            }
        }
    }
    private void spawnInitialEndlessEnemies() {
        for (int i = 0; i < 3; i++) {
            int[] pos = findEmptyCellForEndlessSpawn();
            if (pos != null) {
                try {
                    gm.getEnemies().add(new EnemyE01_CorruptedPearl(pos[0], pos[1]));
                } catch (Exception e) {
                    System.out.println("初始敌人生成失败: " + e.getMessage());
                }
            }
        }
    }


    private int calculateEndlessScore() {
        int timeScore = (int)(endlessSurvivalTime * 10);
        int killScore = endlessKills * 100;
        int waveBonus = endlessWave * 500;
        float healthPercent = calculatePlayerHealthPercentage();
        int healthBonus = (int)(healthPercent * 10);

        return timeScore + killScore + waveBonus + healthBonus;
    }
    private void showEndlessGameOverScreen() {
        endlessGameOverStage = new Stage(new ScreenViewport());
        Table root = new Table();
        root.setFillParent(true);
        endlessGameOverStage.addActor(root);

        int finalScore = calculateEndlessScore();

        root.add(new Label("Game Over", game.getSkin(), "title"))
                .padBottom(40).row();

        root.add(new Label(
                String.format("SURVIVAL TIME: %02d min  %02d sec",
                        (int)endlessSurvivalTime / 60,
                        (int)endlessSurvivalTime % 60),
                game.getSkin()
        )).padBottom(10).row();

        root.add(new Label(
                String.format("LEVEL %d", endlessWave),
                game.getSkin()
        )).padBottom(10).row();

        root.add(new Label(
                String.format("ENEMY KILLED: %d", endlessKills),
                game.getSkin()
        )).padBottom(10).row();

        root.add(new Label(
                String.format("DAMAGE TAKEN: %d", endlessDamageTaken),
                game.getSkin()
        )).padBottom(10).row();

        root.add(new Label(
                String.format("SCORE: %d", finalScore),
                game.getSkin()
        )).padBottom(40).row();

        ButtonFactory bf = new ButtonFactory(game.getSkin());

        root.add(bf.create("try again", () -> {
            game.startNewGame(Difficulty.ENDLESS);
            game.goToGame();
        })).width(400).height(80).padBottom(20).row();

        root.add(bf.create("MENU", () -> {
            game.goToMenu();
        })).width(400).height(80).row();

        Gdx.input.setInputProcessor(endlessGameOverStage);
        endlessGameOverUIInitialized = true;
    }


    private boolean isEndlessMode() {
        return difficultyConfig.difficulty == Difficulty.ENDLESS;
    }

    private void handleInput(float delta) {
        if (KeyBindingManager.getInstance()
                .isJustPressed(KeyBindingManager.GameAction.PAUSE)) {

            if (!endlessGameOver) {
                togglePause();
            }
            return;
        }
        if (KeyBindingManager.getInstance().isJustPressed(KeyBindingManager.GameAction.CONSOLE)) {
            console.toggle();
        }

        if (paused || console.isVisible() || gm.isLevelTransitionInProgress() || endlessGameOver) {
            return;
        }
        input.update(delta, new PlayerInputHandler.InputHandlerCallback() {

            @Override
            public void onMoveInput(Player.PlayerIndex index, int dx, int dy) {
                gm.onMoveInput(index, dx, dy);
            }

            @Override
            public float getMoveDelayMultiplier() {
                return 1.0f;
            }

            @Override
            public boolean onAbilityInput(Player.PlayerIndex index, int slot) {
                return gm.onAbilityInput(index, slot);
            }

            @Override
            public void onInteractInput(Player.PlayerIndex index) {
                gm.onInteractInput(index);
            }


            @Override
            public boolean isUIConsumingMouse() {
                return gm.isUIConsumingMouse();
            }

            // 🔥 [修复] 实现缺失方法
            @Override
            public void onMenuInput() {
                togglePause();
            }

        }, Player.PlayerIndex.P1);


        if (gm.isTwoPlayerMode()) {
            input.update(delta, new PlayerInputHandler.InputHandlerCallback() {

                @Override
                public void onMoveInput(Player.PlayerIndex index, int dx, int dy) {
                    gm.onMoveInput(index, dx, dy);
                }

                @Override
                public float getMoveDelayMultiplier() {
                    return 1.0f;
                }

                @Override
                public boolean onAbilityInput(Player.PlayerIndex index, int slot) {
                    return gm.onAbilityInput(index, slot);
                }

                @Override
                public void onInteractInput(Player.PlayerIndex index) {
                    gm.onInteractInput(index);
                }


                @Override
                public boolean isUIConsumingMouse() {
                    return gm.isUIConsumingMouse();
                }

                @Override
                public void onMenuInput() {
                    togglePause();
                }

            }, Player.PlayerIndex.P2);
        }
    }
    private void togglePause() {
        paused = !paused;

        if (paused) {
            if (pauseStage == null) {
                initPauseUI();
            }
            Gdx.input.setInputProcessor(pauseStage);
        } else {
            Gdx.input.setInputProcessor(null);
            pauseUIInitialized = false;
            pauseStage = null;
        }

        Gdx.app.log("EndlessScreen", paused ? "Paused" : "Resumed");
    }
    private void initPauseUI() {
        pauseStage = new Stage(new ScreenViewport());
        Table root = new Table();
        root.setFillParent(true);
        pauseStage.addActor(root);

        Label title = new Label("PAUSED", game.getSkin(), "title");
        root.add(title).padBottom(40).row();

        ButtonFactory bf = new ButtonFactory(game.getSkin());

        float btnW = 350;
        float btnH = 90;
        float pad  = 15;

        root.add(bf.create("CONTINUE", this::togglePause))
                .width(btnW).height(btnH).pad(pad).row();

        root.add(bf.create("RESET", () -> {
            paused = false;
            pauseUIInitialized = false;

            game.startNewGame(Difficulty.ENDLESS);
            game.goToGame();
        })).width(btnW).height(btnH).pad(pad).row();

        root.add(bf.create("SETTINGS", () -> {
            game.setScreen(
                    new SettingsScreen(
                            game,
                            SettingsScreen.SettingsSource.PAUSE_MENU,
                            this   // ⭐ 非常重要
                    )
            );
        })).width(btnW).height(btnH).pad(pad).row();

        root.add(bf.create("MENU", game::goToMenu))
                .width(btnW).height(btnH).pad(pad);

        pauseUIInitialized = true;
    }

    private List<Item> prepareRenderItems(List<ExitDoor> exitDoorsCopy) {
        List<Item> items = new ArrayList<>();

        for (var wg : maze.getWallGroups()) {
            boolean front = maze.isWallInFrontOfAnyEntity(wg.startX, wg.startY);
            items.add(new Item(wg, front ? Type.WALL_FRONT : Type.WALL_BEHIND));
        }

        for (Player p : gm.getPlayers()) {
            if (!p.isDead()) {
                items.add(new Item(p, 100));
            }
        }


        List<Enemy> enemiesCopy = new ArrayList<>(gm.getEnemies());
        enemiesCopy.forEach(e -> items.add(new Item(e, 50)));
        List<Trap> trapsCopy = new ArrayList<>(gm.getTraps());
        trapsCopy.forEach(t -> {
            if (t != null && t.isActive()) {
                items.add(new Item(t, 10));
            }
        });
        exitDoorsCopy.forEach(d -> items.add(new Item(d, 45)));

        List<Heart> heartsCopy = new ArrayList<>(gm.getHearts());
        heartsCopy.forEach(h -> {
            if (h.isActive()) items.add(new Item(h, 30));
        });

        List<Treasure> treasuresCopy = new ArrayList<>(gm.getTreasures());
        treasuresCopy.forEach(t -> items.add(new Item(t, 20)));
        List<HeartContainer> containersCopy = new ArrayList<>(gm.getHeartContainers());

        if (containersCopy.isEmpty()) {
            System.out.println("🧡 [Render] No HeartContainer in GameManager");
        } else {
            System.out.println("🧡 [Render] HeartContainer count = " + containersCopy.size());
        }

        containersCopy.forEach(hc -> {
            System.out.println(
                    "🧡 [Render] HeartContainer at (" + hc.getX() + "," + hc.getY() +
                            "), active=" + hc.isActive()
            );

            if (hc.isActive()) {
                items.add(new Item(hc, 30));
                System.out.println("✅ [Render] HeartContainer ADDED to render items");
            } else {
                System.out.println("⚠️ [Render] HeartContainer NOT active, skipped");
            }
        });
        List<Key> keysCopy = new ArrayList<>(gm.getKeys());
        keysCopy.forEach(k -> {
            if (k.isActive()) {
                items.add(new Item(k, 35));
            }
        });

        return items;
    }

    @Override
    public void resize(int width, int height) {
        resizing = true;

        if (cam != null) {
            cam.resize(width, height);
        }

        if (console != null) {
            console.resize(width, height);
        }

        if (pauseStage != null) {
            pauseStage.getViewport().update(width, height, true);
        }

        if (endlessGameOverStage != null) {
            endlessGameOverStage.getViewport().update(width, height, true);
        }

        resizing = false;
    }



    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        if (maze != null) maze.dispose();
        if (console != null) console.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
        if (uiTop != null) uiTop.dispose();
        if (uiBottom != null) uiBottom.dispose();
        if (uiLeft != null) uiLeft.dispose();
        if (uiRight != null) uiRight.dispose();
        if (pauseStage != null) pauseStage.dispose();
        if (endlessGameOverStage != null) endlessGameOverStage.dispose();
        heartCreationTimes.clear();
    }
}