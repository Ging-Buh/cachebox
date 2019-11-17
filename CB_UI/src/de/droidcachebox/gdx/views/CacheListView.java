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

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.utils.Align;
import de.droidcachebox.Config;
import de.droidcachebox.GlobalCore;
import de.droidcachebox.SelectedCacheChangedEventListener;
import de.droidcachebox.SelectedCacheChangedEventListeners;
import de.droidcachebox.core.CacheListChangedListeners;
import de.droidcachebox.database.Cache;
import de.droidcachebox.database.CacheList;
import de.droidcachebox.database.Database;
import de.droidcachebox.database.Waypoint;
import de.droidcachebox.gdx.CB_View_Base;
import de.droidcachebox.gdx.Fonts;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.controls.list.Adapter;
import de.droidcachebox.gdx.controls.list.ListViewItemBase;
import de.droidcachebox.gdx.controls.list.Scrollbar;
import de.droidcachebox.gdx.controls.list.V_ListView;
import de.droidcachebox.gdx.controls.popups.SearchDialog;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.UiSizes;
import de.droidcachebox.locator.PositionChangedEvent;
import de.droidcachebox.locator.PositionChangedListeners;
import de.droidcachebox.main.ViewManager;
import de.droidcachebox.main.menuBtn1.contextmenus.CacheContextMenu;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.Point;
import de.droidcachebox.utils.log.Log;

import java.util.Timer;
import java.util.TimerTask;

public class CacheListView extends CB_View_Base implements CacheListChangedListeners.CacheListChangedListener, SelectedCacheChangedEventListener, PositionChangedEvent {
    private static final String log = "CacheListView";
    private static CacheListView that;
    private V_ListView geoCacheListView;
    private Scrollbar scrollBar;
    private GeoCacheListViewAdapter geoCacheListViewAdapter;
    private BitmapFontCache emptyMsg;
    private Boolean isShown = false;
    private float searchPlaceholder = 0;

    private CacheListView() {
        super(ViewManager.leftTab.getContentRec(), "CacheListView");
        registerSkinChangedEvent();
        CacheListChangedListeners.getInstance().addListener(this);
        SelectedCacheChangedEventListeners.getInstance().add(this);
        geoCacheListView = new V_ListView(ViewManager.leftTab.getContentRec(), "CacheListView");
        geoCacheListView.setZeroPos();

        geoCacheListView.addListPosChangedEventHandler(() -> scrollBar.ScrollPositionChanged());
        scrollBar = new Scrollbar(geoCacheListView);

        this.addChild(geoCacheListView);
        this.addChild(scrollBar);
    }

    public static CacheListView getInstance() {
        if (that == null) that = new CacheListView();
        return that;
    }

    @Override
    public void initialize() {
        // Log.debug(log, "CacheListView => Initial()");
        // this.setListPos(0, false);
        geoCacheListView.chkSlideBack();
        GL.that.renderOnce();
    }

    @Override
    public void render(Batch batch) {
        // if Track List empty, draw empty Msg
        try {
            if (geoCacheListViewAdapter == null || geoCacheListViewAdapter.getCount() == 0) {
                if (emptyMsg == null) {
                    emptyMsg = new BitmapFontCache(Fonts.getBig());
                    GlyphLayout bounds = emptyMsg.setText(Translation.get("EmptyCacheList"), 0f, 0f, this.getWidth(), Align.left, true);
                    emptyMsg.setPosition(this.getHalfWidth() - (bounds.width / 2), this.getHalfHeight() - (bounds.height / 2));
                }
                if (emptyMsg != null)
                    emptyMsg.draw(batch, 0.5f);
            } else {
                super.render(batch);
            }
        } catch (Exception e) {
            if (emptyMsg == null) {
                emptyMsg = new BitmapFontCache(Fonts.getBig());
                GlyphLayout bounds = emptyMsg.setText(Translation.get("EmptyCacheList"), 0f, 0f, this.getWidth(), Align.left, true);
                emptyMsg.setPosition(this.getHalfWidth() - (bounds.width / 2), this.getHalfHeight() - (bounds.height / 2));
            }
            if (emptyMsg != null)
                emptyMsg.draw(batch, 0.5f);
        }
    }

