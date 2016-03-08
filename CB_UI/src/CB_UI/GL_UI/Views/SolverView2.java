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
package CB_UI.GL_UI.Views;

import org.slf4j.LoggerFactory;

import CB_Core.CacheTypes;
import CB_Core.Database;
import CB_Core.DAO.WaypointDAO;
import CB_Core.Solver.DataTypes.DataType;
import CB_Core.Solver.Solver;
import CB_Core.Solver.SolverZeile;
import CB_Core.Types.Cache;
import CB_Core.Types.Waypoint;
import CB_Locator.CoordinateGPS;
import CB_UI.GlobalCore;
import CB_UI.SelectedCacheEvent;
import CB_UI.SelectedCacheEventList;
import CB_UI.WaypointListChangedEventList;
import CB_UI.GL_UI.Activitys.EditWaypoint;
import CB_UI.GL_UI.Activitys.EditWaypoint.IReturnListener;
import CB_UI.GL_UI.Controls.Dialogs.SolverDialog2;
import CB_UI.GL_UI.Controls.Dialogs.SolverDialog2.ISolverBackStringListener;
import CB_UI.GL_UI.Main.TabMainView;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.GL_UI.Controls.List.Adapter;
import CB_UI_Base.GL_UI.Controls.List.ListViewItemBase;
import CB_UI_Base.GL_UI.Controls.List.V_ListView;
import CB_UI_Base.GL_UI.Controls.MessageBox.GL_MsgBox;
import CB_UI_Base.GL_UI.Controls.MessageBox.GL_MsgBox.OnMsgBoxClickListener;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxIcon;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.GL_UI.Menu.Menu;
import CB_UI_Base.GL_UI.Menu.MenuID;
import CB_UI_Base.GL_UI.Menu.MenuItem;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.GL_UISizes;
import CB_UI_Base.Math.UiSizes;

public class SolverView2 extends V_ListView implements SelectedCacheEvent {
	final static org.slf4j.Logger log = LoggerFactory.getLogger(SolverView2.class);
	private CustomAdapter lvAdapter;
	private Solver solver;
	private Cache cache;

	public SolverView2(CB_RectF rec, String Name) {
		super(rec, Name);

		log.debug("Create SolverView2 => " + rec.toString());
		cache = null;
	}

	@Override
	public void onShow() {
		log.debug("onShow()");
		SelectedCacheEventList.Add(this);

		setBackground(Sprites.ListBack);
		// Reload when
		// - cache has changed
		// - Solver 1 has changed
		if ((GlobalCore.getSelectedCache() != cache) || ((cache != null) && (cache.getSolver1Changed()))) {
			cache = GlobalCore.getSelectedCache();
			intiList();
			cache.setSolver1Changed(false);
		}
	}

	private void intiList() {
		if (cache == null) {
			solver = new Solver("");
		} else {
			this.cache = GlobalCore.getSelectedCache();
			String s = Database.GetSolver(this.cache);
			if (s == null)
				s = "";
			solver = new Solver(s);
			solver.Solve();
			// wenn der Solver noch leer ist oder die letzte Zeile nicht leer ist dann am Ende eine leere Zeile einfügen
			if ((solver.size() == 0) || (solver.get(solver.size() - 1).getOrgText().length() > 0)) {
				solver.add(solver.size(), new SolverZeile(solver, ""));
			}
		}

		lvAdapter = new CustomAdapter(solver);
		this.setBaseAdapter(lvAdapter);

		int itemCount = solver.size();
		int itemSpace = this.getMaxItemCount();

		if (itemSpace >= itemCount) {
			this.setUnDraggable();
		} else {
			this.setDraggable();
		}

		this.setSelection(0);
		this.setListPos(0, false);

		this.invalidate();
		GL.that.renderOnce();
	}

	private void reloadList() {
		lvAdapter = new CustomAdapter(solver);
		this.setBaseAdapter(lvAdapter);
		int itemCount = solver.size();
		int itemSpace = this.getMaxItemCount();

		if (itemSpace >= itemCount) {
			this.setUnDraggable();
		} else {
			this.setDraggable();
		}

		this.invalidate();
		GL.that.renderOnce();
	}

	@Override
	public void onHide() {
		log.debug("onHide()");
		SelectedCacheEventList.Remove(this);
		if (GlobalCore.isSetSelectedCache())
			Database.SetSolver(GlobalCore.getSelectedCache(), solver.getSolverString());
	}

	@Override
	public void Initial() {
		super.Initial();
		log.debug("SolverView2 => Initial()");
		this.setListPos(0, false);
		chkSlideBack();
		GL.that.renderOnce();
	}

