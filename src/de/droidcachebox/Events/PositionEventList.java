package de.droidcachebox.Events;

import java.util.ArrayList;

import android.location.Location;

public class PositionEventList {
	public static ArrayList<PositionEvent> list = new ArrayList<PositionEvent>();
	public static void Add(PositionEvent event)
	{
		list.add(event);	
	}
	public static void Call(Location location)
	{
		for (PositionEvent event : list)
		{
			event.PositionChanged(location);
		}
	}
	public static void Call(float heading)
	{
		for (PositionEvent event : list)
		{
			event.OrientationChanged(heading);
		}
	}

}
