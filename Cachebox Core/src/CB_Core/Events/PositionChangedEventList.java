package CB_Core.Events;

import java.util.ArrayList;

import CB_Core.GlobalCore;
import CB_Core.GL_UI.GL_Listener.GL_Listener;
import CB_Core.Log.Logger;
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
				try
				{
					event.PositionChanged(locator);
				}
				catch (Exception e)
				{
					Logger.Error("Core.PositionEventList.Call(location)", event.getReceiverName(), e);
					e.printStackTrace();
				}
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
				try
				{
					event.OrientationChanged(heading);
				}
				catch (Exception e)
				{
					Logger.Error("Core.PositionEventList.Call(heading)", event.getReceiverName(), e);
					e.printStackTrace();
				}
			}
		}

		// alle events abgearbeitet, jetzt kann die GL_View einmal Rendern
		GL_Listener.glListener.renderOnce(null);
	}
}
