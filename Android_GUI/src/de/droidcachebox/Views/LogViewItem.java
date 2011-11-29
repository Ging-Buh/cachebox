package de.droidcachebox.Views;

import java.text.SimpleDateFormat;

import CB_Core.Types.Cache;
import CB_Core.Types.LogEntry;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.view.View;
import de.droidcachebox.Global;
import de.droidcachebox.R;
import de.droidcachebox.Ui.ActivityUtils;
import de.droidcachebox.Ui.Sizes;

public class LogViewItem extends View
{
	private Cache cache;
	private LogEntry logEntry;
	private int mAscent;
	private int width;
	private int height;
	public static TextPaint textPaint;
	private StaticLayout layoutComment;
	private StaticLayout layoutFinder;

	private boolean BackColorChanger = false;

	public LogViewItem(Context context, Cache cache, LogEntry logEntry, Boolean BackColorId)
	{

		super(context);
		this.cache = cache;
		this.logEntry = logEntry;

		if (textPaint == null)
		{
			textPaint = new TextPaint();
			textPaint.setTextSize((float) (Sizes.getScaledFontSize() * 1.3));
			textPaint.setColor(Global.getColor(R.attr.TextColor));
			textPaint.setAntiAlias(true);
		}

		BackColorChanger = BackColorId;

	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		setMeasuredDimension(measureWidth(widthMeasureSpec), measureHeight(heightMeasureSpec));
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
			result = (int) textPaint.measureText(cache.Name) + getPaddingLeft() + getPaddingRight();
			if (specMode == MeasureSpec.AT_MOST)
			{
				// Respect AT_MOST value if that was what is called for by
				// measureSpec
				result = Math.min(result, specSize);
			}
		}
		width = specSize;

		int innerWidth = width - (Sizes.getCornerSize() * 2);

		layoutComment = new StaticLayout(logEntry.Comment, textPaint, innerWidth, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
		layoutFinder = new StaticLayout(logEntry.Finder, textPaint, innerWidth, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);

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

		mAscent = (int) textPaint.ascent();
		if (specMode == MeasureSpec.EXACTLY)
		{
			// We were told how big to be
			result = specSize;
		}
		else
		{
			// Measure the text (beware: ascent is a negative number)
			result = (int) (-mAscent + textPaint.descent()) + getPaddingTop() + getPaddingBottom();
			result += layoutComment.getHeight();
			result += layoutFinder.getHeight();

			if (specMode == MeasureSpec.AT_MOST)
			{
				// Respect AT_MOST value if that was what is called for by
				// measureSpec
				result = Math.min(result, specSize);
			}
		}

		result += Sizes.getCornerSize() * 2;
		height = result;
		return result;
	}

	// static Member
	public static Paint Linepaint;
	public static Paint KopfPaint;
	public static Paint NamePaint;
	private static int headHeight;
	private static int headLinePos;

	@Override
	protected void onDraw(Canvas canvas)
	{

		// initial

		if (textPaint == null)
		{
			textPaint = new TextPaint();
			textPaint.setTextSize((float) (Sizes.getScaledFontSize() * 1.3));
			textPaint.setColor(Global.getColor(R.attr.TextColor));
			textPaint.setAntiAlias(true);
		}

		if (Linepaint == null)
		{
			Linepaint = new Paint();
			Linepaint.setAntiAlias(true);
			Linepaint.setColor(Global.getColor(R.attr.ListSeparator));
			KopfPaint = new Paint();
			KopfPaint.setAntiAlias(true);
			KopfPaint.setColor(Global.getColor(R.attr.EmptyBackground));
		}
		if (NamePaint == null)
		{
			NamePaint = new Paint();
			NamePaint.setFakeBoldText(true);
			NamePaint.setAntiAlias(true);
			NamePaint.setTextSize((float) (Sizes.getScaledFontSize() * 1.3));
			NamePaint.setColor(Global.getColor(R.attr.TextColor));
		}

		int m = Sizes.getMargin();

		if (headHeight < 1 || headLinePos < 1)
		{
			headHeight = (int) ((int) (layoutFinder.getHeight() * 1.5) + (Sizes.getCornerSize() * 1.7));
			headLinePos = (headHeight / 2) + (layoutFinder.getHeight() / 2) - m;
		}

		ActivityUtils.drawFillRoundRecWithBorder(canvas, new Rect(m, m, width - m, height - m), 2, Global.getColor(R.attr.ListSeparator),
				(BackColorChanger) ? Global.getColor(R.attr.ListBackground_secend) : Global.getColor(R.attr.ListBackground),
				Sizes.getCornerSize());

		// Kopfzeile

		final Rect KopfRect = new Rect(m + 1, m + 1, width - m - 1, headHeight);
		final RectF KopfRectF = new RectF(KopfRect);
		canvas.drawRoundRect(KopfRectF, Sizes.getCornerSize(), Sizes.getCornerSize(), KopfPaint);
		canvas.drawRect(new Rect(m + 1, headHeight - Sizes.getCornerSize(), width - m - 1, headHeight), KopfPaint);

		int space = (logEntry.TypeIcon >= 0) ? ActivityUtils.PutImageTargetHeight(canvas, Global.LogIcons[logEntry.TypeIcon],
				Sizes.getHalfCornerSize(), 8, headHeight - (Sizes.getCornerSize() * 2))
				+ m : 0;

		canvas.drawText(logEntry.Finder, space + Sizes.getHalfCornerSize(), headLinePos, NamePaint);

		NamePaint.setFakeBoldText(false);
		SimpleDateFormat postFormater = new SimpleDateFormat("dd/MM/yyyy");
		String dateString = postFormater.format(logEntry.Timestamp);
		int DateLength = (int) NamePaint.measureText(dateString);
		canvas.drawText(dateString, width - DateLength - (m * 3), headLinePos, NamePaint);

		// canvas.drawLine(5, headHeight - 2, width-5, headHeight -
		// 2,Linepaint);
		// canvas.drawLine(5, headHeight - 3, width-5, headHeight -
		// 3,Linepaint);

		// Körper
		ActivityUtils.drawStaticLayout(canvas, layoutComment, Sizes.getCornerSize(), headHeight + Sizes.getCornerSize());

	}

}
