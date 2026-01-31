package de.tum.cit.fop.maze.tools;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;
import de.tum.cit.fop.maze.audio.AudioManager;
import de.tum.cit.fop.maze.audio.AudioType;
/**
 * Utility factory class for creating styled {@link TextButton} instances
 * with consistent animations, hover effects, and sound feedback.
 *
 * <p>This factory centralizes button creation logic to ensure uniform
 * visual and audio behavior across the UI, including hover scaling,
 * click feedback, and optional sound effects.
 *
 * <p>The behavior of generated buttons can be customized via animation
 * and sound parameters before creation.
 */
public class ButtonFactory {

    private final Skin skin;
    private final AudioManager audioManager;

    private float hoverDuration = 0.12f;
    private float clickDownDuration = 0.08f;
    private float clickUpDuration = 0.10f;
    private float hoverScale = 1.05f;
    private float clickScale = 0.95f;
    private float hoverBrightness = 1.08f;

    private boolean enableHoverSound = true;
    private boolean enableClickSound = true;
    private AudioType hoverSound = AudioType.UI_HIT_DAZZLE;
    private AudioType clickSound = AudioType.UI_CLICK;
    private AudioType successSound = AudioType.UI_SUCCESS;
    /**
     * Creates a new {@code ButtonFactory} using the given UI skin.
     *
     * @param skin the {@link Skin} used to style created buttons
     */
    public ButtonFactory(Skin skin) {
        this.skin = skin;
        this.audioManager = AudioManager.getInstance();
    }

    /**
     * Configures sound-related parameters for buttons created by this factory.
     *
     * @param enableHoverSound whether hover sound is enabled
     * @param enableClickSound whether click sound is enabled
     * @param hoverSound sound played on hover
     * @param clickSound sound played on click
     */
    public void setSoundParams(boolean enableHoverSound, boolean enableClickSound,
                               AudioType hoverSound, AudioType clickSound) {
        this.enableHoverSound = enableHoverSound;
        this.enableClickSound = enableClickSound;
        this.hoverSound = hoverSound;
        this.clickSound = clickSound;
    }
    /**
     * Configures animation parameters for hover and click feedback.
     *
     * @param hoverDuration duration of hover animation
     * @param clickDownDuration duration of click-down animation
     * @param clickUpDuration duration of click-up animation
     * @param hoverScale scale applied on hover
     * @param clickScale scale applied on click
     * @param hoverBrightness brightness multiplier on hover
     */
    public void setAnimationParams(float hoverDuration, float clickDownDuration,
                                   float clickUpDuration, float hoverScale,
                                   float clickScale, float hoverBrightness) {
        this.hoverDuration = hoverDuration;
        this.clickDownDuration = clickDownDuration;
        this.clickUpDuration = clickUpDuration;
        this.hoverScale = hoverScale;
        this.clickScale = clickScale;
        this.hoverBrightness = hoverBrightness;
    }

    /**
     * Creates a standard button with default sound and animation behavior.
     *
     * @param text button label text
     * @param onClick callback executed on valid click
     * @return newly created {@link TextButton}
     */
    public TextButton create(String text, Runnable onClick) {
        return create(text, onClick, true, true);
    }

