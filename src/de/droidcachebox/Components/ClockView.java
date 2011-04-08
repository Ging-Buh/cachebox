package de.droidcachebox.Components;

import de.droidcachebox.Global;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

public class ClockView extends View {

	private Paint paint;
	
	public ClockView(Context context) {
		super(context);

		// TODO Auto-generated constructor stub
		paint = new Paint();
		// set's the paint's colour
		paint.setColor(Global.TitleBarText);
		// set's paint's text size
		paint.setTextSize(25);
		// smooth's out the edges of what is being drawn
		paint.setAntiAlias(true);
		
		this.setBackgroundColor(Global.TitleBarColor);
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
	    super.onMeasure(widthMeasureSpec, heightMeasureSpec);

	    Rect bounds = new Rect();
	    paint.getTextBounds("88:88", 0, 5, bounds);
	    int parentWidth = bounds.width() + 10;
	    int parentHeight = MeasureSpec.getSize(heightMeasureSpec);
	    this.setMeasuredDimension(
	            parentWidth, parentHeight);
	}	
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

//		canvas.drawRect(canvas.getClipBounds(), background);
		canvas.drawText("14:43", 5, 30, paint);
		// if the view is visible onDraw will be called at some point in the
		// future
		invalidate();
	}

}
