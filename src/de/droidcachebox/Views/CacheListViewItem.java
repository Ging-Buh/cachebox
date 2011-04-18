package de.droidcachebox.Views;

import de.droidcachebox.Config;
import de.droidcachebox.Global;
import de.droidcachebox.R;
import de.droidcachebox.Geocaching.Cache;
import de.droidcachebox.Geocaching.Coordinate;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.LinearLayout;

public class CacheListViewItem extends View {
    private Cache cache;
    private int mAscent;
    private int width;
    private int height;
    
    
    
    /// <summary>
    /// Höhe einer Zeile auf dem Zielgerät
    /// </summary>
    private int lineHeight = 16;
    
    private int imgSize = 16;

	public CacheListViewItem(Context context, Cache cache) {
		// TODO Auto-generated constructor stub
		super(context);
        this.cache = cache;
        
        this.setBackgroundColor(Config.GetBool("nightMode")? Global.Colors.Night.ListBackground : Global.Colors.Day.ListBackground);
       }

	
	@Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(measureWidth(widthMeasureSpec),75);
              //  measureHeight(heightMeasureSpec));
        this.height = 75;
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
        height = result;
        return result;
    }
    /**
     * Render the text
     * 
     * @see android.view.View#onDraw(android.graphics.Canvas)
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
       
        //canvas.drawRect(new Rect(0, 0, width, 75), Global.ListItemBackgroundPaint[paintID]);

        //canvas.drawText(cache.Name, 5, 30, Global.ListItemTextPaint[paintID]);
      	//Paint tmpPaint = new Paint(Global.ListItemTextPaint[paintID]);
      	//tmpPaint.setTextAlign(Align.RIGHT);
      	//	canvas.drawText(Float.toString(cache.Distance()), width - 10, 70, tmpPaint);
      	
      	// Drawing Ported 
      	
      	int x=0;
      	int y=0;
        
        
        Paint DrawBackPaint = new Paint( (cache == Global.SelectedCache())? Global.Paints.Day.selectedBack : Config.GetBool("nightMode")? Global.Paints.Night.ListBackground : Global.Paints.Day.ListBackground);
      	canvas.drawPaint(DrawBackPaint);
       
        
        if (cache.Rating > 0)
            Global.PutImageTargetHeight(canvas, Global.SmallStarIcons[(int)(cache.Rating * 2)], 0, y + imgSize, lineHeight / 2);

       Paint NamePaint = (cache == Global.SelectedCache())? Config.GetBool("nightMode")? Global.Paints.Night.Text.selected: Global.Paints.Day.Text.selected : Config.GetBool("nightMode")? Global.Paints.Night.Text.noselected: Global.Paints.Day.Text.noselected;  
       canvas.drawText(cache.Name, 5, 30, NamePaint);

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

        Paint Linepaint = Config.GetBool("nightMode")? Global.Paints.Night.ListSeperator : Global.Paints.Day.ListSeperator;
        canvas.drawLine(x, y + this.height - 2, width, y + this.height - 2,Linepaint); 
        canvas.drawLine(x, y + this.height - 3, width, y + this.height - 3,Linepaint);
        
               
          if (cache.Found()) // Todo Longri Draw Icons width Transparence
              Global.PutImageTargetHeight(canvas, Global.CacheIconsBigFound[cache.Type.ordinal()], 0, 30, imgSize);
          else
              Global.PutImageTargetHeight(canvas, Global.CacheIconsBig[cache.Type.ordinal()], 0, 30, imgSize);

          if (cache.Favorit())
         {
            Global.PutImageTargetHeight(canvas, Global.Icons[19], 0, y, lineHeight);
         }

          if (!cache.Available && !cache.Archived)
         {
              Global.PutImageTargetHeight(canvas, Global.Icons[25], 0, y, lineHeight);
         }

          if (cache.Archived)
          {
             Global.PutImageTargetHeight(canvas, Global.Icons[24], 0, y, lineHeight);
          }

        //  if (cache.Owner == gcLogin && !String.IsNullOrEmpty(gcLogin))
          //  {
          //      Global.PutImageTargetHeight(canvas, Global.MapIcons[20], 0, y, lineHeight);
          //  }


        //  if (cache.MysterySolved && !cache.Archived)
        //  {
        //      Global.PutImageTargetHeight(gfx, Global.MapIcons[21], 0, y, lineHeight, colorKeyIcons);
        //  }

        //  if (cache.ListingChanged)
        //  {
        //      Global.PutImageTargetHeight(gfx, Global.MapIcons[22], 0, y + imgSize - 15, lineHeight, colorKeyIcons);
        //  }


        //  int left = x + imgSize + 3;
        //  gfx.DrawString("D", font, blackBrush, left, y + lineHeight * 2);
        //  left += lineHeight / 2;

        //  if (cache == Global.SelectedCache)
        //      left += Global.PutImageTargetHeight(gfx, Global.StarIcons[(int)(cache.Difficulty * 2)], left, y + lineHeight * 2 + lineHeight / 4, lineHeight / 2, colorKeyStarIcons);
        //  else
        //      left += Global.PutImageTargetHeight(gfx, Global.StarIcons[(int)(cache.Difficulty * 2)], left, y + lineHeight * 2 + lineHeight / 4, lineHeight / 2);

        //  left += lineHeight;

        //  gfx.DrawString("T", font, blackBrush, left, y + lineHeight * 2);
        //  left += lineHeight / 2;
        //  if (cache == Global.SelectedCache)
        //      left += Global.PutImageTargetHeight(gfx, Global.StarIcons[(int)(cache.Terrain * 2)], left, y + lineHeight * 2 + lineHeight / 4, lineHeight / 2, colorKeyStarIcons);
        //  else
        //      left += Global.PutImageTargetHeight(gfx, Global.StarIcons[(int)(cache.Terrain * 2)], left, y + lineHeight * 2 + lineHeight / 4, lineHeight / 2);


        //  int numTb = cache.NumTravelbugs;
        //  if (numTb > 0)
        //  {
        //      int tbWidth = Global.PutImageTargetHeight(gfx, Global.Icons[0], width - rightBorder, y + lineHeight, lineHeight);

        //      if (numTb > 1)
        //          gfx.DrawString("x" + numTb.ToString(), font, blackBrush, width - rightBorder + tbWidth, y + lineHeight + 1);
        //  }
        	
      	
      	
      	
      	
    }
}
