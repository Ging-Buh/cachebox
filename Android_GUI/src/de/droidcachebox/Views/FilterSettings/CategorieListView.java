package de.droidcachebox.Views.FilterSettings;

import java.util.ArrayList;
import de.droidcachebox.FilterProperties;
import de.droidcachebox.Global;
import de.droidcachebox.R;
import de.droidcachebox.Components.ActivityUtils;
import de.droidcachebox.Events.ViewOptionsMenu;
import de.droidcachebox.Ui.Sizes;
import de.droidcachebox.Views.FieldNoteViewItem;
import CB_Core.GlobalCore;
import CB_Core.Log.Logger;
import CB_Core.Types.Categories;
import CB_Core.Types.Category;
import CB_Core.Types.GpxFilename;
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

public class CategorieListView extends ListView implements ViewOptionsMenu {

	public static CategorieEntry aktCategorieEntry;
	public static final int COLLABSE_BUTTON_ITEM=0;
	public static final int CHECK_ITEM=1;
	public static final int THREE_STATE_ITEM=2;
	public static final int NUMERICK_ITEM=3;
	public static float lastTouchX;
	public static float lastTouchY;
	public static int windowW=0;
    public static int windowH=0 ;
    
    
    
	private ArrayList<CategorieEntry> lCategories;
	private ArrayList<CategorieListViewItem>lCategorieListViewItems;
	private CustomAdapter lvAdapter;
	private Context mContext;
	
	
	public static class CategorieEntry
	{
		private GpxFilename mFile;
		private Category mCat;
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
		
		
		public CategorieEntry(GpxFilename file, Drawable Icon, int itemType)
		{
			mCat=null;
			mFile=file;
			mIcon=Icon;
			mItemType=itemType;
			ID= IdCounter++;
			
		}
		
		public CategorieEntry(Category cat, Drawable Icon, int itemType)
		{
			mCat=cat;
			mFile=null;
			mIcon=Icon;
			mItemType=itemType;
			ID= IdCounter++;
			
		}
		
		public CategorieEntry(GpxFilename file, Drawable[] Icons, int itemType, double min, double max, double iniValue, double Step) 
		{
			mFile=file;
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
		
		public GpxFilename getFile(){return mFile;}
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
			
			if(mItemType==COLLABSE_BUTTON_ITEM)
			{
				//collabs Button chk clicked
				int State = mCat.getChek();
				if (State == 0)
				{//keins ausgewählt, also alle anwählen
					
					for(GpxFilename tmp : mCat)
					{
						tmp.Checked=true;
					}
					
				}
				else 
				{//einer oder mehr ausgewählt, also alle abwählen
					
					for(GpxFilename tmp : mCat)
					{
						tmp.Checked=false;
					}
					
				}
			}
			else
			{
				mNumerickState += mNumerickStep;
				if(mNumerickState>mNumerickMax) mNumerickState = mNumerickMin;
			}
			
			
		}
		public void minusClick() 
		{
			if(mItemType==COLLABSE_BUTTON_ITEM)
			{
				// Collabs Button Pin Clicked
				this.mCat.pinned=!this.mCat.pinned;
				
			}
			else
			{
				mNumerickState -= mNumerickStep;
				if(mNumerickState<0) mNumerickState = mNumerickMax;
			}
		}
		public void stateClick()
		{
			mState += 1;
			if(mItemType==CategorieListView.CHECK_ITEM || mItemType==CategorieListView.COLLABSE_BUTTON_ITEM)
			{
				if(mState>1)mState=0;
			}
			else if(mItemType==CategorieListView.THREE_STATE_ITEM)
			{
				if(mState>1)mState=-1;
			}
			
		}

		public String getCatName() 
		{
			return mCat.GpxFilename;
		}

		public Category getCat() 
		{
			return mCat;
		}
		
	}
	
