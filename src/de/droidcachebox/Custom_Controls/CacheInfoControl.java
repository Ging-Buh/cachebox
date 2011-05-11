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


public final class CacheInfoControl extends View {

	public CacheInfoControl(Context context) {
		super(context);
		init();
	}

	public CacheInfoControl(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
		TypedArray a = context.obtainStyledAttributes(attrs,
		        R.styleable.CompassControlStyle);

		
		/*rimColorFilter =  a.getColor(R.styleable.CompassControlStyle_Compass_rimColorFilter, rimColorFilter);*/
	
		
		a.recycle();
	}

	public CacheInfoControl(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
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
	private void init() {
		
		
	}

	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		/*Log.d("Cachebox", "Width spec: " + MeasureSpec.toString(widthMeasureSpec));
		Log.d("Cachebox", "Height spec: " + MeasureSpec.toString(heightMeasureSpec));
		
		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int widthSize = this.width = MeasureSpec.getSize(widthMeasureSpec);
		
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int heightSize = this.height = MeasureSpec.getSize(heightMeasureSpec);
		
		int chosenWidth = chooseDimension(widthMode, widthSize);
		int chosenHeight = chooseDimension(heightMode, heightSize);
		
		int chosenDimension = Math.min(chosenWidth, chosenHeight);
		*/
		

		// Berechne Höhe so das 7 Einträge in die Liste passen
        this.height = (int) 800 / 7;
		this.imgSize = (int) (this.height / 1.2);
        this.lineHeight = (int) this.height / 3;
       // this.rightBorder =(int) (this.height * 1.5);
        this.rightBorder =0;
        
       // setMeasuredDimension(chosenDimension, chosenDimension);
        setMeasuredDimension(measureWidth(widthMeasureSpec),this.height);
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
        width = specSize;
        return result;
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


	@Override
	protected void onDraw(Canvas canvas) {
	//	drawBackground(canvas);
		 super.onDraw(canvas);
        
		 if (aktCache == null)
			 return;
		 
	      	int x=0;
	      	int y=0;
	        Boolean notAvailable = (!aktCache.Available && !aktCache.Archived);
	        Boolean Night = Config.GetBool("nightMode");
	        Boolean GlobalSelected = aktCache == Global.SelectedCache();
	        int IconPos = imgSize - (int) (imgSize/1.5);
	        
	        
	        Paint DrawBackPaint = new Paint(Global.Paints.ListBackground);
	       
	        DrawBackPaint.setColor(Global.getColor(R.attr.ListBackground));
	       
	        canvas.drawPaint(DrawBackPaint);
		
	        
	        
	        
	        Paint DTPaint =  Night? Global.Paints.Night.Text.noselected: Global.Paints.Day.Text.noselected ;
	      	      
	        
	        if (aktCache.Rating > 0)
	            Global.PutImageTargetHeight(canvas, Global.StarIcons[(int)(aktCache.Rating * 2)], 0, y + lineHeight * 2 + lineHeight / 4, lineHeight / 2);

	       Paint NamePaint = new Paint( (GlobalSelected)? Night? Global.Paints.Night.Text.selected: Global.Paints.Day.Text.selected : Night? Global.Paints.Night.Text.selected: Global.Paints.Day.Text.selected);  
	       if(notAvailable)
	       {
		       NamePaint.setColor(Color.RED);
		       NamePaint.setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
	       }
	       
	       String[] WrapText = StringFunctions.TextWarpArray(aktCache.Name, 23);
	       
	       
	       String Line1 =WrapText[0];
	       
	       canvas.drawText(Line1, imgSize + 5, 27, NamePaint);
	       if (!StringFunctions.IsNullOrEmpty(WrapText[1]))
	       {
	    	   String Line2 =WrapText[1];
	    	   canvas.drawText(Line2, imgSize + 5, 50, NamePaint);
	       }
	          if (Global.LastValidPosition.Valid || Global.Marker.Valid)
	          {
	              Coordinate position = (Global.Marker.Valid) ? Global.Marker : Global.LastValidPosition;
	              double heading = (Global.Locator != null) ? Global.Locator.getHeading() : 0;

	              // FillArrow: Luftfahrt
	              // Bearing: Luftfahrt
	              // Heading: Im Uhrzeigersinn, Geocaching-Norm

	              double bearing = Coordinate.Bearing(position.Latitude, position.Longitude, aktCache.Latitude(), aktCache.Longitude());
	              double relativeBearing = bearing - heading;
	           //   double relativeBearingRad = relativeBearing * Math.PI / 180.0;

			        // Draw Arrow
			       Global.PutImageTargetHeight(canvas, Global.Arrows[1],relativeBearing,(int)( width - rightBorder/2) ,(int)(lineHeight /8), (int)(lineHeight*2.4));

			       canvas.drawText(UnitFormatter.DistanceString(aktCache.Distance()), width - rightBorder + 2, (int) ((lineHeight * 2) + (lineHeight/1.4)), DTPaint);
	         }
	       
	       
	      
	        Paint Linepaint = Night? Global.Paints.Night.ListSeperator : Global.Paints.Day.ListSeperator;
	        canvas.drawLine(x, y + this.height - 2, width, y + this.height - 2,Linepaint); 
	        canvas.drawLine(x, y + this.height - 3, width, y + this.height - 3,Linepaint);
	        
	          
	        
	        if (aktCache.MysterySolved())
	        {
	        	Global.PutImageTargetHeight(canvas, Global.CacheIconsBig[19], 0, 0, imgSize); 
	        }
	        else
	        {
	        	Global.PutImageTargetHeight(canvas, Global.CacheIconsBig[aktCache.Type.ordinal()], 0, 0, imgSize); 
	        }
	        
	        
	          if (aktCache.Found())
	          {
	        	  
	              Global.PutImageTargetHeight(canvas, Global.Icons[2], IconPos, IconPos, imgSize/2);//Smile
	          }
	              

	          if (aktCache.Favorit())
	         {
	            Global.PutImageTargetHeight(canvas, Global.Icons[19], 0, y, lineHeight);
	         }

	         

	          if (aktCache.Archived)
	          {
	             Global.PutImageTargetHeight(canvas, Global.Icons[24], 0, y, lineHeight);
	          }

	         if (aktCache.Owner.equals(Config.GetString("GcLogin")) && !(Config.GetString("GcLogin").equals("")))
	           {
	               Global.PutImageTargetHeight(canvas,Global.Icons[17], IconPos, IconPos, imgSize/2);
	           }


	        

	        //  if (cache.ListingChanged)
	        //  {
	        //      Global.PutImageTargetHeight(canvas, Global.MapIcons[22], 0, y + imgSize - 15, lineHeight);
	        //  }

	        
	        int left = x + imgSize + 5;
	        canvas.drawText("D",left,(int) ((lineHeight * 2) + (lineHeight/1.4) ) , DTPaint);
	          left += (DTPaint.getTextSize());

	            left += Global.PutImageTargetHeight(canvas, Global.StarIcons[(int)(aktCache.Difficulty * 2)], left, y + lineHeight * 2 + lineHeight / 4, lineHeight / 2);

	         left += (DTPaint.getTextSize());

	         canvas.drawText("T", left,(int) ((lineHeight * 2) + (lineHeight/1.4) ) , DTPaint);
	         left += (DTPaint.getTextSize());
	         left += Global.PutImageTargetHeight(canvas, Global.StarIcons[(int)(aktCache.Terrain * 2)], left, y + lineHeight * 2 + lineHeight / 4, lineHeight / 2);


	          int numTb = aktCache.NumTravelbugs;
	         if (numTb > 0)
	          {
	              int tbWidth = Global.PutImageTargetHeight(canvas, Global.Icons[0], width - rightBorder, y + lineHeight, lineHeight);

	              if (numTb > 1)
	            	  canvas.drawText("x" + String.valueOf(numTb), width - rightBorder + tbWidth+2, (int)( y + lineHeight + (lineHeight/1.4)) , DTPaint);
	          }
	        	
	      	
	         
	         // Wenn nicht Available dann Komplettes item aus Grauen
	         if (notAvailable)
	         {
	              Global.PutImageTargetHeight(canvas, Global.Icons[25], 0, y, lineHeight);
	              int Alpha = (GlobalSelected)? 100 : Night ? 50 : 160;
	              DrawBackPaint.setAlpha(Alpha);
	              canvas.drawRect(new Rect(0,0,this.width,this.height-2), DrawBackPaint);
	         }
			
		canvas.restore();
			
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		Log.d("Cachebox", "Size changed to " + w + "x" + h);
		
		
	}
	
	
	
	public void setCache(Cache cache)
	{
		aktCache = cache;
	}
	
}
