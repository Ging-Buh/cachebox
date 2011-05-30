package de.droidcachebox.Custom_Controls;


import de.droidcachebox.Config;
import de.droidcachebox.Global;
import de.droidcachebox.R;
import de.droidcachebox.UnitFormatter;
import de.droidcachebox.Components.StringFunctions;
import de.droidcachebox.Geocaching.Cache;
import de.droidcachebox.Geocaching.Coordinate;
import de.droidcachebox.Views.CacheListView;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;

import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.MeasureSpec;



/*
 * Control Tamplate zum Copieren!
 * 
 * XML Layout einbindung über :
 * 
    <de.droidcachebox.Custom_Controls.Control_Tamplate
			android:id="@+id/myName" android:layout_height="wrap_content"
			android:layout_width="fill_parent" android:layout_marginLeft="2dip"
			android:layout_marginRight="2dip" android:layout_marginTop="1dip" />
 */





public final class Control_Tamplate extends View 
{

	public Control_Tamplate(Context context) 
	{
		super(context);
	}

	public Control_Tamplate(Context context, AttributeSet attrs) 
	{
		super(context, attrs);
	}

	public Control_Tamplate(Context context, AttributeSet attrs, int defStyle) 
	{
		super(context, attrs, defStyle);
	}
	
	
	/*
	 *  Private Member
	 */
	private int height;
	private int width;
	
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) 
	{
		this.width = measure(widthMeasureSpec);
		this.height = measure(heightMeasureSpec);
		
        
      
        setMeasuredDimension(this.width, this.height);
	}
	
	
	
    /**
     * Determines the width of this view
     * @param measureSpec A measureSpec packed into an int
     * @return The width of the view, honoring constraints from measureSpec
     */
    private int measure(int measureSpec) 
    {
        int result = 0;
        
        int specSize = MeasureSpec.getSize(measureSpec);

       
            result = specSize;
        
        
        return result;
    }



	@Override
	protected void onDraw(Canvas canvas) 
	{
	
		 super.onDraw(canvas);
        
		 canvas.drawColor(Color.RED);
			
		canvas.restore();
			
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) 
	{
		Log.d("Cachebox", "Size changed to " + w + "x" + h);
	}
	
	public void setHeight(int MyHeight)
	{
		this.height = MyHeight;
		this.invalidate();
	}
	
}
