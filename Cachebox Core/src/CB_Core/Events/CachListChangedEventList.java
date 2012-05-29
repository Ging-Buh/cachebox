package CB_Core.Events;

import java.util.ArrayList;

public class CachListChangedEventList
{
	public static ArrayList<CacheListChangedEventListner> list = new ArrayList<CacheListChangedEventListner>();

	public static void Add(CacheListChangedEventListner event)
	{
		if (!list.contains(event)) list.add(event);
	}

	public static void Remove(CacheListChangedEventListner event)
	{
		list.remove(event);
	}

	public static void Call()
	{
		for (CacheListChangedEventListner event : list)
		{
			event.CacheListChangedEvent();
		}
	}

}
