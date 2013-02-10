package CB_Core.Locator.Events;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import CB_Core.Locator.Locator;
import CB_Core.Locator.Events.PositionChangedEvent.Priority;

public class PositionChangedEventList
{
	public static ArrayList<PositionChangedEvent> list = new ArrayList<PositionChangedEvent>();

	public static void Add(PositionChangedEvent event)
	{
		synchronized (list)
		{
			if (!list.contains(event))
			{
				list.add(event);

				Collections.sort(list, new Comparator<PositionChangedEvent>()
				{
					@Override
					public int compare(PositionChangedEvent arg0, PositionChangedEvent arg1)
					{
						int o1 = arg0.getPriority().ordinal();
						int o2 = arg1.getPriority().ordinal();
						return (o1 < o2 ? -1 : (o1 == o2 ? 0 : 1));
					}
				});

			}
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

	private static long lastPositionChanged = 0;

	public static void PositionChanged(final Locator locator)
	{
		minPosEventTime = Math.min(minPosEventTime, System.currentTimeMillis() - lastPosTime);
		lastPosTime = System.currentTimeMillis();

		if (lastPositionChanged != 0 && lastPositionChanged > System.currentTimeMillis() - Locator.getMinUpdateTime()) return;
		lastPositionChanged = System.currentTimeMillis();

		Thread thread = new Thread(new Runnable()
		{

			@Override
			public void run()
			{
				synchronized (list)
				{
					for (PositionChangedEvent event : list)
					{
						// If display is switched off fire only events with high priority!
						if (Locator.isDisplayOff()
								&& !(event.getPriority() == Priority.High || event.getReceiverName().equalsIgnoreCase("Core.MainViewBase"))) continue;
						try
						{
							event.PositionChanged();
						}
						catch (Exception e)
						{
							// TODO reactivate if possible Logger.Error("Core.PositionEventList.Call(location)", event.getReceiverName(),
							// e);
							e.printStackTrace();
						}
					}
				}
			}
		});

		thread.run();

	}

	private static long lastOrintationChangedEvent = 0;

	public static void Orientation(final float heading)
	{

		if (Locator.isDisplayOff()) return; // Hier braucht niemand ein OriantationChangedEvent

		minOrientationEventTime = Math.min(minOrientationEventTime, System.currentTimeMillis() - lastOrientTime);
		lastOrientTime = System.currentTimeMillis();

		if (lastOrintationChangedEvent != 0 && lastOrintationChangedEvent > System.currentTimeMillis() - Locator.getMinUpdateTime()) return;
		lastOrintationChangedEvent = System.currentTimeMillis();

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
							event.OrientationChanged();
						}
						catch (Exception e)
						{
							// TODO reactivate if possible Logger.Error("Core.PositionEventList.Call(heading)", event.getReceiverName(), e);
							e.printStackTrace();
						}
					}
				}
			}
		});

		thread.run();

	}
}
