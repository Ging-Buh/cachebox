package de.droidcachebox.Events;

import java.util.ArrayList;

import de.droidcachebox.Geocaching.Cache;
import de.droidcachebox.Geocaching.Waypoint;

public class ColorChangedEventList 
{
	public static ArrayList<ColorChangedEvent> list = new ArrayList<ColorChangedEvent>();
	
	public static void Add(ColorChangedEvent event)
	{
		list.add(event);	
	}
	
	public static void Call()
	{
		for (ColorChangedEvent event : list)
		{
			event.ColorChangedEvent();
		}
	
	}

}
