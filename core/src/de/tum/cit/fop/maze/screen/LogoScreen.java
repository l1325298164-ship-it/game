package de.tum.cit.fop.maze.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import de.tum.cit.fop.maze.MazeRunnerGame;
/**
 * Splash screen displaying the game logo animation.
 *
 * <p>The logo animation plays once on startup and transitions
 * to the main menu when finished or when the user skips it.
 */
public class LogoScreen implements Screen {

    private final MazeRunnerGame game;
    private SpriteBatch batch;

    private TextureAtlas logoAtlas;
    private Animation<TextureRegion> logoAnim;

    private float stateTime = 0f;
    private static final float TOTAL_DURATION = 6f;
    /**
     * Creates the logo splash screen.
     *
     * @param game the main game instance
     */
    public LogoScreen(MazeRunnerGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        batch = game.getSpriteBatch();

        logoAtlas = new TextureAtlas(
                Gdx.files.internal("logo/logo.atlas")
        );

        Array<TextureAtlas.AtlasRegion> atlasRegions =
                logoAtlas.findRegions("logo");

        Array<TextureRegion> frames = new Array<>();
        for (TextureAtlas.AtlasRegion r : atlasRegions) {
            frames.add(r);
        }

        float frameDuration = TOTAL_DURATION / frames.size;

        logoAnim = new Animation<>(
                frameDuration,
                frames,
                Animation.PlayMode.NORMAL
        );
    }
    /**
     * Updates and renders the logo animation.
     *
     * @param delta time elapsed since last frame (in seconds)
     */
    @Override
    public void render(float delta) {
        stateTime += delta;

        if (Gdx.input.justTouched() ||
                Gdx.input.isKeyJustPressed(Input.Keys.ANY_KEY)) {
            finish();
            return;
        }

        TextureRegion frame = logoAnim.getKeyFrame(stateTime);

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(com.badlogic.gdx.graphics.GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        batch.draw(
                frame,
                0,
                0,
                Gdx.graphics.getWidth(),
                Gdx.graphics.getHeight()
        );
        batch.end();

        if (logoAnim.isAnimationFinished(stateTime)) {
            finish();
        }
    }

    private void finish() {
        dispose();
        game.setScreen(new MenuScreen(game));
    }

    @Override public void resize(int w, int h) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        if (logoAtlas != null) {
            logoAtlas.dispose();
        }
    }
}
