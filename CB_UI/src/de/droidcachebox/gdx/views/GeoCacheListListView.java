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
import de.droidcachebox.CacheSelectionChangedListeners;
import de.droidcachebox.Config;
import de.droidcachebox.GlobalCore;
import de.droidcachebox.core.CacheListChangedListeners;
import de.droidcachebox.database.Cache;
import de.droidcachebox.database.Database;
import de.droidcachebox.database.GeoCacheType;
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
import de.droidcachebox.menu.ViewManager;
import de.droidcachebox.menu.menuBtn1.contextmenus.CacheContextMenu;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.Point;
import de.droidcachebox.utils.log.Log;

import java.util.Timer;
import java.util.TimerTask;

public class GeoCacheListListView extends CB_View_Base implements CacheListChangedListeners.CacheListChangedListener, CacheSelectionChangedListeners.CacheSelectionChangedListener, PositionChangedEvent {
    private static final String log = "GeoCacheListListView";
    private static GeoCacheListListView that;
    private V_ListView geoCacheListView;
    private Scrollbar scrollBar;
    private GeoCacheListViewAdapter geoCacheListViewAdapter;
    private BitmapFontCache emptyMsg;
    private boolean isShown = false;
    private float searchPlaceholder = 0;

    private GeoCacheListListView() {
        super(ViewManager.leftTab.getContentRec(), "CacheListView");
        registerSkinChangedEvent();
        CacheListChangedListeners.getInstance().addListener(this);
        CacheSelectionChangedListeners.getInstance().addListener(this);
        geoCacheListView = new V_ListView(ViewManager.leftTab.getContentRec(), "CacheListView");
        geoCacheListView.setZeroPos();

        geoCacheListView.addListPosChangedEventHandler(() -> scrollBar.scrollPositionChanged());
        scrollBar = new Scrollbar(geoCacheListView);

        addChild(geoCacheListView);
        addChild(scrollBar);
    }

    public static GeoCacheListListView getInstance() {
        if (that == null) that = new GeoCacheListListView();
        return that;
    }

    @Override
    public void initialize() {
        // Log.debug(log, "CacheListView => Initial()");
        // setListPos(0, false);
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
                    GlyphLayout bounds = emptyMsg.setText(Translation.get("EmptyCacheList"), 0f, 0f, getWidth(), Align.left, true);
                    emptyMsg.setPosition(getHalfWidth() - (bounds.width / 2), getHalfHeight() - (bounds.height / 2));
                }
                if (emptyMsg != null)
                    emptyMsg.draw(batch, 0.5f);
            } else {
                super.render(batch);
            }
        } catch (Exception e) {
            if (emptyMsg == null) {
                emptyMsg = new BitmapFontCache(Fonts.getBig());
                GlyphLayout bounds = emptyMsg.setText(Translation.get("EmptyCacheList"), 0f, 0f, getWidth(), Align.left, true);
                emptyMsg.setPosition(getHalfWidth() - (bounds.width / 2), getHalfHeight() - (bounds.height / 2));
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
                geoCacheListViewAdapter = new GeoCacheListViewAdapter();
                geoCacheListView.setAdapter(geoCacheListViewAdapter);
                if (geoCacheListView.getMaxNumberOfVisibleItems() >= Database.Data.cacheList.size()) {
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

                resetIsInitialized();
                geoCacheListView.chkSlideBack();
            }
        }, 150);

        GL.that.renderOnce();
    }

    public void setSelectedCacheVisible() {
        if (GlobalCore.getSelectedCache() == null)
            return;

        Log.debug(log, "start bg-task for making selected Cache visible.");

        geoCacheListView.runIfListInitial(() -> {
            int id = 0;
            Point firstAndLast = geoCacheListView.getFirstAndLastVisibleIndex();

            synchronized (Database.Data.cacheList) {
                for (int i = 0, n = Database.Data.cacheList.size(); i < n; i++) {
                    Cache ca = Database.Data.cacheList.get(i);
                    if (ca.generatedId == GlobalCore.getSelectedCache().generatedId) {
                        geoCacheListView.setSelection(id);
                        if (geoCacheListView.isDraggable()) {
                            if (!(firstAndLast.x <= id && firstAndLast.y >= id)) {
                                geoCacheListView.scrollToItem(id);
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
        geoCacheListView.setAdapter(null);
    }

    @Override
    public void cacheListChanged() {
        synchronized (Database.Data.cacheList) {
            geoCacheListView.setAdapter(null);
            geoCacheListViewAdapter = new GeoCacheListViewAdapter();
            geoCacheListView.setAdapter(geoCacheListViewAdapter);
            if (geoCacheListView.getMaxNumberOfVisibleItems() >= Database.Data.cacheList.size()) {
                geoCacheListView.setUnDraggable();
            } else {
                geoCacheListView.setDraggable();
            }
        }

        if (GlobalCore.isSetSelectedCache()) {
            boolean diverend = true;

            try {
                diverend = GlobalCore.getSelectedCache().generatedId != ((CacheListViewItem) geoCacheListView.getSelectedItem()).getCache().generatedId;
            } catch (Exception ignored) {
            }

            if (diverend) {
                setSelectedCacheVisible();
            }
        }

        geoCacheListView.chkSlideBack();
    }

    @Override
    public void handleCacheChanged(Cache cache, Waypoint waypoint) {
        // view must be refilled with values
        if (GlobalCore.isSetSelectedCache()) {
            CacheListViewItem selItem = (CacheListViewItem) geoCacheListView.getSelectedItem();
            if (selItem != null && GlobalCore.getSelectedCache().generatedId != selItem.getCache().generatedId) {
                // TODO Run if ListView Initial and after showing
                geoCacheListView.runIfListInitial(this::setSelectedCacheVisible);
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
        geoCacheListViewAdapter = null;
        if (emptyMsg != null)
            emptyMsg.clear();
        emptyMsg = null;

        CacheListChangedListeners.getInstance().removeListener(this);
        CacheSelectionChangedListeners.getInstance().remove(this);
        PositionChangedListeners.removeListener(this);

        super.dispose();
    }

    private class GeoCacheListViewAdapter implements Adapter {

        @Override
        public int getCount() {
            if (Database.Data.cacheList == null)
                return 0;
            return Database.Data.cacheList.size();
        }

        @Override
        public ListViewItemBase getView(int index) {
            synchronized (Database.Data.cacheList) {
                if (Database.Data.cacheList == null) return null;
                if (Database.Data.cacheList.size() <= index) return null;

                CacheListViewItem v = new CacheListViewItem(UiSizes.getInstance().getCacheListItemRec().asFloat(), index, Database.Data.cacheList.get(index));

                v.setClickable(true);

                if (Database.Data.cacheList.get(index).getGeoCacheType() == GeoCacheType.Traditional)
                    v.setEnabled(false);

                v.setClickHandler((v1, x, y, pointer, button) -> {
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
                        CacheContextMenu.getInstance().getCacheContextMenu(true).show();
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
                    CacheContextMenu.getInstance().getCacheContextMenu(true).show();
                    return true;
                });

                return v;
            }

        }

        @Override
        public float getItemSize(int position) {
            if (Database.Data.cacheList == null)
                return 0;
            synchronized (Database.Data.cacheList) {
                if (Database.Data.cacheList.size() == 0)
                    return 0;
                Cache cache = Database.Data.cacheList.get(position);
                if (cache == null)
                    return 0;

                // alle Items haben die gleiche Größe (Höhe)
                return UiSizes.getInstance().getCacheListItemRec().getHeight();
            }
        }
    }
}
