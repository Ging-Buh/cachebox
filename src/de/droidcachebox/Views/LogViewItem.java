package de.droidcachebox.Views;

import java.text.BreakIterator;
import java.text.DateFormat;

import de.droidcachebox.Config;
import de.droidcachebox.Global;
import de.droidcachebox.R;
import de.droidcachebox.Geocaching.Cache;
import de.droidcachebox.Geocaching.LogEntry;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Paint.Align;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.view.View;
import android.view.View.MeasureSpec;

public class LogViewItem extends View {
    private Cache cache;
    private LogEntry logEntry;
    private int mAscent;
    private int width;
    private int height;
    private TextPaint textPaint;
    private StaticLayout layoutComment;
    private StaticLayout layoutFinder;
    private StaticLayout layoutDate;
    private static boolean BackColorChanger=false;
    
    
	public LogViewItem(Context context, Cache cache, LogEntry logEntry) {
		// TODO Auto-generated constructor stub
		super(context);
        this.cache = cache;
        this.logEntry = logEntry;
        
        textPaint = new TextPaint(Config.GetBool("nightMode")? Global.Paints.Night.Text.noselected : Global.Paints.Day.Text.noselected );
        textPaint.setTextSize(24);
//        textPaint.setSubpixelText(true);
        
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
                measureHeight(heightMeasureSpec));
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
        
      	layoutComment = new StaticLayout(logEntry.Comment, textPaint, width, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
      	layoutFinder = new StaticLayout(logEntry.Finder, textPaint, width, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
      	DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);
      	layoutDate = new StaticLayout(df.format(logEntry.Timestamp), textPaint, width, Alignment.ALIGN_OPPOSITE, 1.0f, 0.0f, false);
        
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
          	result += layoutComment.getHeight();
            result += layoutFinder.getHeight();

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
        int paintID = 0;
        canvas.drawPaint(Config.GetBool("nightMode")? Global.Paints.Night.ListBackground : Global.Paints.Day.ListBackground);

        layoutFinder.draw(canvas);
//        canvas.translate(width-100, 0);
        layoutDate.draw(canvas);
//        canvas.translate(-width+100, 0);
        
     	
      	canvas.translate(0, layoutFinder.getHeight());
      	layoutComment.draw(canvas);
      	canvas.translate(0, -layoutFinder.getHeight());

      	Paint li = new Paint();
      	li.setColor(Color.BLACK);
      	canvas.drawLine(0, height-2, width, height-2, li);
    }

}
