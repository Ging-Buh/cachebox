package de.cachebox_test.Events;

import java.util.ArrayList;

import de.cachebox_test.Energy;
import de.cachebox_test.main;
import de.cachebox_test.Custom_Controls.downSlider;
import de.cachebox_test.Views.AboutView;

public class GpsStateChangeEventList
{
	public static ArrayList<GpsStateChangeEvent> list = new ArrayList<GpsStateChangeEvent>();

	public static void Add(GpsStateChangeEvent event)
	{
		list.add(event);
	}

	public static void Remove(GpsStateChangeEvent event)
	{
		list.remove(event);
	}

	private static int count = 0;

	public static void Call()
	{
		count++;
		for (GpsStateChangeEvent event : list)
		{

			// Wenn das Display aus ist, brauchen wir das Event nicht feuern!
			if (Energy.DisplayOff())
			{
				// ausser an main, aber reduziert!
				// nur jedes 10. mal, was 10 Sec entspricht
				if (event instanceof main && count > 10)
				{
					FireEvent(event);
					count = 0;
				}
			}
			else
			{
				// main braucht das Event nur alle 2 Sec
				if (event instanceof main && count > 1)
				{
					FireEvent(event);
					count = 0;
				}

				if (event instanceof downSlider && Energy.SliderIsShown())
				{
					FireEvent(event);
				}

				if (event instanceof AboutView && Energy.AboutIsShown())
				{
					FireEvent(event);
				}
			}

		}
		if (count > 10) count = 0;
	}

	private static void FireEvent(GpsStateChangeEvent event)
	{
		event.GpsStateChanged();
		// Log.d("CACHEBOX", "GPS State Change called " + event.toString());
	}

}
