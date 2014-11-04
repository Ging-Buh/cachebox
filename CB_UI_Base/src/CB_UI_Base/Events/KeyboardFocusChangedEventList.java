package CB_UI_Base.Events;

import CB_UI_Base.Tag;
import CB_UI_Base.GL_UI.Controls.EditTextFieldBase;
import CB_Utils.Lists.CB_List;

import com.badlogic.gdx.Gdx;

public class KeyboardFocusChangedEventList
{
	public static CB_List<KeyboardFocusChangedEvent> list = new CB_List<KeyboardFocusChangedEvent>();

	public static void Add(KeyboardFocusChangedEvent event)
	{
		synchronized (list)
		{
			Gdx.app.log(Tag.TAG, "FocusChangedEventList register" + event.toString());
			if (!list.contains(event)) list.add(event);
		}
	}

	public static void Remove(KeyboardFocusChangedEvent event)
	{
		synchronized (list)
		{
			Gdx.app.debug(Tag.TAG, "FocusChangedEventList unregister" + event.toString());
			list.remove(event);
		}
	}

	public static void Call(final EditTextFieldBase focus)
	{
		if (focus != null && !focus.dontShowKeyBoard())
		{
			Gdx.input.setOnscreenKeyboardVisible(true);
		}
		else
		{
			Gdx.input.setOnscreenKeyboardVisible(false);
		}
		synchronized (list)
		{

			for (int i = 0, n = list.size(); i < n; i++)
			{
				KeyboardFocusChangedEvent event = list.get(i);
				// Gdx.app.debug(Tag.TAG,"FocusChangedEventList fire to " + event.toString());
				event.KeyboardFocusChanged(focus);
			}
		}
	}

}
