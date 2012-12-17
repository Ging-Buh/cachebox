package CB_Core.GL_UI.Views;

import java.util.Timer;
import java.util.TimerTask;

import CB_Core.GlobalCore;
import CB_Core.DB.Database;
import CB_Core.Events.CachListChangedEventList;
import CB_Core.Events.CacheListChangedEventListner;
import CB_Core.Events.PositionChangedEvent;
import CB_Core.Events.PositionChangedEventList;
import CB_Core.Events.SelectedCacheEvent;
import CB_Core.Events.SelectedCacheEventList;
import CB_Core.GL_UI.CB_View_Base;
import CB_Core.GL_UI.Fonts;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Controls.List.Adapter;
import CB_Core.GL_UI.Controls.List.ListViewItemBase;
import CB_Core.GL_UI.Controls.List.V_ListView;
import CB_Core.GL_UI.Controls.PopUps.SearchDialog;
import CB_Core.GL_UI.GL_Listener.GL;
import CB_Core.GL_UI.Menu.CB_AllContextMenuHandler;
import CB_Core.Locator.Locator;
import CB_Core.Log.Logger;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.UiSizes;
import CB_Core.Types.Cache;
import CB_Core.Types.CacheList;
import CB_Core.Types.Waypoint;

import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class CacheListView extends CB_View_Base implements CacheListChangedEventListner, SelectedCacheEvent, PositionChangedEvent
{

	V_ListView listView;

	public static CacheListView that;
	private CustomAdapter lvAdapter;
	private BitmapFontCache emptyMsg;

	public CacheListView(CB_RectF rec, String Name)
	{
		super(rec, Name);
		registerSkinChangedEvent();
		CachListChangedEventList.Add(this);
		that = this;
		listView = new V_ListView(rec, Name);
		listView.setZeroPos();
		this.addChild(listView);
	}

	@Override
	public void Initial()
	{
		// Logger.LogCat("CacheListView => Initial()");
		// this.setListPos(0, false);
		listView.chkSlideBack();
		GL.that.renderOnce(this.getName() + " Initial()");
	}

	@Override
	public void render(SpriteBatch batch)
	{
		// if Track List empty, draw empty Msg
		if (lvAdapter == null || lvAdapter.getCount() == 0)
		{
			if (emptyMsg == null)
			{
				emptyMsg = new BitmapFontCache(Fonts.getBig());
				TextBounds bounds = emptyMsg.setWrappedText(GlobalCore.Translations.Get("EmptyCacheList"), 0, 0, this.width);
				emptyMsg.setPosition(this.halfWidth - (bounds.width / 2), this.halfHeight - (bounds.height / 2));
			}
			if (emptyMsg != null) emptyMsg.draw(batch, 0.5f);
		}
		else
		{
			super.render(batch);
		}
	}

	private Boolean isShown = false;

	@Override
	public void onShow()
	{

		if (isShown) return;

		if (searchPlaceholder > 0)
		{
			// Blende Search Dialog wieder ein
			if (SearchDialog.that != null) SearchDialog.that.showNotCloseAutomaticly();
		}

		isShown = true;
		Logger.LogCat("CacheList onShow");
		setBackground(SpriteCache.ListBack);

		CachListChangedEventList.Add(this);
		SelectedCacheEventList.Add(this);
		PositionChangedEventList.Add(this);

		synchronized (Database.Data.Query)
		{
			lvAdapter = new CustomAdapter(Database.Data.Query);
			listView.setBaseAdapter(lvAdapter);

			int itemCount = Database.Data.Query.size();
			int itemSpace = listView.getMaxItemCount();

			if (itemSpace >= itemCount)
			{
				listView.setUndragable();
			}
			else
			{
				listView.setDragable();
			}
		}
		TimerTask task = new TimerTask()
		{
			@Override
			public void run()
			{
				// aktuellen Cache in der List anzeigen
				if (GlobalCore.getSelectedCache() != null)
				{
					setSelectedCacheVisible();

				}
				else
					listView.setSelection(0);

				resetInitial();
			}
		};

		Timer timer = new Timer();
		timer.schedule(task, 150);

		GL.that.renderOnce(this.getName() + " onShow()");
	}

	/**
	 * setzt den Aktuell selectierten Cache an die 2. Pos in der Liste
	 */
	public void setSelectedCacheVisible()
	{
		int centerList = listView.getMaxItemCount() / 2;
		setSelectedCacheVisible(-centerList);
	}

	/**
	 * setzt den Aktuell selectierten Cache an pos
	 * 
	 * @param pos
	 */
	public void setSelectedCacheVisible(int pos)
	{
		if (!listView.isDrageble()) return;
		int id = 0;
		int first = listView.getFirstVisiblePosition();
		int last = listView.getLastVisiblePosition();

		synchronized (Database.Data.Query)
		{
			for (Cache ca : Database.Data.Query)
			{
				if (ca == GlobalCore.getSelectedCache())
				{
					listView.setSelection(id);
					if (!(first <= id && last >= id)) listView.scrollToItem(id - pos);
					break;
				}
				id++;
			}
		}

		listView.chkSlideBack();
		GL.that.renderOnce(this.getName() + " setSelectedCachVisible");
	}

	@Override
	public void onHide()
	{
		isShown = false;
		Logger.LogCat("CacheList onHide");
		SelectedCacheEventList.Remove(this);
		CachListChangedEventList.Remove(this);
		PositionChangedEventList.Remove(this);

		if (searchPlaceholder > 0)
		{
			// Blende Search Dialog aus
			SearchDialog.that.close();
		}

		lvAdapter = null;
		listView.setBaseAdapter(lvAdapter);
	}

	private OnClickListener onItemClickListner = new OnClickListener()
	{

		@Override
		public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
		{
			int selectionIndex = ((ListViewItemBase) v).getIndex();

			Cache cache;
			synchronized (Database.Data.Query)
			{
				cache = Database.Data.Query.get(selectionIndex);
			}
			if (cache != null)
			{
				// Wenn ein Cache einen Final waypoint hat dann soll gleich dieser aktiviert werden
				Waypoint waypoint = cache.GetFinalWaypoint();
				GlobalCore.setSelectedWaypoint(cache, waypoint);
			}
			listView.setSelection(selectionIndex);
			return true;
		}
	};

	private OnLongClickListener onItemLongClickListner = new OnLongClickListener()
	{

		@Override
		public boolean onLongClick(GL_View_Base v, int x, int y, int pointer, int button)
		{
			int selectionIndex = ((ListViewItemBase) v).getIndex();

			Cache cache;
			synchronized (Database.Data.Query)
			{
				cache = Database.Data.Query.get(selectionIndex);
			}
			Waypoint finalWp = null;
			if (cache.HasFinalWaypoint()) finalWp = cache.GetFinalWaypoint();
			// shutdown AutoResort when selecting a cache by hand
			GlobalCore.autoResort = false;
			GlobalCore.setSelectedWaypoint(cache, finalWp);

			invalidate();
			CB_AllContextMenuHandler.showBtnCacheContextMenu();
			return true;
		}
	};

	public class CustomAdapter implements Adapter
	{
		private CacheList cacheList;

		private int Count = 0;

		public CustomAdapter(CacheList cacheList)
		{
			Logger.DEBUG("CacheList new Custom Adapter");
			synchronized (cacheList)
			{
				this.cacheList = cacheList;

				Count = cacheList.size();
			}

		}

		public int getCount()
		{
			if (cacheList == null) return 0;

			return Count;
		}

		// public Object getItem(int position)
		// {
		// if (cacheList == null) return null;
		// return cacheList.get(position);
		// }

		@Override
		public ListViewItemBase getView(int position)
		{
			if (cacheList == null) return null;

			synchronized (cacheList)
			{
				Cache cache = cacheList.get(position);

				if (!cache.isSearchVisible()) return null;

				CacheListViewItem v = new CacheListViewItem(UiSizes.getCacheListItemRec().asFloat(), position, cache);
				v.setClickable(true);
				v.setOnClickListener(onItemClickListner);
				v.setOnLongClickListener(onItemLongClickListner);

				return v;
			}

		}

		@Override
		public float getItemSize(int position)
		{
			if (cacheList == null) return 0;

			synchronized (cacheList)
			{
				if (cacheList.size() == 0) return 0;
				Cache cache = cacheList.get(position);

				if (!cache.isSearchVisible()) return 0;

				// alle Items haben die gleiche Gr��e (H�he)
				return UiSizes.getCacheListItemRec().getHeight();
			}

		}

	}

	@Override
	public void CacheListChangedEvent()
	{
		Logger.DEBUG("CacheListChangetEvent on Cache List");
		listView.setBaseAdapter(null);
		synchronized (Database.Data.Query)
		{
			lvAdapter = new CustomAdapter(Database.Data.Query);

			listView.setBaseAdapter(lvAdapter);

			int itemCount = Database.Data.Query.size();
			int itemSpace = listView.getMaxItemCount();

			if (itemSpace >= itemCount)
			{
				listView.setUndragable();
			}
			else
			{
				listView.setDragable();
			}
		}
		listView.chkSlideBack();
	}

	@Override
	public void SelectedCacheChanged(Cache cache, Waypoint waypoint)
	{
		if (GlobalCore.getSelectedCache() != null)
		{
			setSelectedCacheVisible();
		}
	}

	@Override
	protected void SkinIsChanged()
	{
		listView.reloadItems();
		setBackground(SpriteCache.ListBack);
		CacheListViewItem.ResetBackground();
	}

	@Override
	public void PositionChanged(Locator locator)
	{
		GL.that.renderOnce("Core.CacheListView");
	}

	@Override
	public void OrientationChanged(float heading)
	{
		GL.that.renderOnce("Core.CacheListView");
	}

	@Override
	public String getReceiverName()
	{
		return "Core.CacheListView";
	}

	@Override
	public void onRezised(CB_RectF rec)
	{
		super.onRezised(rec);
		listView.setSize(rec);
		listView.setHeight(rec.getHeight() - searchPlaceholder);
		listView.setZeroPos();
	}

	private float searchPlaceholder = 0;

	public void setTopPlaceHolder(float PlaceHoldHeight)
	{
		searchPlaceholder = PlaceHoldHeight;
		onRezised(this);
	}

	public void resetPlaceHolder()
	{
		searchPlaceholder = 0;
		onRezised(this);
	}

	public V_ListView getListView()
	{
		return listView;
	}

}