    /**
     * Creates a button with customizable click and success sound behavior.
     *
     * @param text button label text
     * @param onClick callback executed on valid click
     * @param playClickSound whether click sound is played
     * @param playSuccessSound whether success sound is played after click
     * @return newly created {@link TextButton}
     */
    public TextButton create(String text, Runnable onClick,
                             boolean playClickSound, boolean playSuccessSound) {


        TextButton button = new TextButton(text, skin, "navTextButton");
        button.getLabel().setFontScale(0.7f);//全局更改字体大小
        button.pad(18f, 60f, 18f, 60f);

        button.setTransform(true);
        button.setOrigin(Align.center);

        final boolean[] isOver = {false};
        final boolean[] isPressed = {false};

        button.addListener(new InputListener() {

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                isOver[0] = true;

                if (enableHoverSound && pointer == -1) {
                    audioManager.playSound(hoverSound.name(), 0.6f);
                }

                if (!isPressed[0]) {
                    resetToBase(button); // ✅ 关键
                    button.setScale(hoverScale);
                    button.setColor(
                            hoverBrightness,
                            hoverBrightness,
                            hoverBrightness,
                            1f
                    );
                }
            }


            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                isOver[0] = false;

                if (!isPressed[0]) {
                    performNormalAnimation(button);
                }
            }

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int buttonCode) {
                isPressed[0] = true;

                if (enableClickSound && playClickSound) {
                    audioManager.playUIClick();
                }

                resetToBase(button);

                button.setScale(clickScale);
                button.setColor(0.9f, 0.9f, 0.9f, 1f);

                return true;
            }


            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int buttonCode) {
                boolean wasPressed = isPressed[0];
                isPressed[0] = false;

                boolean isValidClick = wasPressed &&
                        x >= 0 && x <= button.getWidth() &&
                        y >= 0 && y <= button.getHeight();

                resetToBase(button);

                if (isOver[0]) {
                    button.setScale(hoverScale);
                    button.setColor(
                            hoverBrightness,
                            hoverBrightness,
                            hoverBrightness,
                            1f
                    );
                }

                if (onClick != null && isValidClick) {
                    if (playSuccessSound) {
                        audioManager.playSound(successSound.name(), 0.8f);
                    }
                    onClick.run();
                }
            }

        });

        return button;
    }
    private void resetToBase(TextButton button) {
        button.clearActions();
        button.setScale(1f);
        button.setColor(Color.WHITE);
    }










    private void performHoverAnimation(TextButton button) {
        button.clearActions();
        button.addAction(
                Actions.parallel(
                        Actions.scaleTo(hoverScale, hoverScale, hoverDuration),
                        Actions.color(new Color(hoverBrightness, hoverBrightness, hoverBrightness, 1f), hoverDuration)
                )
        );
    }

    private void performNormalAnimation(TextButton button) {
        button.clearActions();
        button.addAction(
                Actions.parallel(
                        Actions.scaleTo(1f, 1f, hoverDuration),
                        Actions.color(Color.WHITE, hoverDuration)
                )
        );
    }

    private void performClickDownAnimation(TextButton button) {
        button.clearActions();
        button.addAction(
                Actions.parallel(
                        Actions.scaleTo(clickScale, clickScale, clickDownDuration),
                        Actions.color(new Color(0.9f, 0.9f, 0.9f, 1f), clickDownDuration)
                )
        );
    }

    private void performClickUpToHoverAnimation(TextButton button) {
        button.clearActions();
        button.addAction(
                Actions.parallel(
                        Actions.scaleTo(hoverScale, hoverScale, clickUpDuration),
                        Actions.color(new Color(hoverBrightness, hoverBrightness, hoverBrightness, 1f), clickUpDuration)
                )
        );
    }

    private void performClickUpToNormalAnimation(TextButton button) {
        button.clearActions();
        button.addAction(
                Actions.parallel(
                        Actions.scaleTo(1f, 1f, clickUpDuration),
                        Actions.color(Color.WHITE, clickUpDuration)
                )
        );
    }

    /**
     * Creates a button with stronger click feedback and sound effects.
     *
     * @param text button label text
     * @param onClick callback executed on click
     * @return newly created {@link TextButton}
     */
    public TextButton createWithStrongFeedback(String text, Runnable onClick) {
        float originalClickScale = this.clickScale;
        float originalClickDownDuration = this.clickDownDuration;

        this.clickScale = 0.92f;
        this.clickDownDuration = 0.06f;

        AudioType originalClickSound = this.clickSound;
        this.clickSound = AudioType.UI_THROW_ATTACK;

        TextButton button = create(text, onClick);

        this.clickScale = originalClickScale;
        this.clickDownDuration = originalClickDownDuration;
        this.clickSound = originalClickSound;

        return button;
    }
    /**
     * Creates a button with a shake animation on click.
     *
     * @param text button label text
     * @param onClick callback executed on click
     * @return newly created {@link TextButton}
     */
    public TextButton createWithShakeEffect(String text, Runnable onClick) {
        TextButton button = create(text, onClick, true, true);

        button.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int buttonCode) {
                button.clearActions();
                button.addAction(
                        Actions.sequence(
                                Actions.parallel(
                                        Actions.scaleTo(clickScale, clickScale, clickDownDuration),
                                        Actions.color(new Color(0.9f, 0.9f, 0.9f, 1f), clickDownDuration),
                                        Actions.sequence(
                                                Actions.moveBy(-2, 0, 0.02f),
                                                Actions.moveBy(4, 0, 0.04f),
                                                Actions.moveBy(-2, 0, 0.02f)
                                        )
                                )
                        )
                );
                return true;
            }
        });

        return button;
    }

    /**
     * Creates a button without any sound feedback.
     *
     * @param text button label text
     * @param onClick callback executed on click
     * @return newly created {@link TextButton}
     */
    public TextButton createSilent(String text, Runnable onClick) {
        return create(text, onClick, false, false);
    }
    /**
     * Creates a navigation-style button with emphasized audio feedback.
     *
     * @param text button label text
     * @param onClick callback executed on click
     * @return newly created {@link TextButton}
     */
    public TextButton createNavigationButton(String text, Runnable onClick) {
        AudioType originalHoverSound = this.hoverSound;
        AudioType originalClickSound = this.clickSound;

        this.hoverSound = AudioType.UI_HIT_DAZZLE;
        this.clickSound = AudioType.UI_THROW_ATTACK;

        TextButton button = createWithStrongFeedback(text, onClick);

        this.hoverSound = originalHoverSound;
        this.clickSound = originalClickSound;

        return button;
    }
    /**
     * Creates an important button with enhanced visual and audio feedback.
     *
     * @param text button label text
     * @param onClick callback executed on click
     * @return newly created {@link TextButton}
     */
    public TextButton createImportantButton(String text, Runnable onClick) {
        audioManager.setSfxVolume(audioManager.getSfxVolume() * 1.2f); // 临时提高音量

        TextButton button = create(text, onClick, true, true);

        button.addListener(new InputListener() {
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int buttonCode) {
                if (x >= 0 && x <= button.getWidth() && y >= 0 && y <= button.getHeight()) {
                    button.addAction(Actions.sequence(
                            Actions.color(new Color(1.5f, 1.5f, 1.5f, 1f), 0.05f),
                            Actions.color(Color.WHITE, 0.1f)
                    ));
                }
            }
        });

        return button;
    }
}