package CB_UI_Base.GL_UI.utils;

import CB_UI_Base.GL_UI.Fonts;
import CB_UI_Base.GL_UI.SpriteCacheBase;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldStyle;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

public class Style
{
	public static TextFieldStyle getTextFieldStyle()
	{
		TextFieldStyle ret = new TextFieldStyle();

		ret.background = new NinePatchDrawable(new NinePatch(SpriteCacheBase.getThemedSprite("text-field-back"), 16, 16, 16, 16));
		ret.font = Fonts.getNormal();
		ret.fontColor = Color.WHITE;

		ret.messageFont = Fonts.getSmall();
		ret.messageFontColor = Color.WHITE;

		ret.cursor = new NinePatchDrawable(new NinePatch(SpriteCacheBase.getThemedSprite("selection-input-icon"), 1, 1, 2, 2));

		ret.selection = new SpriteDrawable(SpriteCacheBase.getThemedSprite("InfoPanelBack"));

		return ret;
	}

	public static TextFieldStyle getTextFieldStyleFocus()
	{
		TextFieldStyle ret = new TextFieldStyle();

		ret.background = new NinePatchDrawable(new NinePatch(SpriteCacheBase.getThemedSprite("text-field-back-focus"), 16, 16, 16, 16));
		ret.font = Fonts.getNormal();
		ret.fontColor = Color.WHITE;

		ret.messageFont = Fonts.getSmall();
		ret.messageFontColor = Color.WHITE;

		ret.cursor = new NinePatchDrawable(new NinePatch(SpriteCacheBase.getThemedSprite("selection-input-icon"), 1, 1, 2, 2));

		ret.selection = new SpriteDrawable(SpriteCacheBase.getThemedSprite("InfoPanelBack"));

		return ret;
	}

}
