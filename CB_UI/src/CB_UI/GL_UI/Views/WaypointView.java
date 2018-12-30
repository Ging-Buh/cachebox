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

import CB_Core.Api.GroundspeakAPI;
import CB_Core.CacheTypes;
import CB_Core.DAO.WaypointDAO;
import CB_Core.Database;
import CB_Core.Types.Cache;
import CB_Core.Types.Waypoint;
import CB_Locator.Coordinate;
import CB_Locator.Locator;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.GL_UI.Activitys.EditWaypoint;
import CB_UI.GL_UI.Activitys.EditWaypoint.IReturnListener;
import CB_UI.GL_UI.Activitys.MeasureCoordinate;
import CB_UI.GL_UI.Activitys.ProjectionCoordinate;
import CB_UI.GL_UI.Activitys.ProjectionCoordinate.Type;
import CB_UI.*;
import CB_UI_Base.GL_UI.Activitys.ActivityBase;
import CB_UI_Base.GL_UI.Controls.List.Adapter;
import CB_UI_Base.GL_UI.Controls.List.ListViewItemBase;
import CB_UI_Base.GL_UI.Controls.List.V_ListView;
import CB_UI_Base.GL_UI.Controls.MessageBox.GL_MsgBox;
import CB_UI_Base.GL_UI.Controls.MessageBox.GL_MsgBox.OnMsgBoxClickListener;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxIcon;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.Menu.Menu;
import CB_UI_Base.GL_UI.Menu.MenuItem;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.SizeChangedEvent;
import CB_UI_Base.Math.UiSizes;
import CB_Utils.Lists.CB_List;
import CB_Utils.Log.Log;
import CB_Utils.Math.Point;

