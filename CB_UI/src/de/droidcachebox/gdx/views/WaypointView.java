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

import de.droidcachebox.WaypointListChangedEventList;
import de.droidcachebox.core.GroundspeakAPI;
import de.droidcachebox.database.*;
import de.droidcachebox.gdx.ActivityBase;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.activities.EditWaypoint;
import de.droidcachebox.gdx.activities.MeasureCoordinate;
import de.droidcachebox.gdx.activities.ProjectionCoordinate;
import de.droidcachebox.gdx.activities.ProjectionCoordinate.Type;
import de.droidcachebox.gdx.controls.list.Adapter;
import de.droidcachebox.gdx.controls.list.ListViewItemBase;
import de.droidcachebox.gdx.controls.list.V_ListView;
import de.droidcachebox.gdx.controls.messagebox.MessageBox;
import de.droidcachebox.gdx.controls.messagebox.MessageBoxButtons;
import de.droidcachebox.gdx.controls.messagebox.MessageBoxIcon;
import de.droidcachebox.gdx.main.Menu;
import de.droidcachebox.gdx.main.MenuItem;
import de.droidcachebox.gdx.main.ViewManager;
import de.droidcachebox.gdx.math.UiSizes;
import de.droidcachebox.locator.Coordinate;
import de.droidcachebox.locator.Locator;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.CB_List;
import de.droidcachebox.utils.Point;
import de.droidcachebox.utils.log.Log;

public class WaypointView extends V_ListView implements de.droidcachebox.SelectedCacheChangedEventListener, de.droidcachebox.WaypointListChangedEvent {
    private static final String log = "WaypointView";
    private static WaypointView that;
    private Waypoint aktWaypoint = null;
    private Cache aktCache = null;
    private CustomAdapter lvAdapter;
    private boolean createNewWaypoint = false;

    private WaypointView() {
        super(ViewManager.leftTab.getContentRec(), "WaypointView");
        setBackground(Sprites.ListBack);
        SetSelectedCache(de.droidcachebox.GlobalCore.getSelectedCache());
        de.droidcachebox.SelectedCacheChangedEventListeners.getInstance().add(this);
        de.droidcachebox.WaypointListChangedEventList.Add(this);
        this.setDisposeFlag(false);
    }

    public static WaypointView getInstance() {
        if (that == null) that = new WaypointView();
        return that;
    }

    @Override
    public void onShow() {
        SetSelectedCache(aktCache);
        chkSlideBack();

    }

    @Override
    public void onHide() {

    }

    private void SetSelectedCache(Cache cache) {

        if (aktCache != cache) {
            aktCache = de.droidcachebox.GlobalCore.getSelectedCache();
            this.setBaseAdapter(null);
            lvAdapter = new CustomAdapter(aktCache);
            this.setBaseAdapter(lvAdapter);
        }
        // aktuellen Waypoint in der List anzeigen

        Point lastAndFirst = this.getFirstAndLastVisibleIndex();

        if (aktCache == null)
            return;

        int itemCount = aktCache.waypoints == null ? 1 : aktCache.waypoints.size() + 1;
        int itemSpace = this.getMaxItemCount();

        if (itemSpace >= itemCount) {
            this.setUnDraggable();
        } else {
            this.setDraggable();
        }

        if (de.droidcachebox.GlobalCore.getSelectedWaypoint() != null) {

            if (aktWaypoint == de.droidcachebox.GlobalCore.getSelectedWaypoint()) {
                // is selected
                return;
            }

            aktWaypoint = de.droidcachebox.GlobalCore.getSelectedWaypoint();
            int id = 0;

            for (int i = 0, n = aktCache.waypoints.size(); i < n; i++) {
                Waypoint wp = aktCache.waypoints.get(i);
                id++;
                if (wp == aktWaypoint) {
                    this.setSelection(id);
                    if (this.isDraggable()) {
                        if (!(lastAndFirst.x <= id && lastAndFirst.y >= id)) {
                            this.scrollToItem(id);
                            Log.debug(log, "Scroll to:" + id);
                        }
                    }

                    break;
                }
            }
        } else {
            aktWaypoint = null;
            this.setSelection(0);
            if (this.isDraggable()) {
                if (!(lastAndFirst.x <= 0 && lastAndFirst.y >= 0)) {
                    this.scrollToItem(0);
                    Log.debug(log, "Scroll to:" + 0);
                }
            }
        }

    }

