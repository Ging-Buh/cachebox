package de.droidcachebox.Events;

import java.util.ArrayList;

import de.droidcachebox.Geocaching.Cache;
import de.droidcachebox.Geocaching.Waypoint;

public class SelectedCacheEventList {
	public static ArrayList<SelectedCacheEvent> list = new ArrayList<SelectedCacheEvent>();
	public static void Add(SelectedCacheEvent event)
	{
		list.add(event);	
	}
	public static void Call(Cache cache, Waypoint waypoint)
	{
		for (SelectedCacheEvent event : list)
		{
			event.SelectedCacheChanged(cache, waypoint);
		}
	
	}

}
