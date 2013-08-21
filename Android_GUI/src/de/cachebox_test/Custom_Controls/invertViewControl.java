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

import java.util.Timer;
import java.util.TimerTask;

import CB_UI.Config;
import CB_UI_Base.GL_UI.ViewConst;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import de.cachebox_test.Global;
import de.cachebox_test.R;
import de.cachebox_test.main;
import de.cachebox_test.Views.DescriptionView;

/**
 * @author Longri
 */
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
		b = Bitmap.createBitmap(this.getWidth(), this.getHeight(), Bitmap.Config.ARGB_8888);

		Canvas c = new Canvas(b);

		WebViewLayout.draw(c);

		canvas.drawBitmap(b, 0, 0, Config.settings.nightMode.getValue() ? Global.invertPaint : new Paint());

		super.onDraw(canvas);

		// beim ersten zeichnen muss gewartet werden, bis das WebView auch
		// gezeichnet ist.
		// dies Zeichnet aber nur wenn das DescView nochmal aufgerufen wird.
		// Also machen wir das nach 100 msec
		if (firstDraw)
		{
			final TimerTask task = new TimerTask()
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
									if (firstDraw)
									{
										firstDraw = false;
										((main) main.mainActivity).showView(ViewConst.DESCRIPTION_VIEW);
									}
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

		b.recycle();
		b = null;

	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh)
	{

	}

}
