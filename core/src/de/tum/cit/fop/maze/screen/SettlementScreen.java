package de.tum.cit.fop.maze.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import de.tum.cit.fop.maze.MazeRunnerGame;
import de.tum.cit.fop.maze.audio.AudioManager;
import de.tum.cit.fop.maze.game.save.GameSaveData;
import de.tum.cit.fop.maze.game.achievement.AchievementType;
import de.tum.cit.fop.maze.game.score.LevelResult;
import de.tum.cit.fop.maze.tools.ButtonFactory;
import de.tum.cit.fop.maze.utils.LeaderboardManager;
import de.tum.cit.fop.maze.utils.Logger;
import de.tum.cit.fop.maze.game.save.StorageManager;

/**
 * Screen displayed after completing a level.
 *
 * <p>This screen shows the level result summary, score breakdown,
 * achievements unlocked, and handles leaderboard submission,
 * automatic saving, and navigation to the next level or menu.
 */
public class SettlementScreen implements Screen {
    private ScrollPane scrollPane;
    private Label scrollHintLabel;

    private final MazeRunnerGame game;
    private final LevelResult result;
    private final GameSaveData saveData;
    private Stage stage;
    private final LeaderboardManager leaderboardManager;

    private Texture backgroundTexture;

    private boolean isHighScore = false;
    private boolean scoreSubmitted = false;
    private TextField nameInput;

    private float displayedTotalScore;
    private final float targetTotalScore;
    private boolean isScoreRolling = true;

    private Label labelTotalScore;
    /**
     * Creates the settlement screen after a level is completed.
     *
     * @param game the main game instance
     * @param result the result data of the completed level
     * @param saveData the current save data to be updated
     */
    public SettlementScreen(MazeRunnerGame game, LevelResult result, GameSaveData saveData) {
        if (game == null) throw new IllegalArgumentException("MazeRunnerGame cannot be null");
        if (result == null) result = new LevelResult(0, 0, 0, "D", 0, 1.0f);
        if (saveData == null) saveData = new GameSaveData();

        this.game = game;
        this.result = result;
        this.saveData = saveData;
        this.leaderboardManager = new LeaderboardManager();

        this.displayedTotalScore = saveData.score;
        this.targetTotalScore = this.saveData.score + result.finalScore;

        this.isHighScore = leaderboardManager.isHighScore((int) targetTotalScore);

        try {
            if (Gdx.files.internal("imgs/menu_bg/bg_front.png").exists()) {
                backgroundTexture = new Texture(Gdx.files.internal("imgs/menu_bg/bg_front.png"));
            }
        } catch (Exception e) {
            Logger.warning("Failed to load settlement background: " + e.getMessage());
        }
    }

    @Override
    public void show() {
        AudioManager.getInstance().stopAll();

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        performAutoSave();

        setupUI();

        stage.setKeyboardFocus(null);
    }


    private void performAutoSave() {
        try {
            GameSaveData autoSaveData = new GameSaveData(saveData);

            autoSaveData.score += result.finalScore;

            autoSaveData.currentLevel++;

            autoSaveData.resetSessionStats();
            autoSaveData.maze = null;

            StorageManager.getInstance().saveAuto(autoSaveData);

            Logger.info("Auto save created: Level " + autoSaveData.currentLevel + ", Score " + autoSaveData.score);
        } catch (Exception e) {
            Logger.error("Failed to perform auto save: " + e.getMessage());
        }
    }

