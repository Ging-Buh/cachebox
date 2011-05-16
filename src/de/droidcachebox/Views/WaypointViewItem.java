package de.droidcachebox.Views;

import de.droidcachebox.Config;
import de.droidcachebox.Global;
import de.droidcachebox.R;
import de.droidcachebox.UnitFormatter;
import de.droidcachebox.Geocaching.Cache;
import de.droidcachebox.Geocaching.Coordinate;
import de.droidcachebox.Geocaching.Waypoint;
import android.R.bool;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
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
    private int height;
    private boolean BackColorChanger = false;
    private final int CornerSize =20;
    private int rightBorder;
    private int lineHeight;
    private int imgSize;

	public WaypointViewItem(Context context, Cache cache, Waypoint waypoint, Boolean BackColorId) {
		// TODO Auto-generated constructor stub
		super(context);
        this.cache = cache;
        this.waypoint = waypoint;
        
        BackColorChanger = BackColorId;
       }

	
	@Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
       
		 // Berechne Höhe so das 7 Einträge in die Liste passen
        this.height = (int) WaypointView.windowH / 5;
        this.imgSize = (int) (this.height * 0.6);
        this.lineHeight = (int) this.height / 4;
        this.rightBorder =(int) (this.height );
        
        setMeasuredDimension(measureWidth(widthMeasureSpec),this.height);
		
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
        
        
        Boolean isSelected = false;
        if (Global.SelectedWaypoint() == waypoint ||(( Global.SelectedCache()== cache && !(waypoint == null)&& Global.SelectedWaypoint() == waypoint )))
        {
        	isSelected=true;
        }
        
        
        int innerHeight = height - (CornerSize*2);
        int innerWidth = width - (CornerSize);
        
        int rowHeight = 35; // (int) (layoutFinder.getHeight()*1.5)+CornerSize;
        int LineXPos = 20; // (rowHeight/2)+(layoutFinder.getHeight()/2)-5;
        Boolean Night = Config.GetBool("nightMode");
        Paint NamePaint = new Paint( Night? Global.Paints.Night.Text.selected: Global.Paints.Day.Text.selected);
        NamePaint.setFakeBoldText(true);
       // NamePaint.setTextSize(layoutFinder.getHeight());
        Paint Linepaint = Night? Global.Paints.Night.ListSeperator : Global.Paints.Day.ListSeperator;
        Linepaint.setAntiAlias(true);
       
        canvas.drawColor(Global.getColor(R.attr.myBackground));

        Paint BackPaint = new Paint();
        BackPaint.setAntiAlias(true);
       
        final Rect rect = new Rect(7, 7, width-7, height-7);
        final RectF rectF = new RectF(rect);
        
        final Rect outerRect = new Rect(5, 5, width-5, height-5);
        final RectF OuterRectF = new RectF(outerRect);

        canvas.drawRoundRect( OuterRectF,CornerSize,CornerSize, Linepaint);
       
        
        int BackgroundColor;
        if (BackColorChanger)
        {
        	BackgroundColor = (isSelected)? Global.getColor(R.attr.ListBackground_select): Global.getColor(R.attr.ListBackground);
        }
        else
        {
        	BackgroundColor = (isSelected)? Global.getColor(R.attr.ListBackground_select): Global.getColor(R.attr.ListBackground_secend);
        }
        
        BackPaint.setColor(BackgroundColor);
        canvas.drawRoundRect( rectF,CornerSize-2,CornerSize-2, BackPaint);
        
        if (waypoint == null) // this Item is the Cache
        {
             cache.DrawInfo(canvas,CornerSize/2,CornerSize, innerHeight, innerWidth, imgSize, lineHeight, rightBorder, Color.TRANSPARENT, Cache.DrawStyle.withoutSeparator);    
        }
        else
        {	
        	Paint tmpPaint = new Paint(Config.GetBool("nightMode")? Global.Paints.Night.Text.noselected : Global.Paints.Day.Text.noselected);
        	int left= 20;
        	int top = 30;
        	Rect bounds = new Rect();
        	tmpPaint.getTextBounds("471km", 0, 4, bounds);
        	int lineHeight = bounds.height()+10;
        	int iconWidth = 0;
        	// draw icon
        	if (((int)waypoint.Type.ordinal()) < Global.CacheIconsBig.length)
        		iconWidth=Global.PutImageTargetHeight(canvas, Global.CacheIconsBig[(int)waypoint.Type.ordinal()], CornerSize/2,CornerSize, imgSize);

        	// draw Text info
        	String title = waypoint.GcCode + ": " + waypoint.Title;
        	left += iconWidth;
        	tmpPaint.setFakeBoldText(true);
        	canvas.drawText(title, left, top,tmpPaint);
        	top += lineHeight;
        	tmpPaint.setFakeBoldText(false);
        	canvas.drawText(waypoint.Description, left, top,tmpPaint);
        	top += lineHeight;
        	canvas.drawText(Global.FormatLatitudeDM(waypoint.Latitude()) + " / " + Global.FormatLongitudeDM(waypoint.Longitude()), left, top,tmpPaint);
        	top += lineHeight;
        	canvas.drawText(waypoint.Clue, left, top,tmpPaint);
        	
        	// draw Arrow and distance
        	if (Global.LastValidPosition.Valid || Global.Marker.Valid)
            {
        		
    	            Coordinate position = (Global.Marker.Valid) ? Global.Marker : Global.LastValidPosition;
    	            double heading = (Global.Locator != null) ? Global.Locator.getHeading() : 0;
    	            double bearing = Coordinate.Bearing(position.Latitude, position.Longitude, waypoint.Latitude(), waypoint.Longitude());
    	            double cacheBearing = bearing - heading;
    	            String cacheDistance = UnitFormatter.DistanceString(waypoint.Distance());
    			       
    			  
        		
        		Global.PutImageTargetHeight(canvas, Global.Arrows[1],cacheBearing,(int)( width - rightBorder/2) ,(int)(lineHeight /8), (int)(lineHeight*2.4));
    		    canvas.drawText(cacheDistance, innerWidth - bounds.width() - (CornerSize*2), top, tmpPaint);
    		       
           }
        	
         
        }
    }
}
