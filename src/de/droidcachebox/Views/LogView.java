package de.droidcachebox.Views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

public class LogView extends View {

	private Paint paint;
	/**
	 * Constructor
	 */
	private String text;
	public LogView(Context context, String text) {
		super(context);
		this.text = text;
		paint = new Paint();
		// set's the paint's colour
		paint.setColor(Color.RED);
		// set's paint's text size
		paint.setTextSize(25);
		// smooth's out the edges of what is being drawn
		paint.setAntiAlias(true);
	}

	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		canvas.drawText(text, 5, 30, paint);
		// if the view is visible onDraw will be called at some point in the
		// future
		invalidate();
	}


}
