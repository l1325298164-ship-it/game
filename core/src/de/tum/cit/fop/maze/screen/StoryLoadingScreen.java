package de.tum.cit.fop.maze.screen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.TimeUtils;
import de.tum.cit.fop.maze.MazeRunnerGame;
import de.tum.cit.fop.maze.utils.TextureManager;
/**
 * Loading screen displayed before the story begins.
 *
 * <p>This screen loads required assets, displays a loading progress bar
 * with animated visuals, and automatically starts the story once
 * all assets are loaded.
 */
public class StoryLoadingScreen implements Screen {
    private TextureRegion starRegion;
    private final MazeRunnerGame game;
    private final AssetManager assets;

    private SpriteBatch batch;
    private BitmapFont font;

    private FrameBuffer blurFbo;

    private Animation<TextureRegion> catAnim;
    private float stateTime = 0f;
    private final String CAT_ATLAS_PATH = "ani/cat/right/cat_right.atlas"; // 确认你的文件名
    private boolean catInitialized = false;
    private com.badlogic.gdx.utils.Array<Sparkle> sparkles = new com.badlogic.gdx.utils.Array<>();
    private float sparkleTimer = 0;
    private long showTime;
    private boolean storyStarted = false;
    /**
     * Creates the story loading screen.
     *
     * @param game the main game instance providing assets and rendering context
     */
    public StoryLoadingScreen(MazeRunnerGame game) {
        this.game = game;
        this.assets = game.getAssets();
    }

