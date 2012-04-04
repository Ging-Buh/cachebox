package CB_Core.Events;

import java.util.ArrayList;

import CB_Core.GlobalCore;
import CB_Core.GL_UI.GL_Listener.GL_Listener;
import CB_Core.Types.Locator;

public class PositionChangedEventList
{
	public static ArrayList<PositionChangedEvent> list = new ArrayList<PositionChangedEvent>();

	public static void Add(PositionChangedEvent event)
	{
		synchronized (list)
		{
			list.add(event);
		}

	}

	public static void Remove(PositionChangedEvent event)
	{
		synchronized (list)
		{
			list.remove(event);
		}

	}

	public static void PositionChanged(Locator locator)
	{
		GlobalCore.Locator = locator;
		synchronized (list)
		{
			for (PositionChangedEvent event : list)
			{
				event.PositionChanged(locator);
			}
		}

		// alle events abgearbeitet, jetzt kann die GL_View einmal Rendern
		GL_Listener.glListener.renderOnce(null);
	}

	public static void Orientation(float heading)
	{
		synchronized (list)
		{
			for (PositionChangedEvent event : list)
			{
				event.OrientationChanged(heading);
			}
		}

		// alle events abgearbeitet, jetzt kann die GL_View einmal Rendern
		GL_Listener.glListener.renderOnce(null);
	}
}
