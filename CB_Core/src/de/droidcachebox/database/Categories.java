package de.droidcachebox.database;

import de.droidcachebox.core.FilterProperties;
import de.droidcachebox.database.Database_Core.Parameters;
import de.droidcachebox.utils.FileFactory;
import de.droidcachebox.utils.MoveableList;

import java.util.ArrayList;

public class Categories extends MoveableList<Category> {
    /**
     *
     */
    private static final long serialVersionUID = 5348105269187711224L;

    public Categories() {
    }

    public Category getCategory(String gpxFilename) {
        // if necessary, adds a new Category entry and then returns the entry from Categories
        gpxFilename = FileFactory.createFile(gpxFilename).getName();
        for (Category category : this) {
            if (gpxFilename.toUpperCase().equals(category.GpxFilename.toUpperCase())) {
                return category;
            }
        }

        Category newCategory = createNewCategory(gpxFilename);
        add(newCategory);
        return newCategory;
    }

    public Category getCategoryByGpxFilenameId(long gpxFilenameId) {
        for (Category category : this) {
            if (category.containsGpxFilenameId(gpxFilenameId)) {
                return category;
            }
        }
        return null;
    }

    Category createNewCategory(String filename) {
        filename = FileFactory.createFile(filename).getName();

        // neue Category in DB anlegen
        Category result = new Category();

        Parameters args = new Parameters();
        args.put("GPXFilename", filename);
        try {
            Database.Data.sql.insert("Category", args);
        } catch (Exception exc) {
            //Log.err(log, "CreateNewCategory", filename, exc);
        }

        long Category_ID = 0;

        CoreCursor reader = Database.Data.sql.rawQuery("Select max(ID) from Category", null);
        reader.moveToFirst();
        if (!reader.isAfterLast()) {
            Category_ID = reader.getLong(0);
        }
        reader.close();
        result.Id = Category_ID;
        result.GpxFilename = filename;
        result.Checked = true;
        result.pinned = false;

        return result;
    }

    private void checkAll() {
        for (int i = 0, n = size(); i < n; i++) {
            Category cat = get(i);
            cat.Checked = false;
            for (GpxFilename gpx : cat) {
                gpx.Checked = true;
            }
        }
    }

    public void readFromFilter(FilterProperties filter) {
        checkAll();
        boolean foundOne = false;
        for (long id : filter.categories) {
            for (int i = 0, n = size(); i < n; i++) {
                Category cat = get(i);
                if (cat.Id == id) {
                    cat.Checked = true;
                    foundOne = true;
                }
            }
        }
        if (!foundOne) {
            // Wenn gar keine Category aktiv -> alle aktivieren!
            for (int i = 0, n = size(); i < n; i++) {
                Category cat = get(i);
                cat.Checked = true;
            }
        }
        for (long id : filter.gpxFilenameIds) {
            for (int i = 0, n = size(); i < n; i++) {
                Category cat = get(i);
                for (GpxFilename gpx : cat) {
                    if (gpx.Id == id) {
                        gpx.Checked = false;
                    }
                }
            }
        }
        for (Category category : this) {
            // wenn Category nicht checked ist -> alle GpxFilenames deaktivieren
            if (category.Checked)
                continue;
            for (GpxFilename gpx : category) {
                gpx.Checked = false;
            }
        }
    }

    public FilterProperties updateFilterProperties(FilterProperties filter) {
        if (filter.gpxFilenameIds == null) filter.gpxFilenameIds = new ArrayList<>();
        filter.gpxFilenameIds.clear();
        if (filter.categories == null) filter.categories = new ArrayList<>();
        filter.categories.clear();
        for (Category category : this) {
            if (category.Checked) {
                // GpxFilename Filter nur setzen, wenn die Category aktiv ist!
                filter.categories.add(category.Id);
                for (GpxFilename gpx : category) {
                    if (!gpx.Checked)
                        filter.gpxFilenameIds.add(gpx.Id);
                }
            } else {
                // Category ist nicht aktiv -> alle GpxFilenames in Filter aktivieren
                for (GpxFilename gpx : category)
                    filter.gpxFilenameIds.add(gpx.Id);
            }
        }
        return filter;
    }

}
