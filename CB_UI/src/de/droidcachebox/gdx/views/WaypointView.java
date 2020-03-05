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

import de.droidcachebox.*;
import de.droidcachebox.core.GroundspeakAPI;
import de.droidcachebox.database.*;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.activities.EditWaypoint;
import de.droidcachebox.gdx.activities.MeasureCoordinate;
import de.droidcachebox.gdx.activities.ProjectionCoordinate;
import de.droidcachebox.gdx.controls.list.Adapter;
import de.droidcachebox.gdx.controls.list.ListViewItemBase;
import de.droidcachebox.gdx.controls.list.V_ListView;
import de.droidcachebox.gdx.controls.messagebox.MessageBox;
import de.droidcachebox.gdx.controls.messagebox.MessageBoxButton;
import de.droidcachebox.gdx.controls.messagebox.MessageBoxIcon;
import de.droidcachebox.gdx.main.Menu;
import de.droidcachebox.gdx.math.UiSizes;
import de.droidcachebox.locator.Coordinate;
import de.droidcachebox.locator.Locator;
import de.droidcachebox.menu.ViewManager;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.CB_List;
import de.droidcachebox.utils.Point;
import de.droidcachebox.utils.log.Log;

public class WaypointView extends V_ListView implements SelectedCacheChangedEventListener, WaypointListChangedEvent {
    private static final String log = "WaypointView";
    private static WaypointView that;
    private Waypoint currentWaypoint = null;
    private Cache currentCache = null;
    private WayPointListViewAdapter wayPointListViewAdapter;
    private boolean createNewWaypoint = false;

    private WaypointView() {
        super(ViewManager.leftTab.getContentRec(), "WaypointView");
        setBackground(Sprites.ListBack);
        setSelectedCache(GlobalCore.getSelectedCache());
        SelectedCacheChangedEventListeners.getInstance().add(this);
        WaypointListChangedEventList.Add(this);
        setDisposeFlag(false);
    }

    public static WaypointView getInstance() {
        if (that == null) that = new WaypointView();
        return that;
    }

    @Override
    public void onShow() {
        setSelectedCache(currentCache);
        chkSlideBack();

    }

    @Override
    public void onHide() {

    }

    private void setSelectedCache(Cache cache) {

        if (currentCache != cache) {
            currentCache = GlobalCore.getSelectedCache();
            setAdapter(null);
            wayPointListViewAdapter = new WayPointListViewAdapter(currentCache);
            setAdapter(wayPointListViewAdapter);
        }
        // aktuellen Waypoint in der List anzeigen

        Point lastAndFirst = getFirstAndLastVisibleIndex();

        if (currentCache == null)
            return;

        if (getMaxNumberOfVisibleItems() >= (currentCache.getWayPoints() == null ? 1 : currentCache.getWayPoints().size() + 1))
            setUnDraggable();
        else
            setDraggable();

        if (GlobalCore.getSelectedWayPoint() != null) {

            if (currentWaypoint == GlobalCore.getSelectedWayPoint()) {
                // is selected
                return;
            }

            currentWaypoint = GlobalCore.getSelectedWayPoint();
            int id = 0;

            for (int i = 0, n = currentCache.getWayPoints().size(); i < n; i++) {
                Waypoint wp = currentCache.getWayPoints().get(i);
                id++;
                if (wp == currentWaypoint) {
                    setSelection(id);
                    if (isDraggable()) {
                        if (!(lastAndFirst.x <= id && lastAndFirst.y >= id)) {
                            scrollToItem(id);
                            Log.debug(log, "Scroll to:" + id);
                        }
                    }

                    break;
                }
            }
        } else {
            currentWaypoint = null;
            setSelection(0);
            if (isDraggable()) {
                if (!(lastAndFirst.x <= 0 && lastAndFirst.y >= 0)) {
                    scrollToItem(0);
                    Log.debug(log, "Scroll to:" + 0);
                }
            }
        }

    }

