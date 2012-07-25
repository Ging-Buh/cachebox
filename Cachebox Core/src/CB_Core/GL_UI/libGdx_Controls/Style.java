package CB_Core.GL_UI.libGdx_Controls;

import CB_Core.GL_UI.Fonts;
import CB_Core.GL_UI.SpriteCache;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldStyle;

public class Style
{
	public static TextFieldStyle getTextFieldStyle()
	{
		TextFieldStyle ret = new TextFieldStyle();

		ret.background = new NinePatch(SpriteCache.getThemedSprite("text-field-back"), 16, 16, 16, 16);
		ret.font = Fonts.getNormal();
		ret.fontColor = Color.WHITE;

		ret.messageFont = Fonts.getSmall();
		ret.messageFontColor = Color.WHITE;

		ret.cursor = new NinePatch(SpriteCache.getThemedSprite("selection-input-icon"), 1, 1, 2, 2);

		ret.selection = SpriteCache.getThemedSprite("InfoPanelBack");

		return ret;
	}

	public static TextFieldStyle getTextFieldStyleFocus()
	{
		TextFieldStyle ret = new TextFieldStyle();

		ret.background = new NinePatch(SpriteCache.getThemedSprite("text-field-back-focus"), 16, 16, 16, 16);
		ret.font = Fonts.getNormal();
		ret.fontColor = Color.WHITE;

		ret.messageFont = Fonts.getSmall();
		ret.messageFontColor = Color.WHITE;

		ret.cursor = new NinePatch(SpriteCache.getThemedSprite("selection-input-icon"), 1, 1, 2, 2);

		ret.selection = SpriteCache.getThemedSprite("InfoPanelBack");

		return ret;
	}

}