 	public CategorieListView(Context context, final Activity parentActivity) {
		super(context);
		mContext=context;


		fillCategorieList();
		
		this.setAdapter(null);
		lvAdapter = new CustomAdapter(getContext(), lCategories, lCategorieListViewItems);
		this.setAdapter(lvAdapter);

		this.setOnItemClickListener(new OnItemClickListener() 
		{
			
	       		
			
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) 
			{
				
				
//				if(((CategorieListViewItem)arg1).getCategorieEntry().getItemType()==CategorieListView.COLLABSE_BUTTON_ITEM)
					
				
				Rect HitRec = new Rect();
				arg1.getHitRect(HitRec);
				
				Rect plusBtnHitRec = new Rect(HitRec.width()-HitRec.height(),HitRec.top,HitRec.right,HitRec.bottom);
				Rect minusBtnHitRec = new Rect(HitRec.left,HitRec.top,HitRec.height(),HitRec.bottom);
								
				if(((CategorieListViewItem)arg1).getCategorieEntry().getItemType()== NUMERICK_ITEM)
				{
					
					if(plusBtnHitRec.contains((int)CategorieListView.lastTouchX, (int)CategorieListView.lastTouchY))
					{
						((CategorieListViewItem)arg1).plusClick();
						SetCategory();
					}
					else if(minusBtnHitRec.contains((int)CategorieListView.lastTouchX, (int)CategorieListView.lastTouchY))
					{
						((CategorieListViewItem)arg1).minusClick();
						SetCategory();
					}
				}
				
				if(((CategorieListViewItem)arg1).getCategorieEntry().getItemType()== CHECK_ITEM 
						|| ((CategorieListViewItem)arg1).getCategorieEntry().getItemType()== THREE_STATE_ITEM)
				{
					
					if(plusBtnHitRec.contains((int)CategorieListView.lastTouchX, (int)CategorieListView.lastTouchY))
					{
						((CategorieListViewItem)arg1).stateClick();
						SetCategory();
					}
					
				}
				
				if(((CategorieListViewItem)arg1).getCategorieEntry().getItemType()== COLLABSE_BUTTON_ITEM)
				{
					if(plusBtnHitRec.contains((int)CategorieListView.lastTouchX, (int)CategorieListView.lastTouchY))
					{
						((CategorieListViewItem)arg1).plusClick();
						if(lCategories!=null)
						{
							for(CategorieEntry tmp : lCategories)
							{
								GpxFilename file = tmp.getFile();
								if (file!=null)
								{
									tmp.setState(file.Checked? 1:0);
								}
								
							}
						}
						SetCategory();
					}
					else if(minusBtnHitRec.contains((int)CategorieListView.lastTouchX, (int)CategorieListView.lastTouchY))
					{
						((CategorieListViewItem)arg1).minusClick();
						SetCategory();
					}
					else
					{
						collabseButton_Clicked((CategorieListViewItem) arg1);
						lvAdapter.notifyDataSetInvalidated();
					}
					
					
				}
				invalidate();
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

		ActivityUtils.setListViewPropertys(this);
		
		
	}
 	
 	public void SetCategory()
	{
		//Set Categorie State
		if(lCategorieListViewItems!=null)
		{
			for(CategorieListViewItem tmp : lCategorieListViewItems)
			{
				GpxFilename file = tmp.categorieEntry.getFile();
				
				for (Category cat : GlobalCore.Categories)
				{
					int index = cat.indexOf(file);
					if(index!=-1)
					{
						
							cat.get(index).Checked=(tmp.categorieEntry.getState()==1)? true:false;
						
					}
					else
					{
						if(tmp.getCategorieEntry().getCat()!=null)
						{
							if(cat==tmp.getCategorieEntry().getCat())
							{
								cat.pinned= tmp.getCategorieEntry().getCat().pinned;
							}
							
						}
						
					}
										
				}
				
			}
		}
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
	    private ArrayList<CategorieEntry> categorieList;
	    private ArrayList<CategorieListViewItem>lCategoriesListViewItems;
	 
	    public CustomAdapter(Context context, ArrayList<CategorieEntry> lCategories,ArrayList<CategorieListViewItem>CategorieListViewItems ) {
	        this.context = context;
	        this.categorieList = lCategories;
	        this.lCategoriesListViewItems=CategorieListViewItems;
	    }
	 
	    public int getCount() 
	    {
	    	if(categorieList==null)return 0;
	    	return categorieList.size();
	    }
	 
	    public Object getItem(int position) 
	    {
	    	if(categorieList==null)return null;
	        return categorieList.get(position);
	    }
	 
	    public long getItemId(int position) {
	        return position;
	    }
	 
	    public View getView(int position, View convertView, ViewGroup parent)
	    {
	        if (lCategoriesListViewItems==null)return null;
	    	CategorieListViewItem v=lCategoriesListViewItems.get(position);
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
			//LoadFilterProperties(EditFilterSettings.tmpFilterProps);
		}
	
	}

	@Override
	public void OnHide() 
	{
		SetCategory();
		
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

	private void fillCategorieList()
	{
		Resources res = mContext.getResources();
		
		for (Category cat : GlobalCore.Categories)
		{
			CategorieListViewItem CollapseItem = addCategorieCollapseItem(Global.Icons[20], cat, COLLABSE_BUTTON_ITEM);
			for (GpxFilename File : cat )
			{
				CollapseItem.addChild(addCategorieItem( Global.Icons[20], File, CHECK_ITEM ));
			}
		}
		
		// lCategories is filled now we set the checked attr
		if(lCategories!=null)
		{
			for(CategorieEntry tmp : lCategories)
			{
				GpxFilename file = tmp.getFile();
				if (file!=null)
				{
					tmp.setState(file.Checked? 1:0);
				}
				
			}
		}
		
		

	}
	
	private CategorieListViewItem addCategorieItem(Drawable[] Icons,
			GpxFilename file, int ItemType, double i, double j, double k, double f) 
	{

		if(lCategories==null)
		{
			lCategories = new ArrayList<CategorieListView.CategorieEntry>();
			lCategorieListViewItems = new ArrayList<CategorieListViewItem>();
		}
		CategorieEntry tmp = new CategorieEntry(file,Icons,ItemType,i,j,k,f);
		lCategories.add(tmp);
		Boolean BackGroundChanger = ((lCategories.size() % 2) == 1);
		CategorieListViewItem v = new CategorieListViewItem(mContext, tmp, BackGroundChanger);
		// inital mit GONE
		v.setVisibility(View.GONE);
		lCategorieListViewItems.add(v);
		return v;
		
	}

	private CategorieListViewItem addCategorieItem(Drawable Icon, GpxFilename file, int ItemType)
	{
		if(lCategories==null)
		{
			lCategories = new ArrayList<CategorieListView.CategorieEntry>();
			lCategorieListViewItems = new ArrayList<CategorieListViewItem>();
		}
		CategorieEntry tmp = new CategorieEntry(file,Icon,ItemType);
		lCategories.add(tmp);
		Boolean BackGroundChanger = ((lCategories.size() % 2) == 1);
		CategorieListViewItem v = new CategorieListViewItem(mContext, tmp, BackGroundChanger);
		// inital mit GONE
		v.setVisibility(View.GONE);
		lCategorieListViewItems.add(v);
		return v;
	}
	
	private CategorieListViewItem addCategorieCollapseItem(Drawable Icon, Category cat, int ItemType)
	{
		if(lCategories==null)
		{
			lCategories = new ArrayList<CategorieListView.CategorieEntry>();
			lCategorieListViewItems = new ArrayList<CategorieListViewItem>();
		}
		CategorieEntry tmp = new CategorieEntry(cat,Icon,ItemType);
		lCategories.add(tmp);
		Boolean BackGroundChanger = ((lCategories.size() % 2) == 1);
		CategorieListViewItem v = new CategorieListViewItem(mContext, tmp, BackGroundChanger);
		lCategorieListViewItems.add(v);
		return v;
	}
	
	private void collabseButton_Clicked(CategorieListViewItem item)
	{
		item.toggleChildeViewState();
		this.invalidate();
	}
	
}


