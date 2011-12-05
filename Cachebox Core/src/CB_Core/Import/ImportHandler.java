package CB_Core.Import;

import CB_Core.GlobalCore;
import CB_Core.DAO.CacheDAO;
import CB_Core.DAO.CategoryDAO;
import CB_Core.DAO.GpxFilenameDAO;
import CB_Core.DAO.ImageDAO;
import CB_Core.DAO.LogDAO;
import CB_Core.DAO.WaypointDAO;
import CB_Core.Types.Cache;
import CB_Core.Types.Categories;
import CB_Core.Types.Category;
import CB_Core.Types.GpxFilename;
import CB_Core.Types.ImageEntry;
import CB_Core.Types.LogEntry;
import CB_Core.Types.Waypoint;

public class ImportHandler implements IImportHandler
{

	CacheDAO cacheDAO = new CacheDAO();
	LogDAO logDAO = new LogDAO();
	WaypointDAO waypointDAO = new WaypointDAO();
	CategoryDAO categoryDAO = new CategoryDAO();
	GpxFilenameDAO gpxFilenameDAO = new GpxFilenameDAO();
	ImageDAO imageDAO = new ImageDAO();

	public Integer cacheCount = 0;
	public Integer logCount = 0;
	public Integer waypointCount = 0;

	public Categories categories;

	@Override
	public void handleCache(Cache cache)
	{

		if (cacheDAO.cacheExists(cache.Id))
		{
			cacheDAO.UpdateDatabase(cache);
		}
		else
		{
			cacheDAO.WriteToDatabase(cache);
		}
		cacheCount++;
	}

	@Override
	public void handleLog(LogEntry log)
	{
		logDAO.WriteToDatabase(log);
		logCount++;
	}

	@Override
	public void handleWaypoint(Waypoint waypoint)
	{
		waypointDAO.WriteImportToDatabase(waypoint);
		waypointCount++;
	}

	@Override
	public Category getCategory(String filename)
	{
		return categoryDAO.GetCategory(GlobalCore.Categories, filename);
	}

	@Override
	public GpxFilename NewGpxFilename(Category category, String filename)
	{
		return categoryDAO.CreateNewGpxFilename(category, filename);
	}

	@Override
	public void GPXFilenameUpdateCacheCount()
	{
		gpxFilenameDAO.GPXFilenameUpdateCacheCount();

		categoryDAO.LoadCategoriesFromDatabase(GlobalCore.Categories);
		categoryDAO.DeleteEmptyCategories(GlobalCore.Categories);
	}

	@Override
	public void handleImage(ImageEntry image, Boolean ignoreExisting)
	{
		imageDAO.WriteToDatabase(image, ignoreExisting);
	}

}
