package de.droidcachebox.Views;

import de.droidcachebox.R;
import de.droidcachebox.Events.SelectedCacheEvent;
import de.droidcachebox.Events.SelectedCacheEventList;
import de.droidcachebox.Events.ViewOptionsMenu;
import de.droidcachebox.Geocaching.Cache;
import de.droidcachebox.Geocaching.Waypoint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class EmptyViewTemplate extends FrameLayout implements ViewOptionsMenu, SelectedCacheEvent {
	Context context;
	Cache aktCache;
	
	Button TestButton;

	public EmptyViewTemplate(Context context, LayoutInflater inflater) 
	{
		super(context);
		
		SelectedCacheEventList.Add(this);

		RelativeLayout notesLayout = (RelativeLayout)inflater.inflate(R.layout.emptyviewtemplate, null, false);
		this.addView(notesLayout);
		
		TestButton = (Button)this.findViewById(R.id.button1);
		
		TestButton.setOnClickListener(new OnClickListener() 
		{

			@Override
			public void onClick(View arg0) 
			{
				
				
			}
			
		});
		
	}

	@Override
	public void SelectedCacheChanged(Cache cache, Waypoint waypoint) 
	{
		if (aktCache != cache)
		{
			aktCache = cache;
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
	}

	@Override
	public void OnHide() 
	{
	}

	@Override
	public void OnFree() {
		
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
