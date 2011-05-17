package de.droidcachebox.Components;

import de.droidcachebox.Global;
import de.droidcachebox.R;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
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

	public static void drawFillRoundRecWithBorder(Canvas canvas, Rect rec, int BorderSize, int BorderColor, int FillColor)
	{
		drawFillRoundRecWithBorder(canvas, rec, BorderSize, BorderColor, FillColor, Global.scaledFontSize_normal);
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
	
	

    /// <summary>
    /// Zeichnet das Bild und skaliert es proportional so, dass es die
    /// übergebene füllt.
    /// </summary>
    /// <param name="graphics"></param>
    /// <param name="image"></param>
    /// <param name="x"></param>
    /// <param name="y"></param>
    /// <param name="height"></param>
    public static int PutImageTargetHeight(Canvas canvas, Drawable image, int x, int y, int height)
    {
       // float scale = (float)height / (float)image.getBounds().height();
       // int width = (int)Math.round(image.getBounds().width() * scale);
        
        float scale = (float)height / (float)image.getIntrinsicHeight();
        int width = (int)Math.round(image.getIntrinsicWidth() * scale);

        Rect oldBounds = image.getBounds();
        image.setBounds(x, y, x + width, y + height);
        image.draw(canvas);
        image.setBounds(oldBounds);

        return width;
    }
    
    
  /// <summary>
    /// Zeichnet das Bild und skaliert es proportional so, dass es die
    /// übergebene füllt.
    /// </summary>
    /// <param name="graphics"></param>
    /// <param name="image"></param>
    /// <param name="x"></param>
    /// <param name="y"></param>
    /// <param name="height"></param>
    public static int PutImageTargetHeight(Canvas canvas, Drawable image,double Angle, int x, int y, int height)
    {
    	Bitmap bmp = ((BitmapDrawable)image).getBitmap();
    	
    	// Getting width & height of the given image.
    	int w = bmp.getWidth();
    	int h = bmp.getHeight();
    	// Setting post rotate to 90
    	Matrix mtx = new Matrix();
    	mtx.postRotate((float) Angle);
    	// Rotating Bitmap
    	Bitmap rotatedBMP = Bitmap.createBitmap(bmp, 0, 0, w, h, mtx, true);
    	BitmapDrawable bmd = new BitmapDrawable(rotatedBMP);

    	
        
        float scale = (float)height / (float)bmd.getIntrinsicHeight();
        int width = (int)Math.round(bmd.getIntrinsicWidth() * scale);

        Rect oldBounds = bmd.getBounds();
        bmd.setBounds(x, y, x + width, y + height);
        bmd.draw(canvas);
        bmd.setBounds(oldBounds);

        return width;
    }
    


}
