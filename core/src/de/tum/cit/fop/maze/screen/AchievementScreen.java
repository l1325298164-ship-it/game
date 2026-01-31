package de.tum.cit.fop.maze.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import de.tum.cit.fop.maze.MazeRunnerGame;
import de.tum.cit.fop.maze.game.achievement.AchievementType;
import de.tum.cit.fop.maze.game.achievement.CareerData;
import de.tum.cit.fop.maze.tools.ButtonFactory;
import de.tum.cit.fop.maze.utils.Logger;
import de.tum.cit.fop.maze.game.save.StorageManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Screen displaying the player's unlocked and locked achievements.
 *
 * <p>This screen shows all available achievements, their descriptions,
 * unlock status, and progress toward completion based on {@link CareerData}.
 *
 * <p>Achievements are sorted with unlocked ones displayed first.
 * The screen provides a scrollable UI and allows returning to the previous screen.
 */
public class AchievementScreen implements Screen {

    private final MazeRunnerGame game;
    private final Screen previousScreen;
    private Stage stage;
    private CareerData careerData;
    private Texture backgroundTexture;

    private static final int TARGET_KILLS_E01 = 60;
    private static final int TARGET_KILLS_E02 = 40;
    private static final int TARGET_KILLS_E03 = 50;
    private static final int TARGET_KILLS_E04_DASH = 50;
    private static final int TARGET_KILLS_GLOBAL = 500;
    private static final int TARGET_HEARTS = 50;
    private static final int TARGET_TREASURE_TYPES = 3;
    /**
     * Creates the achievement overview screen.
     *
     * @param game the main game instance
     * @param previousScreen the screen to return to when exiting this screen
     */
    public AchievementScreen(MazeRunnerGame game, Screen previousScreen) {
        this.game = game;
        this.previousScreen = previousScreen;
        this.careerData = StorageManager.getInstance().loadCareer();

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

        Table headerTable = new Table();
        headerTable.pad(20);

        Label title = new Label("ACHIEVEMENTS", game.getSkin(), "title");
        title.setColor(Color.GOLD);
        title.setFontScale(1.2f);
        headerTable.add(title).padBottom(10).row();

        int unlocked = careerData.unlockedAchievements.size();
        int total = AchievementType.values().length;
        Label statsLabel = new Label("Progress: " + unlocked + " / " + total, game.getSkin());
        statsLabel.setColor(Color.LIGHT_GRAY);
        headerTable.add(statsLabel).row();

        root.add(headerTable).padTop(30).padBottom(20).row();

        Table listContent = new Table();
        listContent.top();
        listContent.pad(10);

        float cardWidth = Gdx.graphics.getWidth() * 0.85f;

        List<AchievementType> allAchievements = new ArrayList<>(Arrays.asList(AchievementType.values()));

        allAchievements.sort((a, b) -> {
            boolean aUnlocked = careerData.unlockedAchievements.contains(a.id);
            boolean bUnlocked = careerData.unlockedAchievements.contains(b.id);

            if (aUnlocked && !bUnlocked) return -1; // a 在前
            if (!aUnlocked && bUnlocked) return 1;  // b 在前
            return a.ordinal() - b.ordinal();       // 同状态下按默认顺序
        });

        for (AchievementType type : allAchievements) {
            boolean isUnlocked = careerData.unlockedAchievements.contains(type.id);
            Table card = createAchievementCard(type, isUnlocked);
            listContent.add(card).width(cardWidth).padBottom(25).row();
        }

        ScrollPane scrollPane = new ScrollPane(listContent, createScrollPaneStyle());
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false); // 禁止水平，允许垂直
        scrollPane.setOverscroll(false, true); // 允许回弹

        root.add(scrollPane).expand().fill().padBottom(20).row();

        Table footer = new Table();
        ButtonFactory bf = new ButtonFactory(game.getSkin());
        footer.add(bf.create("BACK", () -> game.setScreen(previousScreen)))
                .width(350).height(70);

