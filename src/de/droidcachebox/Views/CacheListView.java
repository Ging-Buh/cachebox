package de.droidcachebox.Views;

import java.util.List;

import de.droidcachebox.Config;
import de.droidcachebox.Database;
import de.droidcachebox.Global;

import de.droidcachebox.Events.ViewOptionsMenu;
import de.droidcachebox.Geocaching.Cache;
import de.droidcachebox.Geocaching.CacheList;
import android.R.drawable;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;

public class CacheListView extends ListView implements ViewOptionsMenu {
	

	private Paint paint;
	/**
	 * Constructor
	 */
	public CacheListView(final Context context) {
		super(context);

		
		this.setAdapter(null);
		CustomAdapter lvAdapter = new CustomAdapter(getContext(), Database.Data.Query);
		this.setAdapter(lvAdapter);
		this.setLongClickable(true);
		this.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
        		Cache cache = Database.Data.Query.get(arg2);
        		Global.SelectedCache(cache);
        		invalidate();
				return;
			}
		});
/*		this.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
        		Cache cache = Database.Data.Query.get(arg2);
        		Global.SelectedCache(cache);
				return true;
			}
		});*/
		this.setBackgroundColor(Config.GetBool("nightMode")? Global.Colors.Night.EmptyBackground : Global.Colors.Day.EmptyBackground);
		this.setCacheColorHint(Global.Colors.TitleBarColor);
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
	        CacheListViewItem v = new CacheListViewItem(context, cache);
	 
	        //v.setBackgroundColor((position % 2) == 1 ? Color.rgb(50,50,50) : Color.BLACK);
	 
	        /*v.setOnClickListener(new OnItemClickListener(position));*/
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
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnHide() {
		// TODO Auto-generated method stub
		
	}
}

