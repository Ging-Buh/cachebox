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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Stellet eine Zeichen Oberfläsche dar
 * 
 * @author Longri
 */
public final class CanvasDrawControl extends View
{
	public CanvasDrawControl(Context context)
	{
		super(context);
	}

	public CanvasDrawControl(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	public CanvasDrawControl(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
	}

	/*
	 * Private Member
	 */
	private int height;
	private int width;

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		this.width = measure(widthMeasureSpec);
		this.height = measure(heightMeasureSpec);

		setMeasuredDimension(this.width, this.height);
	}

	/**
	 * Determines the width of this view
	 * 
	 * @param measureSpec
	 *            A measureSpec packed into an int
	 * @return The width of the view, honoring constraints from measureSpec
	 */
	private int measure(int measureSpec)
	{
		int result = 0;

		int specSize = MeasureSpec.getSize(measureSpec);

		result = specSize;

		return result;
	}

	@Override
	protected void onDraw(Canvas canvas)
	{

		super.onDraw(canvas);
		try
		{
			if (myBitmap != null) canvas.drawBitmap(myBitmap, 0, 0, new Paint());
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private Bitmap myBitmap;

	public Canvas getCanvas()
	{

		myBitmap = Bitmap.createBitmap(width, height, Config.RGB_565);
		Canvas c = new Canvas(myBitmap);
		return c;
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh)
	{
		Log.d("Cachebox", "Size changed to " + w + "x" + h);
	}

	public void setHeight(int MyHeight)
	{
		this.height = MyHeight;
		this.invalidate();
	}

}
