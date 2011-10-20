package de.droidcachebox.Views.FilterSettings;

import java.util.ArrayList;
import CB_Core.FilterProperties;
import de.droidcachebox.Global;
import de.droidcachebox.R;
import de.droidcachebox.Events.ViewOptionsMenu;
import de.droidcachebox.Ui.ActivityUtils;
import de.droidcachebox.Ui.Sizes;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import CB_Core.GlobalCore;

public class PresetListView extends ListView implements ViewOptionsMenu {

	public static final String[] presets = new String[] {
            // All Caches
            "0,0,0,0,0,0,0,0,0,1.0,5.0,1.0,5.0,0.0,4.0,0.0,5.0,true,true,true,true,true,true,true,true,true,true,true,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,,,,,", 

            // All Caches to find
            "-1,-1,-1,-1,0,0,0,0,0,1.0,5.0,1.0,5.0,0.0,4.0,0.0,5.0,true,true,true,true,true,true,true,true,true,true,true,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,,,,,", 

            // Quick Cache
            "-1,-1,-1,-1,0,0,0,0,0,1.0,2.5,1.0,2.5,0.0,4.0,0.0,5.0,true,false,false,true,true,false,false,false,false,false,false,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,,,,,",

            // Fetch some Travelbugs
            "-1,-1,0,0,1,0,0,0,0,1.0,3.0,1.0,3.0,0.0,4.0,0.0,5.0,true,false,false,false,false,false,false,false,false,false,false,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,,,,,",

            // Drop off Travelbugs
            "-1,-1,0,0,0,0,0,0,0,1.0,3.0,1.0,3.0,2.0,4.0,0.0,5.0,true,false,false,false,false,false,false,false,false,false,false,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,,,,,",

            // Highlights
            "-1,-1,0,0,0,0,0,0,0,1.0,5.0,1.0,5.0,0.0,4.0,3.5,5.0,true,true,true,true,true,true,true,true,true,true,true,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,,,,,",

            // Favoriten
            "0,0,0,0,0,1,0,0,0,1.0,5.0,1.0,5.0,0.0,4.0,0.0,5.0,true,true,true,true,true,true,true,true,true,true,true,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,,,,,", 

            // prepare to archive
            "0,0,-1,-1,0,-1,-1,-1,0,1.0,5.0,1.0,5.0,0.0,4.0,0.0,5.0,true,true,true,true,true,true,true,true,true,true,true,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,,,,,",
            
            // Listing Changed
            "0,0,0,0,0,0,0,1,0,1.0,5.0,1.0,5.0,0.0,4.0,0.0,5.0,true,true,true,true,true,true,true,true,true,true,true,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,,,,,"

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
				
				EditFilterSettings.tmpFilterProps = new FilterProperties(presets[arg2]);
				
        		invalidate();
				return;
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
	public void OnFree() {
		
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
		addPresetItem(Global.Icons[18], GlobalCore.Translations.Get("AllCaches"),presets[0] );
		addPresetItem(Global.Icons[2], GlobalCore.Translations.Get("AllCachesToFind"),presets[1] );
		addPresetItem(Global.CacheIconsBig[0], GlobalCore.Translations.Get("QuickCaches"),presets[2] );
		addPresetItem(Global.Icons[15], GlobalCore.Translations.Get("GrabTB"),presets[3] );
		addPresetItem(Global.Icons[16], GlobalCore.Translations.Get("DropTB"),presets[4] );
		addPresetItem(Global.Icons[17], GlobalCore.Translations.Get("Highlights"),presets[5] );
		addPresetItem(Global.Icons[19], GlobalCore.Translations.Get("Favorites"),presets[6] );
		addPresetItem(Global.Icons[22], GlobalCore.Translations.Get("PrepareToArchive"),presets[7] );
		addPresetItem(Global.Icons[26], GlobalCore.Translations.Get("ListingChanged"),presets[8] );

	}
	
	private void addPresetItem(Drawable Icon, String Name, String PresetString)
	{
		if(lPresets==null)lPresets=new ArrayList<PresetListView.PresetEntry>();
		lPresets.add(new PresetEntry(Name,Icon,PresetString));
	}
	
    
}


