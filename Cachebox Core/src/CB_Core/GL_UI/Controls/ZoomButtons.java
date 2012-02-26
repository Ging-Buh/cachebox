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

package CB_Core.GL_UI.Controls;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.GL_Listener.GL_Listener;
import CB_Core.Log.Logger;
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
	private final int timeToFadeOut = 13000; // 7Sec
	private final int fadeStep = 50; // 100 mSec
	private boolean fadeOut = false;
	private boolean fadeIn = false;
	private float FadeValue = 1.0f;

	private ZoomButtons THIS;

	// # Constructors
	/**
	 * Constructor f¸r ein neues TestView mit Angabe der linken unteren Ecke und der Hˆhe und Breite
	 * 
	 * @param X
	 * @param Y
	 * @param Width
	 * @param Height
	 */
	public ZoomButtons(float X, float Y, float Width, float Height, String Name)
	{
		super(X, Y, Width, Height, Name);
		THIS = this;
		onRezised(this);
		resetFadeOut();
	}

	private OnClickListener mOnClickListenerUp;
	private OnClickListener mOnClickListenerDown;

	public void setOnClickListenerUp(OnClickListener l)
	{
		if (!isClickable)
		{
			isClickable = true;
		}
		mOnClickListenerUp = l;
	}

	public void setOnClickListenerDown(OnClickListener l)
	{
		if (!isClickable)
		{
			isClickable = true;
		}
		mOnClickListenerDown = l;
	}

	@Override
	public boolean click(int x, int y, int pointer, int button)
	{

		boolean behandelt = false;

		if (mOnClickListenerUp != null)
		{
			if (HitRecUp.contains(x, y))
			{
				if (GL_View_Base.debug) Logger.LogCat("ZoomButton OnClick UP x/Y " + x + "/" + y);
				resetFadeOut();
				mOnClickListenerUp.onClick(this, x, y, pointer, button);
				behandelt = true;
			}
		}

		if (mOnClickListenerUp != null && !behandelt)
		{
			if (HitRecDown.contains(x, y))
			{
				if (GL_View_Base.debug) Logger.LogCat("ZoomButton OnClick DOWN x/Y " + x + "/" + y);
				resetFadeOut();
				mOnClickListenerDown.onClick(this, x, y, pointer, button);
				behandelt = true;
			}
		}

		return behandelt;
	}

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

	private boolean firstDraw = true;

	@Override
	public void render(SpriteBatch batch)
	{

		if (firstDraw)
		{
			resetFadeOut();
			firstDraw = false;
		}

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
	 * Irgend eine Taste gedr¸ckt, also FadeOut zur¸ck setzen
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
		startTimerToFadeOut();
	}

	Timer timer;

	private void cancelTimerToFadeOut()
	{

		if (timer != null)
		{
			timer.cancel();
			timer = null;
		}
	}

	private void startTimerToFadeOut()
	{
		cancelTimerToFadeOut();

		timer = new Timer();
		TimerTask task = new TimerTask()
		{
			@Override
			public void run()
			{
				GL_Listener.glListener.addRenderView(THIS, GL_Listener.FRAME_RATE_ACTION);
				cancelTimerToFadeOut();
			}
		};
		timer.schedule(task, timeToFadeOut);
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
					GL_Listener.glListener.removeRenderView(this);
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
					GL_Listener.glListener.removeRenderView(this);
				}
				timeLastAction = new Date();
			}
		}
	}

	@Override
	public void onRezised(CB_RectF rec)
	{
		// rect auf Teilen in zwei gleich groﬂe
		HitRecUp = rec.copy();
		HitRecUp.setPos(new Vector2()); // setze auf 0,0
		HitRecDown = rec.copy();
		HitRecDown.setPos(new Vector2()); // setze auf 0,0
		HitRecUp.setWidth(rec.getWidth() / 2);
		HitRecDown.setWidth(rec.getWidth() / 2);
		HitRecUp.setPos(new Vector2(HitRecDown.getWidth(), 0));

	}

	@Override
	public boolean onTouchDown(int x, int y, int pointer, int button)
	{
		return touchDownTest(new Vector2(x, y));
	}

	@Override
	public boolean onLongClick(int x, int y, int pointer, int button)
	{
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean onTouchDragged(int x, int y, int pointer)
	{
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean onTouchUp(int x, int y, int pointer, int button)
	{
		onTouchUp = onTouchDown = false;
		return true;
	}

	@Override
	public void dispose()
	{
		// TODO Auto-generated method stub

	}

}
