package CB_Core.Events;

import java.util.ArrayList;

import CB_Core.Types.Cache;
import CB_Core.Types.Waypoint;


public class SelectedCacheEventList {
	public static ArrayList<SelectedCacheEvent> list = new ArrayList<SelectedCacheEvent>();
	public static void Add(SelectedCacheEvent event)
	{
		list.add(event);	
	}
	public static void Remove (SelectedCacheEvent event)
	{
		list.remove(event);
	}
	public static void Call(Cache cache, Waypoint waypoint)
	{
		for (SelectedCacheEvent event : list)
		{
			event.SelectedCacheChanged(cache, waypoint);
		}
	
	}

}
