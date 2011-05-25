package de.droidcachebox.Views;

import java.util.List;

import de.droidcachebox.Config;
import de.droidcachebox.Database;
import de.droidcachebox.Global;
import de.droidcachebox.R;

import de.droidcachebox.Events.CachListChangedEventList;
import de.droidcachebox.Events.CacheListChangedEvent;
import de.droidcachebox.Events.PositionEvent;
import de.droidcachebox.Events.PositionEventList;
import de.droidcachebox.Events.ViewOptionsMenu;
import de.droidcachebox.Geocaching.Cache;
import de.droidcachebox.Geocaching.CacheList;
import de.droidcachebox.Geocaching.Coordinate;
import de.droidcachebox.Geocaching.Waypoint;
import android.R.drawable;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;

public class CacheListView extends ListView implements ViewOptionsMenu, PositionEvent, CacheListChangedEvent {
	
	private CustomAdapter lvAdapter;
	private Paint paint;
	/**
	 * Constructor
	 */
	public CacheListView(final Context context) {
		super(context);

		CachListChangedEventList.Add(this);
		this.setAdapter(null);
		lvAdapter = new CustomAdapter(getContext(), Database.Data.Query);
		this.setAdapter(lvAdapter);
//		this.setLongClickable(true);
		this.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
        		Cache cache = Database.Data.Query.get(arg2);
        		
        		Waypoint finalWp = null;
        		if (cache.HasFinalWaypoint())
        			finalWp = cache.GetFinalWaypoint();
        		Global.SelectedWaypoint(cache, finalWp);

        		invalidate();
				return;
			}
		});

		this.setBackgroundColor(Global.getColor(R.attr.EmptyBackground));
		this.setCacheColorHint(R.color.Day_TitleBarColor);
		this.setDividerHeight(5);
		this.setDivider(getBackground());
		
		
	}
	
	
	
	public void CacheListChangedEvent()
	{
		this.setAdapter(null);
		lvAdapter = new CustomAdapter(getContext(), Database.Data.Query);
		this.setAdapter(lvAdapter);
		lvAdapter.notifyDataSetChanged();
	}
	
	 	static public int windowW=0;
	    static public int windowH=0 ;
	    @Override
	    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) 
	    {
	    // we overriding onMeasure because this is where the application gets its right size.
	    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	    windowW = getMeasuredWidth();
	    windowH = getMeasuredHeight();
	    }

	public class CustomAdapter extends BaseAdapter /*implements OnClickListener*/ {
		 
		/*private class OnItemClickListener implements OnClickListener{
		    private int mPosition;
		    OnItemClickListener(int position){
		            mPosition = position;
		    }
		    public void onClick(View arg0) {
		            Log.v("ddd", "onItemClick at position" + mPosition);
		    }
		}*/
	 
	    private Context context;
	    private CacheList cacheList;
	 
	    public CustomAdapter(Context context, CacheList cacheList ) {
	        this.context = context;
	        this.cacheList = cacheList;
	    }
	 
	    public int getCount() {
	        return cacheList.size();
	    }
	 
	    public Object getItem(int position) {
	        return cacheList.get(position);
	    }
	 
	    public long getItemId(int position) {
	        return position;
	    }
	 
	    public View getView(int position, View convertView, ViewGroup parent)
	    {
	        Cache cache = cacheList.get(position);
	        Boolean BackGroundChanger = ((position % 2) == 1);
	        CacheListViewItem v = new CacheListViewItem(context, cache,BackGroundChanger);
	 
	        return v;
	    }
	 
	    /*public void onClick(View v) {
	            Log.v(LOG_TAG, "Row button clicked");
	    }*/
	 
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
	public void OnShow() {
		PositionEventList.Add(this);

		// aktuellen Cache in der List anzeigen
		if (Global.SelectedCache() != null)
		{
			int id = 0;
			
			for (Cache ca : Database.Data.Query)
			{
				if (ca == Global.SelectedCache())
				{
					this.setSelection(id - 2);
					break;
				}
				id++;
			}
		} else
			this.setSelection(0);		
	}

	@Override
	public void OnHide() {
		PositionEventList.Remove(this);
		
	}

	@Override
	public int GetMenuId() {
		// TODO Auto-generated method stub
		return 0;
	}

	private long lastRender;
	@Override
	public void PositionChanged(Location location) 
	{
		if(lastRender + 2000 > System.currentTimeMillis())return;
		this.invalidate();
		lastRender = System.currentTimeMillis();
	}

	
	
	@Override
	public void OrientationChanged(float heading) {
		if(lastRender + 2000 > System.currentTimeMillis())return;
		this.invalidate();
		lastRender = System.currentTimeMillis();
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

