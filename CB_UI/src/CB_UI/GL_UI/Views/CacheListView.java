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

import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.LoggerFactory;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.utils.Align;

import CB_Core.CacheListChangedEventList;
import CB_Core.CacheListChangedEventListener;
import CB_Core.Database;
import CB_Core.Types.Cache;
import CB_Core.Types.CacheList;
import CB_Core.Types.Waypoint;
import CB_Locator.Events.PositionChangedEvent;
import CB_Locator.Events.PositionChangedEventList;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.GlobalCore;
import CB_UI.SelectedCacheEvent;
import CB_UI.SelectedCacheEventList;
import CB_UI.GL_UI.Controls.PopUps.SearchDialog;
import CB_UI.GL_UI.Menu.CB_AllContextMenuHandler;
import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.Fonts;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.IRunOnGL;
import CB_UI_Base.GL_UI.SpriteCacheBase;
import CB_UI_Base.GL_UI.Controls.List.Adapter;
import CB_UI_Base.GL_UI.Controls.List.ListViewBase.IListPosChanged;
import CB_UI_Base.GL_UI.Controls.List.ListViewItemBase;
import CB_UI_Base.GL_UI.Controls.List.Scrollbar;
import CB_UI_Base.GL_UI.Controls.List.V_ListView;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.UiSizes;
import CB_Utils.Math.Point;

public class CacheListView extends CB_View_Base implements CacheListChangedEventListener, SelectedCacheEvent, PositionChangedEvent {
    final static org.slf4j.Logger log = LoggerFactory.getLogger(CacheListView.class);
    private V_ListView listView;
    private Scrollbar scrollBar;

    private CustomAdapter lvAdapter;
    private BitmapFontCache emptyMsg;

    public CacheListView(CB_RectF rec, String Name) {
	super(rec, Name);
	registerSkinChangedEvent();
	CacheListChangedEventList.Add(this);
	SelectedCacheEventList.Add(this);
	listView = new V_ListView(rec, Name);
	listView.setZeroPos();

	listView.addListPosChangedEventHandler(new IListPosChanged() {
	    @Override
	    public void ListPosChanged() {
		scrollBar.ScrollPositionChanged();
	    }
	});
	scrollBar = new Scrollbar(listView);

	this.addChild(listView);
	this.addChild(scrollBar);
    }

