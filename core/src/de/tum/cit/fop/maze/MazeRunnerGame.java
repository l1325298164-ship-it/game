package de.tum.cit.fop.maze;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import de.tum.cit.fop.maze.audio.AudioConfig;
import de.tum.cit.fop.maze.audio.AudioManager;
import de.tum.cit.fop.maze.audio.AudioType;
import de.tum.cit.fop.maze.entities.boss.BossLoadingScreen;
import de.tum.cit.fop.maze.entities.chapter.ChapterContext;
import de.tum.cit.fop.maze.game.*;
import de.tum.cit.fop.maze.game.save.GameSaveData;
import de.tum.cit.fop.maze.screen.*;
import de.tum.cit.fop.maze.tools.MazeRunnerGameHolder;
import de.tum.cit.fop.maze.tools.PVAnimationCache;
import de.tum.cit.fop.maze.tools.PVNode;
import de.tum.cit.fop.maze.tools.PVPipeline;
import de.tum.cit.fop.maze.utils.Logger;
import de.tum.cit.fop.maze.game.save.StorageManager;
import de.tum.cit.fop.maze.utils.TextureManager;

import java.util.List;
/**
 * Core entry point of the MazeRunner game.
 *
 * <p>This class is responsible for:
 * <ul>
 *     <li>Initializing global systems (audio, assets, UI skin)</li>
 *     <li>Managing game screens and transitions</li>
 *     <li>Holding runtime game state such as difficulty and GameManager</li>
 *     <li>Controlling story progression pipeline</li>
 * </ul>
 *
 * <p>It acts as the central coordinator between gameplay,
 * UI screens, and persistent storage.
 */
public class MazeRunnerGame extends Game {
    private AssetManager assets;
    private Difficulty currentDifficulty = Difficulty.NORMAL;
    /**
     * Returns the currently active difficulty.
     *
     * @return active difficulty, defaults to NORMAL if unset
     */
    public Difficulty getCurrentDifficulty() {
        return currentDifficulty != null ? currentDifficulty : Difficulty.NORMAL;
    }
    /**
     * Provides access to the global AssetManager.
     *
     * @return asset manager instance
     */
    public AssetManager getAssets() {
        return assets;
    }
    private SpriteBatch spriteBatch;
    private Skin skin;
    private AudioManager audioManager;
    private boolean twoPlayerMode = false;
    /**
     * Indicates whether the game is running in two-player mode.
     *
     * @return true if multiplayer is enabled
     */
    public boolean isTwoPlayerMode() { return twoPlayerMode; }

    private GameManager gameManager;
    private DifficultyConfig difficultyConfig;
    private PVPipeline storyPipeline;
    /**
     * Checks if a gameplay screen is currently active.
     *
     * @return true if GameScreen or EndlessScreen is running
     */
    public boolean hasRunningGame() {
        return getScreen() instanceof GameScreen || getScreen() instanceof EndlessScreen;
    }
    /**
     * Resumes gameplay by clearing any custom input processor.
     * Typically used after pausing overlays.
     */
    public void resumeGame() {
        if (getScreen() instanceof GameScreen gs) {
            Gdx.input.setInputProcessor(null);
        }
    }
    /**
     * Returns the active GameManager controlling gameplay logic.
     *
     * @return game manager instance
     */
    public GameManager getGameManager() { return gameManager; }
    /**
     * Starts a completely new game session.
     *
     * <p>This resets the GameManager, applies the selected difficulty,
     * and transitions to the appropriate first screen.
     *
     * @param difficulty selected difficulty level
     */
    public void startNewGame(Difficulty difficulty) {
        this.currentDifficulty = difficulty;
        Logger.debug("Start new game with difficulty = " + difficulty);

        this.difficultyConfig = DifficultyConfig.of(difficulty);
        this.gameManager = new GameManager(this.difficultyConfig, this.twoPlayerMode);
        this.gameManager.markAsNewGame();
        if (difficulty == Difficulty.ENDLESS) {
            if (getScreen() != null) getScreen().hide();
            EndlessScreen endlessScreen = new EndlessScreen(this, difficultyConfig);
            setScreen(endlessScreen);
            return;
        }

        this.stage = StoryStage.STORY_BEGIN;
        setScreen(new StoryLoadingScreen(this));
    }


    /**
     * Debug helper that jumps directly into the boss loading screen.
     */
    public void debugEnterBoss() {
        setScreen(new BossLoadingScreen(this));
    }
    /**
     * Starts a game directly at a specific chapter.
     *
     * <p>Used for chapter selection or debugging flows.
     *
     * @param difficulty chosen difficulty
     * @param chapterContext chapter configuration and metadata
     */
    public void startChapterGame(
            Difficulty difficulty,
            ChapterContext chapterContext
    ) {
        this.currentDifficulty = difficulty;
        this.difficultyConfig = DifficultyConfig.of(difficulty);

        this.gameManager = new GameManager(
                this.difficultyConfig,
                this.twoPlayerMode,
                chapterContext
        );

        setScreen(new GameScreen(this, difficultyConfig, chapterContext));
    }

