package de.droidcachebox.Views.Forms;

import java.io.File;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.view.View;
import de.droidcachebox.Global;
import de.droidcachebox.R;
import de.droidcachebox.Ui.ActivityUtils;
import de.droidcachebox.Ui.Sizes;

public class SelectDBItem extends View
{
	private File file;
	private boolean BackColorChanger = false;
	private int width;
	private int height;
	private StaticLayout LayoutName;
	private StaticLayout LayoutSize;
	private StaticLayout LayoutDate;
	private TextPaint LayoutTextPaint;
	private TextPaint LayoutTextPaintBold;
	private int LineSep;

	public SelectDBItem(Context context, File file, Boolean BackColorId)
	{
		// TODO Auto-generated constructor stub
		super(context);
		this.file = file;

		BackColorChanger = BackColorId;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		measureWidth(widthMeasureSpec);

		int TextWidth = this.width;

		Rect bounds = new Rect();
		LayoutTextPaint = new TextPaint();
		LayoutTextPaint.setTextSize((float) (Sizes.getScaledFontSize() * 1.3));
		LayoutTextPaint.getTextBounds("T", 0, 1, bounds);
		LayoutTextPaint.setAntiAlias(true);
		LayoutTextPaint.setColor(Global.getColor(R.attr.TextColor));
		LayoutTextPaintBold = new TextPaint(LayoutTextPaint);
		LayoutTextPaintBold.setFakeBoldText(true);
		LayoutTextPaintBold.setAntiAlias(true);

		LineSep = bounds.height() / 3;

		LayoutName = new StaticLayout(file.getName(), LayoutTextPaintBold, TextWidth, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
		LayoutSize = new StaticLayout("x", LayoutTextPaint, TextWidth, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
		LayoutDate = new StaticLayout("y", LayoutTextPaint, TextWidth, Alignment.ALIGN_OPPOSITE, 1.0f, 0.0f, false);

		this.height = (LineSep * 5) + LayoutName.getHeight() + LayoutSize.getHeight();

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
			result = (int) Global.Paints.mesurePaint.measureText(file.getName()) + getPaddingLeft() + getPaddingRight();
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

		result = 100;
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
		if (file == SelectDB.AktFile)
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
		Rect DrawingRec = new Rect(5, 5, width - 5, height - 5);
		ActivityUtils.drawFillRoundRecWithBorder(canvas, DrawingRec, 2, LineColor, BackgroundColor);

		int left = 15;
		int top = LineSep * 2;

		int iconWidth = 0;

		// draw Text info
		left += iconWidth;
		top += ActivityUtils.drawStaticLayout(canvas, LayoutName, left, top);
		ActivityUtils.drawStaticLayout(canvas, LayoutSize, left, top);
		ActivityUtils.drawStaticLayout(canvas, LayoutDate, width - left, top);

	}
}
