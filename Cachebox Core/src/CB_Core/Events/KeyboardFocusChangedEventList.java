package CB_Core.Events;

import java.util.ArrayList;

import CB_Core.GL_UI.Controls.EditTextFieldBase;
import CB_Core.Log.Logger;

public class KeyboardFocusChangedEventList
{
	public static ArrayList<KeyboardFocusChangedEvent> list = new ArrayList<KeyboardFocusChangedEvent>();

	public static void Add(KeyboardFocusChangedEvent event)
	{
		synchronized (list)
		{
			Logger.LogCat("FocusChangedEventList register" + event.toString());
			if (!list.contains(event)) list.add(event);
		}
	}

	public static void Remove(KeyboardFocusChangedEvent event)
	{
		synchronized (list)
		{
			Logger.LogCat("FocusChangedEventList unregister" + event.toString());
			list.remove(event);
		}
	}

	public static void Call(final EditTextFieldBase focus)
	{
		synchronized (list)
		{
			for (KeyboardFocusChangedEvent event : list)
			{
				Logger.LogCat("FocusChangedEventList fire to " + event.toString());
				event.KeyboardFocusChanged(focus);
			}
		}
	}

}
