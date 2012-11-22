package CB_Core.DAO;

import java.util.ArrayList;

import CB_Core.DB.CoreCursor;
import CB_Core.DB.Database;
import CB_Core.DB.Database.Parameters;
import CB_Core.Log.Logger;
import CB_Core.Types.ImageEntry;

public class ImageDAO
{
	public void WriteToDatabase(ImageEntry image, Boolean ignoreExisting)
	{
		Parameters args = new Parameters();
		args.put("CacheId", image.CacheId);
		args.put("GcCode", image.GcCode);
		args.put("Name", image.Name);
		args.put("Description", image.Description);
		args.put("ImageUrl", image.ImageUrl);
		args.put("IsCacheImage", image.IsCacheImage);
		try
		{
			if (ignoreExisting)
			{
				Database.Data.insertWithConflictIgnore("Images", args);
			}
			else
			{
				Database.Data.insertWithConflictReplace("Images", args);
			}
		}
		catch (Exception exc)
		{
			Logger.Error("Write Image", "", exc);
		}
	}

	public ArrayList<ImageEntry> getImagesForCache(String GcCode)
	{
		ArrayList<ImageEntry> images = new ArrayList<ImageEntry>();

		CoreCursor reader = Database.Data.rawQuery(
				"select CacheId, GcCode, Name, Description, ImageUrl, IsCacheImage from Images where GcCode=?", new String[]
					{ GcCode });
		if (reader.getCount() > 0)
		{
			reader.moveToFirst();
			while (reader.isAfterLast() == false)
			{
				ImageEntry image = new ImageEntry(reader);
				images.add(image);
				reader.moveToNext();
			}
		}
		reader.close();
		return images;
	}

	public ArrayList<ImageEntry> getDescriptionImagesForCache(String GcCode)
	{
		ArrayList<ImageEntry> images = new ArrayList<ImageEntry>();

		CoreCursor reader = Database.Data.rawQuery(
				"select CacheId, GcCode, Name, Description, ImageUrl, IsCacheImage from Images where GcCode=? and IsCacheImage=1",
				new String[]
					{ GcCode });

		if (reader == null) return images;

		reader.moveToFirst();
		while (reader.isAfterLast() == false)
		{
			ImageEntry image = new ImageEntry(reader);
			images.add(image);
			reader.moveToNext();
		}
		reader.close();

		return images;
	}

	public ArrayList<String> getImageURLsForCache(String GcCode)
	{
		ArrayList<String> images = new ArrayList<String>();

		CoreCursor reader = Database.Data.rawQuery("select ImageUrl from Images where GcCode=?", new String[]
			{ GcCode });

		if (reader == null) return images;
		reader.moveToFirst();
		while (reader.isAfterLast() == false)
		{
			images.add(reader.getString(0));
			reader.moveToNext();
		}
		reader.close();

		return images;
	}

	public int getImageCount(String whereClause)
	{
		int count = 0;

		CoreCursor reader = Database.Data.rawQuery("select count(id) from Images where GcCode in (select GcCode from Caches "
				+ ((whereClause.length() > 0) ? "where " + whereClause : whereClause) + ")", null);

		if (reader == null) return 0;
		reader.moveToFirst();

		if (!reader.isAfterLast())
		{
			count = reader.getInt(0);
		}
		reader.close();

		return count;
	}

	public ArrayList<String> getGcCodes(String whereClause)
	{
		ArrayList<String> gcCodes = new ArrayList<String>();

		CoreCursor reader = Database.Data.rawQuery("select GcCode from Caches "
				+ ((whereClause.length() > 0) ? "where " + whereClause : whereClause), null);

		if (reader == null) return gcCodes;
		reader.moveToFirst();

		while (!reader.isAfterLast())
		{
			gcCodes.add(reader.getString(0));
			reader.moveToNext();
		}
		reader.close();

		return gcCodes;
	}
}
