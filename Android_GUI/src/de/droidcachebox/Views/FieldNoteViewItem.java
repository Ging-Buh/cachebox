package de.droidcachebox.Views;

import java.text.SimpleDateFormat;

import de.droidcachebox.Global;
import de.droidcachebox.R;
import de.droidcachebox.Components.ActivityUtils;
import de.droidcachebox.Events.ViewOptionsMenu;
import de.droidcachebox.Geocaching.FieldNoteEntry;
import de.droidcachebox.Ui.Sizes;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.Layout.Alignment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class FieldNoteViewItem extends View implements ViewOptionsMenu  {
	private FieldNoteEntry fieldnote;
    private static int width;
    private int height = 0;
    private static int rightBorder;
    private boolean BackColorChanger = false;
    private StaticLayout layoutTypeText;
    private StaticLayout layoutComment;
    private static TextPaint textPaint;
    private static int drawTextHeight;
    
    public FieldNoteViewItem(Context context, FieldNoteEntry fieldnote, Boolean BackColorId) {
		super(context);

        this.fieldnote = fieldnote;
        BackColorChanger = BackColorId;
        
        if(textPaint==null)
        {
        	textPaint = new TextPaint();
        	textPaint.setTextSize(Sizes.getScaledFontSize_normal());
        	textPaint.setColor(Global.getColor(R.attr.TextColor));
        	textPaint.setAntiAlias(true);
        }
     
	}

    private Boolean isNullHeightItem=false;
	public FieldNoteViewItem(Context context) 
	{
		super(context);
		isNullHeightItem=true;
	}

	@Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
       if(isNullHeightItem)
       {
    	   height=0;
       }
       else
       {
		 if (height == 0) // Höhe ist noch nicht berechnet 
	        {
			 FieldNoteViewItem.width = measureWidth(widthMeasureSpec);
			 height = 0;
			 FieldNoteViewItem.rightBorder =(int) (height * 1.5);
	        }
		 if (headHeight<1||headLinePos<1)
	        {
	     	   headHeight = (int) (layoutTypeText.getHeight()*1.5)+Sizes.getCornerSize();
	     	   headLinePos = (headHeight/2)+(layoutTypeText.getHeight()/2)-5;
	        }
		 
		 Rect bounds = new Rect();
	     Global.Paints.mesurePaint.getTextBounds("T", 0, 1, bounds);
	     drawTextHeight=bounds.height();
		 
		 height += headHeight 						// höhe der Kopf Zeile
			 	+ Sizes.getIconSize()							// höhe cacheIcon
		 		+ drawTextHeight				  	// höhe GC-Code draw
		 		+ Sizes.getCornerSize()*2				// ???
		 		+ layoutComment.getHeight();		// höhe des comment Textes
       }
		 
		 
		 setMeasuredDimension(FieldNoteViewItem.width, height);
	            
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
            result = (int) Global.Paints.mesurePaint.measureText(fieldnote.CacheName) + getPaddingLeft()
                    + getPaddingRight();
            if (specMode == MeasureSpec.AT_MOST) {
                // Respect AT_MOST value if that was what is called for by measureSpec
                result = Math.min(result, specSize);
            }
        }
        
        width = specSize;
        int innerWidth = width - (Sizes.getCornerSize()*2);
        layoutTypeText = new StaticLayout(fieldnote.typeString, textPaint, innerWidth, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
        layoutComment = new StaticLayout(fieldnote.comment, textPaint, innerWidth, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
        
        
        return result;
    }



 // static Member
    private static Paint Linepaint;
    private static Paint TextPaint;
    private static int headHeight;
    private static int headLinePos;
    private static TextPaint cacheNamePaint;
    private static int nameLayoutWidth = 0;
    @Override
    protected void onDraw(Canvas canvas) {
        
    	if(isNullHeightItem)return;
    	
        //initial
        if (Linepaint==null)
        {
     	   Linepaint = new Paint();
            Linepaint.setAntiAlias(true);
            Linepaint.setColor(Global.getColor(R.attr.ListSeparator));
        }
        if (TextPaint==null)
        {
     	   TextPaint = new Paint();
     	   TextPaint.setAntiAlias(true);
           TextPaint.setFakeBoldText(true);
           TextPaint.setTextSize(Sizes.getScaledFontSize_normal());
           TextPaint.setColor(Global.getColor(R.attr.TextColor));
        }
        if (cacheNamePaint==null)
		{
        	cacheNamePaint = new TextPaint();
        	cacheNamePaint.setAntiAlias(true);
        	cacheNamePaint.setFakeBoldText(true);
        	cacheNamePaint.setTextSize(Sizes.getScaledFontSize_normal());
        	cacheNamePaint.setColor(Global.getColor(R.attr.TextColor));
            
		}
        if(nameLayoutWidth==0)
        {
        	nameLayoutWidth = width - Sizes.getIconSize() - rightBorder ;
        }
		
        textPaint.setColor(Global.getColor(R.attr.TextColor));
        TextPaint.setColor(Global.getColor(R.attr.TextColor));
        cacheNamePaint.setColor(Global.getColor(R.attr.TextColor));
        
        boolean selected = false;
        if (this.fieldnote == FieldNotesView.aktFieldNote)
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
     		   Global.getColor(R.attr.ListSeparator), BackgroundColor, Sizes.getCornerSize());
        
       
        // Kopfzeile
	        final Rect KopfRect = new Rect(5, 5, width-5, headHeight);;
	        final RectF KopfRectF = new RectF(KopfRect);
	        canvas.drawRoundRect( KopfRectF,Sizes.getCornerSize(),Sizes.getCornerSize(), Linepaint);
	        canvas.drawRect(new Rect(5, headHeight-Sizes.getCornerSize(), width-5, headHeight), Linepaint);
	        
	        //Icon
	        	int space = (fieldnote.typeIcon >= 0) ? ActivityUtils.PutImageTargetHeight(canvas, Global.LogIcons[fieldnote.typeIcon],Sizes.getHalfCornerSize(), 8, headHeight-10) + 4 : 0;
	        
	        // typeString
	        	canvas.drawText(fieldnote.typeString, space + Sizes.getHalfCornerSize(), headLinePos, TextPaint);
	       
	        // Time/Date
		        TextPaint.setFakeBoldText(false);
		        SimpleDateFormat postFormater = new SimpleDateFormat("HH:mm - dd/MM/yyyy"); 
		        String dateString = postFormater.format(fieldnote.timestamp); 
		        int DateLength = (int) TextPaint.measureText(dateString);
		        canvas.drawText(dateString, width - DateLength-10, headLinePos, TextPaint);
        
        // Info Körper
		    int left = Sizes.getCornerSize();     
		    int top = headHeight;    
		     // 1st Line Icon and Name
		        
			     // Draw Icon
		    		ActivityUtils.PutImageTargetHeight(canvas, Global.CacheIconsBig[fieldnote.cacheType], left  , top - (int) (Sizes.getScaledFontSize_normal() / 2) , Sizes.getIconSize()); 
		    	   
		    	 // Draw Cache Name
		    		StaticLayout layoutCacheName = new StaticLayout(fieldnote.CacheName, cacheNamePaint, nameLayoutWidth, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
		    	    int LayoutHeight = ActivityUtils.drawStaticLayout(canvas, layoutCacheName, left + Sizes.getIconSize() + 5, top);
		    	       
		    	 // over draw 3st Cache name line
		    	     if(layoutCacheName.getLineCount()>2)
		    	     {
		    	    	 Paint backPaint = new Paint();
		    	    	 backPaint.setColor(BackgroundColor); // Color.RED
		    	    	 int VislinesHeight = LayoutHeight*2/layoutCacheName.getLineCount();
		    	    	 canvas.drawRect(new Rect(left + Sizes.getIconSize() + 5,top + VislinesHeight,nameLayoutWidth+left + Sizes.getIconSize() + 5,top+LayoutHeight+VislinesHeight-4), backPaint);
		    	     }
		    	     
		     // 2st Line Infos
		    	//GC-Code
		    	     top += Sizes.getIconSize();
		    	     canvas.drawText(fieldnote.gcCode, left, top, cacheNamePaint);
		    	     top+=drawTextHeight;
	    	    // comment
		    	     ActivityUtils.drawStaticLayout(canvas, layoutComment, left, top);
		        
		        
		        
		        
		        
        /*canvas.drawText(fieldnote.CacheName, 70, 20, new Paint());
        canvas.drawText(fieldnote.comment, 70, 35, new Paint());
        canvas.drawText(fieldnote.gcCode, 70, 50, new Paint());
        canvas.drawText("#" + fieldnote.foundNumber, 70, 65, new Paint());
        canvas.drawText(fieldnote.timestamp.toLocaleString(), 200, 70, new Paint());
        canvas.drawText("Id:" + fieldnote.Id, 250, 35, new Paint());
        canvas.drawText("CacheId:" + fieldnote.CacheId, 250, 50, new Paint());
*/
        
        
        
        
    }

	@Override
	public boolean ItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void BeforeShowMenu(Menu menu) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int GetMenuId() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void OnShow() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnHide() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnFree() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void ActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int GetContextMenuId() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void BeforeShowContextMenu(Menu menu) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean ContextMenuItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		return false;
	}
}
