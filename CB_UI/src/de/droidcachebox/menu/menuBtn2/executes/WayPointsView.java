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
package de.droidcachebox.menu.menuBtn2.executes;

import static de.droidcachebox.core.GroundspeakAPI.isAccessTokenInvalid;

import de.droidcachebox.CacheSelectionChangedListeners;
import de.droidcachebox.GlobalCore;
import de.droidcachebox.WaypointListChangedEvent;
import de.droidcachebox.WaypointListChangedEventList;
import de.droidcachebox.core.GroundspeakAPI;
import de.droidcachebox.database.WaypointDAO;
import de.droidcachebox.dataclasses.Cache;
import de.droidcachebox.dataclasses.GeoCacheType;
import de.droidcachebox.dataclasses.Waypoint;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.activities.EditWaypoint;
import de.droidcachebox.gdx.activities.MeasureCoordinate;
import de.droidcachebox.gdx.activities.ProjectionCoordinate;
import de.droidcachebox.gdx.controls.dialogs.ButtonDialog;
import de.droidcachebox.gdx.controls.dialogs.MsgBoxButton;
import de.droidcachebox.gdx.controls.dialogs.MsgBoxIcon;
import de.droidcachebox.gdx.controls.list.Adapter;
import de.droidcachebox.gdx.controls.list.ListViewItemBase;
import de.droidcachebox.gdx.controls.list.V_ListView;
import de.droidcachebox.gdx.main.Menu;
import de.droidcachebox.gdx.math.UiSizes;
import de.droidcachebox.gdx.views.WaypointViewItem;
import de.droidcachebox.locator.Coordinate;
import de.droidcachebox.locator.Locator;
import de.droidcachebox.menu.Action;
import de.droidcachebox.menu.ViewManager;
import de.droidcachebox.menu.menuBtn2.ShowWayPoints;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.CB_List;
import de.droidcachebox.utils.Point;
import de.droidcachebox.utils.log.Log;

public class WayPointsView extends V_ListView implements CacheSelectionChangedListeners.CacheSelectionChangedListener, WaypointListChangedEvent {
    private static final String sClass = "WaypointsView";
    private Waypoint currentWaypoint = null;
    private Cache currentCache = null;
    private WayPointListViewAdapter wayPointListViewAdapter;
    private boolean createNewWaypoint = false;

    public WayPointsView() {
        super(ViewManager.leftTab.getContentRec(), "WaypointView");
        setBackground(Sprites.ListBack);
        setDisposeFlag(false);
    }

    @Override
    public void onShow() {
        try {
            setSelectedCache(GlobalCore.getSelectedCache());
            chkSlideBack();
            CacheSelectionChangedListeners.getInstance().addListener(this);
            WaypointListChangedEventList.Add(this);
        } catch (Exception ex) {
            Log.err(sClass, "onShow", ex);
        }
    }

    @Override
    public void onHide() {
        CacheSelectionChangedListeners.getInstance().remove(this);
        WaypointListChangedEventList.remove(this);
        ((ShowWayPoints) Action.ShowWayPoints.action).viewIsHiding();
    }

    @Override
    public void handleCacheSelectionChanged(Cache selectedCache, Waypoint selectedWaypoint) {
        try {
            // view must be refilled with values
            // cache and currentCache are the same objects so ==, but content has changed, thus setting currentCache to null
            currentCache = null;
            setSelectedCache(selectedCache);
        } catch (Exception ex) {
            Log.err(sClass, "handleCacheChanged", ex);
        }
    }

    @Override
    public void wayPointListChanged(Cache cache) {
        try {
            if (cache != currentCache)
                return;
            currentCache = null;
            setSelectedCache(cache);
        } catch (Exception ex) {
            Log.err(sClass, "wayPointListChanged", ex);
        }
    }