    public enum PV4Result { START, EXIT }
    public enum StoryStage {
        STORY_BEGIN, MAZE_GAME_TUTORIAL, PV4, MODE_MENU, MAZE_GAME, MAIN_MENU
    }


    private StoryStage stage = StoryStage.MAIN_MENU;
    /**
     * Initializes the game.
     *
     * <p>Creates rendering resources, loads UI skins,
     * configures audio, and shows the logo screen.
     */
    @Override
    public void create() {
        MazeRunnerGameHolder.init(this);
        assets = new AssetManager();
        currentDifficulty = Difficulty.NORMAL;
        difficultyConfig = DifficultyConfig.of(currentDifficulty);
        gameManager = new GameManager(difficultyConfig, twoPlayerMode);

        spriteBatch = new SpriteBatch();
        this.skin = new Skin();

        TextureAtlas buttonAtlas = new TextureAtlas(
                Gdx.files.internal("ui/button.atlas")
        );
        TextureAtlas windowAtlas = new TextureAtlas(
                Gdx.files.internal("Skin/skin.atlas")
        );

        skin.addRegions(buttonAtlas);
        skin.addRegions(windowAtlas);

        skin.load(Gdx.files.internal("ui/skinbutton.json"));

        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        Texture whiteTexture = new Texture(pixmap);
        skin.add("white", new TextureRegion(whiteTexture));
        pixmap.dispose();

        patchSkin(skin);

        initializeSoundManager();
        setScreen(new LogoScreen(this));
    }


    private void patchSkin(Skin skin) {
        BitmapFont font;
        try {
            if (skin.has("default-font", BitmapFont.class)) {
                font = skin.get("default-font", BitmapFont.class);
            } else if (skin.has("font", BitmapFont.class)) {
                font = skin.get("font", BitmapFont.class);
            } else {
                try {
                    font = new BitmapFont(Gdx.files.internal("ui/font.fnt"));
                } catch (Exception e) {
                    font = new BitmapFont();
                }
                skin.add("default-font", font);
            }
        } catch (Exception e) {
            font = new BitmapFont();
            skin.add("default-font", font);
        }

        if (!skin.has("default", Label.LabelStyle.class)) {
            Label.LabelStyle ls = new Label.LabelStyle();
            ls.font = font;
            ls.fontColor = Color.WHITE;
            skin.add("default", ls);
        }

        if (!skin.has("default", Window.WindowStyle.class)) {
            Window.WindowStyle ws = new Window.WindowStyle();
            ws.titleFont = font;
            ws.titleFontColor = Color.YELLOW;
            if (skin.has("white", TextureRegion.class)) {
                ws.background = skin.newDrawable("white", new Color(0.1f, 0.1f, 0.1f, 0.9f));
            }
            skin.add("default", ws);
        }

        if (!skin.has("default", TextButton.TextButtonStyle.class)) {
            TextButton.TextButtonStyle tbs = new TextButton.TextButtonStyle();
            tbs.font = font;
            tbs.fontColor = Color.WHITE;
            if (skin.has("white", TextureRegion.class)) {
                tbs.up = skin.newDrawable("white", new Color(0.4f, 0.4f, 0.4f, 1f));
                tbs.down = skin.newDrawable("white", new Color(0.2f, 0.2f, 0.2f, 1f));
                tbs.over = skin.newDrawable("white", new Color(0.5f, 0.5f, 0.5f, 1f));
            }
            skin.add("default", tbs);
        }
    }

    @Override
    public void setScreen(Screen screen) {
        super.setScreen(screen);
    }

    private void buildStoryPipeline() {
        PVAnimationCache.get("ani/pv/1/PV_1.atlas", "PV_1");
        PVAnimationCache.get("ani/pv/2/PV_2.atlas", "PV_2");
        PVAnimationCache.get("ani/pv/3/PV_3.atlas", "PV_3");
        storyPipeline = new PVPipeline(this, List.of(
                new PVNode("ani/pv/1/PV_1.atlas", "PV_1", AudioType.PV_1, IntroScreen.PVExit.NEXT_STAGE),
                new PVNode("ani/pv/2/PV_2.atlas", "PV_2", AudioType.PV_2, IntroScreen.PVExit.NEXT_STAGE),
                new PVNode("ani/pv/3/PV_3.atlas", "PV_3", AudioType.PV_3, IntroScreen.PVExit.NEXT_STAGE)
        ));

        storyPipeline.onFinished(() -> {
            stage = StoryStage.MAZE_GAME_TUTORIAL;
            AudioManager.getInstance().playMusic(AudioType.TUTORIAL_MAIN_BGM);
            setScreen(new MazeGameTutorialScreen(this, difficultyConfig));
        });
    }
    /**
     * Resets progress and starts the story from the beginning.
     */
    public void startStoryFromBeginning() {
        difficultyConfig = DifficultyConfig.of(Difficulty.NORMAL);
        gameManager = new GameManager(difficultyConfig, twoPlayerMode);
        stage = StoryStage.STORY_BEGIN;
        advanceStory();
    }

