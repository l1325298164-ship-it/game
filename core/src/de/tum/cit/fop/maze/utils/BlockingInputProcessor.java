package de.tum.cit.fop.maze.utils;

import com.badlogic.gdx.InputProcessor;
/**
 * An {@link InputProcessor} implementation that blocks almost all input events.
 *
 * <p>This processor consumes keyboard, mouse, and touch input by always
 * returning {@code true}, preventing input from being passed to other
 * processors in the input chain.
 *
 * <p>It is typically used to temporarily disable player input, for example
 * during cutscenes, menus, loading screens, or modal dialogs.
 */
public class BlockingInputProcessor implements InputProcessor {

    @Override public boolean keyDown(int keycode) { return true; }
    @Override public boolean keyUp(int keycode) { return true; }
    @Override public boolean keyTyped(char character) { return true; }

    @Override public boolean touchDown(int screenX, int screenY, int pointer, int button) { return true; }
    @Override public boolean touchUp(int screenX, int screenY, int pointer, int button) { return true; }

    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override public boolean touchDragged(int screenX, int screenY, int pointer) { return true; }
    @Override public boolean mouseMoved(int screenX, int screenY) { return true; }
    @Override public boolean scrolled(float amountX, float amountY) { return true; }
}
