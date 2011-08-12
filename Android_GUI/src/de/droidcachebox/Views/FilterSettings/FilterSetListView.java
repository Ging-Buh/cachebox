package de.droidcachebox.Views.FilterSettings;

import java.util.ArrayList;
import CB_Core.FilterProperties;
import de.droidcachebox.Global;
import de.droidcachebox.R;
import de.droidcachebox.Events.ViewOptionsMenu;
import de.droidcachebox.Ui.ActivityUtils;
import de.droidcachebox.Ui.Sizes;
import de.droidcachebox.Views.FieldNoteViewItem;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;

public class FilterSetListView extends ListView implements ViewOptionsMenu {

	public static FilterSetEntry aktFilterSetEntry;
	public static final int COLLABSE_BUTTON_ITEM=0;
	public static final int CHECK_ITEM=1;
	public static final int THREE_STATE_ITEM=2;
	public static final int NUMERICK_ITEM=3;
	public static float lastTouchX;
	public static float lastTouchY;
	public static int windowW=0;
    public static int windowH=0 ;
    
    private static FilterSetListViewItem NotAvailable;
	private static FilterSetListViewItem Archived;
	private static FilterSetListViewItem Finds;
	private static FilterSetListViewItem Own;
	private static FilterSetListViewItem ContainsTravelBugs;
	private static FilterSetListViewItem Favorites;
	private static FilterSetListViewItem HasUserData;
	private static FilterSetListViewItem ListingChanged;
	private static FilterSetListViewItem WithManualWaypoint;
	private static FilterSetListViewItem minTerrain;
	private static FilterSetListViewItem maxTerrain;
	private static FilterSetListViewItem minDifficulty;
	private static FilterSetListViewItem maxDifficulty;
	private static FilterSetListViewItem minContainerSize;
	private static FilterSetListViewItem maxContainerSize;
	private static FilterSetListViewItem minRating;
	private static FilterSetListViewItem maxRating;
	private static FilterSetListViewItem Types;
	private static FilterSetListViewItem Attr ;
    
