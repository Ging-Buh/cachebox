package de.droidcachebox.Views;

import java.text.SimpleDateFormat;
import de.droidcachebox.Config;
import de.droidcachebox.Global;
import de.droidcachebox.R;
import de.droidcachebox.Components.ActivityUtils;
import de.droidcachebox.Geocaching.Cache;
import de.droidcachebox.Geocaching.LogEntry;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.view.View;


public class LogViewItem extends View {
    private Cache cache;
    private LogEntry logEntry;
    private int mAscent;
    private int width;
    private int height;
    private TextPaint textPaint;
    private StaticLayout layoutComment;
    private StaticLayout layoutFinder;
    
    private boolean BackColorChanger=false;
    private final int CornerSize =20;
    
	public LogViewItem(Context context, Cache cache, LogEntry logEntry, Boolean BackColorId) {
		
		super(context);
        this.cache = cache;
        this.logEntry = logEntry;
        
        textPaint = new TextPaint(Config.GetBool("nightMode")? Global.Paints.Night.Text.noselected : Global.Paints.Day.Text.noselected );
        textPaint.setTextSize(Global.scaledFontSize_normal);
        BackColorChanger = BackColorId;
        
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
            result = (int) Global.Paints.Day.Text.selected.measureText(cache.Name) + getPaddingLeft()
                    + getPaddingRight();
            if (specMode == MeasureSpec.AT_MOST) {
                // Respect AT_MOST value if that was what is called for by measureSpec
                result = Math.min(result, specSize);
            }
        }
        width = specSize;
        
        int innerWidth = width - (CornerSize*2);
        
      	layoutComment = new StaticLayout(logEntry.Comment, textPaint, innerWidth, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
      	layoutFinder = new StaticLayout(logEntry.Finder, textPaint, innerWidth, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
      	        
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

        mAscent = (int) Global.Paints.Day.Text.selected.ascent();
        if (specMode == MeasureSpec.EXACTLY) {
            // We were told how big to be
            result = specSize;
        } else {
            // Measure the text (beware: ascent is a negative number)
            result = (int) (-mAscent + Global.Paints.Day.Text.selected.descent()) + getPaddingTop()
                    + getPaddingBottom();
          	result += layoutComment.getHeight();
            result += layoutFinder.getHeight();

            if (specMode == MeasureSpec.AT_MOST) {
                // Respect AT_MOST value if that was what is called for by measureSpec
                result = Math.min(result, specSize);
            }
        }        

        result +=CornerSize*2;
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
       int rowHeight = (int) (layoutFinder.getHeight()*1.5)+CornerSize;
       int LineXPos = (rowHeight/2)+(layoutFinder.getHeight()/2)-5;
       Boolean Night = Config.GetBool("nightMode");
       Paint NamePaint = new Paint( Night? Global.Paints.Night.Text.selected: Global.Paints.Day.Text.selected);
       NamePaint.setFakeBoldText(true);
       NamePaint.setTextSize(Global.scaledFontSize_normal);
       Paint Linepaint = Night? Global.Paints.Night.ListSeperator : Global.Paints.Day.ListSeperator;
       Linepaint.setAntiAlias(true);
       int LineColor = Global.getColor(R.attr.ListSeparator);
      
       canvas.drawColor(Global.getColor(R.attr.myBackground));

       Paint BackPaint = new Paint();
       BackPaint.setAntiAlias(true);
      
       ActivityUtils.drawFillRoundRecWithBorder(canvas, new Rect(5, 5, width-5, height-5), 2, 
    		   LineColor,(BackColorChanger)? Global.getColor(R.attr.ListBackground_secend): Global.getColor(R.attr.ListBackground), 
    						   CornerSize);
       
      
       // Kopfzeile
       final Rect KopfRect = new Rect(5, 5, width-5, rowHeight);;
       final RectF KopfRectF = new RectF(KopfRect);
       canvas.drawRoundRect( KopfRectF,CornerSize,CornerSize, Linepaint);
       canvas.drawRect(new Rect(5, rowHeight-CornerSize, width-5, rowHeight), Linepaint);
       
       int space = (logEntry.TypeIcon >= 0) ? ActivityUtils.PutImageTargetHeight(canvas, Global.LogIcons[logEntry.TypeIcon],CornerSize/2, 8, rowHeight-10) + 4 : 0;

       SimpleDateFormat postFormater = new SimpleDateFormat("dd/MM/yyyy"); 
       String dateString = postFormater.format(logEntry.Timestamp); 
       canvas.drawText(logEntry.Finder, space + CornerSize/2, LineXPos, NamePaint);
       
       NamePaint.setFakeBoldText(false);
       int DateLength = (int) NamePaint.measureText(dateString);
       canvas.drawText(dateString, width - DateLength-10, LineXPos, NamePaint);
      
       
       canvas.drawLine(5, rowHeight - 2, width-5, rowHeight - 2,Linepaint); 
       canvas.drawLine(5, rowHeight - 3, width-5, rowHeight - 3,Linepaint);
       
       
       // Körper
       ActivityUtils.drawStaticLayout(canvas, layoutComment, CornerSize, rowHeight + CornerSize);
       
       
     	  
          
    }

}
