package CB_Core.GL_UI.utils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class ColorDrawable extends EmptyDrawable
{

	private Texture tex;
	private Color mColor;

	public ColorDrawable(Color color)
	{
		setColor(color);
	}

	@Override
	public void draw(SpriteBatch batch, float x, float y, float width, float height)
	{
		if (tex != null)
		{
			Color altColor = batch.getColor();

			float r = altColor.r;
			float g = altColor.g;
			float b = altColor.b;
			float a = altColor.a;

			batch.setColor(mColor);
			batch.draw(tex, x, y, width, height);
			batch.setColor(r, g, b, a);
		}
		else
		{
			setColor(mColor);
		}
	}

	public void setColor(Color color)
	{
		mColor = color;
		int w = 2;
		int h = 2;
		Pixmap p = new Pixmap(w, h, Pixmap.Format.RGB565);
		p.setColor(color);

		p.fillRectangle(0, 0, w, h);

		try
		{
			tex = new Texture(p);
		}
		catch (Exception e)
		{
			tex = null;
		}

		p.dispose();
	}

	/**
	 * Calculates the next highest power of two for a given integer.
	 * 
	 * @param n
	 *            the number
	 * @return a power of two equal to or higher than n
	 */
	public static int getNextHighestPO2(int n)
	{
		n -= 1;
		n = n | (n >> 1);
		n = n | (n >> 2);
		n = n | (n >> 4);
		n = n | (n >> 8);
		n = n | (n >> 16);
		n = n | (n >> 32);
		return n + 1;
	}

}
