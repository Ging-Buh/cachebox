package de.droidcachebox.Components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import de.droidcachebox.Global;

public class CacheNameView extends View
{

	public CacheNameView(Context context)
	{
		super(context);

	}

	public CacheNameView(Context context, AttributeSet attrs)
	{
		super(context, attrs);

	}

	private static int height;
	private int width;

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		this.width = measure(widthMeasureSpec);
		height = measure(heightMeasureSpec);

		setMeasuredDimension(this.width, height);
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

	protected void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);

		final Drawable Slide = Global.BtnIcons[0];
		Rect mRect = new Rect();
		mRect.set(-10, -2, width + 10, height + 2);
		Slide.setBounds(mRect);
		Slide.draw(canvas);

	}

	// public void setHeight(int newheight)
	// {
	// height = newheight;
	// setMeasuredDimension(this.width, height);
	// }

	public static int getMyHeight()
	{
		return height;
	}

}
