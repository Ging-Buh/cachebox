package CB_Core.DAO;

import CB_Core.Database;
import CB_Core.Types.GpxFilename;
import de.cb.sqlite.CoreCursor;
import de.cb.sqlite.Database_Core.Parameters;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GpxFilenameDAO {
    public GpxFilename ReadFromCursor(CoreCursor reader) {
        long id;
        String gpxFileName;
        long categoryId = -1;

        id = reader.getLong(0);
        gpxFileName = reader.getString(1);

        GpxFilename result = new GpxFilename(id, gpxFileName, categoryId);

        if (reader.isNull(2))
            result.Imported = new Date();
        else {
            String sDate = reader.getString(2);
            DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                result.Imported = iso8601Format.parse(sDate);
            } catch (ParseException e) {
                result.Imported = new Date();
            }
        }

        if (reader.isNull(3))
            result.CacheCount = 0;
        else
            result.CacheCount = reader.getInt(3);

        return result;
    }

    public void GPXFilenameUpdateCacheCount() {
        // welche GPXFilenamen sind in der DB erfasst

        CoreCursor reader = Database.Data.sql.rawQuery("select GPXFilename_ID, Count(*) as CacheCount from Caches where GPXFilename_ID is not null Group by GPXFilename_ID", null);

        reader.moveToFirst();
        while (!reader.isAfterLast()) {
            Integer GPXFilename_ID = reader.getInt(0);
            Integer CacheCount = reader.getInt(1);

            Parameters args = new Parameters();
            args.put("CacheCount", CacheCount);

            Database.Data.sql.update("GPXFilenames", args, "ID=?", new String[]{String.valueOf(GPXFilename_ID)});
            reader.moveToNext();
        }

        reader.close();

        Database.Data.sql.delete("GPXFilenames", "Cachecount is NULL or CacheCount = 0", null);
        Database.Data.sql.delete("GPXFilenames", "ID not in (Select GPXFilename_ID From Caches)", null);
    }

}