    @Override
    public void selectedCacheChanged(Cache cache, Waypoint waypoint) {
        // view must be refilled with values
        // cache and aktCache are the same objects so ==, but content has changed, thus setting aktCache to null
        aktCache = null;
        SetSelectedCache(cache);
    }

    @Override
    public void WaypointListChanged(Cache cache) {
        if (cache != aktCache)
            return;
        aktCache = null;
        SetSelectedCache(cache);
    }

    public Menu getContextMenu() {
        Menu cm = new Menu("WaypointViewContextMenuTitle");
        if (aktWaypoint != null)
            cm.addMenuItem("show", null, () -> editWP(false));
        if (aktWaypoint != null)
            cm.addMenuItem("edit", null, () -> editWP(true));
        cm.addMenuItem("AddWaypoint", null, this::addWP);
        if ((aktWaypoint != null) && (aktWaypoint.IsUserWaypoint))
            cm.addMenuItem("delete", null, this::deleteWP);
        if (aktWaypoint != null || aktCache != null)
            cm.addMenuItem("Projection", null, this::addProjection);
        MenuItem mi = cm.addMenuItem("UploadCorrectedCoordinates", null, () -> {
            GL.that.postAsync(() -> {
                if (aktCache.hasCorrectedCoordinates())
                    GroundspeakAPI.uploadCorrectedCoordinates(aktCache.getGcCode(), aktCache.Pos);
                else if (aktWaypoint.isCorrectedFinal())
                    GroundspeakAPI.uploadCorrectedCoordinates(aktCache.getGcCode(), aktWaypoint.Pos);
                if (GroundspeakAPI.APIError == 0) {
                    MessageBox.show(Translation.get("ok"), Translation.get("UploadCorrectedCoordinates"), MessageBoxButtons.OK, MessageBoxIcon.Information, null);
                } else {
                    MessageBox.show(GroundspeakAPI.LastAPIError, Translation.get("UploadCorrectedCoordinates"), MessageBoxButtons.OK, MessageBoxIcon.Information, null);
                }
            });
        });
        mi.setEnabled(aktCache.hasCorrectedCoordinates() || (aktWaypoint != null && aktWaypoint.isCorrectedFinal()));
        cm.addMenuItem("FromGps", null, this::addMeasure);

        return cm;
    }

    public void addWP() {
        createNewWaypoint = true;
        String newGcCode;
        try {
            newGcCode = Database.Data.CreateFreeGcCode(de.droidcachebox.GlobalCore.getSelectedCache().getGcCode());
        } catch (Exception e) {
            return;
        }
        Coordinate coord = de.droidcachebox.GlobalCore.getSelectedCoord();
        if (coord == null)
            coord = Locator.getInstance().getMyPosition();
        if ((coord == null) || (!coord.isValid()))
            coord = de.droidcachebox.GlobalCore.getSelectedCache().Pos;
        //Waypoint newWP = new Waypoint(newGcCode, CacheTypes.ReferencePoint, "", coord.getLatitude(), coord.getLongitude(), GlobalCore.getSelectedCache().Id, "", Translation.Get("wyptDefTitle"));
        Waypoint newWP = new Waypoint(newGcCode, CacheTypes.ReferencePoint, "", coord.getLatitude(), coord.getLongitude(), de.droidcachebox.GlobalCore.getSelectedCache().Id, "", newGcCode);

        editWP(newWP, true);

    }

    private void editWP(boolean showCoordinateDialog) {
        if (aktWaypoint != null) {
            createNewWaypoint = false;
            editWP(aktWaypoint, showCoordinateDialog);
        }
    }

