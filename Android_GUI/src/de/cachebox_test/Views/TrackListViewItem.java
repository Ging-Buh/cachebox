package de.cachebox_test.Views;

import CB_Core.Math.CB_Rect;
import CB_Core.Math.UiSizes;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.view.View;
import de.cachebox_test.Global;
import de.cachebox_test.R;
import de.cachebox_test.main;
import de.cachebox_test.Custom_Controls.ColorDialog.AmbilWarnaDialog;
import de.cachebox_test.Custom_Controls.ColorDialog.AmbilWarnaDialog.OnAmbilWarnaListener;
import de.cachebox_test.Map.RouteOverlay.Trackable;
import de.cachebox_test.Ui.ActivityUtils;

/**
 * Item der TrackListView zur Darstellung der RouteOverlay.Routes <br>
 * <br>
 * <br>
 * <img src="doc-files/TrackListViewItem.png" width=250 height=44>
 * 
 * @author Longri
 */
public class TrackListViewItem extends View
{
	private Trackable route;
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
	private static CB_Rect lColorBounds;
	private static CB_Rect lBounds;
	private static CB_Rect rBounds;
	private static CB_Rect rChkBounds;

	public TrackListViewItem(Context context, Trackable route, Boolean BackColorId)
	{
		super(context);
		this.route = route;
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
		if (this.route.ShowRoute)
		{
			Rect oldBounds = Global.Icons[27].getBounds();
			Global.Icons[27].setBounds(rChkBounds.getLeft(), rChkBounds.getBottom(), rChkBounds.getRight(), rChkBounds.getTop());
			Global.Icons[27].draw(canvas);
			Global.Icons[27].setBounds(oldBounds);
		}

		// Draw Color of Route
		int RouteColor = route.paint.getColor();
		if (lBounds == null || lColorBounds == null)
		{
			lBounds = new CB_Rect(7, 7, height - 7, height - 7);
			int halfSize = lBounds.getWidth() / 6;
			int corrRecSize = (lBounds.getWidth() - lBounds.getHeight()) / 2;
			lColorBounds = new CB_Rect(lBounds.getLeft() + halfSize, lBounds.getBottom() + halfSize - corrRecSize, lBounds.getRight()
					- halfSize, lBounds.getTop() - halfSize + corrRecSize);

		}

		ActivityUtils.drawFillRoundRecWithBorder(canvas, lColorBounds, 3, RouteColor, RouteColor, UiSizes.getCornerSize());

		left += lBounds.getWidth();

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
			if (route.Name == null || route.Name.equals(""))
			{
				Name = "no Name";
			}
			else
			{
				Name = route.Name;
			}
			int TextWidth = this.width - lBounds.getLeft() - (this.width - rBounds.getLeft());
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

	public void switchCheked()
	{
		this.route.ShowRoute = !this.route.ShowRoute;
	}

	public void changeColor()
	{
		// initialColor is the initially-selected color to be shown in the
		// rectangle on the left of the arrow.
		// for example, 0xff000000 is black, 0xff0000ff is blue. Please be aware
		// of the initial 0xff which is the alpha.
		AmbilWarnaDialog dialog = new AmbilWarnaDialog(main.mainActivity, route.paint.getColor(), new OnAmbilWarnaListener()
		{
			@Override
			public void onOk(AmbilWarnaDialog dialog, int color)
			{
				route.paint.setColor(color);
			}

			@Override
			public void onCancel(AmbilWarnaDialog dialog)
			{
				// cancel was selected by the user
			}
		});

		dialog.show();

	}

	public Trackable getRoute()
	{
		return route;
	}
}
