package CB_Core.Types;

import CB_Core.Database;
import CB_Utils.fileProvider.FileFactory;
import de.cb.sqlite.CoreCursor;
import de.cb.sqlite.Database_Core.Parameters;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Category extends ArrayList<GpxFilename> implements Comparable<Category> {
    /**
     *
     */
    private static final long serialVersionUID = -7257078663021910097L;
    public long Id;
    public String GpxFilename;
    public boolean pinned;
    public boolean Checked;

    public Category() {

    }

    /**
     * Does not check if filename not already exists in this category
     *
     * @param filename
     * @return
     */
    public GpxFilename addGpxFilename(String filename) {
        filename = FileFactory.createFile(filename).getName();

        Parameters args = new Parameters();
        args.put("GPXFilename", filename);
        args.put("CategoryId", this.Id);
        DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String stimestamp = iso8601Format.format(new Date());
        args.put("Imported", stimestamp);
        try {
            Database.Data.insert("GpxFilenames", args);
        } catch (Exception exc) {
            //Log.err(log, "CreateNewGpxFilename", filename, exc);
        }

        long GPXFilename_ID = 0;

        CoreCursor reader = Database.Data.rawQuery("Select max(ID) from GpxFilenames", null);
        reader.moveToFirst();
        if (!reader.isAfterLast()) {
            GPXFilename_ID = reader.getLong(0);
        }
        reader.close();
        GpxFilename result = new GpxFilename(GPXFilename_ID, filename, this.Id);
        this.add(result);
        return result;
    }

    public int CacheCount() {
        int result = 0;
        for (GpxFilename gpx : this)
            result += gpx.CacheCount;
        return result;
    }

    public Date LastImported() {
        if (size() == 0)
            return new Date();
        return this.get(this.size() - 1).Imported;
    }

    public String GpxFilenameWoNumber() {
        // Nummer der PQ weglassen, wenn dahinter noch eine Bezeichnung kommt.
        String name = GpxFilename;
        int pos = name.indexOf('_');
        if (pos < 0)
            return name;
        String part = name.substring(0, pos);
        if (part.length() < 7)
            return name;
        try {
            // Vorderen Teil nur dann weglassen, wenn dies eine Zahl ist.
            Integer.valueOf(part);
        } catch (Exception exc) {
            return name;
        }

        name = name.substring(pos + 1, name.length());

        if (name.toLowerCase().indexOf(".gpx") == name.length() - 4)
            name = name.substring(0, name.length() - 4);
        return name;
    }

    @Override
    public int compareTo(Category o) {
        if (o.Id > this.Id)
            return 1;
        else if (o.Id < this.Id)
            return -1;
        else
            return 0;
    }

    /**
     * gibt den chk status der enthaltenen GpxFiles zurück </br> 0 = keins
     * ausgewählt </br> 1 = alle ausgewählt </br> -1 = nicht alle, aber
     * mindestens eins ausgewählt
     *
     * @return
     */
    public int getCheck() {
        int result = 0;

        int chkCounter = 0;
        int counter = 0;
        for (GpxFilename gpx : this) {
            if (gpx.Checked)
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
            if (gpx.Id == gpxFilenameId)
                return gpx.GpxFileName;
        }
        return "";
    }

    public boolean containsGpxFilenameId(long gpxFilenameId) {
        for (GpxFilename gpx : this) {
            if (gpx.Id == gpxFilenameId)
                return true;
        }
        return false;
    }

    public boolean containsGpxFilename(String gpxFilename) {
        for (GpxFilename gpx : this) {
            if (gpx.GpxFileName.equals(gpxFilename))
                return true;
        }
        return false;
    }

}
