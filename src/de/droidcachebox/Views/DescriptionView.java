package de.droidcachebox.Views;

import java.util.ArrayList;
import java.util.HashMap;

import de.droidcachebox.Config;
import de.droidcachebox.Global;
import de.droidcachebox.main;

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
import android.os.Handler;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;

public class DescriptionView extends WebView implements ViewOptionsMenu, SelectedCacheEvent {

	private boolean mustLoadDescription;
	private Cache aktCache;
	private HashMap<Cache.Attributes, Integer> attributeLookup;
	private ArrayList<String> NonLocalImages = new ArrayList<String>();
	private ArrayList<String> NonLocalImagesUrl = new ArrayList<String>();
	
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
        
        attributeLookup = new HashMap<Cache.Attributes, Integer>();
        attributeLookup.put(Cache.Attributes.Default, 0);
        attributeLookup.put(Cache.Attributes.Dogs, 1);
        attributeLookup.put(Cache.Attributes.Fee, 2);
        attributeLookup.put(Cache.Attributes.ClimbingGear, 3);
        attributeLookup.put(Cache.Attributes.Boat, 4);
        attributeLookup.put(Cache.Attributes.Scuba, 5);
        attributeLookup.put(Cache.Attributes.Kids, 6);
        attributeLookup.put(Cache.Attributes.TakesLess, 7);
        attributeLookup.put(Cache.Attributes.ScenicView, 8);
        attributeLookup.put(Cache.Attributes.SignificantHike, 9);
        attributeLookup.put(Cache.Attributes.Climbing, 10);
        attributeLookup.put(Cache.Attributes.Wading, 11);
        attributeLookup.put(Cache.Attributes.Swimming, 12);
        attributeLookup.put(Cache.Attributes.Anytime, 13);
        attributeLookup.put(Cache.Attributes.Night, 14);
        attributeLookup.put(Cache.Attributes.Winter, 15);
        attributeLookup.put(Cache.Attributes.PoisonPlants, 17);
        attributeLookup.put(Cache.Attributes.Snakes, 18);
        attributeLookup.put(Cache.Attributes.Ticks, 19);
        attributeLookup.put(Cache.Attributes.AbandonedMines, 20);
        attributeLookup.put(Cache.Attributes.Cliff, 21);
        attributeLookup.put(Cache.Attributes.Hunting, 22);
        attributeLookup.put(Cache.Attributes.Dangerous, 23);
        attributeLookup.put(Cache.Attributes.WheelchairAccessible, 24);
        attributeLookup.put(Cache.Attributes.Parking, 25);
        attributeLookup.put(Cache.Attributes.PublicTransportation, 26);
        attributeLookup.put(Cache.Attributes.Drinking, 27);
        attributeLookup.put(Cache.Attributes.Restrooms, 28);
        attributeLookup.put(Cache.Attributes.Telephone, 29);
        attributeLookup.put(Cache.Attributes.Picnic, 30);
        attributeLookup.put(Cache.Attributes.Camping, 31);
        attributeLookup.put(Cache.Attributes.Bicycles, 32);
        attributeLookup.put(Cache.Attributes.Motorcycles, 33);
        attributeLookup.put(Cache.Attributes.Quads, 34);
        attributeLookup.put(Cache.Attributes.Offroad, 35);
        attributeLookup.put(Cache.Attributes.Snowmobiles, 36);
        attributeLookup.put(Cache.Attributes.Horses, 37);
        attributeLookup.put(Cache.Attributes.Campfires, 38);
        attributeLookup.put(Cache.Attributes.Thorns, 39);
        attributeLookup.put(Cache.Attributes.Stealth, 40);
        attributeLookup.put(Cache.Attributes.Stroller, 41);
        attributeLookup.put(Cache.Attributes.NeedsMaintenance, 42);
        attributeLookup.put(Cache.Attributes.Livestock, 43);
        attributeLookup.put(Cache.Attributes.Flashlight, 44);
        attributeLookup.put(Cache.Attributes.TruckDriver, 46);
        attributeLookup.put(Cache.Attributes.FieldPuzzle, 47);
        attributeLookup.put(Cache.Attributes.UVLight, 48);
        attributeLookup.put(Cache.Attributes.Snowshoes, 49);
        attributeLookup.put(Cache.Attributes.CrossCountrySkiis, 50);
        attributeLookup.put(Cache.Attributes.SpecialTool, 51);
        attributeLookup.put(Cache.Attributes.NightCache, 52);
        attributeLookup.put(Cache.Attributes.ParkAndGrab, 53);
        attributeLookup.put(Cache.Attributes.AbandonedStructure, 54);
        attributeLookup.put(Cache.Attributes.ShortHike, 55);
        attributeLookup.put(Cache.Attributes.MediumHike, 56);
        attributeLookup.put(Cache.Attributes.LongHike, 57);
        attributeLookup.put(Cache.Attributes.FuelNearby, 58);
        attributeLookup.put(Cache.Attributes.FoodNearby, 59);
	}
	
	public void setCache(Cache cache)
	{
        final String mimeType = "text/html";
        final String encoding = "utf-8";
        if (cache != null)
        {
        	NonLocalImages = new ArrayList<String>();
        	NonLocalImagesUrl = new ArrayList<String>();
        	String cachehtml =  cache.GetDescription();
        	String html = DescriptionImageGrabber.ResolveImages(cache, cachehtml, !Config.GetBool("AllowInternetAccess"), NonLocalImages, NonLocalImagesUrl);
        	
            if (!Config.GetBool("DescriptionNoAttributes"))
                html = getAttributesHtml(cache.AttributesPositive(), cache.AttributesNegative()) + html;
        	
        	
        	this.loadDataWithBaseURL("fake://fake.de", html, mimeType, encoding, null);
        }
        this.getSettings().setLightTouchEnabled(true);
        
        
     // Falls nicht geladene Bilder vorliegen und eine Internetverbindung
     // erlaubt ist, diese laden und Bilder erneut auflösen
        if (Config.GetBool("AllowInternetAccess") && NonLocalImagesUrl.size() > 0)
        {
        	downloadThread = new Thread() {
                public void run() {
                	        			
        	        while (NonLocalImagesUrl != null && NonLocalImagesUrl.size()> 0)
        	        {
        	            String local, url;
        	            local = NonLocalImages.get(0);
        	            url = NonLocalImagesUrl.get(0);
        	            NonLocalImagesUrl.remove(0);
        	            NonLocalImages.remove(0);
        	            try 
        	            {
        					DescriptionImageGrabber.Download(url, local);
        				} catch (Exception e) 
        				{
        					String Msg = (e==null)? "" : e.getMessage();
        					Global.AddLog("ERROR :DescriptionImageGrabber.Download(url, local) \n" + Msg, true);
        				}
        	        }
                     downloadReadyHandler.post(downloadComplete);
                }
            };
        	loaderThread.start();
        }
        
	}
	
	 final Handler downloadReadyHandler = new Handler();
	 Thread downloadThread;
	    
	    final Runnable downloadComplete = new Runnable() 
	    {
		    public void run() 
		    {
		    	 setCache(aktCache);
		    }
	    };
	

    private String getAttributesHtml(long attributesPositive, long attributesNegative)
    {
        StringBuilder sb = new StringBuilder();

        for (Cache.Attributes attribute : attributeLookup.keySet())
        {
        	long att = Cache.GetAttributeIndex(attribute);
        	long and = att & attributesPositive;
            if ((att & attributesPositive) > 0)
                sb.append("<img style=\"border: 1px white solid;\" src=\"file://" + Config.WorkPath + "/data/Attributes/att_" + attributeLookup.get(attribute).toString() + "_1.gif\">");
        }
        for (Cache.Attributes attribute : attributeLookup.keySet())
            if (((long)attribute.ordinal() & attributesNegative) > 0)
                sb.append("<img style=\"border: 1px white solid;\" src=\"file://" + Config.WorkPath + "/data/Attributes/att_" + attributeLookup.get(attribute).toString() + "_0.gif\">");

        if (sb.length() > 0)
            sb.append("<br>");
        return sb.toString();
    }

   
    
    
    
    //downloadThread.start();

       
    
    
    
    // Threding methods to reload images if allowd
	Thread loaderThread = new Thread() 
    {
		@Override
	    public void run() 
	    {
	                
			               // Fertig!
			        try {
						
						{
							main.mainActivity.runOnUiThread(new Runnable() 
							{
					               @Override
					               public void run() 
					               {
					            	
					               }
					           });

						}
							
					} catch (Exception e) 
					{
						String Msg = (e==null)? "" : e.getMessage();
						Global.AddLog("ERROR : Beim neu setzen der Images nach dem Laden (DescriptionView)\n" + Msg, true);
					}
			    
	    }
    };
 
    
     
    
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
	public void OnFree() {
		this.destroy();
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

	@Override
	public int GetContextMenuId() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void BeforeShowContextMenu(Menu menu) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean ContextMenuItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		return false;
	}
}
