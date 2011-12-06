package de.cachebox_test.Views;

import CB_Core.Types.Cache;
import CB_Core.Types.JokerEntry;
import android.content.Context;
import android.graphics.Canvas;
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
import de.cachebox_test.Ui.Sizes;

public class JokerViewItem extends View
{
	private Cache cache;
	private JokerEntry joker;
	private int mAscent;
	private int width;
	private int height;
	private boolean BackColorChanger = false;
	private int rightBorder;
	private int imgSize;
	private StaticLayout LayoutName;
	private StaticLayout LayoutTage;
	private StaticLayout LayoutTelefon;
	private StaticLayout LayoutBemerkung;
	private TextPaint LayoutTextPaint;
	private TextPaint LayoutTextPaintBold;
	private int LineSep;

	public JokerViewItem(Context context, Cache cache, JokerEntry joker, Boolean BackColorId)
	{
		// TODO Auto-generated constructor stub
		super(context);
		this.cache = cache;
		this.joker = joker;

		BackColorChanger = BackColorId;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		measureWidth(widthMeasureSpec);
		this.imgSize = (int) ((JokerView.windowH / 5) * 0.6);
		this.rightBorder = (int) (JokerView.windowH / 5);
		int TextWidth = this.width - this.imgSize - this.rightBorder;

		Rect bounds = new Rect();
		LayoutTextPaint = new TextPaint();
		LayoutTextPaint.setTextSize((float) (Sizes.getScaledFontSize() * 1.3));
		LayoutTextPaint.getTextBounds("T", 0, 1, bounds);
		LineSep = bounds.height() / 3;

		LayoutTextPaint.setAntiAlias(true);
		LayoutTextPaint.setColor(Global.getColor(R.attr.TextColor));

		if (joker.Tage == -1) // this Joker is Owner
		{
			LayoutTage = new StaticLayout("Owner von diesem Cache", LayoutTextPaint, TextWidth, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
		}
		else
		{
			LayoutTage = new StaticLayout("gefunden vor " + String.valueOf(joker.Tage) + " Tagen", LayoutTextPaint, TextWidth,
					Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
		}
		LayoutTelefon = new StaticLayout("Tel: " + joker.Telefon, LayoutTextPaint, TextWidth, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
		LayoutBemerkung = new StaticLayout("Bem.:" + joker.Bemerkung, LayoutTextPaint, TextWidth, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
		LayoutTextPaintBold = new TextPaint(LayoutTextPaint);
		LayoutTextPaintBold.setFakeBoldText(true);
		LayoutName = new StaticLayout(joker.GCLogin + " (" + joker.Vorname + ", " + joker.Name + ")", LayoutTextPaintBold, TextWidth,
				Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
		this.height = (LineSep * 5) + LayoutTage.getHeight() + LayoutTelefon.getHeight() + LayoutBemerkung.getHeight()
				+ LayoutName.getHeight();

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

		Boolean isSelected = false;
		// if (Global.SelectedWaypoint() == waypoint ||((
		// Global.SelectedCache()== cache && !(waypoint == null)&&
		// Global.SelectedWaypoint() == waypoint )))
		// {
		// isSelected=true;
		// }

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
		Rect DrawingRec = new Rect(5, 5, width - 5, height - 5);
		ActivityUtils.drawFillRoundRecWithBorder(canvas, DrawingRec, 2, LineColor, BackgroundColor);

		if (joker == null) // this Item is the Cache
		{
			CacheDraw.DrawInfo(cache, canvas, DrawingRec, BackgroundColor, DrawStyle.withoutSeparator, false);
		}
		else
		{

			int left = 15;
			int top = LineSep * 2;

			int iconWidth = 0;
			// draw icon
			// if (((int)waypoint.Type.ordinal()) < Global.CacheIconsBig.length)
			// iconWidth=ActivityUtils.PutImageTargetHeight(canvas,
			// Global.CacheIconsBig[(int)waypoint.Type.ordinal()],
			// CornerSize/2,CornerSize, imgSize);

			// draw Text info
			left += iconWidth;
			top += ActivityUtils.drawStaticLayout(canvas, LayoutName, left, top);
			top += ActivityUtils.drawStaticLayout(canvas, LayoutTage, left, top);
			top += ActivityUtils.drawStaticLayout(canvas, LayoutTelefon, left, top);
			ActivityUtils.drawStaticLayout(canvas, LayoutBemerkung, left, top);
		}
	}
}
