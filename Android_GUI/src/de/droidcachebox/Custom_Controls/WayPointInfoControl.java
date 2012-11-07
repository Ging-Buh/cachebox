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
package de.droidcachebox.Custom_Controls;

import CB_Core.GlobalCore;
import CB_Core.Math.CB_Rect;
import CB_Core.Math.UiSizes;
import CB_Core.Types.Cache;
import CB_Core.Types.Waypoint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;
import de.droidcachebox.Global;
import de.droidcachebox.R;
import de.droidcachebox.Ui.ActivityUtils;

public final class WayPointInfoControl extends View
{

	public WayPointInfoControl(Context context)
	{
		super(context);
	}

	public WayPointInfoControl(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	public WayPointInfoControl(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
	}

	/*
	 * Private Member
	 */
	private int height;
	private int width;
	private Cache cache;
	private Waypoint waypoint;
	private int mAscent;
	private boolean BackColorChanger = false;

	private int rightBorder;
	private int imgSize;
	private StaticLayout LayoutName;
	private StaticLayout LayoutDesc;
	private StaticLayout LayoutCord;
	private StaticLayout LayoutClue;
	private TextPaint LayoutTextPaint;
	private TextPaint LayoutTextPaintBold;
	private int LineSep;

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{

		this.width = measure(widthMeasureSpec);
		this.height = measure(heightMeasureSpec);

		if (waypoint == null)
		{
			setMeasuredDimension(this.width, this.height);
			return;
		}

		this.imgSize = (int) ((UiSizes.getWindowHeight() / 5) * 0.6);
		this.rightBorder = (int) (UiSizes.getWindowHeight() / 5);
		int TextWidth = this.width - this.imgSize - this.rightBorder;

		Rect bounds = new Rect();
		LayoutTextPaint = new TextPaint();
		LayoutTextPaint.setTextSize(UiSizes.getScaledFontSize());
		LayoutTextPaint.getTextBounds("T", 0, 1, bounds);
		LineSep = bounds.height() / 3;

		String Clue = "";
		if (waypoint.Clue != null) Clue = waypoint.Clue;
		LayoutTextPaint.setAntiAlias(true);
		LayoutTextPaint.setColor(Global.getColor(R.attr.TextColor));
		LayoutCord = new StaticLayout(GlobalCore.FormatLatitudeDM(waypoint.Pos.getLatitude()) + " / "
				+ GlobalCore.FormatLongitudeDM(waypoint.Pos.getLongitude()), LayoutTextPaint, TextWidth, Alignment.ALIGN_NORMAL, 1.0f,
				0.0f, false);
		LayoutDesc = new StaticLayout(waypoint.Description, LayoutTextPaint, TextWidth, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
		LayoutClue = new StaticLayout(Clue, LayoutTextPaint, TextWidth, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
		LayoutTextPaintBold = new TextPaint(LayoutTextPaint);
		LayoutTextPaintBold.setFakeBoldText(true);
		LayoutName = new StaticLayout(waypoint.GcCode + ": " + waypoint.Title, LayoutTextPaintBold, TextWidth, Alignment.ALIGN_NORMAL,
				1.0f, 0.0f, false);
		this.height = (LineSep * 5) + LayoutCord.getHeight() + LayoutDesc.getHeight() + LayoutClue.getHeight() + LayoutName.getHeight();

		if (this.height > UiSizes.getCacheInfoHeight()) this.height = UiSizes.getCacheInfoHeight();

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

		if (waypoint == null) return;

		int LineColor = Global.getColor(R.attr.ListSeparator);
		CB_Rect DrawingRec = new CB_Rect(5, 5, width - 5, height - 5);
		ActivityUtils.drawFillRoundRecWithBorder(canvas, DrawingRec, 2, LineColor, Global.getColor(R.attr.ListBackground_select));

		int left = 15;
		int top = LineSep * 2;

		int iconWidth = 0;
		// draw icon
		if (((int) waypoint.Type.ordinal()) < Global.CacheIconsBig.length) iconWidth = ActivityUtils.PutImageTargetHeight(canvas,
				Global.CacheIconsBig[(int) waypoint.Type.ordinal()], UiSizes.getHalfCornerSize(), UiSizes.getCornerSize(), imgSize);

		// draw Text info
		left += iconWidth;
		top += ActivityUtils.drawStaticLayout(canvas, LayoutName, left, top);
		top += ActivityUtils.drawStaticLayout(canvas, LayoutDesc, left, top);
		top += ActivityUtils.drawStaticLayout(canvas, LayoutCord, left, top);
		if (waypoint.Clue != null) ActivityUtils.drawStaticLayout(canvas, LayoutClue, left, top);

		canvas.restore();

	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh)
	{
		// Log.d("Cachebox", "Size changed to " + w + "x" + h);
	}

	public void setHeight(int MyHeight)
	{
		this.height = MyHeight;
		this.invalidate();
	}

	public void setWaypoint(Waypoint WP)
	{
		waypoint = WP;
		this.invalidate();
	}

}
