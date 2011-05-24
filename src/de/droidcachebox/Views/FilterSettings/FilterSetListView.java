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
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class FilterSetListView extends ListView implements ViewOptionsMenu {

	public static FilterSetEntry aktFilterSetEntry;
	
	
	private ArrayList<FilterSetEntry> lFilterSets;
	private ArrayList<FilterSetListViewItem>lFilterSetListViewItems;
	private CustomAdapter lvAdapter;
	public static float lastTouchX;
	public static float lastTouchY;
	
 	public static class FilterSetEntry
	{
		private String mName;
		private Drawable mIcon;
		private Drawable[] mIconArray;
		private int mState=0;
		private int mItemType;
		private int ID;
		private static int IdCounter;
		
		private double mNumerickMax;
		private double mNumerickMin;
		private double mNumerickStep;
		private double mNumerickState;
		
		
		public FilterSetEntry(String Name, Drawable Icon, int itemType)
		{
			mName=Name;
			mIcon=Icon;
			mItemType=itemType;
			ID= IdCounter++;
		}
		
		public FilterSetEntry(String Name, Drawable[] Icons, int itemType, double min, double max, double iniValue, double Step) 
		{
			mName=Name;
			mIconArray=Icons;
			mItemType=itemType;
			mNumerickMin=min;
			mNumerickMax=max;
			mNumerickState=iniValue;
			mNumerickStep=Step;
			ID= IdCounter++;
		}

		public void setState(int State)
		{
			mState=State;
		}
		
		public String getName(){return mName;}
		public Drawable getIcon()
		{
			if(mItemType==NUMERICK_ITEM)
			{
				try
				{
					double ArrayMultiplier = (mIconArray.length>5)? 2:1;
					
					return mIconArray[(int)(mNumerickState*ArrayMultiplier)];
				}catch(Exception e){}
				
			}
			return mIcon;
		}
		public int getState(){return mState;}
		public int getItemType(){return mItemType;}
		public int getID() {return ID;}
		public double getNumState(){return mNumerickState;}

		public void plusClick() 
		{
			mNumerickState += mNumerickStep;
			if(mNumerickState>mNumerickMax) mNumerickState = mNumerickMin;
		}
		public void minusClick() 
		{
			mNumerickState -= mNumerickStep;
			if(mNumerickState<0) mNumerickState = mNumerickMax;
		}
		public void stateClick()
		{
			mState += 1;
			if(mItemType==FilterSetListView.CHECK_ITEM)
			{
				if(mState>1)mState=0;
			}
			else if(mItemType==FilterSetListView.THREE_STATE_ITEM)
			{
				if(mState>2)mState=0;
			}
		}
		
	}
	
	
	
	private Context mContext;
	public FilterSetListView(Context context, final Activity parentActivity) {
		super(context);
		mContext=context;


		fillFilterSetList();
		
		this.setAdapter(null);
		lvAdapter = new CustomAdapter(getContext(), lFilterSets, lFilterSetListViewItems);
		this.setAdapter(lvAdapter);

		this.setOnItemClickListener(new OnItemClickListener() {
			
	        public String[] presets = new String[] {
	            // All Caches
	            "0,0,0,0,0,0,0,0,0,1,5,1,5,0,4,0,5,True,True,True,True,True,True,True,True,True,True,True,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,,,,", 

	            // All Caches to find
	            "-1,-1,-1,-1,0,0,0,0,0,1,5,1,5,0,4,0,5,True,True,True,True,True,True,True,True,True,True,True,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,", 

	            // Quick Cache
	            "-1,-1,-1,-1,0,0,0,0,0,1,2.5,1,2.5,0,4,0,5,True,False,False,True,True,False,False,False,False,False,False,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,",

	            // Fetch some Travelbugs
	            "-1,-1,0,0,1,0,0,0,0,1,3,1,3,0,4,0,5,True,False,False,False,False,False,False,False,False,False,False,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,",

	            // Drop off Travelbugs
	            "-1,-1,0,0,0,0,0,0,0,1,3,1,3,2,4,0,5,True,False,False,False,False,False,False,False,False,False,False,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,",

	            // Highlights
	            "-1,-1,0,0,0,0,0,0,0,1,5,1,5,0,4,3.5,5,True,True,True,True,True,True,True,True,True,True,True,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,",

	            // Favoriten
	            "0,0,0,0,0,1,0,0,0,1,5,1,5,0,4,0,5,True,True,True,True,True,True,True,True,True,True,True,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,", 

	            // prepare to archive
	            "0,0,-1,-1,0,-1,-1,-1,0,1,5,1,5,0,4,0,5,True,True,True,True,True,True,True,True,True,True,True,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,",
	            
	            // Listing Changed
	            "0,0,0,0,0,0,0,1,0,1,5,1,5,0,4,0,5,True,True,True,True,True,True,True,True,True,True,True,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,"

	    };
			
			
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				if(((FilterSetListViewItem)arg1).getFilterSetEntry().getItemType()==FilterSetListView.COLLABSE_BUTTON_ITEM)
					collabseButton_Clicked((FilterSetListViewItem) arg1);
				lvAdapter.notifyDataSetInvalidated();
				invalidate();
				
				Rect HitRec = new Rect();
				arg1.getHitRect(HitRec);
				
				Rect plusBtnHitRec = new Rect(HitRec.width()-HitRec.height(),HitRec.top,HitRec.right,HitRec.bottom);
				Rect minusBtnHitRec = new Rect(HitRec.left,HitRec.top,HitRec.width()+HitRec.height(),HitRec.bottom);
								
				if(((FilterSetListViewItem)arg1).getFilterSetEntry().getItemType()== NUMERICK_ITEM)
				{
					
					if(plusBtnHitRec.contains((int)FilterSetListView.lastTouchX, (int)FilterSetListView.lastTouchY))
					{
						((FilterSetListViewItem)arg1).plusClick();
					}
					else if(minusBtnHitRec.contains((int)FilterSetListView.lastTouchX, (int)FilterSetListView.lastTouchY))
					{
						((FilterSetListViewItem)arg1).minusClick();
					}
				}
				
				if(((FilterSetListViewItem)arg1).getFilterSetEntry().getItemType()== CHECK_ITEM 
						|| ((FilterSetListViewItem)arg1).getFilterSetEntry().getItemType()== THREE_STATE_ITEM)
				{
					
					if(plusBtnHitRec.contains((int)FilterSetListView.lastTouchX, (int)FilterSetListView.lastTouchY))
					{
						((FilterSetListViewItem)arg1).stateClick();
					}
					
				}
				
				return;
			}
		});
		
		this.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				lastTouchX = arg1.getX();
				lastTouchY = arg1.getY();
				return false;
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
	    private ArrayList<FilterSetListViewItem>lFilterSetListViewItems;
	 
	    public CustomAdapter(Context context, ArrayList<FilterSetEntry> lFilterSets,ArrayList<FilterSetListViewItem>FilterSetListViewItems ) {
	        this.context = context;
	        this.filterSetList = lFilterSets;
	        this.lFilterSetListViewItems=FilterSetListViewItems;
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
	        
	        FilterSetListViewItem v=lFilterSetListViewItems.get(position);
	        if(v.getVisibility()==View.GONE)
	        	return new FieldNoteViewItem(context);
	 
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
	public static final int THREE_STATE_ITEM=2;
	public static final int NUMERICK_ITEM=3;
	
	private void fillFilterSetList()
	{
		Resources res = mContext.getResources();
		
		// add General
		FilterSetListViewItem General = addFilterSetCollabseItem(null, "General", COLLABSE_BUTTON_ITEM);
		General.addChild(addFilterSetItem(  Global.Icons[25], "disabled", CHECK_ITEM ));
		General.addChild(addFilterSetItem(  Global.Icons[24], "archived", CHECK_ITEM ));
		General.addChild(addFilterSetItem(  Global.Icons[2], "my finds", CHECK_ITEM ));
		General.addChild(addFilterSetItem(  Global.Icons[17], "my own caches", CHECK_ITEM ));
		General.addChild(addFilterSetItem(  Global.Icons[10], "with trackables", CHECK_ITEM ));
		General.addChild(addFilterSetItem(  Global.Icons[19], "favorites", CHECK_ITEM ));            
		General.addChild(addFilterSetItem(  Global.Icons[21], "has user data", CHECK_ITEM ));
		General.addChild(addFilterSetItem(  Global.Icons[26], "listing changed", CHECK_ITEM ));
		General.addChild(addFilterSetItem(  Global.Icons[26], "manual waypoint", CHECK_ITEM ));
		
		// add D/T
		FilterSetListViewItem DT = addFilterSetCollabseItem(null, "D / T", COLLABSE_BUTTON_ITEM);
		DT.addChild(addFilterSetItem( Global.StarIcons, "Min. Difficulty", NUMERICK_ITEM, 1, 5, 1, 0.5f));
		DT.addChild(addFilterSetItem( Global.StarIcons, "Max. Difficulty", NUMERICK_ITEM, 1, 5, 5, 0.5f));
		DT.addChild(addFilterSetItem( Global.StarIcons, "Min. Terrain", NUMERICK_ITEM, 1, 5, 1, 0.5f));
		DT.addChild(addFilterSetItem( Global.StarIcons, "Max. Terrain", NUMERICK_ITEM, 1, 5, 5, 0.5f));
		DT.addChild(addFilterSetItem( Global.SizeIcons, "Min. Container Size", NUMERICK_ITEM, 0, 4, 0, 1));
		DT.addChild(addFilterSetItem( Global.SizeIcons, "Max. Container Size", NUMERICK_ITEM, 0, 4, 4, 1));
		DT.addChild(addFilterSetItem( Global.StarIcons, "Min. Rating", NUMERICK_ITEM, 0, 5, 0, 0.5f));
		DT.addChild(addFilterSetItem( Global.StarIcons, "Max. Rating", NUMERICK_ITEM, 0, 5, 5, 0.5f));
		
		// add CacheTypes
		FilterSetListViewItem Types = addFilterSetCollabseItem(null, "Cache Types", COLLABSE_BUTTON_ITEM);
		Types.addChild(addFilterSetItem( Global.CacheIconsBig[0], "Traditional", CHECK_ITEM ));
		Types.addChild(addFilterSetItem( Global.CacheIconsBig[1], "Multi-Cache", CHECK_ITEM ));
		Types.addChild(addFilterSetItem( Global.CacheIconsBig[2], "Mystery", CHECK_ITEM ));
		Types.addChild(addFilterSetItem( Global.CacheIconsBig[3], "Webcam Cache", CHECK_ITEM ));
		Types.addChild(addFilterSetItem( Global.CacheIconsBig[4], "Earthcache", CHECK_ITEM ));
		Types.addChild(addFilterSetItem( Global.CacheIconsBig[5], "Event", CHECK_ITEM ));
		Types.addChild(addFilterSetItem( Global.CacheIconsBig[6], "Mega Event", CHECK_ITEM ));
		Types.addChild(addFilterSetItem( Global.CacheIconsBig[7], "Cache In Trash Out", CHECK_ITEM ));
		Types.addChild(addFilterSetItem( Global.CacheIconsBig[8], "Virtual Cache", CHECK_ITEM ));
		Types.addChild(addFilterSetItem( Global.CacheIconsBig[9], "Letterbox", CHECK_ITEM ));
		Types.addChild(addFilterSetItem( Global.CacheIconsBig[10], "Wherigo", CHECK_ITEM ));
		
		//add Attributes
		FilterSetListViewItem Attr = addFilterSetCollabseItem(null, "Attributes", COLLABSE_BUTTON_ITEM);
		Attr.addChild(addFilterSetItem(res.getDrawable(R.drawable.att_1_1), "Dogs", THREE_STATE_ITEM ));
        Attr.addChild(addFilterSetItem(res.getDrawable(R.drawable.att_32_1), "Bicycles", THREE_STATE_ITEM ));
        Attr.addChild(addFilterSetItem(res.getDrawable(R.drawable.att_33_1), "Motorcycles", THREE_STATE_ITEM ));
        Attr.addChild(addFilterSetItem(res.getDrawable(R.drawable.att_34_1), "Quads", THREE_STATE_ITEM ));
        Attr.addChild(addFilterSetItem(res.getDrawable(R.drawable.att_35_1), "Off-road vehicles", THREE_STATE_ITEM ));
        Attr.addChild(addFilterSetItem(res.getDrawable(R.drawable.att_36_1), "Snowmobiles", THREE_STATE_ITEM ));
        Attr.addChild(addFilterSetItem(res.getDrawable(R.drawable.att_37_1), "Horses", THREE_STATE_ITEM ));
        Attr.addChild(addFilterSetItem(res.getDrawable(R.drawable.att_38_1), "Campfires", THREE_STATE_ITEM ));
        Attr.addChild(addFilterSetItem(res.getDrawable(R.drawable.att_46_1), "Truck Driver/RV", THREE_STATE_ITEM ));

        Attr.addChild(addFilterSetItem(res.getDrawable(R.drawable.att_2_1), "Access or parking fee", THREE_STATE_ITEM ));
        Attr.addChild(addFilterSetItem(res.getDrawable(R.drawable.att_3_1), "Climbing gear", THREE_STATE_ITEM ));
        Attr.addChild(addFilterSetItem(res.getDrawable(R.drawable.att_4_1), "Boat", THREE_STATE_ITEM ));
        Attr.addChild(addFilterSetItem(res.getDrawable(R.drawable.att_5_1), "Scuba gear", THREE_STATE_ITEM ));
        Attr.addChild(addFilterSetItem(res.getDrawable(R.drawable.att_44_1), "Flashlight required", THREE_STATE_ITEM ));
        Attr.addChild(addFilterSetItem(res.getDrawable(R.drawable.att_48_1), "UV Light Required", THREE_STATE_ITEM ));
        Attr.addChild(addFilterSetItem(res.getDrawable(R.drawable.att_49_1), "Snowshoes", THREE_STATE_ITEM ));
        Attr.addChild(addFilterSetItem(res.getDrawable(R.drawable.att_50_1), "Cross Country Skis", THREE_STATE_ITEM ));
        Attr.addChild(addFilterSetItem(res.getDrawable(R.drawable.att_51_1), "Special Tool Required", THREE_STATE_ITEM ));

        Attr.addChild(addFilterSetItem(res.getDrawable(R.drawable.att_6_1), "Recommended for kids", THREE_STATE_ITEM ));
        Attr.addChild(addFilterSetItem(res.getDrawable(R.drawable.att_7_1), "Takes less than an hour", THREE_STATE_ITEM ));
        Attr.addChild(addFilterSetItem(res.getDrawable(R.drawable.att_8_1), "Scenic view", THREE_STATE_ITEM ));
        Attr.addChild(addFilterSetItem(res.getDrawable(R.drawable.att_9_1), "Significant hike", THREE_STATE_ITEM ));
        Attr.addChild(addFilterSetItem(res.getDrawable(R.drawable.att_10_1), "Difficult climbing", THREE_STATE_ITEM ));
        Attr.addChild(addFilterSetItem(res.getDrawable(R.drawable.att_11_1), "May require wading", THREE_STATE_ITEM ));
        Attr.addChild(addFilterSetItem(res.getDrawable(R.drawable.att_12_1), "May require swimming", THREE_STATE_ITEM ));
        Attr.addChild(addFilterSetItem(res.getDrawable(R.drawable.att_13_1), "Available at all times", THREE_STATE_ITEM ));
        Attr.addChild(addFilterSetItem(res.getDrawable(R.drawable.att_14_1), "Recommended at night", THREE_STATE_ITEM ));
        Attr.addChild(addFilterSetItem(res.getDrawable(R.drawable.att_15_1), "Available during winter", THREE_STATE_ITEM ));
        Attr.addChild(addFilterSetItem(res.getDrawable(R.drawable.att_40_1), "Stealth required", THREE_STATE_ITEM ));
        Attr.addChild(addFilterSetItem(res.getDrawable(R.drawable.att_42_1), "Needs maintenance", THREE_STATE_ITEM ));
        Attr.addChild(addFilterSetItem(res.getDrawable(R.drawable.att_43_1), "Watch for livestock", THREE_STATE_ITEM ));
        Attr.addChild(addFilterSetItem(res.getDrawable(R.drawable.att_47_1), "Field Puzzle", THREE_STATE_ITEM ));
        Attr.addChild(addFilterSetItem(res.getDrawable(R.drawable.att_52_1), "Night Cache", THREE_STATE_ITEM ));
        Attr.addChild(addFilterSetItem(res.getDrawable(R.drawable.att_53_1), "Park and Grab", THREE_STATE_ITEM ));
        Attr.addChild(addFilterSetItem(res.getDrawable(R.drawable.att_54_1), "Abandoned Structure", THREE_STATE_ITEM ));
        Attr.addChild(addFilterSetItem(res.getDrawable(R.drawable.att_55_1), "Short hike (less than 1km)", THREE_STATE_ITEM ));
        Attr.addChild(addFilterSetItem(res.getDrawable(R.drawable.att_56_1), "Medium hike (1km-10km)", THREE_STATE_ITEM ));
        Attr.addChild(addFilterSetItem(res.getDrawable(R.drawable.att_57_1), "Long Hike (+10km)", THREE_STATE_ITEM ));

        Attr.addChild(addFilterSetItem(res.getDrawable(R.drawable.att_17_1), "Poison plants", THREE_STATE_ITEM ));
        Attr.addChild(addFilterSetItem(res.getDrawable(R.drawable.att_18_1), "Dangerous Animals", THREE_STATE_ITEM ));
        Attr.addChild(addFilterSetItem(res.getDrawable(R.drawable.att_19_1), "Ticks", THREE_STATE_ITEM ));
        Attr.addChild(addFilterSetItem(res.getDrawable(R.drawable.att_20_1), "Abandoned mines", THREE_STATE_ITEM ));
        Attr.addChild(addFilterSetItem(res.getDrawable(R.drawable.att_21_1), "Cliff / falling rocks", THREE_STATE_ITEM ));
        Attr.addChild(addFilterSetItem(res.getDrawable(R.drawable.att_22_1), "Hunting", THREE_STATE_ITEM ));
        Attr.addChild(addFilterSetItem(res.getDrawable(R.drawable.att_23_1), "Dangerous Area", THREE_STATE_ITEM ));
        Attr.addChild(addFilterSetItem(res.getDrawable(R.drawable.att_39_1), "Thorns", THREE_STATE_ITEM ));

        Attr.addChild(addFilterSetItem(res.getDrawable(R.drawable.att_24_1), "Wheelchair accessible", THREE_STATE_ITEM ));
        Attr.addChild(addFilterSetItem(res.getDrawable(R.drawable.att_25_1), "Parking available", THREE_STATE_ITEM ));
        Attr.addChild(addFilterSetItem(res.getDrawable(R.drawable.att_26_1), "Public transportation", THREE_STATE_ITEM ));
        Attr.addChild(addFilterSetItem(res.getDrawable(R.drawable.att_27_1), "Drinking water nearby", THREE_STATE_ITEM ));
        Attr.addChild(addFilterSetItem(res.getDrawable(R.drawable.att_28_1), "Public restrooms nearby", THREE_STATE_ITEM ));
        Attr.addChild(addFilterSetItem(res.getDrawable(R.drawable.att_29_1), "Telephone nearby", THREE_STATE_ITEM ));
        Attr.addChild(addFilterSetItem(res.getDrawable(R.drawable.att_30_1), "Picnic tables nearby", THREE_STATE_ITEM ));
        Attr.addChild(addFilterSetItem(res.getDrawable(R.drawable.att_31_1), "Camping available", THREE_STATE_ITEM ));
        Attr.addChild(addFilterSetItem(res.getDrawable(R.drawable.att_41_1), "Stroller accessible", THREE_STATE_ITEM ));
        Attr.addChild(addFilterSetItem(res.getDrawable(R.drawable.att_58_1), "Fuel Nearby", THREE_STATE_ITEM ));
        Attr.addChild(addFilterSetItem(res.getDrawable(R.drawable.att_59_1), "Food Nearby", THREE_STATE_ITEM ));
		
		

	}
	
	private FilterSetListViewItem addFilterSetItem(Drawable[] Icons,
			String Name, int ItemType, double i, double j, double k, double f) 
	{

		if(lFilterSets==null)
		{
			lFilterSets = new ArrayList<FilterSetListView.FilterSetEntry>();
			lFilterSetListViewItems = new ArrayList<FilterSetListViewItem>();
		}
		FilterSetEntry tmp = new FilterSetEntry(Name,Icons,ItemType,i,j,k,f);
		lFilterSets.add(tmp);
		Boolean BackGroundChanger = ((lFilterSets.size() % 2) == 1);
		FilterSetListViewItem v = new FilterSetListViewItem(mContext, tmp, BackGroundChanger);
		// inital mit GONE
		v.setVisibility(View.GONE);
		lFilterSetListViewItems.add(v);
		return v;
		
	}

	private FilterSetListViewItem addFilterSetItem(Drawable Icon, String Name, int ItemType)
	{
		if(lFilterSets==null)
		{
			lFilterSets = new ArrayList<FilterSetListView.FilterSetEntry>();
			lFilterSetListViewItems = new ArrayList<FilterSetListViewItem>();
		}
		FilterSetEntry tmp = new FilterSetEntry(Name,Icon,ItemType);
		lFilterSets.add(tmp);
		Boolean BackGroundChanger = ((lFilterSets.size() % 2) == 1);
		FilterSetListViewItem v = new FilterSetListViewItem(mContext, tmp, BackGroundChanger);
		// inital mit GONE
		v.setVisibility(View.GONE);
		lFilterSetListViewItems.add(v);
		return v;
	}
	
	private FilterSetListViewItem addFilterSetCollabseItem(Drawable Icon, String Name, int ItemType)
	{
		if(lFilterSets==null)
		{
			lFilterSets = new ArrayList<FilterSetListView.FilterSetEntry>();
			lFilterSetListViewItems = new ArrayList<FilterSetListViewItem>();
		}
		FilterSetEntry tmp = new FilterSetEntry(Name,Icon,ItemType);
		lFilterSets.add(tmp);
		Boolean BackGroundChanger = ((lFilterSets.size() % 2) == 1);
		FilterSetListViewItem v = new FilterSetListViewItem(mContext, tmp, BackGroundChanger);
		lFilterSetListViewItems.add(v);
		return v;
	}
	
    
	
	private void collabseButton_Clicked(FilterSetListViewItem item)
	{
		item.toggleChildeViewState();
		this.invalidate();
	}
	
}


