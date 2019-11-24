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
    }

    @Override
    public void handleLog(LogEntry log) {
        logDAO.WriteToDatabase(log);
    }

    @Override
    public void handleWaypoint(Waypoint waypoint) {
        waypointDAO.WriteImportToDatabase(waypoint);
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
    public void updateCacheCountForGPXFilenames() {
        Database.Data.updateCacheCountForGPXFilenames();
        CategoryDAO.getInstance().deleteEmptyCategories();
    }

    @Override
    public void handleImage(ImageEntry image, Boolean ignoreExisting) {
        imageDAO.WriteToDatabase(image, ignoreExisting);
    }

}
