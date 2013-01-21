package CB_Core.GL_UI.utils;

import CB_Core.GL_UI.SpriteCache;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class ColorDrawable extends EmptyDrawable
{

	/**
	 * Da beim Zeichnen dieses Sprites, dieses nicht Manipuliert wird, brauchen wir hier nur eine einmalige Statische Instanz
	 */
	private static Sprite pixelSprite;

	private Color mColor;

	public ColorDrawable(Color color)
	{
		setColor(color);
	}

	@Override
	public void draw(SpriteBatch batch, float x, float y, float width, float height)
	{
		if (pixelSprite != null)
		{
			Color altColor = batch.getColor();

			float r = altColor.r;
			float g = altColor.g;
			float b = altColor.b;
			float a = altColor.a;

			batch.setColor(mColor);
			batch.draw(pixelSprite, x, y, width, height);
			batch.setColor(r, g, b, a);
		}
		else
		{
			pixelSprite = SpriteCache.getThemedSprite("pixel2x2");
		}
	}

	public void setColor(Color color)
	{
		mColor = color;
	}

}
