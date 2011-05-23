package de.droidcachebox.Views.FilterSettings;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import de.droidcachebox.Config;
import de.droidcachebox.Database;
import de.droidcachebox.Global;
import de.droidcachebox.R;
import de.droidcachebox.Events.SelectedCacheEvent;
import de.droidcachebox.Events.ViewOptionsMenu;
import de.droidcachebox.Geocaching.Cache;
import de.droidcachebox.Geocaching.CacheList;
import de.droidcachebox.Geocaching.FieldNoteEntry;
import de.droidcachebox.Geocaching.FieldNoteList;
import de.droidcachebox.Geocaching.Waypoint;
import de.droidcachebox.Views.FieldNoteViewItem;
import de.droidcachebox.Views.CacheListView.CustomAdapter;
import de.droidcachebox.Views.Forms.EditFieldNote;
import de.droidcachebox.Views.Forms.EditWaypoint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Path.FillType;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class PresetListView extends ListView implements ViewOptionsMenu {

	public static PresetEntry aktPreset;
	
	//private Activity parentActivity;
	private ArrayList<PresetEntry> lPresets;
	private CustomAdapter lvAdapter;
	
	public class PresetEntry
	{
		private String mName;
		private Drawable mIcon;
		
		
		public PresetEntry(String Name, Drawable Icon)
		{
			mName=Name;
			mIcon=Icon;
		}
		
		public String getName(){return mName;}
		public Drawable getIcone(){return mIcon;}
	}
	
	
	public PresetListView(Context context, final Activity parentActivity) {
		super(context);
	//	this.parentActivity = parentActivity;


		fillPresetList();
		
		this.setAdapter(null);
		lvAdapter = new CustomAdapter(getContext(), lPresets);
		this.setAdapter(lvAdapter);

		this.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				
        		invalidate();
				return;
			}
		});

		this.setBackgroundColor(Global.getColor(R.attr.EmptyBackground));
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

    public class CustomAdapter extends BaseAdapter {
		 
	    private Context context;
	    private ArrayList<PresetEntry> presetList;
	 
	    public CustomAdapter(Context context, ArrayList<PresetEntry> lPresets ) {
	        this.context = context;
	        this.presetList = lPresets;
	    }
	 
	    public int getCount() {
	        return presetList.size();
	    }
	 
	    public Object getItem(int position) {
	        return presetList.get(position);
	    }
	 
	    public long getItemId(int position) {
	        return position;
	    }
	 
	    public View getView(int position, View convertView, ViewGroup parent)
	    {
	        PresetEntry fne = presetList.get(position);
	        Boolean BackGroundChanger = ((position % 2) == 1);
	        PresetListViewItem v = new PresetListViewItem(context, fne, BackGroundChanger);
	 
	        return v;
	    }
	}
	
	
	
	
	
	@Override
	public boolean ItemSelected(MenuItem item) {
		
		switch (item.getItemId())
		{
			

		}
		return false;
	}

		@Override
	public void BeforeShowMenu(Menu menu) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int GetMenuId() {
		// TODO Auto-generated method stub
		return R.menu.menu_fieldnotesview;
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
	public void ActivityResult(int requestCode, int resultCode, Intent data) {
		if (data == null)
			return;
		Bundle bundle = data.getExtras();
		if (bundle != null)
		{																	   	
		}
		
	}
	

	@Override
	public int GetContextMenuId() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void BeforeShowContextMenu(Menu menu) 
	{
		// TODO Auto-generated method stub
	}

	@Override
	public boolean ContextMenuItemSelected(MenuItem item) {
		switch (item.getItemId())
		{
		
		}
		return false;
	}

	
	
	private void fillPresetList()
	{
		addPresetItem(Global.Icons[18], "All caches" );
		addPresetItem(Global.Icons[2], "All caches to find" );
		addPresetItem(Global.CacheIconsBig[0], "Quick caches" );
		addPresetItem(Global.Icons[15], "Grab some travelbugs" );
		addPresetItem(Global.Icons[16], "Drop off travelbugs" );
		addPresetItem(Global.Icons[17], "Highlights" );
		addPresetItem(Global.Icons[19], "Favorites" );
		addPresetItem(Global.Icons[22], "Prepare to archive" );
		addPresetItem(Global.Icons[26], "Listing changed" );

	}
	
	private void addPresetItem(Drawable Icon, String Name)
	{
		if(lPresets==null)lPresets=new ArrayList<PresetListView.PresetEntry>();
		lPresets.add(new PresetEntry(Name,Icon));
	}
	
    
}


