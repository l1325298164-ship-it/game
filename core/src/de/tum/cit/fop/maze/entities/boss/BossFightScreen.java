package de.tum.cit.fop.maze.entities.boss;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import de.tum.cit.fop.maze.MazeRunnerGame;
import de.tum.cit.fop.maze.abilities.Ability;
import de.tum.cit.fop.maze.abilities.AbilityManager;
import de.tum.cit.fop.maze.audio.AudioManager;
import de.tum.cit.fop.maze.audio.AudioType;
import de.tum.cit.fop.maze.entities.*;
import de.tum.cit.fop.maze.entities.boss.config.*;
import de.tum.cit.fop.maze.entities.enemy.Enemy;
import de.tum.cit.fop.maze.game.DifficultyConfig;
import de.tum.cit.fop.maze.game.GameConstants;
import de.tum.cit.fop.maze.game.GameManager;
import de.tum.cit.fop.maze.maze.BossMazeRenderer;
import de.tum.cit.fop.maze.maze.MazeRenderer;
import de.tum.cit.fop.maze.screen.MenuScreen;
import de.tum.cit.fop.maze.utils.BlockingInputProcessor;
import de.tum.cit.fop.maze.utils.BossCamera;
import de.tum.cit.fop.maze.utils.BossMazeCamera;
import de.tum.cit.fop.maze.utils.CameraManager;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
/**
 * Main screen responsible for handling the entire boss fight.
 * <p>
 * This screen coordinates boss rendering, maze gameplay,
 * phase transitions, rage mechanics, timeline-driven events,
 * and victory or failure flow.
 */
public class BossFightScreen implements Screen {

    private float introDelayTimer = 0f;
    private static final float INTRO_DELAY =10f;
    private static final float INTRO_FADE_TIME = 1.0f;

    private enum BossRageState {
        NORMAL,
        RAGE_WARNING,
        RAGE_PUNISH,
        FINAL_LOCKED,
        AUTO_DEATH
    }
    private float rageFlashTimer = 0f;
    private static final float RAGE_FLASH_DURATION = 0.15f;

    private boolean inVictoryHold = false;


    private boolean cupShakeActive = false;
    private float cupShakeTimer = 0f;
    private float cupShakeDuration = 0f;

    private float cupShakeXAmp = 0f;
    private float cupShakeYAmp = 0f;
    private float cupShakeXFreq = 1f;
    private float cupShakeYFreq = 1f;


    private float aoeCycleTime = 0f;
    private final Map<AoeTimeline.AoePattern, Float> aoeTimers = new HashMap<>();


    private final GlyphLayout glyphLayout = new GlyphLayout();

    private Sound currentDialogueSound;

    private BossTimeline bossTimeline;
    private BossTimelineRunner timelineRunner;


    private BossRageState rageState = BossRageState.NORMAL;

    private float rageAoeTimer = 0f;
    private float rageAoeTickTimer = 0f;
    private static final float RAGE_AOE_DURATION = 3f;

    private float bossTimelineTime = 0f;


    private boolean showMazeWarning = false;
    private float mazeWarningTimer = 0f;
    private static final float MAZE_WARNING_TIME = 10f;
    private BitmapFont uiFont;
    private boolean phaseSwitchQueued = false;



    private OrthographicCamera uiCamera;
    private Texture aoeFillTex;
    private Texture aoeRingTex;
    private float bossMaxHp = 1000f;
    private float bossHp = bossMaxHp;

    private de.tum.cit.fop.maze.ui.HUD hud;

    private Texture teacupTex;
    private float teacupWorldX = 640f;
    private float teacupWorldY = 230f;
    private float teacupSize   = 920f;

    private float cupRadius;
    private float cupCenterX;
    private float cupCenterY;
    private static final float BOSS_WIDTH  = 1320f;
    private static final float BOSS_HEIGHT = 1120f;

    private float mazeSlideOffsetY = 0f;
    private float mergeProgress = 0f;
    private static final float MERGE_TIME = 3.6f;
    private float mergeTimer = 0f;
    private float phaseTime = 0f;
    private enum BossDeathState {
        NONE,
        TRIGGERED,
        MERGING_SCREEN,
        PLAYING_DEATH,
        FINISHED
    }

    private BossDeathState bossDeathState = BossDeathState.NONE;
    private float bossDeathTimer = 0f;

    private ShapeRenderer shapeRenderer;

    private enum PhaseTransitionState {
        NONE,
        FREEZE,
        FADING_OUT,
        SWITCHING,
        FADING_IN
    }
    private static class BossAOE {
        float x;
        float y;

        float radius;

        float life;
        float maxLife;

        float warningTime;
        boolean active;
        boolean damageDone;

        int damage;
        final Set<Player> damagedPlayers = new HashSet<>();
    }
    private float rageOverlayPulse = 0f;

    private final List<BossAOE> activeAOEs = new ArrayList<>();
    private PhaseTransitionState transitionState = PhaseTransitionState.NONE;
    private float transitionTimer = 0f;

    private float fadeAlpha = 0f;
    private static final float FREEZE_TIME = 0.5f;
    private static final float FADE_TIME = 0.4f;

    private BossMazePhaseSelector phaseSelector;
    private BossMazeConfig currentBossConfig;

    private BossCamera bossCamera;
    private CameraManager mazeCameraManager;
    private BossMazeCamera bossMazeCamera;

