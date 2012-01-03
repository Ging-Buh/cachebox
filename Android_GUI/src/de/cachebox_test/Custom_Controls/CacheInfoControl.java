package de.cachebox_test.Custom_Controls;


import CB_Core.Config;
import de.cachebox_test.Global;
import de.cachebox_test.R;
import de.cachebox_test.UnitFormatter;

import de.cachebox_test.Components.CacheDraw;
import de.cachebox_test.Components.CacheDraw.DrawStyle;
import de.cachebox_test.Views.CacheListView;
import CB_Core.Types.Cache;
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


public final class CacheInfoControl extends View {

	public CacheInfoControl(Context context) {
		super(context);
		
	}

	public CacheInfoControl(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		
	}

	public CacheInfoControl(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
	}
	
	
	/*
	 *  Private Member
	 */
	
	private Cache aktCache;
    private int lineHeight = 37;
    private int imgSize = 37;
	private int height;
	private int width;
	private int rightBorder;
	private int ownBackgroundColor;
	private Boolean useOwnBackColor = false;
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		
		
		/*int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int widthSize = this.width = MeasureSpec.getSize(widthMeasureSpec);
		
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int heightSize = this.height = MeasureSpec.getSize(heightMeasureSpec);
		*/
		//int chosenWidth = chooseDimension(widthMode, widthSize);
		//int chosenHeight = chooseDimension(heightMode, heightSize);
		
		//int chosenDimension = Math.min(chosenWidth, chosenHeight);
		
		

		// Berechne Höhe so das 7 Einträge in die Liste passen
		this.width = measureWidth(widthMeasureSpec);
		
        //this.height = (int) chosenHeight - this.width;
		this.imgSize = (int) (this.height / 1.2);
        this.lineHeight = (int) this.height / 3;
        this.rightBorder =(int) (this.width / 6);
       
        
       // setMeasuredDimension(chosenDimension, chosenDimension);
        setMeasuredDimension(this.width, this.height);
	}
	
	
	
	private int chooseDimension(int mode, int size) {
		if (mode == MeasureSpec.AT_MOST || mode == MeasureSpec.EXACTLY) {
			return size;
		} else { // (mode == MeasureSpec.UNSPECIFIED)
			return getPreferredSize();
		} 
	}
	
	// in case there is no size specified
	private int getPreferredSize() {
		return 300;
	}
	
	
	
    /**
     * Determines the width of this view
     * @param measureSpec A measureSpec packed into an int
     * @return The width of the view, honoring constraints from measureSpec
     */
    private int measureWidth(int measureSpec) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            // We were told how big to be
            result = specSize;
        } else {
            // Measure the text
            result = (int) Global.Paints.Day.ListBackground.measureText(aktCache.Name) + getPaddingLeft()
                    + getPaddingRight();
            if (specMode == MeasureSpec.AT_MOST) {
                // Respect AT_MOST value if that was what is called for by measureSpec
                result = Math.min(result, specSize);
            }
        }
        
        return result;
    }


    private DrawStyle style = DrawStyle.withoutBearing;
    public void setStyle(DrawStyle newStyle)
    {
    	style=newStyle;
    }

	@Override
	protected void onDraw(Canvas canvas) {
	//	drawBackground(canvas);
		 super.onDraw(canvas);
        
		 if (aktCache == null)
			 return;
		 int UseColor;
		 if (useOwnBackColor)
			 {
			 	UseColor = ownBackgroundColor;
			 }
		 else
		 {
			 UseColor = Global.getColor(R.attr.ListBackground);
		 }
		 CacheDraw.DrawInfo(aktCache,canvas, width, height, UseColor, style);
	     
			
		canvas.restore();
			
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		//Log.d("Cachebox", "Size changed to " + w + "x" + h);
		
		
	}
	
	public void setCache(Cache cache, int BackGroundColor)
	{
		useOwnBackColor=true;
		ownBackgroundColor = BackGroundColor;
		if(cache!=null)
			aktCache = cache;
	}
	
	
	public void setCache(Cache cache)
	{
		useOwnBackColor=false;
		aktCache = cache;
	}
	
	public void setHeight(int MyHeight)
	{
		this.height = MyHeight;
		this.invalidate();
	}
	
}