    public void startStoryWithLoading() {
        setScreen(new StoryLoadingScreen(this));
    }
    /**
     * Advances the story state machine to the next stage.
     *
     * Handles screen transitions and music changes.
     */
    public void advanceStory() {
        Logger.debug("advanceStory ENTER, stage = " + stage);
        switch (stage) {
            case STORY_BEGIN -> {
                buildStoryPipeline();
                storyPipeline.start();
            }
            case MAZE_GAME_TUTORIAL -> {
                stage = StoryStage.PV4;
                Animation<TextureRegion> pv4 = PVAnimationCache.get("ani/pv/4/PV_4.atlas", "PV_4");
                setScreen(new IntroScreen(this, pv4, IntroScreen.PVExit.PV4_CHOICE, AudioType.PV_4, null));
            }
            case PV4 -> {
                stage = StoryStage.MODE_MENU;
                AudioManager.getInstance().playMusic(AudioType.TUTORIAL_MAIN_BGM);
                setScreen(new ChapterSelectScreen(this));
            }
            case MODE_MENU -> {
                stage = StoryStage.MAZE_GAME;
                setScreen(new GameScreen(this, difficultyConfig));
            }
            default -> Logger.debug("advanceStory ignored at stage = " + stage);
        }
    }

    public void onTutorialFinished(MazeGameTutorialScreen tutorial) {
        if (stage == StoryStage.MAZE_GAME_TUTORIAL) {
            Gdx.app.postRunnable(this::advanceStory);
        }
    }

    public void onTutorialFailed(MazeGameTutorialScreen tutorial, MazeGameTutorialScreen.MazeGameTutorialResult result) {
        stage = StoryStage.MAIN_MENU;
        setScreen(new MenuScreen(this));
    }
    /**
     * Handles the player's decision after PV4.
     *
     * @param result selected option
     */
    public void onPV4Choice(PV4Result result) {
        if (stage != StoryStage.PV4) return;
        if (result == PV4Result.START) {
            saveProgress();
            stage = StoryStage.MODE_MENU;
            setScreen(new ChapterSelectScreen(this));
        } else {
            stage = StoryStage.MAIN_MENU;
            setScreen(new MenuScreen(this));
        }
    }

    public void goToMenu() {
        resetGameState();
        setScreen(new MenuScreen(this));
    }

    public void exitGame() {
        dispose();
        Gdx.app.exit();
        System.exit(0);
    }

    public void goToGame() {
        resetMaze(getCurrentDifficulty());
    }

    private void initializeSoundManager() {
        audioManager = AudioManager.getInstance();
        audioManager.setMasterVolume(1.0f);
        audioManager.setMusicVolume(0.6f);
        audioManager.setSfxVolume(0.8f);
        audioManager.setMusicEnabled(true);
        audioManager.setSfxEnabled(true);
        AudioConfig uiConfig = audioManager.getAudioConfig(AudioType.UI_CLICK);
        if (uiConfig != null) uiConfig.setPersistent(true);
    }

    public AudioManager getSoundManager() { return audioManager; }
    private void saveProgress() { Logger.debug("Progress saved (PV4)"); }
    private void resetGameState() { stage = StoryStage.MAIN_MENU; }
    /**
     * Returns the shared SpriteBatch used for rendering.
     *
     * @return sprite batch
     */
    public SpriteBatch getSpriteBatch() { return spriteBatch; }
    /**
     * Returns the UI skin.
     *
     * @return skin instance
     */
    public Skin getSkin() { return skin; }
    /**
     * Releases all allocated resources.
     *
     * Must be called when the application exits to avoid memory leaks.
     */
    @Override
    public void dispose() {
        if (spriteBatch != null) spriteBatch.dispose();
        if (skin != null) skin.dispose();
        if (audioManager != null) audioManager.dispose();
        assets.dispose();
        TextureManager.getInstance().dispose();
    }
    /**
     * Recreates the maze and restarts gameplay using the given difficulty.
     *
     * @param difficulty difficulty to apply
     */
    public void resetMaze(Difficulty difficulty) {
        this.currentDifficulty = difficulty;
        this.difficultyConfig = DifficultyConfig.of(difficulty);

        this.gameManager = new GameManager(this.difficultyConfig, this.twoPlayerMode);
        this.gameManager.markAsNewGame();

        Screen old = getScreen();

        if (difficulty == Difficulty.ENDLESS) {
            setScreen(new EndlessScreen(this, difficultyConfig));
        } else {
            setScreen(new GameScreen(this, difficultyConfig));
        }

        if (old != null) old.dispose();
    }

