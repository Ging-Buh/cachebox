package de.droidcachebox.Custom_Controls;

import java.util.ArrayList;
import java.util.HashMap;

import CB_Core.Enums.Attributes;
import CB_Core.Log.Logger;
import CB_Core.Types.Cache;
import CB_Core.Types.Waypoint;

import CB_Core.Config;
import de.droidcachebox.Database;
import de.droidcachebox.Global;
import CB_Core.Events.SelectedCacheEvent;
import CB_Core.Events.SelectedCacheEventList;
import de.droidcachebox.Events.ViewOptionsMenu;
import de.droidcachebox.Geocaching.DescriptionImageGrabber;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;

public class DescriptionViewControl extends WebView implements ViewOptionsMenu, SelectedCacheEvent {

	private boolean mustLoadDescription;
	private Cache aktCache;
	private Boolean isVisible=false;
	private HashMap<Attributes, Integer> attributeLookup;
	private ArrayList<String> NonLocalImages = new ArrayList<String>();
	private ArrayList<String> NonLocalImagesUrl = new ArrayList<String>();
	
	public DescriptionViewControl(Context context) {
		super(context);
		
	}

	public DescriptionViewControl(Context context, AttributeSet attrs) 
	{
		super(context, attrs);
		mustLoadDescription = false;
		SelectedCacheEventList.Add(this);
		
//		this.getSettings().setJavaScriptEnabled(true);
		this.getSettings().setLightTouchEnabled(false);
		this.getSettings().setLoadWithOverviewMode(true);
		this.getSettings().setSupportZoom(true);
		this.getSettings().setBuiltInZoomControls(true);
        
        attributeLookup = new HashMap<Attributes, Integer>();
        attributeLookup.put(Attributes.Default, 0);
        attributeLookup.put(Attributes.Dogs, 1);
        attributeLookup.put(Attributes.Fee, 2);
        attributeLookup.put(Attributes.ClimbingGear, 3);
        attributeLookup.put(Attributes.Boat, 4);
        attributeLookup.put(Attributes.Scuba, 5);
        attributeLookup.put(Attributes.Kids, 6);
        attributeLookup.put(Attributes.TakesLess, 7);
        attributeLookup.put(Attributes.ScenicView, 8);
        attributeLookup.put(Attributes.SignificantHike, 9);
        attributeLookup.put(Attributes.Climbing, 10);
        attributeLookup.put(Attributes.Wading, 11);
        attributeLookup.put(Attributes.Swimming, 12);
        attributeLookup.put(Attributes.Anytime, 13);
        attributeLookup.put(Attributes.Night, 14);
        attributeLookup.put(Attributes.Winter, 15);
        attributeLookup.put(Attributes.PoisonPlants, 17);
        attributeLookup.put(Attributes.Snakes, 18);
        attributeLookup.put(Attributes.Ticks, 19);
        attributeLookup.put(Attributes.AbandonedMines, 20);
        attributeLookup.put(Attributes.Cliff, 21);
        attributeLookup.put(Attributes.Hunting, 22);
        attributeLookup.put(Attributes.Dangerous, 23);
        attributeLookup.put(Attributes.WheelchairAccessible, 24);
        attributeLookup.put(Attributes.Parking, 25);
        attributeLookup.put(Attributes.PublicTransportation, 26);
        attributeLookup.put(Attributes.Drinking, 27);
        attributeLookup.put(Attributes.Restrooms, 28);
        attributeLookup.put(Attributes.Telephone, 29);
        attributeLookup.put(Attributes.Picnic, 30);
        attributeLookup.put(Attributes.Camping, 31);
        attributeLookup.put(Attributes.Bicycles, 32);
        attributeLookup.put(Attributes.Motorcycles, 33);
        attributeLookup.put(Attributes.Quads, 34);
        attributeLookup.put(Attributes.Offroad, 35);
        attributeLookup.put(Attributes.Snowmobiles, 36);
        attributeLookup.put(Attributes.Horses, 37);
        attributeLookup.put(Attributes.Campfires, 38);
        attributeLookup.put(Attributes.Thorns, 39);
        attributeLookup.put(Attributes.Stealth, 40);
        attributeLookup.put(Attributes.Stroller, 41);
        attributeLookup.put(Attributes.NeedsMaintenance, 42);
        attributeLookup.put(Attributes.Livestock, 43);
        attributeLookup.put(Attributes.Flashlight, 44);
        attributeLookup.put(Attributes.TruckDriver, 46);
        attributeLookup.put(Attributes.FieldPuzzle, 47);
        attributeLookup.put(Attributes.UVLight, 48);
        attributeLookup.put(Attributes.Snowshoes, 49);
        attributeLookup.put(Attributes.CrossCountrySkiis, 50);
        attributeLookup.put(Attributes.SpecialTool, 51);
        attributeLookup.put(Attributes.NightCache, 52);
        attributeLookup.put(Attributes.ParkAndGrab, 53);
        attributeLookup.put(Attributes.AbandonedStructure, 54);
        attributeLookup.put(Attributes.ShortHike, 55);
        attributeLookup.put(Attributes.MediumHike, 56);
        attributeLookup.put(Attributes.LongHike, 57);
        attributeLookup.put(Attributes.FuelNearby, 58);
        attributeLookup.put(Attributes.FoodNearby, 59);
		
	}