    @Override
    public void selectedCacheChanged(Cache cache, Waypoint waypoint) {
        // view must be refilled with values
        // cache and aktCache are the same objects so ==, but content has changed, thus setting aktCache to null
        currentCache = null;
        setSelectedCache(cache);
    }

    @Override
    public void WaypointListChanged(Cache cache) {
        if (cache != currentCache)
            return;
        currentCache = null;
        setSelectedCache(cache);
    }

    public Menu getContextMenu() {
        Menu cm = new Menu("WaypointViewContextMenuTitle");
        if (currentWaypoint != null) {
            cm.addMenuItem("show", null, () -> editWP(false));
            cm.addMenuItem("edit", null, () -> editWP(true));
            if (currentWaypoint.isUserWaypoint)
                cm.addMenuItem("delete", null, this::deleteWP);
        }
        cm.addMenuItem("AddWaypoint", null, this::addWP);
        cm.addMenuItem("Projection", null, this::addProjection);
        cm.addMenuItem("FromGps", null, this::addMeasure);
        if (currentCache.hasCorrectedCoordinates() || (currentWaypoint != null && currentWaypoint.isCorrectedFinal())) {
            cm.addMenuItem("UploadCorrectedCoordinates", null, () -> GL.that.postAsync(() -> {
                if (currentCache.hasCorrectedCoordinates())
                    GroundspeakAPI.uploadCorrectedCoordinates(currentCache.getGeoCacheCode(), currentCache.getCoordinate());
                else if (currentWaypoint.isCorrectedFinal())
                    GroundspeakAPI.uploadCorrectedCoordinates(currentCache.getGeoCacheCode(), currentWaypoint.getCoordinate());
                if (GroundspeakAPI.APIError == 0) {
                    MessageBox.show(Translation.get("ok"), Translation.get("UploadCorrectedCoordinates"), MessageBoxButton.OK, MessageBoxIcon.Information, null);
                } else {
                    MessageBox.show(GroundspeakAPI.LastAPIError, Translation.get("UploadCorrectedCoordinates"), MessageBoxButton.OK, MessageBoxIcon.Information, null);
                }
            }));
        }

        return cm;
    }

    public void addWP() {
        createNewWaypoint = true;
        String newGcCode;
        try {
            newGcCode = Database.Data.createFreeGcCode(GlobalCore.getSelectedCache().getGeoCacheCode());
        } catch (Exception e) {
            return;
        }
        Coordinate coord = GlobalCore.getSelectedCoordinate();
        if (coord == null)
            coord = Locator.getInstance().getMyPosition();
        if ((coord == null) || (!coord.isValid()))
            coord = GlobalCore.getSelectedCache().getCoordinate();
        //Waypoint newWP = new Waypoint(newGcCode, CacheTypes.ReferencePoint, "", coord.getLatitude(), coord.getLongitude(), GlobalCore.getSelectedCache().Id, "", Translation.Get("wyptDefTitle"));
        Waypoint newWP = new Waypoint(newGcCode, GeoCacheType.ReferencePoint, "", coord.getLatitude(), coord.getLongitude(), GlobalCore.getSelectedCache().generatedId, "", newGcCode);

        editWP(newWP, true);

    }

    private void editWP(boolean showCoordinateDialog) {
        if (currentWaypoint != null) {
            createNewWaypoint = false;
            editWP(currentWaypoint, showCoordinateDialog);
        }
    }

