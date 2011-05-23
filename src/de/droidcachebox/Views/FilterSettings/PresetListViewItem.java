package de.droidcachebox.Views.FilterSettings;

import java.text.SimpleDateFormat;

import de.droidcachebox.Config;
import de.droidcachebox.Global;
import de.droidcachebox.R;
import de.droidcachebox.Components.ActivityUtils;
import de.droidcachebox.Geocaching.Cache;
import de.droidcachebox.Geocaching.FieldNoteEntry;
import de.droidcachebox.Views.FieldNoteViewItem;
import de.droidcachebox.Views.FieldNotesView;
import de.droidcachebox.Views.FilterSettings.PresetListView.PresetEntry;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.Layout.Alignment;
import android.view.View;
import android.view.View.MeasureSpec;

public class PresetListViewItem extends View {
	private PresetEntry mPresetEntry;
    private int mAscent;
    private static int width;
    private static int height = 0;
    private static int rightBorder;
    private boolean BackColorChanger = false;
    private StaticLayout layoutEntryName;
   
    private static TextPaint textPaint;
  
    
    public PresetListViewItem(Context context, PresetEntry fne, Boolean BackColorId) {
		super(context);

        this.mPresetEntry = fne;
        BackColorChanger = BackColorId;
        
        if(textPaint==null)
        {
        	textPaint = new TextPaint();
        	textPaint.setTextSize(Global.scaledFontSize_normal);
        	textPaint.setColor(Global.getColor(R.attr.TextColor));
        	textPaint.setAntiAlias(true);
        }
     
	}

	@Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
       
		
		
		 width = PresetListView.windowW;
		
		 height = iconSize +(Global.CornerSize*2); 
      
		
		
								
			 			 				
	     setMeasuredDimension(width, height);
	            
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

       /* String EntryName=mPresetEntry.getName();
        
        if (specMode == MeasureSpec.EXACTLY) {
            // We were told how big to be
            result = specSize;
        } else {
            // Measure the text
            result = (int) Global.Paints.mesurePaint.measureText(EntryName) + getPaddingLeft()
                    + getPaddingRight();
            if (specMode == MeasureSpec.AT_MOST) {
                // Respect AT_MOST value if that was what is called for by measureSpec
                result = Math.min(result, specSize);
            }
        }*/
        
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

        mAscent = (int) Global.Paints.mesurePaint.ascent();
        if (specMode == MeasureSpec.EXACTLY) {
            // We were told how big to be
            result = specSize;
        } else {
            // Measure the text (beware: ascent is a negative number)
            result = (int) (-mAscent + Global.Paints.mesurePaint.descent()) + getPaddingTop()
                    + getPaddingBottom();
            if (specMode == MeasureSpec.AT_MOST) {
                // Respect AT_MOST value if that was what is called for by measureSpec
                result = Math.min(result, specSize);
            }
        }
     
        return result;
    }

 // static Member
    
    private static Paint TextPaint;
    
    private static final int iconSize = (int) (Global.scaledFontSize_normal * 3.5);
    
    @Override
    protected void onDraw(Canvas canvas) {
        
        //initial
    	 int left = Global.CornerSize;
         int top = Global.CornerSize;
        
        if (TextPaint==null)
        {
     	   TextPaint = new Paint();
     	   TextPaint.setAntiAlias(true);
           TextPaint.setFakeBoldText(true);
           TextPaint.setTextSize((float) (Global.scaledFontSize_normal*1.3));
           TextPaint.setColor(Global.getColor(R.attr.TextColor));
        }
        
        if(layoutEntryName==null)
        {        	
        	int innerWidth = width - (Global.CornerSize*2)- iconSize;
            layoutEntryName = new StaticLayout(mPresetEntry.getName(), textPaint, innerWidth, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
        }
		
        textPaint.setColor(Global.getColor(R.attr.TextColor));
       
        
        boolean selected = false;
        if (this.mPresetEntry == PresetListView.aktPreset)
        	selected = true;
        
       
		int BackgroundColor;
        if (BackColorChanger)
        {
        	BackgroundColor = (selected)? Global.getColor(R.attr.ListBackground_select): Global.getColor(R.attr.ListBackground);
        }
        else
        {
        	BackgroundColor = (selected)? Global.getColor(R.attr.ListBackground_select): Global.getColor(R.attr.ListBackground_secend);
        }
        
        
        ActivityUtils.drawFillRoundRecWithBorder(canvas, new Rect(5, 5, width-5, height-5), 2, 
     		   Global.getColor(R.attr.ListSeparator), BackgroundColor, 
     						   Global.CornerSize);
        
        
        //draw Icon
        left+= ActivityUtils.PutImageTargetHeight(canvas, mPresetEntry.getIcone(), left , top , iconSize)+Global.CornerSize/2;
        
        ActivityUtils.drawStaticLayout(canvas, layoutEntryName, left, top);
  
    }
}
