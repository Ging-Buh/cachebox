package de.droidcachebox.ex_import;

import de.droidcachebox.core.CoreData;
import de.droidcachebox.database.CBDB;
import de.droidcachebox.database.Cache;
import de.droidcachebox.database.CacheDAO;
import de.droidcachebox.database.Categories;
import de.droidcachebox.database.Category;
import de.droidcachebox.database.CategoryDAO;
import de.droidcachebox.database.GpxFilename;
import de.droidcachebox.database.ImageDAO;
import de.droidcachebox.database.LogEntry;
import de.droidcachebox.database.Waypoint;
import de.droidcachebox.database.WaypointDAO;

public class ImportHandler implements IImportHandler {

    public Categories categories;
    private CacheDAO cacheDAO = new CacheDAO();
    private WaypointDAO waypointDAO = new WaypointDAO();
    private ImageDAO imageDAO = new ImageDAO();

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
        CBDB.Data.WriteLogEntry(log);
    }

    @Override
    public void handleWayPoint(Waypoint wayPoint) {
        waypointDAO.WriteImportToDatabase(wayPoint);
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
        CBDB.Data.updateCacheCountForGPXFilenames();
        CategoryDAO.getInstance().deleteEmptyCategories();
    }

}