    private void editWP(Waypoint wp, boolean showCoordinateDialog) {

        EditWaypoint EdWp = new EditWaypoint(wp, waypoint -> {
            if (waypoint != null) {
                if (createNewWaypoint) {

                    GlobalCore.getSelectedCache().getWayPoints().add(waypoint);
                    wayPointListViewAdapter = new WayPointListViewAdapter(GlobalCore.getSelectedCache());
                    that.setAdapter(wayPointListViewAdapter);
                    currentWaypoint = waypoint;
                    GlobalCore.setSelectedWaypoint(GlobalCore.getSelectedCache(), waypoint);
                    if (waypoint.isStartWaypoint) {
                        // Es muss hier sichergestellt sein dass dieser Waypoint der einzige dieses Caches ist, der als Startpunkt
                        // definiert
                        // ist!!!
                        WaypointDAO wpd = new WaypointDAO();
                        wpd.ResetStartWaypoint(GlobalCore.getSelectedCache(), waypoint);
                    }
                    WaypointDAO waypointDAO = new WaypointDAO();
                    waypointDAO.WriteToDatabase(waypoint);

                    currentCache = null;
                    currentWaypoint = null;

                    selectedCacheChanged(GlobalCore.getSelectedCache(), waypoint);

                } else {
                    currentWaypoint.setTitle(waypoint.getTitle());
                    currentWaypoint.waypointType = waypoint.waypointType;
                    currentWaypoint.setCoordinate(waypoint.getCoordinate());
                    currentWaypoint.setDescription(waypoint.getDescription());
                    currentWaypoint.isStartWaypoint = waypoint.isStartWaypoint;
                    currentWaypoint.setClue(waypoint.getClue());

                    // set waypoint as UserWaypoint, because waypoint is changed by user
                    currentWaypoint.isUserWaypoint = true;

                    if (waypoint.isStartWaypoint) {
                        // Es muss hier sichergestellt sein dass dieser Waypoint der einzige dieses Caches ist, der als Startpunkt
                        // definiert
                        // ist!!!
                        WaypointDAO wpd = new WaypointDAO();
                        wpd.ResetStartWaypoint(GlobalCore.getSelectedCache(), currentWaypoint);
                    }
                    WaypointDAO waypointDAO = new WaypointDAO();
                    waypointDAO.UpdateDatabase(currentWaypoint);

                    wayPointListViewAdapter = new WayPointListViewAdapter(GlobalCore.getSelectedCache());
                    that.setAdapter(wayPointListViewAdapter);
                }
            }
        }, showCoordinateDialog, false);
        EdWp.show();

    }

    private void deleteWP() {
        MessageBox.show(Translation.get("?DelWP") + "\n\n[" + currentWaypoint.getTitleForGui() + "]", Translation.get("!DelWP"), MessageBoxButton.YesNo, MessageBoxIcon.Question, (which, data) -> {
            switch (which) {
                case MessageBox.BTN_LEFT_POSITIVE:
                    // Yes button clicked
                    Database.deleteFromDatabase(currentWaypoint);
                    GlobalCore.getSelectedCache().getWayPoints().remove(currentWaypoint);
                    GlobalCore.setSelectedWaypoint(GlobalCore.getSelectedCache(), null);
                    currentWaypoint = null;
                    wayPointListViewAdapter = new WayPointListViewAdapter(GlobalCore.getSelectedCache());
                    that.setAdapter(wayPointListViewAdapter);

                    int itemCount = wayPointListViewAdapter.getCount();
                    int itemSpace = that.getMaxNumberOfVisibleItems();

                    if (itemSpace >= itemCount) {
                        that.setUnDraggable();
                    } else {
                        that.setDraggable();
                    }

                    that.scrollToItem(0);

                    break;
                case MessageBox.BTN_RIGHT_NEGATIVE:
                    // No button clicked
                    break;
            }
            return true;
        });
    }

