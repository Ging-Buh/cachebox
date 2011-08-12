package de.droidcachebox.Views;




import CB_Core.GlobalCore;
import CB_Core.Log.Logger;
import CB_Core.Types.Cache;
import CB_Core.Types.Coordinate;
import CB_Core.Types.Waypoint;
import CB_Core.Enums.CacheTypes;

import CB_Core.Config;
import de.droidcachebox.Database;
import de.droidcachebox.Global;
import de.droidcachebox.R;
import CB_Core.Events.SelectedCacheEvent;
import CB_Core.Events.SelectedCacheEventList;
import de.droidcachebox.DAO.WaypointDAO;
import de.droidcachebox.Events.ViewOptionsMenu;
import de.droidcachebox.Ui.ActivityUtils;
import de.droidcachebox.Ui.AllContextMenuCallHandler;
import de.droidcachebox.Views.Forms.EditCoordinate;
import de.droidcachebox.Views.Forms.EditWaypoint;
import de.droidcachebox.Views.Forms.MessageBox;
import de.droidcachebox.Views.Forms.MessageBoxButtons;
import de.droidcachebox.Views.Forms.MessageBoxIcon;
import de.droidcachebox.Views.Forms.projectionCoordinate;
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
	public Waypoint aktWaypoint = null;
	boolean createNewWaypoint = false;
	public Cache aktCache = null;
	
	
	/**
	 * Constructor
	 */
	public WaypointView(final Context context, final Activity parentActivity) {
		super(context);
		this.parentActivity = parentActivity;
		SelectedCacheEventList.Add(this);
		this.setAdapter(null);
		lvAdapter = new CustomAdapter(getContext(), GlobalCore.SelectedCache());
		this.setAdapter(lvAdapter);
		this.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				aktWaypoint = null;
				if (arg2 > 0)
					aktWaypoint = GlobalCore.SelectedCache().waypoints.get(arg2 - 1);
        		aktCache = GlobalCore.SelectedCache();
        		// shutdown AutoResort when selecting a cache or waypoint by hand
        		Global.autoResort = false;
        		GlobalCore.SelectedWaypoint(GlobalCore.SelectedCache(), aktWaypoint);
			}
		});
		ActivityUtils.setListViewPropertys(this);
		
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
					GlobalCore.SelectedCache().waypoints.add(waypoint);
					this.setAdapter(lvAdapter);
					aktWaypoint = waypoint;
					GlobalCore.SelectedWaypoint(GlobalCore.SelectedCache(), waypoint);
		        	WaypointDAO waypointDAO = new WaypointDAO();
					waypointDAO.WriteToDatabase(waypoint);
					
				} else
				{
					aktWaypoint.Title = waypoint.Title;
					aktWaypoint.Type = waypoint.Type;
					aktWaypoint.Pos = waypoint.Pos;
					aktWaypoint.Description = waypoint.Description;
					aktWaypoint.Clue = waypoint.Clue;
		        	WaypointDAO waypointDAO = new WaypointDAO();
					waypointDAO.UpdateDatabase(aktWaypoint);
					lvAdapter.notifyDataSetChanged();
				}
			}
			
			Coordinate coord = (Coordinate)bundle.getSerializable("CoordResult");
			if (coord != null)
			{
				if (createNewWaypoint)
				{
					String newGcCode = "";
					try {
						newGcCode = Database.CreateFreeGcCode(GlobalCore.SelectedCache().GcCode);
					} catch (Exception e) {
						
						return ;
					}
					Waypoint newWP = new Waypoint(newGcCode, CacheTypes.ReferencePoint, "Entered Manually", coord.Latitude, coord.Longitude, GlobalCore.SelectedCache().Id, "", "projiziert");
					GlobalCore.SelectedCache().waypoints.add(newWP);
					this.setAdapter(lvAdapter);
					aktWaypoint = newWP;
					GlobalCore.SelectedWaypoint(GlobalCore.SelectedCache(), newWP);
		        	WaypointDAO waypointDAO = new WaypointDAO();
					waypointDAO.WriteToDatabase(newWP);
					
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
	    		return 0;
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
	public void OnShow() {
		// aktuellen Waypoint in der List anzeigen
		int first = this.getFirstVisiblePosition();
		int last = this.getLastVisiblePosition();

		if (aktCache== null) return;
		
		if (GlobalCore.SelectedWaypoint() != null)
		{
			aktWaypoint = GlobalCore.SelectedWaypoint();
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

	
	
	
	@Override public int GetMenuId() {return 0;}
	@Override public int GetContextMenuId() {return 0;}
	@Override public void BeforeShowContextMenu(Menu menu) {}
	@Override public boolean ContextMenuItemSelected(MenuItem item) {return false;}
	@Override
	public boolean ItemSelected(MenuItem item) 
	{
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
					newGcCode = Database.CreateFreeGcCode(GlobalCore.SelectedCache().GcCode);
				} catch (Exception e) {
					// 	TODO Auto-generated catch block
					return true;
				}
				Coordinate coord = GlobalCore.LastValidPosition;
				if ((coord == null) || (!coord.Valid))
					coord = GlobalCore.SelectedCache().Pos;
                Waypoint newWP = new Waypoint(newGcCode, CacheTypes.ReferencePoint, "Entered Manually", coord.Latitude, coord.Longitude, GlobalCore.SelectedCache().Id, "", "manual");
				Intent mainIntent = new Intent().setClass(getContext(), EditWaypoint.class);
				Bundle b = new Bundle();
				b.putSerializable("Waypoint", newWP);
				mainIntent.putExtras(b);
	    		parentActivity.startActivityForResult(mainIntent, 0);
				break;
			case R.id.menu_waypointview_project:
				createNewWaypoint = true;
				
				Coordinate coord2 = GlobalCore.LastValidPosition;
				if (aktWaypoint != null)
				{
					coord2 = aktWaypoint.Pos;
				}
				else if(aktCache != null)
				{
					coord2 = aktCache.Pos;
				}
				
				Intent coordIntent = new Intent().setClass(getContext(), projectionCoordinate.class);
		        Bundle b2 = new Bundle();
		        b2.putSerializable("Coord", coord2);
		        b2.putSerializable("Title", Global.Translations.Get("Projection"));
		        b2.putSerializable("Radius", false);
		        coordIntent.putExtras(b2);
		        parentActivity.startActivityForResult(coordIntent, 0);
				break;
			case R.id.menu_waypointview_delete:
				DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
				    @Override
				    public void onClick(DialogInterface dialog, int which) {
				        switch (which){
				        case DialogInterface.BUTTON_POSITIVE:
				            //Yes button clicked
				        	Database.DeleteFromDatabase(aktWaypoint);
				        	GlobalCore.SelectedCache().waypoints.remove(aktWaypoint);
				        	GlobalCore.SelectedWaypoint(GlobalCore.SelectedCache(), null);
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
	public void BeforeShowMenu(Menu menu) 
	{
		AllContextMenuCallHandler.showWayPointViewContextMenu();
	}

	
}

