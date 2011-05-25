package de.droidcachebox.Views.FilterSettings;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import de.droidcachebox.Config;
import de.droidcachebox.Database;
import de.droidcachebox.FilterProperties;
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

	public static final String[] presets = new String[] {
            // All Caches
            "0,0,0,0,0,0,0,0,0,1.0,5.0,1.0,5.0,0.0,4.0,0.0,5.0,true,true,true,true,true,true,true,true,true,true,true,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,,,,", 

            // All Caches to find
            "-1,-1,-1,-1,0,0,0,0,0,1.0,5.0,1.0,5.0,0.0,4.0,0.0,5.0,true,true,true,true,true,true,true,true,true,true,true,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,,,,", 

            // Quick Cache
            "-1,-1,-1,-1,0,0,0,0,0,1.0,2.5,1.0,2.5,0.0,4.0,0.0,5.0,true,false,false,true,true,false,false,false,false,false,false,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,,,,",

            // Fetch some Travelbugs
            "-1,-1,0,0,1,0,0,0,0,1.0,3.0,1.0,3.0,0.0,4.0,0.0,5.0,true,false,false,false,false,false,false,false,false,false,false,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,,,,",

            // Drop off Travelbugs
            "-1,-1,0,0,0,0,0,0,0,1.0,3.0,1.0,3.0,2.0,4.0,0.0,5.0,true,false,false,false,false,false,false,false,false,false,false,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,,,,",

            // Highlights
            "-1,-1,0,0,0,0,0,0,0,1.0,5.0,1.0,5.0,0.0,4.0,3.5,5.0,true,true,true,true,true,true,true,true,true,true,true,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,,,,",

            // Favoriten
            "0,0,0,0,0,1,0,0,0,1.0,5.0,1.0,5.0,0.0,4.0,0.0,5.0,true,true,true,true,true,true,true,true,true,true,true,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,,,,", 

            // prepare to archive
            "0,0,-1,-1,0,-1,-1,-1,0,1.0,5.0,1.0,5.0,0.0,4.0,0.0,5.0,true,true,true,true,true,true,true,true,true,true,true,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,,,,",
            
            // Listing Changed
            "0,0,0,0,0,0,0,1,0,1.0,5.0,1.0,5.0,0.0,4.0,0.0,5.0,true,true,true,true,true,true,true,true,true,true,true,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,,,,"

    };
	
	public static PresetEntry aktPreset;
	
	//private Activity parentActivity;
	private ArrayList<PresetEntry> lPresets;
	private CustomAdapter lvAdapter;
	
	public class PresetEntry
	{
		private String mName;
		private Drawable mIcon;
		private String mPresetString;
		
		public PresetEntry(String Name, Drawable Icon, String PresetString)
		{
			mName=Name;
			mIcon=Icon;
			mPresetString=PresetString;
		}
		
		public String getName(){return mName;}
		public Drawable getIcone(){return mIcon;}
		public String getPresetString(){return mPresetString;}
	}
	
	
	public PresetListView(Context context, final Activity parentActivity) {
		super(context);
	//	this.parentActivity = parentActivity;


		fillPresetList();
		
		this.setAdapter(null);
		lvAdapter = new CustomAdapter(getContext(), lPresets);
		this.setAdapter(lvAdapter);

		this.setOnItemClickListener(new OnItemClickListener() 
		{
	        	
			
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				
				Global.LastFilter = new FilterProperties(presets[arg2]);
				
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
		addPresetItem(Global.Icons[18], "All caches" ,presets[0] );
		addPresetItem(Global.Icons[2], "All caches to find",presets[1] );
		addPresetItem(Global.CacheIconsBig[0], "Quick caches",presets[2] );
		addPresetItem(Global.Icons[15], "Grab some travelbugs",presets[3] );
		addPresetItem(Global.Icons[16], "Drop off travelbugs",presets[4] );
		addPresetItem(Global.Icons[17], "Highlights",presets[5] );
		addPresetItem(Global.Icons[19], "Favorites",presets[6] );
		addPresetItem(Global.Icons[22], "Prepare to archive",presets[7] );
		addPresetItem(Global.Icons[26], "Listing changed",presets[8] );

	}
	
	private void addPresetItem(Drawable Icon, String Name, String PresetString)
	{
		if(lPresets==null)lPresets=new ArrayList<PresetListView.PresetEntry>();
		lPresets.add(new PresetEntry(Name,Icon,PresetString));
	}
	
    
}


