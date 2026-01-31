package de.tum.cit.fop.maze.game.save;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import de.tum.cit.fop.maze.MazeRunnerGame;
import de.tum.cit.fop.maze.tools.ButtonFactory;
import de.tum.cit.fop.maze.utils.Logger;

/**
 * UI panel for displaying and managing saved game slots.
 *
 * <p>This panel shows the auto-save entry and all manual save slots,
 * allowing the player to load or delete existing saves.
 * It is designed to be displayed as an overlay inside menu screens.
 *
 * <p>The panel handles its own background rendering, input handling,
 * and dynamic rebuilding when save data changes.
 */
public class SaveListPanel extends Table {

    private final MazeRunnerGame game;
    private final Skin skin;
    private final StorageManager storage;

    private Texture backgroundTexture;
    /**
     * Creates a save list panel displaying all available save slots.
     *
     * @param game the main game instance used to load save data and switch screens
     * @param skin the UI skin used for rendering widgets
     */
    public SaveListPanel(MazeRunnerGame game, Skin skin) {
        super(skin);
        this.game = game;
        this.skin = skin;
        this.storage = StorageManager.getInstance();

        this.setFillParent(true);

        this.setTouchable(Touchable.enabled);
        this.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
            }
        });

        loadBackground();

        rebuild();
    }

    private void loadBackground() {
        try {
            if (Gdx.files.internal("imgs/menu_bg/bg_front.png").exists()) {
                backgroundTexture = new Texture(Gdx.files.internal("imgs/menu_bg/bg_front.png"));
            }
        } catch (Exception e) {
            Logger.warning("SaveListPanel background load failed: " + e.getMessage());
        }

        if (backgroundTexture == null) {
            setBackground(createColorDrawable(new Color(0.05f, 0.05f, 0.08f, 0.95f)));
        }
    }

    /**
     * Draws the panel, including its background texture if available.
     *
     * @param batch the batch used for rendering
     * @param parentAlpha the parent alpha value
     */
    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (backgroundTexture != null) {
            batch.setColor(0.5f, 0.5f, 0.5f, parentAlpha);
            batch.draw(backgroundTexture, getX(), getY(), getWidth(), getHeight());
            batch.setColor(Color.WHITE);
        }
        super.draw(batch, parentAlpha);
    }

    private void rebuild() {
        clearChildren();

        Table headerTable = new Table();
        headerTable.pad(30);

        Label title = new Label("SELECT SAVE FILE", skin, "title");
        title.setColor(Color.GOLD);
        title.setFontScale(1.2f);
        headerTable.add(title).padBottom(10).row();

        Label hint = new Label("Choose a record to continue your journey", skin);
        hint.setColor(Color.LIGHT_GRAY);
        headerTable.add(hint).row();

        add(headerTable).top().padTop(40).row();

        Table listContent = new Table();
        listContent.top().pad(20);

        GameSaveData autoData = storage.loadAutoSave();
        listContent.add(createSaveCard("AUTO SAVE", -1, autoData, true))
                .width(1000).padBottom(20).row();

        for (int i = 1; i <= StorageManager.MAX_SAVE_SLOTS; i++) {
            GameSaveData data = storage.loadGameFromSlot(i);
            listContent.add(createSaveCard("SAVE SLOT " + i, i, data, false))
                    .width(1000).padBottom(20).row();
        }

        listContent.add(new Label("", skin)).height(80).row();

        ScrollPane scrollPane = new ScrollPane(listContent, createScrollPaneStyle());
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false);

        add(scrollPane).expand().fill().padBottom(20).row();

        Table footer = new Table();
        ButtonFactory bf = new ButtonFactory(skin);

        footer.add(bf.create("BACK", this::remove))
                .width(300).height(60);

        add(footer).bottom().padBottom(40);
    }

    private Table createSaveCard(String title, int slotId, GameSaveData data, boolean isAuto) {
        Table card = new Table();
        boolean exists = (data != null);

        Color bgColor = exists ? new Color(0.15f, 0.15f, 0.18f, 0.9f) : new Color(0.1f, 0.1f, 0.1f, 0.5f);
        Color borderColor = isAuto ? new Color(1f, 0.8f, 0.2f, 1f) : (exists ? Color.GRAY : Color.DARK_GRAY);

        card.setBackground(createBorderedDrawable(bgColor, borderColor));
        card.pad(20);

        Table infoTable = new Table();

        Label titleLabel = new Label(title, skin);
        titleLabel.setFontScale(1.1f);
        titleLabel.setColor(isAuto ? Color.ORANGE : Color.LIGHT_GRAY);
        infoTable.add(titleLabel).left().padBottom(10).row();

        if (exists) {
            String scoreStr = String.format("%,d", data.score);
            String info = String.format("Floor: %d   |   %s   |   Score: %s",
                    data.currentLevel, data.difficulty, scoreStr);

            Label details = new Label(info, skin);
            details.setColor(Color.WHITE);
            details.setWrap(true);
            infoTable.add(details).left().width(600);
        } else {
            Label empty = new Label("- Empty Slot -", skin);
            empty.setColor(new Color(1, 1, 1, 0.3f));
            infoTable.add(empty).left();
        }

        card.add(infoTable).expandX().fillX().left();

        Table btnTable = new Table();
        ButtonFactory bf = new ButtonFactory(skin);

        if (exists) {
            btnTable.add(bf.create("LOAD", () -> showLoadDialog(slotId, title)))
                    .width(130).height(50).padRight(15);

            if (!isAuto) {
                TextButton delBtn = bf.create("DEL", () -> showDeleteDialog(slotId));
                delBtn.setColor(1f, 0.4f, 0.4f, 1f);
                btnTable.add(delBtn).width(130).height(50);
            } else {
                Label tag = new Label("AUTO", skin);
                tag.setColor(Color.YELLOW);
                tag.setAlignment(Align.center);
                btnTable.add(tag).width(130);
            }
        } else {
            Label unused = new Label("UNUSED", skin);
            unused.setColor(new Color(1,1,1,0.1f));
            unused.setAlignment(Align.center);
            btnTable.add(unused).width(275);
        }

        card.add(btnTable).right();

        return card;
    }


    private void showLoadDialog(int slotId, String title) {
        class LoadDialog extends Dialog {
            public LoadDialog() { super("", skin); }
            @Override
            protected void result(Object object) {
                if ((Boolean) object) {
                    if (slotId == -1) {
                        GameSaveData auto = storage.loadAutoSave();
                        if (auto != null) {
                            storage.saveAuto(auto);
                            game.loadGame();
                        }
                    } else {
                        game.loadGameFromSlot(slotId);
                    }
                } else {
                    hide();
                }
            }
        }

        LoadDialog d = new LoadDialog();
        styleDialog(d);

        Label text = new Label("Load " + title + "?\nCurrent progress will be lost.", skin);
        text.setAlignment(Align.center);
        d.text(text);

        ButtonFactory bf = new ButtonFactory(skin);
        d.getButtonTable().add(bf.create("YES", () -> d.result(true))).width(120).padRight(20);
        d.getButtonTable().add(bf.create("NO", () -> d.result(false))).width(120);

        d.show(getStage());
    }

    private void showDeleteDialog(int slotId) {
        class DeleteDialog extends Dialog {
            public DeleteDialog() { super("", skin); }
            @Override
            protected void result(Object object) {
                if ((Boolean) object) {
                    storage.deleteSaveSlot(slotId);
                    rebuild();
                } else {
                    hide();
                }
            }
        }

        DeleteDialog d = new DeleteDialog();
        styleDialog(d);

        Label text = new Label("Delete Slot " + slotId + "?\nThis cannot be undone!", skin);
        text.setColor(Color.SALMON);
        text.setAlignment(Align.center);
        d.text(text);

        ButtonFactory bf = new ButtonFactory(skin);
        d.getButtonTable().add(bf.create("DELETE", () -> d.result(true))).width(120).padRight(20);
        d.getButtonTable().add(bf.create("CANCEL", () -> d.result(false))).width(120);

        d.show(getStage());
    }

    private void styleDialog(Dialog d) {
        d.setBackground(createBorderedDrawable(new Color(0.1f,0.1f,0.1f,0.95f), Color.WHITE));
        d.getContentTable().pad(30);
        d.getButtonTable().pad(20);
    }


    private ScrollPane.ScrollPaneStyle createScrollPaneStyle() {
        ScrollPane.ScrollPaneStyle style = new ScrollPane.ScrollPaneStyle();
        style.vScrollKnob = createColorDrawable(new Color(1f, 1f, 1f, 0.2f));
        return style;
    }

    private TextureRegionDrawable createColorDrawable(Color color) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture t = new Texture(pixmap);

        pixmap.dispose();
        return new TextureRegionDrawable(new TextureRegion(t));
    }

    private TextureRegionDrawable createBorderedDrawable(Color bg, Color border) {
        int w = 64, h = 64, b = 2;
        Pixmap p = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        p.setColor(border);
        p.fill();
        p.setColor(bg);
        p.fillRectangle(b, b, w - 2*b, h - 2*b);
        Texture t = new Texture(p);
        p.dispose();
        return new TextureRegionDrawable(new TextureRegion(t));
    }
}