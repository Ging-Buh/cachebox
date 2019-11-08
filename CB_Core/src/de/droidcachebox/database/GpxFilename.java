package de.droidcachebox.database;

import java.util.Date;

public class GpxFilename implements Comparable<GpxFilename> {
    public long Id;
    public String GpxFileName;
    public Date Imported;
    public int CacheCount;
    public long CategoryId;
    public boolean Checked;

    public GpxFilename(long Id, String GpxFileName, long categoryId) {
        this.Id = Id;
        this.GpxFileName = GpxFileName;
        this.Imported = new Date();
        this.CategoryId = categoryId;
    }

    @Override
    public int compareTo(GpxFilename arg0) {

        return 0;
    }

}
