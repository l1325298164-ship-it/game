package de.tum.cit.fop.maze.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import de.tum.cit.fop.maze.MazeRunnerGame;
import de.tum.cit.fop.maze.input.KeyBindingManager;
import de.tum.cit.fop.maze.input.KeyBindingManager.GameAction;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
/**
 * Screen allowing the player to view and rebind keyboard controls.
 *
 * <p>This screen lists all available {@link GameAction}s and enables
 * interactive rebinding of keys via user input.
 */
public class KeyMappingScreen implements Screen {

    private final MazeRunnerGame game;
    private final Screen previousScreen;
    private Stage stage;
    private Skin skin;

    private boolean isWaitingForKey = false;
    private GameAction actionRebinding = null;
    private TextButton buttonRebinding = null;
    /**
     * Creates the key mapping configuration screen.
     *
     * @param game the main game instance
     * @param previousScreen the screen to return to when exiting
     */
    public KeyMappingScreen(MazeRunnerGame game, Screen previousScreen) {
        this.game = game;
        this.previousScreen = previousScreen;
    }

    @Override
    public void show() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        skin = game.getSkin();

        Table rootTable = new Table();
        rootTable.setFillParent(true);
        stage.addActor(rootTable);

        Label titleLabel = new Label("CONTROLS SETTINGS", skin);
        titleLabel.setFontScale(1.5f);
        rootTable.add(titleLabel).padBottom(50).row();

        Table contentTable = new Table();

        for (GameAction action : GameAction.values()) {
            String actionName = action.name().replace("_", " ");
            Label nameLabel = new Label(actionName, skin);

            String keyName = KeyBindingManager.getInstance().getKeyName(action);
            TextButton keyButton = new TextButton(keyName, skin);

            keyButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (!isWaitingForKey) {
                        startRebinding(action, keyButton);
                    }
                }
            });

            contentTable.add(nameLabel).left().padRight(500);

            contentTable.add(keyButton).width(150).height(40).padBottom(10).row();
        }

        com.badlogic.gdx.scenes.scene2d.ui.ScrollPane.ScrollPaneStyle scrollStyle = new com.badlogic.gdx.scenes.scene2d.ui.ScrollPane.ScrollPaneStyle();
        if (skin.has("white", com.badlogic.gdx.graphics.g2d.TextureRegion.class)) {
            com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable knob = new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(skin.getRegion("white"));
            knob.setMinWidth(10);
            scrollStyle.vScrollKnob = knob;
        }

        com.badlogic.gdx.scenes.scene2d.ui.ScrollPane scrollPane = new com.badlogic.gdx.scenes.scene2d.ui.ScrollPane(contentTable, scrollStyle);
        scrollPane.setScrollingDisabled(true, false);
        scrollPane.setFadeScrollBars(false);
        stage.setScrollFocus(scrollPane);
        rootTable.add(scrollPane).expand().fill().row();

        Table bottomTable = new Table();

        TextButton resetButton = new TextButton("Default", skin);
        resetButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                KeyBindingManager.getInstance().resetToDefaults();
                game.setScreen(new KeyMappingScreen(game, previousScreen));
            }
        });

        TextButton backButton = new TextButton("Back", skin);
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(previousScreen);
                dispose();
            }
        });

        bottomTable.add(resetButton).width(150).height(50).padRight(300);
        bottomTable.add(backButton).width(150).height(50);

        rootTable.add(bottomTable).padTop(40);
    }

    private void startRebinding(GameAction action, TextButton button) {
        isWaitingForKey = true;
        actionRebinding = action;
        buttonRebinding = button;

        button.setText("Press any key...");

        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.ESCAPE) {
                    finishRebinding(KeyBindingManager.getInstance().getKey(action)); // 恢复原状
                    return true;
                }

                finishRebinding(keycode);
                return true;
            }
        });
    }


    private void finishRebinding(int keycode) {
        KeyBindingManager.getInstance().setBinding(actionRebinding, keycode);

        String newKeyName = Input.Keys.toString(keycode);
        buttonRebinding.setText(newKeyName);

        isWaitingForKey = false;
        actionRebinding = null;
        buttonRebinding = null;

        Gdx.input.setInputProcessor(stage);
    }
    /**
     * Renders the key mapping UI and processes input rebinding.
     *
     * @param delta time elapsed since last frame (in seconds)
     */
    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        if (stage != null) stage.dispose();
    }
}
