package CB_Core.Events;

import java.util.ArrayList;

import CB_Core.GlobalCore;
import CB_Core.Types.Locator;

public class PositionChangedEventList
{
	public static ArrayList<PositionChangedEvent> list = new ArrayList<PositionChangedEvent>();

	public static void Add(PositionChangedEvent event)
	{
		list.add(event);
	}

	public static void Remove(PositionChangedEvent event)
	{
		list.remove(event);
	}

	public static void PositionChanged(Locator locator)
	{
		GlobalCore.Locator = locator;
		for (PositionChangedEvent event : list)
		{
			event.PositionChanged(locator);
		}
	}

	public static void Orientation(float heading)
	{
		for (PositionChangedEvent event : list)
		{
			event.OrientationChanged(heading);
		}
	}
}
