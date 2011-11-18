package CB_Core.Types;

import java.util.ArrayList;

import CB_Core.FilterProperties;

public class Categories extends ArrayList<Category>
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 5348105269187711224L;

	public Categories()
	{
	}

	private void checkAll()
	{
		for (Category cat : this)
		{
			cat.Checked = false;
			for (GpxFilename gpx : cat)
			{
				gpx.Checked = true;
			}
		}
	}

	public void ReadFromFilter(FilterProperties filter)
	{
		checkAll();
		boolean foundOne = false;
		for (long id : filter.Categories)
		{
			for (Category cat : this)
			{
				if (cat.Id == id)
				{
					cat.Checked = true;
					foundOne = true;
				}
			}
		}
		if (!foundOne)
		{
			// Wenn gar keine Category aktiv -> alle aktivieren!
			for (Category cat : this)
			{
				cat.Checked = true;
			}
		}
		for (long id : filter.GPXFilenameIds)
		{
			for (Category cat : this)
			{
				for (GpxFilename gpx : cat)
				{
					if (gpx.Id == id)
					{
						gpx.Checked = false;
					}
				}
			}
		}
		for (Category cat : this)
		{
			// wenn Category nicht checked ist -> alle GpxFilenames deaktivieren
			if (cat.Checked) continue;
			for (GpxFilename gpx : cat)
			{
				gpx.Checked = false;
			}
		}
	}

	public void WriteToFilter(FilterProperties filter)
	{
		filter.GPXFilenameIds.clear();
		filter.Categories.clear();
		for (Category cat : this)
		{
			if (cat.Checked)
			{
				// GpxFilename Filter nur setzen, wenn die Category aktiv ist!
				filter.Categories.add(cat.Id);
				for (GpxFilename gpx : cat)
				{
					if (!gpx.Checked) filter.GPXFilenameIds.add(gpx.Id);
				}
			}
			else
			{
				// Category ist nicht aktiv -> alle GpxFilenames in Filter
				// aktivieren
				for (GpxFilename gpx : cat)
					filter.GPXFilenameIds.add(gpx.Id);
			}
		}
	}

}
