package CB_Core.Events;

import java.util.ArrayList;

import CB_Core.Config;
import CB_Core.Energy;
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
			if (!list.contains(event)) list.add(event);
		}

	}

	public static void Remove(PositionChangedEvent event)
	{
		synchronized (list)
		{
			list.remove(event);
		}

	}

	public static long minPosEventTime = Long.MAX_VALUE;
	public static long minOrientationEventTime = Long.MAX_VALUE;

	public static long lastPosTime = 0;
	public static long lastOrientTime = 0;

	public static long maxEventListTime = 0;
	private static long lastPositionChanged = 0;

	public static void PositionChanged(final Locator locator)
	{
		minPosEventTime = Math.min(minPosEventTime, System.currentTimeMillis() - lastPosTime);
		lastPosTime = System.currentTimeMillis();

		if (lastPositionChanged != 0 && lastPositionChanged > System.currentTimeMillis() - Config.settings.gpsUpdateTime.getValue()) return;
		lastPositionChanged = System.currentTimeMillis();

		Thread thread = new Thread(new Runnable()
		{

			@Override
			public void run()
			{
				long thradStart = System.currentTimeMillis();
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
						// Bei ausgesachltenem Display nur an GlobalLocationReceiver senden!!!
						if (Energy.DisplayOff()
								&& !(event.getReceiverName().equalsIgnoreCase("GlobalLocationReceiver") || event.getReceiverName()
										.equalsIgnoreCase("Core.MainViewBase"))) continue;
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

				maxEventListTime = Math.max(maxEventListTime, System.currentTimeMillis() - thradStart);

			}
		});

		thread.run();

	}

	private static float lastHeading = 0;

	private static long lastOrintationChangedEvent = 0;

	public static void Orientation(final float heading)
	{

		if (Energy.DisplayOff()) return; // Hier braucht niemand ein OriantationChangedEvent

		minOrientationEventTime = Math.min(minOrientationEventTime, System.currentTimeMillis() - lastOrientTime);
		lastOrientTime = System.currentTimeMillis();

		if (lastOrintationChangedEvent != 0
				&& lastOrintationChangedEvent > System.currentTimeMillis() - Config.settings.gpsUpdateTime.getValue()) return;
		lastOrintationChangedEvent = System.currentTimeMillis();

		lastHeading = heading;

		Thread thread = new Thread(new Runnable()
		{

			@Override
			public void run()
			{
				long thradStart = System.currentTimeMillis();
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

				maxEventListTime = Math.max(maxEventListTime, System.currentTimeMillis() - thradStart);

			}
		});

		thread.run();

	}
}
