package CB_UI_Base.Events;

import CB_Utils.Lists.CB_List;
import CB_Utils.Log.Logger;

public class invalidateTextureEventList
{
	public static CB_List<invalidateTextureEvent> list = new CB_List<invalidateTextureEvent>();

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

		try
		{
			synchronized (list)
			{
				for (int i = 0, n = list.size(); i < n; i++)
				{
					invalidateTextureEvent event = list.get(i);
					if (event != null) event.invalidateTexture();
				}
			}
		}
		catch (Exception e)
		{
			Logger.Error("invalidateTextureEventList", "Call()", e);
		}
	}
}
