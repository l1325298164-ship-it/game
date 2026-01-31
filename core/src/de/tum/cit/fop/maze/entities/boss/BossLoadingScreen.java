package de.tum.cit.fop.maze.entities.boss;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.TimeUtils;
import de.tum.cit.fop.maze.MazeRunnerGame;
import de.tum.cit.fop.maze.audio.AudioManager;
import de.tum.cit.fop.maze.audio.AudioType;
import de.tum.cit.fop.maze.entities.boss.config.BossDifficultyFactory;
import de.tum.cit.fop.maze.entities.boss.config.BossMazeConfig;
import de.tum.cit.fop.maze.entities.boss.config.BossMazeConfigLoader;
import de.tum.cit.fop.maze.game.DifficultyConfig;
import de.tum.cit.fop.maze.maze.MazeGenerator;
import de.tum.cit.fop.maze.screen.MenuScreen;
import de.tum.cit.fop.maze.utils.BlockingInputProcessor;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
/**
 * Loading screen displayed before the boss fight starts.
 * <p>
 * This screen is responsible for asynchronously loading
 * boss-related assets and pre-generating maze data for
 * all boss phases before entering the fight.
 */
public class BossLoadingScreen implements Screen {

    private final MazeRunnerGame game;
    private final AssetManager assets;
    private final SpriteBatch batch;

    private BitmapFont font;

    private long showTime;
    private boolean finished = false;
    private float blinkTime = 0f;

    private static final long MIN_SHOW_TIME_MS = 1200;
    /**
     * Creates a loading screen for the boss fight.
     *
     * @param game main game instance
     */
    public BossLoadingScreen(MazeRunnerGame game) {
        this.game = game;
        this.assets = game.getAssets();
        this.batch = game.getSpriteBatch();
    }
    /**
     * Initializes loading state, queues boss assets,
     * and starts asynchronous phase preloading.
     */
    @Override
    public void show() {
        showTime = TimeUtils.millis();
        font = game.getSkin().getFont("default-font");
        queueBossAssets();
        preloadAllPhasesAsync();
        Gdx.input.setInputProcessor(new BlockingInputProcessor());
    }

    private void queueBossAssets() {

        assets.load("story_file/boss/bossFight/BOSS_PV.atlas", TextureAtlas.class);

        assets.load("story_file/boss/teacup_top.png", Texture.class);

        assets.load("effects/aoe_fill.png", Texture.class);
        assets.load("effects/aoe_ring.png", Texture.class);

        assets.load("sounds_file/BGM/boss_bgm.mp3", Music.class);

        assets.load("story_file/boss/voice/boss_1.mp3", Sound.class);
        assets.load("story_file/boss/voice/boss_2.mp3", Sound.class);
        assets.load("story_file/boss/voice/boss_3.mp3", Sound.class);
    }
    /**
     * Renders the loading progress and transitions
     * to the boss fight screen once loading is complete.
     *
     * @param delta time elapsed since the last frame
     */
    @Override
    public void render(float delta) {
        AudioManager.getInstance().playMusic(AudioType.BOSS_LOADING);
        batch.setProjectionMatrix(
                new Matrix4().setToOrtho2D(
                        0, 0,
                        Gdx.graphics.getWidth(),
                        Gdx.graphics.getHeight()
                )
        );

        font.getData().setScale(1f);
        blinkTime += delta;
        assets.update();

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(new MenuScreen(game));
            return;
        }

        boolean ready =
                assets.isFinished()
                        && TimeUtils.timeSinceMillis(showTime) > MIN_SHOW_TIME_MS;

        if (ready && !finished) {
            finished = true;
            AudioManager.getInstance().stopMusic();
            game.setScreen(new BossFightScreen(game));
            return;
        }

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();

        font.setColor(1f, 1f, 1f, 0.65f);
        font.getData().setScale(0.5f);
        font.draw(
                batch,
                "ESC  —  Return to Menu",
                18f,
                Gdx.graphics.getHeight() - 18f
        );

        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();

        int percent = MathUtils.floor(assets.getProgress() * 100f);
        font.setColor(Color.WHITE);
        font.getData().setScale(0.9f);
        font.draw(
                batch,
                "LOADING " + percent + "%",
                0,
                h * 0.55f,
                w,
                Align.center,
                false
        );

        float alpha = 0.4f + 0.6f * MathUtils.sin(blinkTime * 1.2f);
        font.setColor(1f, 1f, 1f, alpha);
        font.getData().setScale(0.6f);
        font.draw(
                batch,
                "Next: 2 minutes of high-intensity boss combat.\nPrepare yourself.",
                0,
                h * 0.40f,
                w,
                Align.center,
                true
        );

        batch.end();
        font.getData().setScale(1f);
    }

    private final Map<Integer, BossPhasePreloadData> phaseCache =
            Collections.synchronizedMap(new HashMap<>());


    private void preloadAllPhasesAsync() {

        new Thread(() -> {

            BossMazeConfig config =
                    BossMazeConfigLoader.loadOne("story_file/boss/boss_phases.json");

            MazeGenerator generator = new MazeGenerator();

            for (int i = 0; i < 3; i++) {

                BossMazeConfig.Phase phase = config.phases.get(i);

                DifficultyConfig dc =
                        BossDifficultyFactory.create(config.base, phase);

                int[][] maze = generator.generateMaze(dc);

                BossPhasePreloadData data = new BossPhasePreloadData();
                data.maze = maze;
                data.phase = phase;

                phaseCache.put(i, data);

                Gdx.app.log(
                        "BOSS_PRELOAD",
                        "Phase " + i + " maze ready (" +
                                dc.mazeWidth + "x" + dc.mazeHeight + ")"
                );
            }

        }, "BossPhasePreloader").start();

    }

    @Override public void resize(int w, int h) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    /**
     * Disposes resources owned by this screen.
     */
    @Override public void dispose() {}
}
