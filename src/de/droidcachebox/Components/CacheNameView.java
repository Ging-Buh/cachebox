package de.droidcachebox.Components;

import de.droidcachebox.Config;
import de.droidcachebox.Global;
import de.droidcachebox.R;
import de.droidcachebox.Events.SelectedCacheEvent;
import de.droidcachebox.Events.SelectedCacheEventList;
import de.droidcachebox.Geocaching.Cache;
import de.droidcachebox.Geocaching.Waypoint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.View.MeasureSpec;

public class CacheNameView extends View implements SelectedCacheEvent {

	private Cache cache;
	private Paint paint;
	private Resources res;
	
	public CacheNameView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		res = context.getResources();
		SelectedCacheEventList.Add(this);
		
		this.setBackgroundColor(Config.GetBool("nightMode")? res.getColor(R.color.Night_TitleBarColor) : res.getColor(R.color.Day_TitleBarColor));
		// TODO Auto-generated constructor stub
		paint = new Paint();
		// set's the paint's colour
		paint.setColor(Config.GetBool("nightMode")? res.getColor(R.color.Night_TitleBarText) : res.getColor(R.color.Day_TitleBarText));
		// set's paint's text size
		paint.setTextSize(25);
		// smooth's out the edges of what is being drawn
		paint.setAntiAlias(true);
		
	}

	@Override
	public void SelectedCacheChanged(Cache cache, Waypoint waypoint) {
		// TODO Auto-generated method stub
		this.cache = cache;
		invalidate();
	}
	
	private int height;
	private int width;
	
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) 
	{
		this.width = measure(widthMeasureSpec);
		this.height = measure(heightMeasureSpec);
		
        
      
        setMeasuredDimension(this.width, this.height);
	}
	
	
	
    /**
     * Determines the width of this view
     * @param measureSpec A measureSpec packed into an int
     * @return The width of the view, honoring constraints from measureSpec
     */
    private int measure(int measureSpec) 
    {
        int result = 0;
        
        int specSize = MeasureSpec.getSize(measureSpec);

       
            result = specSize;
        
        
        return result;
    }

	

	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		final Drawable Slide = Global.BtnIcons[0];
		Rect mRect = new Rect();
		mRect.set(-10, -2, width+10 , height+2);
		Slide.setBounds(mRect);
   	 	
		Slide.setColorFilter(new PorterDuffColorFilter(Global.getColor(R.attr.SlideDownColorFilter), android.graphics.PorterDuff.Mode.MULTIPLY ));
   	 	
		Slide.draw(canvas);
		
		if (cache == null)
			return;

		canvas.drawText(cache.Name, 5, 30, paint);
		
	}
}
