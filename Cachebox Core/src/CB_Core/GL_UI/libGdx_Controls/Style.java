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

		ret.background = new NinePatch(SpriteCache.getThemedSprite("listrec_secend"), 8, 8, 8, 8);
		ret.font = Fonts.getNormal();
		ret.fontColor = Color.BLACK;

		ret.messageFont = Fonts.getSmall();
		ret.messageFontColor = Color.RED;

		ret.cursor = new NinePatch(SpriteCache.getThemedSprite("selection-input-icon"), 1, 1, 2, 2);

		ret.selection = SpriteCache.getThemedSprite("InfoPanelBack");

		return ret;
	}
}