    private void addProjection() {
        createNewWaypoint = true;

        final Coordinate coord;
        String projName;
        if (currentWaypoint == null) {
            if (currentCache == null) {
                coord = Locator.getInstance().getMyPosition();
                projName = Translation.get("FromGps");
            } else {
                coord = currentCache.getCoordinate();
                projName = currentCache.getGeoCacheName();
            }
        } else {
            if (!currentWaypoint.getCoordinate().isValid() || currentWaypoint.getCoordinate().isZero()) {
                coord = Locator.getInstance().getMyPosition();
                projName = Translation.get("FromGps");
            }
            else {
                coord = currentWaypoint.getCoordinate();
                projName = currentWaypoint.getTitle();
            }
        }

        new ProjectionCoordinate("Projection", coord, (targetCoord, startCoord, Bearing, distance) -> {
            if (targetCoord == null || targetCoord.equals(coord))
                return;

            String newGcCode;
            try {
                newGcCode = Database.Data.createFreeGcCode(GlobalCore.getSelectedCache().getGeoCacheCode());
            } catch (Exception e) {

                return;
            }
            //Waypoint newWP = new Waypoint(newGcCode, CacheTypes.ReferencePoint, "Entered Manually", targetCoord.getLatitude(), targetCoord.getLongitude(), GlobalCore.getSelectedCache().Id, "", "projiziert");
            Waypoint newWP = new Waypoint(newGcCode, GeoCacheType.ReferencePoint, "Entered Manually", targetCoord.getLatitude(), targetCoord.getLongitude(), GlobalCore.getSelectedCache().generatedId, "", newGcCode);
            GlobalCore.getSelectedCache().getWayPoints().add(newWP);
            wayPointListViewAdapter = new WayPointListViewAdapter(GlobalCore.getSelectedCache());
            that.setAdapter(wayPointListViewAdapter);
            currentWaypoint = newWP;
            GlobalCore.setSelectedWaypoint(GlobalCore.getSelectedCache(), newWP);
            WaypointDAO waypointDAO = new WaypointDAO();
            waypointDAO.WriteToDatabase(newWP);

        }, ProjectionCoordinate.ProjectionType.projection, projName).show();

    }

    private void addMeasure() {
        createNewWaypoint = true;

        MeasureCoordinate mC = new MeasureCoordinate("Projection", returnCoord -> {
            if (returnCoord == null)
                return;

            String newGcCode;
            try {
                newGcCode = Database.Data.createFreeGcCode(GlobalCore.getSelectedCache().getGeoCacheCode());
            } catch (Exception e) {

                return;
            }
            //Waypoint newWP = new Waypoint(newGcCode, CacheTypes.ReferencePoint, "Measured", returnCoordinate.getLatitude(), returnCoordinate.getLongitude(), GlobalCore.getSelectedCache().Id, "", "Measured");
            Waypoint newWP = new Waypoint(newGcCode, GeoCacheType.ReferencePoint, "Measured", returnCoord.getLatitude(), returnCoord.getLongitude(), GlobalCore.getSelectedCache().generatedId, "", newGcCode);
            GlobalCore.getSelectedCache().getWayPoints().add(newWP);

            wayPointListViewAdapter = new WayPointListViewAdapter(GlobalCore.getSelectedCache());
            setAdapter(wayPointListViewAdapter);

            currentWaypoint = newWP;
            GlobalCore.setSelectedWaypoint(GlobalCore.getSelectedCache(), newWP);
            WaypointDAO waypointDAO = new WaypointDAO();
            waypointDAO.WriteToDatabase(newWP);

        });

        mC.show();

    }

    @Override
    public void dispose() {
        // release all Member
        wayPointListViewAdapter = null;
        currentWaypoint = null;
        currentCache = null;
        that = null;

        // release all EventHandler
        SelectedCacheChangedEventListeners.getInstance().remove(this);
        WaypointListChangedEventList.Remove(this);
        super.dispose();
    }

    public class WayPointListViewAdapter implements Adapter {
        private Cache cache;
        private CB_List<ListViewItemBase> wayPoints;

        WayPointListViewAdapter(Cache cache) {
            this.cache = cache;
            wayPoints = new CB_List<>();
            wayPoints.ensureCapacity(cache.getWayPoints().size() + 1, true);
        }

        public void setCache(Cache cache) {
            this.cache = cache;
            wayPoints = new CB_List<>();
            wayPoints.ensureCapacity(cache.getWayPoints().size() + 1, true);
        }

        @Override
        public int getCount() {
            if (cache != null && cache.getWayPoints() != null)
                return cache.getWayPoints().size() + 1;
            else
                return 0;
        }