    private void editWP(Waypoint wp, boolean showCoordinateDialog) {

        EditWaypoint EdWp = new EditWaypoint(wp, waypoint -> {
            if (waypoint != null) {
                if (createNewWaypoint) {

                    de.droidcachebox.GlobalCore.getSelectedCache().waypoints.add(waypoint);
                    lvAdapter = new CustomAdapter(de.droidcachebox.GlobalCore.getSelectedCache());
                    that.setBaseAdapter(lvAdapter);
                    aktWaypoint = waypoint;
                    de.droidcachebox.GlobalCore.setSelectedWaypoint(de.droidcachebox.GlobalCore.getSelectedCache(), waypoint);
                    if (waypoint.IsStart) {
                        // Es muss hier sichergestellt sein dass dieser Waypoint der einzige dieses Caches ist, der als Startpunkt
                        // definiert
                        // ist!!!
                        WaypointDAO wpd = new WaypointDAO();
                        wpd.ResetStartWaypoint(de.droidcachebox.GlobalCore.getSelectedCache(), waypoint);
                    }
                    WaypointDAO waypointDAO = new WaypointDAO();
                    waypointDAO.WriteToDatabase(waypoint);

                    aktCache = null;
                    aktWaypoint = null;

                    selectedCacheChanged(de.droidcachebox.GlobalCore.getSelectedCache(), waypoint);

                } else {
                    aktWaypoint.setTitle(waypoint.getTitle());
                    aktWaypoint.Type = waypoint.Type;
                    aktWaypoint.Pos = waypoint.Pos;
                    aktWaypoint.setDescription(waypoint.getDescription());
                    aktWaypoint.IsStart = waypoint.IsStart;
                    aktWaypoint.setClue(waypoint.getClue());

                    // set waypoint as UserWaypoint, because waypoint is changed by user
                    aktWaypoint.IsUserWaypoint = true;

                    if (waypoint.IsStart) {
                        // Es muss hier sichergestellt sein dass dieser Waypoint der einzige dieses Caches ist, der als Startpunkt
                        // definiert
                        // ist!!!
                        WaypointDAO wpd = new WaypointDAO();
                        wpd.ResetStartWaypoint(de.droidcachebox.GlobalCore.getSelectedCache(), aktWaypoint);
                    }
                    WaypointDAO waypointDAO = new WaypointDAO();
                    waypointDAO.UpdateDatabase(aktWaypoint);

                    lvAdapter = new CustomAdapter(de.droidcachebox.GlobalCore.getSelectedCache());
                    that.setBaseAdapter(lvAdapter);
                }
            }
        }, showCoordinateDialog, false);
        EdWp.show();

    }

    private void deleteWP() {
        MessageBox.show(Translation.get("?DelWP") + "\n\n[" + aktWaypoint.getTitleForGui() + "]", Translation.get("!DelWP"), MessageBoxButtons.YesNo, MessageBoxIcon.Question, (which, data) -> {
            switch (which) {
                case MessageBox.BUTTON_POSITIVE:
                    // Yes button clicked
                    Database.DeleteFromDatabase(aktWaypoint);
                    de.droidcachebox.GlobalCore.getSelectedCache().waypoints.remove(aktWaypoint);
                    de.droidcachebox.GlobalCore.setSelectedWaypoint(de.droidcachebox.GlobalCore.getSelectedCache(), null);
                    aktWaypoint = null;
                    lvAdapter = new CustomAdapter(de.droidcachebox.GlobalCore.getSelectedCache());
                    that.setBaseAdapter(lvAdapter);

                    int itemCount = lvAdapter.getCount();
                    int itemSpace = that.getMaxItemCount();

                    if (itemSpace >= itemCount) {
                        that.setUnDraggable();
                    } else {
                        that.setDraggable();
                    }

                    that.scrollToItem(0);

                    break;
                case MessageBox.BUTTON_NEGATIVE:
                    // No button clicked
                    break;
            }
            return true;
        });
    }

