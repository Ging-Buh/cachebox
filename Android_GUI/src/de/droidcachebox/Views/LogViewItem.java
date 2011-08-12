package de.droidcachebox.Views;

import java.text.SimpleDateFormat;
import de.droidcachebox.Global;
import de.droidcachebox.R;
import de.droidcachebox.Ui.ActivityUtils;
import de.droidcachebox.Ui.Sizes;
import CB_Core.Types.Cache;
import CB_Core.Types.LogEntry;
import android.content.Context;
import android.graphics.Canvas;
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
    private static TextPaint textPaint;
    private StaticLayout layoutComment;
    private StaticLayout layoutFinder;
    
    private boolean BackColorChanger=false;
    
    
	public LogViewItem(Context context, Cache cache, LogEntry logEntry, Boolean BackColorId) {
		
		super(context);
        this.cache = cache;
        this.logEntry = logEntry;
        
        if(textPaint==null)
        {
        	textPaint = new TextPaint();
        	textPaint.setTextSize(Sizes.getScaledFontSize_normal());
        	textPaint.setColor(Global.getColor(R.attr.TextColor));
        	textPaint.setAntiAlias(true);
        }
        
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
            result = (int) textPaint.measureText(cache.Name) + getPaddingLeft()
                    + getPaddingRight();
            if (specMode == MeasureSpec.AT_MOST) {
                // Respect AT_MOST value if that was what is called for by measureSpec
                result = Math.min(result, specSize);
            }
        }
        width = specSize;
        
        int innerWidth = width - (Sizes.getCornerSize()*2);
        
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

        mAscent = (int) textPaint.ascent();
        if (specMode == MeasureSpec.EXACTLY) {
            // We were told how big to be
            result = specSize;
        } else {
            // Measure the text (beware: ascent is a negative number)
            result = (int) (-mAscent + textPaint.descent()) + getPaddingTop()
                    + getPaddingBottom();
          	result += layoutComment.getHeight();
            result += layoutFinder.getHeight();

            if (specMode == MeasureSpec.AT_MOST) {
                // Respect AT_MOST value if that was what is called for by measureSpec
                result = Math.min(result, specSize);
            }
        }        

        result +=Sizes.getCornerSize()*2;
        height = result;
        return result;
    }
    
    
    // static Member
    private static Paint Linepaint;
    private static Paint NamePaint;
    private static int headHeight;
    private static int headLinePos;
    @Override
    protected void onDraw(Canvas canvas) {
      
       //initial
       if (Linepaint==null)
       {
    	   Linepaint = new Paint();
           Linepaint.setAntiAlias(true);
           Linepaint.setColor(Global.getColor(R.attr.ListSeparator));
       }
       if (NamePaint==null)
       {
    	   NamePaint = new Paint();
           NamePaint.setFakeBoldText(true);
           NamePaint.setAntiAlias(true);
           NamePaint.setTextSize(Sizes.getScaledFontSize_normal());
           NamePaint.setColor(Global.getColor(R.attr.TextColor));
       }
       if (headHeight<1||headLinePos<1)
       {
    	   headHeight = (int) (layoutFinder.getHeight()*1.5)+ Sizes.getCornerSize();
    	   headLinePos = (headHeight/2)+(layoutFinder.getHeight()/2)-5;
       }
       
           
       ActivityUtils.drawFillRoundRecWithBorder(canvas, new Rect(5, 5, width-5, height-5), 2, 
    		   Global.getColor(R.attr.ListSeparator),(BackColorChanger)? Global.getColor(R.attr.ListBackground_secend): Global.getColor(R.attr.ListBackground), 
    				   Sizes.getCornerSize());
       
      
       // Kopfzeile
       final Rect KopfRect = new Rect(5, 5, width-5, headHeight);;
       final RectF KopfRectF = new RectF(KopfRect);
       canvas.drawRoundRect( KopfRectF,Sizes.getCornerSize(),Sizes.getCornerSize(), Linepaint);
       canvas.drawRect(new Rect(5, headHeight-Sizes.getCornerSize(), width-5, headHeight), Linepaint);
       
       int space = (logEntry.TypeIcon >= 0) ? ActivityUtils.PutImageTargetHeight(canvas, Global.LogIcons[logEntry.TypeIcon],Sizes.getHalfCornerSize(), 8, headHeight-10) + 4 : 0;

       
       canvas.drawText(logEntry.Finder, space + Sizes.getHalfCornerSize(), headLinePos, NamePaint);
       
       NamePaint.setFakeBoldText(false);
       SimpleDateFormat postFormater = new SimpleDateFormat("dd/MM/yyyy"); 
       String dateString = postFormater.format(logEntry.Timestamp); 
       int DateLength = (int) NamePaint.measureText(dateString);
       canvas.drawText(dateString, width - DateLength-10, headLinePos, NamePaint);
      
       
       canvas.drawLine(5, headHeight - 2, width-5, headHeight - 2,Linepaint); 
       canvas.drawLine(5, headHeight - 3, width-5, headHeight - 3,Linepaint);
       
       
       // Körper
       ActivityUtils.drawStaticLayout(canvas, layoutComment, Sizes.getCornerSize(), headHeight + Sizes.getCornerSize());
       
       
     	  
          
    }

}
