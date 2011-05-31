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
import android.graphics.PorterDuffColorFilter;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;

import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.MeasureSpec;



/*
 * Control Tamplate zum Copieren!
 * 
 * XML Layout einbindung über :
 * 
    <de.droidcachebox.Custom_Controls.downSlider
			android:id="@+id/myName" android:layout_height="wrap_content"
			android:layout_width="fill_parent" android:layout_marginLeft="2dip"
			android:layout_marginRight="2dip" android:layout_marginTop="1dip" />
 */





public final class downSlider extends View 
{

	public downSlider(Context context) 
	{
		super(context);
	}

	public downSlider(Context context, AttributeSet attrs) 
	{
		super(context, attrs);
		
	}

	public downSlider(Context context, AttributeSet attrs, int defStyle) 
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

    int yPos =100;

	@Override
	protected void onDraw(Canvas canvas) 
	{
	
		final Drawable Slide = Global.BtnIcons[0];
		Rect mRect = new Rect();
		mRect.set(-2, yPos, width+2 , yPos+50);
		Slide.setBounds(mRect);
   	 	
		Slide.setColorFilter(new PorterDuffColorFilter(Global.getColor(R.attr.SlideDownColorFilter), android.graphics.PorterDuff.Mode.MULTIPLY ));
   	 	Slide.draw(canvas);
		
		//canvas.drawColor(Color.RED);
		
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
	
	public void setPos(int Pos)
	{
		yPos=Pos;
		this.invalidate();
	}
	
}