    @Override
    public void Initial() {
	// log.debug("CacheListView => Initial()");
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
		    GlyphLayout bounds = emptyMsg.setText(Translation.Get("EmptyCacheList"), 0f, 0f, this.getWidth(), Align.left, true);
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
		GlyphLayout bounds = emptyMsg.setText(Translation.Get("EmptyCacheList"), 0f, 0f, this.getWidth(), Align.left, true);
		emptyMsg.setPosition(this.getHalfWidth() - (bounds.width / 2), this.getHalfHeight() - (bounds.height / 2));
	    }
	    if (emptyMsg != null)
		emptyMsg.draw(batch, 0.5f);
	}
    }

    private Boolean isShown = false;

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
	log.debug("CacheList onShow");
	setBackground(SpriteCacheBase.ListBack);

	PositionChangedEventList.Add(this);

	synchronized (Database.Data.Query) {
	    try {
		lvAdapter = new CustomAdapter(Database.Data.Query);
		listView.setBaseAdapter(lvAdapter);

		int itemCount = Database.Data.Query.size();
		int itemSpace = listView.getMaxItemCount();

		if (itemSpace >= itemCount) {
		    listView.setUndragable();
		} else {
		    listView.setDragable();
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

	listView.RunIfListInitial(new IRunOnGL() {

	    @Override
	    public void run() {
		int id = 0;
		Point firstAndLast = listView.getFirstAndLastVisibleIndex();

		synchronized (Database.Data.Query) {
		    for (int i = 0, n = Database.Data.Query.size(); i < n; i++) {
			Cache ca = Database.Data.Query.get(i);
			if (ca.Id == GlobalCore.getSelectedCache().Id) {
			    listView.setSelection(id);
			    if (listView.isDragable()) {
				if (!(firstAndLast.x <= id && firstAndLast.y >= id)) {
				    listView.scrollToItem(id);
				    log.debug("Scroll to:" + id);
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
			GL.that.RunOnGL(new IRunOnGL() {

			    @Override
			    public void run() {
				if (listView != null)
				    listView.chkSlideBack();
				GL.that.renderOnce();
			    }
			});
		    }
		};

		Timer timer = new Timer();
		timer.schedule(task, 50);
	    }
	});

	GL.that.renderOnce();
    }

    @Override
    public void onHide() {
	isShown = false;
	log.debug("CacheList onHide");
	PositionChangedEventList.Remove(this);

	if (searchPlaceholder < 0) {
	    // Blende Search Dialog aus
	    SearchDialog.that.close();
	}

	lvAdapter = null;
	listView.setBaseAdapter(lvAdapter);
    }

    private OnClickListener onItemClickListener = new OnClickListener() {

	@Override
	public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
	    int selectionIndex = ((ListViewItemBase) v).getIndex();

	    Cache cache;
	    synchronized (Database.Data.Query) {
		cache = Database.Data.Query.get(selectionIndex);
	    }
	    if (cache != null) {
		// Wenn ein Cache einen Final waypoint hat dann soll gleich dieser aktiviert werden
		Waypoint waypoint = cache.GetFinalWaypoint();
		if (waypoint == null)
		    waypoint = cache.GetStartWaypoint();
		GlobalCore.setSelectedWaypoint(cache, waypoint);
	    }
	    listView.setSelection(selectionIndex);
	    setSelectedCacheVisible();
	    return true;
	}
    };

    private OnClickListener onItemLongClickListener = new OnClickListener() {

	@Override
	public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
	    int selectionIndex = ((ListViewItemBase) v).getIndex();

	    Cache cache;
	    synchronized (Database.Data.Query) {
		cache = Database.Data.Query.get(selectionIndex);
	    }
	    Waypoint finalWp = null;
	    if (cache.HasFinalWaypoint())
		finalWp = cache.GetFinalWaypoint();
	    if (finalWp == null)
		finalWp = cache.GetStartWaypoint();
	    // shutdown AutoResort when selecting a cache by hand
	    GlobalCore.setAutoResort(false);
	    GlobalCore.setSelectedWaypoint(cache, finalWp);

	    invalidate();
	    CB_AllContextMenuHandler.showBtnCacheContextMenu();
	    return true;
	}
    };

    public class CustomAdapter implements Adapter {
	private CacheList cacheList;

	private int Count = 0;

	public CustomAdapter(CacheList cacheList) {
	    synchronized (cacheList) {
		this.cacheList = cacheList;

		Count = cacheList.size();
	    }
	    log.debug("CacheListView ctor CustomAdapter " + Count + " Caches");
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

		CacheListViewItem v = new CacheListViewItem(UiSizes.that.getCacheListItemRec().asFloat(), position, cache);
		v.setClickable(true);
		v.setOnClickListener(onItemClickListener);
		v.setOnLongClickListener(onItemLongClickListener);

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
		return UiSizes.that.getCacheListItemRec().getHeight();
	    }

	}

	public void dispose() {
	    cacheList = null;
	}

    }

    @Override
    public void CacheListChangedEvent() {
	log.debug("CacheListChangedEvent on Cache List");
	listView.setBaseAdapter(null);
	synchronized (Database.Data.Query) {
	    lvAdapter = new CustomAdapter(Database.Data.Query);

	    listView.setBaseAdapter(lvAdapter);

	    int itemCount = Database.Data.Query.size();
	    int itemSpace = listView.getMaxItemCount();

	    if (itemSpace >= itemCount) {
		listView.setUndragable();
	    } else {
		listView.setDragable();
	    }
	}

	if (GlobalCore.isSetSelectedCache()) {
	    boolean diverend = true;

	    try {
		diverend = GlobalCore.getSelectedCache().Id != ((CacheListViewItem) listView.getSelectedItem()).getCache().Id;
	    } catch (Exception e) {
	    }

	    if (diverend) {
		setSelectedCacheVisible();
	    }
	}

	listView.chkSlideBack();
    }

    @Override
    public void SelectedCacheChanged(Cache cache, Waypoint waypoint) {
	if (GlobalCore.isSetSelectedCache()) {
	    CacheListViewItem selItem = (CacheListViewItem) listView.getSelectedItem();
	    if (selItem != null && GlobalCore.getSelectedCache().Id != selItem.getCache().Id) {
		// TODO Run if ListView Initial and after showing
		listView.RunIfListInitial(new IRunOnGL() {

		    @Override
		    public void run() {
			setSelectedCacheVisible();
		    }
		});

	    }
	}
    }

    @Override
    protected void SkinIsChanged() {
	if (listView != null)
	    listView.reloadItems();
	setBackground(SpriteCacheBase.ListBack);
	CacheListViewItem.ResetBackground();
    }

    @Override
    public void PositionChanged() {
	GL.that.renderOnce();
    }

    @Override
    public void OrientationChanged() {
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

    private float searchPlaceholder = 0;

    public void setTopPlaceHolder(float PlaceHoldHeight) {
	searchPlaceholder = -PlaceHoldHeight;
	onResized(this);
    }

    public void resetPlaceHolder() {
	searchPlaceholder = 0;
	onResized(this);
    }

    public V_ListView getListView() {
	return listView;
    }

    @Override
    public Priority getPriority() {
	return Priority.Normal;
    }

    @Override
    public void SpeedChanged() {
    }

    @Override
    public void dispose() {

	onItemLongClickListener = null;
	onItemClickListener = null;

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

	CacheListChangedEventList.Remove(this);
	SelectedCacheEventList.Remove(this);
	PositionChangedEventList.Remove(this);

	super.dispose();
    }
}
