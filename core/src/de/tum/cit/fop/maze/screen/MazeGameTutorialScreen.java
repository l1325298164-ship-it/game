package de.tum.cit.fop.maze.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import de.tum.cit.fop.maze.MazeRunnerGame;
import de.tum.cit.fop.maze.effects.portal.PortalEffectManager;
import de.tum.cit.fop.maze.game.DifficultyConfig;
import de.tum.cit.fop.maze.game.GameConstants;
import de.tum.cit.fop.maze.game.GameManager;
import de.tum.cit.fop.maze.entities.Player;

/**
 * Interactive tutorial screen teaching basic maze movement mechanics.
 *
 * <p>This screen guides the player through movement, sprinting,
 * and reaching an exit using a scripted tutorial maze.
 * The tutorial finishes with a {@link MazeGameTutorialResult}.
 */
public class MazeGameTutorialScreen implements Screen {


    private Viewport viewport;

    private Pixmap mazeMask;
    private Texture mazeTexture;
    private boolean debugShowMask = false;
    private Player tutorialPlayer;
    private float playerX;
    private float playerY;
    /**
     * Result of the maze tutorial session.
     */
    public enum MazeGameTutorialResult {
        SUCCESS,
        FAILURE_DEAD,
        EXIT_BY_PLAYER
    }

    private final MazeRunnerGame game;
    private final DifficultyConfig config;
    private GameManager gm;
    private float exitHintTimer = 0f;

    private OrthographicCamera hudCamera;
    private ShapeRenderer shapeRenderer;

    private static final float PLAYER_FOOT_OFFSET = 30f;
    private static final float PLAYER_BODY_OFFSET = 20f;
    private static final float PORTAL_Y_OFFSET = 30f;
    private static final float PLAYER_HALO_RADIUS = 26f;
    private static final float PLAYER_HALO_ALPHA_BASE = 0.38f;


    private boolean finished = false;
    private boolean movedUp, movedDown, movedLeft, movedRight, usedShift;
    private boolean reachedTarget = false;
    private static final float WALK_SPEED = 220f;   // 普通移动速度
    private static final float SPRINT_SPEED = 420f; // 冲刺速度
    private static final int MAZE_WIDTH = 30;
    private static final int MAZE_HEIGHT = 20;
    private static final float CELL_SIZE = 32f;
    private Texture goalTexture;
    private final GlyphLayout glyphLayout = new GlyphLayout();

    private float goalX = 1200f;
    private float goalY = 300f;
    private PortalEffectManager goalPortal;
    private float goalRadius = 40f;


    private final int targetX = 25;
    private final int targetY = 10;

    private boolean upPressed, downPressed, leftPressed, rightPressed;
    /**
     * Creates the maze gameplay tutorial screen.
     *
     * @param game the main game instance
     * @param config difficulty configuration used for tutorial setup
     */
    public MazeGameTutorialScreen(MazeRunnerGame game, DifficultyConfig config) {
        this.game = game;
        this.config = config;
        viewport = new ScreenViewport();
        viewport.apply(true);
    }

