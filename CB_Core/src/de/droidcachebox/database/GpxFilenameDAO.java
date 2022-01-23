package de.droidcachebox.database;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.droidcachebox.database.Database_Core.Parameters;
import de.droidcachebox.dataclasses.GpxFilename;

public class GpxFilenameDAO {
    public GpxFilename ReadFromCursor(CoreCursor reader) {
        long id;
        String gpxFileName;
        long categoryId = -1;

        id = reader.getLong(0);
        gpxFileName = reader.getString(1);

        GpxFilename result = new GpxFilename(id, gpxFileName, categoryId);

        if (reader.isNull(2))
            result.importedDate = new Date();
        else {
            String sDate = reader.getString(2);
            DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                result.importedDate = iso8601Format.parse(sDate);
            } catch (ParseException e) {
                result.importedDate = new Date();
            }
        }

        if (reader.isNull(3))
            result.numberOfGeocaches = 0;
        else
            result.numberOfGeocaches = reader.getInt(3);

        return result;
    }

    public void GPXFilenameUpdateCacheCount() {
        // welche GPXFilenamen sind in der DB erfasst

        CoreCursor reader = CBDB.getInstance().rawQuery("select GPXFilename_ID, Count(*) as CacheCount from Caches where GPXFilename_ID is not null Group by GPXFilename_ID", null);

        reader.moveToFirst();
        while (!reader.isAfterLast()) {
            Integer GPXFilename_ID = reader.getInt(0);
            Integer CacheCount = reader.getInt(1);

            Parameters args = new Parameters();
            args.put("CacheCount", CacheCount);

            CBDB.getInstance().update("GPXFilenames", args, "ID=?", new String[]{String.valueOf(GPXFilename_ID)});
            reader.moveToNext();
        }

        reader.close();

        CBDB.getInstance().delete("GPXFilenames", "Cachecount is NULL or CacheCount = 0", null);
        CBDB.getInstance().delete("GPXFilenames", "ID not in (Select GPXFilename_ID From Caches)", null);
    }

}
