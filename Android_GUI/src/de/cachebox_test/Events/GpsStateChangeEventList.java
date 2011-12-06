package de.cachebox_test.Events;

import java.util.ArrayList;

import CB_Core.Config;
import de.cachebox_test.Global;


import CB_Core.GlobalCore;
import CB_Core.Types.Coordinate;
import android.location.Location;
import android.location.LocationManager;
import android.os.SystemClock;

public class GpsStateChangeEventList {
	public static ArrayList<GpsStateChangeEvent> list = new ArrayList<GpsStateChangeEvent>();
	public static void Add(GpsStateChangeEvent event)
	{
		list.add(event);	
	}
	
	public static void Remove(GpsStateChangeEvent event)
	{
		list.remove(event);	
	}
	
	public static void Call()
	{
		
		for (GpsStateChangeEvent event : list)
		{
			event.GpsStateChanged();
		}
	}

	

}