        root.add(footer).padBottom(30);
    }

    private Table createAchievementCard(AchievementType type, boolean isUnlocked) {
        Table card = new Table();

        Color bgColor = isUnlocked ? new Color(0.2f, 0.25f, 0.35f, 0.5f) : new Color(0.05f, 0.05f, 0.05f, 0.3f);
        card.setBackground(createColorDrawable(bgColor));
        card.pad(20);

        String iconText = isUnlocked ? "★" : "🔒";
        Label iconLabel = new Label(iconText, game.getSkin());
        iconLabel.setFontScale(2.5f);
        iconLabel.setColor(isUnlocked ? Color.GOLD : Color.DARK_GRAY);
        iconLabel.setAlignment(Align.center);
        card.add(iconLabel).padBottom(10).row();

        Label nameLabel = new Label(type.displayName, game.getSkin());
        nameLabel.setFontScale(1.3f);
        nameLabel.setColor(isUnlocked ? Color.WHITE : Color.GRAY);
        nameLabel.setAlignment(Align.center);
        card.add(nameLabel).expandX().fillX().padBottom(8).row();

        String descText = type.description;
        Label descLabel = new Label(descText, game.getSkin());
        descLabel.setFontScale(1.0f);
        descLabel.setColor(isUnlocked ? Color.LIGHT_GRAY : new Color(0.6f, 0.6f, 0.6f, 1f));
        descLabel.setAlignment(Align.center);
        descLabel.setWrap(true);
        card.add(descLabel).width(Gdx.graphics.getWidth() * 0.7f).padBottom(10).row();

        String progressText = getProgressText(type);
        if (progressText != null && !isUnlocked) {
            Label progLabel = new Label(progressText, game.getSkin());
            progLabel.setFontScale(0.9f);
            progLabel.setColor(Color.CYAN);
            progLabel.setAlignment(Align.center);
            card.add(progLabel);
        } else if (isUnlocked) {
            Label doneLabel = new Label("- COMPLETED -", game.getSkin());
            doneLabel.setColor(Color.GREEN);
            doneLabel.setFontScale(0.8f);
            doneLabel.setAlignment(Align.center);
            card.add(doneLabel);
        }

        return card;
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
        TextureRegionDrawable drawable = new TextureRegionDrawable(new TextureRegion(new Texture(pixmap)));
        pixmap.dispose();
        return drawable;
    }

    private String getProgressText(AchievementType type) {
        try {
            switch (type) {
                case ACH_04_PEARL_SWEEPER: return careerData.totalKills_E01 + "/" + TARGET_KILLS_E01;
                case ACH_05_COFFEE_GRINDER: return careerData.totalKills_E02 + "/" + TARGET_KILLS_E02;
                case ACH_06_CARAMEL_MELT: return careerData.totalKills_E03 + "/" + TARGET_KILLS_E03;
                case ACH_07_SHELL_BREAKER: return careerData.totalDashKills_E04 + "/" + TARGET_KILLS_E04_DASH;
                case ACH_08_BEST_SELLER: return careerData.totalKills_Global + "/" + TARGET_KILLS_GLOBAL;
                case ACH_09_FREE_TOPPING: return careerData.totalHeartsCollected + "/" + TARGET_HEARTS;
                case ACH_10_TREASURE_MASTER: return careerData.collectedBuffTypes.size() + "/" + TARGET_TREASURE_TYPES;
                default: return null;
            }
        } catch (Exception e) {
            return "???";
        }
    }
    /**
     * Renders the achievement screen and updates UI animations.
     *
     * @param delta time elapsed since last frame (in seconds)
     */
    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.1f, 0.12f, 0.18f, 1f);
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
    @Override public void show() {
        Gdx.input.setInputProcessor(stage);
    }
    @Override public void hide() {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void dispose() {
        stage.dispose();
        if (backgroundTexture != null) backgroundTexture.dispose();
    }
}