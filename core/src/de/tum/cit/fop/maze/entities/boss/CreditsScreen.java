package de.tum.cit.fop.maze.entities.boss;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import de.tum.cit.fop.maze.MazeRunnerGame;
import de.tum.cit.fop.maze.screen.MenuScreen;
/**
 * End credits screen displayed after the boss story concludes.
 * <p>
 * Scrolls a list of credits vertically and automatically
 * returns to the main menu when finished.
 */
public class CreditsScreen implements Screen {

    private final MazeRunnerGame game;
    private SpriteBatch batch;
    private BitmapFont font;
    private OrthographicCamera camera;

    private float y;
    private static final float SPEED = 50f;

    private final String[] credits = {
            "THE END",
            "",
            "QQCHA",
            "",
            "CuI Jianxi",
            "Producer",
            "",
            "CuI Jianxi",
            "PV Director",
            "",
            "CuI Jianxi",
            "Executive Producer",
            "",
            "CuI Jianxi",
            "Voice Production",
            "",
            "CuI Jianxi / Jiang Zejia / Li Linzhi / Peng Yida",
            "Programming",
            "",
            "Jiang Zejia",
            "Endless Mode Programming",
            "",
            "Li Linzhi",
            "Boss Development",
            "",
            "Jiang Zejia",
            "Art & Animation",
            "",
            "Li Linzhi",
            "Texture Art & Direction",
            "",
            "Peng Yida",
            "Sound Effects & Visual Effects",
            "",
            "Peng Yida",
            "Leaderboard, Score & Save Systems",
            "",
            "You",
            "QQ CHA",
            "",
            "Thanks for playing"
    };

    /**
     * Creates the credits screen.
     *
     * @param game main game instance
     */
    public CreditsScreen(MazeRunnerGame game) {
        this.game = game;
    }
    /**
     * Initializes rendering resources and positions
     * the credits off-screen for scrolling.
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

        y = -credits.length * 40f;
    }
    /**
     * Renders and scrolls the credits text.
     * <p>
     * Once the credits have fully scrolled past the screen,
     * the game transitions back to the main menu.
     *
     * @param delta time elapsed since the last frame
     */
    @Override
    public void render(float delta) {
        y += delta * SPEED;

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        font.getData().setScale(0.5f);

        float drawY = y;
        for (String line : credits) {
            font.draw(batch, line, 200, drawY);
            drawY += 40;
        }

        batch.end();

        if (y > Gdx.graphics.getHeight() + 200) {
            game.setScreen(new MenuScreen(game));
        }
    }

    @Override public void resize(int w, int h) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    /**
     * Disposes rendering resources used by the credits screen.
     */
    @Override public void dispose() {
        batch.dispose();
    }
}

