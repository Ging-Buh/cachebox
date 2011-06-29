package CB_Core.TranslationEngine;

import java.util.ArrayList;



public class SelectedLangChangedEventList 
{
	public static ArrayList<SelectedLangChangedEvent> list = new ArrayList<SelectedLangChangedEvent>();
	
	public static void Add(SelectedLangChangedEvent event)
	{
		list.add(event);	
	}
	
	public static void Call()
	{
		for (SelectedLangChangedEvent event : list)
		{
			event.SelectedLangChangedEventCalled();
		}
	
	}

}
