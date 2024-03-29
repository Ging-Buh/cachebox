/*
 * Copyright (C) 2014 team-cachebox.de
 *
 * Licensed under the : GNU General Public License (GPL);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.gnu.org/licenses/gpl.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.droidcachebox.database;

import de.droidcachebox.core.CoreData;
import de.droidcachebox.database.Database_Core.Parameters;
import de.droidcachebox.dataclasses.Categories;
import de.droidcachebox.dataclasses.Category;
import de.droidcachebox.dataclasses.GpxFilename;
import de.droidcachebox.utils.log.Log;

public class CategoryDAO {
    private static final String sClass = "CategoryDAO";
    private static CategoryDAO categoryDAO;

    private CategoryDAO() {
    }

    public static CategoryDAO getInstance() {
        if (categoryDAO == null) categoryDAO = new CategoryDAO();
        return categoryDAO;
    }

    private Category readFromCursor(CoreCursor reader) {
        Category result = new Category();

        result.categoryId = reader.getLong(0);
        result.gpxFileName = reader.getString(1);
        result.pinned = reader.getInt(2) != 0;

        // alle GpxFilenames einlesen
        CoreCursor c = CBDB.getInstance().rawQuery("select ID, GPXFilename, Imported, CacheCount from GpxFilenames where CategoryId=?", new String[]{String.valueOf(result.categoryId)});
        c.moveToFirst();
        while (!c.isAfterLast()) {
            GpxFilenameDAO gpxFilenameDAO = new GpxFilenameDAO();
            GpxFilename gpx = gpxFilenameDAO.ReadFromCursor(c);
            result.add(gpx);
            c.moveToNext();
        }
        c.close();

        return result;
    }

    public void setPinned(Category category, boolean pinned) {
        if (category.pinned == pinned)
            return;
        category.pinned = pinned;

        Parameters args = new Parameters();
        args.put("pinned", pinned);
        try {
            CBDB.getInstance().update("Category", args, "Id=" + category.categoryId, null);
        } catch (Exception exc) {
            Log.err(sClass, "setPinned", "CategoryDAO", exc);
        }
    }

    // Categories
    public void loadCategoriesFromDatabase() {
        // read all Categories

        CoreData.categories.beginnUpdate();
        CoreData.categories.clear();

        CoreCursor c = CBDB.getInstance().rawQuery("select ID, GPXFilename, Pinned from Category", null);
        if (c != null) {
            c.moveToFirst();
            while (!c.isAfterLast()) {
                Category category = readFromCursor(c);
                CoreData.categories.add(category);
                c.moveToNext();
            }
            c.close();
        }
        CoreData.categories.sort();
        CoreData.categories.endUpdate();
    }

    public void deleteEmptyCategories() {
        CoreData.categories.beginnUpdate();

        Categories delete = new Categories();
        for (int i = 0, n = CoreData.categories.size(); i < n; i++) {
            Category cat = CoreData.categories.get(i);
            if (cat.CacheCount() == 0) {
                CBDB.getInstance().delete("Category", "Id=?", new String[]{String.valueOf(cat.categoryId)});
                delete.add(cat);
            }
        }

        for (int i = 0, n = delete.size(); i < n; i++) {
            CoreData.categories.remove(delete.get(i));
        }
        CoreData.categories.endUpdate();
    }
}
