package de.tum.cit.fop.maze.entities.boss;

import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Align;
/**
 * Dialog shown when the boss encounter is initiated.
 * <p>
 * Allows the player to either start the boss fight
 * or escape the encounter.
 */
public class BossFoundDialog extends Dialog {

    private Runnable onFight;
    private Runnable onEscape;
    /**
     * Creates a boss encounter dialog.
     *
     * @param skin UI skin used to style the dialog
     */
    public BossFoundDialog(Skin skin) {
        super("", skin);

        setModal(true);
        setMovable(false);

        getTitleLabel().setVisible(false);
        getTitleTable().clear();

        getContentTable().pad(40);

        Label label = new Label("Boss finds you.", skin);
        label.setAlignment(Align.center);

        getContentTable().add(label).width(420);

        getButtonTable().padTop(20).padBottom(25);

        button("READY TO COMBAT", true);
        button("RUN AWAY", false);

        pack();
    }
    /**
     * Handles the dialog result and invokes the corresponding callback.
     *
     * @param object result object indicating the selected option
     */
    @Override
    protected void result(Object object) {
        boolean fight = (Boolean) object;
        hide();

        if (fight) {
            if (onFight != null) onFight.run();
        } else {
            if (onEscape != null) onEscape.run();
        }
    }
    /**
     * Sets the callback executed when the player chooses to fight.
     *
     * @param onFight action executed on fight selection
     */
    public void setOnFight(Runnable onFight) {
        this.onFight = onFight;
    }
    /**
     * Sets the callback executed when the player chooses to escape.
     *
     * @param onEscape action executed on escape selection
     */
    public void setOnEscape(Runnable onEscape) {
        this.onEscape = onEscape;
    }
}


