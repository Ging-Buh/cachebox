package CB_Locator.Events;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import CB_Locator.Locator;
import CB_Locator.Events.PositionChangedEvent.Priority;

public class PositionChangedEventList
{
	private static ArrayList<PositionChangedEvent> list = new ArrayList<PositionChangedEvent>();

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
						int o2 = arg0.getPriority().ordinal();
						int o1 = arg1.getPriority().ordinal();
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

	private static long lastOrintationChangedEvent = 0;
	private static Thread OrientationChangedThread;
	private static Thread PositionChangedThread;
	private static Thread SpeedChangedThread;

	public static void PositionChanged()
	{
		minPosEventTime = Math.min(minPosEventTime, System.currentTimeMillis() - lastPosTime);
		lastPosTime = System.currentTimeMillis();

		if (lastPositionChanged != 0 && lastPositionChanged > System.currentTimeMillis() - Locator.getMinUpdateTime()) return;
		lastPositionChanged = System.currentTimeMillis();

		if (PositionChangedThread != null)
		{
			if (PositionChangedThread.getState() != Thread.State.TERMINATED) return;
			else
				PositionChangedThread = null;
		}

		if (PositionChangedThread == null) PositionChangedThread = new Thread(new Runnable()
		{

			@Override
			public void run()
			{
				synchronized (list)
				{
					try
					{
						for (PositionChangedEvent event : list)
						{
							// If display is switched off fire only events with high priority!
							if (Locator.isDisplayOff() && (event.getPriority() != Priority.High)) continue;
							try
							{
								event.PositionChanged();
							}
							catch (Exception e)
							{
								// TODO reactivate if possible Logger.Error("Core.PositionEventList.Call(location)",
								// event.getReceiverName(),
								// e);
								e.printStackTrace();
							}
						}
					}
					catch (Exception e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});

		PositionChangedThread.start();

	}

	public static void OrientationChanged()
	{

		if (Locator.isDisplayOff()) return; // Hier braucht niemand ein OriantationChangedEvent

		minOrientationEventTime = Math.min(minOrientationEventTime, System.currentTimeMillis() - lastOrientTime);
		lastOrientTime = System.currentTimeMillis();

		if (lastOrintationChangedEvent != 0 && lastOrintationChangedEvent > System.currentTimeMillis() - Locator.getMinUpdateTime()) return;
		lastOrintationChangedEvent = System.currentTimeMillis();

		if (OrientationChangedThread != null)
		{
			if (OrientationChangedThread.getState() != Thread.State.TERMINATED) return;
			else
				OrientationChangedThread = null;
		}

		if (OrientationChangedThread == null) OrientationChangedThread = new Thread(new Runnable()
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
		OrientationChangedThread.start();
	}

	public static void SpeedChanged()
	{

		if (Locator.isDisplayOff()) return; // Hier braucht niemand ein SpeedChangedEvent

		if (SpeedChangedThread == null) SpeedChangedThread = new Thread(new Runnable()
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
							event.SpeedChanged();
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
		SpeedChangedThread.run();
	}
}