    private Viewport bossViewport;
    private Viewport mazeViewport;

    private GameManager gameManager;
    private DifficultyConfig difficultyConfig;
    private MazeRenderer mazeRenderer;

    private Player player;
    private final MazeRunnerGame game;
    private SpriteBatch batch;


    private int screenWidth;
    private int screenHeight;
    private boolean phaseShakeActive = false;
    private float phaseShakeTimer = 0f;
    private float phaseShakeDuration = 0f;

    private float phaseShakeXAmp;
    private float phaseShakeYAmp;
    private float phaseShakeXFreq;
    private float phaseShakeYFreq;

    private static final float MAZE_VIEW_CELLS_WIDTH = 20f;
    private static final float MAZE_VIEW_CELLS_HEIGHT = 17f;
    private TextureAtlas bossAtlas;
    private Animation<TextureRegion> bossAnim;
    private float bossAnimTime = 0f;
    /**
     * Creates a new boss fight screen.
     *
     * @param game main game instance
     */
    public BossFightScreen(MazeRunnerGame game) {
        this.game = game;
    }
    /**
     * Initializes all boss fight resources and prepares
     * the initial maze and timeline state.
     */
    @Override
    public void show() {
        Gdx.input.setInputProcessor(null);


        uiCamera = new OrthographicCamera();
        uiCamera.setToOrtho(
                false,
                Gdx.graphics.getWidth(),
                Gdx.graphics.getHeight()
        );
        uiCamera.update();
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();


        uiFont = game.getSkin().get("default-font", BitmapFont.class);

        var assets = game.getAssets();

        teacupTex = assets.get("story_file/boss/teacup_top.png", Texture.class);

        bossAtlas = assets.get("story_file/boss/bossFight/BOSS_PV.atlas", TextureAtlas.class);

        bossAnim = new Animation<>(
                1f / 24f,
                bossAtlas.getRegions(),
                Animation.PlayMode.LOOP
        );

        aoeFillTex = new Texture(Gdx.files.internal("effects/aoe_fill.png"));
        aoeRingTex = new Texture(Gdx.files.internal("effects/aoe_ring.png"));
        bossTimeline = BossTimelineLoader.load("story_file/boss/boss_timeline.json");
        timelineRunner = new BossTimelineRunner(bossTimeline);

        currentBossConfig = BossMazeConfigLoader.loadOne("story_file/boss/boss_phases.json");
        phaseSelector = new BossMazePhaseSelector(currentBossConfig.phases);
        if (currentBossConfig.aoeTimeline != null) {
            Gdx.app.log(
                    "BOSS_AOE",
                    "patterns = " + currentBossConfig.aoeTimeline.patterns.size
            );
        }
        bossCamera = new BossCamera(1280, 720);
        bossCamera.getCamera().position.set(640f, 360f, 0f);
        bossCamera.getCamera().update();

        screenWidth = Gdx.graphics.getWidth();
        screenHeight = Gdx.graphics.getHeight();

        bossViewport = new FitViewport(1280, 720, bossCamera.getCamera());

        bossDeathState = BossDeathState.NONE;
        bossDeathTimer = 0f;
        mergeTimer = 0f;
        mergeProgress = 0f;
        mazeSlideOffsetY = 0f;

        transitionState = PhaseTransitionState.NONE;
        transitionTimer = 0f;
        fadeAlpha = 0f;
        pendingInitialPhase = phaseSelector.getCurrent();
        gameManager = null;
        aoeTimers.clear();
        aoeCycleTime = 0f;

        preloadInitialMaze();

    }
    private BossMazeConfig.Phase pendingInitialPhase;
    private boolean mazeStarted = false;
    /**
     * Renders the boss fight and updates all active systems.
     * <p>
     * This includes boss animation, maze rendering, AOE effects,
     * phase transitions, timeline execution, and UI overlays.
     *
     * @param delta time elapsed since the last frame
     */
    @Override
    public void render(float delta) {
        bossAnimTime += delta;



        if (rageState == BossRageState.RAGE_PUNISH) {
            rageOverlayPulse += delta * 4f; // 呼吸速度
        } else {
            rageOverlayPulse = 0f;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.H)) {
            bossHp -= 50f;
            bossHp = Math.max(0f, bossHp);
            hud.updateBossHp(bossHp);
        }


        boolean isMergingOrAfter =
                bossDeathState == BossDeathState.TRIGGERED
                        || bossDeathState == BossDeathState.MERGING_SCREEN
                        || bossDeathState == BossDeathState.PLAYING_DEATH
                        || bossDeathState == BossDeathState.FINISHED;

        if ( Gdx.input.isKeyJustPressed(Input.Keys.K)) {
            enterVictoryMode();
        }


