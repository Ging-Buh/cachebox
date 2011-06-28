package de.droidcachebox.Components;

import de.droidcachebox.Global;
import de.droidcachebox.R;
import de.droidcachebox.main;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.PorterDuff.Mode;
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
		main.isRestart=true;
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
		drawFillRoundRecWithBorder(canvas, rec, BorderSize, BorderColor, FillColor, Global.CornerSize);
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
        int width = (int)Math.round((float)image.getIntrinsicWidth() * scale);

        Rect oldBounds = image.getBounds();
        image.setBounds(x, y, x + width, y + height);
        image.draw(canvas);
        image.setBounds(oldBounds);

        return width;
    }
    
    
    public static int PutImageTargetHeightColor(Canvas canvas, Drawable image, int x, int y, int height, int color)
    {
       // float scale = (float)height / (float)image.getBounds().height();
       // int width = (int)Math.round(image.getBounds().width() * scale);
        
        float scale = (float)height / (float)image.getIntrinsicHeight();
        int width = (int)Math.round(image.getIntrinsicWidth() * scale);

        Rect oldBounds = image.getBounds();
        image.setBounds(x, y, x + width, y + height);
        image.setColorFilter(color, Mode.MULTIPLY);
        image.draw(canvas);
        image.setBounds(oldBounds);
        image.clearColorFilter();
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
    public static int PutImageTargetHeight(Canvas canvas, Drawable image,double Angle, int x, int y, int newHeight)
    {

    	float scale = (float)newHeight / (float)image.getIntrinsicHeight();
    	float newWidth = (int)Math.round((float)image.getIntrinsicWidth() * scale);
    	
    	Bitmap bmp = ((BitmapDrawable)image).getBitmap();
    	int width = bmp.getWidth();
    	int height = bmp.getHeight();
    	

    	
    	float scaleWidth = ((float) newWidth) / width;
    	float scaleHeight = ((float) newHeight) / height;
    	 // createa matrix for the manipulation
    	Matrix matrix = new Matrix();
    	 // resize the bit map
    	matrix.postScale(scaleWidth, scaleHeight);
    	 // rotate the Bitmap
    	matrix.postRotate((float) Angle);
    	 // recreate the new Bitmap
    	Bitmap resizedBitmap = Bitmap.createBitmap(bmp, 0, 0,
    	                   width, height, matrix, true);
    	 // make a Drawable from Bitmap to allow to set the BitMap
    	 // to the ImageView, ImageButton or what ever
    	 BitmapDrawable bmd = new BitmapDrawable(resizedBitmap);


    	
        
        bmd.setBounds(x, y,x+ bmd.getIntrinsicWidth(),y+ bmd.getIntrinsicHeight());
        bmd.draw(canvas);
       

        return bmd.getIntrinsicWidth();

    }
    
    
    
    public static int PutImageScale(Canvas canvas, Drawable image,double Angle, int x, int y, double scale)
    {
    	
    	if(scale==0.0) return 0;

    	float newWidth = (int)Math.round((float)image.getIntrinsicWidth() * scale);
    	float newHeight = (int)Math.round((float)image.getIntrinsicHeight() * scale);
    	
    	Bitmap bmp = ((BitmapDrawable)image).getBitmap();
    	int width = bmp.getWidth();
    	int height = bmp.getHeight();
    	

    	
    	float scaleWidth = ((float) newWidth) / width;
    	float scaleHeight = ((float) newHeight) / height;
    	 // createa matrix for the manipulation
    	Matrix matrix = new Matrix();
    	 // resize the bit map
    	matrix.postScale(scaleWidth, scaleHeight);
    	 // rotate the Bitmap
    	matrix.postRotate((float) Angle);
    	 // recreate the new Bitmap
    	Bitmap resizedBitmap = Bitmap.createBitmap(bmp, 0, 0,
    	                   width, height, matrix, true);
    	 // make a Drawable from Bitmap to allow to set the BitMap
    	 // to the ImageView, ImageButton or what ever
    	 BitmapDrawable bmd = new BitmapDrawable(resizedBitmap);


    	
        
        bmd.setBounds(x, y,x+ bmd.getIntrinsicWidth(),y+ bmd.getIntrinsicHeight());
        bmd.draw(canvas);
       

        return bmd.getIntrinsicWidth();

    }
    


}