	private final OnClickListener onItemClickListener = new OnClickListener() {

		@Override
		public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
			int selectionIndex = ((ListViewItemBase) v).getIndex();
			// GlobalCore.SelectedCache(Database.Data.Query.get(selectionIndex));
			setSelection(selectionIndex);
			// edit als default Aktion bei Click
			TabMainView.solverView2.ChangeLine();
			return true;
		}
	};

	private final OnClickListener onItemLongClickListener = new OnClickListener() {

		@Override
		public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
			int selectionIndex = ((ListViewItemBase) v).getIndex();
			setSelection(selectionIndex);

			Menu cm = getContextMenu();
			cm.Show();

			return true;
		}

	};

	public Menu getContextMenu() {
		Menu cm = new Menu("SolverViewItemContextMenu");

		cm.addOnClickListener(new OnClickListener() {

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
				switch (((MenuItem) v).getMenuItemId()) {
				case MenuID.MI_CHANGE_LINE:
					TabMainView.solverView2.ChangeLine();
					return true;
				case MenuID.MI_INSERT_LINE:
					TabMainView.solverView2.InsertLine();
					return true;
				case MenuID.MI_DELETE_LINE:
					TabMainView.solverView2.DeleteLine();
					return true;
				case MenuID.MI_SET_AS_WAYPOINT:
					TabMainView.solverView2.SetAsWaypoint();
					break;
				case MenuID.MI_SET_AS_MAPCENTER:
					TabMainView.solverView2.SetAsMapCenter();
					break;
				case MenuID.MI_ADD_MISSING_VARIABLES:
					int ii = 0;
					for (String s : solver.MissingVariables.keySet()) {
						solver.add(ii++, new SolverZeile(solver, s + "="));
					}
					for (int i = 0; i < solver.size(); i++) {
						SolverZeile zeile2 = solver.get(i);
						zeile2.setText(zeile2.getOrgText());
						zeile2.Parse();
					}
					reloadList();
					break;
				}
				return false;
			}
		});

		cm.addItem(MenuID.MI_CHANGE_LINE, "editLine");
		cm.addItem(MenuID.MI_INSERT_LINE, "addLine");
		cm.addItem(MenuID.MI_DELETE_LINE, "delLine");
		cm.addItem(MenuID.MI_SET_AS_WAYPOINT, "AddWaypoint");
		cm.addItem(MenuID.MI_SET_AS_MAPCENTER, "setMapCenter");
		cm.addItem(MenuID.MI_ADD_MISSING_VARIABLES, "addMissingVariables");
		return cm;
	}

	public class CustomAdapter implements Adapter {
		private final Solver solver;

		public CustomAdapter(Solver solver) {
			this.solver = solver;
		}

		@Override
		public int getCount() {
			if (solver == null)
				return 0;
			return solver.size();
		}

		public Object getItem(int position) {
			if (solver == null)
				return null;
			return solver.get(position);
		}

		@Override
		public ListViewItemBase getView(int position) {
			if (solver == null)
				return null;

			//FIXME cache SolverViewItem, don't create new. Bad dispose this cache

			SolverZeile solverZeile = solver.get(position);
			SolverViewItem v = new SolverViewItem(UiSizes.that.getCacheListItemRec().asFloat(), position, solverZeile);
			v.setClickable(true);
			v.setOnClickListener(onItemClickListener);
			v.setOnLongClickListener(onItemLongClickListener);

			return v;
		}

		@Override
		public float getItemSize(int position) {
			if (solver == null)
				return 0;

			// SolverZeile solverZeile = solver.get(position);
			// if (solverZeile.Solution.length() == 0) return UiSizes.getCacheListItemRec().getHeight();
			// else
			return UiSizes.that.getCacheListItemRec().getHeight();
		}

	}

	@Override
	public void SelectedCacheChanged(Cache cache, Waypoint waypoint) {
		if (cache == this.cache)
			return; // Cache hat sich nicht geändert!
		// Solver speichern
		if (this.cache != null)
			Database.SetSolver(this.cache, solver.getSolverString());
		// nächsten Cache laden
		this.cache = cache;
		intiList();
	}

	public void ChangeLine() {
		// Show Dialog
		CB_RectF rec = GL_UISizes.UI_Left.copy();
		if (GlobalCore.isTab) {
			// da der Linke Tab bei einem Tablett nicht so Breit ist wie auf einem Phone,
			// verdoppeln wir hier die Breite (sieht besser aus)
			rec.setWidth(rec.getWidth() * 2);
		}

		String SolverString = solver.get(mSelectedIndex).getOrgText();

		// SolverDialog solverDialog = new SolverDialog(rec, "SolverDialog", SolverString);
		SolverDialog2 solverDialog = new SolverDialog2(cache, solver, SolverString, true, DataType.None);

		neu = false;
		solverDialog.show(backListener);
	}

	boolean neu = false;

	final ISolverBackStringListener backListener = new ISolverBackStringListener() {

		@Override
		public void BackString(String backString) {
			SolverZeile zeile;
			if (neu) {
				zeile = new SolverZeile(solver, backString);
				solver.add(mSelectedIndex, zeile);
			} else {
				zeile = solver.get(mSelectedIndex);
				zeile.setText(backString);
			}

			for (int i = 0; i < solver.size(); i++) {
				SolverZeile zeile2 = solver.get(i);
				zeile2.setText(zeile2.getOrgText());
				zeile2.Parse();
			}

			if (!neu) {
				// wenn der letzte Eintrag geändert wurde dann soll hinter dem letzten Eintrag eine weitere neue Zeile eingefügt werden
				if (mSelectedIndex == solver.size() - 1) {
					solver.add(solver.size(), new SolverZeile(solver, ""));
					neu = true; // damit die Liste neu geladen wird
				}
			}

			reloadList();

			// Store Solver Content into Database after editing one line
			if (GlobalCore.isSetSelectedCache())
				Database.SetSolver(GlobalCore.getSelectedCache(), solver.getSolverString());
		}
	};

	public void InsertLine() {
		CB_RectF rec = GL_UISizes.UI_Left.copy();
		if (GlobalCore.isTab) {
			// da der Linke Tab bei einem Tablett nicht so Breit ist wie auf einem Phone,
			// verdoppeln wir hier die Breite (sieht besser aus)
			rec.setWidth(rec.getWidth() * 2);
		}

		String SolverString = "";

		SolverDialog2 solverDialog = new SolverDialog2(cache, solver, SolverString, true, DataType.None);

		neu = true;
		solverDialog.show(backListener);
	}

	final OnMsgBoxClickListener deleteListener = new OnMsgBoxClickListener() {
		@Override
		public boolean onClick(int which, Object data) {
			if (which == 1) {
				solver.remove(mSelectedIndex);
				solver = new Solver(solver.getSolverString());
				solver.Solve();
				solver.add(solver.size(), new SolverZeile(solver, ""));

				// Store Solver Content into Database after editing one line
				if (GlobalCore.isSetSelectedCache())
					Database.SetSolver(GlobalCore.getSelectedCache(), solver.getSolverString());

				reloadList();
				return true;
			} else
				return false;
		}
	};

	public void DeleteLine() {
		GL_MsgBox.Show("Zeile löschen?", "Solver", MessageBoxButtons.YesNo, MessageBoxIcon.Question, deleteListener);
	}

	private CoordinateGPS getSelectedCoordinateResult() {
		// Get the coordinate of the actual selected solver line
		// if one Coordinate is splitted into 2 Lines (first Line latitude, second Line longitude) then the first line has to be selected
		// if the result(s) does not include any coordinate -> return null
		try {
			SolverZeile zeile = solver.get(mSelectedIndex);
			String text = zeile.Solution;
			// Wenn in dieser Zeile eine Zuweisung enthalten ist -> diese einfach entfernen!
			if (text.contains("=")) {
				text = text.substring(text.indexOf("=") + 1);
			}
			CoordinateGPS result = new CoordinateGPS(text);
			if (!result.isValid()) {
				// Zweizeilig versuchen
				SolverZeile zeile2 = solver.get(mSelectedIndex + 1);
				String text2 = zeile2.Solution;
				if (text2.contains("="))
					text2 = text2.substring(text2.indexOf("=") + 1);
				result = new CoordinateGPS(text + " " + text2);
			}
			if (result.isValid())
				return result;
			else
				return null;
		} catch (Exception ex) {
			return null;
		}
	}

	public void SetAsWaypoint() {
		// Add new Waypoint with the selected Coordinates in the solver list
		// if one Coordinate is splitted into 2 Lines (first Line latitude, second Line longitude) then the first line has to be selected
		CoordinateGPS result = getSelectedCoordinateResult();
		if (result != null) {
			// Create New Waypoint
			Waypoint wp = new Waypoint(true);
			wp.CacheId = GlobalCore.getSelectedCache().Id;
			wp.setCoordinate(result);
			wp.Type = CacheTypes.Final;
			wp.setTitle("Final");
			wp.IsUserWaypoint = true;
			try {
				wp.setGcCode(Database.CreateFreeGcCode(GlobalCore.getSelectedCache().getGcCode()));
			} catch (Exception e) {
				return;
			}
			EditWaypoint EdWp = new EditWaypoint(wp, new IReturnListener() {
				@Override
				public void returnedWP(Waypoint waypoint) {
					if (waypoint != null) {
						// Waypoint in der DB speichern
						GlobalCore.getSelectedCache().waypoints.add(waypoint);
						WaypointDAO waypointDAO = new WaypointDAO();
						waypointDAO.WriteToDatabase(waypoint);
						WaypointListChangedEventList.Call(GlobalCore.getSelectedCache());
						GlobalCore.setSelectedWaypoint(GlobalCore.getSelectedCache(), waypoint);
					}
				}
			}, false, true);
			EdWp.show();
		}
	}

	public void SetAsMapCenter() {
		// Center Map to the actual selected Coordinates in the solver list
		// if one Coordinate is splitted into 2 Lines (first Line latitude, second Line longitude) then the first line has to be selected
		CoordinateGPS result = getSelectedCoordinateResult();
		if (result != null) {
			// Set Map Center
			TabMainView.mapView.setCenter(result);
			// Show MapView
			TabMainView.actionShowMap.Execute();
		}
	}

	@Override
	public void dispose() {

		this.setBaseAdapter(null);
		lvAdapter = null;
		solver = null;
		cache = null;
		super.dispose();
		log.debug("SolverView2 disposed");
	}
}
