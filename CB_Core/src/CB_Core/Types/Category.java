package CB_Core.Types;

import java.util.ArrayList;
import java.util.Date;

public class Category extends ArrayList<GpxFilename> implements Comparable<Category>
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -7257078663021910097L;
	public long Id;
	public String GpxFilename;
	public boolean pinned;
	public boolean Checked;

	public Category()
	{

	}

	public int CacheCount()
	{
		int result = 0;
		for (GpxFilename gpx : this)
			result += gpx.CacheCount;
		return result;
	}

	public Date LastImported()
	{
		if (size() == 0) return new Date();
		return this.get(this.size() - 1).Imported;
	}

	public String GpxFilenameWoNumber()
	{
		// Nummer der PQ weglassen, wenn dahinter noch eine Bezeichnung kommt.
		String name = GpxFilename;
		int pos = name.indexOf('_');
		if (pos < 0) return name;
		String part = name.substring(0, pos);
		if (part.length() < 7) return name;
		try
		{
			// Vorderen Teil nur dann weglassen, wenn dies eine Zahl ist.
			Integer.valueOf(part);
		}
		catch (Exception exc)
		{
			return name;
		}

		name = name.substring(pos + 1, name.length());

		if (name.toLowerCase().indexOf(".gpx") == name.length() - 4) name = name.substring(0, name.length() - 4);
		return name;
	}

	@Override
	public int compareTo(Category o)
	{
		if (o.Id > this.Id) return 1;
		else if (o.Id < this.Id) return -1;
		else
			return 0;
	}

	/**
	 * gibt den chk status der enthaltenen GpxFiles zurück </br> 0 = keins
	 * ausgewählt </br> 1 = alle ausgewählt </br> -1 = nicht alle, aber
	 * mindestens eins ausgewählt
	 * 
	 * @return
	 */
	public int getChek()
	{
		int result = 0;

		int chkCounter = 0;
		int counter = 0;
		for (GpxFilename gpx : this)
		{
			if (gpx.Checked) chkCounter++;

			counter++;
		}

		if (chkCounter == 0) result = 0;
		else if (chkCounter == counter) result = 1;
		else
			result = -1;

		return result;
	}

}
