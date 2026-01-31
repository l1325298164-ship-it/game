package de.tum.cit.fop.maze.game.achievement;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import de.tum.cit.fop.maze.utils.TextureManager;

/**
 * Displays a temporary on-screen popup when an achievement is unlocked.
 *
 * <p>This popup animates into view, remains visible for a short duration,
 * and then animates out automatically. It is designed to be rendered
 * on top of the game UI using a {@link SpriteBatch}.
 */
public class AchievementPopup {

    private enum State {
        HIDDEN,
        ENTERING,
        VISIBLE,
        EXITING
    }

    private State state = State.HIDDEN;
    private AchievementType currentAchievement;

    private float timer = 0f;
    private static final float ANIM_IN_DURATION = 0.6f;
    private static final float DISPLAY_DURATION = 2.5f;
    private static final float ANIM_OUT_DURATION = 0.4f;

    private static final float POPUP_WIDTH = 360f;
    private static final float POPUP_HEIGHT = 80f;
    private static final float MARGIN_TOP = 80f;
    private final BitmapFont font;
    private final TextureRegion whitePixel;
    /**
     * Creates an achievement popup renderer.
     *
     * @param font the font used to render the popup text
     */
    public AchievementPopup(BitmapFont font) {
        this.font = font;
        this.whitePixel = new TextureRegion(TextureManager.getInstance().getWhitePixel());
    }

    /**
     * Displays a new achievement popup.
     *
     * <p>If another popup is currently visible, it will be replaced
     * by the new achievement.
     *
     * @param achievement the achievement to display
     */
    public void show(AchievementType achievement) {
        this.currentAchievement = achievement;
        this.state = State.ENTERING;
        this.timer = 0f;
    }

    /**
     * Renders the achievement popup and updates its animation state.
     *
     * <p>This method should be called every frame while the popup
     * is active. Rendering is skipped automatically when the popup
     * is hidden.
     *
     * @param batch the {@link SpriteBatch} used for rendering
     */
    public void render(SpriteBatch batch) {
        if (state == State.HIDDEN || currentAchievement == null) return;

        float delta = Gdx.graphics.getDeltaTime();
        updateAnimation(delta);
        if (state == State.HIDDEN || currentAchievement == null) return;
        float screenW = Gdx.graphics.getWidth();
        float screenH = Gdx.graphics.getHeight();

        float animProgress = 0f;
        float alpha = 1f;
        float yOffset = 0f;

        switch (state) {
            case ENTERING:
                animProgress = Math.min(1f, timer / ANIM_IN_DURATION);
                yOffset = Interpolation.swingOut.apply(-100f, 0f, animProgress);
                alpha = animProgress;
                break;
            case VISIBLE:
                yOffset = 0f;
                alpha = 1f;
                break;
            case EXITING:
                animProgress = Math.min(1f, timer / ANIM_OUT_DURATION);
                yOffset = Interpolation.pow2In.apply(0f, 100f, animProgress);
                alpha = 1f - animProgress;
                break;
        }

        float drawX = (screenW - POPUP_WIDTH) / 2f;
        float drawY = screenH - MARGIN_TOP - POPUP_HEIGHT + yOffset;

        batch.setColor(0.1f, 0.1f, 0.12f, 0.9f * alpha);
        batch.draw(whitePixel, drawX, drawY, POPUP_WIDTH, POPUP_HEIGHT);

        batch.setColor(1f, 0.8f, 0.0f, 1f * alpha);
        batch.draw(whitePixel, drawX, drawY, 6f, POPUP_HEIGHT);

        float iconCenterX = drawX + 40f;
        float iconCenterY = drawY + POPUP_HEIGHT / 2f + 10f;

        font.getData().setScale(2.5f);
        font.setColor(1f, 0.84f, 0f, alpha);
        if (state == State.VISIBLE) {
            float shake = (float)Math.sin(timer * 5f) * 2f;
            font.draw(batch, "★", iconCenterX - 10, iconCenterY + shake);
        } else {
            font.draw(batch, "★", iconCenterX - 10, iconCenterY);
        }

        float textX = drawX + 70f;
        float textTopY = drawY + POPUP_HEIGHT - 10f;

        font.getData().setScale(0.7f);
        font.setColor(1f, 0.8f, 0.2f, 0.8f * alpha);
        font.draw(batch, "ACHIEVEMENT UNLOCKED", textX, textTopY);

        font.getData().setScale(1.1f);
        font.setColor(1f, 1f, 1f, 1f * alpha);
        font.draw(batch, currentAchievement.displayName, textX, textTopY - 20f);

        font.getData().setScale(0.75f);
        font.setColor(0.8f, 0.8f, 0.8f, 0.8f * alpha);

        String desc = currentAchievement.description;
        if (desc.length() > 35) desc = desc.substring(0, 32) + "...";
        font.draw(batch, desc, textX, textTopY - 45f);

        batch.setColor(Color.WHITE);
        font.getData().setScale(1.2f);
        font.setColor(Color.WHITE);
    }

    private void updateAnimation(float delta) {
        timer += delta;
        switch (state) {
            case ENTERING:
                if (timer >= ANIM_IN_DURATION) {
                    state = State.VISIBLE;
                    timer = 0f;
                }
                break;
            case VISIBLE:
                if (timer >= DISPLAY_DURATION) {
                    state = State.EXITING;
                    timer = 0f;
                }
                break;
            case EXITING:
                if (timer >= ANIM_OUT_DURATION) {
                    state = State.HIDDEN;
                    currentAchievement = null;
                }
                break;
        }
    }
    /**
     * Checks whether the popup is currently active.
     *
     * @return {@code true} if the popup is visible or animating,
     *         {@code false} if it is hidden
     */
    public boolean isBusy() {
        return state != State.HIDDEN;
    }
}