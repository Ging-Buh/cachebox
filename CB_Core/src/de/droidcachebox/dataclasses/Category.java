package de.droidcachebox.dataclasses;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import de.droidcachebox.database.CBDB;
import de.droidcachebox.database.CoreCursor;
import de.droidcachebox.database.Database_Core.Parameters;
import de.droidcachebox.utils.FileFactory;

public class Category extends ArrayList<GpxFilename> implements Comparable<Category> {
    /**
     *
     */
    private static final long serialVersionUID = -7257078663021910097L;
    public long categoryId;
    public String gpxFileName;
    public boolean pinned;
    public boolean checked;

    public Category() {
    }

    /**
     * Does not check if filename not already exists in this category
     *
     * @param fileName .
     * @return .
     */
    public GpxFilename addGpxFilename(String fileName) {
        fileName = FileFactory.createFile(fileName).getName();

        Parameters args = new Parameters();
        args.put("GPXFilename", fileName);
        args.put("CategoryId", categoryId);
        DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        String sTimeStamp = iso8601Format.format(new Date());
        args.put("Imported", sTimeStamp);
        try {
            CBDB.getInstance().insert("GpxFilenames", args);
        } catch (Exception ignored) {
        }

        long GPXFilename_ID = 0;

        CoreCursor c = CBDB.getInstance().rawQuery("Select max(ID) from GpxFilenames", null);
        if (c != null) {
            c.moveToFirst();
            if (!c.isAfterLast()) {
                GPXFilename_ID = c.getLong(0);
            }
            c.close();
        }
        GpxFilename result = new GpxFilename(GPXFilename_ID, fileName, categoryId);
        this.add(result);
        return result;
    }

    public GpxFilename addGpxFilename(String fileName, Date importedDate) {

        Parameters args = new Parameters();
        args.put("GPXFilename", fileName);
        args.put("CategoryId", categoryId);
        DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        String sTimeStamp = iso8601Format.format(importedDate);
        args.put("Imported", sTimeStamp);
        try {
            CBDB.getInstance().insert("GpxFilenames", args);
        } catch (Exception ignored) {
        }

        long GPXFilename_ID = 0;
        CoreCursor c = CBDB.getInstance().rawQuery("Select max(ID) from GpxFilenames", null);
        if (c != null) {
            c.moveToFirst();
            if (!c.isAfterLast()) {
                GPXFilename_ID = c.getLong(0);
            }
            c.close();
        }
        GpxFilename result = new GpxFilename(GPXFilename_ID, fileName, this.categoryId);
        this.add(result);
        return result;
    }

    public int CacheCount() {
        int result = 0;
        for (GpxFilename gpx : this)
            result += gpx.numberOfGeocaches;
        return result;
    }

    public Date LastImported() {
        if (size() == 0)
            return new Date();
        return this.get(this.size() - 1).importedDate;
    }

    public String GpxFilenameWoNumber() {
        // ignore number of PQ, if has description.
        String name = gpxFileName;
        int pos = name.indexOf('_');
        if (pos < 0)
            return name;
        String part = name.substring(0, pos);
        if (part.length() < 7)
            return name;
        try {
            // ignore beginning only if this is a number
            Integer.valueOf(part);
        } catch (Exception exc) {
            return name;
        }

        name = name.substring(pos + 1);

        if (name.toLowerCase().indexOf(".gpx") == name.length() - 4)
            name = name.substring(0, name.length() - 4);
        return name;
    }

    /**
     * @return state concerning the check marker of the included entries</br>
     * 0 = none checked</br>
     * 1 = all checked</br>
     * -1 = at least one checked</br>
     *
     */
    public int getCheckState() {
        int result;

        int chkCounter = 0;
        int counter = 0;
        for (GpxFilename gpx : this) {
            if (gpx.checked)
                chkCounter++;

            counter++;
        }

        if (chkCounter == 0)
            result = 0;
        else if (chkCounter == counter)
            result = 1;
        else
            result = -1;

        return result;
    }

    public String getGpxFilename(long gpxFilenameId) {
        for (GpxFilename gpx : this) {
            if (gpx.id == gpxFilenameId)
                return gpx.gpxFileName;
        }
        return "";
    }

    public boolean containsGpxFilenameId(long gpxFilenameId) {
        for (GpxFilename gpx : this) {
            if (gpx.id == gpxFilenameId)
                return true;
        }
        return false;
    }

    public boolean containsGpxFilename(String gpxFilename) {
        for (GpxFilename gpx : this) {
            if (gpx.gpxFileName.equals(gpxFilename))
                return true;
        }
        return false;
    }

    @Override
    public int compareTo(Category o) {
        if (o.categoryId > this.categoryId)
            return 1;
        else if (o.categoryId < this.categoryId)
            return -1;
        else
            return 0;
    }
}
