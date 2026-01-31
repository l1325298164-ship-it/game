package de.tum.cit.fop.maze.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import de.tum.cit.fop.maze.MazeRunnerGame;
import de.tum.cit.fop.maze.game.save.GameSaveData;
import de.tum.cit.fop.maze.game.save.StorageManager;
import de.tum.cit.fop.maze.tools.ButtonFactory;
import de.tum.cit.fop.maze.utils.Logger;
/**
 * Screen for selecting and managing saved game records.
 *
 * <p>This screen displays available save slots, allows loading
 * or deleting saves, and provides navigation back to the previous screen.
 */

public class SaveSelectScreen implements Screen {

    private final MazeRunnerGame game;
    private final Screen previousScreen;
    private final StorageManager storage;
    private Stage stage;
    private Texture backgroundTexture;

    private NinePatchDrawable cardBackground;
    /**
     * Creates the save selection screen.
     *
     * @param game the main game instance
     * @param previousScreen the screen to return to when exiting
     */
    public SaveSelectScreen(MazeRunnerGame game, Screen previousScreen) {
        this.game = game;
        this.previousScreen = previousScreen;
        this.storage = StorageManager.getInstance();
        this.stage = new Stage(new ScreenViewport());

        try {
            if (Gdx.files.internal("imgs/menu_bg/bg_front.png").exists()) {
                backgroundTexture = new Texture(Gdx.files.internal("imgs/menu_bg/bg_front.png"));
            }
        } catch (Exception e) {
            Logger.warning("Background not found: " + e.getMessage());
        }

        this.cardBackground = createBorderedBackground(
                new Color(0.05f, 0.05f, 0.1f, 0.4f), // 背景：深蓝黑，透明度 40%
                new Color(1f, 1f, 1f, 0.25f)         // 边框：灰白，透明度 25%
        );

        setupUI();
    }

    private void setupUI() {
        stage.clear();
        Gdx.input.setInputProcessor(stage);

        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        Table headerTable = new Table();
        Label title = new Label("SELECT RECORD", game.getSkin(), "title");
        title.setColor(Color.GOLD);
        title.setFontScale(1.3f); // 标题加大
        headerTable.add(title).padBottom(15).row();

        Label hint = new Label("Choose a record to resume your journey", game.getSkin());
        hint.setColor(Color.LIGHT_GRAY);
        headerTable.add(hint).row();

        root.add(headerTable).padTop(60).padBottom(30).row();

        Table listContent = new Table();
        listContent.top().pad(20);

        float cardWidth = Gdx.graphics.getWidth() * 0.8f;

        boolean hasRecords = false;

        for (int i = 1; i <= StorageManager.MAX_SAVE_SLOTS; i++) {
            GameSaveData data = storage.loadGameFromSlot(i);
            if (data != null) {
                Table card = createSaveCard(i, data);
                listContent.add(card).width(cardWidth).padBottom(25).row(); // 间距 25
                hasRecords = true;
            }
        }

        if (!hasRecords) {
            Label empty = new Label("No records found.", game.getSkin());
            empty.setColor(Color.GRAY);
            empty.setFontScale(1.2f);
            listContent.add(empty).padTop(100);
        }

        ScrollPane scrollPane = new ScrollPane(listContent, createInvisibleScrollPaneStyle());
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false);
        stage.setScrollFocus(scrollPane);
        root.add(scrollPane).expand().fill().padBottom(20).row();

        Table footer = new Table();
        ButtonFactory bf = new ButtonFactory(game.getSkin());
        footer.add(bf.create("BACK", () -> game.setScreen(previousScreen)))
                .width(300).height(70);

