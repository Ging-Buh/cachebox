package de.droidcachebox.dataclasses;

import java.util.ArrayList;

import de.droidcachebox.core.FilterProperties;
import de.droidcachebox.database.CBDB;
import de.droidcachebox.database.CoreCursor;
import de.droidcachebox.database.Database_Core.Parameters;
import de.droidcachebox.utils.FileFactory;
import de.droidcachebox.utils.MoveableList;

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
            if (gpxFilename.equalsIgnoreCase(category.gpxFileName)) {
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

    public Category createNewCategory(String filename) {
        filename = FileFactory.createFile(filename).getName();

        // create a new Category in DB
        Category result = new Category();

        Parameters args = new Parameters();
        args.put("GPXFilename", filename);
        try {
            CBDB.getInstance().insert("Category", args);
        } catch (Exception ignored) {
        }

        long Category_ID = 0;

        CoreCursor c = CBDB.getInstance().rawQuery("Select max(ID) from Category", null);
        if (c != null) {
            c.moveToFirst();
            if (!c.isAfterLast()) {
                Category_ID = c.getLong(0);
            }
            c.close();
        }
        result.categoryId = Category_ID;
        result.gpxFileName = filename;
        result.checked = true;
        result.pinned = false;

        return result;
    }

    private void checkAll() {
        for (int i = 0, n = size(); i < n; i++) {
            Category cat = get(i);
            cat.checked = false;
            for (GpxFilename gpx : cat) {
                gpx.checked = true;
            }
        }
    }

    public void readFromFilter(FilterProperties filter) {
        checkAll();
        boolean foundOne = false;
        for (long id : filter.categories) {
            for (int i = 0, n = size(); i < n; i++) {
                Category cat = get(i);
                if (cat.categoryId == id) {
                    cat.checked = true;
                    foundOne = true;
                }
            }
        }
        if (!foundOne) {
            // if no category found -> check all
            for (int i = 0, n = size(); i < n; i++) {
                Category cat = get(i);
                cat.checked = true;
            }
        }
        for (long id : filter.gpxFilenameIds) {
            for (int i = 0, n = size(); i < n; i++) {
                Category cat = get(i);
                for (GpxFilename gpx : cat) {
                    if (gpx.id == id) {
                        gpx.checked = false;
                    }
                }
            }
        }
        for (Category category : this) {
            // if category not checked -> uncheck all included entries of gpx-files
            if (category.checked)
                continue;
            for (GpxFilename gpx : category) {
                gpx.checked = false;
            }
        }
    }

    public void updateFilterProperties(FilterProperties filter) {
        if (filter.gpxFilenameIds == null) filter.gpxFilenameIds = new ArrayList<>();
        filter.gpxFilenameIds.clear();
        if (filter.categories == null) filter.categories = new ArrayList<>();
        filter.categories.clear();
        for (Category category : this) {
            if (category.checked) {
                filter.categories.add(category.categoryId);
                for (GpxFilename gpx : category) {
                    if (!gpx.checked)
                        filter.gpxFilenameIds.add(gpx.id);
                }
            } else {
                for (GpxFilename gpx : category)
                    filter.gpxFilenameIds.add(gpx.id);
            }
        }
    }

}
