package CB_Core.Events;

import java.util.ArrayList;

import CB_Core.GlobalCore;
import CB_Core.GL_UI.GL_Listener.GL_Listener;
import CB_Core.Locator.Locator;
import CB_Core.Log.Logger;

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

	public static void PositionChanged(final Locator locator)
	{
		Thread thread = new Thread(new Runnable()
		{

			@Override
			public void run()
			{

				if (!locator.hasHeading())
				{
					Logger.LogCat("Locator has noe Heading Last Heading= " + lastHeading);
					locator.setHeading(lastHeading);

				}

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
		});

		thread.run();

		if (!locator.UseCompass())
		{
			// Use GPS direction
			Orientation(locator.getHeading());
		}

	}

	private static float lastHeading = 0;

	public static void Orientation(final float heading)
	{

		lastHeading = heading;

		Thread thread = new Thread(new Runnable()
		{

			@Override
			public void run()
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
		});

		thread.run();

	}
}
