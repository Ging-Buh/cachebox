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

import CB_Core.GL_UI.Fonts;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.GL_UISizes;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import de.cachebox_test.Global;
import de.cachebox_test.main;

/**
 * Enthält die Logik und Render Methoden für die Zoom Scala
 * 
 * @author longri
 */
public class GL_ZoomScale
{

	private int minzoom = 6;
	private int maxzoom = 20;
	private int zoom = 13;
	private CB_RectF ScaleDrawRec;
	private boolean isVisible = true;
	private Date timeLastAction = new Date();
	private final int timeToFadeOut = 5000; // 5Sec
	private final int fadeStep = 50; // 100 mSec
	private boolean fadeOut = false;
	private boolean fadeIn = false;
	private float FadeValue = 1.0f;
	private Sprite CachedScaleSprite = null;
	private float diffCameraZoom = 0f;
	private CB_RectF ValueRec;

	private int topRow;
	private int bottomRow = 1;
	private int centerColumn;
	private int halfWidth;
	private float dist = 20;
	private int lineHeight = 10;
	private float numSteps;
	private float grundY;

	/**
	 * Constructor, für die Übergabe von max,min und act Zoom Level
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

	public void Render(SpriteBatch batch, CB_RectF rect)
	{

		if (rect.getWidth() < 1 || rect.getHeight() < 1) return;

		// rect auf Teilen in zwei gleich große
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
		if (ValueRec != null)
		{
			Sprite valueBack;
			valueBack = SpriteCache.ZoomValueBack;
			valueBack.setBounds(ValueRec.getX(), ValueRec.getY(), ValueRec.getWidth(), ValueRec.getHeight());
			valueBack.draw(batch, FadeValue);
		}

		com.badlogic.gdx.graphics.Color c = Fonts.get22().getColor();
		Fonts.get22().setColor(1f, 1f, 1f, FadeValue);
		Fonts.get22().draw(batch, String.valueOf(zoom), ValueRec.getX() + (ValueRec.getWidth() / 3),
				ValueRec.getY() + ValueRec.getHeight() / 1.5f);
		Fonts.get22().setColor(c.r, c.g, c.b, c.a);
	}

	public void setDiffCameraZoom(float value, boolean positive)
	{

		if (value >= 1 || value <= -1)
		{
			diffCameraZoom = 0;// - zoom;
		}
		else
		{
			if (positive)
			{
				diffCameraZoom = value;
			}
			else
			{
				diffCameraZoom = 1 + value;
			}
			// + zoom;
		}
		// Log.d("CACHEBOX", "Value=" + value + " |ZoomDiff=" + diffCameraZoom + "  |Zoom=" + zoom);

	}

	public void setZoom(int value)
	{
		zoom = value;
	}

	public void setMaxZoom(int value)
	{
		maxzoom = value;
		ValueRec = null;
		CachedScaleSprite = null;
	}

	public void setMinZoom(int value)
	{
		minzoom = value;
		ValueRec = null;
		CachedScaleSprite = null;
	}

	private CB_RectF storedRec;

	/**
	 * Zeichnet die Scala in eine Bitmap, damit diese als Sprite benutzt werden kann!
	 * 
	 * @param rect
	 */
	private Sprite drawSprite(CB_RectF rect)
	{

		if (storedRec == null || !(storedRec.equals(rect)))
		{
			storedRec = rect.copy();
			ValueRec = null;
		}

		int y = (int) ((1 - ((float) ((zoom + diffCameraZoom) - minzoom)) / numSteps) * (bottomRow - topRow)) + topRow;

		if (ValueRec == null)
		{
			topRow = (int) rect.getHeight() - 1;
			bottomRow = 1;
			centerColumn = (int) (rect.getWidth() / 2);
			halfWidth = (int) (rect.getWidth() / 4);
			lineHeight = 10;
			numSteps = maxzoom - minzoom;
			grundY = rect.getY() - halfWidth;

			dist = (bottomRow - topRow) / numSteps;

			ValueRec = new CB_RectF(rect.getX() + GL_UISizes.infoShadowHeight + centerColumn - rect.getWidth() / 2 - lineHeight / 2, grundY
					+ y, rect.getWidth(), rect.getWidth() / 2);
		}
		else
		{
			ValueRec.setY(grundY + y);
		}

		if (CachedScaleSprite != null) return CachedScaleSprite;

		// set Height and Width to next PO2 need for OpenGL 1.1 bad only for BMP creation.
		CB_RectF BMP_Rec = rect.copy();
		BMP_Rec.setPO2();

		Bitmap drawSurface = Bitmap.createBitmap((int) BMP_Rec.getWidth(), (int) BMP_Rec.getHeight(), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(drawSurface);

		Paint paint = new Paint();
		paint.setColor(main.N ? Global.getInvertMatrixBlack() : Color.BLACK);
		canvas.drawLine(centerColumn, topRow, centerColumn, bottomRow, paint);

		// Paint font = new Paint();
		// font.setTextSize(UiSizes.getScaledFontSize_big());
		// font.setFakeBoldText(true);
		// font.setColor(main.N ? Global.getInvertMatrixBlack() : Color.BLACK);
		// Paint white = new Paint();
		// white.setColor(main.N ? Global.getInvertMatrixWhite() : Color.WHITE);
		// white.setStyle(Style.FILL);
		Paint black = new Paint();
		black.setColor(main.N ? Global.getInvertMatrixBlack() : Color.BLACK);
		black.setStyle(Style.STROKE);
		black.setStrokeWidth(2f);

		for (int i = minzoom; i <= maxzoom; i++)
		{
			y = (int) ((1 - ((float) (i - minzoom)) / numSteps) * (bottomRow - topRow)) + topRow;
			canvas.drawLine(centerColumn - halfWidth, y, centerColumn + halfWidth, y, black);

		}

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		drawSurface.compress(Bitmap.CompressFormat.PNG, 50, baos);

		byte[] ByteArray = baos.toByteArray();

		int length = ByteArray.length;

		Pixmap pixmap = new Pixmap(ByteArray, 0, length);

		Texture tex = new Texture(pixmap, Pixmap.Format.RGBA8888, false);

		CachedScaleSprite = new Sprite(tex, (int) rect.getWidth(), (int) rect.getHeight());
		return CachedScaleSprite;

	}

	/**
	 * Irgend eine Zoom Funktion ausgeführt, also FadeOut zurück setzen und die Scala Einblenden!
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

	public boolean isShown()
	{
		return isVisible;
	}

}
