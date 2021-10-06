package de.droidcachebox.ex_import;

import de.droidcachebox.core.CoreData;
import de.droidcachebox.database.Cache;
import de.droidcachebox.database.CacheDAO;
import de.droidcachebox.database.Categories;
import de.droidcachebox.database.Category;
import de.droidcachebox.database.CategoryDAO;
import de.droidcachebox.database.GpxFilename;
import de.droidcachebox.database.LogEntry;
import de.droidcachebox.database.LogsTableDAO;
import de.droidcachebox.database.Waypoint;
import de.droidcachebox.database.WaypointDAO;

public class ImportHandler implements IImportHandler {

    private final CacheDAO cacheDAO = CacheDAO.getInstance();
    public Categories categories;

    @Override
    public void handleCache(Cache cache) {

        if (cacheDAO.cacheExists(cache.generatedId)) {
            cacheDAO.UpdateDatabase(cache);
        } else {
            cacheDAO.WriteToDatabase(cache);
        }

        if (cache.getWayPoints().size() > 0) {
            for (int i = 0; i < cache.getWayPoints().size(); i++) {
                handleWayPoint(cache.getWayPoints().get(i));
            }
        }

        // Delete LongDescription from this Cache! LongDescription is Loading by showing DescriptionView direct from DB
        cache.setLongDescription("");
    }

    @Override
    public void handleLog(LogEntry log) {
        LogsTableDAO.getInstance().WriteLogEntry(log);
    }

    @Override
    public void handleWayPoint(Waypoint wayPoint) {
        WaypointDAO.getInstance().WriteImportToDatabase(wayPoint);
    }

    @Override
    public Category getCategory(String fileName) {
        return CoreData.categories.getCategory(fileName);
    }

    @Override
    public GpxFilename NewGpxFilename(Category category, String fileName) {
        return category.addGpxFilename(fileName);
    }

    @Override
    public void updateCacheCountForGPXFilenames() {
        CacheDAO.getInstance().updateCacheCountForGPXFilenames();
        CategoryDAO.getInstance().deleteEmptyCategories();
    }

}
