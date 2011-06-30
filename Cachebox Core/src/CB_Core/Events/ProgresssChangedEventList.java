package CB_Core.Events;

import java.util.ArrayList;


public class ProgresssChangedEventList 
{
	public static ArrayList<ProgressChangedEvent> list = new ArrayList<ProgressChangedEvent>();
	
	public static void Add(ProgressChangedEvent event)
	{
		list.add(event);	
	}
	
	public static void Call(String Msg,String ProgressMessage, int Progress)
	{
		for (ProgressChangedEvent event : list)
		{
			event.ProgressChangedEventCalled(Msg,ProgressMessage,Progress);
		}
	
	}
	
	public static void Call(String Msg, int Progress)
	{
		for (ProgressChangedEvent event : list)
		{
			event.ProgressChangedEventCalled("",Msg,Progress);
		}
	
	}

	public static void Del(ProgressChangedEvent event) 
	{
		list.remove(event);
	}

}
