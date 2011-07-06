package de.droidcachebox.Views;




import CB_Core.GlobalCore;
import CB_Core.Log.Logger;
import CB_Core.Types.Cache;
import CB_Core.Types.Coordinate;
import CB_Core.Types.Waypoint;
import CB_Core.Enums.CacheTypes;

import de.droidcachebox.Config;
import de.droidcachebox.Database;
import de.droidcachebox.Global;
import de.droidcachebox.R;
import de.droidcachebox.Events.SelectedCacheEvent;
import de.droidcachebox.Events.SelectedCacheEventList;
import de.droidcachebox.Events.ViewOptionsMenu;
import de.droidcachebox.Views.Forms.EditWaypoint;
import de.droidcachebox.Views.Forms.MessageBox;
import de.droidcachebox.Views.Forms.MessageBoxButtons;
import de.droidcachebox.Views.Forms.MessageBoxIcon;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
	boolean createNewWaypoint = false;
	Cache aktCache = null;
	
	
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
        		aktCache = Global.SelectedCache();
        		// shutdown AutoResort when selecting a cache or waypoint by hand
        		Global.autoResort = false;
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
					Database.WriteToDatabase(waypoint);
					
				} else
				{
					aktWaypoint.Title = waypoint.Title;
					aktWaypoint.Type = waypoint.Type;
					aktWaypoint.Pos = waypoint.Pos;
					aktWaypoint.Description = waypoint.Description;
					aktWaypoint.Clue = waypoint.Clue;
					Database.UpdateDatabase(aktWaypoint);
					lvAdapter.notifyDataSetChanged();
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
		if (aktCache != cache)
		{
			// Liste nur dann neu Erstellen, wenn der aktuelle Cache geändert wurde
			aktCache = cache;
			this.setAdapter(null);
			lvAdapter = new CustomAdapter(getContext(), cache);
			this.setAdapter(lvAdapter);
			lvAdapter.notifyDataSetChanged();
		} else
			invalidate();
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
					newGcCode = Database.CreateFreeGcCode(Global.SelectedCache().GcCode);
				} catch (Exception e) {
					// 	TODO Auto-generated catch block
					return true;
				}
				Coordinate coord = GlobalCore.LastValidPosition;
				if ((coord == null) || (!coord.Valid))
					coord = Global.SelectedCache().Pos;
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
				        	Database.DeleteFromDatabase(aktWaypoint);
				        	Global.SelectedCache().waypoints.remove(aktWaypoint);
				        	Global.SelectedWaypoint(Global.SelectedCache(), null);
				        	lvAdapter.notifyDataSetChanged();
				            break;
				        case DialogInterface.BUTTON_NEGATIVE:
				            //No button clicked
				            break;
				        }
				        dialog.dismiss();
				    }
				};

				MessageBox.Show(Global.Translations.Get("?DelWP") + "\n\n[" + aktWaypoint.Title + "]", Global.Translations.Get("!DelWP"), MessageBoxButtons.YesNo, MessageBoxIcon.Question, dialogClickListener);
							
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
			Global.TranslateMenuItem(menu, R.id.menu_waypointview_new, "addWaypoint");
			mi = menu.findItem(R.id.menu_waypointview_delete);
			if (mi != null)
			{
				mi.setTitle(Global.Translations.Get("delete"));
				mi.setVisible((aktWaypoint != null) && (aktWaypoint.IsUserWaypoint));
			}
		} catch (Exception e)
		{
			Logger.Error("WaypointView.BeforeShowMenu()", menu.toString(), e);
		}
	}

	@Override
	public void OnShow() {
		// aktuellen Waypoint in der List anzeigen
		int first = this.getFirstVisiblePosition();
		int last = this.getLastVisiblePosition();

		if (Global.SelectedWaypoint() != null)
		{
			aktWaypoint = Global.SelectedWaypoint();
			int id = 0;
			
			for (Waypoint wp : aktCache.waypoints)
			{
				id++;
				if (wp == aktWaypoint)
				{
					if(!(first<id && last>id))
						this.setSelection(id - 2);
					break;
				}
			}
		} else
			this.setSelection(0);
	}

	@Override
	public void OnHide() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnFree() {
		
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