    @Override
    public void show() {
        batch = game.getSpriteBatch();
        resetProjection(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        Skin skin = game.getSkin();
        font = skin.getFont("default-font");

        Gdx.input.setInputProcessor(null);
        showTime = TimeUtils.millis();

        createBlurFbo(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        if (!assets.isLoaded(CAT_ATLAS_PATH, TextureAtlas.class)) {
            assets.load(CAT_ATLAS_PATH, TextureAtlas.class);
        }
        assets.load("effects/sparkle.atlas", TextureAtlas.class);
        assets.finishLoadingAsset("effects/sparkle.atlas");

        TextureAtlas atlas = assets.get("effects/sparkle.atlas", TextureAtlas.class);
        starRegion = atlas.findRegion("sparkle");
        queuePV("ani/pv/1/PV_1.atlas");
        queuePV("ani/pv/2/PV_2.atlas");
        queuePV("ani/pv/3/PV_3.atlas");
        queuePV("ani/pv/4/PV_4.atlas");
    }

    @Override
    public void render(float delta) {
        assets.update();
        stateTime += delta;

        if (!catInitialized && assets.isLoaded(CAT_ATLAS_PATH, TextureAtlas.class)) {
            TextureAtlas atlas = assets.get(CAT_ATLAS_PATH, TextureAtlas.class);

            com.badlogic.gdx.utils.Array<TextureAtlas.AtlasRegion> catFrames = new com.badlogic.gdx.utils.Array<>();

            for (TextureAtlas.AtlasRegion region : atlas.getRegions()) {
                if (region.name.startsWith("cat_right")) {
                    catFrames.add(region);
                }
            }

            if (catFrames.size > 0) {
                catFrames.sort((o1, o2) -> o1.name.compareTo(o2.name));

                catAnim = new Animation<>(0.1f, catFrames, Animation.PlayMode.LOOP);
                catInitialized = true;
                System.out.println("✅ [Animation]  " + catFrames.size);
            } else {
                System.err.println("❌ [Error]  frame...");
            }
        }

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        float progress = assets.getProgress();
        batch.begin();
        float barWidth = Gdx.graphics.getWidth() * 0.9f;
        float barHeight = 15f;
        float barX = (Gdx.graphics.getWidth() - barWidth) / 2f;
        float barY = Gdx.graphics.getHeight() * 0.18f;

        TextureRegion white;
        try {
            white = game.getSkin().getRegion("white");
        } catch (Exception ignored) {
            white = new TextureRegion(TextureManager.getInstance().getWhitePixel());
        }

        batch.setColor(0.2f, 0.2f, 0.2f, 0.5f);
        batch.draw(white, barX, barY, barWidth, barHeight);

        batch.setColor(255f/255f, 182f/255f, 193f/255f, 1f);
        float fillWidth = barWidth * progress;
        if (fillWidth > 0) {
            batch.draw(white, barX, barY, fillWidth, barHeight);
        }


        batch.setColor(Color.WHITE);


        if (catInitialized) {
            TextureRegion currentFrame = catAnim.getKeyFrame(stateTime);
            float catSize = 250f;
            float catX = -catSize + (Gdx.graphics.getWidth() + catSize) * progress;
            float catY = Gdx.graphics.getHeight() * 0.2f;

            sparkleTimer += delta;
            if (sparkleTimer > 0.05f) { // 每 0.1 秒生成一颗新星星
                Color sparkleColor = Math.random() > 0.5 ? Color.WHITE : new Color(1f, 182f/255f, 193f/255f, 1f);
                sparkles.add(new Sparkle(catX + catSize/2, catY + catSize/2, sparkleColor));
                sparkleTimer = 0;
            }
            batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);


            for (int i = sparkles.size - 1; i >= 0; i--) {
                Sparkle s = sparkles.get(i);
                s.update(delta);
                if (s.life <= 0) {
                    sparkles.removeIndex(i);
                    continue;
                }

                float alpha = s.life / s.maxLife;
                batch.setColor(s.color.r, s.color.g, s.color.b, alpha);

                if (starRegion != null) {
                    batch.draw(starRegion, s.x, s.y, s.size / 2f, s.size / 2f, s.size, s.size, 1f, 1f, stateTime * 100);
                }
            }

            batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            batch.setColor(Color.WHITE);

            batch.draw(currentFrame, catX, catY, catSize, catSize);
            for (int i = sparkles.size -1; i >= 0; i--) {
                Sparkle s = sparkles.get(i);
                s.update(delta);
                if (s.life <= 0) {
                    sparkles.removeIndex(i);
                    continue;
                }

                float alpha = s.life / s.maxLife;
                batch.setColor(s.color.r, s.color.g, s.color.b, alpha);

                if (starRegion != null) {
                    batch.draw(starRegion, s.x, s.y, s.size / 2f, s.size / 2f, s.size, s.size, 1f, 1f, stateTime * 100);
                }
            }
            batch.setColor(Color.WHITE);


        }
        font.draw(batch, "LOADING " + (int)(progress * 100) + "%", 0, Gdx.graphics.getHeight() * 0.15f, Gdx.graphics.getWidth(), Align.center, false);

        batch.end();

        if (!storyStarted && assets.isFinished() && TimeUtils.timeSinceMillis(showTime) > 2000) {
            storyStarted = true;
            System.out.println("✨ All assets loaded, starting story...");
            game.startStoryFromBeginning();
        }
    }

    private void resetProjection(int width, int height) {
        OrthographicCamera cam = new OrthographicCamera(width, height);
        cam.position.set(width / 2f, height / 2f, 0);
        cam.update();
        batch.setProjectionMatrix(cam.combined);
    }

    private void createBlurFbo(int width, int height) {
        if (blurFbo != null) blurFbo.dispose();
        blurFbo = new FrameBuffer(Pixmap.Format.RGBA8888, width / 4, height / 4, false);
    }

    private void queuePV(String path) {
        if (!assets.isLoaded(path, TextureAtlas.class)) {
            assets.load(path, TextureAtlas.class);
        }
    }

    @Override public void resize(int width, int height) { resetProjection(width, height); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() { if (blurFbo != null) blurFbo.dispose(); }



    private class Sparkle {
        float x, y, size, life, maxLife;
        Color color;

        Sparkle(float x, float y, Color color) {
            this.x = x + (float)(Math.random() * 100 - 80);
            this.y = y + (float)(Math.random() * 100 - 50);

            this.size = (float)(Math.random() * 20 +2);

            this.maxLife = (float)(Math.random() * 0.5f + 0.8f);
            this.life = maxLife;
            this.color = color;
        }

        void update(float delta) { life -= delta; }
    }
}