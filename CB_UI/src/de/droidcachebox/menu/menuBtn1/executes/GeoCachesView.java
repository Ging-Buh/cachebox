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
package de.droidcachebox.menu.menuBtn1.executes;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.utils.Align;

import java.util.Timer;
import java.util.TimerTask;

import de.droidcachebox.AbstractShowAction;
import de.droidcachebox.CacheSelectionChangedListeners;
import de.droidcachebox.GlobalCore;
import de.droidcachebox.core.CacheListChangedListeners;
import de.droidcachebox.database.CBDB;
import de.droidcachebox.dataclasses.Cache;
import de.droidcachebox.dataclasses.GeoCacheType;
import de.droidcachebox.dataclasses.Waypoint;
import de.droidcachebox.gdx.CB_View_Base;
import de.droidcachebox.gdx.Fonts;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.controls.list.Adapter;
import de.droidcachebox.gdx.controls.list.ListViewItemBase;
import de.droidcachebox.gdx.controls.list.Scrollbar;
import de.droidcachebox.gdx.controls.list.V_ListView;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.UiSizes;
import de.droidcachebox.gdx.views.CacheListViewItem;
import de.droidcachebox.locator.PositionChangedEvent;
import de.droidcachebox.locator.PositionChangedListeners;
import de.droidcachebox.menu.Action;
import de.droidcachebox.menu.ViewManager;
import de.droidcachebox.menu.menuBtn1.contextmenus.CacheContextMenu;
import de.droidcachebox.settings.Settings;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.Point;
import de.droidcachebox.utils.log.Log;

public class GeoCachesView extends CB_View_Base implements CacheListChangedListeners.CacheListChangedListener, CacheSelectionChangedListeners.CacheSelectionChangedListener, PositionChangedEvent {
    private static final String sClass = "GeoCacheListListView";
    private final V_ListView geoCacheListView;
    private Scrollbar scrollBar;
    private GeoCacheListViewAdapter geoCacheListViewAdapter;
    private BitmapFontCache emptyMsg;
    private boolean isShown = false;
    private float heightOfSearchDialog = 0;

    public GeoCachesView() {
        super(ViewManager.leftTab.getContentRec(), "CacheListView");
        registerSkinChangedEvent();
        geoCacheListView = new V_ListView(ViewManager.leftTab.getContentRec(), "CacheListView");
        geoCacheListView.setZeroPos();

        geoCacheListView.addListPosChangedEventHandler(() -> scrollBar.scrollPositionChanged());
        scrollBar = new Scrollbar(geoCacheListView);

        addChild(geoCacheListView);
        addChild(scrollBar);
    }