    private void addProjection() {
        createNewWaypoint = true;

        final Coordinate coord = (aktWaypoint != null) ? aktWaypoint.Pos : (aktCache != null) ? aktCache.Pos : Locator.getInstance().getMyPosition();
        String ProjName;

        ProjName = (aktWaypoint != null) ? aktWaypoint.getTitle() : (aktCache != null) ? aktCache.getName() : null;

        Log.debug(log, "WaypointView.addProjection()");
        Log.debug(log, "   AktWaypoint:" + ((aktWaypoint == null) ? "null" : aktWaypoint.toString()));
        Log.debug(log, "   AktCache:" + ((aktCache == null) ? "null" : aktCache.toString()));
        Log.debug(log, "   using Coord:" + coord.toString());

        ProjectionCoordinate pC = new ProjectionCoordinate(ActivityBase.activityRec(), "Projection", coord, (targetCoord, startCoord, Bearing, distance) -> {
            if (targetCoord == null || targetCoord.equals(coord))
                return;

            String newGcCode;
            try {
                newGcCode = Database.Data.CreateFreeGcCode(de.droidcachebox.GlobalCore.getSelectedCache().getGcCode());
            } catch (Exception e) {

                return;
            }
            //Waypoint newWP = new Waypoint(newGcCode, CacheTypes.ReferencePoint, "Entered Manually", targetCoord.getLatitude(), targetCoord.getLongitude(), GlobalCore.getSelectedCache().Id, "", "projiziert");
            Waypoint newWP = new Waypoint(newGcCode, CacheTypes.ReferencePoint, "Entered Manually", targetCoord.getLatitude(), targetCoord.getLongitude(), de.droidcachebox.GlobalCore.getSelectedCache().Id, "", newGcCode);
            de.droidcachebox.GlobalCore.getSelectedCache().waypoints.add(newWP);
            lvAdapter = new CustomAdapter(de.droidcachebox.GlobalCore.getSelectedCache());
            that.setBaseAdapter(lvAdapter);
            aktWaypoint = newWP;
            de.droidcachebox.GlobalCore.setSelectedWaypoint(de.droidcachebox.GlobalCore.getSelectedCache(), newWP);
            WaypointDAO waypointDAO = new WaypointDAO();
            waypointDAO.WriteToDatabase(newWP);

        }, Type.projetion, ProjName);

        pC.show();

    }

    private void addMeasure() {
        createNewWaypoint = true;

        MeasureCoordinate mC = new MeasureCoordinate(ActivityBase.activityRec(), "Projection", returnCoord -> {
            if (returnCoord == null)
                return;

            String newGcCode;
            try {
                newGcCode = Database.Data.CreateFreeGcCode(de.droidcachebox.GlobalCore.getSelectedCache().getGcCode());
            } catch (Exception e) {

                return;
            }
            //Waypoint newWP = new Waypoint(newGcCode, CacheTypes.ReferencePoint, "Measured", returnCoord.getLatitude(), returnCoord.getLongitude(), GlobalCore.getSelectedCache().Id, "", "Measured");
            Waypoint newWP = new Waypoint(newGcCode, CacheTypes.ReferencePoint, "Measured", returnCoord.getLatitude(), returnCoord.getLongitude(), de.droidcachebox.GlobalCore.getSelectedCache().Id, "", newGcCode);
            de.droidcachebox.GlobalCore.getSelectedCache().waypoints.add(newWP);

            lvAdapter = new CustomAdapter(de.droidcachebox.GlobalCore.getSelectedCache());
            that.setBaseAdapter(lvAdapter);

            aktWaypoint = newWP;
            de.droidcachebox.GlobalCore.setSelectedWaypoint(de.droidcachebox.GlobalCore.getSelectedCache(), newWP);
            WaypointDAO waypointDAO = new WaypointDAO();
            waypointDAO.WriteToDatabase(newWP);

        });

        mC.show();

    }

    @Override
    public void dispose() {
        // release all Member
        lvAdapter = null;
        aktWaypoint = null;
        aktCache = null;
        that = null;

        // release all EventHandler
        de.droidcachebox.SelectedCacheChangedEventListeners.getInstance().remove(this);
        WaypointListChangedEventList.Remove(this);
        super.dispose();
    }

    public class CustomAdapter implements Adapter {
        private Cache cache;
        private CB_List<ListViewItemBase> items;

        public CustomAdapter(Cache cache) {
            this.cache = cache;
            this.items = new CB_List<>();
            this.items.ensureCapacity(cache.waypoints.size() + 1, true);
        }

        public void setCache(Cache cache) {
            this.cache = cache;
            this.items = new CB_List<>();
            this.items.ensureCapacity(cache.waypoints.size() + 1, true);
        }

        @Override
        public int getCount() {
            if (cache != null && cache.waypoints != null)
                return cache.waypoints.size() + 1;
            else
                return 0;
        }

        Object getItem(int position) {
            if (cache != null) {
                if (position == 0)
                    return cache;
                else
                    return cache.waypoints.get(position - 1);
            } else
                return null;
        }