        update(delta);


        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);


        bossViewport.apply();
        batch.setProjectionMatrix(bossCamera.getCamera().combined);
        batch.begin();


        float worldWidth = bossViewport.getWorldWidth();
        float worldHeight = bossViewport.getWorldHeight();
        float bossWorldX = worldWidth / 2 - BOSS_WIDTH / 2;
        float bossWorldY = -100f;

        TextureRegion bossFrame = bossAnim.getKeyFrame(bossAnimTime);

        batch.draw(
                bossFrame,
                bossWorldX,
                bossWorldY,
                BOSS_WIDTH,
                BOSS_HEIGHT
        );






        float shakeX = 0f;
        float shakeY = 0f;

        if (cupShakeActive) {
            float t = cupShakeTimer;
            shakeX += MathUtils.sin(t * cupShakeXFreq) * cupShakeXAmp;
            shakeY += MathUtils.cos(t * cupShakeYFreq) * cupShakeYAmp;
        }

        if (phaseShakeActive) {
            float t = phaseShakeTimer;
            shakeX += MathUtils.sin(t * phaseShakeXFreq) * phaseShakeXAmp;
            shakeY += MathUtils.cos(t * phaseShakeYFreq) * phaseShakeYAmp;
        }
        if (shouldRenderGameplay()) {
            batch.draw(
                    teacupTex,
                    teacupWorldX - teacupSize / 2f + shakeX,
                    teacupWorldY - teacupSize / 2f + shakeY,
                    teacupSize,
                    teacupSize
            );
        }
        batch.end();




        if (mazeStarted
                && mazeViewport != null
                && mazeCameraManager != null
                && gameManager != null
                && gameManager.getPlayer() != null
                && shouldRenderGameplay()) {

            mazeViewport.apply();

            if (!isMazeFrozen()) {
                bossMazeCamera.update(delta, gameManager.getPlayers());
            }
            updateMouseTargetForBossMaze();
            OrthographicCamera cam = mazeCameraManager.getCamera();
            cam.update();
            cupCenterX = cam.position.x + shakeX;
            cupCenterY = cam.position.y + shakeY;
            cupRadius  = cam.viewportHeight * cam.zoom * 0.30f;

            Gdx.gl.glEnable(GL20.GL_STENCIL_TEST);
            Gdx.gl.glClearStencil(0);
            Gdx.gl.glClear(GL20.GL_STENCIL_BUFFER_BIT);

            Gdx.gl.glStencilFunc(GL20.GL_ALWAYS, 1, 0xFF);
            Gdx.gl.glStencilOp(GL20.GL_KEEP, GL20.GL_KEEP, GL20.GL_REPLACE);
            Gdx.gl.glColorMask(false, false, false, false);
            shapeRenderer.setProjectionMatrix(cam.combined);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

            float ellipseRadius = cupRadius;
            float ellipseScaleX = 1.15f;
            float ellipseScaleY = 0.85f;

            shapeRenderer.identity();

            shapeRenderer.translate(cupCenterX, cupCenterY, 0);

            shapeRenderer.scale(ellipseScaleX, ellipseScaleY, 1f);

            shapeRenderer.circle(0, 0, ellipseRadius, 64);

            shapeRenderer.identity();

            shapeRenderer.end();

            Gdx.gl.glColorMask(true, true, true, true);
            Gdx.gl.glStencilFunc(GL20.GL_EQUAL, 1, 0xFF);
            Gdx.gl.glStencilOp(GL20.GL_KEEP, GL20.GL_KEEP, GL20.GL_KEEP);

            batch.setProjectionMatrix(cam.combined);
            batch.begin();

            mazeRenderer.renderFloor(batch);
            for (MazeRenderer.WallGroup g : mazeRenderer.getWallGroups()) {
                mazeRenderer.renderWallGroup(batch, g);
            }



            for (Key k : gameManager.getKeys()) {
                if (k != null && k.isActive()) {
                    k.drawSprite(batch);
                }
            }

            for (Heart h : gameManager.getHearts()) {
                if (h != null && h.isActive()) {
                    h.drawSprite(batch);
                }
            }

            for (Treasure t : gameManager.getTreasures()) {
                if (t != null && t.isActive()) {
                    t.drawSprite(batch);
                }
            }

            for (HeartContainer hc : gameManager.getHeartContainers()) {
                if (hc != null && hc.isActive()) {
                    hc.drawSprite(batch);
                }
            }







            if (!activeAOEs.isEmpty()) {
                Gdx.gl.glEnable(GL20.GL_BLEND);
                Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

                batch.setProjectionMatrix(mazeCameraManager.getCamera().combined);


                for (BossAOE aoe : activeAOEs) {
                    float size = aoe.radius * 2f;
                    float drawX = aoe.x - aoe.radius;
                    float drawY = aoe.y - aoe.radius;




                    batch.setColor(1f, 1f, 1f, 0.35f);
                    batch.draw(aoeFillTex, drawX, drawY, size, size);

                    if (aoe.active) {
                        batch.setColor(1f, 0.1f, 0.1f, 0.9f);
                    } else {
                        batch.setColor(1f, 0.8f, 0.3f, 0.9f);
                    }

                    batch.draw(aoeRingTex, drawX, drawY, size, size);
                }

                batch.setColor(1f, 1f, 1f, 1f);

            }
            for (Player p : gameManager.getPlayers()) {
                if (p != null && !p.isDead()) {
                    p.drawSprite(batch);
                }
            }

            for (Enemy e : gameManager.getEnemies()) {
                if (e.isActive()) {
                    e.drawSprite(batch);
                }
            }
            if (gameManager.getBobaBulletEffectManager() != null) {
                gameManager.getBobaBulletEffectManager().render(batch);
            }
            if (gameManager.getCombatEffectManager() != null) {
                gameManager.getCombatEffectManager().renderSprites(batch);
            }




            shapeRenderer.setProjectionMatrix(
                    mazeCameraManager.getCamera().combined
            );

            for (Player p : gameManager.getPlayers()) {
                if (p == null || p.isDead()) continue;

                AbilityManager am = p.getAbilityManager();
                if (am == null) continue;

                am.drawAbilities(batch, shapeRenderer, p);
            }



            batch.end();

            if (gameManager.getCombatEffectManager() != null) {
                Gdx.gl.glEnable(GL20.GL_BLEND);
                Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

                shapeRenderer.setProjectionMatrix(cam.combined);
                shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                gameManager.getCombatEffectManager().renderShapes(shapeRenderer);
                shapeRenderer.end();
            }
            Gdx.gl.glDisable(GL20.GL_STENCIL_TEST);










            if (showMazeWarning) {
                renderMazeRebuildWarning();
            }



            batch.setProjectionMatrix(uiCamera.combined);
            batch.begin();
            hud.renderInGameUI(batch, true);
            batch.end();





        }


        if (fadeAlpha > 0f && !isMergingOrAfter) {
            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

            shapeRenderer.setProjectionMatrix(
                    mazeCameraManager.getCamera().combined
            );

            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(0f, 0f, 0f, fadeAlpha);

            shapeRenderer.rect(
                    0,
                    0,
                    difficultyConfig.mazeWidth * GameConstants.CELL_SIZE,
                    difficultyConfig.mazeHeight * GameConstants.CELL_SIZE
            );

            shapeRenderer.end();
            Gdx.gl.glDisable(GL20.GL_BLEND);
        }


        if (rageState == BossRageState.RAGE_PUNISH) {
            drawRageOverlay();
        }
        drawRageFlashOverlay();


        
    }

    private void drawRageFlashOverlay() {
        if (rageFlashTimer <= 0f) return;

        float alpha = rageFlashTimer / RAGE_FLASH_DURATION; // 1 → 0

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.setProjectionMatrix(uiCamera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        shapeRenderer.setColor(
                0.9f,
                0.1f,
                0.1f,
                alpha * 0.8f
        );

        shapeRenderer.rect(
                0,
                0,
                Gdx.graphics.getWidth(),
                Gdx.graphics.getHeight()
        );

        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }


    private void updateMouseTargetForBossMaze() {

        if (mazeCameraManager == null || gameManager == null) return;

        OrthographicCamera cam = mazeCameraManager.getCamera();

        Vector3 world = cam.unproject(
                new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0)
        );

        gameManager.setMouseTargetTile(
                (int)(world.x / GameConstants.CELL_SIZE),
                (int)(world.y / GameConstants.CELL_SIZE)
        );
    }


    private void drawRageOverlay() {
        float pulse =
                0.35f
                        + 0.10f * MathUtils.sin(rageOverlayPulse);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.setProjectionMatrix(uiCamera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        shapeRenderer.setColor(
                0.35f,
                0.05f,
                0.05f,
                pulse
        );

        shapeRenderer.rect(
                0,
                0,
                Gdx.graphics.getWidth(),
                Gdx.graphics.getHeight()
        );

        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }



    private float failTimer = 0f;


    private void enterVictoryMode() {
        inVictoryHold = true;

        victoryEndTimer = 0f;

        activeAOEs.clear();
        showMazeWarning = false;
        transitionState = PhaseTransitionState.NONE;
    }


    private void renderMazeRebuildWarning() {
        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();

        float boxW = 420f;
        float boxH = 140f;
        float boxX = w / 2f ;
        float boxY = h * 0.82f;

        float blink =
                0.75f + 0.25f * MathUtils.sin(mazeWarningTimer * 6f);


        batch.setProjectionMatrix(uiCamera.combined);
        batch.begin();

        batch.setColor(0.15f, 0.12f, 0.05f, blink);

        String title = "ATTENTION";
        int seconds = MathUtils.ceil(mazeWarningTimer);
        uiFont.getData().setScale(0.5f);
        uiFont.setColor(0.92f, 0.90f, 0.78f, blink);

        glyphLayout.setText(uiFont, title);

        float textX = boxX - glyphLayout.width / 2f;

        float textY = boxY + boxH - 30f;

        uiFont.draw(batch, glyphLayout, textX, textY);

        uiFont.draw(
                batch,
                String.valueOf(seconds),
                boxX,
                boxY + 40
        );

        batch.setColor(1, 1, 1, 1);
        batch.end();
    }

    private float pvTimer = 0f;
    private static final float BGM_DELAY = 0.1f;
    private boolean bossBgmStarted = false;

    private void update(float delta) {
        updateRagePunish(delta);
        if (rageFlashTimer > 0f) {
            rageFlashTimer -= delta;
        }
        if (checkPlayersDeath()) return;
        if (gameManager != null && hud != null) {
            gameManager.setUIConsumesMouse(
                    hud.isMouseOverInteractiveUI()
            );
        }

        pvTimer += delta;
        if (!bossBgmStarted && pvTimer >= BGM_DELAY) {
            AudioManager.getInstance().playMusic(AudioType.BOSS_BGM);
            bossBgmStarted = true;
        }

        bossTimelineTime += delta;
        timelineRunner.update(bossTimelineTime, this);


        if (mazePaused) {
            introDelayTimer += delta;

            if (introDelayTimer >= INTRO_DELAY) {
                mazePaused = false;   // ⭐ 解锁渲染 + update
            }
            return; // 只挡迷宫逻辑，不挡 Boss
        }
        if (inVictoryHold) {
            victoryEndTimer += delta;

            if (victoryEndTimer >= VICTORY_PV_TIME) {
                AudioManager.getInstance().stopMusic();
                game.setScreen(new BossStoryScreen(game));
            }
            return;
        }




        if (!isMazeFrozen()) {
            gameManager.update(delta);
        }

        updateCupShake(delta);

        updateAoeTimeline(delta);
        updatePhaseTransition(delta);
        updateBossDeath(delta);
        updateActiveAOEs(delta);
    }




    private void updateCupShake(float delta) {
        if (!cupShakeActive) return;

        cupShakeTimer += delta;
        if (cupShakeTimer >= cupShakeDuration) {
            cupShakeActive = false;
        }
    }
    private void updateRagePunish(float delta) {
        if (rageState != BossRageState.RAGE_PUNISH) return;

        rageAoeTimer += delta;
        rageAoeTickTimer += delta;

        if (rageAoeTickTimer >= 1f) {
            rageAoeTickTimer = 0f;

            Gdx.app.log(
                    "RAGE_AOE",
                    "Tick! timer=" + rageAoeTimer
                            + " players=" + gameManager.getPlayers().size()
            );

            for (Player p : gameManager.getPlayers()) {
                if (p == null) {
                    Gdx.app.log("RAGE_AOE", "Player is null");
                    continue;
                }
                if (p.isDead()) {
                    Gdx.app.log("RAGE_AOE", "Player " + p.getPlayerIndex() + " is dead");
                    continue;
                }

                int before = p.getLives();
                p.takeDamage(5);
                int after = p.getLives();

                Gdx.app.log(
                        "RAGE_AOE",
                        "Player " + p.getPlayerIndex()
                                + " HP: " + before + " -> " + after
                );
            }

            rageFlashTimer = RAGE_FLASH_DURATION;
        }

        if (rageAoeTimer >= RAGE_AOE_DURATION) {
            Gdx.app.log("RAGE_AOE", "Rage punish finished");
            rageState = BossRageState.NORMAL;
        }
    }


    private void updateAoeTimeline(float delta) {

        if (isMazeFrozen() || currentBossConfig.aoeTimeline == null) return;

        AoeTimeline timeline = currentBossConfig.aoeTimeline;
        aoeCycleTime += delta;

        float t = aoeCycleTime % timeline.cycle;

        for (AoeTimeline.AoePattern pattern : timeline.patterns) {

            if (t < pattern.start || t > pattern.end) {
                aoeTimers.remove(pattern);
                continue;
            }

            float timer = aoeTimers.getOrDefault(pattern, 0f) + delta;

            if (timer >= pattern.interval) {
                timer = 0f;

                for (Player p : gameManager.getPlayers()) {
                    if (p == null || p.isDead()) continue;

                    for (int i = 0; i < pattern.count; i++) {
                        spawnTimelineAOE(p, pattern.radius, pattern.damage);
                    }
                }
            }

            aoeTimers.put(pattern, timer);
        }
    }

    private void updatePhaseTransition(float delta) {

        if (showMazeWarning) {
            mazeWarningTimer -= delta;
            if (mazeWarningTimer <= 0f) {
                showMazeWarning = false;
                transitionState = PhaseTransitionState.FREEZE;
                transitionTimer = 0f;
            }
        }

        phaseTime += delta;

        if (phaseShakeActive) {
            phaseShakeTimer += delta;
            if (phaseShakeTimer >= phaseShakeDuration) {
                phaseShakeActive = false;
            }
        }

        switch (transitionState) {

            case NONE -> {
                if (!phaseSwitchQueued
                        && bossDeathState == BossDeathState.NONE
                        && phaseSelector.shouldPrepareNextPhase(delta)) {

                    phaseSwitchQueued = true;
                    triggerPhaseShake();
                    showMazeWarning = true;
                    mazeWarningTimer = MAZE_WARNING_TIME;
                }
            }

            case FREEZE -> {
                transitionTimer += delta;
                if (transitionTimer >= FREEZE_TIME) {
                    transitionState = PhaseTransitionState.FADING_OUT;
                    transitionTimer = 0f;
                }
            }

            case FADING_OUT -> {
                transitionTimer += delta;
                fadeAlpha = Math.min(1f, transitionTimer / FADE_TIME);
                if (fadeAlpha >= 1f) {
                    transitionState = PhaseTransitionState.SWITCHING;
                }
            }

            case SWITCHING -> {
                BossMazeConfig.Phase next = phaseSelector.advanceAndGet();
                applyPhase(next);

                phaseSwitchQueued = false;
                transitionState = PhaseTransitionState.FADING_IN;
                transitionTimer = 0f;
            }

            case FADING_IN -> {
                transitionTimer += delta;
                fadeAlpha = 1f - Math.min(1f, transitionTimer / FADE_TIME);
                if (fadeAlpha <= 0f) {
                    fadeAlpha = 0f;
                    transitionState = PhaseTransitionState.NONE;
                }
            }
        }
    }
    private void updateActiveAOEs(float delta) {

        for (int i = activeAOEs.size() - 1; i >= 0; i--) {
            BossAOE aoe = activeAOEs.get(i);
            aoe.life -= delta;

            if (!aoe.active && aoe.life <= aoe.maxLife - aoe.warningTime) {
                aoe.active = true;
            }

            if (aoe.life <= 0f) {
                activeAOEs.remove(i);
                continue;
            }

            if (!aoe.active) continue;

            for (Player p : gameManager.getPlayers()) {
                if (p == null || p.isDead()) continue;

                if (!aoe.damagedPlayers.contains(p)
                        && isPlayerInsideAOE(p, aoe)) {

                    p.takeDamage(aoe.damage);
                    aoe.damagedPlayers.add(p);
                }
            }
        }
    }


    private void triggerPhaseShake() {
        phaseShakeActive = true;
        phaseShakeTimer = 0f;
        phaseShakeDuration = 0.6f;

        phaseShakeXAmp = 9f;
        phaseShakeYAmp = 7f;
        phaseShakeXFreq = 2.8f;
        phaseShakeYFreq = 2.4f;
    }



private boolean mazePreloaded = false;
    private boolean mazePaused = true;

private final Map<Integer, BossPhasePreloadData> phaseCache =
        new ConcurrentHashMap<>();

    private void preloadInitialMaze() {

    if (mazePreloaded) return;

    DifficultyConfig dc =
            BossDifficultyFactory.create(
                    currentBossConfig.base,
                    pendingInitialPhase
            );

    gameManager = new GameManager(dc,game.isTwoPlayerMode());
    gameManager.resetGame();
    gameManager.respawnPlayersTogetherForBoss();
    player = gameManager.getPlayer();

    hud = new de.tum.cit.fop.maze.ui.HUD(gameManager);
    hud.enableBossHUD(bossMaxHp);
    hud.updateBossHp(bossHp);

    difficultyConfig = dc;

    mazeCameraManager = new CameraManager(dc);
    OrthographicCamera cam = mazeCameraManager.getCamera();

    float viewW = MAZE_VIEW_CELLS_WIDTH * GameConstants.CELL_SIZE;
    float viewH = MAZE_VIEW_CELLS_HEIGHT * GameConstants.CELL_SIZE;

    cam.viewportWidth = viewW;
    cam.viewportHeight = viewH;
    cam.zoom = 1f;
    cam.update();

    mazeCameraManager.centerOnPlayerImmediately(player);

    bossMazeCamera = new BossMazeCamera(cam, dc);
    mazeRenderer = new BossMazeRenderer(gameManager, dc);

    mazeViewport = new ExtendViewport(viewW, viewH, cam);
    mazeViewport.update(screenWidth, screenHeight, false);

    mazePaused = true;
    mazeStarted = true;
    mazePreloaded = true;

    aoeCycleTime = 0f;
    aoeTimers.clear();
    activeAOEs.clear();

    gameManager.setEnemyKillListener(e -> dealDamageToBoss(50f));
}
    private void applyPhase(BossMazeConfig.Phase phase) {

        phaseTime = 0f;
        hud.setBossPhase(phase.index);

        cachePlayersBeforeMazeReset();
        DifficultyConfig dc =
                BossDifficultyFactory.create(
                        currentBossConfig.base,
                        phase
                );

        BossPhasePreloadData preload = phaseCache.get(phase.index);

        if (preload != null) {
            gameManager.rebuildMazeForBossWithPrebuilt(dc, preload.maze);
        } else {
            // 兜底（极少发生）
            gameManager.rebuildMazeForBoss(dc);
        }

        difficultyConfig = dc;

        restorePlayersAfterMazeReset();
        rebuildMazeCameraAndViewport(dc);

        aoeCycleTime = 0f;
        aoeTimers.clear();
        activeAOEs.clear();

        gameManager.setEnemyKillListener(e -> dealDamageToBoss(50f));
    }

    private void rebuildMazeCameraAndViewport(DifficultyConfig dc) {

        mazeCameraManager = new CameraManager(dc);
        OrthographicCamera cam = mazeCameraManager.getCamera();

        float viewW = MAZE_VIEW_CELLS_WIDTH * GameConstants.CELL_SIZE;
        float viewH = MAZE_VIEW_CELLS_HEIGHT * GameConstants.CELL_SIZE;

        cam.viewportWidth = viewW;
        cam.viewportHeight = viewH;
        cam.zoom = 1f;
        cam.update();

        bossMazeCamera = new BossMazeCamera(cam, dc);
        bossMazeCamera.snapToPlayers(gameManager.getPlayers());
        mazeRenderer = new BossMazeRenderer(gameManager, dc);

        mazeViewport = new ExtendViewport(viewW, viewH, cam);
        mazeViewport.update(screenWidth, screenHeight, false);
    }


    /**
     * Handles viewport and camera updates when the window is resized.
     *
     * @param width  new screen width
     * @param height new screen height
     */
    @Override
    public void resize(int width, int height) {
        screenWidth = width;
        screenHeight = height;

        bossViewport.update(width, height, true);

        if (mazeViewport != null) {
            mazeViewport.update(width, height);
        }
        if (uiCamera != null) {
            uiCamera.setToOrtho(false, width, height);
            uiCamera.update();
        }
    }
    /**
     * Applies damage to the boss and updates rage and lock states.
     *
     * @param damage amount of damage dealt
     */
    public void dealDamageToBoss(float damage) {

        if (bossHp <= bossMaxHp * 0.05f && rageState != BossRageState.FINAL_LOCKED) {
            bossHp = bossMaxHp * 0.05f;
            rageState = BossRageState.FINAL_LOCKED;
            hud.setBossFinalLocked(true);
        }

        if (rageState == BossRageState.FINAL_LOCKED) {
            return;
        }
        bossHp -= damage;
        bossHp = Math.max(0f, bossHp);

        hud.updateBossHp(bossHp);

    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        if (gameManager != null) {
            gameManager.dispose();
        }
        if (currentDialogueSound != null) {
            currentDialogueSound.dispose();
        }

    }

    private static class PlayerSnapshot {
        Player.PlayerIndex index;
        int lives;
        float mana;
        AbilityManagerSnapshot abilitySnapshot;
    }


    private static class AbilitySnapshot {
        String abilityId;
        int level;
    }

    private static class AbilityManagerSnapshot {
        List<AbilitySnapshot> abilities = new ArrayList<>();
        int[] equippedSlots = new int[4];
        static AbilityManagerSnapshot from(AbilityManager am) {

            AbilityManagerSnapshot snap = new AbilityManagerSnapshot();

            if (am == null) return snap;

            Map<String, Ability> abilities = am.getAbilities();

            Map<Ability, Integer> abilityIndexMap = new HashMap<>();
            int index = 0;

            for (Map.Entry<String, Ability> entry : abilities.entrySet()) {
                Ability a = entry.getValue();

                AbilitySnapshot as = new AbilitySnapshot();
                as.abilityId = entry.getKey();
                as.level = a.getLevel();

                snap.abilities.add(as);
                abilityIndexMap.put(a, index++);
            }

            Ability[] slots = am.getAbilitySlots();
            for (int i = 0; i < snap.equippedSlots.length; i++) {
                Ability slotAbility = slots[i];
                if (slotAbility != null && abilityIndexMap.containsKey(slotAbility)) {
                    snap.equippedSlots[i] = abilityIndexMap.get(slotAbility);
                } else {
                    snap.equippedSlots[i] = -1;
                }
            }

            return snap;
        }
        void applyTo(AbilityManager am) {

            if (am == null) return;

            for (AbilitySnapshot as : abilities) {
                Ability a = am.getAbilities().get(as.abilityId);
                if (a != null) {
                    a.setLevel(as.level);
                }
            }

            Ability[] slots = am.getAbilitySlots();
            Arrays.fill(slots, null);

            for (int i = 0; i < equippedSlots.length; i++) {
                int idx = equippedSlots[i];
                if (idx >= 0 && idx < abilities.size()) {
                    AbilitySnapshot as = abilities.get(idx);
                    Ability a = am.getAbilities().get(as.abilityId);
                    slots[i] = a;
                }
            }
        }

    }

    private void triggerBossDeath() {
        bossDeathState = BossDeathState.TRIGGERED;
        bossDeathTimer = 0f;

        transitionState = PhaseTransitionState.NONE; // 防止 phase 切换
    }

    private float deathHoldTimer = 0f;

    private void updateBossDeath(float delta) {
        if (bossDeathState == BossDeathState.NONE) return;

        bossDeathTimer += delta;

        switch (bossDeathState) {
            case TRIGGERED -> {
                if (bossDeathTimer > 0.5f) {
                    bossDeathState = BossDeathState.MERGING_SCREEN;
                    bossDeathTimer = 0f;
                    mergeTimer = 0f;
                }
            }

            case MERGING_SCREEN -> {
                mergeTimer += delta;
                mergeProgress = Math.min(1f, mergeTimer / MERGE_TIME);

                if (mergeProgress >= 1f) {
                    bossDeathState = BossDeathState.PLAYING_DEATH;
                    deathHoldTimer = 0f;
                }
            }

            case PLAYING_DEATH -> {
                deathHoldTimer += delta;
                if (deathHoldTimer > 3.0f) {
                    bossDeathState = BossDeathState.FINISHED;
                }
            }

            case FINISHED -> {


            }
        }
    }

    private boolean isMazeFrozen() {
        return mazePaused
                || bossDeathState != BossDeathState.NONE
                || transitionState != PhaseTransitionState.NONE;
    }




    private void spawnTimelineAOE(Player player, float radius, int damage) {
        if (player == null) return;

        float px =
                player.getX() * GameConstants.CELL_SIZE
                        + GameConstants.CELL_SIZE / 2f;

        float py =
                player.getY() * GameConstants.CELL_SIZE
                        + GameConstants.CELL_SIZE / 2f;

        BossAOE aoe = new BossAOE();
        aoe.x = px;
        aoe.y = py;
        aoe.damage = damage;
        aoe.radius = radius;

        aoe.maxLife = 1.5f;
        aoe.life = aoe.maxLife;

        aoe.warningTime = 1.2f;
        aoe.active = false;
        aoe.damageDone = false;

        activeAOEs.add(aoe);
        AudioManager.getInstance().play(AudioType.BOSS_AOE_WARNING);
    }


    private boolean isPlayerInsideAOE(Player player, BossAOE aoe) {
        float px =
                player.getX() * GameConstants.CELL_SIZE
                        + GameConstants.CELL_SIZE / 2f;
        float py =
                player.getY() * GameConstants.CELL_SIZE
                        + GameConstants.CELL_SIZE / 2f;

        float dx = px - aoe.x;
        float dy = py - aoe.y;

        return dx * dx + dy * dy <= aoe.radius * aoe.radius;
    }


    /**
     * Plays a boss dialogue event triggered by the timeline.
     *
     * @param speaker   dialogue speaker identifier
     * @param text      dialogue text
     * @param voicePath optional voice audio path
     */
    public void playBossDialogue(String speaker, String text, String voicePath) {


        if (currentDialogueSound != null) {
            currentDialogueSound.stop();
            currentDialogueSound.dispose();
            currentDialogueSound = null;
        }

        if (voicePath != null && !voicePath.isEmpty()) {
            currentDialogueSound = Gdx.audio.newSound(Gdx.files.internal(voicePath));
            currentDialogueSound.play(1.0f);
        }
    }

    /**
     * Triggers a rage check event.
     * <p>
     * If conditions are met, the boss enters a rage punishment state.
     */
    public void enterRageCheck() {
        if (rageState != BossRageState.NORMAL) return;

        Gdx.app.log("RAGE", "Rage check triggered!");

        rageState = BossRageState.RAGE_PUNISH;
        rageAoeTimer = 0f;
        rageAoeTickTimer = 0f;
    }


    /**
     * Handles a boss HP threshold check triggered by the timeline.
     * <p>
     * If the boss HP is above the threshold, the player fails the fight.
     *
     * @param threshold HP percentage threshold
     * @param failEnding optional failure ending identifier
     */
    public void handleHpThreshold(float threshold, String failEnding) {
        if (bossHp > bossMaxHp * threshold) {
            game.setScreen(
                    new BossFailScreen(game, BossFailType.DAMAGE_NOT_ENOUGH)
            );
        } else {
            rageState = BossRageState.RAGE_PUNISH;
            rageAoeTimer = 0f;
            rageAoeTickTimer = 0f;
        }
    }
    /**
     * Starts a global AOE rage punishment.
     *
     * @param duration     duration of the AOE effect
     * @param tickInterval damage tick interval
     * @param damage       damage per tick
     */
    public void startGlobalAoe(float duration, float tickInterval, int damage) {
        rageState = BossRageState.RAGE_PUNISH;
        rageAoeTimer = 0f;
        rageAoeTickTimer = 0f;
    }
    /**
     * Locks the boss HP at a final threshold.
     *
     * @param threshold HP percentage to lock at
     */
    public void lockFinalHp(float threshold) {
        if (rageState != BossRageState.FINAL_LOCKED) {
            bossHp = bossMaxHp * threshold;
            rageState = BossRageState.FINAL_LOCKED;
            hud.updateBossHp(bossHp);
        }
    }

    private boolean victoryTriggered = false;

    /**
     * Marks the boss timeline as finished and triggers
     * the victory sequence.
     */
    public void markTimelineFinished() {
        if (victoryTriggered) return;
        victoryTriggered = true;


        enterVictoryMode();

        victoryEndTimer = 0f;
    }
    private float victoryEndTimer = 0f;
    private static final float VICTORY_PV_TIME = 12f;

    private boolean shouldRenderGameplay() {
        return mazeStarted
                && !mazePaused
                && !inVictoryHold;
    }
    /**
     * Starts a screen shake effect for the boss cup.
     *
     * @param duration shake duration
     * @param xAmp     horizontal amplitude
     * @param yAmp     vertical amplitude
     * @param xFreq    horizontal frequency
     * @param yFreq    vertical frequency
     */
    public void startCupShake(
            float duration,
            float xAmp,
            float yAmp,
            float xFreq,
            float yFreq
    ) {
        cupShakeActive = true;
        cupShakeTimer = 0f;
        cupShakeDuration = duration;

        cupShakeXAmp = xAmp;
        cupShakeYAmp = yAmp;
        cupShakeXFreq = xFreq;
        cupShakeYFreq = yFreq;
    }
    private boolean checkPlayersDeath() {
        for (Player p : gameManager.getPlayers()) {
            if (p != null && p.getLives() <= 0) {
                game.setScreen(
                        new BossFailScreen(game, BossFailType.PLAYER_DEAD)
                );
                return true;
            }
        }
        return false;
    }
    private final List<PlayerSnapshot> cachedPlayerSnapshots = new ArrayList<>();

    private void cachePlayersBeforeMazeReset() {
        cachedPlayerSnapshots.clear();

        for (Player p : gameManager.getPlayers()) {
            if (p == null) continue;

            PlayerSnapshot snap = new PlayerSnapshot();
            snap.index = p.getPlayerIndex();
            snap.lives = p.getLives();
            snap.mana  = p.getMana();

            snap.abilitySnapshot =
                    AbilityManagerSnapshot.from(p.getAbilityManager());

            cachedPlayerSnapshots.add(snap);
        }
    }
    private void restorePlayersAfterMazeReset() {
        if (cachedPlayerSnapshots.isEmpty()) return;

        for (PlayerSnapshot snap : cachedPlayerSnapshots) {
            Player p = gameManager.getPlayers().stream()
                    .filter(pp -> pp.getPlayerIndex() == snap.index)
                    .findFirst()
                    .orElse(null);

            if (p == null) continue;

            p.setLives(snap.lives);
            p.setMana(snap.mana);

            if (snap.abilitySnapshot != null) {
                snap.abilitySnapshot.applyTo(p.getAbilityManager());
            }

            p.setMovingAnim(false);
        }

        cachedPlayerSnapshots.clear();
    }



}