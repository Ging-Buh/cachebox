package de.droidcachebox.ex_import;

import de.droidcachebox.database.*;

public interface IImportHandler {

    void handleCache(Cache cache);

    void handleLog(LogEntry log);

    void handleImage(ImageEntry image, Boolean ignoreExisting);

    void handleWaypoint(Waypoint waypoint);

    Category getCategory(String Filename);

    GpxFilename NewGpxFilename(Category category, String filename);

    void updateCacheCountForGPXFilenames();

}
