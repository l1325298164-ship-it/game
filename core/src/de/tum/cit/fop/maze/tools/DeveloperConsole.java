package de.tum.cit.fop.maze.tools;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldStyle;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import de.tum.cit.fop.maze.game.GameManager;
import de.tum.cit.fop.maze.entities.Player;
import de.tum.cit.fop.maze.entities.enemy.Enemy;

/**
 * In-game developer console for debugging and testing purposes.
 *
 * <p>The developer console provides a command-line style interface
 * that allows developers to execute debug commands such as healing
 * the player, spawning items, modifying game variables, or clearing enemies.
 *
 * <p>The console is rendered as a self-contained {@link Stage} and can be
 * toggled on or off during gameplay.
 */
public class DeveloperConsole {

    private boolean isVisible = false;
    private final Stage stage;
    private final GameManager gameManager;

    private TextField inputField;
    private Label logLabel;
    private ScrollPane scrollPane;
    private final StringBuilder logHistory = new StringBuilder();

    private Texture bgTexture;
    private Texture cursorTexture;
    private BitmapFont font;
    /**
     * Creates a new developer console bound to a specific {@link GameManager}.
     *
     * @param gameManager the game manager instance used to execute debug commands
     * @param skin the UI skin used for styling console components
     */
    public DeveloperConsole(GameManager gameManager, Skin skin) {
        this.gameManager = gameManager;
        this.stage = new Stage(new ScreenViewport());

        setupSelfContainedUI();
    }

    private void setupSelfContainedUI() {
        font = new BitmapFont(); // 使用 LibGDX 默认字体 (Arial)
        font.getData().setScale(2.0f);

        Pixmap p = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        p.setColor(0, 0, 0, 0.7f);
        p.fill();
        bgTexture = new Texture(p);

        p.setColor(Color.WHITE);
        p.fill();
        cursorTexture = new Texture(p);
        p.dispose(); // 释放 Pixmap

        TextureRegionDrawable bgDrawable = new TextureRegionDrawable(new TextureRegion(bgTexture));
        TextureRegionDrawable cursorDrawable = new TextureRegionDrawable(new TextureRegion(cursorTexture));

        LabelStyle labelStyle = new LabelStyle(font, Color.WHITE);

        TextFieldStyle tfStyle = new TextFieldStyle();
        tfStyle.font = font;
        tfStyle.fontColor = Color.WHITE;
        tfStyle.cursor = cursorDrawable;
        tfStyle.selection = cursorDrawable;
        tfStyle.background = bgDrawable; // 输入框背景

        Table rootTable = new Table();
        rootTable.setFillParent(true);
        rootTable.top().left();
        Table consoleTable = new Table();
        consoleTable.setBackground(bgDrawable); // 整个控制台背景

        logLabel = new Label("Console initialized. Commands: help, heal, give_key, kill_all\n", labelStyle);
        logLabel.setWrap(true);
        logLabel.setAlignment(Align.topLeft);

        scrollPane = new ScrollPane(logLabel); // 默认 ScrollPane 样式即可
        scrollPane.setFadeScrollBars(false);

        inputField = new TextField("", tfStyle);
        inputField.setMessageText("Enter command...");

        consoleTable.add(scrollPane).expandX().fillX().height(300).pad(5).row();
        consoleTable.add(inputField).expandX().fillX().height(50).pad(5).row();

        rootTable.add(consoleTable).growX().top();
        stage.addActor(rootTable);

        inputField.setTextFieldListener((textField, c) -> {
            if (c == '\r' || c == '\n') {
                String cmd = textField.getText().trim();
                if (!cmd.isEmpty()) {
                    processCommand(cmd);
                    textField.setText("");
                }
            }
        });
    }

    private void processCommand(String rawCommand) {
        log("> " + rawCommand);

        String[] parts = rawCommand.split(" ");
        String command = parts[0].toLowerCase();

        try {
            switch (command) {
                case "help":
                    log("Available: heal [amt], give_key, kill_all, clear");
                    break;
                case "clear":
                    logHistory.setLength(0);
                    logLabel.setText("");
                    break;
                case "heal":
                    Player p = gameManager.getPlayer();
                    int amt = parts.length > 1 ? Integer.parseInt(parts[1]) : 100;
                    p.heal(amt);
                    log("Healed player by " + amt);
                    break;
                case "give_key":
                    gameManager.getPlayer().setHasKey(true);
                    log("Key added to inventory.");
                    break;
                case "kill_all":
                    int count = 0;
                    for (Enemy e : gameManager.getEnemies()) {
                        e.takeDamage(9999);
                        count++;
                    }
                    log("Killed " + count + " enemies.");
                    break;
                case "set":
                    if (parts.length < 3) {
                        log("Usage: set <variable> <value>");
                    } else {
                        try {
                            String key = parts[1].toLowerCase(); // 转小写，防止大小写不一致
                            float val = Float.parseFloat(parts[2]);

                            if (key.equals("cam_zoom")) {
                                if (val <= 0) {
                                    log("Error: cam_zoom must be positive."); // 必须是正数 (>0)
                                    break;
                                }
                            } else if (key.equals("time_scale")) {
                                if (val < 0) {
                                    log("Error: time_scale cannot be negative."); // 不能是负数 (>=0)
                                    break;
                                }
                            }

                            gameManager.setVariable(key, val);
                            log("Set " + key + " to " + val);

                        } catch (NumberFormatException e) {
                            log("Invalid number format.");
                        }
                    }
                    break;


                case "get":
                    if (parts.length < 2) {
                        log("Usage: get <variable>");
                    } else {
                        String key = parts[1];
                        float val = gameManager.getVariable(key);
                        log(key + " = " + val);
                    }
                    break;

                default:
                    log("Unknown command.");
                    break;
            }
        } catch (Exception e) {
            log("Error: " + e.getMessage());
        }
    }

    private void log(String message) {
        logHistory.append(message).append("\n");
        if (logLabel != null) {
            logLabel.setText(logHistory.toString());
        }
        if (scrollPane != null) {
            // 自动滚动到底部
            Gdx.app.postRunnable(() -> scrollPane.setScrollY(scrollPane.getMaxY()));
        }
    }
    /**
     * Toggles the visibility of the developer console.
     *
     * <p>When enabled, the console captures keyboard input and
     * allows command execution. When disabled, input focus is released.
     */
    public void toggle() {
        isVisible = !isVisible;
        if (isVisible) {
            Gdx.input.setInputProcessor(stage);
            stage.setKeyboardFocus(inputField);
        } else {
            stage.setKeyboardFocus(null);
            stage.unfocusAll();
        }
    }
    /**
     * Renders the developer console if it is currently visible.
     *
     * <p>This method updates and draws the internal {@link Stage}.
     */
    public void render() {
        if (isVisible) {
            stage.act();
            stage.draw();
        }
    }
    /**
     * Updates the viewport size of the developer console.
     *
     * @param width new screen width
     * @param height new screen height
     */
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }
    /**
     * Releases all resources used by the developer console.
     *
     * <p>This includes textures, fonts, and the internal {@link Stage}.
     */
    public void dispose() {
        if (stage != null) stage.dispose();
        if (bgTexture != null) bgTexture.dispose();
        if (cursorTexture != null) cursorTexture.dispose();
        if (font != null) font.dispose();
    }
    /**
     * Checks whether the developer console is currently visible.
     *
     * @return {@code true} if the console is visible, {@code false} otherwise
     */
    public boolean isVisible() {
        return isVisible;
    }
}