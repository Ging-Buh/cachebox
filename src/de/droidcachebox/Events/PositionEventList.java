package de.droidcachebox.Events;

import java.util.ArrayList;

import de.droidcachebox.Config;
import de.droidcachebox.Global;
import de.droidcachebox.Geocaching.Coordinate;

import android.location.Location;

public class PositionEventList {
	public static ArrayList<PositionEvent> list = new ArrayList<PositionEvent>();
	public static void Add(PositionEvent event)
	{
		list.add(event);	
	}
	public static void Call(Location location)
	{
		Global.LastValidPosition = new Coordinate(location.getLatitude(), location.getLongitude());
		Global.Marker.Valid=false;
		for (PositionEvent event : list)
		{
			event.PositionChanged(location);
		}
	}
	public static void Call(float heading)
	{
		if (!Config.GetBool("HtcCompass"))
			return;		
		for (PositionEvent event : list)
		{
			event.OrientationChanged(heading);
		}
	}

}
