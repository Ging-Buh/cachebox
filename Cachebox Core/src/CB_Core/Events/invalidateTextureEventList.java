package CB_Core.Events;

import java.util.ArrayList;

public class invalidateTextureEventList
{
	public static ArrayList<invalidateTextureEvent> list = new ArrayList<invalidateTextureEvent>();

	public static void Add(invalidateTextureEvent event)
	{
		synchronized (list)
		{
			if (!list.contains(event)) list.add(event);
		}
	}

	public static void Remove(invalidateTextureEvent event)
	{
		synchronized (list)
		{
			list.remove(event);
		}
	}

	public static void Call()
	{
		Thread thread = new Thread(new Runnable()
		{

			@Override
			public void run()
			{
				synchronized (list)
				{
					for (invalidateTextureEvent event : list)
					{
						event.invalidateTexture();
					}
				}
			}
		});

		thread.run();

	}
}
