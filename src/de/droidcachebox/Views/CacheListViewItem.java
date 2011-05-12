package de.droidcachebox.Views;

import de.droidcachebox.Config;
import de.droidcachebox.Global;
import de.droidcachebox.R;
import de.droidcachebox.UnitFormatter;
import de.droidcachebox.Components.StringFunctions;
import de.droidcachebox.Events.PositionEvent;
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
import android.location.Location;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.LinearLayout;
import android.widget.AbsListView.LayoutParams;
import android.widget.RelativeLayout;

public class CacheListViewItem extends View {
    private Cache cache;
    private int mAscent;
    private int width;
    private int height;
    private int rightBorder;
    private boolean BackColorChanger = false;
       
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
    
    
    
    
	public CacheListViewItem(Context context, Cache cache, Boolean BackColorId) {
		// TODO Auto-generated constructor stub
		super(context);
        this.cache = cache;
        gcLogin = Config.GetString("GcLogin");
        BackColorChanger = BackColorId;
        
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
        
        Boolean GlobalSelected = cache == Global.SelectedCache();
        int BackgroundColor;
        if (BackColorChanger)
        {
        	BackgroundColor = (GlobalSelected)? Global.getColor(R.attr.ListBackground_select): Global.getColor(R.attr.ListBackground);
        }
        else
        {
        	BackgroundColor = (GlobalSelected)? Global.getColor(R.attr.ListBackground_select): Global.getColor(R.attr.ListBackground_secend);
        }
        
        cache.DrawInfo(canvas, height, width, imgSize, lineHeight, rightBorder, BackgroundColor, Cache.DrawStyle.all);
        
        
    }

}
