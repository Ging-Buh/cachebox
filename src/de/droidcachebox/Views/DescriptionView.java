package de.droidcachebox.Views;

import java.util.ArrayList;

import de.droidcachebox.Global;

import de.droidcachebox.Events.SelectedCacheEvent;
import de.droidcachebox.Events.SelectedCacheEventList;
import de.droidcachebox.Events.ViewOptionsMenu;
import de.droidcachebox.Geocaching.Cache;
import de.droidcachebox.Geocaching.DescriptionImageGrabber;
import de.droidcachebox.Geocaching.Waypoint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;

public class DescriptionView extends WebView implements ViewOptionsMenu, SelectedCacheEvent {

	private boolean mustLoadDescription;
	private Cache aktCache;
	/**
	 * Constructor
	 */
	public DescriptionView(Context context, String text) {
		super(context);
		mustLoadDescription = false;
		SelectedCacheEventList.Add(this);
		
//		this.getSettings().setJavaScriptEnabled(true);
		this.getSettings().setLightTouchEnabled(false);
		this.getSettings().setLoadWithOverviewMode(true);
		this.getSettings().setSupportZoom(true);
		this.getSettings().setBuiltInZoomControls(true);
        
	}
	
	public void setCache(Cache cache)
	{
        final String mimeType = "text/html";
        final String encoding = "utf-8";
        if (cache != null)
        {
        	ArrayList<String> NonLocalImages = new ArrayList<String>();
        	ArrayList<String> NonLocalImagesUrl = new ArrayList<String>();
        	String cachehtml =  cache.GetDescription();
        	String html = DescriptionImageGrabber.ResolveImages(cache, cachehtml, true, NonLocalImages, NonLocalImagesUrl);
        	this.loadDataWithBaseURL("fake://fake.de", html, mimeType, encoding, null);
        }
        this.getSettings().setLightTouchEnabled(true);
	}

	@Override
	public boolean ItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void BeforeShowMenu(Menu menu) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnShow() {
		if (mustLoadDescription)
		{
			setCache(aktCache);
			mustLoadDescription = false;
		}
		
	}

	@Override
	public void OnHide() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void SelectedCacheChanged(Cache cache, Waypoint waypoint) {
		// TODO Auto-generated method stub
		if (cache != aktCache)
		{
			aktCache = cache;
			mustLoadDescription = true;
		}
	}

	@Override
	public int GetMenuId() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void ActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		
	}
}
