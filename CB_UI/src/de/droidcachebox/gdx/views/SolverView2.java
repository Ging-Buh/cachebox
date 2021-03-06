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
package de.droidcachebox.gdx.views;

import de.droidcachebox.CacheSelectionChangedListeners;
import de.droidcachebox.GlobalCore;
import de.droidcachebox.WaypointListChangedEventList;
import de.droidcachebox.WrapType;
import de.droidcachebox.database.*;
import de.droidcachebox.gdx.COLOR;
import de.droidcachebox.gdx.Fonts;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.activities.EditWaypoint;
import de.droidcachebox.gdx.activities.SolverDialog2;
import de.droidcachebox.gdx.activities.SolverDialog2.ISolverBackStringListener;
import de.droidcachebox.gdx.controls.CB_Label;
import de.droidcachebox.gdx.controls.list.Adapter;
import de.droidcachebox.gdx.controls.list.ListViewItemBackground;
import de.droidcachebox.gdx.controls.list.ListViewItemBase;
import de.droidcachebox.gdx.controls.list.V_ListView;
import de.droidcachebox.gdx.controls.messagebox.MessageBox;
import de.droidcachebox.gdx.controls.messagebox.MessageBoxButton;
import de.droidcachebox.gdx.controls.messagebox.MessageBoxIcon;
import de.droidcachebox.gdx.main.Menu;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.UiSizes;
import de.droidcachebox.locator.CoordinateGPS;
import de.droidcachebox.menu.ViewManager;
import de.droidcachebox.menu.menuBtn3.ShowMap;
import de.droidcachebox.solver.DataType;
import de.droidcachebox.solver.Solver;
import de.droidcachebox.solver.SolverZeile;
import de.droidcachebox.utils.log.Log;

public class SolverView2 extends V_ListView implements CacheSelectionChangedListeners.CacheSelectionChangedListener {
    private static final String log = "SolverView2";
    private static SolverView2 that;
    private final ISolverBackStringListener backListener;
    private boolean neu;
    private CustomAdapter lvAdapter;
    private Solver solver;
    private Cache cache;

    private SolverView2() {
        super(ViewManager.leftTab.getContentRec(), "SolverView2");
        // Log.debug(log, "Create SolverView2 => " + toString());
        cache = null;
        neu = false;
        backListener = backString -> {
            SolverZeile zeile;
            if (neu) {
                zeile = new SolverZeile(solver, backString);
                solver.add(selectedIndex, zeile);
            } else {
                zeile = solver.get(selectedIndex);
                zeile.setText(backString);
            }

            for (SolverZeile zeile2 : solver) {
                zeile2.setText(zeile2.getOrgText());
                zeile2.Parse();
            }

            if (!neu) {
                // wenn der letzte Eintrag geändert wurde dann soll hinter dem letzten Eintrag eine weitere neue Zeile eingefügt werden
                if (selectedIndex == solver.size() - 1) {
                    solver.add(solver.size(), new SolverZeile(solver, ""));
                    neu = true; // damit die Liste neu geladen wird
                }
            }

            reloadList();

            // Store Solver Content into Database after editing one line
            if (GlobalCore.isSetSelectedCache())
                Database.setSolver(GlobalCore.getSelectedCache(), solver.getSolverString());
        };
    }

    public static SolverView2 getInstance() {
        if (that == null) that = new SolverView2();
        return that;
    }

    @Override
    public void onShow() {
        Log.debug(log, "onShow()");
        CacheSelectionChangedListeners.getInstance().addListener(this);

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
            cache = GlobalCore.getSelectedCache();
            String s = Database.getSolver(cache);
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
        setAdapter(lvAdapter);

        int itemCount = solver.size();
        int itemSpace = getMaxNumberOfVisibleItems();

        if (itemSpace >= itemCount) {
            setUnDraggable();
        } else {
            setDraggable();
        }

        setSelection(0);
        setListPos(0, false);

        invalidate();
        GL.that.renderOnce();
    }

    private void reloadList() {
        lvAdapter = new CustomAdapter(solver);
        setAdapter(lvAdapter);
        int itemCount = solver.size();
        int itemSpace = getMaxNumberOfVisibleItems();

        if (itemSpace >= itemCount) {
            setUnDraggable();
        } else {
            setDraggable();
        }

        invalidate();
        GL.that.renderOnce();
    }

    @Override
    public void onHide() {
        Log.debug(log, "onHide()");
        CacheSelectionChangedListeners.getInstance().remove(this);
        if (GlobalCore.isSetSelectedCache())
            Database.setSolver(GlobalCore.getSelectedCache(), solver.getSolverString());
    }

