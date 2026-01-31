package de.tum.cit.fop.maze.entities.boss;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import de.tum.cit.fop.maze.MazeRunnerGame;
import de.tum.cit.fop.maze.audio.AudioManager;
import de.tum.cit.fop.maze.audio.AudioType;
import de.tum.cit.fop.maze.game.story.StoryProgress;
/**
 * Story screen shown after the boss fight is completed.
 * <p>
 * Displays a short narrative sequence and advances the
 * story progress before transitioning to the credits.
 */
public class BossStoryScreen implements Screen {

    private final MazeRunnerGame game;
    private SpriteBatch batch;
    private BitmapFont font;
    private OrthographicCamera camera;

    private final String[] storyLines = {
            "You have escaped this cage.",
            "Yet a greater cage may still lie ahead."
    };

    /**
     * Creates the post-boss story screen.
     *
     * @param game main game instance
     */
    public BossStoryScreen(MazeRunnerGame game) {
        this.game = game;
    }
    /**
     * Initializes rendering resources for the story screen.
     */
    @Override
    public void show() {
        batch = new SpriteBatch();
        font = game.getSkin().get("default-font", BitmapFont.class);

        camera = new OrthographicCamera();
        camera.setToOrtho(false,
                Gdx.graphics.getWidth(),
                Gdx.graphics.getHeight());
        camera.update();
    }

    /**
     * Renders the story text and waits for player input
     * to continue to the credits screen.
     *
     * @param delta time elapsed since the last frame
     */
    @Override
    public void render(float delta) {
        if (Gdx.input.justTouched() || Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            StoryProgress sp = StoryProgress.load();
            sp.markBossDefeated(1);
            sp.save();
            AudioManager.getInstance().playMusic(AudioType.BOSS_BGM);
            game.setScreen(new CreditsScreen(game));
            return;
        }

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        font.getData().setScale(0.45f);
        float y = Gdx.graphics.getHeight() * 0.65f;

        for (String line : storyLines) {
            font.draw(batch, line, 120, y);
            y -= 40;
        }

        font.getData().setScale(0.3f);
        font.draw(batch,
                "[Click / ENTER to continue]",
                120,
                120);

        batch.end();
    }

    @Override public void resize(int w, int h) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    /**
     * Disposes rendering resources used by this screen.
     */
    @Override public void dispose() {
        batch.dispose();
    }
}

