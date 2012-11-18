package de.droidcachebox.Views;

import CB_Core.Math.CB_Rect;
import CB_Core.Math.UiSizes;
import CB_Core.Types.Trackable;
import android.content.Context;
import android.graphics.Canvas;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.view.View;
import de.droidcachebox.Global;
import de.droidcachebox.R;
import de.droidcachebox.Ui.ActivityUtils;

/**
 * Item der TrackListView zur Darstellung der RouteOverlay.Routes <br>
 * <br>
 * <br>
 * <img src="doc-files/TrackListViewItem.png" width=250 height=44>
 * 
 * @author Longri
 */
public class TrackableListViewItem extends View
{
	private Trackable trackable;
	private int width;
	private int height;
	private boolean BackColorChanger = false;
	private StaticLayout LayoutName;

	// static Member
	private static TextPaint textPaint;

	// private Member
	int left;
	int top;
	int BackgroundColor;

	private static CB_Rect rBounds;
	private static CB_Rect rChkBounds;

	public TrackableListViewItem(Context context, CB_Core.Types.Trackable trackable2, Boolean BackColorId)
	{
		super(context);
		this.trackable = trackable2;
		BackColorChanger = BackColorId;
	}

	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{

		measureWidth(widthMeasureSpec);

		height = UiSizes.getIconSize() + (UiSizes.getCornerSize() * 2);

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
		width = specSize;
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

		// initial
		left = UiSizes.getCornerSize();
		top = UiSizes.getCornerSize();
		canvas.drawColor(Global.getColor(R.attr.myBackground));

		if (BackColorChanger)
		{
			BackgroundColor = (isSelected()) ? Global.getColor(R.attr.ListBackground_select) : Global.getColor(R.attr.ListBackground);
		}
		else
		{
			BackgroundColor = (isSelected()) ? Global.getColor(R.attr.ListBackground_select) : Global
					.getColor(R.attr.ListBackground_secend);
		}

		int LineColor = Global.getColor(R.attr.ListSeparator);
		CB_Rect DrawingRec = new CB_Rect(5, 5, width - 5, height - 5);
		ActivityUtils.drawFillRoundRecWithBorder(canvas, DrawingRec, 2, LineColor, BackgroundColor);

		drawRightChkBox(canvas);

		// Draw Route Name
		if (textPaint == null)
		{
			textPaint = new TextPaint();
			textPaint.setAntiAlias(true);
			textPaint.setFakeBoldText(true);
			textPaint.setTextSize((float) (UiSizes.getScaledFontSize() * 1.3));
			textPaint.setColor(Global.getColor(R.attr.TextColor));
		}

		if (LayoutName == null)
		{
			String Name = "";
			if (trackable.getName() == null || trackable.getName().equals(""))
			{
				Name = "no Name";
			}
			else
			{
				Name = trackable.getName();
			}
			int TextWidth = this.width - (this.width - rBounds.getLeft());
			LayoutName = new StaticLayout(Name, textPaint, TextWidth, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);

		}

		top = (this.height - LayoutName.getHeight()) / 2;
		left += ActivityUtils.drawStaticLayout(canvas, LayoutName, left, top);

	}

	private void drawRightChkBox(Canvas canvas)
	{
		if (rBounds == null || rChkBounds == null)
		{
			rBounds = new CB_Rect(width - height - 7, 7, width - 7, height - 7);// =
			// right
			// Button
			// bounds
			int halfSize = rBounds.getWidth() / 4;
			int corrRecSize = (rBounds.getWidth() - rBounds.getHeight()) / 2;
			rChkBounds = new CB_Rect(rBounds.getLeft() + halfSize, rBounds.getBottom() + halfSize - corrRecSize, rBounds.getRight()
					- halfSize, rBounds.getTop() - halfSize + corrRecSize);
		}
		ActivityUtils.drawFillRoundRecWithBorder(canvas, rChkBounds, 3, Global.getColor(R.attr.ListSeparator), BackgroundColor,
				UiSizes.getCornerSize());
	}

}
