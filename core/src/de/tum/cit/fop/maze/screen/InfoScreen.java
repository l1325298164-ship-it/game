package de.tum.cit.fop.maze.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion; // 导入防止报错
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import de.tum.cit.fop.maze.MazeRunnerGame;
import de.tum.cit.fop.maze.tools.ButtonFactory;
import de.tum.cit.fop.maze.utils.Logger;

/**
 * Screen displaying game-related information and navigation options.
 *
 * <p>This screen provides access to achievements, leaderboard,
 * and allows returning to the previous screen.
 */
public class InfoScreen implements Screen {

    private final MazeRunnerGame game;
    private final Screen previousScreen;
    private Stage stage;
    private Texture backgroundTexture;
    /**
     * Creates the information screen.
     *
     * @param game the main game instance
     * @param previousScreen the screen to return to when exiting
     */
    public InfoScreen(MazeRunnerGame game, Screen previousScreen) {
        this.game = game;
        this.previousScreen = previousScreen;

        try {
            if (Gdx.files.internal("imgs/menu_bg/bg_front.png").exists()) {
                backgroundTexture = new Texture(Gdx.files.internal("imgs/menu_bg/bg_front.png"));
            }
        } catch (Exception e) {
            Logger.error("Failed to load background: " + e.getMessage());
        }

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        setupUI();
    }

    private void setupUI() {
        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        Table contentTable = new Table();

        Label title = new Label("GAME INFO", game.getSkin(), "title");
        title.setColor(Color.CYAN);
        title.setFontScale(1.3f);
        contentTable.add(title).padBottom(80).row();

        ButtonFactory bf = new ButtonFactory(game.getSkin());

        float btnWidth = 450f;
        float btnHeight = 80f;
        float spacing = 35f;

        contentTable.add(bf.create("ACHIEVEMENTS", () ->
                game.setScreen(new AchievementScreen(game, this))
        )).width(btnWidth).height(btnHeight).padBottom(spacing).row();

        contentTable.add(bf.create("LEADERBOARD", () ->
                game.setScreen(new LeaderboardScreen(game, this))
        )).width(btnWidth).height(btnHeight).padBottom(spacing).row();

        contentTable.add(bf.create("BACK", () -> game.setScreen(previousScreen)))
                .width(btnWidth).height(btnHeight).row();

        root.add(contentTable);
    }
    /**
     * Renders the info screen UI.
     *
     * @param delta time elapsed since last frame (in seconds)
     */
    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.05f, 0.05f, 0.08f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.getBatch().begin();
        if (backgroundTexture != null) {
            stage.getBatch().setColor(0.5f, 0.5f, 0.5f, 1f); // 0.5 的亮度
            stage.getBatch().draw(backgroundTexture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            stage.getBatch().setColor(Color.WHITE);
        }
        stage.getBatch().end();

        stage.act(delta);
        stage.draw();
    }

    @Override public void resize(int w, int h) { stage.getViewport().update(w, h, true); }
    @Override public void show() { Gdx.input.setInputProcessor(stage); }
    @Override public void hide() {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void dispose() {
        stage.dispose();
        if (backgroundTexture != null) backgroundTexture.dispose();
    }
}