package de.droidcachebox.Views;

import de.droidcachebox.Global;
import de.droidcachebox.Geocaching.Cache;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.LinearLayout;

public class CacheListViewItem extends View {
    private Cache cache;
    private int mAscent;
    private int width;

	public CacheListViewItem(Context context, Cache cache) {
		// TODO Auto-generated constructor stub
		super(context);
        this.cache = cache;
        
        this.setBackgroundColor(Global.ListItemBackgroundPaint[0].getColor());
       }

	
	@Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(measureWidth(widthMeasureSpec),
                /*measureHeight(heightMeasureSpec)*/76);
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
            result = (int) Global.ListItemBackgroundPaint[0].measureText(cache.Name) + getPaddingLeft()
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

        mAscent = (int) Global.ListItemBackgroundPaint[0].ascent();
        if (specMode == MeasureSpec.EXACTLY) {
            // We were told how big to be
            result = specSize;
        } else {
            // Measure the text (beware: ascent is a negative number)
            result = (int) (-mAscent + Global.ListItemBackgroundPaint[0].descent()) + getPaddingTop()
                    + getPaddingBottom();
            if (specMode == MeasureSpec.AT_MOST) {
                // Respect AT_MOST value if that was what is called for by measureSpec
                result = Math.min(result, specSize);
            }
        }
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
        int paintID = 0;
        if(cache == Global.SelectedCache())
        	paintID = 1;
        canvas.drawRect(new Rect(0, 0, width, 75), Global.ListItemBackgroundPaint[paintID]);

      	canvas.drawText(cache.Name, 5, 30, Global.ListItemTextPaint[paintID]);
      	Paint tmpPaint = new Paint(Global.ListItemTextPaint[paintID]);
      	tmpPaint.setTextAlign(Align.RIGHT);
      	canvas.drawText(Float.toString(cache.Distance()), width - 10, 70, tmpPaint);
    }
}
