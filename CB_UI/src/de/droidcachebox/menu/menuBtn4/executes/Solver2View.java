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
package de.droidcachebox.menu.menuBtn4.executes;

import static de.droidcachebox.menu.Action.ShowMap;

import de.droidcachebox.CacheSelectionChangedListeners;
import de.droidcachebox.GlobalCore;
import de.droidcachebox.WaypointListChangedEventList;
import de.droidcachebox.database.CachesDAO;
import de.droidcachebox.database.WaypointDAO;
import de.droidcachebox.dataclasses.Cache;
import de.droidcachebox.dataclasses.GeoCacheType;
import de.droidcachebox.dataclasses.Waypoint;
import de.droidcachebox.gdx.COLOR;
import de.droidcachebox.gdx.Fonts;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.WrapType;
import de.droidcachebox.gdx.activities.EditWaypoint;
import de.droidcachebox.gdx.activities.SolverDialog2;
import de.droidcachebox.gdx.activities.SolverDialog2.ISolverBackStringListener;
import de.droidcachebox.gdx.controls.CB_Label;
import de.droidcachebox.gdx.controls.dialogs.ButtonDialog;
import de.droidcachebox.gdx.controls.dialogs.MsgBoxButton;
import de.droidcachebox.gdx.controls.dialogs.MsgBoxIcon;
import de.droidcachebox.gdx.controls.list.Adapter;
import de.droidcachebox.gdx.controls.list.ListViewItemBackground;
import de.droidcachebox.gdx.controls.list.ListViewItemBase;
import de.droidcachebox.gdx.controls.list.V_ListView;
import de.droidcachebox.gdx.main.Menu;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.UiSizes;
import de.droidcachebox.locator.CoordinateGPS;
import de.droidcachebox.menu.Action;
import de.droidcachebox.menu.ViewManager;
import de.droidcachebox.menu.menuBtn3.ShowMap;
import de.droidcachebox.menu.menuBtn4.ShowSolver2;
import de.droidcachebox.solver.DataType;
import de.droidcachebox.solver.SolverLine;
import de.droidcachebox.solver.SolverLines;
import de.droidcachebox.utils.log.Log;

public class Solver2View extends V_ListView implements CacheSelectionChangedListeners.CacheSelectionChangedListener {
    private static final String sClass = "Solver2";
    private final ISolverBackStringListener backListener;
    private boolean neu;
    private CustomAdapter lvAdapter;
    private SolverLines solverLines;
    private Cache currentCache;
    private final CachesDAO cachesDAO;

    public Solver2View() {
        super(ViewManager.leftTab.getContentRec(), "SolverView2");
        cachesDAO = new CachesDAO();
        currentCache = null;
        neu = false;
        backListener = backString -> {
            SolverLine line;
            if (neu) {
                line = new SolverLine(solverLines, backString);
                solverLines.add(selectedIndex, line);
            } else {
                line = solverLines.get(selectedIndex);
                line.setText(backString);
            }

            for (SolverLine line2 : solverLines) {
                line2.setText(line2.getOrgText());
                line2.Parse();
            }

            if (!neu) {
                // if the last entry was changed then another new line should be inserted after the last entry
                if (selectedIndex == solverLines.size() - 1) {
                    solverLines.add(solverLines.size(), new SolverLine(solverLines, ""));
                    neu = true; // so that the list is reloaded
                }
            }

            reloadList();

            // Store Solver Content into Database after editing one line
            if (GlobalCore.isSetSelectedCache())
                cachesDAO.setSolver(GlobalCore.getSelectedCache(), solverLines.getSolverString());
        };
    }

    @Override
    public void onShow() {
        Log.debug(sClass, "onShow()");
        CacheSelectionChangedListeners.getInstance().addListener(this);

        setBackground(Sprites.ListBack);
        // Reload when
        // - cache has changed
        // - Solver 1 has changed
        if ((GlobalCore.getSelectedCache() != currentCache) || ((currentCache != null) && (currentCache.getSolver1Changed()))) {
            currentCache = GlobalCore.getSelectedCache();
            intiList();
            currentCache.setSolver1Changed(false);
        }
    }