    @Override
    public void show() {
        viewport = new ScreenViewport();
        viewport.apply(true);

        hudCamera = new OrthographicCamera();
        hudCamera.setToOrtho(false,
                Gdx.graphics.getWidth(),
                Gdx.graphics.getHeight()
        );

        shapeRenderer = new ShapeRenderer();


        System.out.println("=== TUTORIAL START ===");
        objectives.clear();

        objectives.add(new ObjectiveItem("PRESS W"));
        objectives.add(new ObjectiveItem("PRESS S"));
        objectives.add(new ObjectiveItem("PRESS A"));
        objectives.add(new ObjectiveItem("PRESS D"));
        objectives.add(new ObjectiveItem("SHIFT Sprint"));
        objectives.add(new ObjectiveItem("🎯 Reach Exit"));






        mazeTexture = new Texture(
                Gdx.files.internal("story_file/tutorial_bg.png")
        );

        mazeMask = new Pixmap(
                Gdx.files.internal("story_file/tutorial_mask.png")
        );
        mazeMask = new Pixmap(Gdx.files.internal("story_file/tutorial_mask.png"));
        goalTexture = new Texture(Gdx.files.internal("story_file/goal_icon.png"));
        findSpawnByCode();

        gm = new GameManager(config, false);
        gm.setTutorialMode(true);

        tutorialPlayer = new Player(
                0,
                0,
                gm,
                Player.PlayerIndex.P1
        );
        tutorialPlayer.enableTutorialMode();
        tutorialPlayer.setPosition(0, 0);


        syncPlayerToEntity();


        
        goalPortal = new PortalEffectManager(PortalEffectManager.PortalOwner.DOOR);

        shapeRenderer = new ShapeRenderer();

        hudCamera = new OrthographicCamera();
        hudCamera.setToOrtho(false,
                Gdx.graphics.getWidth(),
                Gdx.graphics.getHeight()
        );



        System.out.println("Tutorial Objective: Use WASD or Arrow Keys to move, reach the green target");
        System.out.println("Player Start: (" + playerX + ", " + playerY + ")");
        System.out.println("Target Position: (" + targetX + ", " + targetY + ")");
        System.out.println("Game Stage: STORY_MAZE_GAME_TUTORIAL");
    }

    private void syncPlayerToEntity() {
        tutorialPlayer.setWorldPosition(
                playerX / GameConstants.CELL_SIZE,
                playerY / GameConstants.CELL_SIZE
        );
    }


