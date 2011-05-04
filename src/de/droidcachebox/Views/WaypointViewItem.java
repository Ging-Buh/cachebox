package de.droidcachebox.Views;

import de.droidcachebox.Config;
import de.droidcachebox.Global;
import de.droidcachebox.R;
import de.droidcachebox.Geocaching.Cache;
import de.droidcachebox.Geocaching.Waypoint;
import android.R.bool;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.LinearLayout;

public class WaypointViewItem extends View {
    private Cache cache;
    private Waypoint waypoint;
    private int mAscent;
    private int width;
    private static boolean BackColorChanger = false; 

	public WaypointViewItem(Context context, Cache cache, Waypoint waypoint) {
		// TODO Auto-generated constructor stub
		super(context);
        this.cache = cache;
        this.waypoint = waypoint;
        
        if (BackColorChanger)
        {
        	this.setBackgroundColor(Config.GetBool("nightMode")? R.color.Night_ListBackground : R.color.Day_ListBackground);
        }
        else
        {
        	this.setBackgroundColor(Config.GetBool("nightMode")? R.color.Night_ListBackground_second : R.color.Day_ListBackground_second);
        }
        BackColorChanger = !BackColorChanger;
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
    /**
     * Render the text
     * 
     * @see android.view.View#onDraw(android.graphics.Canvas)
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int paintID = 0;
        if(waypoint == Global.SelectedWaypoint())
        	paintID = 1;
        
        Paint DrawBackPaint = new Paint( (paintID == 1)? Global.Paints.Day.selectedBack : Config.GetBool("nightMode")? Global.Paints.Night.ListBackground : Global.Paints.Day.ListBackground);
        canvas.drawPaint(DrawBackPaint);

        String title = cache.Name;
        if (waypoint != null)
        	title = waypoint.Title;
      	canvas.drawText(title, 5, 30, Config.GetBool("nightMode")? Global.Paints.Night.Text.noselected : Global.Paints.Day.Text.noselected);
      	Paint tmpPaint = new Paint(Config.GetBool("nightMode")? Global.Paints.Night.Text.noselected : Global.Paints.Day.Text.noselected);
      	tmpPaint.setTextAlign(Align.RIGHT);
      	float dist = cache.Distance();
      	if (waypoint != null)
      		dist = waypoint.Distance();
      	canvas.drawText(Float.toString(dist), width - 10, 70, tmpPaint);
    }
}
