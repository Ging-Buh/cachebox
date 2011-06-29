package de.droidcachebox.Views;

import de.droidcachebox.Global;
import de.droidcachebox.R;
import de.droidcachebox.Components.CacheDraw.DrawStyle;
import de.droidcachebox.Custom_Controls.CacheInfoControl;
import de.droidcachebox.Custom_Controls.DescriptionViewControl;
import de.droidcachebox.Events.SelectedCacheEvent;
import de.droidcachebox.Events.SelectedCacheEventList;
import de.droidcachebox.Events.ViewOptionsMenu;
import CB_Core.Types.Cache;
import CB_Core.Types.Waypoint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

public class DescriptionView extends FrameLayout implements ViewOptionsMenu, SelectedCacheEvent {
	Context context;
	Cache aktCache;
	
	Button TestButton;
	CacheInfoControl cacheInfo;
	DescriptionViewControl WebControl;
	
	public DescriptionView(Context context, LayoutInflater inflater) 
	{
		super(context);
		
		SelectedCacheEventList.Add(this);

		RelativeLayout descriptionLayout = (RelativeLayout)inflater.inflate(R.layout.description_view, null, false);
		this.addView(descriptionLayout);
		
		cacheInfo = (CacheInfoControl)findViewById(R.id.CompassDescriptionView);
		cacheInfo.setStyle(DrawStyle.withOwner);
		WebControl = (DescriptionViewControl)findViewById(R.id.DescriptionViewControl);
		
	}
	
	 @Override
	    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) 
	    {
	    // we overriding onMeasure because this is where the application gets its right size.
	    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	    	    
	    cacheInfo.setHeight((int) (Global.scaledFontSize_normal * 4.9));
	   
	    }
		

	@Override
	public void SelectedCacheChanged(Cache cache, Waypoint waypoint) 
	{
		if (aktCache != cache)
		{
			aktCache = cache;
			cacheInfo.setCache(aktCache);
		}
	}

	@Override
	public boolean ItemSelected(MenuItem item) 
	{
		return false;
	}

	@Override
	public void BeforeShowMenu(Menu menu) 
	{
	}

	@Override
	public void OnShow() 
	{
		WebControl.OnShow();
	}

	@Override
	public void OnHide() 
	{
		WebControl.OnHide();
	}

	@Override
	public void OnFree() 
	{
		WebControl.OnFree();
	}

	@Override
	public int GetMenuId() 
	{
		return 0;
	}

	@Override
	public void ActivityResult(int requestCode, int resultCode, Intent data) 
	{
	}

	@Override
	public int GetContextMenuId() 
	{
		return 0;
	}

	@Override
	public void BeforeShowContextMenu(Menu menu) 
	{
	}

	@Override
	public boolean ContextMenuItemSelected(MenuItem item) 
	{
		return false;
	}

}