    private void renderPlayerHalo(float delta) {
        float pulse = 1.0f + 0.08f * (float) Math.sin(exitHintTimer * 3.0f);

        float haloX = playerX + PLAYER_BODY_OFFSET;
        float haloY = playerY + PLAYER_FOOT_OFFSET;

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.setProjectionMatrix(hudCamera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        shapeRenderer.setColor(1.0f, 0.96f, 0.78f, PLAYER_HALO_ALPHA_BASE * 0.6f);
        shapeRenderer.circle(
                haloX,
                haloY,
                PLAYER_HALO_RADIUS * pulse
        );

        shapeRenderer.setColor(1.0f, 0.93f, 0.65f, PLAYER_HALO_ALPHA_BASE);
        shapeRenderer.circle(
                haloX,
                haloY,
                PLAYER_HALO_RADIUS * 0.65f * pulse
        );

        shapeRenderer.end();
    }

    /**
     * Updates and renders the tutorial gameplay each frame.
     *
     * @param delta time elapsed since last frame (in seconds)
     */
    @Override
    public void render(float delta) {
        viewport.apply();
        game.getSpriteBatch().setProjectionMatrix(
                viewport.getCamera().combined
        );

        handleInput();
        update(delta);

        debugShowMask = Gdx.input.isKeyPressed(Input.Keys.M);
        if (Gdx.input.isKeyPressed(Input.Keys.F)) {
            shapeRenderer.setProjectionMatrix(hudCamera.combined);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(Color.GREEN);
            shapeRenderer.circle(goalX, goalY, goalRadius);
            shapeRenderer.end();
        }

        game.getSpriteBatch().begin();
        game.getSpriteBatch().draw(
                mazeTexture,
                0,
                0,
                Gdx.graphics.getWidth(),
                Gdx.graphics.getHeight()
        );
        game.getSpriteBatch().end();

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.setProjectionMatrix(hudCamera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        shapeRenderer.setColor(0.05f, 0.08f, 0.12f, 0.48f);

        shapeRenderer.rect(
                0,
                0,
                Gdx.graphics.getWidth(),
                Gdx.graphics.getHeight()
        );

        shapeRenderer.end();


        renderMaskDebugOverlay();

        game.getSpriteBatch().begin();

        float size = 64f;
        game.getSpriteBatch().draw(
                goalTexture,
                goalX - size / 2f,
                goalY - size / 2f+PORTAL_Y_OFFSET,
                size,
                size
        );
        game.getSpriteBatch().end();
        game.getSpriteBatch().begin();

        Matrix4 oldMatrix = new Matrix4(game.getSpriteBatch().getTransformMatrix());

        Matrix4 scaled = new Matrix4(oldMatrix);
        scaled.scale(3f, 3f, 1f); // ⚠️ 不是 10，而是视觉 10
        game.getSpriteBatch().setTransformMatrix(scaled);
        float portalDrawX = goalX;
        float portalDrawY = goalY + PORTAL_Y_OFFSET;
        goalPortal.renderBack(
                game.getSpriteBatch(),
                portalDrawX / 3.2f,
                portalDrawY / 3.2f
        );

        game.getSpriteBatch().setTransformMatrix(oldMatrix);
        game.getSpriteBatch().end();


        renderPlayerHalo(delta);


        shapeRenderer.setProjectionMatrix(hudCamera.combined);
        game.getSpriteBatch().begin();

        tutorialPlayer.drawSprite(game.getSpriteBatch());

        game.getSpriteBatch().end();

        game.getSpriteBatch().begin();
        goalPortal.renderFront(game.getSpriteBatch());
        game.getSpriteBatch().end();

        game.getSpriteBatch().begin();

        var font = game.getSkin().getFont("default-font");

        float floatOffset = (float) Math.sin(exitHintTimer * 2.0f) * 6f;

        float textX = goalX;
        float textY = goalY + PORTAL_Y_OFFSET - 50f + floatOffset;

        String text = "EXIT HERE";

        glyphLayout.setText(font, text);
        float textW = glyphLayout.width;

        font.setColor(0.6f, 0.85f, 1.0f, 0.18f); // 淡蓝白光

        float glowRadius = 3f; // 光晕扩散半径
        for (int i = 0; i < 6; i++) {
            float angle = i * 60f * MathUtils.degreesToRadians;
            float ox = MathUtils.cos(angle) * glowRadius;
            float oy = MathUtils.sin(angle) * glowRadius;

            font.draw(
                    game.getSpriteBatch(),
                    text,
                    textX - textW / 2f + ox,
                    textY + oy
            );
        }

        font.setColor(0.9f, 0.97f, 1.0f, 1.0f);

        font.draw(
                game.getSpriteBatch(),
                text,
                textX - textW / 2f,
                textY
        );

// 还原
        font.setColor(Color.WHITE);
        game.getSpriteBatch().end();




        renderHUD();

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            finishTutorial(MazeGameTutorialResult.SUCCESS);
        }
    }
    private boolean reachedGoal() {
        float dx = playerX - goalX;
        float dy = playerY - goalY;
        return dx * dx + dy * dy <= goalRadius * goalRadius;
    }

    private void renderMaskDebugOverlay() {
        if (!debugShowMask) return;

        shapeRenderer.setProjectionMatrix(hudCamera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        int screenW = Gdx.graphics.getWidth();
        int screenH = Gdx.graphics.getHeight();

        int maskW = mazeMask.getWidth();
        int maskH = mazeMask.getHeight();

        float cellW = (float) screenW / maskW;
        float cellH = (float) screenH / maskH;

        Color c = new Color();

        for (int y = 0; y < maskH; y++) {
            for (int x = 0; x < maskW; x++) {

                Color.rgba8888ToColor(c, mazeMask.getPixel(x, y));

                boolean walkable =
                        c.a > 0.5f &&
                                c.r > 0.9f &&
                                c.g > 0.9f &&
                                c.b > 0.9f;

                if (walkable) {
                    shapeRenderer.setColor(0f, 1f, 0f, 0.25f);
                } else {
                    shapeRenderer.setColor(1f, 0f, 0f, 0.25f);
                }

                shapeRenderer.rect(
                        x * cellW,
                        (maskH - 1 - y) * cellH,
                        cellW,
                        cellH
                );

            }
        }

        shapeRenderer.end();
    }

    private int screenToMaskX(float screenX) {
        return (int) (screenX / Gdx.graphics.getWidth()
                * mazeMask.getWidth());
    }

    private int screenToMaskY(float screenY) {
        int maskY = (int) (screenY / Gdx.graphics.getHeight()
                * mazeMask.getHeight());

        return mazeMask.getHeight() - 1 - maskY;
    }



    private boolean canWalk(float screenX, float screenY) {
        int x = screenToMaskX(screenX);
        int y = screenToMaskY(screenY);

        if (x < 0 || y < 0
                || x >= mazeMask.getWidth()
                || y >= mazeMask.getHeight()) {
            return false;
        }

        Color c = new Color();
        Color.rgba8888ToColor(c, mazeMask.getPixel(x, y));

        return c.a > 0.5f && c.r > 0.9f && c.g > 0.9f && c.b > 0.9f;
    }

    private boolean exitTriggered = false;



    private void handleInput() {
        upPressed = Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP);
        downPressed = Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN);
        leftPressed = Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT);
        rightPressed = Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT);
    }
    private float getPortalDrawX() {
        return goalX;
    }

    private float getPortalDrawY() {
        return goalY + PORTAL_Y_OFFSET;
    }
    private void update(float delta) {
        if (finished) return;

        if (upPressed) movedUp = true;
        if (downPressed) movedDown = true;
        if (leftPressed) movedLeft = true;
        if (rightPressed) movedRight = true;
        int dx = 0;
        int dy = 0;

        if (upPressed) dy = 1;
        else if (downPressed) dy = -1;
        else if (leftPressed) dx = -1;
        else if (rightPressed) dx = 1;
        if (dx != 0 || dy != 0) {
            tutorialPlayer.updateDirection(dx, dy);
            tutorialPlayer.setMovingAnim(true);
        } else {
            tutorialPlayer.setMovingAnim(false);
        }


        boolean isSprinting = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT);
        if (isSprinting && (upPressed || downPressed || leftPressed || rightPressed)) {
            usedShift = true;
        }
        markObjectiveDone("Move Up", movedUp);
        markObjectiveDone("Move Down", movedDown);
        markObjectiveDone("Move Left", movedLeft);
        markObjectiveDone("Move Right", movedRight);
        markObjectiveDone("SHIFT Sprint", usedShift);
        markObjectiveDone("🎯 Reach Exit", reachedGoal());
        for (int i = objectives.size - 1; i >= 0; i--) {
            ObjectiveItem obj = objectives.get(i);

            if (obj.completed && !obj.removing) {
                obj.completedTime += delta;
                if (obj.completedTime >= 1.0f) {
                    obj.removing = true;
                    obj.slide = 0f;
                }
            }

            if (obj.removing) {
                obj.slide += delta * 2.0f;
                if (obj.slide >= 1f) {
                    objectives.removeIndex(i);
                }
            }
        }

        float speed = (isSprinting ? SPRINT_SPEED : WALK_SPEED) * delta;

        float nextX = playerX + dx * speed;
        float nextY = playerY + dy * speed;
        float footY = nextY + PLAYER_FOOT_OFFSET;
        float footX = nextX + PLAYER_BODY_OFFSET;

        if (canWalk(footX, footY)) {
            playerX = nextX;
            playerY = nextY;
        }

        syncPlayerToEntity();
        tutorialPlayer.update(delta);

        goalPortal.setCenter(
                getPortalDrawX(),
                getPortalDrawY()
        );
        goalPortal.update(delta);

        boolean movementTasksDone =
                movedUp && movedDown && movedLeft && movedRight && usedShift;

        boolean onGoal = reachedGoal();
        reachedTarget = onGoal;

        if (!exitTriggered && movementTasksDone && onGoal) {
            exitTriggered = true;
            goalPortal.startExitAnimation(goalX, goalY);
        }


        if (exitTriggered && goalPortal.isFinished()) {
            finishTutorial(MazeGameTutorialResult.SUCCESS);
        }


        exitHintTimer += delta;
    }

    private void markObjectiveDone(String text, boolean condition) {
        if (!condition) return;

        for (ObjectiveItem obj : objectives) {
            if (obj.text.equals(text) && !obj.completed) {
                obj.completed = true;
                obj.completedTime = 0f; // 🔥 开始计时
                break;
            }
        }
    }


    private void findSpawnByCode() {
        int maskW = mazeMask.getWidth();
        int maskH = mazeMask.getHeight();

        for (int y = 0; y < maskH; y++) {
            for (int x = 0; x < maskW; x++) {

                Color c = new Color();
                Color.rgba8888ToColor(c, mazeMask.getPixel(x, y));

                if (c.a > 0.5f && c.r > 0.9f && c.g > 0.9f && c.b > 0.9f) {

                    float footScreenX = (float) x / maskW * Gdx.graphics.getWidth();
                    float footScreenY = (float) (maskH - 1 - y)
                            / maskH * Gdx.graphics.getHeight();

                    playerX = footScreenX - PLAYER_BODY_OFFSET;
                    playerY = footScreenY - PLAYER_FOOT_OFFSET;

                    return;
                }
            }
        }

        playerX = Gdx.graphics.getWidth() / 2f + PLAYER_BODY_OFFSET;
        playerY = Gdx.graphics.getHeight() / 2f + PLAYER_FOOT_OFFSET;
    }




    private void renderHUD() {
        shapeRenderer.setProjectionMatrix(hudCamera.combined);

        float startX = 40f;
        float startY = Gdx.graphics.getHeight() - 80f;
        float spacing = 70f;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        game.getSpriteBatch().begin();

        for (int i = 0; i < objectives.size; i++) {
            ObjectiveItem obj = objectives.get(i);

            float y = startY - i * spacing;

            float slideX = obj.removing ? -300f * obj.slide : 0f;
            float alpha = obj.removing ? (1f - obj.slide) : 1f;


            var font = game.getSkin().getFont("default-font");
            font.getData().setScale(0.8f);
            if (obj.completed) {
                font.setColor(0.4f, 0.85f, 1.0f, alpha); // 🔥 高亮文字
            } else {
                font.setColor(1f, 1f, 1f, alpha);
            }

            font.draw(
                    game.getSpriteBatch(),
                    obj.text,
                    startX  + slideX,
                    y + 18f
            );
            font.setColor(Color.WHITE);
            font.getData().setScale(1.0f);
        }

        game.getSpriteBatch().end();
        shapeRenderer.end();


    }




    private void finishTutorial(MazeGameTutorialResult result) {
        if (finished) return;

        finished = true;

        System.out.println("=== TUTORIAL END ===");
        System.out.println("Result: " + result);
        System.out.println("Calling game.onTutorialFinished/onTutorialFailed");

        Gdx.app.postRunnable(() -> {
            try {
                if (result == MazeGameTutorialResult.SUCCESS) {
                    System.out.println("Calling game.onTutorialFinished()");
                    game.onTutorialFinished(this);
                } else {
                    System.out.println("Calling game.onTutorialFailed()");
                    game.onTutorialFailed(this, result);
                }
            } catch (Exception e) {
                System.err.println("Tutorial callback error: " + e.getMessage());
                e.printStackTrace();
                game.goToMenu();
            }
        });
    }

    private static class ObjectiveItem {
        String text;
        boolean completed = false;

        float slide = 0f;
        float completedTime = 0f;
        boolean removing = false;

        ObjectiveItem(String text) {
            this.text = text;
        }
    }
    private Array<ObjectiveItem> objectives = new Array<>();



    @Override
    public void resize(int width, int height) {
        if (viewport != null) {
            viewport.update(width, height, true);
        }
        if (hudCamera != null) {
            hudCamera.setToOrtho(false, width, height);
            hudCamera.update();
        }
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {
        System.out.println("Tutorial screen hidden");
        if (gm != null) {
            gm.setTutorialMode(false);
        }
    }

    @Override
    public void dispose() {
        System.out.println("Tutorial screen resources disposed");

        if (mazeMask != null) mazeMask.dispose();
        if (mazeTexture != null) mazeTexture.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
    }
}