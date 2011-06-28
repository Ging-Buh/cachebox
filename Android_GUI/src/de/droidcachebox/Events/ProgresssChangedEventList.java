package de.droidcachebox.Events;

import java.util.ArrayList;

import de.droidcachebox.Geocaching.Cache;
import de.droidcachebox.Geocaching.Waypoint;

public class ProgresssChangedEventList 
{
	public static ArrayList<ProgressChangedEvent> list = new ArrayList<ProgressChangedEvent>();
	
	public static void Add(ProgressChangedEvent event)
	{
		list.add(event);	
	}
	
	public static void Call(String Msg,String ProgressMessage, int Progress)
	{
		for (ProgressChangedEvent event : list)
		{
			event.ProgressChangedEvent(Msg,ProgressMessage,Progress);
		}
	
	}
	
	public static void Call(String Msg, int Progress)
	{
		for (ProgressChangedEvent event : list)
		{
			event.ProgressChangedEvent("",Msg,Progress);
		}
	
	}

	public static void Del(ProgressChangedEvent event) 
	{
		list.remove(event);
	}

}
