/*
 * Copyright (C) 2015 team-cachebox.de
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

import de.droidcachebox.Config;
import de.droidcachebox.GlobalCore;
import de.droidcachebox.SelectedCacheChangedEventListener;
import de.droidcachebox.SelectedCacheChangedEventListeners;
import de.droidcachebox.database.Cache;
import de.droidcachebox.database.Database;
import de.droidcachebox.database.LogEntry;
import de.droidcachebox.database.Waypoint;
import de.droidcachebox.gdx.Fonts;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.controls.list.Adapter;
import de.droidcachebox.gdx.controls.list.ListViewItemBackground;
import de.droidcachebox.gdx.controls.list.ListViewItemBase;
import de.droidcachebox.gdx.controls.list.V_ListView;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.UiSizes;
import de.droidcachebox.main.ViewManager;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.CB_List;

import java.util.ArrayList;
import java.util.Collections;

public class LogView extends V_ListView implements SelectedCacheChangedEventListener {
    private static CB_RectF itemRec;
    private static LogView logView;
    private Cache aktCache;
    private LogListAdapter logListAdapter;
    private CB_List<LogViewItem> logViewItems;

    private LogView() {
        super(ViewManager.leftTab.getContentRec(), "LogView");
        setForceHandleTouchEvents();
        itemRec = (new CB_RectF(0, 0, this.getWidth(), UiSizes.getInstance().getButtonHeight() * 1.1f)).scaleCenter(0.97f);
        setBackground(Sprites.ListBack);

        this.setAdapter(null);
        setCache(GlobalCore.getSelectedCache());
        this.setDisposeFlag(false);
    }

    public static LogView getInstance() {
        if (logView == null) logView = new LogView();
        return logView;
    }

    @Override
    public void onShow() {
        // if Tab register for Cache Changed Event
        setCache(GlobalCore.getSelectedCache());
    }

    @Override
    public void onHide() {
        SelectedCacheChangedEventListeners.getInstance().remove(this);
    }

    @Override
    public void initialize() {
        // super.Initial(); does nothing at the moment

        createItemList();

        logListAdapter = new LogListAdapter();
        this.setAdapter(logListAdapter);

        this.setEmptyMsg(Translation.get("EmptyLogList"));

        this.scrollTo(0);
    }

    private void createItemList() {
        if (logViewItems == null)
            logViewItems = new CB_List<>();
        logViewItems.clear();

        if (aktCache == null)
            return;

        CB_List<LogEntry> cleanLogs = Database.getLogs(aktCache);

        String finders = Config.Friends.getValue().replace(", ", "|").replace(",", "|");
        String[] finder = finders.split("\\|");
        ArrayList<String> friendList = new ArrayList<>();
        Collections.addAll(friendList, finder);

        int index = 0;
        for (int i = 0, n = cleanLogs.size(); i < n; i++) {
            LogEntry logEntry = cleanLogs.get(i);
            if (GlobalCore.filterLogsOfFriends) {
                // nur die Logs der eingetragenen Freunde anzeigen
                if (!friendList.contains(logEntry.finder)) {
                    continue;
                }
            }
            CB_RectF rec = itemRec.copy();
            rec.setHeight(measureItemHeight(logEntry));
            final LogViewItem v = new LogViewItem(rec, index++, logEntry);

            v.setOnLongClickListener((view, x, y, pointer, button) -> {
                v.copyToClipboard();
                GL.that.Toast(Translation.get("CopyToClipboard"));
                return true;
            });

            logViewItems.add(v);
        }

        this.notifyDataSetChanged();

    }

    private float measureItemHeight(LogEntry logEntry) {
        // object ist nicht von Dialog abgeleitet, daher
        float margin = UiSizes.getInstance().getMargin();
        float headHeight = (UiSizes.getInstance().getButtonHeight() / 1.5f) + margin;

        float mesurdWidth = itemRec.getWidth() - ListViewItemBackground.getLeftWidthStatic() - ListViewItemBackground.getRightWidthStatic() - (margin * 2);

        float commentHeight = (margin * 4) + Fonts.MeasureWrapped(logEntry.logText, mesurdWidth).height;

        return headHeight + commentHeight;
    }

    public Cache getCache() {
        return aktCache;
    }

    public void setCache(Cache cache) {
        if (aktCache != cache) {
            aktCache = cache;
            resetInitial();
        }
    }

    @Override
    public void selectedCacheChanged(Cache cache, Waypoint waypoint) {
        setCache(cache);
    }

    @Override
    public void dispose() {
        this.setAdapter(null);
        aktCache = null;
        logListAdapter = null;
        if (logViewItems != null)
            logViewItems.clear();
        logViewItems = null;
        super.dispose();
        //Log.debug(log, "LogView disposed");
    }

    public class LogListAdapter implements Adapter {
        LogListAdapter() {
        }

        @Override
        public int getCount() {
            if (logViewItems != null) {
                return logViewItems.size();
            } else {
                return 0;
            }
        }

        @Override
        public ListViewItemBase getView(int position) {
            if (logViewItems != null) {
                return logViewItems.get(position);
            } else
                return null;
        }

        @Override
        public float getItemSize(int position) {
            if (logViewItems.size() == 0)
                return 0;
            return logViewItems.get(position).getHeight();
        }

    }
}
