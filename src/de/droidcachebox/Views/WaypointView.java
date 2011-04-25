package de.droidcachebox.Views;


import de.droidcachebox.Config;
import de.droidcachebox.Database;
import de.droidcachebox.Global;
import de.droidcachebox.main;
import de.droidcachebox.splash;

import de.droidcachebox.Events.SelectedCacheEvent;
import de.droidcachebox.Events.SelectedCacheEventList;
import de.droidcachebox.Events.ViewOptionsMenu;
import de.droidcachebox.Geocaching.Cache;
import de.droidcachebox.Geocaching.CacheList;
import de.droidcachebox.Geocaching.Coordinate;
import de.droidcachebox.Geocaching.Waypoint;
import de.droidcachebox.Views.Forms.EditCoordinate;
import de.droidcachebox.Views.Forms.EditWaypoint;
import android.R.drawable;
import android.app.Activity;
import android.app.Instrumentation.ActivityMonitor;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;

public class WaypointView extends ListView implements SelectedCacheEvent, ViewOptionsMenu {
	
	CustomAdapter lvAdapter;
	Activity parentActivity;
	Waypoint aktWaypoint = null;
	
	private Paint paint;
	/**
	 * Constructor
	 */
	public WaypointView(final Context context, final Activity parentActivity) {
		super(context);
		this.parentActivity = parentActivity;
		SelectedCacheEventList.Add(this);
		this.setAdapter(null);
		lvAdapter = new CustomAdapter(getContext(), Global.SelectedCache());
		this.setAdapter(lvAdapter);
		this.setLongClickable(true);
		this.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				aktWaypoint = null;
				if (arg2 > 0)
					aktWaypoint = Global.SelectedCache().waypoints.get(arg2 - 1);
        		Global.SelectedWaypoint(Global.SelectedCache(), aktWaypoint);
    	        
        		
        		Intent mainIntent = new Intent().setClass(getContext(), EditWaypoint.class);
    	        Bundle b = new Bundle();
    	        b.putSerializable("Waypoint", aktWaypoint);
    	        mainIntent.putExtras(b);
        		parentActivity.startActivityForResult(mainIntent, 0);
        		
				return true;
			}
		});
		this.setBackgroundColor(Config.GetBool("nightMode")? Global.Colors.Night.EmptyBackground : Global.Colors.Day.EmptyBackground);
		this.setCacheColorHint(Global.Colors.TitleBarColor);
		this.setDividerHeight(5);
		this.setDivider(getBackground());
		
	}

	public void ActivityResult(int requestCode, int resultCode, Intent data)
	{
		Bundle bundle = data.getExtras();
		if (bundle != null)
		{
			Waypoint waypoint = (Waypoint)bundle.getSerializable("WaypointResult");
			if (waypoint != null)
			{
				aktWaypoint.Title = waypoint.Title;
			}
		}
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
	 
	    public void setCache(Cache cache) {
	    	this.cache = cache;
	    
	    }
	    public int getCount() {
	    	if (cache != null)
	    		return cache.waypoints.size() + 1;
	    	else
	    		return 1;
	    }
	 
	    public Object getItem(int position) {
	    	if (cache != null)
	    	{
	    		if (position == 0)
	    			return cache;
	    		else
	    			return cache.waypoints.get(position - 1);
	    	} else
	    		return null;
	    }
	 
	    public long getItemId(int position) {
	        return position;
	    }
	 
	    public View getView(int position, View convertView, ViewGroup parent)
	    {
	    	if (cache != null)
	    	{
	    		if (position == 0)
	    		{
	    			WaypointViewItem v = new WaypointViewItem(context, cache, null);
	    			return v;
	    		} else
	    		{
			        Waypoint waypoint = cache.waypoints.get(position - 1);
			        WaypointViewItem v = new WaypointViewItem(context, cache, waypoint);
			        return v;
	    		}
	    	} else
	    		return null;
	    }
	 
	    /*public void onClick(View v) {
	            Log.v(LOG_TAG, "Row button clicked");
	    }*/
	 
	}

	@Override
	public void SelectedCacheChanged(Cache cache, Waypoint waypoint) {
		// TODO Auto-generated method stub
		this.setAdapter(null);
		lvAdapter = new CustomAdapter(getContext(), cache);
		this.setAdapter(lvAdapter);
		lvAdapter.notifyDataSetChanged();
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
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnHide() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int GetMenuId() {
		// TODO Auto-generated method stub
		return 0;
	}
}

