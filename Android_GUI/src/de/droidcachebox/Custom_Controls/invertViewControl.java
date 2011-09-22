/* 
 * Copyright (C) 2011 team-cachebox.de
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

package de.droidcachebox.Custom_Controls;

import java.util.Timer;
import java.util.TimerTask;

import CB_Core.Config;
import de.droidcachebox.Global;
import de.droidcachebox.R;
import de.droidcachebox.UnitFormatter;
import de.droidcachebox.main;

import de.droidcachebox.Components.CacheDraw;
import de.droidcachebox.Components.CacheDraw.DrawStyle;
import de.droidcachebox.Views.CacheListView;
import de.droidcachebox.Views.DescriptionView;
import CB_Core.Types.Cache;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;

import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.LinearLayout;

public final class invertViewControl extends View
{

	public static invertViewControl Me;

	public invertViewControl(Context context)
	{
		super(context);
		Me = this;
	}

	public invertViewControl(Context context, AttributeSet attrs)
	{
		super(context, attrs);

		Me = this;
	}

	public invertViewControl(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		Me = this;
	}

	LinearLayout WebViewLayout = null;
	Bitmap b = null;
	boolean firstDraw = true;

	@Override
	protected void onDraw(Canvas canvas)
	{

		canvas.drawColor(Global.getColor(R.attr.EmptyBackground));

		WebViewLayout = DescriptionView.webViewLayout;
		b = Bitmap.createBitmap(this.getWidth(), this.getHeight(),
				Bitmap.Config.ARGB_8888);

		Canvas c = new Canvas(b);

		WebViewLayout.draw(c);

		canvas.drawBitmap(b, 0, 0, main.N ? Global.invertPaint : new Paint());

		super.onDraw(canvas);

		// beim ersten zeichnen muss gewartet werden, bis das WebView auch
		// gezeichnet ist.
		// dies Zeichnet aber nur wenn das DescView nochmal aufgerufen wird.
		// Also machen wir das nach 100 msec
		if (firstDraw)
		{
			TimerTask task = new TimerTask()
			{
				@Override
				public void run()
				{
					Thread t = new Thread()
					{
						public void run()
						{
							main.mainActivity.runOnUiThread(new Runnable()
							{
								@Override
								public void run()
								{
									firstDraw = false;
									((main) main.mainActivity).showView(4);
								}
							});
						}
					};

					t.start();

				}
			};

			Timer timer = new Timer();
			timer.schedule(task, 100);

		}

	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh)
	{

	}

}
