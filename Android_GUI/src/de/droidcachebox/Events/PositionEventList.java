package de.droidcachebox.Events;

import java.util.ArrayList;

import CB_Core.Config;
import CB_Core.Energy;
import CB_Core.GlobalCore;
import CB_Core.Locator.Locator;
import CB_Core.Log.Logger;
import CB_Core.Types.Coordinate;
import android.location.Location;
import android.location.LocationManager;
import android.os.SystemClock;

public class PositionEventList
{
	public static ArrayList<PositionEvent> list = new ArrayList<PositionEvent>();

	public static void Add(PositionEvent event)
	{
		list.add(event);
	}

	public static void Remove(PositionEvent event)
	{
		list.remove(event);
	}

	public static void Call(Location location)
	{
		if (!Config.settings.HardwareCompass.getValue())
		{
			// GPS richtung senden

			Call(location.getBearing(), true, "Force Call from GPS Compass is off");
		}

		GlobalCore.LastPosition = new Coordinate(location.getLatitude(), location.getLongitude());
		GlobalCore.LastPosition.Valid = true; // Valid ob GPS oder Phone,
												// hauptsache eine Coordinate
		if (location.getProvider().equalsIgnoreCase(LocationManager.GPS_PROVIDER))
		{
			GlobalCore.LastValidPosition = new Coordinate(location.getLatitude(), location.getLongitude());

			GlobalCore.LastValidPosition.setElevation(location.getAltitude());

			GlobalCore.LastValidPosition.Valid = true;
		}
		else
		{
			GlobalCore.LastValidPosition.Valid = false;
		}
		// GlobalCore.Marker.Valid = false;
		for (PositionEvent event : list)
		{
			try
			{
				event.PositionChanged(location);
			}
			catch (Exception e)
			{
				Logger.Error("PositionEventList.Call(location)", event.getReceiverName(), e);
				e.printStackTrace();
			}
		}

		// Call Core Event
		CB_Core.Locator.Locator locator = new Locator();

		locator.setLocation(location.getLatitude(), location.getLongitude(), location.getAccuracy(), location.hasSpeed(),
				location.getSpeed(), location.hasBearing(), location.getBearing(), location.getAltitude(), location.getProvider());
		CB_Core.Events.PositionChangedEventList.PositionChanged(locator);

	}

	private static int anzCompassValues = 0;
	private static float compassValue = 0;
	private static long lastCompassTick = -99999;

	public static void Call(float heading, String Sender)
	{
		Call(heading, false, Sender);
	}

	public static void Call(float heading, boolean force, String Sender)
	{
		/**
		 * if display is switched off, so we need no heading changes
		 */
		if (Energy.DisplayOff()) return;

		if (!Config.settings.HardwareCompass.getValue() && !force) return;

		anzCompassValues++;
		compassValue += heading;

		long aktTick = SystemClock.uptimeMillis();
		if (aktTick < lastCompassTick + 300)
		{
			// do not update view now, only every 200 millisec
			return;
		}
		if (anzCompassValues == 0)
		{
			lastCompassTick = aktTick;
			return;
		}
		// Durchschnitts Richtung berechnen
		heading = compassValue / anzCompassValues;
		anzCompassValues = 0;
		compassValue = 0;
		lastCompassTick = aktTick;

		int callCounter = 0;

		for (PositionEvent event : list)
		{
			try
			{
				event.OrientationChanged(heading);
				callCounter++;
			}
			catch (Exception e)
			{
				Logger.Error("PositionEventList.Call(heading)", event.getReceiverName(), e);
				e.printStackTrace();
			}
		}

		// Call Core Event
		CB_Core.Events.PositionChangedEventList.Orientation(heading);

	}

}
