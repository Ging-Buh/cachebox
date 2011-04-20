package de.droidcachebox.Views;

import de.droidcachebox.Config;
import de.droidcachebox.Global;
import de.droidcachebox.R;
import de.droidcachebox.Components.StringFunctions;
import de.droidcachebox.Geocaching.Cache;
import de.droidcachebox.Geocaching.Coordinate;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.LinearLayout;
import android.widget.AbsListView.LayoutParams;

public class CacheListViewItem extends View {
    private Cache cache;
    private int mAscent;
    private int width;
    private int height;
    private int rightBorder;
    
    
    /// <summary>
    /// Höhe einer Zeile auf dem Zielgerät
    /// </summary>
    private int lineHeight = 37;
    private int imgSize = 37;

    /// <summary>
    /// Spiegelung des Logins bei Gc, damit ich das nicht dauernd aus der
    /// Config lesen muss.
    /// </summary>
    String gcLogin = "";
    
    
    
    
	public CacheListViewItem(Context context, Cache cache) {
		// TODO Auto-generated constructor stub
		super(context);
        this.cache = cache;
        gcLogin = Config.GetString("GcLogin");
        this.setBackgroundColor(Config.GetBool("nightMode")? Global.Colors.Night.ListBackground : Global.Colors.Day.ListBackground);
       }

	
	@Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
       
        
        
        // Berechne Höhe so das 7 Einträge in die Liste passen
        this.height = (int) CacheListView.windowH / 7;
        this.imgSize = (int) (this.height / 1.2);
        this.lineHeight = (int) this.height / 3;
        this.rightBorder =(int) (this.height * 1.5);
        
