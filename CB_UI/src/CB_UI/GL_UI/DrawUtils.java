package CB_UI.GL_UI;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class DrawUtils
{
	public static void drawSpriteLine(SpriteBatch batch, Sprite sprite, float overlap, float x1, float y1, float x2, float y2)
	{
		drawSpriteLine(batch, sprite, sprite, overlap, x1, y1, x2, y2);
	}

	public static void drawSpriteLine(SpriteBatch batch, Sprite sprite, Sprite spriteEnd, float overlap, float x1, float y1, float x2,
			float y2)
	{
		// chk NPE
		if (batch == null || sprite == null || spriteEnd == null) return;

		float angle = 0; // Pi-basiert, x-Achse 0, gegen Uhrzeigersinn
		if (x1 < x2) // 0-)90( Grad; )270(-360 Grad
		if (y1 <= y2) // 0-)90( Grad
		angle = (float) Math.atan((y2 - y1) / (x2 - x1));
		else
			// )270(-)360( Grad
			angle = (float) (2 * Math.PI - (float) Math.atan((y1 - y2) / (x2 - x1)));
		else if (x1 > x2) // )90( - 180 Grad, 180 - )270( Grad
		if (y1 <= y2) // )90( - 180 Grad
		angle = (float) (Math.PI - (float) Math.atan((y2 - y1) / (x1 - x2)));
		else
			// )270(-)360( Grad
			angle = (float) (Math.PI + (float) Math.atan((y1 - y2) / (x1 - x2)));
		else // 90 Grad, 270 Grad
		if (y1 < y2) // 90 Grad
		angle = (float) (0.5 * Math.PI);
		else if (y1 > y2) // 270 Grad
		angle = (float) (1.5 * Math.PI);
		else
			// 0 Grad
			angle = 0;

		float SpriteHalfX = sprite.getWidth() / 2;
		float SpriteHalfY = sprite.getHeight() / 2;

		sprite.setOrigin(SpriteHalfX, SpriteHalfY);
		sprite.setRotation((float) (angle * 180 / Math.PI) - 90); // Grad-basiert, y-Achse 0 Grad, gegen Uhrzeigersinn

		float SpriteEndHalfX = spriteEnd.getWidth() / 2;
		float SpriteEndHalfY = spriteEnd.getHeight() / 2;

		spriteEnd.setOrigin(SpriteEndHalfX, SpriteEndHalfY);
		spriteEnd.setRotation((float) (angle * 180 / Math.PI) - 90); // Grad-basiert, y-Achse 0 Grad, gegen Uhrzeigersinn

		float h = sprite.getHeight() * overlap;

		double sina = Math.sin(angle);
		double cosa = Math.cos(angle);

		int l = (int) (Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1)));
		int n = (int) ((l - h / 2) / h);
		if (n * h < l - h / 2) n += 1;
		for (int i = 0; i < n + 1; i++)
		{
			float x = (float) ((i * h * cosa) + x1);
			float y = (float) ((i * h * sina) + y1);

			if (i == 0 || i == n)
			{
				spriteEnd.setPosition(x - SpriteEndHalfX, y - SpriteEndHalfY);
				spriteEnd.draw(batch);
			}
			else
			{
				sprite.setPosition(x - SpriteHalfX, y - SpriteHalfY);
				sprite.draw(batch);
			}

		}
	}

}
