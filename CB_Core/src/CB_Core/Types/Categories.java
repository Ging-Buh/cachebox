package CB_Core.Types;

import java.io.File;

import CB_Core.Database;
import CB_Core.FilterProperties;
import CB_Utils.Util.MoveableList;
import de.cb.sqlite.CoreCursor;
import de.cb.sqlite.Database_Core.Parameters;

public class Categories extends MoveableList<Category> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5348105269187711224L;

	public Categories() {
	}

	public Category getCategory(String filename) {
		filename = new File(filename).getName();
		for (int i = 0, n = this.size(); i < n; i++) {
			Category category = this.get(i);
			if (filename.toUpperCase().equals(category.GpxFilename.toUpperCase())) {
				return category;
			}
		}

		Category cat = createNewCategory(filename);
		this.add(cat);
		return cat;
	}

	public Category getCategoryByGpxFilenameId(long gpxFilenameId) {
		for (int i = 0, n = this.size(); i < n; i++) {
			Category category = this.get(i);
			if (category.containsGpxFilenameId(gpxFilenameId)) {
				return category;
			}
		}
		return null;
	}

	public Category createNewCategory(String filename) {
		filename = new File(filename).getName();

		// neue Category in DB anlegen
		Category result = new Category();

		Parameters args = new Parameters();
		args.put("GPXFilename", filename);
		try {
			Database.Data.insert("Category", args);
		} catch (Exception exc) {
			//log.error("CreateNewCategory", filename, exc);
		}

		long Category_ID = 0;

		CoreCursor reader = Database.Data.rawQuery("Select max(ID) from Category", null);
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
		for (int i = 0, n = this.size(); i < n; i++) {
			Category cat = this.get(i);
			cat.Checked = false;
			for (GpxFilename gpx : cat) {
				gpx.Checked = true;
			}
		}
	}

	public void ReadFromFilter(FilterProperties filter) {
		checkAll();
		boolean foundOne = false;
		for (long id : filter.Categories) {
			for (int i = 0, n = this.size(); i < n; i++) {
				Category cat = this.get(i);
				if (cat.Id == id) {
					cat.Checked = true;
					foundOne = true;
				}
			}
		}
		if (!foundOne) {
			// Wenn gar keine Category aktiv -> alle aktivieren!
			for (int i = 0, n = this.size(); i < n; i++) {
				Category cat = this.get(i);
				cat.Checked = true;
			}
		}
		for (long id : filter.GPXFilenameIds) {
			for (int i = 0, n = this.size(); i < n; i++) {
				Category cat = this.get(i);
				for (GpxFilename gpx : cat) {
					if (gpx.Id == id) {
						gpx.Checked = false;
					}
				}
			}
		}
		for (int i = 0, n = this.size(); i < n; i++) {
			Category cat = this.get(i);
			// wenn Category nicht checked ist -> alle GpxFilenames deaktivieren
			if (cat.Checked)
				continue;
			for (GpxFilename gpx : cat) {
				gpx.Checked = false;
			}
		}
	}

	public void WriteToFilter(FilterProperties filter) {
		filter.GPXFilenameIds.clear();
		filter.Categories.clear();
		int n = this.size();
		for (int i = 0; i < n; i++) {
			Category cat = this.get(i);
			if (cat.Checked) {
				// GpxFilename Filter nur setzen, wenn die Category aktiv ist!
				filter.Categories.add(cat.Id);
				for (GpxFilename gpx : cat) {
					if (!gpx.Checked)
						filter.GPXFilenameIds.add(gpx.Id);
				}
			} else {
				// Category ist nicht aktiv -> alle GpxFilenames in Filter aktivieren
				for (GpxFilename gpx : cat)
					filter.GPXFilenameIds.add(gpx.Id);
			}
		}
	}

}
