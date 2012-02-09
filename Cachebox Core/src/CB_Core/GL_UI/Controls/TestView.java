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

		// Add ZoomButtons zu dieser View
		// um zu testen, ob Child Views auch gerendert werden.
		btnZoom = new ZoomButtons(20, 20, 200, 75);
		this.addChild(btnZoom);
	}

	private ZoomButtons btnZoom;

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
		arrow.setBounds(200 - UiSizes.GL.halfPosMarkerSize, 300 - UiSizes.GL.halfPosMarkerSize, UiSizes.GL.PosMarkerSize,
				UiSizes.GL.PosMarkerSize);
		arrow.setOrigin(UiSizes.GL.halfPosMarkerSize, UiSizes.GL.halfPosMarkerSize);
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

}
