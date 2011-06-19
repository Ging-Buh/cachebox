package de.droidcachebox.Views;


import de.droidcachebox.Config;
import de.droidcachebox.Global;
import de.droidcachebox.R;

import de.droidcachebox.Events.SelectedCacheEvent;
import de.droidcachebox.Events.SelectedCacheEventList;
import de.droidcachebox.Events.ViewOptionsMenu;
import de.droidcachebox.Geocaching.Cache;
import de.droidcachebox.Geocaching.JokerEntry;
import de.droidcachebox.Geocaching.JokerList;
import de.droidcachebox.Geocaching.Waypoint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;


public class JokerView extends ListView implements SelectedCacheEvent, ViewOptionsMenu {
	
	CustomAdapter lvAdapter;
	Activity parentActivity;
	Cache aktCache = null;
	
	private Paint paint;
	/**
	 * Constructor
	 */
	public JokerView(final Context context, final Activity parentActivity) {
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
/*				aktWaypoint = null;
				if (arg2 > 0)
					aktWaypoint = Global.SelectedCache().waypoints.get(arg2 - 1);
        		aktCache = Global.SelectedCache();
        		Global.SelectedWaypoint(Global.SelectedCache(), aktWaypoint);*/
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

    @Override
	public void SelectedCacheChanged(Cache cache, Waypoint waypoint) {
		if (aktCache != cache)
		{
			// Wwenn der aktuelle Cache ge�ndert wurde, Telefonjokerliste l�schen
			aktCache = cache;
			Global.Jokers.ClearList();
			this.setAdapter(null);
			lvAdapter = new CustomAdapter(getContext(), cache);
			this.setAdapter(lvAdapter);
			lvAdapter.notifyDataSetChanged();
		} else
			invalidate();
	}

	public void ActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (data == null)
			return;
		Bundle bundle = data.getExtras();
		if (bundle != null)
		{
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
	    		return Global.Jokers.size() + 1;
	    	else
	    		return 0;
	    }
	 
	    public Object getItem(int position) {
	    	if (cache != null)
	    	{
	    		if (position == 0)
	    			return cache;
	    		else
	    			return Global.Jokers.get(position - 1);
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
			    JokerEntry joker = Global.Jokers.get(position);
			    JokerViewItem v = new JokerViewItem(context, cache, joker,BackGroundChanger);
			    return v;
	    	} else
	    		return null;
	    }
	 
	    /*public void onClick(View v) {
	            Log.v(LOG_TAG, "Row button clicked");
	    }*/
	 
	}


	@Override
	public boolean ItemSelected(MenuItem item) {
		switch (item.getItemId())
		{
			case R.id.menu_jokerview_call: 
/*			if (aktWaypoint != null)
			{
				createNewWaypoint = false;
	    		Intent mainIntent = new Intent().setClass(getContext(), EditWaypoint.class);
		        Bundle b = new Bundle();
		        b.putSerializable("Waypoint", aktWaypoint);
		        mainIntent.putExtras(b);
	    		parentActivity.startActivityForResult(mainIntent, 0);
			}*/
			break;
		}
		return true;
	}

	@Override
	public void BeforeShowMenu(Menu menu) {
		try
		{
			MenuItem mi = menu.findItem(R.id.menu_jokerview_call);
			if (mi != null)
			{
				mi.setTitle(Global.Translations.Get("call"));
//				mi.setVisible(aktWaypoint != null);
			}
		} catch (Exception exc)
		{
			return;
		}
	}

	@Override
	public void OnShow() {
		// aktuellen Joker in der List anzeigen
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
		return R.menu.menu_jokerview;
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