	public DescriptionViewControl(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
	}
	
	public DescriptionViewControl(Context context, String text) {
		super(context);
		
	}
	
	
	private int downloadTryCounter=0;
	
	public void setCache(Cache cache)
	{
        final String mimeType = "text/html";
        final String encoding = "utf-8";
        if (cache != null)
        {
        	NonLocalImages = new ArrayList<String>();
        	NonLocalImagesUrl = new ArrayList<String>();
        	String cachehtml =  Database.GetDescription(cache);
        	String html = DescriptionImageGrabber.ResolveImages(cache, cachehtml, !Config.GetBool("AllowInternetAccess"), NonLocalImages, NonLocalImagesUrl);
        	
            if (!Config.GetBool("DescriptionNoAttributes"))
                html = getAttributesHtml(Database.AttributesPositive(cache), Database.AttributesNegative(cache)) + html;
        	
        	
        	this.loadDataWithBaseURL("fake://fake.de", html, mimeType, encoding, null);
        }
        this.getSettings().setLightTouchEnabled(true);
        
        
		// Falls nicht geladene Bilder vorliegen und eine Internetverbindung
		// erlaubt ist, diese laden und Bilder erneut auflösen
		if (Config.GetBool("AllowInternetAccess")
				&& NonLocalImagesUrl.size() > 0) {
			downloadThread = new Thread() {
				public void run() 
				{

					if(downloadTryCounter>0)
					{
						try 
						{
							Thread.sleep(100);
						} catch (InterruptedException e) 
						{
							Logger.Error("DescriptionViewControl.setCache()", "Thread.sleep fehler", e);
							e.printStackTrace();
						}
					}
					
					while (NonLocalImagesUrl != null && NonLocalImagesUrl.size() > 0) {
						String local, url;
						local = NonLocalImages.get(0);
						url = NonLocalImagesUrl.get(0);
						NonLocalImagesUrl.remove(0);
						NonLocalImages.remove(0);
						try {
							DescriptionImageGrabber.Download(url, local);
						} catch (Exception e) {
							Logger.Error("DescriptionViewControl.setCache()","downloadThread run()", e);
						}
					}
					downloadReadyHandler.post(downloadComplete);
					
				}
			};
			downloadThread.start();
		}

	}
	
	 final Handler downloadReadyHandler = new Handler();
	 Thread downloadThread;
	    
	    final Runnable downloadComplete = new Runnable() 
	    {
		    public void run() 
		    {
		    	Global.setDebugMsg("Reload " + String.valueOf(downloadTryCounter++));
		    	if(downloadTryCounter<10) // nur 10 Download versuche zu lassen
		    		setCache(aktCache);
		    }
	    };
	

    private String getAttributesHtml(long attributesPositive, long attributesNegative)
    {
        StringBuilder sb = new StringBuilder();

        for (Attributes attribute : attributeLookup.keySet())
        {
        	long att = Attributes.GetAttributeIndex(attribute);
        	long and = att & attributesPositive;
            if ((att & attributesPositive) > 0)
                sb.append("<img style=\"border: 1px white solid;\" src=\"file://" + Config.WorkPath + "/data/Attributes/att_" + attributeLookup.get(attribute).toString() + "_1.gif\">");
        }
        for (Attributes attribute : attributeLookup.keySet())
            if (((long)attribute.ordinal() & attributesNegative) > 0)
                sb.append("<img style=\"border: 1px white solid;\" src=\"file://" + Config.WorkPath + "/data/Attributes/att_" + attributeLookup.get(attribute).toString() + "_0.gif\">");

        if (sb.length() > 0)
            sb.append("<br>");
        return sb.toString();
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
	public void OnShow() 
	{
		if(downloadTryCounter>9)mustLoadDescription=true; //Versuchs nochmal mit dem Download
		downloadTryCounter=0;
		isVisible=true;
		if (mustLoadDescription)
		{
			setCache(aktCache);
			mustLoadDescription = false;
		}
		
	}
	
	@Override
	public void OnHide() 
	{
		try 
		{
			this.clearCache(true);
		} catch (Exception e) 
		{
			Logger.Error("DescriptionViewControl.OnHide()", "clearCache", e);
				e.printStackTrace();
		}
		isVisible=false;
	}

	@Override
	public void OnFree() 
	{
		try 
		{
			this.clearCache(true);
		} catch (Exception e) {
				e.printStackTrace();
		}
		this.destroy();
	}

	@Override
	public void SelectedCacheChanged(Cache cache, Waypoint waypoint) {
		// TODO Auto-generated method stub
		if (cache != aktCache)
		{
			aktCache = cache;
			mustLoadDescription = true;
			if(isVisible)
				setCache(aktCache); //Wenn die View nicht sichtbar, brauch auch das HTML nicht geladen werden!
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
