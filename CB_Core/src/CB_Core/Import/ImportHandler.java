package CB_Core.Import;

import CB_Core.CoreSettingsForward;
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

public class ImportHandler implements IImportHandler {

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
	public void handleCache(Cache cache) {

		if (cacheDAO.cacheExists(cache.Id)) {
			cacheDAO.UpdateDatabase(cache);
		} else {
			cacheDAO.WriteToDatabase(cache);
		}

		if (cache.waypoints.size() > 0) {
			for (int i = 0; i < cache.waypoints.size(); i++) {
				handleWaypoint(cache.waypoints.get(i));
			}
		}

		// Delete LongDescription from this Cache! LongDescription is Loading by showing DescriptionView direct from DB
		cache.setLongDescription("");
		cacheCount++;
	}

	@Override
	public void handleLog(LogEntry log) {
		logDAO.WriteToDatabase(log);
		logCount++;
	}

	@Override
	public void handleWaypoint(Waypoint waypoint) {
		waypointDAO.WriteImportToDatabase(waypoint);
		waypointCount++;
	}

	@Override
	public Category getCategory(String filename) {
		return CoreSettingsForward.Categories.getCategory(filename);
	}

	@Override
	public GpxFilename NewGpxFilename(Category category, String filename) {
		return category.addGpxFilename(filename);
	}

	@Override
	public void GPXFilenameUpdateCacheCount() {
		gpxFilenameDAO.GPXFilenameUpdateCacheCount();

		categoryDAO.LoadCategoriesFromDatabase();
		categoryDAO.DeleteEmptyCategories();
	}

	@Override
	public void handleImage(ImageEntry image, Boolean ignoreExisting) {
		imageDAO.WriteToDatabase(image, ignoreExisting);
	}

}
