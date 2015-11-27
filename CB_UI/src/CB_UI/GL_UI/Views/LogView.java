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

import java.util.ArrayList;

import org.slf4j.LoggerFactory;

import CB_Core.DB.Database;
import CB_Core.Types.Cache;
import CB_Core.Types.LogEntry;
import CB_Core.Types.Waypoint;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.Config;
import CB_UI.GlobalCore;
import CB_UI.Events.SelectedCacheEvent;
import CB_UI.Events.SelectedCacheEventList;
import CB_UI_Base.GL_UI.Fonts;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.SpriteCacheBase;
import CB_UI_Base.GL_UI.Controls.List.Adapter;
import CB_UI_Base.GL_UI.Controls.List.ListViewItemBackground;
import CB_UI_Base.GL_UI.Controls.List.ListViewItemBase;
import CB_UI_Base.GL_UI.Controls.List.V_ListView;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.UI_Size_Base;
import CB_Utils.Lists.CB_List;

public class LogView extends V_ListView implements SelectedCacheEvent {
    final static org.slf4j.Logger log = LoggerFactory.getLogger(LogView.class);
    public static CB_RectF ItemRec;
    public static LogView that;

    public LogView(CB_RectF rec, String Name) {
	super(rec, Name);
	that = this;
	ItemRec = (new CB_RectF(0, 0, this.getWidth(), UI_Size_Base.that.getButtonHeight() * 1.1f)).ScaleCenter(0.97f);
	setBackground(SpriteCacheBase.ListBack);

	this.setBaseAdapter(null);
	SetSelectedCache(GlobalCore.getSelectedCache(), GlobalCore.getSelectedWaypoint());
	this.setDisposeFlag(false);
    }

    @Override
    public void onShow() {
	// if Tab register for Cache Changed Event
	if (GlobalCore.isTab) {
	    SelectedCacheEventList.Add(this);
	}

	SetSelectedCache(GlobalCore.getSelectedCache(), GlobalCore.getSelectedWaypoint());

	resetInitial();
    }

    @Override
    public void onHide() {
	SelectedCacheEventList.Remove(this);
    }

    @Override
    public void Initial() {
	super.Initial();

	createItemList(aktCache);

	this.setBaseAdapter(null);
	lvAdapter = new CustomAdapter();
	this.setBaseAdapter(lvAdapter);

	this.setEmptyMsg(Translation.Get("EmptyLogList"));

	this.scrollTo(0);
    }

    @Override
    protected void SkinIsChanged() {

    }

    Cache aktCache;
    CustomAdapter lvAdapter;

    CB_List<LogViewItem> itemList;

    private void createItemList(Cache cache) {
	if (itemList == null)
	    itemList = new CB_List<LogViewItem>();
	itemList.clear();

	if (cache == null)
	    return; // Kein Cache angewählt

	CB_List<LogEntry> cleanLogs = new CB_List<LogEntry>();
	cleanLogs = Database.Logs(cache);// cache.Logs();

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

    public class CustomAdapter implements Adapter {
	public CustomAdapter() {
	}

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

    public void SetSelectedCache(Cache cache, Waypoint waypoint) {
	Cache c = cache;

	if (aktCache != c) {
	    aktCache = c;
	}

	resetInitial();
    }

    @Override
    public void SelectedCacheChanged(Cache cache, Waypoint waypoint) {
	SetSelectedCache(cache, waypoint);
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
	log.debug("LogView disposed");
    }
}
