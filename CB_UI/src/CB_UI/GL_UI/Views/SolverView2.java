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

import CB_Core.CacheTypes;
import CB_Core.DAO.WaypointDAO;
import CB_Core.Database;
import CB_Core.Solver.DataTypes.DataType;
import CB_Core.Solver.Solver;
import CB_Core.Solver.SolverZeile;
import CB_Core.Types.Cache;
import CB_Core.Types.Waypoint;
import CB_Locator.CoordinateGPS;
import CB_UI.GL_UI.Activitys.EditWaypoint;
import CB_UI.GL_UI.Activitys.SolverDialog2;
import CB_UI.GL_UI.Activitys.SolverDialog2.ISolverBackStringListener;
import CB_UI.GL_UI.Main.Actions.CB_Action_ShowMap;
import CB_UI.GL_UI.Main.ViewManager;
import CB_UI.GlobalCore;
import CB_UI.SelectedCacheEvent;
import CB_UI.SelectedCacheEventList;
import CB_UI.WaypointListChangedEventList;
import CB_UI_Base.GL_UI.Controls.List.Adapter;
import CB_UI_Base.GL_UI.Controls.List.ListViewItemBase;
import CB_UI_Base.GL_UI.Controls.List.V_ListView;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBox;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxIcon;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.GL_UI.Menu.Menu;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.Math.UiSizes;
import CB_Utils.Log.Log;

public class SolverView2 extends V_ListView implements SelectedCacheEvent {
    private static final String log = "SolverView2";
    private static SolverView2 that;
    private final ISolverBackStringListener backListener;
    private boolean neu;
    private CustomAdapter lvAdapter;
    private Solver solver;
    private Cache cache;

