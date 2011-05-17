package de.droidcachebox.Components;

import de.droidcachebox.Global;
import de.droidcachebox.R;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.StaticLayout;

public class ActivityUtils
{
	private static int sTheme = 1;

	public final static int THEME_DEFAULT = 0;
	public final static int THEME_DAY = 1;
	public final static int THEME_NIGHT = 2;

	/**
	 * Set the theme of the Activity, and restart it by creating a new Activity
	 * of the same type.
	 */
	public static void changeToTheme(Activity activity, int theme)
	{
		sTheme = theme;
		activity.finish();

		activity.startActivity(new Intent(activity, activity.getClass()));
	}

	/** Set the theme of the activity, according to the configuration. */
	public static void onActivityCreateSetTheme(Activity activity)
	{
		switch (sTheme)
		{
		default:
		case THEME_DEFAULT:
			break;
		case THEME_DAY:
			activity.setTheme(R.style.Theme_day);
			break;
		case THEME_NIGHT:
			activity.setTheme(R.style.Theme_night);
			break;
		
		}
		
		Global.initTheme(activity);	
		
	}


	public static int drawStaticLayout(Canvas canvas,StaticLayout layout,int x, int y)
	{
		canvas.translate(x,y);
		layout.draw(canvas);
     	canvas.translate(-x, -y);
     	return layout.getHeight();
	}

	public static void drawFillRoundRecWithBorder(Canvas canvas, Rect rec, int BorderSize, int BorderColor, int FillColor, int CornerSize)
	{
		Paint drawPaint = new Paint();
		drawPaint.setAntiAlias(true);
		
	    final Rect outerRect = rec;
	    final RectF OuterRectF = new RectF(outerRect);

	    drawPaint.setColor(BorderColor);
	    canvas.drawRoundRect( OuterRectF,CornerSize,CornerSize, drawPaint);
	    
	    
	    
	    final Rect rect = new Rect(rec.left+BorderSize,rec.top+BorderSize, rec.right-BorderSize,rec.bottom-BorderSize);
	    final RectF rectF = new RectF(rect);
	       
	    drawPaint.setColor(FillColor);
	    canvas.drawRoundRect( rectF,CornerSize-BorderSize,CornerSize-BorderSize, drawPaint);

	}


}
