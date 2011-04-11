package de.droidcachebox.Views;

import java.util.List;

import de.droidcachebox.Database;
import de.droidcachebox.Global;

import de.droidcachebox.Geocaching.Cache;
import de.droidcachebox.Geocaching.CacheList;
import de.droidcachebox.Geocaching.Waypoint;
import android.R.drawable;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;

public class WaypointView extends ListView {
	
	private Cache cache;

	private Paint paint;
	/**
	 * Constructor
	 */
	public WaypointView(final Context context) {
		super(context);

		
		this.setAdapter(null);
		CustomAdapter lvAdapter = new CustomAdapter(getContext(), Global.SelectedCache());
		this.setAdapter(lvAdapter);
		this.setLongClickable(true);
		this.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
        		Cache cache = Database.Data.Query.get(arg2);
        		Global.SelectedCache(cache);
				return true;
			}
		});
		this.setBackgroundColor(Global.EmptyBackground);
		this.setCacheColorHint(Global.TitleBarColor);
		this.setDividerHeight(5);
		this.setDivider(getBackground());
		
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
	    private Cache cache;
	 
	    public CustomAdapter(Context context, Cache cache ) {
	        this.context = context;
	        this.cache = cache;
	    }
	 
	    public int getCount() {
	    	if (cache != null)
	    		return cache.waypoints.size();
	    	else
	    		return 0;
	    }
	 
	    public Object getItem(int position) {
	    	if (cache != null)
	    		return cache.waypoints.get(position);
	    	else
	    		return null;
	    }
	 
	    public long getItemId(int position) {
	        return position;
	    }
	 
	    public View getView(int position, View convertView, ViewGroup parent)
	    {
	    	if (cache != null)
	    	{
		        Waypoint waypoint = cache.waypoints.get(position);
		        WaypointViewItem v = new WaypointViewItem(context, cache);
		        return v;
	    	} else
	    		return null;
	    }
	 
	    /*public void onClick(View v) {
	            Log.v(LOG_TAG, "Row button clicked");
	    }*/
	 
	}
}

