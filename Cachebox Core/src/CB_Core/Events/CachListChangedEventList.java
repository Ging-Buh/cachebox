package CB_Core.Events;

import java.util.ArrayList;

public class CachListChangedEventList 
{
	public static ArrayList<CacheListChangedEvent> list = new ArrayList<CacheListChangedEvent>();
	
	public static void Add(CacheListChangedEvent event)
	{
		list.add(event);	
	}
	
	public static void Remove(CacheListChangedEvent event)
	{
		list.remove(event);	
	}
	
	public static void Call()
	{
    	for (CacheListChangedEvent event : list)
		{
        	event.CacheListChangedEvent();
		}
	}

}
