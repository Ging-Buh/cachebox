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
import android.content.res.Resources;
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

public class FilterSetListView extends ListView implements ViewOptionsMenu {

	public static FilterSetEntry aktFilterSetEntry;
	
	
	private ArrayList<FilterSetEntry> lFilterSets;
	private CustomAdapter lvAdapter;
	
	public static class FilterSetEntry
	{
		private String mName;
		private Drawable mIcon;
		private int mState=0;
		private int mItemType;
		private int ID;
		private static int IdCounter;
		private ArrayList<Integer> mChildList = new ArrayList<Integer>();
		
		public FilterSetEntry(String Name, Drawable Icon, int itemType)
		{
			mName=Name;
			mIcon=Icon;
			mItemType=itemType;
			ID= IdCounter++;
		}
		
		public void setState(int State)
		{
			mState=State;
		}
		
		public String getName(){return mName;}
		public Drawable getIcone(){return mIcon;}
		public int getState(){return mState;}
		public int getItemType(){return mItemType;}
		public int getID() {return ID;}
		public void addChild(int ID)
		{
			mChildList.add(ID);
		}
	}
	
	
	
	private Context mContext;
	public FilterSetListView(Context context, final Activity parentActivity) {
		super(context);
		mContext=context;


		fillFilterSetList();
		
		this.setAdapter(null);
		lvAdapter = new CustomAdapter(getContext(), lFilterSets);
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
	    private ArrayList<FilterSetEntry> filterSetList;
	 
	    public CustomAdapter(Context context, ArrayList<FilterSetEntry> lFilterSets ) {
	        this.context = context;
	        this.filterSetList = lFilterSets;
	    }
	 
	    public int getCount() {
	        return filterSetList.size();
	    }
	 
	    public Object getItem(int position) {
	        return filterSetList.get(position);
	    }
	 
	    public long getItemId(int position) {
	        return position;
	    }
	 
	    public View getView(int position, View convertView, ViewGroup parent)
	    {
	        FilterSetEntry fne = filterSetList.get(position);
	        Boolean BackGroundChanger = ((position % 2) == 1);
	        FilterSetListViewItem v = new FilterSetListViewItem(context, fne, BackGroundChanger);
	 
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

	public static final int COLLABSE_BUTTON_ITEM=0;
	public static final int CHECK_ITEM=1;
	public static final int THRE_STATE_ITEM=2;
	
	private void fillFilterSetList()
	{
		Resources res = mContext.getResources();
				
		//add Attributes
		FilterSetEntry Attr = addFilterSetCollabseItem(Global.Icons[2], "All caches to find", COLLABSE_BUTTON_ITEM);
		Attr.addChild(addFilterSetItem(res.getDrawable(R.drawable.att_1_1), "Dogs", THRE_STATE_ITEM ));
        Attr.addChild(addFilterSetItem(res.getDrawable(R.drawable.att_32_1), "Bicycles", THRE_STATE_ITEM ));
        Attr.addChild(addFilterSetItem(res.getDrawable(R.drawable.att_33_1), "Motorcycles", THRE_STATE_ITEM ));
        Attr.addChild(addFilterSetItem(res.getDrawable(R.drawable.att_34_1), "Quads", THRE_STATE_ITEM ));
        Attr.addChild(addFilterSetItem(res.getDrawable(R.drawable.att_35_1), "Off-road vehicles", THRE_STATE_ITEM ));
        Attr.addChild(addFilterSetItem(res.getDrawable(R.drawable.att_36_1), "Snowmobiles", THRE_STATE_ITEM ));
        Attr.addChild(addFilterSetItem(res.getDrawable(R.drawable.att_37_1), "Horses", THRE_STATE_ITEM ));
        Attr.addChild(addFilterSetItem(res.getDrawable(R.drawable.att_38_1), "Campfires", THRE_STATE_ITEM ));
        Attr.addChild(addFilterSetItem(res.getDrawable(R.drawable.att_46_1), "Truck Driver/RV", THRE_STATE_ITEM ));

        Attr.addChild(addFilterSetItem(res.getDrawable(R.drawable.att_2_1), "Access or parking fee", THRE_STATE_ITEM ));
        Attr.addChild(addFilterSetItem(res.getDrawable(R.drawable.att_3_1), "Climbing gear", THRE_STATE_ITEM ));
        Attr.addChild(addFilterSetItem(res.getDrawable(R.drawable.att_4_1), "Boat", THRE_STATE_ITEM ));
        Attr.addChild(addFilterSetItem(res.getDrawable(R.drawable.att_5_1), "Scuba gear", THRE_STATE_ITEM ));
        Attr.addChild(addFilterSetItem(res.getDrawable(R.drawable.att_44_1), "Flashlight required", THRE_STATE_ITEM ));
        Attr.addChild(addFilterSetItem(res.getDrawable(R.drawable.att_48_1), "UV Light Required", THRE_STATE_ITEM ));
        Attr.addChild(addFilterSetItem(res.getDrawable(R.drawable.att_49_1), "Snowshoes", THRE_STATE_ITEM ));
        Attr.addChild(addFilterSetItem(res.getDrawable(R.drawable.att_50_1), "Cross Country Skis", THRE_STATE_ITEM ));
        Attr.addChild(addFilterSetItem(res.getDrawable(R.drawable.att_51_1), "Special Tool Required", THRE_STATE_ITEM ));

        Attr.addChild(addFilterSetItem(res.getDrawable(R.drawable.att_6_1), "Recommended for kids", THRE_STATE_ITEM ));
        Attr.addChild(addFilterSetItem(res.getDrawable(R.drawable.att_7_1), "Takes less than an hour", THRE_STATE_ITEM ));
        Attr.addChild(addFilterSetItem(res.getDrawable(R.drawable.att_8_1), "Scenic view", THRE_STATE_ITEM ));
        Attr.addChild(addFilterSetItem(res.getDrawable(R.drawable.att_9_1), "Significant hike", THRE_STATE_ITEM ));
        Attr.addChild(addFilterSetItem(res.getDrawable(R.drawable.att_10_1), "Difficult climbing", THRE_STATE_ITEM ));
        Attr.addChild(addFilterSetItem(res.getDrawable(R.drawable.att_11_1), "May require wading", THRE_STATE_ITEM ));
        Attr.addChild(addFilterSetItem(res.getDrawable(R.drawable.att_12_1), "May require swimming", THRE_STATE_ITEM ));
        Attr.addChild(addFilterSetItem(res.getDrawable(R.drawable.att_13_1), "Available at all times", THRE_STATE_ITEM ));
        Attr.addChild(addFilterSetItem(res.getDrawable(R.drawable.att_14_1), "Recommended at night", THRE_STATE_ITEM ));
        Attr.addChild(addFilterSetItem(res.getDrawable(R.drawable.att_15_1), "Available during winter", THRE_STATE_ITEM ));
        Attr.addChild(addFilterSetItem(res.getDrawable(R.drawable.att_40_1), "Stealth required", THRE_STATE_ITEM ));
        Attr.addChild(addFilterSetItem(res.getDrawable(R.drawable.att_42_1), "Needs maintenance", THRE_STATE_ITEM ));
        Attr.addChild(addFilterSetItem(res.getDrawable(R.drawable.att_43_1), "Watch for livestock", THRE_STATE_ITEM ));
        Attr.addChild(addFilterSetItem(res.getDrawable(R.drawable.att_47_1), "Field Puzzle", THRE_STATE_ITEM ));
        Attr.addChild(addFilterSetItem(res.getDrawable(R.drawable.att_52_1), "Night Cache", THRE_STATE_ITEM ));
        Attr.addChild(addFilterSetItem(res.getDrawable(R.drawable.att_53_1), "Park and Grab", THRE_STATE_ITEM ));
        Attr.addChild(addFilterSetItem(res.getDrawable(R.drawable.att_54_1), "Abandoned Structure", THRE_STATE_ITEM ));
        Attr.addChild(addFilterSetItem(res.getDrawable(R.drawable.att_55_1), "Short hike (less than 1km)", THRE_STATE_ITEM ));
        Attr.addChild(addFilterSetItem(res.getDrawable(R.drawable.att_56_1), "Medium hike (1km-10km)", THRE_STATE_ITEM ));
        Attr.addChild(addFilterSetItem(res.getDrawable(R.drawable.att_57_1), "Long Hike (+10km)", THRE_STATE_ITEM ));

        Attr.addChild(addFilterSetItem(res.getDrawable(R.drawable.att_17_1), "Poison plants", THRE_STATE_ITEM ));
        Attr.addChild(addFilterSetItem(res.getDrawable(R.drawable.att_18_1), "Dangerous Animals", THRE_STATE_ITEM ));
        Attr.addChild(addFilterSetItem(res.getDrawable(R.drawable.att_19_1), "Ticks", THRE_STATE_ITEM ));
        Attr.addChild(addFilterSetItem(res.getDrawable(R.drawable.att_20_1), "Abandoned mines", THRE_STATE_ITEM ));
        Attr.addChild(addFilterSetItem(res.getDrawable(R.drawable.att_21_1), "Cliff / falling rocks", THRE_STATE_ITEM ));
        Attr.addChild(addFilterSetItem(res.getDrawable(R.drawable.att_22_1), "Hunting", THRE_STATE_ITEM ));
        Attr.addChild(addFilterSetItem(res.getDrawable(R.drawable.att_23_1), "Dangerous Area", THRE_STATE_ITEM ));
        Attr.addChild(addFilterSetItem(res.getDrawable(R.drawable.att_39_1), "Thorns", THRE_STATE_ITEM ));

        Attr.addChild(addFilterSetItem(res.getDrawable(R.drawable.att_24_1), "Wheelchair accessible", THRE_STATE_ITEM ));
        Attr.addChild(addFilterSetItem(res.getDrawable(R.drawable.att_25_1), "Parking available", THRE_STATE_ITEM ));
        Attr.addChild(addFilterSetItem(res.getDrawable(R.drawable.att_26_1), "Public transportation", THRE_STATE_ITEM ));
        Attr.addChild(addFilterSetItem(res.getDrawable(R.drawable.att_27_1), "Drinking water nearby", THRE_STATE_ITEM ));
        Attr.addChild(addFilterSetItem(res.getDrawable(R.drawable.att_28_1), "Public restrooms nearby", THRE_STATE_ITEM ));
        Attr.addChild(addFilterSetItem(res.getDrawable(R.drawable.att_29_1), "Telephone nearby", THRE_STATE_ITEM ));
        Attr.addChild(addFilterSetItem(res.getDrawable(R.drawable.att_30_1), "Picnic tables nearby", THRE_STATE_ITEM ));
        Attr.addChild(addFilterSetItem(res.getDrawable(R.drawable.att_31_1), "Camping available", THRE_STATE_ITEM ));
        Attr.addChild(addFilterSetItem(res.getDrawable(R.drawable.att_41_1), "Stroller accessible", THRE_STATE_ITEM ));
        Attr.addChild(addFilterSetItem(res.getDrawable(R.drawable.att_58_1), "Fuel Nearby", THRE_STATE_ITEM ));
        Attr.addChild(addFilterSetItem(res.getDrawable(R.drawable.att_59_1), "Food Nearby", THRE_STATE_ITEM ));
		
		

	}
	
	private int addFilterSetItem(Drawable Icon, String Name, int ItemType)
	{
		if(lFilterSets==null)lFilterSets=new ArrayList<FilterSetListView.FilterSetEntry>();
		FilterSetEntry tmp = new FilterSetEntry(Name,Icon,ItemType);
		lFilterSets.add(tmp);
		return tmp.getID();
	}
	
	private FilterSetEntry addFilterSetCollabseItem(Drawable Icon, String Name, int ItemType)
	{
		if(lFilterSets==null)lFilterSets=new ArrayList<FilterSetListView.FilterSetEntry>();
		FilterSetEntry tmp = new FilterSetEntry(Name,Icon,ItemType);
		lFilterSets.add(tmp);
		return tmp;
	}
	
    
}


