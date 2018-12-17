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
package CB_UI.GL_UI.Views;

import CB_Core.Database;
import CB_Core.Types.Cache;
import CB_Core.Types.LogEntry;
import CB_Core.Types.Waypoint;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.Config;
import CB_UI.GlobalCore;
import CB_UI.SelectedCacheEvent;
import CB_UI.SelectedCacheEventList;
import CB_UI_Base.GL_UI.Controls.List.Adapter;
import CB_UI_Base.GL_UI.Controls.List.ListViewItemBackground;
import CB_UI_Base.GL_UI.Controls.List.ListViewItemBase;
import CB_UI_Base.GL_UI.Controls.List.V_ListView;
import CB_UI_Base.GL_UI.Fonts;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.UI_Size_Base;
import CB_Utils.Lists.CB_List;

import java.util.ArrayList;

public class LogView extends V_ListView implements SelectedCacheEvent {
    private static CB_RectF ItemRec;
    public static LogView that;
    private Cache aktCache;
    private ListViewBaseAdapter lvAdapter;
    private CB_List<LogViewItem> itemList;

    public LogView(CB_RectF rec, String Name) {
        super(rec, Name);
        that = this;
        setForceHandleTouchEvents(true);
        ItemRec = (new CB_RectF(0, 0, this.getWidth(), UI_Size_Base.that.getButtonHeight() * 1.1f)).ScaleCenter(0.97f);
        setBackground(Sprites.ListBack);

        this.setBaseAdapter(null);
        setCache(GlobalCore.getSelectedCache());
        this.setDisposeFlag(false);
    }

    @Override
    public void onShow() {
        // if Tab register for Cache Changed Event
        setCache(GlobalCore.getSelectedCache());
    }

    @Override
    public void onHide() {
        SelectedCacheEventList.Remove(this);
    }

    @Override
    public void Initial() {
        // super.Initial(); does nothing at the moment

        createItemList();

        lvAdapter = new ListViewBaseAdapter();
        this.setBaseAdapter(lvAdapter);

        this.setEmptyMsg(Translation.Get("EmptyLogList"));

        this.scrollTo(0);
    }

    private void createItemList() {
        if (itemList == null)
            itemList = new CB_List<LogViewItem>();
        itemList.clear();

        if (aktCache == null)
            return;

        CB_List<LogEntry> cleanLogs = new CB_List<LogEntry>();
        cleanLogs = Database.Logs(aktCache);

        String finders = Config.Friends.getValue();
        String[] finder = finders.split("\\|");
        ArrayList<String> friendList = new ArrayList<String>();
        for (String f : finder) {
            friendList.add(f);
        }

        int index = 0;
        for (int i = 0, n = cleanLogs.size(); i < n; i++) {
            LogEntry logEntry = cleanLogs.get(i);
            if (GlobalCore.filterLogsOfFriends) {
                // nur die Logs der eingetragenen Freunde anzeigen
                if (!friendList.contains(logEntry.Finder)) {
                    continue;
                }
            }
            CB_RectF rec = ItemRec.copy();
            rec.setHeight(MeasureItemHeight(logEntry));
            final LogViewItem v = new LogViewItem(rec, index++, logEntry);

            v.setOnLongClickListener(new OnClickListener() {

                @Override
                public boolean onClick(GL_View_Base view, int x, int y, int pointer, int button) {
                    v.copyToClipboard();
                    GL.that.Toast(Translation.Get("CopyToClipboard"));
                    return true;
                }
            });

            itemList.add(v);
        }

        this.notifyDataSetChanged();

    }

    private float MeasureItemHeight(LogEntry logEntry) {
        // object ist nicht von Dialog abgeleitet, daher
        float margin = UI_Size_Base.that.getMargin();
        float headHeight = (UI_Size_Base.that.getButtonHeight() / 1.5f) + margin;

        float mesurdWidth = ItemRec.getWidth() - ListViewItemBackground.getLeftWidthStatic() - ListViewItemBackground.getRightWidthStatic() - (margin * 2);

        float commentHeight = (margin * 4) + Fonts.MeasureWrapped(logEntry.Comment, mesurdWidth).height;

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
    public void SelectedCacheChanged(Cache cache, Waypoint waypoint) {
        setCache(cache);
    }

    @Override
    public void dispose() {
        this.setBaseAdapter(null);
        aktCache = null;
        lvAdapter = null;
        if (itemList != null)
            itemList.clear();
        itemList = null;
        super.dispose();
        //Log.debug(log, "LogView disposed");
    }

    public class ListViewBaseAdapter implements Adapter {
        public ListViewBaseAdapter() {
        }

        @Override
        public int getCount() {
            if (itemList != null) {
                return itemList.size();
            } else {
                return 0;
            }
        }

        @Override
        public ListViewItemBase getView(int position) {
            if (itemList != null) {
                return itemList.get(position);
            } else
                return null;
        }

        @Override
        public float getItemSize(int position) {
            if (itemList.size() == 0)
                return 0;
            return itemList.get(position).getHeight();
        }

    }
}