        @Override
        public ListViewItemBase getView(int position) {
            if (cache != null) {
                if (position == 0) {
                    // the cache
                    if (wayPoints.get(position) == null || wayPoints.get(position).isDisposed()) {
                        WaypointViewItem waypointViewItem = new WaypointViewItem(UiSizes.getInstance().getCacheListItemRec().asFloat(), position, cache, null);
                        waypointViewItem.setClickable(true);
                        waypointViewItem.setClickHandler((v, x, y, pointer, button) -> {
                            int selectionIndex = ((ListViewItemBase) v).getIndex();

                            if (selectionIndex == 0) {
                                // Cache selected
                                GlobalCore.setSelectedCache(currentCache);
                            } else {
                                // waypoint selected
                                currentWaypoint = ((WaypointViewItem) v).getWaypoint();
                                GlobalCore.setSelectedWaypoint(currentCache, currentWaypoint);
                            }

                            setSelection(selectionIndex);
                            return true;
                        });
                        waypointViewItem.setOnLongClickListener((v, x, y, pointer, button) -> {
                            int selectionIndex = ((ListViewItemBase) v).getIndex();

                            if (selectionIndex == 0) {
                                // Cache selected
                                GlobalCore.setSelectedCache(currentCache);
                            } else {
                                // waypoint selected
                                WaypointViewItem wpi = (WaypointViewItem) v;
                                currentWaypoint = wpi.getWaypoint();
                                GlobalCore.setSelectedWaypoint(currentCache, currentWaypoint);
                            }

                            setSelection(selectionIndex);
                            getContextMenu().show();
                            return true;
                        });
                        waypointViewItem.addListener(() -> {
                            // relayout items
                            WaypointView.this.calculateItemPosition();
                            mMustSetPos = true;
                            GL.that.renderOnce(true);
                        });
                        wayPoints.replace(waypointViewItem, position);
                    }

                } else {
                    if (wayPoints.get(position) == null || wayPoints.get(position).isDisposed()) {
                        Waypoint waypoint = cache.getWayPoints().get(position - 1);
                        WaypointViewItem waypointViewItem = new WaypointViewItem(UiSizes.getInstance().getCacheListItemRec().asFloat(), position, cache, waypoint);
                        waypointViewItem.setClickable(true);
                        waypointViewItem.setClickHandler((v, x, y, pointer, button) -> {
                            int selectionIndex = ((ListViewItemBase) v).getIndex();

                            if (selectionIndex == 0) {
                                // Cache selected
                                GlobalCore.setSelectedCache(currentCache);
                            } else {
                                // waypoint selected
                                WaypointViewItem wpi = (WaypointViewItem) v;
                                currentWaypoint = wpi.getWaypoint();
                                GlobalCore.setSelectedWaypoint(currentCache, currentWaypoint);
                            }

                            setSelection(selectionIndex);
                            return true;
                        });
                        waypointViewItem.setOnLongClickListener((v, x, y, pointer, button) -> {
                            int selectionIndex = ((ListViewItemBase) v).getIndex();

                            if (selectionIndex == 0) {
                                // Cache selected
                                GlobalCore.setSelectedCache(currentCache);
                            } else {
                                // waypoint selected
                                WaypointViewItem wpi = (WaypointViewItem) v;
                                currentWaypoint = wpi.getWaypoint();
                                GlobalCore.setSelectedWaypoint(currentCache, currentWaypoint);
                            }

                            setSelection(selectionIndex);
                            getContextMenu().show();
                            return true;
                        });
                        waypointViewItem.addListener(() -> {
                            // relayout items
                            WaypointView.this.calculateItemPosition();
                            mMustSetPos = true;
                            GL.that.renderOnce(true);
                        });
                        wayPoints.replace(waypointViewItem, position);
                    }
                }
                return wayPoints.get(position);
            } else
                return null;
        }

        @Override
        public float getItemSize(int position) {

            if (wayPoints.get(position) == null || wayPoints.get(position).isDisposed()) {
                getView(position);
            }

            return wayPoints.get(position).getHeight();
        }

    }

}
