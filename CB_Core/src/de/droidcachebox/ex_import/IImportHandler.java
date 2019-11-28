package de.droidcachebox.ex_import;

import de.droidcachebox.database.*;

public interface IImportHandler {

    void handleCache(Cache cache);

    void handleLog(LogEntry log);

    void handleWayPoint(Waypoint wayPoint);

    Category getCategory(String fileName);

    GpxFilename NewGpxFilename(Category category, String fileName);

    void updateCacheCountForGPXFilenames();

}
