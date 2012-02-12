package CB_Core.GL_UI.Controls;

import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.UiSizes;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

/**
 * Eine Test GLView , die nur ein Image anzeigen soll. Im Einfachsten Fall soll der Richtungspfeil in der Mitte des Schirms plaziert werden.
 * 
 * @author Longri
 */
public class TestView extends GL_View_Base
{

	int arrowX = 200;
	int arrowY = 300;

	// # Constructors
	/**
	 * Constructor für ein neues TestView mit Angabe der linken unteren Ecke und der Höhe und Breite
	 * 
	 * @param X
	 * @param Y
	 * @param Width
	 * @param Height
	 */
	public TestView(float X, float Y, float Width, float Height)
	{
		super(X, Y, Width, Height);

	}

	@Override
	public void render(SpriteBatch batch)
	{

		if (SpriteCache.MapArrows == null)
		{

			SpriteCache.LoadSprites();

			// initial Sizes
			UiSizes.GL.initial();
		}

		Sprite arrow = SpriteCache.MapArrows.get(0);
		arrow.setRotation(0);
		arrow.setBounds(-(width / 2), 0, width, height);
		arrow.setOrigin(centerPos.x, centerPos.y);
		arrow.draw(batch);

	}

	@Override
	public void onRezised(CB_RectF rec)
	{
		UiSizes.GL.initial(width, height);

	}

	@Override
	protected void onClicked(Vector2 pos)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onTouchDown(Vector2 pos)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onTouchRelease()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onTouchDown(int x, int y, int pointer, int button)
	{
		// Pfeil auf Touch-Positon setzen zum Test
		arrowX = x;
		arrowY = y;
		return true;
	}

	@Override
	public boolean onClick(int x, int y, int pointer, int button)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onLongClick(int x, int y, int pointer, int button)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onTouchDragged(int x, int y, int pointer)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onTouchUp(int x, int y, int pointer, int button)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void dispose()
	{
		// TODO Auto-generated method stub

	}

}