    private void setSelectedCache(Cache cache) {
        try {

            if (cache != GlobalCore.getSelectedCache()) {
                Log.err(sClass, new Exception("should set current cache not to selected one"));
            }

            if (currentCache != cache) {
                currentCache = GlobalCore.getSelectedCache();
                setAdapter(null);
                wayPointListViewAdapter = new WayPointListViewAdapter(currentCache);
                setAdapter(wayPointListViewAdapter);
            }

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
                                Log.debug(sClass, "Scroll to:" + id);
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
                        Log.debug(sClass, "Scroll to:" + 0);
                    }
                }
            }

        } catch (Exception ex) {
            Log.err(sClass, "setSelectedCache", ex);
        }
    }

    public Menu getContextMenu() {
        Menu cm = new Menu("WaypointViewContextMenuTitle");
        try {
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
                if (!isAccessTokenInvalid()) {
                    cm.addMenuItem("UploadCorrectedCoordinates", null, () -> GL.that.postAsync(() -> {
                        if (currentCache.hasCorrectedCoordinates())
                            GroundspeakAPI.uploadCorrectedCoordinates(currentCache.getGeoCacheCode(), currentCache.getCoordinate());
                        else if (currentWaypoint.isCorrectedFinal())
                            GroundspeakAPI.uploadCorrectedCoordinates(currentCache.getGeoCacheCode(), currentWaypoint.getCoordinate());
                        if (GroundspeakAPI.APIError == 0) {
                            new ButtonDialog(Translation.get("ok"), Translation.get("UploadCorrectedCoordinates"), MsgBoxButton.OK, MsgBoxIcon.Information).show();
                        } else {
                            new ButtonDialog(GroundspeakAPI.LastAPIError, Translation.get("UploadCorrectedCoordinates"), MsgBoxButton.OK, MsgBoxIcon.Information).show();
                        }
                    }));
                }
            }
        } catch (Exception ex) {
            Log.err(sClass, "getContextMenu", ex);
        }
        return cm;
    }

    public void addWP() {
        try {
            createNewWaypoint = true;
            String newGcCode;
            try {
                newGcCode = WaypointDAO.getInstance().createFreeGcCode(GlobalCore.getSelectedCache().getGeoCacheCode());
            } catch (Exception e) {
                return;
            }
            Coordinate coordinate = GlobalCore.getSelectedCoordinate();
            if (coordinate == null)
                coordinate = Locator.getInstance().getMyPosition();
            if ((coordinate == null) || (!coordinate.isValid()))
                coordinate = GlobalCore.getSelectedCache().getCoordinate();
            Waypoint newWP = new Waypoint(newGcCode, GeoCacheType.ReferencePoint, "", coordinate.getLatitude(), coordinate.getLongitude(), GlobalCore.getSelectedCache().generatedId, "", newGcCode);
            editWP(newWP, true);
        } catch (Exception ex) {
            Log.err(sClass, "addWP", ex);
        }
    }

    private void editWP(boolean showCoordinateDialog) {
        try {
            if (currentWaypoint != null) {
                createNewWaypoint = false;
                editWP(currentWaypoint, showCoordinateDialog);
            }
        } catch (Exception ex) {
            Log.err(sClass, "editWP(boolean showCoordinateDialog)", ex);
        }
    }

    private void editWP(Waypoint wp, boolean showCoordinateDialog) {
        EditWaypoint.IReturnListener afterEdit = waypoint -> {
            Log.debug("after edit waypoint", "some more code");
            if (waypoint != null) {
                if (createNewWaypoint) {

                    GlobalCore.getSelectedCache().getWayPoints().add(waypoint);
                    wayPointListViewAdapter = new WayPointListViewAdapter(GlobalCore.getSelectedCache());
                    WayPointsView.this.setAdapter(wayPointListViewAdapter);
                    currentWaypoint = waypoint;
                    GlobalCore.setSelectedWaypoint(GlobalCore.getSelectedCache(), waypoint);
                    if (waypoint.isStartWaypoint) {
                        // ensure there is only one start point
                        WaypointDAO.getInstance().resetStartWaypoint(GlobalCore.getSelectedCache(), waypoint);
                    }
                    WaypointDAO.getInstance().writeToDatabase(waypoint);

                    currentCache = null;
                    currentWaypoint = null;

                    WayPointsView.this.handleCacheSelectionChanged(GlobalCore.getSelectedCache(), waypoint);

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
                        // ensure there is only one start point
                        WaypointDAO.getInstance().resetStartWaypoint(GlobalCore.getSelectedCache(), currentWaypoint);
                    }
                    WaypointDAO.getInstance().updateDatabase(currentWaypoint);

                    wayPointListViewAdapter = new WayPointListViewAdapter(GlobalCore.getSelectedCache());
                    WayPointsView.this.setAdapter(wayPointListViewAdapter);
                }
            }
        };

        try {
            new EditWaypoint(wp, afterEdit, showCoordinateDialog, false).show();
        } catch (Exception ex) {
            Log.err(sClass, "editWP(Waypoint wp, boolean showCoordinateDialog)", ex);
        }
    }

    private void deleteWP() {
        try {
            ButtonDialog bd = new ButtonDialog(Translation.get("?DelWP") + "\n\n[" + currentWaypoint.getTitleForGui() + "]", Translation.get("!DelWP"), MsgBoxButton.YesNo, MsgBoxIcon.Question);
            bd.setButtonClickHandler((which, data) -> {
                if (which == ButtonDialog.BTN_LEFT_POSITIVE) {
                    try {
                        WaypointDAO.getInstance().deleteFromDatabase(currentWaypoint);
                        GlobalCore.getSelectedCache().getWayPoints().remove(currentWaypoint);
                        currentWaypoint = null;
                        GlobalCore.setSelectedWaypoint(GlobalCore.getSelectedCache(), null);
                        notifyDataSetChanged();
                        scrollToItem(0);
                    } catch (Exception ex) {
                        Log.err(sClass, ex);
                    }
                }
                return true;
            });
            bd.show();
        } catch (Exception ex) {
            Log.err(sClass, "deleteWP", ex);
        }
    }

    private void addProjection() {
        try {
            createNewWaypoint = true;

            final Coordinate coordinate;
            String projName;
            if (currentWaypoint == null) {
                if (currentCache == null) {
                    coordinate = Locator.getInstance().getMyPosition();
                    projName = Translation.get("FromGps");
                } else {
                    coordinate = currentCache.getCoordinate();
                    projName = currentCache.getGeoCacheName();
                }
            } else {
                if (!currentWaypoint.getCoordinate().isValid() || currentWaypoint.getCoordinate().isZero()) {
                    coordinate = Locator.getInstance().getMyPosition();
                    projName = Translation.get("FromGps");
                } else {
                    coordinate = currentWaypoint.getCoordinate();
                    projName = currentWaypoint.getTitle();
                }
            }

            new ProjectionCoordinate("Projection", coordinate, (targetCoordinate, startCoordinate, Bearing, distance) -> {
                if (targetCoordinate == null || targetCoordinate.equals(coordinate))
                    return;

                String newGcCode;
                try {
                    newGcCode = WaypointDAO.getInstance().createFreeGcCode(GlobalCore.getSelectedCache().getGeoCacheCode());
                } catch (Exception e) {

                    return;
                }
                Waypoint newWP = new Waypoint(newGcCode, GeoCacheType.ReferencePoint, "Entered Manually", targetCoordinate.getLatitude(), targetCoordinate.getLongitude(), GlobalCore.getSelectedCache().generatedId, "", newGcCode);
                GlobalCore.getSelectedCache().getWayPoints().add(newWP);
                wayPointListViewAdapter = new WayPointListViewAdapter(GlobalCore.getSelectedCache());
                setAdapter(wayPointListViewAdapter);
                currentWaypoint = newWP;
                GlobalCore.setSelectedWaypoint(GlobalCore.getSelectedCache(), newWP);
                WaypointDAO.getInstance().writeToDatabase(newWP);

            }, ProjectionCoordinate.ProjectionType.projection, projName).show();

        } catch (Exception ex) {
            Log.err(sClass, "addProjection", ex);
        }
    }

    private void addMeasure() {
        new MeasureCoordinate("Projection", returnCoordinate -> {
            if (returnCoordinate != null) {
                try {
                    String newGcCode;
                    try {
                        newGcCode = WaypointDAO.getInstance().createFreeGcCode(GlobalCore.getSelectedCache().getGeoCacheCode());
                    } catch (Exception e) {
                        return;
                    }
                    Waypoint newWP = new Waypoint(newGcCode, GeoCacheType.ReferencePoint, "Measured", returnCoordinate.getLatitude(), returnCoordinate.getLongitude(), GlobalCore.getSelectedCache().generatedId, "", newGcCode);
                    GlobalCore.getSelectedCache().getWayPoints().add(newWP);
                    WaypointDAO.getInstance().writeToDatabase(newWP);
                    GlobalCore.setSelectedWaypoint(GlobalCore.getSelectedCache(), newWP);
                } catch (Exception ex) {
                    Log.err(sClass, "addMeasure", ex);
                }
            }
        }).show();
    }

    public class WayPointListViewAdapter implements Adapter {
        private Cache cache;
        private CB_List<ListViewItemBase> wayPoints;

        WayPointListViewAdapter(Cache cache) {
            this.cache = cache;
            wayPoints = new CB_List<>();
            try {
                wayPoints.ensureCapacity(cache.getWayPoints().size() + 1, true);
            } catch (Exception ex) {
                Log.err(sClass, "Adapter: create WayPointListViewAdapter", ex);
            }
        }

        public void setCache(Cache cache) {
            this.cache = cache;
            wayPoints = new CB_List<>();
            try {
                wayPoints.ensureCapacity(cache.getWayPoints().size() + 1, true);
            } catch (Exception ex) {
                Log.err(sClass, "Adapter: setCache wayPoints.ensureCapacity", ex);
            }
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
            try {
                if (cache != null) {
                    if (position == 0) {
                        // the cache
                        if (wayPoints.get(position) == null) {
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
                            waypointViewItem.setLongClickHandler((v, x, y, pointer, button) -> {
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
                                WayPointsView.this.calculateItemPosition();
                                mMustSetPos = true;
                                GL.that.renderOnce(true);
                            });
                            wayPoints.replace(waypointViewItem, position);
                        }
                    } else {
                        if (wayPoints.get(position) == null) {
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
                            waypointViewItem.setLongClickHandler((v, x, y, pointer, button) -> {
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
                                WayPointsView.this.calculateItemPosition();
                                mMustSetPos = true;
                                GL.that.renderOnce(true);
                            });
                            wayPoints.replace(waypointViewItem, position);
                        }
                    }
                    return wayPoints.get(position);
                } else
                    return null;
            } catch (Exception ex) {
                Log.err(sClass, "Adapter: getView", ex);
                return null;
            }
        }

        @Override
        public float getItemSize(int position) {
            try {
                if (wayPoints.get(position) == null) {
                    getView(position);
                }
                return wayPoints.get(position).getHeight();
            } catch (Exception ex) {
                Log.err(sClass, "Adapter: getItemSize", ex);
                return 0f;
            }
        }

    }

}