        @Override
        public ListViewItemBase getView(int position) {
            if (cache != null) {
                if (position == 0) {
                    // the cache
                    if (items.get(position) == null || items.get(position).isDisposed()) {
                        WaypointViewItem waypointViewItem = new WaypointViewItem(UiSizes.getInstance().getCacheListItemRec().asFloat(), position, cache, null);
                        waypointViewItem.setClickable(true);
                        waypointViewItem.addClickHandler((v, x, y, pointer, button) -> {
                            int selectionIndex = ((ListViewItemBase) v).getIndex();

                            if (selectionIndex == 0) {
                                // Cache selected
                                de.droidcachebox.GlobalCore.setSelectedCache(aktCache);
                            } else {
                                // waypoint selected
                                WaypointViewItem wpi = (WaypointViewItem) v;
                                aktWaypoint = wpi.getWaypoint();
                                de.droidcachebox.GlobalCore.setSelectedWaypoint(aktCache, aktWaypoint);
                            }

                            setSelection(selectionIndex);
                            return true;
                        });
                        waypointViewItem.setOnLongClickListener((v, x, y, pointer, button) -> {
                            int selectionIndex = ((ListViewItemBase) v).getIndex();

                            if (selectionIndex == 0) {
                                // Cache selected
                                de.droidcachebox.GlobalCore.setSelectedCache(aktCache);
                            } else {
                                // waypoint selected
                                WaypointViewItem wpi = (WaypointViewItem) v;
                                aktWaypoint = wpi.getWaypoint();
                                de.droidcachebox.GlobalCore.setSelectedWaypoint(aktCache, aktWaypoint);
                            }

                            setSelection(selectionIndex);
                            getContextMenu().show();
                            return true;
                        });
                        waypointViewItem.Add(() -> {
                            // relayout items
                            WaypointView.this.calcDefaultPosList();
                            mMustSetPos = true;
                            GL.that.renderOnce(true);
                        });
                        items.replace(waypointViewItem, position);
                    }

                    return items.get(position);
                } else {
                    if (items.get(position) == null || items.get(position).isDisposed()) {
                        Waypoint waypoint = cache.waypoints.get(position - 1);
                        WaypointViewItem waypointViewItem = new WaypointViewItem(UiSizes.getInstance().getCacheListItemRec().asFloat(), position, cache, waypoint);
                        waypointViewItem.setClickable(true);
                        waypointViewItem.addClickHandler((v, x, y, pointer, button) -> {
                            int selectionIndex = ((ListViewItemBase) v).getIndex();

                            if (selectionIndex == 0) {
                                // Cache selected
                                de.droidcachebox.GlobalCore.setSelectedCache(aktCache);
                            } else {
                                // waypoint selected
                                WaypointViewItem wpi = (WaypointViewItem) v;
                                aktWaypoint = wpi.getWaypoint();
                                de.droidcachebox.GlobalCore.setSelectedWaypoint(aktCache, aktWaypoint);
                            }

                            setSelection(selectionIndex);
                            return true;
                        });
                        waypointViewItem.setOnLongClickListener((v, x, y, pointer, button) -> {
                            int selectionIndex = ((ListViewItemBase) v).getIndex();

                            if (selectionIndex == 0) {
                                // Cache selected
                                de.droidcachebox.GlobalCore.setSelectedCache(aktCache);
                            } else {
                                // waypoint selected
                                WaypointViewItem wpi = (WaypointViewItem) v;
                                aktWaypoint = wpi.getWaypoint();
                                de.droidcachebox.GlobalCore.setSelectedWaypoint(aktCache, aktWaypoint);
                            }

                            setSelection(selectionIndex);
                            getContextMenu().show();
                            return true;
                        });
                        waypointViewItem.Add(() -> {
                            // relayout items
                            WaypointView.this.calcDefaultPosList();
                            mMustSetPos = true;
                            GL.that.renderOnce(true);
                        });
                        items.replace(waypointViewItem, position);
                    }
                    return items.get(position);
                }
            } else
                return null;
        }

        @Override
        public float getItemSize(int position) {

            if (items.get(position) == null || items.get(position).isDisposed()) {
                getView(position);
            }

            return items.get(position).getHeight();
        }

    }

}