    /**
     * Loads the latest auto-save.
     *
     * Falls back to starting a new game if no save exists.
     */
    public void loadGame() {
        Logger.info("Loading game from save...");
        StorageManager storage = StorageManager.getInstance();
        GameSaveData saveData = storage.loadGame();

        if (saveData == null) {
            startNewGameFromMenu();
            return;
        }

        Difficulty savedDifficulty;
        try {
            savedDifficulty = Difficulty.valueOf(saveData.difficulty);
        } catch (Exception e) {
            savedDifficulty = Difficulty.NORMAL;
        }

        this.currentDifficulty = savedDifficulty;
        this.difficultyConfig = DifficultyConfig.of(savedDifficulty);
        this.setTwoPlayerMode(saveData.twoPlayerMode);
        this.gameManager = new GameManager(this.difficultyConfig, this.twoPlayerMode);
        this.gameManager.restoreFromSaveData(
                saveData,
                StorageManager.SaveTarget.AUTO
        );

        if (savedDifficulty == Difficulty.ENDLESS) {
            setScreen(new EndlessScreen(this, difficultyConfig));
        } else {
            setScreen(new GameScreen(this, difficultyConfig));
        }
    }
    /**
     * Starts a new game triggered from the main menu.
     */
    public void startNewGameFromMenu() {
        Logger.info("Starting new game from menu...");
        StorageManager storage = StorageManager.getInstance();

        Difficulty difficulty = this.currentDifficulty != null ? this.currentDifficulty : Difficulty.NORMAL;
        startNewGame(difficulty);
    }

    public void setGameManager(GameManager gm) { this.gameManager = gm; }

    public void debugEnterTutorial() {
        stage = StoryStage.MAZE_GAME_TUTORIAL;
        storyPipeline = null;
        difficultyConfig = DifficultyConfig.of(Difficulty.NORMAL);
        gameManager = new GameManager(difficultyConfig, twoPlayerMode);
        AssetManager am = getAssets();
        if (!am.isLoaded("ani/pv/4/PV_4.atlas")) {
            am.load("ani/pv/4/PV_4.atlas", TextureAtlas.class);
            am.finishLoadingAsset("ani/pv/4/PV_4.atlas");
        }
        setScreen(new MazeGameTutorialScreen(this, difficultyConfig));
    }

    public void restartCurrentGame() {
        if (!hasRunningGame()) return;
        Difficulty d = getCurrentDifficulty();
        resetMaze(d);
    }

    private boolean twoPlayerModeDirty = false;
    /**
     * Enables or disables two-player mode.
     *
     * Marks the mode as dirty so dependent systems can refresh.
     *
     * @param enabled true to enable multiplayer
     */
    public void setTwoPlayerMode(boolean enabled) {
        if (this.twoPlayerMode != enabled) {
            this.twoPlayerMode = enabled;
            this.twoPlayerModeDirty = true;
        }
    }
    /**
     * Consumes the dirty flag for two-player mode.
     *
     * @return true if the mode changed since last check
     */
    public boolean consumeTwoPlayerModeDirty() {
        boolean dirty = twoPlayerModeDirty;
        twoPlayerModeDirty = false;
        return dirty;
    }
    /**
     * Loads a game from a specific save slot.
     *
     * @param slot save slot index
     */
    public void loadGameFromSlot(int slot) {
        Logger.info("Loading game from slot " + slot);

        StorageManager storage = StorageManager.getInstance();
        GameSaveData saveData = storage.loadGameFromSlot(slot);

        if (saveData == null) {
            Logger.warning("Save slot " + slot + " is empty, starting new game.");
            startNewGameFromMenu();
            return;
        }

        Difficulty savedDifficulty;
        try {
            savedDifficulty = Difficulty.valueOf(saveData.difficulty);
        } catch (Exception e) {
            savedDifficulty = Difficulty.NORMAL;
        }

        this.currentDifficulty = savedDifficulty;
        this.difficultyConfig = DifficultyConfig.of(savedDifficulty);
        this.setTwoPlayerMode(saveData.twoPlayerMode);

        this.gameManager = new GameManager(this.difficultyConfig, this.twoPlayerMode);
        this.gameManager.restoreFromSaveData(
                saveData,
                StorageManager.SaveTarget.fromSlot(slot)
        );

        if (savedDifficulty == Difficulty.ENDLESS) {
            setScreen(new EndlessScreen(this, difficultyConfig));
        } else {
            setScreen(new GameScreen(this, difficultyConfig));
        }
    }



}