public class WaypointView extends V_ListView implements SelectedCacheEvent, WaypointListChangedEvent {
    private static final String log = "WaypointView";
    private static final int MI_EDIT = 0;
    private static final int MI_ADD = 1;
    private static final int MI_DELETE = 2;
    private static final int MI_PROJECTION = 3;
    private static final int MI_FROM_GPS = 4;
    private static final int MI_WP_SHOW = 5;
    private static final int MI_UploadCorrectedCoordinates = 6;
    public static WaypointView that;
    private final SizeChangedEvent onItemSizeChanged = new SizeChangedEvent() {

        @Override
        public void sizeChanged() {
            // relayout items
            WaypointView.this.calcDefaultPosList();
            mMustSetPos = true;
            GL.that.renderOnce(true);
        }
    };
    public Waypoint aktWaypoint = null;
    public Cache aktCache = null;
    private final OnClickListener onItemClickListener = new OnClickListener() {

        @Override
        public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
            int selectionIndex = ((ListViewItemBase) v).getIndex();

            if (selectionIndex == 0) {
                // Cache selected
                GlobalCore.setSelectedCache(aktCache);
            } else {
                // waypoint selected
                WaypointViewItem wpi = (WaypointViewItem) v;
                if (wpi != null) {
                    aktWaypoint = wpi.getWaypoint();
                }
                GlobalCore.setSelectedWaypoint(aktCache, aktWaypoint);
            }

            setSelection(selectionIndex);
            return true;
        }
    };
    CustomAdapter lvAdapter;
    boolean createNewWaypoint = false;
    private final OnClickListener onItemLongClickListener = new OnClickListener() {

        @Override
        public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
            int selectionIndex = ((ListViewItemBase) v).getIndex();

            if (selectionIndex == 0) {
                // Cache selected
                GlobalCore.setSelectedCache(aktCache);
            } else {
                // waypoint selected
                WaypointViewItem wpi = (WaypointViewItem) v;
                if (wpi != null) {
                    aktWaypoint = wpi.getWaypoint();
                }
                GlobalCore.setSelectedWaypoint(aktCache, aktWaypoint);
            }

            setSelection(selectionIndex);
            getContextMenu().Show();
            return true;
        }
    };

    public WaypointView(CB_RectF rec, String Name) {
        super(rec, Name);
        that = this;

        setBackground(Sprites.ListBack);

        SetSelectedCache(GlobalCore.getSelectedCache(), GlobalCore.getSelectedWaypoint());
        SelectedCacheEventList.Add(this);
        WaypointListChangedEventList.Add(this);
        this.setDisposeFlag(false);
    }

    @Override
    public void onShow() {

        SetSelectedCache(aktCache, aktWaypoint);
        chkSlideBack();

    }

    @Override
    public void onHide() {

    }

    private void SetSelectedCache(Cache cache, Waypoint waypoint) {

        if (aktCache != cache) {
            aktCache = GlobalCore.getSelectedCache();
            this.setBaseAdapter(null);
            lvAdapter = new CustomAdapter(aktCache);
            this.setBaseAdapter(lvAdapter);
        }
        // aktuellen Waypoint in der List anzeigen

        Point lastAndFirst = this.getFirstAndLastVisibleIndex();

        Log.debug(log, "[Waypoint Select]");
        try {
            Log.debug(log, "First visible:[" + lastAndFirst.x + "]" + this.lvAdapter.getItem(lastAndFirst.x).toString());
        } catch (Exception e) {
            Log.debug(log, "no firstItem with index :" + lastAndFirst.x);
        }
        try {
            Log.debug(log, "Last visible:[" + lastAndFirst.y + "]" + this.lvAdapter.getItem(lastAndFirst.y).toString());
        } catch (Exception e) {
            Log.debug(log, "no lastItem with index :" + lastAndFirst.y);
        }

        if (aktCache == null)
            return;

        int itemCount = aktCache.waypoints == null ? 1 : aktCache.waypoints.size() + 1;
        int itemSpace = this.getMaxItemCount();

        if (itemSpace >= itemCount) {
            this.setUnDraggable();
        } else {
            this.setDraggable();
        }

        if (GlobalCore.getSelectedWaypoint() != null) {

            if (aktWaypoint == GlobalCore.getSelectedWaypoint()) {
                // is selected
                return;
            }

            aktWaypoint = GlobalCore.getSelectedWaypoint();
            int id = 0;

            for (int i = 0, n = aktCache.waypoints.size(); i < n; i++) {
                Waypoint wp = aktCache.waypoints.get(i);
                id++;
                if (wp == aktWaypoint) {
                    this.setSelection(id);
                    if (this.isDragable()) {
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
            if (this.isDragable()) {
                if (!(lastAndFirst.x <= 0 && lastAndFirst.y >= 0)) {
                    this.scrollToItem(0);
                    Log.debug(log, "Scroll to:" + 0);
                }
            }
        }

    }

    @Override
    public void SelectedCacheChanged(Cache cache, Waypoint waypoint) {
        SetSelectedCache(cache, waypoint);
    }

    @Override
    public void WaypointListChanged(Cache cache) {
        if (cache != aktCache)
            return;
        aktCache = null;
        SetSelectedCache(cache, aktWaypoint);
    }

    public Menu getContextMenu() {
        Menu cm = new Menu("CacheListContextMenu");

        cm.addOnClickListener(new OnClickListener() {
            @Override
            public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
                switch (((MenuItem) v).getMenuItemId()) {
                    case MI_ADD:
                        addWP();
                        return true;
                    case MI_WP_SHOW:
                        editWP(false);
                        return true;
                    case MI_EDIT:
                        editWP(true);
                        return true;
                    case MI_DELETE:
                        deleteWP();
                        return true;
                    case MI_PROJECTION:
                        addProjection();
                        return true;
                    case MI_FROM_GPS:
                        addMeasure();
                        return true;
                    case MI_UploadCorrectedCoordinates:
                        GL.that.postAsync(new Runnable() {
                            @Override
                            public void run() {
                                if (aktCache.hasCorrectedCoordinates())
                                    GroundspeakAPI.uploadCorrectedCoordinates(aktCache.getGcCode(), aktCache.Pos);
                                else if (aktWaypoint.hasCorrectedFinal())
                                    GroundspeakAPI.uploadCorrectedCoordinates(aktCache.getGcCode(), aktWaypoint.Pos);
                                if (GroundspeakAPI.APIError == 0) {
                                    GL_MsgBox.Show(Translation.Get("ok"), Translation.Get("UploadCorrectedCoordinates"), MessageBoxButtons.OK, MessageBoxIcon.Information, null);
                                }
                                else {
                                    GL_MsgBox.Show(GroundspeakAPI.LastAPIError, Translation.Get("UploadCorrectedCoordinates"), MessageBoxButtons.OK, MessageBoxIcon.Information, null);
                                }

                            }
                        });
                        return true;
                }
                return false;
            }
        });

        if (aktWaypoint != null)
            cm.addItem(MI_WP_SHOW, "show");
        if (aktWaypoint != null)
            cm.addItem(MI_EDIT, "edit");
        cm.addItem(MI_ADD, "AddWaypoint");
        if ((aktWaypoint != null) && (aktWaypoint.IsUserWaypoint))
            cm.addItem(MI_DELETE, "delete");
        if (aktWaypoint != null || aktCache != null)
            cm.addItem(MI_PROJECTION, "Projection");
        MenuItem mi = cm.addItem(MI_UploadCorrectedCoordinates, "UploadCorrectedCoordinates");
        mi.setEnabled(aktCache.hasCorrectedCoordinates() || (aktWaypoint != null && aktWaypoint.hasCorrectedFinal()));
        cm.addItem(MI_FROM_GPS, "FromGps");

        return cm;
    }

    public void addWP() {
        createNewWaypoint = true;
        String newGcCode = "";
        try {
            newGcCode = Database.CreateFreeGcCode(GlobalCore.getSelectedCache().getGcCode());
        } catch (Exception e) {
            return;
        }
        Coordinate coord = GlobalCore.getSelectedCoord();
        if (coord == null)
            coord = Locator.getCoordinate();
        if ((coord == null) || (!coord.isValid()))
            coord = GlobalCore.getSelectedCache().Pos;
        //Waypoint newWP = new Waypoint(newGcCode, CacheTypes.ReferencePoint, "", coord.getLatitude(), coord.getLongitude(), GlobalCore.getSelectedCache().Id, "", Translation.Get("wyptDefTitle"));
        Waypoint newWP = new Waypoint(newGcCode, CacheTypes.ReferencePoint, "", coord.getLatitude(), coord.getLongitude(), GlobalCore.getSelectedCache().Id, "", newGcCode);

        editWP(newWP, true);

    }

    private void editWP(boolean showCoordinateDialog) {
        if (aktWaypoint != null) {
            createNewWaypoint = false;
            editWP(aktWaypoint, showCoordinateDialog);
        }
    }

    private void editWP(Waypoint wp, boolean showCoordinateDialog) {

        EditWaypoint EdWp = new EditWaypoint(wp, new IReturnListener() {

            @Override
            public void returnedWP(Waypoint waypoint) {
                if (waypoint != null) {
                    if (createNewWaypoint) {

                        GlobalCore.getSelectedCache().waypoints.add(waypoint);
                        lvAdapter = new CustomAdapter(GlobalCore.getSelectedCache());
                        that.setBaseAdapter(lvAdapter);
                        aktWaypoint = waypoint;
                        GlobalCore.setSelectedWaypoint(GlobalCore.getSelectedCache(), waypoint);
                        if (waypoint.IsStart) {
                            // Es muss hier sichergestellt sein dass dieser Waypoint der einzige dieses Caches ist, der als Startpunkt
                            // definiert
                            // ist!!!
                            WaypointDAO wpd = new WaypointDAO();
                            wpd.ResetStartWaypoint(GlobalCore.getSelectedCache(), waypoint);
                        }
                        WaypointDAO waypointDAO = new WaypointDAO();
                        waypointDAO.WriteToDatabase(waypoint);

                        aktCache = null;
                        aktWaypoint = null;

                        SelectedCacheChanged(GlobalCore.getSelectedCache(), waypoint);

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
                            wpd.ResetStartWaypoint(GlobalCore.getSelectedCache(), aktWaypoint);
                        }
                        WaypointDAO waypointDAO = new WaypointDAO();
                        waypointDAO.UpdateDatabase(aktWaypoint);

                        lvAdapter = new CustomAdapter(GlobalCore.getSelectedCache());
                        that.setBaseAdapter(lvAdapter);
                    }
                }
            }
        }, showCoordinateDialog, false);
        EdWp.show();

    }

    private void deleteWP() {
        GL_MsgBox.Show(Translation.Get("?DelWP") + "\n\n[" + aktWaypoint.getTitle() + "]", Translation.Get("!DelWP"), MessageBoxButtons.YesNo, MessageBoxIcon.Question, new OnMsgBoxClickListener() {

            @Override
            public boolean onClick(int which, Object data) {
                switch (which) {
                    case GL_MsgBox.BUTTON_POSITIVE:
                        // Yes button clicked
                        Database.DeleteFromDatabase(aktWaypoint);
                        GlobalCore.getSelectedCache().waypoints.remove(aktWaypoint);
                        GlobalCore.setSelectedWaypoint(GlobalCore.getSelectedCache(), null);
                        aktWaypoint = null;
                        lvAdapter = new CustomAdapter(GlobalCore.getSelectedCache());
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
                    case GL_MsgBox.BUTTON_NEGATIVE:
                        // No button clicked
                        break;
                }
                return true;
            }
        });
    }

    private void addProjection() {
        createNewWaypoint = true;

        final Coordinate coord = (aktWaypoint != null) ? aktWaypoint.Pos : (aktCache != null) ? aktCache.Pos : Locator.getCoordinate();
        String ProjName = null;

        ProjName = (aktWaypoint != null) ? aktWaypoint.getTitle() : (aktCache != null) ? aktCache.getName() : null;

        Log.debug(log, "WaypointView.addProjection()");
        Log.debug(log, "   AktWaypoint:" + ((aktWaypoint == null) ? "null" : aktWaypoint.toString()));
        Log.debug(log, "   AktCache:" + ((aktCache == null) ? "null" : aktCache.toString()));
        Log.debug(log, "   using Coord:" + coord.toString());

        ProjectionCoordinate pC = new ProjectionCoordinate(ActivityBase.ActivityRec(), "Projection", coord, new CB_UI.GL_UI.Activitys.ProjectionCoordinate.ICoordReturnListener() {

            @Override
            public void returnCoord(Coordinate targetCoord, Coordinate startCoord, double Bearing, double distance) {
                if (coord == null || targetCoord == null || targetCoord.equals(coord))
                    return;

                String newGcCode = "";
                try {
                    newGcCode = Database.CreateFreeGcCode(GlobalCore.getSelectedCache().getGcCode());
                } catch (Exception e) {

                    return;
                }
                //Waypoint newWP = new Waypoint(newGcCode, CacheTypes.ReferencePoint, "Entered Manually", targetCoord.getLatitude(), targetCoord.getLongitude(), GlobalCore.getSelectedCache().Id, "", "projiziert");
                Waypoint newWP = new Waypoint(newGcCode, CacheTypes.ReferencePoint, "Entered Manually", targetCoord.getLatitude(), targetCoord.getLongitude(), GlobalCore.getSelectedCache().Id, "", newGcCode);
                GlobalCore.getSelectedCache().waypoints.add(newWP);
                lvAdapter = new CustomAdapter(GlobalCore.getSelectedCache());
                that.setBaseAdapter(lvAdapter);
                aktWaypoint = newWP;
                GlobalCore.setSelectedWaypoint(GlobalCore.getSelectedCache(), newWP);
                WaypointDAO waypointDAO = new WaypointDAO();
                waypointDAO.WriteToDatabase(newWP);

            }

        }, Type.projetion, ProjName);

        pC.show();

    }

    private void addMeasure() {
        createNewWaypoint = true;

        MeasureCoordinate mC = new MeasureCoordinate(ActivityBase.ActivityRec(), "Projection", new MeasureCoordinate.ICoordReturnListener() {

            @Override
            public void returnCoord(Coordinate returnCoord) {
                if (returnCoord == null)
                    return;

                String newGcCode = "";
                try {
                    newGcCode = Database.CreateFreeGcCode(GlobalCore.getSelectedCache().getGcCode());
                } catch (Exception e) {

                    return;
                }
                //Waypoint newWP = new Waypoint(newGcCode, CacheTypes.ReferencePoint, "Measured", returnCoord.getLatitude(), returnCoord.getLongitude(), GlobalCore.getSelectedCache().Id, "", "Measured");
                Waypoint newWP = new Waypoint(newGcCode, CacheTypes.ReferencePoint, "Measured", returnCoord.getLatitude(), returnCoord.getLongitude(), GlobalCore.getSelectedCache().Id, "", newGcCode);
                GlobalCore.getSelectedCache().waypoints.add(newWP);

                lvAdapter = new CustomAdapter(GlobalCore.getSelectedCache());
                that.setBaseAdapter(lvAdapter);

                aktWaypoint = newWP;
                GlobalCore.setSelectedWaypoint(GlobalCore.getSelectedCache(), newWP);
                WaypointDAO waypointDAO = new WaypointDAO();
                waypointDAO.WriteToDatabase(newWP);

            }
        });

        mC.show();

    }

    public void Refresh() {
        aktWaypoint = null;
        aktCache = null;
        SetSelectedCache(GlobalCore.getSelectedCache(), GlobalCore.getSelectedWaypoint());
    }

    @Override
    public void dispose() {
        // release all Member
        lvAdapter = null;
        aktWaypoint = null;
        aktCache = null;
        that = null;

        // release all EventHandler
        SelectedCacheEventList.Remove(this);
        WaypointListChangedEventList.Remove(this);
        super.dispose();
    }

    public class CustomAdapter implements Adapter {
        private Cache cache;
        private CB_List<ListViewItemBase> items;

        public CustomAdapter(Cache cache) {
            this.cache = cache;
            this.items = new CB_List<ListViewItemBase>();
            this.items.ensureCapacity(cache.waypoints.size() + 1, true);
        }

        public void setCache(Cache cache) {
            this.cache = cache;
            this.items = new CB_List<ListViewItemBase>();
            this.items.ensureCapacity(cache.waypoints.size() + 1, true);
        }

        @Override
        public int getCount() {
            if (cache != null && cache.waypoints != null)
                return cache.waypoints.size() + 1;
            else
                return 0;
        }

        public Object getItem(int position) {
            if (cache != null) {
                if (position == 0)
                    return cache;
                else
                    return cache.waypoints.get(position - 1);
            } else
                return null;
        }

        public long getItemId(int position) {
            return position;
        }

        @Override
        public ListViewItemBase getView(int position) {
            if (cache != null) {
                if (position == 0) {

                    if (items.get(position) == null || items.get(position).isDisposed()) {
                        WaypointViewItem v = new WaypointViewItem(UiSizes.that.getCacheListItemRec().asFloat(), position, cache, null);
                        v.setClickable(true);
                        v.setOnClickListener(onItemClickListener);
                        v.setOnLongClickListener(onItemLongClickListener);
                        v.Add(onItemSizeChanged);
                        items.replace(v, position);
                    }

                    return items.get(position);
                } else {
                    if (items.get(position) == null || items.get(position).isDisposed()) {
                        Waypoint waypoint = cache.waypoints.get(position - 1);
                        WaypointViewItem v = new WaypointViewItem(UiSizes.that.getCacheListItemRec().asFloat(), position, cache, waypoint);
                        v.setClickable(true);
                        v.setOnClickListener(onItemClickListener);
                        v.setOnLongClickListener(onItemLongClickListener);
                        v.Add(onItemSizeChanged);
                        items.replace(v, position);
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