    private void setupUI() {
        stage.clear();

        Table mainRoot = new Table();
        mainRoot.setFillParent(true);
        stage.addActor(mainRoot);


        Table scrollContent = new Table();
        scrollContent.pad(40);
        scrollContent.top();

        Label titleLabel = new Label("LEVEL COMPLETED", game.getSkin(), "title");
        titleLabel.setColor(Color.GOLD);
        titleLabel.setFontScale(1.2f);
        titleLabel.getColor().a = 0f;
        titleLabel.addAction(Actions.fadeIn(1.0f));
        scrollContent.add(titleLabel).padBottom(10).row();

        Label rankLabel = new Label(result.rank, game.getSkin(), "title");
        rankLabel.setFontScale(6.0f);
        rankLabel.setAlignment(Align.center);
        setRankColor(rankLabel, result.rank);

        rankLabel.setOrigin(Align.center);
        rankLabel.setColor(rankLabel.getColor().r, rankLabel.getColor().g, rankLabel.getColor().b, 0f);
        rankLabel.setScale(3.0f);
        rankLabel.addAction(Actions.sequence(
                Actions.delay(0.3f),
                Actions.parallel(Actions.fadeIn(0.2f), Actions.scaleTo(1f, 1f, 0.5f, Interpolation.bounceOut))
        ));
        scrollContent.add(rankLabel).padBottom(0).row();

        if ("S".equals(result.rank)) {
            Label praise = new Label("PERFECT!", game.getSkin());
            praise.setColor(Color.GOLD);
            praise.setFontScale(1.2f);
            scrollContent.add(praise).padBottom(20).row();
        } else {
            scrollContent.add(new Label("", game.getSkin())).height(10).row();
        }

        Table scoreTable = new Table();
        float tableWidth = 500f;

        addScoreRow(scoreTable, "Base Score", "+" + formatScore(result.baseScore), Color.WHITE);
        addScoreRow(scoreTable, "Penalty", "-" + formatScore(result.penaltyScore), Color.SCARLET);
        addScoreRow(scoreTable, "Multiplier", getMultiplierText(result.scoreMultiplier), Color.CYAN);

        Label line = new Label("- - - - - - - - - -", game.getSkin());
        line.setColor(Color.GRAY);
        line.setAlignment(Align.center);
        scoreTable.add(line).colspan(2).pad(10).row();

        addScoreRow(scoreTable, "LEVEL SCORE", String.valueOf(result.finalScore), Color.GOLD);

        scoreTable.add(new Label("TOTAL SCORE", game.getSkin())).align(Align.left).padTop(15);

        String scoreText = isScoreRolling ? formatScore((int)displayedTotalScore) : formatScore((int)targetTotalScore);
        labelTotalScore = new Label(scoreText, game.getSkin());
        labelTotalScore.setColor(Color.ORANGE);
        labelTotalScore.setFontScale(1.5f);
        scoreTable.add(labelTotalScore).align(Align.right).padTop(15);
        scoreTable.row();

        scrollContent.add(scoreTable).width(tableWidth).padBottom(30).row();

        Table statsTable = new Table();
        int totalKills = saveData.sessionKills.values().stream().mapToInt(Integer::intValue).sum();

        Label lKills = new Label("Kills: " + totalKills, game.getSkin());
        lKills.setColor(Color.LIGHT_GRAY);

        Label lDamage = new Label("Damage: " + saveData.sessionDamageTaken, game.getSkin());
        lDamage.setColor(Color.LIGHT_GRAY);

        statsTable.add(lKills).padRight(40);
        statsTable.add(lDamage);
        scrollContent.add(statsTable).padBottom(30).row();

        if (!saveData.newAchievements.isEmpty()) {
            Label achTitle = new Label("NEW UNLOCKS", game.getSkin());
            achTitle.setColor(Color.YELLOW);
            scrollContent.add(achTitle).padBottom(10).row();

            for (String achId : saveData.newAchievements) {
                String name = achId;
                for (AchievementType t : AchievementType.values()) {
                    if (t.id.equals(achId)) { name = t.displayName; break; }
                }
                Label achLabel = new Label("★ " + name, game.getSkin());
                achLabel.setColor(Color.GREEN);
                scrollContent.add(achLabel).padBottom(5).row();
            }
            scrollContent.add(new Label("", game.getSkin())).padBottom(20).row();
        }

        if (isHighScore && !scoreSubmitted) {
            Table inputContainer = new Table();
            inputContainer.setBackground(createColorDrawable(new Color(0f, 0f, 0f, 0.3f)));
            inputContainer.pad(20);

            if (!scoreSubmitted) {
                Label newRecLabel = new Label("NEW HIGH SCORE!", game.getSkin());
                newRecLabel.setColor(Color.YELLOW);
                inputContainer.add(newRecLabel).padBottom(15).row();

                Table inputRow = new Table();

                nameInput = new TextField("", createModernTextFieldStyle());
                nameInput.setMessageText("Enter Name");
                nameInput.setMaxLength(12);
                nameInput.setAlignment(Align.center);
                nameInput.setTextFieldFilter((textField, c) ->
                        Character.isLetterOrDigit(c) || c == '_' || c == ' '
                );

                nameInput.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        stage.setKeyboardFocus(nameInput);
                    }
                });

