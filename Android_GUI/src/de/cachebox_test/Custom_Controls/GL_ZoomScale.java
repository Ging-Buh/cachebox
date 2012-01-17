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

import java.io.ByteArrayOutputStream;
import java.util.Date;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.Log;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import de.cachebox_test.Global;
import de.cachebox_test.main;
import de.cachebox_test.Ui.Sizes;
import de.cachebox_test.Ui.Math.ChangedRectF;

/**
 * Enth�lt die Logik und Render Methoden f�r die Zoom Scala
 * 
 * @author longri
 */
public class GL_ZoomScale
{

	private int minzoom = 6;
	private int maxzoom = 20;
	private int zoom = 13;
	private ChangedRectF ScaleDrawRec;
	private boolean isVisible = true;
	private Date timeLastAction = new Date();
	private final int timeToFadeOut = 5000; // 5Sec
	private final int fadeStep = 50; // 100 mSec
	private boolean fadeOut = false;
	private boolean fadeIn = false;
	private float FadeValue = 1.0f;
	private Sprite CachedScaleSprite = null;
	private float diffCameraZoom = 0f;
	private ChangedRectF ValueRec;

	private int topRow;
	private int bottomRow = 1;
	private int centerColumn;
	private int halfWidth;
	private float dist = 20;
	private int lineHeight = 10;
	private float numSteps;
	private float grundY;

	/**
	 * Constructor, f�r die �bergabe von max,min und act Zoom Level
	 * 
	 * @param minzoom
	 * @param maxzoom
	 * @param zoom
	 */
	public GL_ZoomScale(int minzoom, int maxzoom, int zoom)
	{
		this.minzoom = minzoom;
		this.maxzoom = maxzoom;
		this.zoom = zoom;
	}

	public void Render(SpriteBatch batch, ChangedRectF rect)
	{
		// rect auf Teilen in zwei gleich gro�e
		ScaleDrawRec = rect.copy();

		if (!isVisible) return;
		// Log.d("CACHEBOX", "in=" + fadeIn + " out=" + fadeOut + " Fade=" + FadeValue);
		checkFade();

		// Draw Scale
		Sprite scale;
		scale = drawSprite(rect);
		scale.setBounds(ScaleDrawRec.getX(), ScaleDrawRec.getY(), ScaleDrawRec.getWidth(), ScaleDrawRec.getHeight());
		scale.draw(batch, FadeValue);

		// Draw Value Background
		// if (ValueRec != null)
		// {
		// Sprite valueBack;
		// valueBack = SpriteCache.MapOverlay.get(0);
		// valueBack.setBounds(ValueRec.getX(), ValueRec.getY(), ValueRec.getWidth(), ValueRec.getWidth());
		// valueBack.draw(batch, FadeValue);
		// }

		// Sprite value;
		// value = drawSprite(rect);
		// value.setBounds(ScaleDrawRec.getX(), ScaleDrawRec.getY(), ScaleDrawRec.getWidth(), ScaleDrawRec.getHeight());
		// value.draw(batch, FadeValue);

	}

	public void setDiffCameraZoom(float value)
	{

		if (value > 0)
		{
			diffCameraZoom = value - zoom;
		}
		else
		{
			diffCameraZoom = value + zoom;
		}
		Log.d("CACHEBOX", "Value=" + value + " |ZoomDiff=" + diffCameraZoom + "  |Zoom=" + zoom);

	}

	public void setZoom(int value)
	{
		zoom = value;
	}

	/**
	 * Zeichnet die Scala in eine Bitmap, damit diese als Sprite benutzt werden kann!
	 * 
	 * @param rect
	 */
	private Sprite drawSprite(ChangedRectF rect)
	{

		int y = (int) ((1 - ((float) (zoom - minzoom)) / numSteps) * (bottomRow - topRow)) + topRow;
		y += dist * (1 + diffCameraZoom) * 1.5;

		if (ValueRec == null)
		{

			topRow = (int) rect.getHeight() - 1;
			bottomRow = 1;
			centerColumn = (int) (rect.getWidth() / 2);
			halfWidth = (int) (rect.getWidth() / 4);
			lineHeight = 10;
			numSteps = maxzoom - minzoom;
			grundY = rect.getY() - halfWidth - rect.getWidth();
			float ValueRecHeight = centerColumn;// y - rect.getHeight() / 2 + rect.getHeight();

			dist = (bottomRow - topRow) / numSteps;

			ValueRec = new ChangedRectF(rect.getX() + Sizes.GL.infoShadowHeight + centerColumn - rect.getWidth() / 2 - lineHeight / 2,
					grundY + y, rect.getWidth(), ValueRecHeight);
		}
		else
		{
			ValueRec.setY(grundY + y);
		}

		if (CachedScaleSprite != null) return CachedScaleSprite;

		Bitmap drawSurface = Bitmap.createBitmap((int) rect.getWidth(), (int) rect.getHeight(), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(drawSurface);

		Paint paint = new Paint();
		paint.setColor(main.N ? Global.getInvertMatrixBlack() : Color.BLACK);
		canvas.drawLine(centerColumn, topRow, centerColumn, bottomRow, paint);

		for (int i = minzoom; i <= maxzoom; i++)
		{
			y = (int) ((1 - ((float) (i - minzoom)) / numSteps) * (bottomRow - topRow)) + topRow;

			Paint font = new Paint();
			font.setTextSize(Sizes.getScaledFontSize_big());
			font.setFakeBoldText(true);
			font.setColor(main.N ? Global.getInvertMatrixBlack() : Color.BLACK);
			Paint white = new Paint();
			white.setColor(main.N ? Global.getInvertMatrixWhite() : Color.WHITE);
			white.setStyle(Style.FILL);
			Paint black = new Paint();
			black.setColor(main.N ? Global.getInvertMatrixBlack() : Color.BLACK);
			black.setStyle(Style.STROKE);

			//
			// }
			// else
			// {
			canvas.drawLine(centerColumn - halfWidth, y, centerColumn + halfWidth, y, black);
			// }
		}

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		drawSurface.compress(Bitmap.CompressFormat.PNG, 50, baos);

		byte[] ByteArray = baos.toByteArray();

		int length = ByteArray.length;

		Pixmap pixmap = new Pixmap(ByteArray, 0, length);

		Texture tex = new Texture(pixmap, Pixmap.Format.RGBA8888, false);

		CachedScaleSprite = new Sprite(tex);
		return CachedScaleSprite;

	}

	/**
	 * Irgend eine Zoom Funktion ausgef�hrt, also FadeOut zur�ck setzen und die Scala Einblenden!
	 */
	public void resetFadeOut()
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
				FadeValue -= 0.1f;
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
				FadeValue += 0.2f;
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