        setMeasuredDimension(measureWidth(widthMeasureSpec),this.height);
              //  measureHeight(heightMeasureSpec));
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
            result = (int) Global.Paints.Day.ListBackground.measureText(cache.Name) + getPaddingLeft()
                    + getPaddingRight();
            if (specMode == MeasureSpec.AT_MOST) {
                // Respect AT_MOST value if that was what is called for by measureSpec
                result = Math.min(result, specSize);
            }
        }
        width = specSize;
        return result;
    }

    /**
     * Determines the height of this view
     * @param measureSpec A measureSpec packed into an int
     * @return The height of the view, honoring constraints from measureSpec
     */
    private int measureHeight(int measureSpec) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        mAscent = (int) Global.Paints.Day.ListBackground.ascent();
        if (specMode == MeasureSpec.EXACTLY) {
            // We were told how big to be
            result = specSize;
        } else {
            // Measure the text (beware: ascent is a negative number)
            result = (int) (-mAscent + Global.Paints.Day.ListBackground.descent()) + getPaddingTop()
                    + getPaddingBottom();
            if (specMode == MeasureSpec.AT_MOST) {
                // Respect AT_MOST value if that was what is called for by measureSpec
                result = Math.min(result, specSize);
            }
        }
     
        return result;
    }
    
    
    
    
    
    
   static double fakeBearing =0;
    
    /**
     * Render the text
     * 
     * @see android.view.View#onDraw(android.graphics.Canvas)
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
           	
      	int x=0;
      	int y=0;
        Boolean notAvailable = (!cache.Available && !cache.Archived);
        Boolean Night = Config.GetBool("nightMode");
        Boolean GlobalSelected = cache == Global.SelectedCache();
        
        Paint DrawBackPaint = new Paint( (GlobalSelected)? Global.Paints.Day.selectedBack : Night? Global.Paints.Night.ListBackground : Global.Paints.Day.ListBackground);
      	canvas.drawPaint(DrawBackPaint);
       
        
        if (cache.Rating > 0)
            Global.PutImageTargetHeight(canvas, Global.StarIcons[(int)(cache.Rating * 2)], 0, y + lineHeight * 2 + lineHeight / 4, lineHeight / 2);

       Paint NamePaint = new Paint( (GlobalSelected)? Night? Global.Paints.Night.Text.selected: Global.Paints.Day.Text.selected : Night? Global.Paints.Night.Text.selected: Global.Paints.Day.Text.selected);  
       if(notAvailable)
       {
	       NamePaint.setColor(Color.RED);
	       NamePaint.setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
       }
       
       String[] WrapText = StringFunctions.TextWarpArray(cache.Name, 23);
       
       
       String Line1 =WrapText[0];
       
       canvas.drawText(Line1, imgSize + 5, 27, NamePaint);
       if (!StringFunctions.IsNullOrEmpty(WrapText[1]))
       {
    	   String Line2 =WrapText[1];
    	   canvas.drawText(Line2, imgSize + 5, 50, NamePaint);
       }
        //  if (Global.LastValidPosition.Valid || Global.Marker.Valid)
        //  {
        //      Coordinate position = (Global.Marker.Valid) ? Global.Marker : Global.LastValidPosition;
        //      double heading = (Global.Locator != null) ? Global.Locator.Heading : 0;

        //      // FillArrow: Luftfahrt
        //      // Bearing: Luftfahrt
        //      // Heading: Im Uhrzeigersinn, Geocaching-Norm

        //      double bearing = -Coordinate.Bearing(position.Latitude, position.Longitude, cache.Latitude, cache.Longitude);
        //      double relativeBearing = bearing - heading + 180;
        //      double relativeBearingRad = relativeBearing * Math.PI / 180.0;

        //      Arrow.FillArrow(gfx, Arrow.HeadingBug, blackPen, (Global.NearestCache == cache) ? redBrush : orangeBrush, width - lineHeight, y + lineHeight, lineHeight, -relativeBearingRad);

        //      gfx.DrawString(UnitFormatter.DistanceString(cache.Distance), font, blackBrush, width - rightBorder + 2, y + lineHeight * 2);
        //  }
       
       
       // Draw Arrow
       Global.PutImageTargetHeight(canvas, Global.Arrows[1],fakeBearing++,(int)( width - rightBorder/2) ,(int)(lineHeight /8), (int)(lineHeight*2.4));

        Paint Linepaint = Night? Global.Paints.Night.ListSeperator : Global.Paints.Day.ListSeperator;
        canvas.drawLine(x, y + this.height - 2, width, y + this.height - 2,Linepaint); 
        canvas.drawLine(x, y + this.height - 3, width, y + this.height - 3,Linepaint);
        
          Global.PutImageTargetHeight(canvas, Global.CacheIconsBig[cache.Type.ordinal()], 0, 0, imgSize);     
          if (cache.Found())
          {
        	  int pos =imgSize - (int) (imgSize/1.5);
              Global.PutImageTargetHeight(canvas, Global.Icons[2], pos, pos, imgSize/2);//Smile
          }
              

          if (cache.Favorit())
         {
            Global.PutImageTargetHeight(canvas, Global.Icons[19], 0, y, lineHeight);
         }

         

          if (cache.Archived)
          {
             Global.PutImageTargetHeight(canvas, Global.Icons[24], 0, y, lineHeight);
          }

         if (cache.Owner == gcLogin && !(gcLogin == ""))
           {
               Global.PutImageTargetHeight(canvas,Global.NewMapIcons.get(2).get(20), 0, y, lineHeight);
           }


        //  if (cache.MysterySolved && !cache.Archived)
        //  {
        //      Global.PutImageTargetHeight(canvas, Global.MapIcons[21], 0, y, lineHeight);
        //  }

        //  if (cache.ListingChanged)
        //  {
        //      Global.PutImageTargetHeight(canvas, Global.MapIcons[22], 0, y + imgSize - 15, lineHeight);
        //  }

         Paint DTPaint =  Night? Global.Paints.Night.Text.noselected: Global.Paints.Day.Text.noselected ;
        int left = x + imgSize + 5;
        canvas.drawText("D",left,(int) ((lineHeight * 2) + (lineHeight/1.4) ) , DTPaint);
          left += (DTPaint.getTextSize());

            left += Global.PutImageTargetHeight(canvas, Global.StarIcons[(int)(cache.Difficulty * 2)], left, y + lineHeight * 2 + lineHeight / 4, lineHeight / 2);

         left += (DTPaint.getTextSize());

         canvas.drawText("T", left,(int) ((lineHeight * 2) + (lineHeight/1.4) ) , DTPaint);
         left += (DTPaint.getTextSize());
         left += Global.PutImageTargetHeight(canvas, Global.StarIcons[(int)(cache.Terrain * 2)], left, y + lineHeight * 2 + lineHeight / 4, lineHeight / 2);


          int numTb = cache.NumTravelbugs;
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
    }



}