    private SolverView2() {
        super(ViewManager.leftTab.getContentRec(), "SolverView2");
        // Log.debug(log, "Create SolverView2 => " + this.toString());
        cache = null;
        neu = false;
        backListener = backString -> {
            SolverZeile zeile;
            if (neu) {
                zeile = new SolverZeile(solver, backString);
                solver.add(mSelectedIndex, zeile);
            } else {
                zeile = solver.get(mSelectedIndex);
                zeile.setText(backString);
            }

            for (SolverZeile zeile2 : solver) {
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
        };
    }

    public static SolverView2 getInstance() {
        if (that == null) that = new SolverView2();
        return that;
    }

    @Override
    public void onShow() {
        Log.debug(log, "onShow()");
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
            solver = new Solver("", GlobalCore.getInstance());
        } else {
            this.cache = GlobalCore.getSelectedCache();
            String s = Database.GetSolver(this.cache);
            if (s == null)
                s = "";
            solver = new Solver(s, GlobalCore.getInstance());
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
        Log.debug(log, "onHide()");
        SelectedCacheEventList.Remove(this);
        if (GlobalCore.isSetSelectedCache())
            Database.SetSolver(GlobalCore.getSelectedCache(), solver.getSolverString());
    }

    @Override
    public void Initial() {
        super.Initial();
        Log.debug(log, "SolverView2 => Initial()");
        this.setListPos(0, false);
        chkSlideBack();
        GL.that.renderOnce();
    }

    public Menu getContextMenu() {
        Menu cm = new Menu("SolverView2ContextMenuTitle");
        cm.addMenuItem("editLine", null, this::ChangeLine);
        cm.addMenuItem("addLine", null, this::InsertLine);
        cm.addMenuItem("delLine", null, this::DeleteLine);
        cm.addMenuItem("AddWaypoint", null, this::SetAsWaypoint);
        cm.addMenuItem("setMapCenter", null, this::SetAsMapCenter);
        cm.addMenuItem("addMissingVariables", null, () -> {
            int ii = 0;
            for (String s : solver.MissingVariables.keySet()) {
                solver.add(ii++, new SolverZeile(solver, s + "="));
            }
            for (SolverZeile zeile2 : solver) {
                zeile2.setText(zeile2.getOrgText());
                zeile2.Parse();
            }
            reloadList();
        });
        return cm;
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

    private void ChangeLine() {
        if (solver == null || mSelectedIndex < 0) return;
        SolverDialog2 solverDialog = new SolverDialog2(cache, solver, solver.get(mSelectedIndex).getOrgText(), true, DataType.None);
        neu = false;
        solverDialog.show(backListener);
    }

    private void InsertLine() {
        SolverDialog2 solverDialog = new SolverDialog2(cache, solver, "", true, DataType.None);
        neu = true;
        solverDialog.show(backListener);
    }

    private void DeleteLine() {
        MessageBox.show("Zeile löschen?", "Solver", MessageBoxButtons.YesNo, MessageBoxIcon.Question, (which, data) -> {
            if (which == 1) {
                solver.remove(mSelectedIndex);
                solver = new Solver(solver.getSolverString(), GlobalCore.getInstance());
                solver.Solve();
                solver.add(solver.size(), new SolverZeile(solver, ""));

                // Store Solver Content into Database after editing one line
                if (GlobalCore.isSetSelectedCache())
                    Database.SetSolver(GlobalCore.getSelectedCache(), solver.getSolverString());

                reloadList();
                return true;
            } else
                return false;
        });
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

    private void SetAsWaypoint() {
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
                wp.setGcCode(Database.Data.CreateFreeGcCode(GlobalCore.getSelectedCache().getGcCode()));
            } catch (Exception e) {
                return;
            }
            EditWaypoint EdWp = new EditWaypoint(wp, waypoint -> {
                if (waypoint != null) {
                    // Waypoint in der DB speichern
                    GlobalCore.getSelectedCache().waypoints.add(waypoint);
                    WaypointDAO waypointDAO = new WaypointDAO();
                    waypointDAO.WriteToDatabase(waypoint);
                    WaypointListChangedEventList.Call(GlobalCore.getSelectedCache());
                    GlobalCore.setSelectedWaypoint(GlobalCore.getSelectedCache(), waypoint);
                }
            }, false, true);
            EdWp.show();
        }
    }

    private void SetAsMapCenter() {
        // Center Map to the actual selected Coordinates in the solver list
        // if one Coordinate is splitted into 2 Lines (first Line latitude, second Line longitude) then the first line has to be selected
        CoordinateGPS result = getSelectedCoordinateResult();
        if (result != null) {
            // Set Map Center
            CB_Action_ShowMap.getInstance().normalMapView.setCenter(result);
            CB_Action_ShowMap.getInstance().Execute();
        }
    }

    @Override
    public void dispose() {

        this.setBaseAdapter(null);
        lvAdapter = null;
        solver = null;
        cache = null;
        super.dispose();
        Log.debug(log, "SolverView2 disposed");
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

        @Override
        public ListViewItemBase getView(int position) {
            if (solver == null)
                return null;

            //FIXME cache SolverViewItem, don't create new. Bad dispose this cache

            SolverZeile solverZeile = solver.get(position);
            SolverViewItem solverViewItem = new SolverViewItem(UiSizes.that.getCacheListItemRec().asFloat(), position, solverZeile);
            solverViewItem.setClickable(true);
            solverViewItem.setOnClickListener((v, x, y, pointer, button) -> {
                int selectionIndex = ((ListViewItemBase) v).getIndex();
                // GlobalCore.SelectedCache(Database.Data.cacheList.get(selectionIndex));
                setSelection(selectionIndex);
                // edit als default Aktion bei Click
                ChangeLine();
                return true;
            });
            solverViewItem.setOnLongClickListener((v, x, y, pointer, button) -> {
                int selectionIndex = ((ListViewItemBase) v).getIndex();
                setSelection(selectionIndex);

                Menu cm = getContextMenu();
                cm.show();

                return true;
            });

            return solverViewItem;
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
}