    @Override
    public void initialize() {
        super.initialize();
        Log.debug(log, "SolverView2 => Initial()");
        setListPos(0, false);
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
    public void handleCacheChanged(Cache cache, Waypoint waypoint) {
        if (cache == this.cache)
            return; // Cache hat sich nicht geändert!
        // Solver speichern
        if (this.cache != null)
            Database.setSolver(this.cache, solver.getSolverString());
        // nächsten Cache laden
        this.cache = cache;
        intiList();
    }

    private void ChangeLine() {
        if (solver == null || selectedIndex < 0) return;
        SolverDialog2 solverDialog = new SolverDialog2(cache, solver, solver.get(selectedIndex).getOrgText(), true, DataType.None);
        neu = false;
        solverDialog.show(backListener);
    }

    private void InsertLine() {
        SolverDialog2 solverDialog = new SolverDialog2(cache, solver, "", true, DataType.None);
        neu = true;
        solverDialog.show(backListener);
    }

    private void DeleteLine() {
        MessageBox.show("Zeile löschen?", "Solver", MessageBoxButton.YesNo, MessageBoxIcon.Question, (which, data) -> {
            if (which == 1) {
                solver.remove(selectedIndex);
                solver = new Solver(solver.getSolverString(), GlobalCore.getInstance());
                solver.Solve();
                solver.add(solver.size(), new SolverZeile(solver, ""));

                // Store Solver Content into Database after editing one line
                if (GlobalCore.isSetSelectedCache())
                    Database.setSolver(GlobalCore.getSelectedCache(), solver.getSolverString());

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
            SolverZeile zeile = solver.get(selectedIndex);
            String text = zeile.Solution;
            // Wenn in dieser Zeile eine Zuweisung enthalten ist -> diese einfach entfernen!
            if (text.contains("=")) {
                text = text.substring(text.indexOf("=") + 1);
            }
            CoordinateGPS result = new CoordinateGPS(text);
            if (!result.isValid()) {
                // Zweizeilig versuchen
                SolverZeile zeile2 = solver.get(selectedIndex + 1);
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
            wp.geoCacheId = GlobalCore.getSelectedCache().generatedId;
            wp.setCoordinate(result);
            wp.waypointType = GeoCacheType.Final;
            wp.setTitle("Final");
            wp.isUserWaypoint = true;
            try {
                wp.setGcCode(Database.Data.createFreeGcCode(GlobalCore.getSelectedCache().getGeoCacheCode()));
            } catch (Exception e) {
                return;
            }
            EditWaypoint EdWp = new EditWaypoint(wp, waypoint -> {
                if (waypoint != null) {
                    // Waypoint in der DB speichern
                    GlobalCore.getSelectedCache().getWayPoints().add(waypoint);
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
            ShowMap.getInstance().normalMapView.setCenter(result);
            ShowMap.getInstance().execute();
        }
    }

    @Override
    public void dispose() {

        setAdapter(null);
        lvAdapter = null;
        solver = null;
        cache = null;
        super.dispose();
        Log.debug(log, "SolverView2 disposed");
    }

    public class CustomAdapter implements Adapter {
        private final Solver solver;

        CustomAdapter(Solver solver) {
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

            // cache SolverViewItem, don't create new. Bad dispose this cache

            SolverZeile solverZeile = solver.get(position);
            SolverViewItem solverViewItem = new SolverViewItem(UiSizes.getInstance().getCacheListItemRec().asFloat(), position, solverZeile);
            solverViewItem.setClickable(true);
            solverViewItem.setClickHandler((v, x, y, pointer, button) -> {
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
            return UiSizes.getInstance().getCacheListItemRec().getHeight();
        }

        private class SolverViewItem extends ListViewItemBackground {
            protected boolean isPressed = false;
            protected SolverZeile solverZeile;
            CB_Label lblSolverZeile;

            public SolverViewItem(CB_RectF rec, int Index, SolverZeile solverZeile) {
                super(rec, Index, "");
                this.solverZeile = solverZeile;
            }

            @Override
            protected void initialize() {
                super.initialize();
                lblSolverZeile = new CB_Label(solverZeile.getOrgText() + "\n" + solverZeile.Solution, Fonts.getNormal(), COLOR.getFontColor(), WrapType.MULTILINE);
                lblSolverZeile.setHeight(getHeight()); // todo ob das immer passt?
                setBorders(UiSizes.getInstance().getMargin(), UiSizes.getInstance().getMargin());
                addLast(lblSolverZeile);
            }

            @Override
            public void dispose() {
                lblSolverZeile = null;
            }

            @Override
            public boolean onTouchDown(int x, int y, int pointer, int button) {

                isPressed = true;

                return false;
            }

            @Override
            public boolean onTouchDragged(int x, int y, int pointer, boolean KineticPan) {
                isPressed = false;

                return false;
            }

            @Override
            public boolean onTouchUp(int x, int y, int pointer, int button) {
                isPressed = false;

                return false;
            }

        }
    }
}