    @Override
    public void onShow() {
        scrollBar.onShow();
        if (isShown)
            return;

        if (searchPlaceholder > 0) {
            // Blende Search Dialog wieder ein
            if (SearchDialog.that != null)
                SearchDialog.that.showNotCloseAutomaticly();
        }

        isShown = true;
        Log.debug(log, "CacheList onShow");
        setBackground(Sprites.ListBack);

        PositionChangedListeners.addListener(this);

        synchronized (Database.Data.cacheList) {
            try {
                geoCacheListViewAdapter = new GeoCacheListViewAdapter(Database.Data.cacheList);
                geoCacheListView.setBaseAdapter(geoCacheListViewAdapter);

                int itemCount = Database.Data.cacheList.size();
                int itemSpace = geoCacheListView.getMaxItemCount();

                if (itemSpace >= itemCount) {
                    geoCacheListView.setUnDraggable();
                } else {
                    geoCacheListView.setDraggable();
                }
            } catch (Exception ex) {
                Log.err(log, "create ");
            }
        }

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                // aktuellen Cache in der List anzeigen
                if (GlobalCore.isSetSelectedCache()) {
                    setSelectedCacheVisible();

                } else
                    geoCacheListView.setSelection(0);

                resetInitial();
                geoCacheListView.chkSlideBack();
            }
        }, 150);

        GL.that.renderOnce();
    }

    public void setSelectedCacheVisible() {
        if (GlobalCore.getSelectedCache() == null)
            return;

        geoCacheListView.RunIfListInitial(() -> {
            int id = 0;
            Point firstAndLast = geoCacheListView.getFirstAndLastVisibleIndex();

            synchronized (Database.Data.cacheList) {
                for (int i = 0, n = Database.Data.cacheList.size(); i < n; i++) {
                    Cache ca = Database.Data.cacheList.get(i);
                    if (ca.Id == GlobalCore.getSelectedCache().Id) {
                        geoCacheListView.setSelection(id);
                        if (geoCacheListView.isDraggable()) {
                            if (!(firstAndLast.x <= id && firstAndLast.y >= id)) {
                                geoCacheListView.scrollToItem(id);
                                Log.debug(log, "Scroll to:" + id);
                            }
                        }
                        break;
                    }
                    id++;
                }

            }

            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    GL.that.RunOnGL(() -> {
                        if (geoCacheListView != null)
                            geoCacheListView.chkSlideBack();
                        GL.that.renderOnce();
                    });
                }
            };

            Timer timer = new Timer();
            timer.schedule(task, 50);
        });

        GL.that.renderOnce();
    }

    @Override
    public void onHide() {
        isShown = false;
        Log.debug(log, "CacheList onHide");
        PositionChangedListeners.removeListener(this);

        if (searchPlaceholder < 0) {
            // Blende Search Dialog aus
            SearchDialog.that.close();
        }

        geoCacheListViewAdapter = null;
        geoCacheListView.setBaseAdapter(geoCacheListViewAdapter);
    }

    @Override
    public void cacheListChanged() {
        Log.debug(log, "CacheListChangedEvent on Cache List");
        try {
            geoCacheListView.setBaseAdapter(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        synchronized (Database.Data.cacheList) {
            geoCacheListViewAdapter = new GeoCacheListViewAdapter(Database.Data.cacheList);

            geoCacheListView.setBaseAdapter(geoCacheListViewAdapter);

            int itemCount = Database.Data.cacheList.size();
            int itemSpace = geoCacheListView.getMaxItemCount();

            if (itemSpace >= itemCount) {
                geoCacheListView.setUnDraggable();
            } else {
                geoCacheListView.setDraggable();
            }
        }

        if (GlobalCore.isSetSelectedCache()) {
            boolean diverend = true;

            try {
                diverend = GlobalCore.getSelectedCache().Id != ((CacheListViewItem) geoCacheListView.getSelectedItem()).getCache().Id;
            } catch (Exception ignored) {
            }

            if (diverend) {
                setSelectedCacheVisible();
            }
        }

        geoCacheListView.chkSlideBack();
    }

    @Override
    public void selectedCacheChanged(Cache cache, Waypoint waypoint) {
        // view must be refilled with values
        if (GlobalCore.isSetSelectedCache()) {
            CacheListViewItem selItem = (CacheListViewItem) geoCacheListView.getSelectedItem();
            if (selItem != null && GlobalCore.getSelectedCache().Id != selItem.getCache().Id) {
                // TODO Run if ListView Initial and after showing
                geoCacheListView.RunIfListInitial(this::setSelectedCacheVisible);
            }
        }
    }

    @Override
    protected void skinIsChanged() {
        if (geoCacheListView != null)
            geoCacheListView.reloadItems();
        setBackground(Sprites.ListBack);
        CacheListViewItem.ResetBackground();
    }

    @Override
    public void positionChanged() {
        GL.that.renderOnce();
    }

    @Override
    public void orientationChanged() {
        GL.that.renderOnce();
    }

    @Override
    public String getReceiverName() {
        return "Core.CacheListView";
    }

    @Override
    public void onResized(CB_RectF rec) {
        super.onResized(rec);
        geoCacheListView.setSize(rec);
        geoCacheListView.setHeight(rec.getHeight() + searchPlaceholder);
        geoCacheListView.setZeroPos();
    }

    public void setTopPlaceHolder(float PlaceHoldHeight) {
        searchPlaceholder = -PlaceHoldHeight;
        onResized(this);
    }

    public void resetPlaceHolder() {
        searchPlaceholder = 0;
        onResized(this);
    }

    @Override
    public Priority getPriority() {
        return Priority.Normal;
    }

    @Override
    public void speedChanged() {
    }

    @Override
    public void dispose() {
        that = null;

        if (geoCacheListView != null)
            geoCacheListView.dispose();
        geoCacheListView = null;
        if (scrollBar != null)
            scrollBar.dispose();
        scrollBar = null;
        if (geoCacheListViewAdapter != null)
            geoCacheListViewAdapter.dispose();
        geoCacheListViewAdapter = null;
        if (emptyMsg != null)
            emptyMsg.clear();
        emptyMsg = null;

        CacheListChangedListeners.getInstance().removeListener(this);
        SelectedCacheChangedEventListeners.getInstance().remove(this);
        PositionChangedListeners.removeListener(this);

        super.dispose();
    }

    public class GeoCacheListViewAdapter implements Adapter {
        private CacheList cacheList;
        private int Count;

        GeoCacheListViewAdapter(final CacheList cacheList) {
            synchronized (cacheList) {
                this.cacheList = cacheList;
                Count = cacheList.size();
            }
        }

        @Override
        public int getCount() {
            if (cacheList == null)
                return 0;

            return Count;
        }

        @Override
        public ListViewItemBase getView(int index) {
            synchronized (cacheList) {
                if (cacheList == null) return null;
                if (cacheList.size() <= index) return null;
                CacheListViewItem v = new CacheListViewItem(UiSizes.getInstance().getCacheListItemRec().asFloat(), index, cacheList.get(index));
                v.setClickable(true);

                v.addClickHandler((v1, x, y, pointer, button) -> {
                    int selectionIndex = ((ListViewItemBase) v1).getIndex();
                    Cache geoCache;
                    synchronized (Database.Data.cacheList) {
                        geoCache = Database.Data.cacheList.get(selectionIndex);
                    }
                    if (geoCache != null) {
                        Waypoint waypoint = geoCache.getCorrectedFinal();
                        if (waypoint == null)
                            waypoint = geoCache.getStartWaypoint();
                        // shutdown AutoResort when selecting a cache by hand
                        GlobalCore.setAutoResort(false);
                        GlobalCore.setSelectedWaypoint(geoCache, waypoint);
                    }

                    geoCacheListView.setSelection(selectionIndex);
                    setSelectedCacheVisible();
                    invalidate();
                    if (Config.CacheContextMenuShortClickToggle.getValue())
                        CacheContextMenu.getCacheContextMenu(true).show();
                    return true;
                });

                v.setOnLongClickListener((v1, x, y, pointer, button) -> {
                    int selectionIndex = ((ListViewItemBase) v1).getIndex();
                    Cache geoCache;
                    synchronized (Database.Data.cacheList) {
                        geoCache = Database.Data.cacheList.get(selectionIndex);
                    }
                    if (geoCache != null) {
                        Waypoint waypoint = geoCache.getCorrectedFinal();
                        if (waypoint == null)
                            waypoint = geoCache.getStartWaypoint();
                        // shutdown AutoResort when selecting a cache by hand
                        GlobalCore.setAutoResort(false);
                        GlobalCore.setSelectedWaypoint(geoCache, waypoint);
                    }
                    geoCacheListView.setSelection(selectionIndex);
                    setSelectedCacheVisible();
                    invalidate();
                    CacheContextMenu.getCacheContextMenu(true).show();
                    return true;
                });

                return v;
            }

        }

        @Override
        public float getItemSize(int position) {
            if (cacheList == null)
                return 0;

            synchronized (cacheList) {
                if (cacheList.size() == 0)
                    return 0;
                Cache cache = cacheList.get(position);
                if (cache == null)
                    return 0;

                // alle Items haben die gleiche Größe (Höhe)
                return UiSizes.getInstance().getCacheListItemRec().getHeight();
            }

        }

        public void dispose() {
            cacheList = null;
        }

    }
}
