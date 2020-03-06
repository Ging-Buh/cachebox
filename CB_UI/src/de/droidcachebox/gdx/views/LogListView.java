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
import de.droidcachebox.menu.ViewManager;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.CB_List;

import java.util.ArrayList;
import java.util.Collections;

public class LogListView extends V_ListView implements SelectedCacheChangedEventListener {
    private static CB_RectF itemRec;
    private static LogListView logListView;
    CB_List<LogItem> logs;
    private Cache aktCache;
    private LogListViewAdapter logListViewAdapter;
    private ArrayList<String> friendList;

    private LogListView() {
        super(ViewManager.leftTab.getContentRec(), "LogListView");
        setForceHandleTouchEvents();
        itemRec = (new CB_RectF(0, 0, getWidth(), UiSizes.getInstance().getButtonHeight() * 1.1f)).scaleCenter(0.97f);
        setBackground(Sprites.ListBack);

        createFriendList();
        Config.friends.addSettingChangedListener(this::createFriendList);

        setDisposeFlag(false);
        setCache(GlobalCore.getSelectedCache());

    }

    private void createFriendList() {
        String friends = Config.friends.getValue().replace(", ", "|").replace(",", "|");
        friendList = new ArrayList<>();
        Collections.addAll(friendList, friends.split("\\|"));
    }

    public static LogListView getInstance() {
        if (logListView == null) logListView = new LogListView();
        return logListView;
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
        aktCache = null;
        setCache(GlobalCore.getSelectedCache());
    }

    private CB_RectF getItemRect_F(LogEntry logEntry) {
        CB_RectF cbRectF = new CB_RectF(itemRec);
        float margin = UiSizes.getInstance().getMargin();
        float headHeight = (UiSizes.getInstance().getButtonHeight() / 1.5f) + margin;
        float measuredWidth = itemRec.getWidth() - ListViewItemBackground.getLeftWidthStatic() - ListViewItemBackground.getRightWidthStatic() - (margin * 2);
        float commentHeight = (margin * 4) + Fonts.measureWrapped(logEntry.logText, measuredWidth).height;
        cbRectF.setHeight(headHeight + commentHeight);
        return cbRectF;
    }

    public void setCache(Cache cache) {
        if (cache == null) {
            setAdapter(null);
            logs = new CB_List<>();
            setEmptyMsgBmpFntCah(Translation.get("EmptyLogList"));
            return;
        }
        if (aktCache != cache) {
            aktCache = cache;
            setAdapter(null);
            logs = new CB_List<>();
            setEmptyMsgBmpFntCah(Translation.get("EmptyLogList"));
            for (LogEntry logEntry : Database.getLogs(aktCache)) {
                if (GlobalCore.filterLogsOfFriends) {
                    if (!friendList.contains(logEntry.finder)) {
                        continue;
                    }
                }
                logs.add(new LogItem(logEntry));
            }
            logListViewAdapter = new LogListViewAdapter();
            setAdapter(logListViewAdapter);
            scrollTo(0);
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
        logListViewAdapter = null;
        super.dispose();
    }

    private static class LogItem {
        LogEntry logEntry;
        LogListViewItem logListViewItem;

        public LogItem(LogEntry logEntry) {
            this.logEntry = logEntry;
            logListViewItem = null;
        }
    }

    private class LogListViewAdapter implements Adapter {
        int index = 0;

        LogListViewAdapter() {
        }

        @Override
        public int getCount() {
            if (logs != null) {
                return logs.size();
            } else {
                return 0;
            }
        }

        @Override
        public ListViewItemBase getView(int position) {
            if (logs != null) {
                if (logs.size() > 0) {
                    LogItem logItem = logs.get(position);
                    if (logItem.logListViewItem == null) {
                        logItem.logListViewItem = new LogListViewItem(getItemRect_F(logItem.logEntry), index++, logItem.logEntry);
                    }
                    // todo for not to get stack or heap overflow handle a list to set a old (no longer visible) logListViewItem to null
                    // reducing the list from database is not handled (like in DraftsView)
                    return logItem.logListViewItem;
                }
            }
            return null;
        }

        @Override
        public float getItemSize(int position) {
            if (logs != null) {
                if (logs.size() > 0) {
                    LogItem logItem = logs.get(position);
                    return getItemRect_F(logItem.logEntry).getHeight();
                }
            }
            return 0;
        }
    }
}
