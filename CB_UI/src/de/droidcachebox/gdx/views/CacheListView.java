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
import de.droidcachebox.gdx.main.CacheContextMenu;
import de.droidcachebox.gdx.main.ViewManager;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.UiSizes;
import de.droidcachebox.locator.PositionChangedEvent;
import de.droidcachebox.locator.PositionChangedListeners;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.Point;
import de.droidcachebox.utils.log.Log;

import java.util.Timer;
import java.util.TimerTask;

public class CacheListView extends CB_View_Base implements CacheListChangedListeners.CacheListChangedListener, SelectedCacheChangedEventListener, PositionChangedEvent {
    private static final String log = "CacheListView";
    private static CacheListView that;
    private V_ListView listView;
    private Scrollbar scrollBar;
    private CustomAdapter lvAdapter;
    private BitmapFontCache emptyMsg;
    private Boolean isShown = false;
    private float searchPlaceholder = 0;

    private CacheListView() {
        super(ViewManager.leftTab.getContentRec(), "CacheListView");
        registerSkinChangedEvent();
        CacheListChangedListeners.getInstance().addListener(this);
        SelectedCacheChangedEventListeners.getInstance().add(this);
        listView = new V_ListView(ViewManager.leftTab.getContentRec(), "CacheListView");
        listView.setZeroPos();

        listView.addListPosChangedEventHandler(() -> scrollBar.ScrollPositionChanged());
        scrollBar = new Scrollbar(listView);

        this.addChild(listView);
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
        listView.chkSlideBack();
        GL.that.renderOnce();
    }

    @Override
    public void render(Batch batch) {
        // if Track List empty, draw empty Msg
        try {
            if (lvAdapter == null || lvAdapter.getCount() == 0) {
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
                lvAdapter = new CustomAdapter(Database.Data.cacheList);
                listView.setBaseAdapter(lvAdapter);

                int itemCount = Database.Data.cacheList.size();
                int itemSpace = listView.getMaxItemCount();

                if (itemSpace >= itemCount) {
                    listView.setUnDraggable();
                } else {
                    listView.setDraggable();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                // aktuellen Cache in der List anzeigen
                if (GlobalCore.isSetSelectedCache()) {
                    setSelectedCacheVisible();

                } else
                    listView.setSelection(0);

                resetInitial();
                listView.chkSlideBack();
            }
        };

        Timer timer = new Timer();
        timer.schedule(task, 150);

        GL.that.renderOnce();
    }

    public void setSelectedCacheVisible() {
        if (GlobalCore.getSelectedCache() == null)
            return;

        listView.RunIfListInitial(() -> {
            int id = 0;
            Point firstAndLast = listView.getFirstAndLastVisibleIndex();

            synchronized (Database.Data.cacheList) {
                for (int i = 0, n = Database.Data.cacheList.size(); i < n; i++) {
                    Cache ca = Database.Data.cacheList.get(i);
                    if (ca.Id == GlobalCore.getSelectedCache().Id) {
                        listView.setSelection(id);
                        if (listView.isDraggable()) {
                            if (!(firstAndLast.x <= id && firstAndLast.y >= id)) {
                                listView.scrollToItem(id);
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
                        if (listView != null)
                            listView.chkSlideBack();
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

        lvAdapter = null;
        listView.setBaseAdapter(lvAdapter);
    }

    @Override
    public void cacheListChanged() {
        Log.debug(log, "CacheListChangedEvent on Cache List");
        try {
            listView.setBaseAdapter(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        synchronized (Database.Data.cacheList) {
            lvAdapter = new CustomAdapter(Database.Data.cacheList);

            listView.setBaseAdapter(lvAdapter);

            int itemCount = Database.Data.cacheList.size();
            int itemSpace = listView.getMaxItemCount();

            if (itemSpace >= itemCount) {
                listView.setUnDraggable();
            } else {
                listView.setDraggable();
            }
        }

        if (GlobalCore.isSetSelectedCache()) {
            boolean diverend = true;

            try {
                diverend = GlobalCore.getSelectedCache().Id != ((CacheListViewItem) listView.getSelectedItem()).getCache().Id;
            } catch (Exception ignored) {
            }

            if (diverend) {
                setSelectedCacheVisible();
            }
        }

        listView.chkSlideBack();
    }

    @Override
    public void selectedCacheChanged(Cache cache, Waypoint waypoint) {
        // view must be refilled with values
        if (GlobalCore.isSetSelectedCache()) {
            CacheListViewItem selItem = (CacheListViewItem) listView.getSelectedItem();
            if (selItem != null && GlobalCore.getSelectedCache().Id != selItem.getCache().Id) {
                // TODO Run if ListView Initial and after showing
                listView.RunIfListInitial(this::setSelectedCacheVisible);
            }
        }
    }

    @Override
    protected void skinIsChanged() {
        if (listView != null)
            listView.reloadItems();
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
        listView.setSize(rec);
        listView.setHeight(rec.getHeight() + searchPlaceholder);
        listView.setZeroPos();
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

        if (listView != null)
            listView.dispose();
        listView = null;
        if (scrollBar != null)
            scrollBar.dispose();
        scrollBar = null;
        if (lvAdapter != null)
            lvAdapter.dispose();
        lvAdapter = null;
        if (emptyMsg != null)
            emptyMsg.clear();
        emptyMsg = null;

        CacheListChangedListeners.getInstance().removeListener(this);
        SelectedCacheChangedEventListeners.getInstance().remove(this);
        PositionChangedListeners.removeListener(this);

        super.dispose();
    }

    public class CustomAdapter implements Adapter {
        private CacheList cacheList;

        private int Count;

        public CustomAdapter(CacheList cacheList) {
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
        public ListViewItemBase getView(int position) {
            synchronized (cacheList) {
                if (cacheList == null)
                    return null;

                if (cacheList.size() <= position)
                    return null;

                Cache cache = cacheList.get(position);

                CacheListViewItem v = new CacheListViewItem(UiSizes.getInstance().getCacheListItemRec().asFloat(), position, cache);
                v.setClickable(true);

                v.addClickHandler((v1, x, y, pointer, button) -> {
                    int selectionIndex = ((ListViewItemBase) v1).getIndex();
                    Cache tmp;
                    synchronized (Database.Data.cacheList) {
                        tmp = Database.Data.cacheList.get(selectionIndex);
                    }
                    if (tmp != null) {
                        // Wenn ein Cache einen Final waypoint hat dann soll gleich dieser aktiviert werden
                        Waypoint waypoint = tmp.getCorrectedFinal();
                        if (waypoint == null)
                            waypoint = tmp.getStartWaypoint();
                        GlobalCore.setSelectedWaypoint(tmp, waypoint);
                    }
                    listView.setSelection(selectionIndex);
                    setSelectedCacheVisible();
                    return true;
                });

                v.setOnLongClickListener((v12, x, y, pointer, button) -> {
                    int selectionIndex = ((ListViewItemBase) v12).getIndex();

                    Cache tmp;
                    synchronized (Database.Data.cacheList) {
                        tmp = Database.Data.cacheList.get(selectionIndex);
                    }
                    Waypoint finalWp = tmp.getCorrectedFinal();
                    if (finalWp == null)
                        finalWp = tmp.getStartWaypoint();
                    // shutdown AutoResort when selecting a cache by hand
                    GlobalCore.setAutoResort(false);
                    GlobalCore.setSelectedWaypoint(tmp, finalWp);

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