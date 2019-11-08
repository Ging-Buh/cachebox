package de.droidcachebox.gdx.graphics;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldStyle;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import de.droidcachebox.gdx.Fonts;
import de.droidcachebox.gdx.Sprites;

public class Style {
    public static TextFieldStyle getTextFieldStyle() {
        TextFieldStyle ret = new TextFieldStyle();

        ret.background = new NinePatchDrawable(new NinePatch(Sprites.getSprite("text-field-back"), 16, 16, 16, 16));
        ret.font = Fonts.getNormal();
        ret.fontColor = Color.WHITE;

        ret.messageFont = Fonts.getSmall();
        ret.messageFontColor = Color.WHITE;

        ret.cursor = new NinePatchDrawable(new NinePatch(Sprites.getSprite("selection-input-icon"), 1, 1, 2, 2));

        ret.selection = new SpriteDrawable(Sprites.getSprite("InfoPanelBack"));

        return ret;
    }

    public static TextFieldStyle getTextFieldStyleFocus() {
        TextFieldStyle ret = new TextFieldStyle();

        ret.background = new NinePatchDrawable(new NinePatch(Sprites.getSprite("text-field-back-focus"), 16, 16, 16, 16));
        ret.font = Fonts.getNormal();
        ret.fontColor = Color.WHITE;

        ret.messageFont = Fonts.getSmall();
        ret.messageFontColor = Color.WHITE;

        ret.cursor = new NinePatchDrawable(new NinePatch(Sprites.getSprite("selection-input-icon"), 1, 1, 2, 2));

        ret.selection = new SpriteDrawable(Sprites.getSprite("InfoPanelBack"));

        return ret;
    }

}