                inputRow.add(nameInput).width(300).height(50).padRight(15);

                ButtonFactory bf = new ButtonFactory(game.getSkin());
                inputRow.add(bf.create("SUBMIT", () -> {
                    String name = nameInput.getText();
                    if (name == null || name.trim().isEmpty()) name = "Traveler";

                    leaderboardManager.addScore(name, (int) targetTotalScore);

                    scoreSubmitted = true;
                    if (game.getGameManager() != null) {
                        game.getGameManager().saveGameProgress();
                    }

                    Gdx.app.postRunnable(this::setupUI);

                })).width(200).height(50);

                inputContainer.add(inputRow);

            } else {
                Label successLabel = new Label("SCORE SUBMITTED!", game.getSkin());
                successLabel.setColor(Color.GREEN);
                successLabel.setFontScale(1.1f);
                inputContainer.add(successLabel).padBottom(5).row();

                Label infoLabel = new Label("Check the Leaderboard in Menu.", game.getSkin());
                infoLabel.setColor(Color.GRAY);
                infoLabel.setFontScale(0.9f);
                inputContainer.add(infoLabel);
            }
            scrollContent.add(inputContainer).padBottom(30).row();
        }

        scrollContent.add(new Label("", game.getSkin())).height(150).row();

        scrollPane = new ScrollPane(scrollContent, createScrollPaneStyle());
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false);
        scrollPane.setCancelTouchFocus(false);

        mainRoot.add(scrollPane).expand().fill().row();

        stage.setScrollFocus(scrollPane);


        if (!scoreSubmitted) {
            scrollHintLabel = new Label("Scroll down ↓", game.getSkin());
            scrollHintLabel.setColor(1f, 1f, 1f, 0.8f);
            scrollHintLabel.setAlignment(Align.center);
            scrollHintLabel.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.disabled);
            scrollHintLabel.setPosition(Gdx.graphics.getWidth() / 2f, 130f, Align.center);

            scrollHintLabel.addAction(Actions.sequence(
                    Actions.parallel(
                            Actions.repeat(4, Actions.sequence(
                                    Actions.moveBy(0, -10, 0.5f, Interpolation.sine),
                                    Actions.moveBy(0, 10, 0.5f, Interpolation.sine)
                            )),
                            Actions.sequence(
                                    Actions.delay(3.0f),
                                    Actions.fadeOut(0.5f)
                            )
                    ),
                    Actions.removeActor()
            ));

            stage.addActor(scrollHintLabel);
        }


        Table footer = new Table();
        footer.pad(20);
        footer.setBackground(createColorDrawable(new Color(0, 0, 0, 0.6f)));

        ButtonFactory bf = new ButtonFactory(game.getSkin());
        float btnWidth = 380f;
        float btnHeight = 80f;

        footer.add(bf.create("NEXT LEVEL", () -> performSaveAndExit(true))).width(btnWidth).height(btnHeight).padRight(40);
        footer.add(bf.create("MENU", () -> performSaveAndExit(false))).width(btnWidth).height(btnHeight);

        mainRoot.add(footer).fillX().bottom();
    }


    private ScrollPane.ScrollPaneStyle createScrollPaneStyle() {
        ScrollPane.ScrollPaneStyle style = new ScrollPane.ScrollPaneStyle();
        style.vScrollKnob = createColorDrawable(new Color(1f, 1f, 1f, 0.3f));
        return style;
    }


    private TextField.TextFieldStyle createModernTextFieldStyle() {
        TextField.TextFieldStyle style = new TextField.TextFieldStyle();
        style.font = game.getSkin().getFont("default-font");
        if (style.font == null) style.font = new BitmapFont();
        style.fontColor = Color.WHITE;

        style.cursor = createColorDrawable(Color.WHITE);
        style.cursor.setMinWidth(2);

        style.selection = createColorDrawable(new Color(0, 0.5f, 1f, 0.5f));

        style.background = createBorderedDrawable(
                new Color(0.15f, 0.15f, 0.18f, 1f), // Bg
                new Color(0.5f, 0.5f, 0.5f, 1f)     // Border
        );

        style.focusedBackground = createBorderedDrawable(
                new Color(0.2f, 0.2f, 0.25f, 1f),
                new Color(0.9f, 0.8f, 0.2f, 1f)     // Gold Border
        );

        return style;
    }


    private TextureRegionDrawable createBorderedDrawable(Color bgColor, Color borderColor) {
        int w = 64;
        int h = 64;
        int border = 2;

        Pixmap pixmap = new Pixmap(w, h, Pixmap.Format.RGBA8888);

        pixmap.setColor(borderColor);
        pixmap.fill();

        pixmap.setColor(bgColor);
        pixmap.fillRectangle(border, border, w - 2*border, h - 2*border);

        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return new TextureRegionDrawable(new TextureRegion(texture));
    }

    private TextureRegionDrawable createColorDrawable(Color color) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        TextureRegionDrawable drawable = new TextureRegionDrawable(new TextureRegion(new Texture(pixmap)));
        pixmap.dispose();
        return drawable;
    }

    private void performSaveAndExit(boolean toNextLevel) {
        clearNewAchievements();
        StorageManager storage = StorageManager.getInstance();

        saveData.score += result.finalScore;

        saveData.currentLevel++;
        saveData.levelBaseScore = 0;
        saveData.levelPenalty = 0;

        saveData.maze = null;

        if (toNextLevel) {
            if (game.getGameManager() != null && game.getGameManager().getScoreManager() != null) {
                GameSaveData tempData = new GameSaveData();
                tempData.score = saveData.score;
                tempData.levelBaseScore = 0;
                tempData.levelPenalty = 0;
                game.getGameManager().getScoreManager().restoreState(tempData);
            }

            storage.saveAuto(saveData);
            game.loadGame();
        } else {
            storage.saveAuto(saveData);
            game.goToMenu();
        }
    }

    private void addScoreRow(Table table, String name, String value, Color valueColor) {
        table.add(new Label(name, game.getSkin())).align(Align.left).expandX();
        Label valLabel = new Label(value, game.getSkin());
        valLabel.setColor(valueColor);
        table.add(valLabel).align(Align.right);
        table.row();
    }

    private String formatScore(int score) {
        return String.format("%,d", score);
    }

    private String getMultiplierText(float multiplier) {
        String difficultyHint = "";
        if (multiplier >= 1.5f) difficultyHint = " (Hard)";
        else if (multiplier >= 1.2f) difficultyHint = " (Normal)";
        else if (multiplier >= 1.0f) difficultyHint = " (Easy)";
        else if (multiplier >= 2.0f) difficultyHint = " (Endless)";
        return String.format("x%.1f%s", multiplier, difficultyHint);
    }

    private void setRankColor(Label label, String rank) {
        switch (rank) {
            case "S" -> label.setColor(1f, 0.84f, 0f, 1f);
            case "A" -> label.setColor(0.75f, 0.75f, 0.75f, 1f);
            case "B" -> label.setColor(0.8f, 0.5f, 0.2f, 1f);
            default  -> label.setColor(Color.WHITE);
        }
    }

    private void clearNewAchievements() {
        if (saveData != null) {
            saveData.newAchievements.clear();
            saveData.sessionDamageTaken = 0;
            saveData.sessionKills.clear();
        }
    }
    /**
     * Renders the settlement screen, including background,
     * score animation, and UI interactions.
     *
     * @param delta time elapsed since last frame (in seconds)
     */
    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.08f, 0.08f, 0.12f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.getBatch().begin();
        if (backgroundTexture != null) {
            stage.getBatch().setColor(0.4f, 0.4f, 0.4f, 1f);
            stage.getBatch().draw(backgroundTexture, 0, 0,
                    Gdx.graphics.getWidth(),
                    Gdx.graphics.getHeight());
            stage.getBatch().setColor(Color.WHITE);
        }
        stage.getBatch().end();

        if (isScoreRolling && labelTotalScore != null) {
            float diff = targetTotalScore - displayedTotalScore;
            if (Math.abs(diff) < 5) {
                displayedTotalScore = targetTotalScore;
                isScoreRolling = false;
            } else {
                displayedTotalScore += diff * 2.0f * delta;
            }
            labelTotalScore.setText(formatScore((int) displayedTotalScore));
        }

        stage.act(delta);
        stage.draw();
    }

    @Override public void resize(int w, int h) { stage.getViewport().update(w, h, true); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() {
        stage.dispose();
        if(backgroundTexture != null) backgroundTexture.dispose();
    }
}