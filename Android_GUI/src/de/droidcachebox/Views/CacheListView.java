package de.droidcachebox.Views;

import CB_Core.GlobalCore;
import CB_Core.DB.Database;
import de.droidcachebox.Global;
import de.droidcachebox.Events.PositionEvent;
import de.droidcachebox.Events.PositionEventList;
import de.droidcachebox.Events.ViewOptionsMenu;
import de.droidcachebox.Ui.ActivityUtils;
import de.droidcachebox.Ui.AllContextMenuCallHandler;
import CB_Core.Types.CacheList;

import CB_Core.Events.CachListChangedEventList;
import CB_Core.Types.Cache;
import CB_Core.Types.Waypoint;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;

public class CacheListView extends ListView implements ViewOptionsMenu, PositionEvent, CB_Core.Events.CacheListChangedEvent {
	
	private CustomAdapter lvAdapter;
	
	/**
	 * Constructor
	 */
	public CacheListView(final Context context) {
		super(context);

		CachListChangedEventList.Add(this);
		this.setAdapter(null);
		try
		{
			lvAdapter = new CustomAdapter(getContext(), Database.Data.Query);
			this.setAdapter(lvAdapter);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
//		this.setLongClickable(true);
		this.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
        		Cache cache = Database.Data.Query.get(arg2);
        		
        		Waypoint finalWp = null;
        		if (cache.HasFinalWaypoint())
        			finalWp = cache.GetFinalWaypoint();
        		// shutdown AutoResort when selecting a cache by hand
        		Global.autoResort = false;
        		GlobalCore.SelectedWaypoint(cache, finalWp);

        		invalidate();
				return;
			}
		});

		ActivityUtils.setListViewPropertys(this);
		this.setDrawingCacheQuality(DRAWING_CACHE_QUALITY_LOW);
		this.setChildrenDrawingCacheEnabled(true);
		
		
		this.setOnScrollListener(new OnScrollListener()
		{
			
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState)
			{
				 if(scrollState == 2)
				 {
					 CacheListViewItem.isFastScrolling=true;
				 }
				 else 
				 {
					 CacheListViewItem.isFastScrolling=false;
				 }
			}
			
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount)
			{
								
			}
		});
		  

		
		
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
	public void BeforeShowMenu(Menu menu) 
	{
		AllContextMenuCallHandler.showCachelistViewContextMenu();
		
	}

	@Override
	public void OnShow() {
		PositionEventList.Add(this);
		ActivityUtils.setListViewPropertys(this);
		// aktuellen Cache in der List anzeigen
		if (GlobalCore.SelectedCache() != null)
		{
			int last =this.getLastVisiblePosition();
			
			if (last==-1 && Database.Data.Query.size()>0 )
			{
				lvAdapter.notifyDataSetChanged();
			}
			
			setSelectedCacheVisible();
			
		} else
			this.setSelection(0);		
	}
	
	
	/**
	 * setzt den Aktuell selectierten Cache an die 2. Pos in der Liste
	 */
	public void setSelectedCacheVisible()
	{
		setSelectedCacheVisible(2);
	}
	
	/**
	 * setzt den Aktuell selectierten Cache an pos
	 * @param pos
	 */
	public void setSelectedCacheVisible(int pos)
	{
		int id = 0;
		int first = this.getFirstVisiblePosition();
		int last =this.getLastVisiblePosition();
		
		for (Cache ca : Database.Data.Query)
		{
			if (ca == GlobalCore.SelectedCache())
			{
				if(!(first<id && last>id))
					this.setSelection(id - pos);
				break;
			}
			id++;
		}
	}

	@Override
	public void OnHide() {
		PositionEventList.Remove(this);
		
	}

	@Override
	public void OnFree() {
		
	}

	@Override
	public int GetMenuId() 
	{
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

	public void notifyCacheListChange()
	{
		lvAdapter.notifyDataSetChanged();
	}


}

