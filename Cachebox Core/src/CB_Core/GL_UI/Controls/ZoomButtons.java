package CB_Core.GL_UI.Controls;

import java.util.Date;

import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.Math.CB_RectF;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public class ZoomButtons extends GL_View_Base
{

	private int minzoom = 6;
	private int maxzoom = 20;
	private int zoom = 13;
	private CB_RectF HitRecUp;
	private CB_RectF HitRecDown;
	private CB_RectF BtnDrawRec;

	private Date timeLastAction = new Date();
	private final int timeToFadeOut = 7000; // 7Sec
	private final int fadeStep = 50; // 100 mSec
	private boolean fadeOut = false;
	private boolean fadeIn = false;
	private float FadeValue = 1.0f;

	// # Constructors
	/**
	 * Constructor für ein neues TestView mit Angabe der linken unteren Ecke und der Höhe und Breite
	 * 
	 * @param X
	 * @param Y
	 * @param Width
	 * @param Height
	 */
	public ZoomButtons(float X, float Y, float Width, float Height)
	{
		super(X, Y, Width, Height);
		onRezised(this);
	}

	//
	// /**
	// * Standard Constructor. </br> minzoom = 6 </br> maxzoom = 20 </br> zoom = 13; </br>
	// */
	// public ZoomButtons()
	// {
	// }
	//
	// /**
	// * Constructor, für die Übergabe von max,min und act Zoom Level
	// *
	// * @param minzoom
	// * @param maxzoom
	// * @param zoom
	// */
	// public ZoomButtons(int minzoom, int maxzoom, int zoom)
	// {
	// this.minzoom = minzoom;
	// this.maxzoom = maxzoom;
	// this.zoom = zoom;
	// }

	@Override
	public boolean hitTest(Vector2 pos)
	{
		if (zoom != maxzoom)
		{
			if (HitRecUp != null)
			{
				if (HitRecUp.contains(pos.x, pos.y))
				{
					/* if (FadeValue > 0.4f) */ZoomAdd(1);
					resetFadeOut();
					return true;
				}
			}
		}

		if (zoom != minzoom)
		{
			if (HitRecDown != null)
			{
				if (HitRecDown.contains(pos.x, pos.y))
				{
					/* if (FadeValue > 0.4f) */ZoomAdd(-1);
					resetFadeOut();
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean touchDownTest(Vector2 pos)
	{
		if (HitRecUp != null)
		{
			if (HitRecUp.contains(pos.x, pos.y))
			{
				onTouchUp = true;
				resetFadeOut();
				return true;
			}
		}
		if (HitRecDown != null)
		{
			if (HitRecDown.contains(pos.x, pos.y))
			{
				onTouchDown = true;
				resetFadeOut();
				return true;
			}
		}
		return false;
	}

	@Override
	public void render(SpriteBatch batch)
	{

		if (!this.isVisible()) return;
		// Log.d("CACHEBOX", "in=" + fadeIn + " out=" + fadeOut + " Fade=" + FadeValue);
		checkFade();

		// draw down button
		Sprite btnDown;
		if (zoom == minzoom)
		{
			btnDown = SpriteCache.ZoomBtn.get(2);// disabled
		}
		else
		{
			btnDown = SpriteCache.ZoomBtn.get(onTouchDown ? 1 : 0);
		}
		btnDown.setBounds(HitRecDown.getX(), HitRecDown.getY(), HitRecDown.getWidth(), HitRecDown.getHeight());
		btnDown.draw(batch, FadeValue);

		// draw up button
		Sprite btnUp;
		if (zoom == maxzoom)
		{
			btnUp = SpriteCache.ZoomBtn.get(5);// disabled
		}
		else
		{
			btnUp = SpriteCache.ZoomBtn.get(onTouchUp ? 4 : 3);
		}
		btnUp.setBounds(HitRecUp.getX(), HitRecUp.getY(), HitRecUp.getWidth(), HitRecUp.getHeight());
		btnUp.draw(batch, FadeValue);
	}

	public void ZoomAdd(int value)
	{

		zoom += value;
		if (zoom > maxzoom) zoom = maxzoom;
		if (zoom < minzoom) zoom = minzoom;

		// //Log.d("CACHEBOX", "ZoomAdd" + zoom);
	}

	public void setZoom(int value)
	{
		zoom = value;
		if (zoom > maxzoom) zoom = maxzoom;
		if (zoom < minzoom) zoom = minzoom;
	}

	public int getZoom()
	{
		return zoom;
	}

	public void setMaxZoom(int value)
	{
		if (minzoom > value)
		{
			try
			{
				throw new Exception("value out of range minzoom > maxzoom");
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

		maxzoom = value;
	}

	public int getMaxZoom()
	{
		return maxzoom;
	}

	public void setMinZoom(int value)
	{
		if (maxzoom < value)
		{
			try
			{
				throw new Exception("value out of range minzoom > maxzoom");
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

		minzoom = value;
	}

	public int getMinZoom()
	{
		return minzoom;
	}

	/**
	 * Irgend eine Taste gedrückt, also FadeOut zurück setzen
	 */
	private void resetFadeOut()
	{
		// Log.d("CACHEBOX", "Reset Fade Out");
		if (fadeIn && !fadeOut)
		{
			fadeIn = false;
			FadeValue = 1.0f;
		}
		else if (!this.isVisible())
		{
			// Log.d("CACHEBOX", "Start Fade In");
			this.setVisibility(VISIBLE);
			fadeIn = true;
			FadeValue = 0f;
		}
		if (fadeOut)
		{
			fadeOut = false;
			FadeValue = 1.0f;
		}

		timeLastAction = new Date();
	}

	private void checkFade()
	{
		if (!fadeOut && !fadeIn && this.isVisible())
		{
			Date now = new Date();
			if (now.getTime() - timeLastAction.getTime() > timeToFadeOut)
			{
				// Log.d("CACHEBOX", "Start Fade Out");
				// Zeit abgelaufen start Fade Out
				fadeOut = true;
				timeLastAction = new Date();
			}
		}
		else if (fadeOut)
		{
			Date now = new Date();
			if (now.getTime() - timeLastAction.getTime() > fadeStep)
			{
				FadeValue -= 0.05f;
				if (FadeValue <= 0f)
				{
					// Log.d("CACHEBOX", "Ende Fade Out");
					FadeValue = 0f;
					fadeOut = false;
					this.setVisibility(INVISIBLE);
				}
				timeLastAction = new Date();
			}
		}
		else if (fadeIn)
		{
			Date now = new Date();
			if (now.getTime() - timeLastAction.getTime() > fadeStep)
			{
				FadeValue += 0.1f;
				if (FadeValue >= 1f)
				{
					// Log.d("CACHEBOX", "Ende Fade In");
					FadeValue = 1f;
					fadeIn = false;
				}
				timeLastAction = new Date();
			}
		}
	}

	@Override
	public void onRezised(CB_RectF rec)
	{
		// rect auf Teilen in zwei gleich große
		HitRecUp = rec.copy();
		HitRecDown = rec.copy();
		HitRecUp.setWidth(rec.getWidth() / 2);
		HitRecDown.setWidth(rec.getWidth() / 2);
		HitRecUp.setPos(new Vector2(rec.getX() + HitRecDown.getWidth(), rec.getY()));

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
		// TODO Auto-generated method stub
		return false;
	}

}
