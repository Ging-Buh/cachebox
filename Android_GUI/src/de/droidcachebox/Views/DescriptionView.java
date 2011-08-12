package de.droidcachebox.Views;

import java.io.File;

import de.droidcachebox.ExtAudioRecorder;
import de.droidcachebox.Global;
import de.droidcachebox.R;
import de.droidcachebox.TrackRecorder;
import de.droidcachebox.main;
import de.droidcachebox.Components.CacheDraw.DrawStyle;
import de.droidcachebox.Custom_Controls.CacheInfoControl;
import de.droidcachebox.Custom_Controls.DescriptionViewControl;
import de.droidcachebox.Custom_Controls.IconContextMenu.IconContextMenu;
import de.droidcachebox.Custom_Controls.IconContextMenu.IconContextMenu.IconContextItemSelectedListener;
import de.droidcachebox.DAO.CacheDAO;
import CB_Core.Config;
import CB_Core.FileIO;
import CB_Core.GlobalCore;
import CB_Core.Events.SelectedCacheEvent;
import CB_Core.Events.SelectedCacheEventList;
import de.droidcachebox.Events.ViewOptionsMenu;
import de.droidcachebox.Ui.ActivityUtils;
import de.droidcachebox.Ui.AllContextMenuCallHandler;
import de.droidcachebox.Ui.Sizes;
import de.droidcachebox.Views.Forms.MessageBox;
import CB_Core.Types.Cache;
import CB_Core.Types.Waypoint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Messenger;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class DescriptionView extends FrameLayout implements ViewOptionsMenu, SelectedCacheEvent {
	Context context;
	public Cache aktCache;
	
	Button TestButton;
	public CacheInfoControl cacheInfo;
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
		WebControl.setWillNotCacheDrawing(false);
	}
	
	 @Override
	    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) 
	    {
	    // we overriding onMeasure because this is where the application gets its right size.
	    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	    	    
	    cacheInfo.setHeight(Sizes.getCacheInfoHeight());
	   
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
		AllContextMenuCallHandler.showCacheDescViewContextMenu();
	}
	
	public void reloadCacheInfo()
	{
		Cache reCache = aktCache; //Todo hole Info über API
		CacheDAO dao = new CacheDAO();
		dao.UpdateDatabase(reCache);
		//Todo Update CachList
		MessageBox.Show("TODO:API-Abfrage" + String.format("%n")+ "DescriptionView.java -> Line:136");
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