    private void intiList() {
        if (currentCache == null) {
            solverLines = new SolverLines("", GlobalCore.getInstance());
        } else {
            currentCache = GlobalCore.getSelectedCache();
            String s = cachesDAO.getSolver(currentCache);
            if (s == null)
                s = "";
            solverLines = new SolverLines(s, GlobalCore.getInstance());
            solverLines.Solve();
            // if the solver is still empty or the last row is not empty then insert an empty row at the end
            if ((solverLines.size() == 0) || (solverLines.get(solverLines.size() - 1).getOrgText().length() > 0)) {
                solverLines.add(solverLines.size(), new SolverLine(solverLines, ""));
            }
        }

        lvAdapter = new CustomAdapter(solverLines);
        setAdapter(lvAdapter);

        int itemCount = solverLines.size();
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
        lvAdapter = new CustomAdapter(solverLines);
        setAdapter(lvAdapter);
        int itemCount = solverLines.size();
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
        CacheSelectionChangedListeners.getInstance().remove(this);
        if (GlobalCore.isSetSelectedCache())
            cachesDAO.setSolver(GlobalCore.getSelectedCache(), solverLines.getSolverString());
        ((ShowSolver2) Action.ShowSolver2.action).viewIsHiding();
    }

    @Override
    public void renderInit() {
        super.renderInit();
        Log.debug(sClass, "SolverView2 => Initial()");
        setListPos(0, false);
        chkSlideBack();
        GL.that.renderOnce();
    }

    public Menu getContextMenu() {
        Menu cm = new Menu("SolverView2ContextMenuTitle");
        cm.addMenuItem("editLine", null, this::changeLine);
        cm.addMenuItem("addLine", null, this::insertLine);
        cm.addMenuItem("delLine", null, this::deleteLine);
        cm.addMenuItem("AddWaypoint", null, this::SetAsWaypoint);
        cm.addMenuItem("setMapCenter", null, this::SetAsMapCenter);
        cm.addMenuItem("addMissingVariables", null, () -> {
            int ii = 0;
            for (String s : solverLines.MissingVariables.keySet()) {
                solverLines.add(ii++, new SolverLine(solverLines, s + "="));
            }
            for (SolverLine line2 : solverLines) {
                line2.setText(line2.getOrgText());
                line2.Parse();
            }
            reloadList();
        });
        return cm;
    }

    @Override
    public void handleCacheChanged(Cache selectedCache, Waypoint waypoint) {
        if (selectedCache == currentCache)
            return; // geoCache did not change
        // Solver save
        if (currentCache != null)
            cachesDAO.setSolver(currentCache, solverLines.getSolverString());
        // load next geoCache
        currentCache = selectedCache;
        intiList();
    }

    private void changeLine() {
        if (solverLines == null || selectedIndex < 0) return;
        SolverDialog2 solverDialog = new SolverDialog2(currentCache, solverLines, solverLines.get(selectedIndex).getOrgText(), true, DataType.None);
        neu = false;
        solverDialog.show(backListener);
    }

    private void insertLine() {
        SolverDialog2 solverDialog = new SolverDialog2(currentCache, solverLines, "", true, DataType.None);
        neu = true;
        solverDialog.show(backListener);
    }

    private void deleteLine() {
        ButtonDialog bd = new ButtonDialog("Zeile löschen?", "Solver", MsgBoxButton.YesNo, MsgBoxIcon.Question);
        bd.setButtonClickHandler((which, data) -> {
            if (which == 1) {
                solverLines.remove(selectedIndex);
                solverLines = new SolverLines(solverLines.getSolverString(), GlobalCore.getInstance());
                solverLines.Solve();
                solverLines.add(solverLines.size(), new SolverLine(solverLines, ""));

                // Store Solver Content into Database after editing one line
                if (GlobalCore.isSetSelectedCache())
                    cachesDAO.setSolver(GlobalCore.getSelectedCache(), solverLines.getSolverString());

                reloadList();
                return true;
            } else
                return false;
        });
        bd.show();
    }

