package de.droidcachebox.Events;

import java.util.ArrayList;

import de.droidcachebox.Geocaching.Cache;
import de.droidcachebox.Geocaching.Waypoint;

public class SelectedLangChangedEventList 
{
	public static ArrayList<SelectedLangChangedEvent> list = new ArrayList<SelectedLangChangedEvent>();
	
	public static void Add(SelectedLangChangedEvent event)
	{
		list.add(event);	
	}
	
	public static void Call()
	{
		for (SelectedLangChangedEvent event : list)
		{
			event.SelectedLangChangedEvent();
		}
	
	}

}
