package de.tum.cit.fop.maze.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import de.tum.cit.fop.maze.MazeRunnerGame;
import de.tum.cit.fop.maze.audio.AudioManager;
import de.tum.cit.fop.maze.audio.AudioType;
import de.tum.cit.fop.maze.game.story.StoryProgress;
import de.tum.cit.fop.maze.tools.ButtonFactory;
/**
 * Screen responsible for playing story preview videos (PV).
 *
 * <p>This screen handles animation playback, background music,
 * skip logic, and exit behavior after the PV finishes.
 * Different exit behaviors are controlled via {@link PVExit}.
 */
public class IntroScreen implements Screen {

    private final MazeRunnerGame game;
    private final Animation<TextureRegion> pvAnim;
    private final PVExit exitType;
    private final AudioType musicType;
    private final PVFinishedListener finishedListener;

    private float stateTime = 0f;
    private boolean exited = false;
    private boolean animationFinished = false;

    private final SpriteBatch batch;
    private final Viewport viewport;

    private Stage stage;
    private ButtonFactory buttonFactory;
    private boolean showPV4Buttons = false;

    private static final float WORLD_WIDTH = 2784f;
    private static final float WORLD_HEIGHT = 1536f;



    private float skipTimer = 0f;
    private static final float SKIP_THRESHOLD = 2.0f;
    private boolean isSkipping = false;
    private TextButton escButton;
    private static final float PROGRESS_BAR_WIDTH = 230f;
    private static final float PROGRESS_BAR_HEIGHT = 15f;
    /**
     * Defines how the game exits after the PV finishes.
     */
    public enum PVExit {
        NEXT_STAGE,
        TO_MENU,
        PV4_CHOICE
    }
    /**
     * Callback invoked when a PV animation finishes.
     */
    public interface PVFinishedListener {
        void onPVFinished();
    }
    /**
     * Creates an intro screen that plays a story animation (PV).
     *
     * @param game the main game instance
     * @param animation the animation to be played
     * @param exit defines what happens after the PV finishes
     * @param audio background music played during the PV (may be null)
     * @param listener optional callback invoked when the PV finishes
     */
    public IntroScreen(
            MazeRunnerGame game,
            Animation<TextureRegion> animation,
            PVExit exit,
            AudioType audio,
            PVFinishedListener listener
    ) {
        this.game = game;
        this.pvAnim = animation;
        this.exitType = exit;
        this.musicType = audio;
        this.finishedListener = listener;

        this.batch = game.getSpriteBatch();
        this.viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT);
    }

    @Override
    public void show() {
        Gdx.app.debug("IntroScreen", "PV started");

        stateTime = 0f;
        exited = false;
        animationFinished = false;
        showPV4Buttons = false;

        if (musicType != null) {
            game.getSoundManager().playMusic(musicType);
        }

        if (exitType == PVExit.PV4_CHOICE) {
            stage = new Stage(viewport, batch);
            buttonFactory = new ButtonFactory(game.getSkin());
            createPV4Buttons();
            stage.getActors().forEach(actor -> actor.setVisible(false));
        }

        if (stage == null) {
            stage = new Stage(viewport, batch);
            buttonFactory = new ButtonFactory(game.getSkin());
        }

        createSkipUI();
        Gdx.input.setInputProcessor(stage);
    }

    private void createSkipUI() {
        buttonFactory.setAnimationParams(0.11f, 0.10f, 0.10f, 1.0f, 1.0f, 1.0f);
        escButton = buttonFactory.create("ESC SKIP", this::skipAnimation);
        buttonFactory.setAnimationParams(0.12f, 0.08f, 0.10f, 1.05f, 0.95f, 1.08f);

        escButton.setSize(240, 60);
        escButton.setOrigin(Align.center);
        escButton.setPosition(40, WORLD_HEIGHT - 120);

        stage.addActor(escButton);
    }
    private void createPV4Buttons() {
        if (stage == null) return;

        TextButton startButton = buttonFactory.createNavigationButton(
                "Start Chapter",
                () -> {
                        StoryProgress progress = StoryProgress.load();
        progress.markPvWatched(1);
        progress.save();

        game.onPV4Choice(MazeRunnerGame.PV4Result.START);
    }
        );

        float buttonWidth = 600f;
        float buttonHeight = 110f;

        startButton.setSize(buttonWidth, buttonHeight);
        startButton.setPosition(
                (WORLD_WIDTH - buttonWidth) / 2f,
                WORLD_HEIGHT * 0.28f
        );

        stage.addActor(startButton);
    }
    /**
     * Updates and renders the PV animation and handles skip
     * and exit logic each frame.
     *
     * @param delta time elapsed since last frame (in seconds)
     */
    @Override
    public void render(float delta) {
        stateTime += Math.min(delta, 1f / 24f);
        checkSkipInput(delta);

        ScreenUtils.clear(0, 0, 0, 1);

        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);

        batch.begin();
        if (!pvAnim.isAnimationFinished(stateTime)) {
            TextureRegion frame = pvAnim.getKeyFrame(stateTime, false);
            batch.draw(frame, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);

            renderProgressBar();
        } else {
            animationFinished = true;
            TextureRegion[] frames = pvAnim.getKeyFrames();
            TextureRegion lastFrame = frames[frames.length - 1];
            batch.draw(lastFrame, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);

            // PV4 逻辑
            if (exitType == PVExit.PV4_CHOICE && !showPV4Buttons) {
                showPV4Buttons = true;
                Gdx.input.setInputProcessor(stage);
                AudioManager.getInstance().stopMusic();
                if (escButton != null) {
                    escButton.setVisible(false);
                }
                stage.getActors().forEach(actor -> {
                    if (actor != escButton) actor.setVisible(true);
                });
            }
        }
        batch.end();

        if (stage != null) {
            stage.getViewport().apply();
            stage.act(delta);
            stage.draw();
        }

        if (exitType != PVExit.PV4_CHOICE
                && pvAnim.isAnimationFinished(stateTime)
                && stateTime > pvAnim.getAnimationDuration() + 2f) {
            handleExit();
        }
    }

    private void renderProgressBar() {
        if (skipTimer <= 0) return;

        TextureRegion white = game.getSkin().getRegion("white");
        if (white == null) return;

        float x = 40;
        float y = WORLD_HEIGHT - 50;
        float progress = Math.min(skipTimer / SKIP_THRESHOLD, 1.0f);
        float currentWidth = PROGRESS_BAR_WIDTH * progress;

        batch.setColor(0.1f, 0.1f, 0.1f, 0.6f);
        batch.draw(white, x, y, PROGRESS_BAR_WIDTH, PROGRESS_BAR_HEIGHT);

        com.badlogic.gdx.graphics.g2d.Sprite gradientBar = new com.badlogic.gdx.graphics.g2d.Sprite(white);
        gradientBar.setPosition(x, y);
        gradientBar.setSize(currentWidth, PROGRESS_BAR_HEIGHT);

        Color colorPink = new Color(1f, 0.4f, 0.7f, 1f);   // 左侧粉色
        Color colorYellow = new Color(1f, 1f, 0.2f, 1f); // 右侧黄色

        gradientBar.getVertices()[com.badlogic.gdx.graphics.g2d.SpriteBatch.C1] = colorPink.toFloatBits();
        gradientBar.getVertices()[com.badlogic.gdx.graphics.g2d.SpriteBatch.C2] = colorPink.toFloatBits();
        gradientBar.getVertices()[com.badlogic.gdx.graphics.g2d.SpriteBatch.C3] = colorYellow.toFloatBits();
        gradientBar.getVertices()[com.badlogic.gdx.graphics.g2d.SpriteBatch.C4] = colorYellow.toFloatBits();

        batch.setColor(Color.WHITE);
        gradientBar.draw(batch);
    }
    private void checkSkipInput(float delta) {
        if (Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.ESCAPE)) {
            skipTimer += delta;

            if (escButton != null) {
                escButton.setOrigin(Align.center);
                escButton.setScale(0.95f);
                escButton.setColor(Color.LIGHT_GRAY);
            }

            if (skipTimer >= SKIP_THRESHOLD && !isSkipping) {
                isSkipping = true;
                skipAnimation();
            }
        } else {
            skipTimer = 0f;
            if (escButton != null && !escButton.isPressed()) {
                escButton.setScale(1.0f);
                escButton.setColor(Color.WHITE);
            }
        }
    }


    private void skipAnimation() {
        // 将 stateTime 设置为动画长度，使其进入“播放完成”状态
        stateTime = pvAnim.getAnimationDuration();

        // 如果是普通的 NEXT_STAGE，直接 handleExit
        if (exitType != PVExit.PV4_CHOICE) {
            handleExit();
        }
        // 如果是 PV4_CHOICE，render 里的逻辑会自动显示按钮
    }


    private void handleExit() {
        if (exited) return;
        exited = true;

        switch (exitType) {
            case NEXT_STAGE -> {
                if (finishedListener != null) {
                    finishedListener.onPVFinished(); // ⭐ Pipeline 接管
                }
            }
            case TO_MENU -> game.goToMenu();
            case PV4_CHOICE -> {
            }
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void hide() {
        if (stage != null) {
            Gdx.input.setInputProcessor(null);
        }
    }

    @Override
    public void dispose() {
        if (stage != null) {
            stage.dispose();
            stage = null;
        }
    }

    @Override public void pause() {}
    @Override public void resume() {}
}
