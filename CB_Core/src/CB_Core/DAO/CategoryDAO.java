package CB_Core.DAO;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import CB_Core.CoreSettingsForward;
import CB_Core.DB.Database;
import CB_Core.Types.Categories;
import CB_Core.Types.Category;
import CB_Core.Types.GpxFilename;
import CB_Utils.DB.CoreCursor;
import CB_Utils.DB.Database_Core.Parameters;
import CB_Utils.Log.Logger;

public class CategoryDAO
{
	public Category ReadFromCursor(CoreCursor reader)
	{
		Category result = new Category();

		result.Id = reader.getLong(0);
		result.GpxFilename = reader.getString(1);
		result.pinned = reader.getInt(2) != 0;

		// alle GpxFilenames einlesen
		CoreCursor reader2 = Database.Data.rawQuery("select ID, GPXFilename, Imported, CacheCount from GpxFilenames where CategoryId=?",
				new String[]
					{ String.valueOf(result.Id) });
		reader2.moveToFirst();
		while (reader2.isAfterLast() == false)
		{
			GpxFilenameDAO gpxFilenameDAO = new GpxFilenameDAO();
			GpxFilename gpx = gpxFilenameDAO.ReadFromCursor(reader2);
			result.add(gpx);
			reader2.moveToNext();
		}
		reader2.close();

		return result;
	}

	public Category CreateNewCategory(String filename)
	{
		filename = new File(filename).getName();

		// neue Category in DB anlegen
		Category result = new Category();

		Parameters args = new Parameters();
		args.put("GPXFilename", filename);
		try
		{
			Database.Data.insert("Category", args);
		}
		catch (Exception exc)
		{
			Logger.Error("CreateNewCategory", filename, exc);
		}

		long Category_ID = 0;

		CoreCursor reader = Database.Data.rawQuery("Select max(ID) from Category", null);
		reader.moveToFirst();
		if (reader.isAfterLast() == false)
		{
			Category_ID = reader.getLong(0);
		}
		reader.close();
		result.Id = Category_ID;
		result.GpxFilename = filename;
		result.Checked = true;
		result.pinned = false;

		return result;
	}

	public GpxFilename CreateNewGpxFilename(Category category, String filename)
	{
		filename = new File(filename).getName();

		Parameters args = new Parameters();
		args.put("GPXFilename", filename);
		args.put("CategoryId", category.Id);
		DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String stimestamp = iso8601Format.format(new Date());
		args.put("Imported", stimestamp);
		try
		{
			Database.Data.insert("GpxFilenames", args);
		}
		catch (Exception exc)
		{
			Logger.Error("CreateNewGpxFilename", filename, exc);
		}

		long GPXFilename_ID = 0;

		CoreCursor reader = Database.Data.rawQuery("Select max(ID) from GpxFilenames", null);
		reader.moveToFirst();
		if (reader.isAfterLast() == false)
		{
			GPXFilename_ID = reader.getLong(0);
		}
		reader.close();
		GpxFilename result = new GpxFilename(GPXFilename_ID, filename, category.Id);
		category.add(result);
		return result;
	}

	public void SetPinned(Category category, boolean pinned)
	{
		if (category.pinned == pinned) return;
		category.pinned = pinned;

		Parameters args = new Parameters();
		args.put("pinned", pinned);
		try
		{
			Database.Data.update("Category", args, "Id=" + String.valueOf(category.Id), null);
		}
		catch (Exception exc)
		{
			Logger.Error("SetPinned", "CategoryDAO", exc);
		}
	}

	// Categories
	public void LoadCategoriesFromDatabase()
	{
		// alle Categories einlesen

		CoreSettingsForward.Categories.beginnTransaction();

		CoreSettingsForward.Categories.clear();

		CoreCursor reader = Database.Data.rawQuery("select ID, GPXFilename, Pinned from Category", null);
		reader.moveToFirst();

		while (reader.isAfterLast() == false)
		{
			Category category = ReadFromCursor(reader);
			CoreSettingsForward.Categories.add(category);
			reader.moveToNext();
		}
		reader.close();
		CoreSettingsForward.Categories.sort();
		CoreSettingsForward.Categories.endTransaction();
	}

	public Category GetCategory(Categories categories, String filename)
	{
		filename = new File(filename).getName();
		for (int i = 0, n = categories.size(); i < n; i++)
		{
			Category category = categories.get(i);
			if (filename.toUpperCase().equals(category.GpxFilename.toUpperCase()))
			{
				return category;
			}
		}

		Category cat = CreateNewCategory(filename);
		categories.add(cat);
		return cat;
	}

	public void DeleteEmptyCategories()
	{
		CoreSettingsForward.Categories.beginnTransaction();

		Categories delete = new Categories();
		for (int i = 0, n = CoreSettingsForward.Categories.size(); i < n; i++)
		{
			Category cat = CoreSettingsForward.Categories.get(i);
			if (cat.CacheCount() == 0)
			{
				Database.Data.delete("Category", "Id=?", new String[]
					{ String.valueOf(cat.Id) });
				delete.add(cat);
			}
		}

		for (int i = 0, n = delete.size(); i < n; i++)
		{
			CoreSettingsForward.Categories.remove(delete.get(i));
		}
		CoreSettingsForward.Categories.endTransaction();
	}
}
