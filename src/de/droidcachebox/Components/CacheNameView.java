package de.droidcachebox.Components;

import de.droidcachebox.Global;
import de.droidcachebox.Events.SelectedCacheEvent;
import de.droidcachebox.Events.SelectedCacheEventList;
import de.droidcachebox.Geocaching.Cache;
import de.droidcachebox.Geocaching.Waypoint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

public class CacheNameView extends View implements SelectedCacheEvent {

	private Cache cache;
	private Paint paint;
	
	public CacheNameView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub

		SelectedCacheEventList.Add(this);
		
		this.setBackgroundColor(Global.TitleBarColor);
		// TODO Auto-generated constructor stub
		paint = new Paint();
		// set's the paint's colour
		paint.setColor(Global.TitleBarText);
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

	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		if (cache == null)
			return;
//		canvas.drawRect(canvas.getClipBounds(), background);
		canvas.drawText(cache.Name, 5, 30, paint);
		// if the view is visible onDraw will be called at some point in the
		// future
		invalidate();
	}
}
