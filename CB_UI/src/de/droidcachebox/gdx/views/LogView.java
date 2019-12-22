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
    CB_List<LogItem> allLogs;
    private Cache aktCache;
    private LogListAdapter logListAdapter;
    private ArrayList<String> friendList;

    private LogView() {
        super(ViewManager.leftTab.getContentRec(), "LogView");
        setForceHandleTouchEvents();
        itemRec = (new CB_RectF(0, 0, getWidth(), UiSizes.getInstance().getButtonHeight() * 1.1f)).scaleCenter(0.97f);
        setBackground(Sprites.ListBack);

        setAdapter(null);
        setCache(GlobalCore.getSelectedCache());
        setDisposeFlag(false);
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

        String friends = Config.Friends.getValue().replace(", ", "|").replace(",", "|");
        String[] finder = friends.split("\\|");
        friendList = new ArrayList<>();
        Collections.addAll(friendList, finder);

        createItemList();

        logListAdapter = new LogListAdapter();
        setAdapter(logListAdapter);

        setEmptyMsg(Translation.get("EmptyLogList"));

        scrollTo(0);
    }

    private void createItemList() {

        if (aktCache == null)
            return;

        allLogs = new CB_List<>();
        for (LogEntry logEntry : Database.getLogs(aktCache)) {
            if (GlobalCore.filterLogsOfFriends) {
                if (!friendList.contains(logEntry.finder)) {
                    continue;
                }
            }
            allLogs.add(new LogItem(logEntry));
        }
        // notifyDataSetChanged();
    }

    private CB_RectF getItemRect_F(LogEntry logEntry) {
        CB_RectF cbRectF = new CB_RectF(itemRec);
        float margin = UiSizes.getInstance().getMargin();
        float headHeight = (UiSizes.getInstance().getButtonHeight() / 1.5f) + margin;
        float measuredWidth = itemRec.getWidth() - ListViewItemBackground.getLeftWidthStatic() - ListViewItemBackground.getRightWidthStatic() - (margin * 2);
        float commentHeight = (margin * 4) + Fonts.MeasureWrapped(logEntry.logText, measuredWidth).height;
        cbRectF.setHeight(headHeight + commentHeight);
        return cbRectF;
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
        setAdapter(null);
        aktCache = null;
        logListAdapter = null;
        super.dispose();
    }

    private static class LogItem {
        LogEntry logEntry;
        LogViewItem logViewItem;

        public LogItem(LogEntry logEntry) {
            this.logEntry = logEntry;
            logViewItem = null;
        }
    }

    public class LogListAdapter implements Adapter {
        int index = 0;

        LogListAdapter() {
        }

        @Override
        public int getCount() {
            if (allLogs != null) {
                return allLogs.size();
            } else {
                return 0;
            }
        }

        @Override
        public ListViewItemBase getView(int position) {
            if (allLogs != null) {
                if (allLogs.size() > 0) {
                    LogItem logItem = allLogs.get(position);
                    if (logItem.logViewItem == null) {
                        logItem.logViewItem = new LogViewItem(getItemRect_F(logItem.logEntry), index++, logItem.logEntry);
                    }
                    return logItem.logViewItem;
                }
            }
            return null;
        }

        @Override
        public float getItemSize(int position) {
            if (allLogs != null) {
                if (allLogs.size() > 0) {
                    LogItem logItem = allLogs.get(position);
                    return getItemRect_F(logItem.logEntry).getHeight();
                }
            }
            return 0;
        }
    }
}
