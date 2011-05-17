package de.droidcachebox.Views;


import de.droidcachebox.Config;
import de.droidcachebox.Database;
import de.droidcachebox.Global;
import de.droidcachebox.R;
import de.droidcachebox.main;
import de.droidcachebox.splash;

import de.droidcachebox.Events.SelectedCacheEvent;
import de.droidcachebox.Events.SelectedCacheEventList;
import de.droidcachebox.Events.ViewOptionsMenu;
import de.droidcachebox.Geocaching.Cache;
import de.droidcachebox.Geocaching.Cache.CacheTypes;
import de.droidcachebox.Geocaching.CacheList;
import de.droidcachebox.Geocaching.Coordinate;
import de.droidcachebox.Geocaching.Waypoint;
import de.droidcachebox.Map.Layer;
import de.droidcachebox.Views.Forms.EditCoordinate;
import de.droidcachebox.Views.Forms.EditWaypoint;
import android.R.drawable;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Instrumentation.ActivityMonitor;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class WaypointView extends ListView implements SelectedCacheEvent, ViewOptionsMenu {
	
	CustomAdapter lvAdapter;
	Activity parentActivity;
	Waypoint aktWaypoint = null;
	boolean createNewWaypoint = false;
	
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
		this.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				aktWaypoint = null;
				if (arg2 > 0)
					aktWaypoint = Global.SelectedCache().waypoints.get(arg2 - 1);
        		Global.SelectedWaypoint(Global.SelectedCache(), aktWaypoint);
			}
		});
		this.setBackgroundColor(Config.GetBool("nightMode")? R.color.Night_EmptyBackground : R.color.Day_EmptyBackground);
		this.setCacheColorHint(R.color.Day_TitleBarColor);
		this.setDividerHeight(5);
		this.setDivider(getBackground());
		
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

	public void ActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (data == null)
			return;
		Bundle bundle = data.getExtras();
		if (bundle != null)
		{
			Waypoint waypoint = (Waypoint)bundle.getSerializable("WaypointResult");
			if (waypoint != null)
			{
				if (createNewWaypoint)
				{
					Global.SelectedCache().waypoints.add(waypoint);
					this.setAdapter(lvAdapter);
					aktWaypoint = waypoint;
					Global.SelectedWaypoint(Global.SelectedCache(), waypoint);
					waypoint.WriteToDatabase();
					
				} else
				{
					aktWaypoint.Title = waypoint.Title;
					aktWaypoint.Type = waypoint.Type;
					aktWaypoint.Coordinate = waypoint.Coordinate;
					aktWaypoint.Description = waypoint.Description;
					aktWaypoint.Clue = waypoint.Clue;
					aktWaypoint.UpdateDatabase();
				}
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
	    		 Boolean BackGroundChanger = ((position % 2) == 1);
	    		if (position == 0)
	    		{
	    			WaypointViewItem v = new WaypointViewItem(context, cache, null,BackGroundChanger);
	    			return v;
	    		} else
	    		{
			        Waypoint waypoint = cache.waypoints.get(position - 1);
			        WaypointViewItem v = new WaypointViewItem(context, cache, waypoint,BackGroundChanger);
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
		switch (item.getItemId())
		{
			case R.id.menu_waypointview_edit: 
			if (aktWaypoint != null)
			{
				createNewWaypoint = false;
	    		Intent mainIntent = new Intent().setClass(getContext(), EditWaypoint.class);
		        Bundle b = new Bundle();
		        b.putSerializable("Waypoint", aktWaypoint);
		        mainIntent.putExtras(b);
	    		parentActivity.startActivityForResult(mainIntent, 0);
			}
			break;
			case R.id.menu_waypointview_new:
				createNewWaypoint = true;
				String newGcCode = "";
				try {
					newGcCode = Waypoint.CreateFreeGcCode(Global.SelectedCache().GcCode);
				} catch (Exception e) {
					// 	TODO Auto-generated catch block
					return true;
				}
				Coordinate coord = Global.LastValidPosition;
				if ((coord == null) || (!coord.Valid))
					coord = Global.SelectedCache().Coordinate;
                Waypoint newWP = new Waypoint(newGcCode, CacheTypes.ReferencePoint, "Entered Manually", coord.Latitude, coord.Longitude, Global.SelectedCache().Id, "", "manual");
				Intent mainIntent = new Intent().setClass(getContext(), EditWaypoint.class);
				Bundle b = new Bundle();
				b.putSerializable("Waypoint", newWP);
				mainIntent.putExtras(b);
	    		parentActivity.startActivityForResult(mainIntent, 0);
				break;
			case R.id.menu_waypointview_delete:
				DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
				    @Override
				    public void onClick(DialogInterface dialog, int which) {
				        switch (which){
				        case DialogInterface.BUTTON_POSITIVE:
				            //Yes button clicked
				        	aktWaypoint.DeleteFromDatabase();
				        	Global.SelectedCache().waypoints.remove(aktWaypoint);
				        	Global.SelectedWaypoint(Global.SelectedCache(), null);
				            break;
				        case DialogInterface.BUTTON_NEGATIVE:
				            //No button clicked
				            break;
				        }
				    }
				};

				AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
				builder.setMessage(Global.Translations.Get("?DelWP") + "\n\n[" + aktWaypoint.Title + "]")
					.setTitle(Global.Translations.Get("!DelWP"))
					.setPositiveButton(Global.Translations.Get("yes"), dialogClickListener)
				    .setNegativeButton(Global.Translations.Get("no"), dialogClickListener).show();
		}
		return true;
	}

	@Override
	public void BeforeShowMenu(Menu menu) {
		try
		{
			MenuItem mi = menu.findItem(R.id.menu_waypointview_edit);
			if (mi != null)
			{
				mi.setTitle(Global.Translations.Get("edit"));
				mi.setVisible(aktWaypoint != null);
			}
			Global.Translations.TranslateMenuItem(menu, R.id.menu_waypointview_new, "addWaypoint");
			mi = menu.findItem(R.id.menu_waypointview_delete);
			if (mi != null)
			{
				mi.setTitle(Global.Translations.Get("delete"));
				mi.setVisible((aktWaypoint != null) && (aktWaypoint.IsUserWaypoint));
			}
		} catch (Exception exc)
		{
			return;
		}
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
		return R.menu.menu_waypointview;
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

