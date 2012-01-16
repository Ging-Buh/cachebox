/* 
 * Copyright (C) 2011-2012 team-cachebox.de
 *
 * Licensed under the : GNU General Public License (GPL);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.gnu.org/licenses/gpl.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.cachebox_test.Custom_Controls;

import java.util.Date;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

import de.cachebox_test.Map.SpriteCache;
import de.cachebox_test.Ui.Math.ChangedRectF;

/**
 * Enthält die Logik und Render Methoden für Zoom Buttons
 * 
 * @author Longri
 */
public class GL_ZoomBtn
{

	private int minzoom = 6;
	private int maxzoom = 20;
	private int zoom = 13;
	private ChangedRectF HitRecUp;
	private ChangedRectF HitRecDown;
	private ChangedRectF BtnDrawRec;
	private ChangedRectF ScaleDrawRec;
	private boolean isVisible = true;
	private Date timeLastAction = new Date();
	private final int timeToFadeOut = 7000; // 7Sec
	private final int fadeStep = 50; // 100 mSec
	private boolean fadeOut = false;
	private boolean fadeIn = false;
	private float FadeValue = 1.0f;

	private boolean onTouchUp = false;
	private boolean onTouchDown = false;

	/**
	 * Standard Constructor. </br> minzoom = 6 </br> maxzoom = 20 </br> zoom = 13; </br>
	 */
	public GL_ZoomBtn()
	{
	}

	/**
	 * Constructor, für die Übergabe von max,min und act Zoom Level
	 * 
	 * @param minzoom
	 * @param maxzoom
	 * @param zoom
	 */
	public GL_ZoomBtn(int minzoom, int maxzoom, int zoom)
	{
		this.minzoom = minzoom;
		this.maxzoom = maxzoom;
		this.zoom = zoom;
	}

	public boolean hitTest(Vector2 pos)
	{
		if (zoom != maxzoom)
		{
			if (HitRecUp != null)
			{
				if (HitRecUp.contains(pos.x, pos.y))
				{
					if (FadeValue > 0.4f) ZoomAdd(1);
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
					if (FadeValue > 0.4f) ZoomAdd(-1);
					return true;
				}
			}
		}
		return false;
	}

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

	public void Render(SpriteBatch batch, ChangedRectF rect)
	{
		// rect auf Teilen in zwei gleich große
		HitRecUp = rect.copy();
		HitRecDown = rect.copy();
		HitRecUp.setWidth(rect.getWidth() / 2);
		HitRecDown.setWidth(rect.getWidth() / 2);
		HitRecUp.setPos(new Vector2(rect.getX() + HitRecDown.getWidth(), rect.getY()));

		if (!isVisible) return;
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

	public void TouchRelease()
	{
		onTouchUp = false;
		onTouchDown = false;
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
		else if (!isVisible)
		{
			// Log.d("CACHEBOX", "Start Fade In");
			isVisible = true;
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
		if (!fadeOut && !fadeIn && isVisible)
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
					isVisible = false;
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

}
