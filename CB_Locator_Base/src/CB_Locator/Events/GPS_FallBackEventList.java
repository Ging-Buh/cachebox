package CB_Locator.Events;

import java.util.ArrayList;

public class GPS_FallBackEventList
{
	public static ArrayList<GPS_FallBackEvent> list = new ArrayList<GPS_FallBackEvent>();

	public static void Add(GPS_FallBackEvent event)
	{
		list.add(event);
	}

	public static void Remove(GPS_FallBackEvent event)
	{
		list.remove(event);
	}

	public static void Call()
	{

		for (GPS_FallBackEvent event : list)
		{
			event.FallBackToNetworkProvider();
		}

	}
}