    private CoordinateGPS getSelectedCoordinateResult() {
        // Get the coordinate of the actual selected solver line
        // if one Coordinate is split into 2 Lines (first Line latitude, second Line longitude) then the first line has to be selected
        // if the result(s) does not include any coordinate -> return null
        try {
            SolverLine line = solverLines.get(selectedIndex);
            String text = line.Solution;
            // If this line contains an assignment -> just remove it!
            if (text.contains("=")) {
                text = text.substring(text.indexOf("=") + 1);
            }
            CoordinateGPS result = new CoordinateGPS(text);
            if (!result.isValid()) {
                // try two lines
                SolverLine line2 = solverLines.get(selectedIndex + 1);
                String text2 = line2.Solution;
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
        // if one Coordinate is split into 2 Lines (first Line latitude, second Line longitude) then the first line has to be selected
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
                wp.setWaypointCode(WaypointDAO.getInstance().createFreeGcCode(GlobalCore.getSelectedCache().getGeoCacheCode()));
            } catch (Exception e) {
                return;
            }
            EditWaypoint EdWp = new EditWaypoint(wp, waypoint -> {
                if (waypoint != null) {
                    // save waypoint into db
                    GlobalCore.getSelectedCache().getWayPoints().add(waypoint);
                    WaypointDAO.getInstance().writeToDatabase(waypoint);
                    WaypointListChangedEventList.Call(GlobalCore.getSelectedCache());
                    GlobalCore.setSelectedWaypoint(GlobalCore.getSelectedCache(), waypoint);
                }
            }, false, true);
            EdWp.show();
        }
    }

    private void SetAsMapCenter() {
        // Center Map to the actual selected Coordinates in the solver list
        // if one Coordinate is split into 2 Lines (first Line latitude, second Line longitude) then the first line has to be selected
        CoordinateGPS result = getSelectedCoordinateResult();
        if (result != null) {
            // Set Map Center
            ((ShowMap) ShowMap.action).normalMapView.setCenter(result);
            ShowMap.action.execute();
        }
    }

    @Override
    public void dispose() {

        setAdapter(null);
        lvAdapter = null;
        solverLines = null;
        currentCache = null;
        super.dispose();
        Log.debug(sClass, "SolverView2 disposed");
    }

    public class CustomAdapter implements Adapter {
        private final SolverLines solverLines;

        CustomAdapter(SolverLines solverLines) {
            this.solverLines = solverLines;
        }

        @Override
        public int getCount() {
            if (solverLines == null)
                return 0;
            return solverLines.size();
        }

        @Override
        public ListViewItemBase getView(int position) {
            if (solverLines == null)
                return null;

            // cache SolverViewItem, don't create new. Bad dispose this cache

            SolverLine solverLine = solverLines.get(position);
            SolverViewItem solverViewItem = new SolverViewItem(UiSizes.getInstance().getCacheListItemRec().asFloat(), position, solverLine);
            solverViewItem.setClickable(true);
            solverViewItem.setClickHandler((v, x, y, pointer, button) -> {
                int selectionIndex = ((ListViewItemBase) v).getIndex();
                // GlobalCore.SelectedCache(Database.Data.cacheList.get(selectionIndex));
                setSelection(selectionIndex);
                // edit as default action on click
                changeLine();
                return true;
            });
            solverViewItem.setLongClickHandler((v, x, y, pointer, button) -> {
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
            if (solverLines == null)
                return 0;
            return UiSizes.getInstance().getCacheListItemRec().getHeight();
        }

        private class SolverViewItem extends ListViewItemBackground {
            protected boolean isPressed = false;
            protected SolverLine solverLine;
            CB_Label lblSolverLine;

            public SolverViewItem(CB_RectF rec, int Index, SolverLine solverLine) {
                super(rec, Index, "");
                this.solverLine = solverLine;
            }

            @Override
            protected void renderInit() {
                super.renderInit();
                lblSolverLine = new CB_Label(solverLine.getOrgText() + "\n" + solverLine.Solution, Fonts.getNormal(), COLOR.getFontColor(), WrapType.MULTILINE);
                lblSolverLine.setHeight(getHeight());
                setBorders(UiSizes.getInstance().getMargin(), UiSizes.getInstance().getMargin());
                addLast(lblSolverLine);
            }

            @Override
            public void dispose() {
                lblSolverLine = null;
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
