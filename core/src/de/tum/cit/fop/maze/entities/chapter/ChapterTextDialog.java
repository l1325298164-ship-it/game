package de.tum.cit.fop.maze.entities.chapter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import de.tum.cit.fop.maze.tools.ButtonFactory;
/**
 * Dialog component for displaying chapter relic text content.
 * <p>
 * This dialog presents multi-line story text and allows the
 * player to either read or discard the relic, notifying the
 * caller through a {@link ChapterDialogCallback}.
 */
public class ChapterTextDialog extends Group {

    private final Stage stage;
    private final Skin skin;
    private final ChapterDialogCallback callback;
    private final RelicData data;
    /**
     * Creates and displays a chapter text dialog.
     * <p>
     * The dialog is added directly to the given stage and
     * blocks interaction until a choice is made.
     *
     * @param stage    stage used to display the dialog
     * @param skin     UI skin for styling
     * @param data     relic data containing text content
     * @param callback callback invoked on player choice
     */
    public ChapterTextDialog(
            Stage stage,
            Skin skin,
            RelicData data,
            ChapterDialogCallback callback
    ) {
        this.stage = stage;
        this.skin = skin;
        this.data = data;
        this.callback = callback;

        setSize(stage.getWidth(), stage.getHeight());
        setPosition(0, 0);

        createDialog();

        stage.addActor(this);
    }



    private void createDialog() {

        float dialogWidth  = stage.getWidth()  * 0.75f;
        float dialogHeight = stage.getHeight() * 0.75f;

        Table root = new Table();
        root.setSize(dialogWidth, dialogHeight);
        root.setPosition(
                (getWidth() - dialogWidth) / 2f,
                (getHeight() - dialogHeight) / 2f
        );

        if (data.background != null && Gdx.files.internal(data.background).exists()) {
            Texture bg = new Texture(Gdx.files.internal(data.background));
            root.setBackground(new TextureRegionDrawable(bg));
        }

        addActor(root);

        BitmapFont defaultFont = new BitmapFont();
        defaultFont.getData().setScale(2.4f);

        Label.LabelStyle textStyle = new Label.LabelStyle();
        textStyle.font = defaultFont;
        textStyle.fontColor = Color.BLACK;

        float textWidth = dialogWidth * 0.8f;

        Label contentLabel = new Label(buildContentText(), textStyle);
        contentLabel.setWrap(true);
        contentLabel.setAlignment(Align.center);
        contentLabel.setWidth(textWidth);

        ScrollPane.ScrollPaneStyle scrollStyle = new ScrollPane.ScrollPaneStyle();

        ScrollPane scrollPane = new ScrollPane(contentLabel, scrollStyle);
        stage.setScrollFocus(scrollPane);
        scrollPane.setScrollingDisabled(true, false);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollbarsVisible(false);
        scrollPane.setOverscroll(false, false);



        root.add(scrollPane)
                .width(textWidth)
                .expand()
                .center()
                .padTop(100f)
                .row();

        ButtonFactory bf = new ButtonFactory(skin);

        TextButton readBtn = bf.create("READ", () -> {
            remove();
            callback.onRead();
        });

        TextButton discardBtn = bf.create("DISPOSE", () -> {
            remove();
            callback.onDiscard();
        });

        Table btnTable = new Table();
        btnTable.defaults().width(260).height(80);

        btnTable.add(readBtn).expandX().left().padLeft(120);
        btnTable.add(discardBtn).expandX().right().padRight(120);

        root.add(btnTable)
                .fillX()
                .padBottom(40);
    }


    private String buildContentText() {
        StringBuilder sb = new StringBuilder();
        for (String line : data.content) {
            if ("---".equals(line)) {
                sb.append("\n========\n\n");
            } else {
                sb.append(line).append("\n\n");
            }
        }
        return sb.toString();
    }
}
