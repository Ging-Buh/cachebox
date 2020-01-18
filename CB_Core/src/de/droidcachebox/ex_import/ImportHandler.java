package de.droidcachebox.ex_import;

import de.droidcachebox.core.CoreSettingsForward;
import de.droidcachebox.database.*;

public class ImportHandler implements IImportHandler {

    public Categories categories;
    private CacheDAO cacheDAO = new CacheDAO();
    private LogDAO logDAO = new LogDAO();
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
        logDAO.WriteToDatabase(log);
    }

    @Override
    public void handleWayPoint(Waypoint wayPoint) {
        waypointDAO.WriteImportToDatabase(wayPoint);
    }

    @Override
    public Category getCategory(String fileName) {
        return CoreSettingsForward.categories.getCategory(fileName);
    }

    @Override
    public GpxFilename NewGpxFilename(Category category, String fileName) {
        return category.addGpxFilename(fileName);
    }

    @Override
    public void updateCacheCountForGPXFilenames() {
        Database.Data.updateCacheCountForGPXFilenames();
        CategoryDAO.getInstance().deleteEmptyCategories();
    }

}
