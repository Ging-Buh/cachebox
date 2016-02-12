package CB_Core.Import;

import CB_Core.Types.Cache;
import CB_Core.Types.Category;
import CB_Core.Types.GpxFilename;
import CB_Core.Types.ImageEntry;
import CB_Core.Types.LogEntry;
import CB_Core.Types.Waypoint;

public interface IImportHandler {

	void handleCache(Cache cache);

	void handleLog(LogEntry log);

	void handleImage(ImageEntry image, Boolean ignoreExisting);

	void handleWaypoint(Waypoint waypoint);

	Category getCategory(String Filename);

	GpxFilename NewGpxFilename(Category category, String filename);

	void GPXFilenameUpdateCacheCount();

}