    @Override
    public void renderInit() {
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
            emptyMsg = new BitmapFontCache(Fonts.getBig());
            GlyphLayout bounds = emptyMsg.setText("empty / leere Liste", 0f, 0f, getWidth(), Align.left, true);
            emptyMsg.setPosition(getHalfWidth() - (bounds.width / 2), getHalfHeight() - (bounds.height / 2));
            emptyMsg.draw(batch, 0.5f);
        }
    }

    @Override
    public void onShow() {
        scrollBar.onShow();
        if (isShown)
            return;
        isShown = true;
        setBackground(Sprites.ListBack);

        PositionChangedListeners.addListener(this);

        synchronized (CBDB.cacheList) {
            try {
                geoCacheListViewAdapter = new GeoCacheListViewAdapter();
                geoCacheListView.setAdapter(geoCacheListViewAdapter);
                if (geoCacheListView.getMaxNumberOfVisibleItems() >= CBDB.cacheList.size()) {
                    geoCacheListView.setUnDraggable();
                } else {
                    geoCacheListView.setDraggable();
                }
            } catch (Exception ex) {
                Log.err(sClass, "create ");
            }
        }

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                // mark selected Cache in list
                if (GlobalCore.isSetSelectedCache()) {
                    setSelectedCacheVisible();
                } else
                    geoCacheListView.setSelection(0);
                resetRenderInitDone();
                geoCacheListView.chkSlideBack();
            }
        }, 150);

        GL.that.renderOnce();

        CacheListChangedListeners.getInstance().addListener(this);
        CacheSelectionChangedListeners.getInstance().addListener(this);

    }

    public void setSelectedCacheVisible() {
        if (GlobalCore.getSelectedCache() == null)
            return;

        Log.debug(sClass, "start bg-task for making selected Cache visible.");

        geoCacheListView.runIfListInitial(() -> {
            int id = 0;
            Point firstAndLast = geoCacheListView.getFirstAndLastVisibleIndex();

            synchronized (CBDB.cacheList) {
                for (int i = 0, n = CBDB.cacheList.size(); i < n; i++) {
                    Cache ca = CBDB.cacheList.get(i);
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
                    GL.that.runOnGL(() -> {
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
        Log.debug(sClass, "CacheList onHide");
        PositionChangedListeners.removeListener(this);
        CacheListChangedListeners.getInstance().removeListener(this);
        CacheSelectionChangedListeners.getInstance().remove(this);
        geoCacheListViewAdapter = null;
        geoCacheListView.setAdapter(null);
        ((AbstractShowAction) Action.ShowGeoCaches.action).viewIsHiding();
    }

    /*
     for CacheListChangedListeners.CacheListChangedListener
     */
    @Override
    public void cacheListChanged() {
        synchronized (CBDB.cacheList) {
            geoCacheListView.setAdapter(null);
            geoCacheListViewAdapter = new GeoCacheListViewAdapter();
            geoCacheListView.setAdapter(geoCacheListViewAdapter);
            if (geoCacheListView.getMaxNumberOfVisibleItems() >= CBDB.cacheList.size()) {
                geoCacheListView.setUnDraggable();
            } else {
                geoCacheListView.setDraggable();
            }
        }

        if (GlobalCore.isSetSelectedCache()) {
            try {
                if (GlobalCore.getSelectedCache().generatedId != ((CacheListViewItem) geoCacheListView.getSelectedItem()).getCache().generatedId) {
                    setSelectedCacheVisible();
                }
            } catch (Exception ignored) {
            }
        }

        geoCacheListView.chkSlideBack();
    }
    /*
     end  CacheListChangedListeners.CacheListChangedListener
     */

    @Override
    public void handleCacheSelectionChanged(Cache cache, Waypoint selectedWaypoint) {
        // view must be refilled with values
        if (GlobalCore.isSetSelectedCache()) {
            Log.debug(sClass, "handle geoCache " + cache.getGeoCacheCode());
            CacheListViewItem selItem = (CacheListViewItem) geoCacheListView.getSelectedItem();
            if (selItem != null && GlobalCore.getSelectedCache().generatedId != selItem.getCache().generatedId) {
                // TODO Run if ListView Initial and after showing
                geoCacheListView.runIfListInitial(this::setSelectedCacheVisible);
            }
        } else {
            Log.debug(sClass, "geoCache is nothing");
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
        geoCacheListView.setHeight(rec.getHeight() - heightOfSearchDialog);
        geoCacheListView.setZeroPos();
    }

    public void setHeightOfSearchDialog(float heightOfSearchDialog) {
        this.heightOfSearchDialog = heightOfSearchDialog;
        onResized(this);
    }

    public float getYPositionForSearchDialog(float heightOfSearchDialog) {
        this.heightOfSearchDialog = heightOfSearchDialog;
        onResized(this);
        return getMaxY();
    }

    public void resetHeightForSearchDialog() {
        heightOfSearchDialog = 0;
        onResized(this);
    }

    @Override
    public Priority getPriority() {
        return Priority.Normal;
    }

    @Override
    public void speedChanged() {
    }

    private class GeoCacheListViewAdapter implements Adapter {

        @Override
        public int getCount() {
            if (CBDB.cacheList == null)
                return 0;
            return CBDB.cacheList.size();
        }

        @Override
        public ListViewItemBase getView(int index) {
            synchronized (CBDB.cacheList) {
                if (CBDB.cacheList == null) return null;
                if (CBDB.cacheList.size() <= index) return null;

                CacheListViewItem v = new CacheListViewItem(UiSizes.getInstance().getCacheListItemRec().asFloat(), index, CBDB.cacheList.get(index));

                v.setClickable(true);

                if (CBDB.cacheList.get(index).getGeoCacheType() == GeoCacheType.Traditional)
                    v.setEnabled(false);

                v.setClickHandler((v1, x, y, pointer, button) -> {
                    int selectionIndex = ((ListViewItemBase) v1).getIndex();
                    Cache geoCache;
                    synchronized (CBDB.cacheList) {
                        geoCache = CBDB.cacheList.get(selectionIndex);
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
                    if (Settings.CacheContextMenuShortClickToggle.getValue())
                        CacheContextMenu.getInstance().getCacheContextMenu(true).show();
                    return true;
                });

                v.setLongClickHandler((v1, x, y, pointer, button) -> {
                    int selectionIndex = ((ListViewItemBase) v1).getIndex();
                    Cache geoCache;
                    synchronized (CBDB.cacheList) {
                        geoCache = CBDB.cacheList.get(selectionIndex);
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
            if (CBDB.cacheList == null)
                return 0;
            synchronized (CBDB.cacheList) {
                if (CBDB.cacheList.size() == 0)
                    return 0;
                Cache cache = CBDB.cacheList.get(position);
                if (cache == null)
                    return 0;

                // all items with same height
                return UiSizes.getInstance().getCacheListItemRec().getHeight();
            }
        }
    }
}
