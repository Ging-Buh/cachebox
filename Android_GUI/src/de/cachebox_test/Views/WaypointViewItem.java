package de.cachebox_test.Views;

import CB_Core.GlobalCore;
import CB_Core.Math.CB_Rect;
import CB_Core.Math.UiSizes;
import CB_Core.Types.Cache;
import CB_Core.Types.Waypoint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.view.View;
import de.cachebox_test.Global;
import de.cachebox_test.R;
import de.cachebox_test.Components.CacheDraw;
import de.cachebox_test.Components.CacheDraw.DrawStyle;
import de.cachebox_test.Ui.ActivityUtils;

public class WaypointViewItem extends View
{
	private Cache cache;
	private Waypoint waypoint;
	private int mAscent;
	private int width;
	private int height;
	private boolean BackColorChanger = false;

	private int rightBorder;
	private int imgSize;
	private StaticLayout LayoutName;
	private StaticLayout LayoutDesc;
	private StaticLayout LayoutCord;
	private StaticLayout LayoutClue;
	private TextPaint LayoutTextPaint;
	private TextPaint LayoutTextPaintRed;
	private TextPaint LayoutTextPaintBold;
	private int LineSep;

	public WaypointViewItem(Context context, Cache cache, Waypoint waypoint, Boolean BackColorId)
	{
		// TODO Auto-generated constructor stub
		super(context);
		this.cache = cache;
		this.waypoint = waypoint;

		BackColorChanger = BackColorId;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		measureWidth(widthMeasureSpec);
		this.imgSize = (int) ((WaypointView.windowH / 5) * 0.6);
		this.rightBorder = (int) (WaypointView.windowH / 5);
		int TextWidth = this.width - this.imgSize - this.rightBorder;

		Rect bounds = new Rect();
		LayoutTextPaint = new TextPaint();
		LayoutTextPaint.setTextSize((float) (UiSizes.getScaledFontSize() * 1.3));
		LayoutTextPaint.getTextBounds("T", 0, 1, bounds);
		LineSep = bounds.height() / 3;

		if (waypoint == null) // this Item is the Cache
		{
			this.height = bounds.height() * 7;
		}
		else
		{
			String Clue = "";
			if (waypoint.Clue != null) Clue = waypoint.Clue;
			LayoutTextPaint.setAntiAlias(true);
			LayoutTextPaint.setColor(Global.getColor(R.attr.TextColor));
			LayoutCord = new StaticLayout(GlobalCore.FormatLatitudeDM(waypoint.Latitude()) + " / "
					+ GlobalCore.FormatLongitudeDM(waypoint.Longitude()), LayoutTextPaint, TextWidth, Alignment.ALIGN_NORMAL, 1.0f, 0.0f,
					false);
			LayoutDesc = new StaticLayout(waypoint.Description, LayoutTextPaint, TextWidth, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);

			LayoutTextPaintBold = new TextPaint(LayoutTextPaint);
			LayoutTextPaintBold.setFakeBoldText(true);
			LayoutTextPaintRed = new TextPaint(LayoutTextPaint);
			LayoutTextPaintRed.setColor(Color.RED);

			LayoutClue = new StaticLayout(Clue, LayoutTextPaintRed, TextWidth, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);

			LayoutName = new StaticLayout(waypoint.GcCode + ": " + waypoint.Title, LayoutTextPaintBold, TextWidth, Alignment.ALIGN_NORMAL,
					1.0f, 0.0f, false);
			this.height = (LineSep * 5) + LayoutCord.getHeight() + LayoutDesc.getHeight() + LayoutClue.getHeight() + LayoutName.getHeight();
		}

		setMeasuredDimension(this.width, this.height);

	}

	/**
	 * Determines the width of this view
	 * 
	 * @param measureSpec
	 *            A measureSpec packed into an int
	 * @return The width of the view, honoring constraints from measureSpec
	 */
	private int measureWidth(int measureSpec)
	{
		int result = 0;
		int specMode = MeasureSpec.getMode(measureSpec);
		int specSize = MeasureSpec.getSize(measureSpec);

		if (specMode == MeasureSpec.EXACTLY)
		{
			// We were told how big to be
			result = specSize;
		}
		else
		{
			// Measure the text
			result = (int) Global.Paints.mesurePaint.measureText(cache.Name) + getPaddingLeft() + getPaddingRight();
			if (specMode == MeasureSpec.AT_MOST)
			{
				// Respect AT_MOST value if that was what is called for by
				// measureSpec
				result = Math.min(result, specSize);
			}
		}
		width = specSize;
		return result;
	}

	/**
	 * Determines the height of this view
	 * 
	 * @param measureSpec
	 *            A measureSpec packed into an int
	 * @return The height of the view, honoring constraints from measureSpec
	 */
	private int measureHeight(int measureSpec)
	{
		int result = 0;
		int specMode = MeasureSpec.getMode(measureSpec);
		int specSize = MeasureSpec.getSize(measureSpec);

		mAscent = (int) Global.Paints.mesurePaint.ascent();
		if (specMode == MeasureSpec.EXACTLY)
		{
			// We were told how big to be
			result = specSize;
		}
		else
		{
			// Measure the text (beware: ascent is a negative number)
			result = (int) (-mAscent + Global.Paints.mesurePaint.descent()) + getPaddingTop() + getPaddingBottom();
			if (specMode == MeasureSpec.AT_MOST)
			{
				// Respect AT_MOST value if that was what is called for by
				// measureSpec
				result = Math.min(result, specSize);
			}
		}
		return result;
	}

	/**
	 * Render the text
	 * 
	 * @see android.view.View#onDraw(android.graphics.Canvas)
	 */
	@Override
	protected void onDraw(Canvas canvas)
	{
		int m = UiSizes.getMargin();

		Boolean isSelected = false;
		if (GlobalCore.SelectedWaypoint() == waypoint
				|| ((GlobalCore.SelectedCache() == cache && !(waypoint == null) && GlobalCore.SelectedWaypoint() == waypoint)))
		{
			isSelected = true;
		}

		canvas.drawColor(Global.getColor(R.attr.myBackground));
		int BackgroundColor;
		if (BackColorChanger)
		{
			BackgroundColor = (isSelected) ? Global.getColor(R.attr.ListBackground_select) : Global.getColor(R.attr.ListBackground);
		}
		else
		{
			BackgroundColor = (isSelected) ? Global.getColor(R.attr.ListBackground_select) : Global.getColor(R.attr.ListBackground_secend);
		}

		int LineColor = Global.getColor(R.attr.ListSeparator);
		CB_Rect DrawingRec = new CB_Rect(m, m, width - m, height - m);
		ActivityUtils.drawFillRoundRecWithBorder(canvas, DrawingRec, 2, LineColor, BackgroundColor);

		if (waypoint == null) // this Item is the Cache
		{
			CacheDraw.DrawInfo(cache, canvas, DrawingRec, BackgroundColor, DrawStyle.withoutSeparator, false);
		}
		else
		{

			int left = m * 3;
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

			// draw Arrow and distance
			// Draw Bearing
			// if (Cache.BearingRec!=null)
			// cache.DrawBearing(canvas,Cache.BearingRec,waypoint);

		}
	}
}