        root.add(footer).padBottom(50);
    }


    private Table createSaveCard(int slotId, GameSaveData data) {
        Table card = new Table();
        card.setBackground(cardBackground);

        card.pad(30);

        Table infoTable = new Table();

        Label nameLabel = new Label("SLOT " + slotId, game.getSkin());
        nameLabel.setColor(Color.GOLD);
        nameLabel.setFontScale(1.4f); // 再次加大
        infoTable.add(nameLabel).left().padBottom(15).row();

        String mode = data.twoPlayerMode ? "2-Player" : "Solo";
        String infoText = String.format("Level %d   •   %s   •   %s",
                data.currentLevel, data.difficulty, mode);

        Label detailLabel = new Label(infoText, game.getSkin());
        detailLabel.setColor(Color.WHITE);
        detailLabel.setFontScale(1.1f);
        infoTable.add(detailLabel).left().padBottom(10).row();

        String timeStr = storage.getSlotLastModifiedTime(slotId);
        Label timeLabel = new Label("Saved: " + timeStr, game.getSkin());
        timeLabel.setColor(new Color(0.7f, 0.7f, 0.7f, 1f));
        timeLabel.setFontScale(0.9f);
        infoTable.add(timeLabel).left();

        card.add(infoTable).expandX().left().padLeft(10);

        Table btnTable = new Table();
        ButtonFactory bf = new ButtonFactory(game.getSkin());

        TextButton loadBtn = bf.create("LOAD", () -> {
            game.getGameManager().setCurrentSaveTarget(StorageManager.SaveTarget.fromSlot(slotId));
            game.loadGameFromSlot(slotId);
        });
        btnTable.add(loadBtn).width(180).height(60).padBottom(15).row();

        TextButton delBtn = bf.create("DEL", () -> showDeleteConfirm(slotId));
        delBtn.setColor(new Color(0.8f, 0.3f, 0.3f, 1f));
        btnTable.add(delBtn).width(180).height(60);

        card.add(btnTable).right().padRight(10);

        return card;
    }


    private NinePatchDrawable createBorderedBackground(Color fillColor, Color borderColor) {
        int size = 9;
        Pixmap p = new Pixmap(size, size, Pixmap.Format.RGBA8888);

        p.setColor(fillColor);
        p.fill();

        p.setColor(borderColor);
        p.drawRectangle(0, 0, size, size);

        Texture t = new Texture(p);
        p.dispose();

        return new NinePatchDrawable(new NinePatch(t, 1, 1, 1, 1));
    }

    private void showDeleteConfirm(int slotId) {
        Dialog d = new Dialog("", game.getSkin()) {
            @Override protected void result(Object object) {
                if (Boolean.TRUE.equals(object)) {
                    storage.deleteSaveSlot(slotId);
                    setupUI();
                }
            }
        };

        d.setBackground(createBorderedBackground(
                new Color(0.1f, 0.1f, 0.15f, 0.9f),
                Color.GRAY
        ));

        Label l = new Label("Delete this record?", game.getSkin());
        l.setAlignment(Align.center);
        l.setFontScale(1.2f);
        d.getContentTable().add(l).pad(40).row();

        d.button("DELETE", true);
        d.button("CANCEL", false);

        Table bt = d.getButtonTable();

        bt.defaults().pad(15).width(180).height(65);

        bt.center();

        for (com.badlogic.gdx.scenes.scene2d.Actor a : bt.getChildren()) {
            if (a instanceof TextButton tb) {
                tb.getLabel().setFontScale(1.0f);
                tb.getLabel().setAlignment(Align.center);

                if ("DELETE".equals(tb.getText().toString())) {
                    tb.setColor(new Color(0.85f, 0.3f, 0.3f, 1f));
                }
            }
        }

        d.show(stage);
    }




    private ScrollPane.ScrollPaneStyle createInvisibleScrollPaneStyle() {
        return new ScrollPane.ScrollPaneStyle();
    }
    /**
     * Renders the save selection UI.
     *
     * @param delta time elapsed since last frame (in seconds)
     */
    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.05f, 0.05f, 0.08f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.getBatch().begin();
        if (backgroundTexture != null) {
            stage.getBatch().setColor(0.5f, 0.5f, 0.5f, 1f);
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
        if(backgroundTexture != null) backgroundTexture.dispose();
    }
}