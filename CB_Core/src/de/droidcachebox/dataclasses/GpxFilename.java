package de.droidcachebox.dataclasses;

import java.util.Date;

public class GpxFilename {
    public long id;
    public String gpxFileName;
    public Date importedDate;
    public int numberOfGeocaches;
    public long categoryId;
    public boolean checked;

    public GpxFilename(long id, String gpxFileName, long categoryId) {
        this.id = id;
        this.gpxFileName = gpxFileName;
        this.importedDate = new Date();
        this.categoryId = categoryId;
    }

}