	private ArrayList<FilterSetEntry> lFilterSets;
	private ArrayList<FilterSetListViewItem>lFilterSetListViewItems;
	private CustomAdapter lvAdapter;
	private Context mContext;
	
	
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
		public void setState(float State)
		{
			mNumerickState=State;
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
				if(mState>1)mState=-1;
			}
		}
		
	}
	
 	public FilterSetListView(Context context, final Activity parentActivity) {
		super(context);
		mContext=context;


		fillFilterSetList();
		
		this.setAdapter(null);
		lvAdapter = new CustomAdapter(getContext(), lFilterSets, lFilterSetListViewItems);
		this.setAdapter(lvAdapter);

		this.setOnItemClickListener(new OnItemClickListener() 
		{
			
	       		
			
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) 
			{
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
						SetFilter();
					}
					else if(minusBtnHitRec.contains((int)FilterSetListView.lastTouchX, (int)FilterSetListView.lastTouchY))
					{
						((FilterSetListViewItem)arg1).minusClick();
						SetFilter();
					}
				}
				
				if(((FilterSetListViewItem)arg1).getFilterSetEntry().getItemType()== CHECK_ITEM 
						|| ((FilterSetListViewItem)arg1).getFilterSetEntry().getItemType()== THREE_STATE_ITEM)
				{
					
					if(plusBtnHitRec.contains((int)FilterSetListView.lastTouchX, (int)FilterSetListView.lastTouchY))
					{
						((FilterSetListViewItem)arg1).stateClick();
						SetFilter();
					}
					
				}
				
				return;
			}
			
			private void SetFilter()
			{
				EditFilterSettings.tmpFilterProps = FilterSetListView.SaveFilterProperties();
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

		ActivityUtils.setListViewPropertys(this);
		
		
	}

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
	public void OnShow() 
	{
		if(EditFilterSettings.tmpFilterProps != null && !EditFilterSettings.tmpFilterProps.ToString().equals(""))
		{
			LoadFilterProperties(EditFilterSettings.tmpFilterProps);
		}
	
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

	private void fillFilterSetList()
	{
		Resources res = mContext.getResources();
		
		// add General
		FilterSetListViewItem General = addFilterSetCollabseItem(null, "General", COLLABSE_BUTTON_ITEM);
		NotAvailable = General.addChild(addFilterSetItem(  Global.Icons[25], "disabled", THREE_STATE_ITEM ));
		Archived = General.addChild(addFilterSetItem(  Global.Icons[24], "archived", THREE_STATE_ITEM ));
		Finds = General.addChild(addFilterSetItem(  Global.Icons[2], "my finds", THREE_STATE_ITEM ));
		Own = General.addChild(addFilterSetItem(  Global.Icons[17], "my own caches", THREE_STATE_ITEM ));
		ContainsTravelBugs = General.addChild(addFilterSetItem(  Global.Icons[10], "with trackables", THREE_STATE_ITEM ));
		Favorites = General.addChild(addFilterSetItem(  Global.Icons[19], "favorites", THREE_STATE_ITEM ));            
		HasUserData = General.addChild(addFilterSetItem(  Global.Icons[21], "has user data", THREE_STATE_ITEM ));
		ListingChanged = General.addChild(addFilterSetItem(  Global.Icons[26], "listing changed", THREE_STATE_ITEM ));
		WithManualWaypoint = General.addChild(addFilterSetItem(  Global.Icons[26], "manual waypoint", THREE_STATE_ITEM ));
		
		// add D/T
		FilterSetListViewItem DT = addFilterSetCollabseItem(null, "D / T", COLLABSE_BUTTON_ITEM);
		minDifficulty = DT.addChild(addFilterSetItem( Global.StarIcons, "Min. Difficulty", NUMERICK_ITEM, 1, 5, 1, 0.5f));
		maxDifficulty = DT.addChild(addFilterSetItem( Global.StarIcons, "Max. Difficulty", NUMERICK_ITEM, 1, 5, 5, 0.5f));
		minTerrain = DT.addChild(addFilterSetItem( Global.StarIcons, "Min. Terrain", NUMERICK_ITEM, 1, 5, 1, 0.5f));
		maxTerrain = DT.addChild(addFilterSetItem( Global.StarIcons, "Max. Terrain", NUMERICK_ITEM, 1, 5, 5, 0.5f));
		minContainerSize = DT.addChild(addFilterSetItem( Global.SizeIcons, "Min. Container Size", NUMERICK_ITEM, 0, 4, 0, 1));
		maxContainerSize = DT.addChild(addFilterSetItem( Global.SizeIcons, "Max. Container Size", NUMERICK_ITEM, 0, 4, 4, 1));
		minRating = DT.addChild(addFilterSetItem( Global.StarIcons, "Min. Rating", NUMERICK_ITEM, 0, 5, 0, 0.5f));
		maxRating = DT.addChild(addFilterSetItem( Global.StarIcons, "Max. Rating", NUMERICK_ITEM, 0, 5, 5, 0.5f));
		
		// add CacheTypes
		Types = addFilterSetCollabseItem(null, "Cache Types", COLLABSE_BUTTON_ITEM);
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
		Attr = addFilterSetCollabseItem(null, "Attributes", COLLABSE_BUTTON_ITEM);
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
	

	
	public void LoadFilterProperties(FilterProperties props)
    {
        NotAvailable.setValue(props.NotAvailable);
        Archived.setValue(props.Archived);
        Finds.setValue(props.Finds);
        Own.setValue(props.Own);
        ContainsTravelBugs.setValue(props.ContainsTravelbugs);
        Favorites.setValue(props.Favorites);           
        HasUserData.setValue(props.HasUserData);
        ListingChanged.setValue(props.ListingChanged);
        WithManualWaypoint.setValue(props.WithManualWaypoint);
        
        minTerrain.setValue(props.MinTerrain);
        maxTerrain.setValue(props.MaxTerrain);
        minDifficulty.setValue(props.MinDifficulty);
        maxDifficulty.setValue(props.MaxDifficulty);
        minContainerSize.setValue(props.MinContainerSize);
        maxContainerSize.setValue(props.MaxContainerSize);
        minRating.setValue(props.MinRating);
        maxRating.setValue(props.MaxRating);
        

        for (int i = 0; i < 11; i++)
        	Types.getChild(i).setValue(props.cacheTypes[i]);

        for (int i = 0; i < Attr.getChildLength(); i++)
        {
            if (i < props.attributesFilter.length)
            	Attr.getChild(i).setValue(props.attributesFilter[i]);
        }

    }
	
	public static FilterProperties SaveFilterProperties()
     {
         FilterProperties props = new FilterProperties();
         props.NotAvailable = NotAvailable.getChecked();
         props.Archived = Archived.getChecked();
         props.Finds = Finds.getChecked();
         props.Own = Own.getChecked();
         props.ContainsTravelbugs = ContainsTravelBugs.getChecked();
         props.Favorites = Favorites.getChecked();            
         props.HasUserData = HasUserData.getChecked();
         props.ListingChanged = ListingChanged.getChecked();
         props.WithManualWaypoint = WithManualWaypoint.getChecked();

         props.MinDifficulty = minDifficulty.getValue();
         props.MaxDifficulty = maxDifficulty.getValue();
         props.MinTerrain = minTerrain.getValue();
         props.MaxTerrain = maxTerrain.getValue();
         props.MinContainerSize = minContainerSize.getValue();
         props.MaxContainerSize = maxContainerSize.getValue();
         props.MinRating = minRating.getValue();
         props.MaxRating = maxRating.getValue();

         for (int i = 0; i < 11; i++)
             props.cacheTypes[i] = Types.getChild(i).getBoolean();

         for (int i = 0; i < Attr.getChildLength(); i++)
         {
             if (i < props.attributesFilter.length)
                 props.attributesFilter[i] = Attr.getChild(i).getChecked();
         }
        
         return props;
     }

	
}


