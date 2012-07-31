package CB_Core.GL_UI.libGdx_Controls;

import CB_Core.GL_UI.Fonts;
import CB_Core.GL_UI.SpriteCache;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldStyle;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

public class Style
{
	public static TextFieldStyle getTextFieldStyle()
	{
		TextFieldStyle ret = new TextFieldStyle();

		ret.background = new Drawable()
		{

			@Override
			public float getTopHeight()
			{
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public float getRightWidth()
			{
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public float getMinWidth()
			{
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public float getMinHeight()
			{
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public float getLeftWidth()
			{
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public float getBottomHeight()
			{
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public void draw(SpriteBatch batch, float x, float y, float width, float height)
			{
				// TODO Auto-generated method stub

			}
		};

		ret.background = new NinePatchDrawable(new NinePatch(SpriteCache.getThemedSprite("text-field-back"), 16, 16, 16, 16));
		ret.font = Fonts.getNormal();
		ret.fontColor = Color.WHITE;

		ret.messageFont = Fonts.getSmall();
		ret.messageFontColor = Color.WHITE;

		ret.cursor = new NinePatchDrawable(new NinePatch(SpriteCache.getThemedSprite("selection-input-icon"), 1, 1, 2, 2));

		ret.selection = new SpriteDrawable(SpriteCache.getThemedSprite("InfoPanelBack"));

		return ret;
	}

	public static TextFieldStyle getTextFieldStyleFocus()
	{
		TextFieldStyle ret = new TextFieldStyle();

		ret.background = new NinePatchDrawable(new NinePatch(SpriteCache.getThemedSprite("text-field-back-focus"), 16, 16, 16, 16));
		ret.font = Fonts.getNormal();
		ret.fontColor = Color.WHITE;

		ret.messageFont = Fonts.getSmall();
		ret.messageFontColor = Color.WHITE;

		ret.cursor = new NinePatchDrawable(new NinePatch(SpriteCache.getThemedSprite("selection-input-icon"), 1, 1, 2, 2));

		ret.selection = new SpriteDrawable(SpriteCache.getThemedSprite("InfoPanelBack"));

		return ret;
	}

}
