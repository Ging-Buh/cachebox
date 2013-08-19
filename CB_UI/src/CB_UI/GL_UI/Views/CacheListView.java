package CB_UI.GL_UI.Views;

import java.util.Timer;
import java.util.TimerTask;

import CB_Core.DB.Database;
import CB_Core.Events.CachListChangedEventList;
import CB_Core.Events.CacheListChangedEventListner;
import CB_Core.Types.Cache;
import CB_Core.Types.CacheList;
import CB_Core.Types.Waypoint;
import CB_Locator.Events.PositionChangedEvent;
import CB_Locator.Events.PositionChangedEventList;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.GlobalCore;
import CB_UI.Events.SelectedCacheEvent;
import CB_UI.Events.SelectedCacheEventList;
import CB_UI.GL_UI.CB_View_Base;
import CB_UI.GL_UI.Fonts;
import CB_UI.GL_UI.GL_View_Base;
import CB_UI.GL_UI.SpriteCache;
import CB_UI.GL_UI.Controls.List.Adapter;
import CB_UI.GL_UI.Controls.List.ListViewItemBase;
import CB_UI.GL_UI.Controls.List.V_ListView;
import CB_UI.GL_UI.Controls.PopUps.SearchDialog;
import CB_UI.GL_UI.GL_Listener.GL;
import CB_UI.GL_UI.Menu.CB_AllContextMenuHandler;
import CB_UI.Math.CB_RectF;
import CB_UI.Math.UiSizes;
import CB_Utils.Log.Logger;

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
		SelectedCacheEventList.Add(this);
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
		try
		{
			if (lvAdapter == null || lvAdapter.getCount() == 0)
			{
				if (emptyMsg == null)
				{
					emptyMsg = new BitmapFontCache(Fonts.getBig());
					TextBounds bounds = emptyMsg.setWrappedText(Translation.Get("EmptyCacheList"), 0, 0, this.width);
					emptyMsg.setPosition(this.halfWidth - (bounds.width / 2), this.halfHeight - (bounds.height / 2));
				}
				if (emptyMsg != null) emptyMsg.draw(batch, 0.5f);
			}
			else
			{
				super.render(batch);
			}
		}
		catch (Exception e)
		{
			if (emptyMsg == null)
			{
				emptyMsg = new BitmapFontCache(Fonts.getBig());
				TextBounds bounds = emptyMsg.setWrappedText(Translation.Get("EmptyCacheList"), 0, 0, this.width);
				emptyMsg.setPosition(this.halfWidth - (bounds.width / 2), this.halfHeight - (bounds.height / 2));
			}
			if (emptyMsg != null) emptyMsg.draw(batch, 0.5f);
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

		PositionChangedEventList.Add(this);

		synchronized (Database.Data.Query)
		{
			try
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
			catch (Exception e)
			{
				e.printStackTrace();
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
				listView.chkSlideBack();
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
					if (listView.isDrageble())
					{
						if (!(first <= id && last >= id) || (searchPlaceholder < 0)) listView.scrollToItem(id - pos);
					}
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
		PositionChangedEventList.Remove(this);

		if (searchPlaceholder < 0)
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
				if (waypoint == null) waypoint = cache.GetStartWaypoint();
				GlobalCore.setSelectedWaypoint(cache, waypoint);
			}
			listView.setSelection(selectionIndex);
			return true;
		}
	};

	private OnClickListener onItemLongClickListner = new OnClickListener()
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
			Waypoint finalWp = null;
			if (cache.HasFinalWaypoint()) finalWp = cache.GetFinalWaypoint();
			if (finalWp == null) finalWp = cache.GetStartWaypoint();
			// shutdown AutoResort when selecting a cache by hand
			GlobalCore.setAutoResort(false);
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
			synchronized (cacheList)
			{
				this.cacheList = cacheList;

				Count = cacheList.size();
			}
			Logger.DEBUG("CacheListView ctor CustomAdapter " + Count + " Caches");
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

				CacheListViewItem v = new CacheListViewItem(UiSizes.that.getCacheListItemRec().asFloat(), position, cache);
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

				// alle Items haben die gleiche Größe (Höhe)
				return UiSizes.that.getCacheListItemRec().getHeight();
			}

		}

	}

	@Override
	public void CacheListChangedEvent()
	{
		Logger.DEBUG("CacheListChangedEvent on Cache List");
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
	public void PositionChanged()
	{
		GL.that.renderOnce("Core.CacheListView");
	}

	@Override
	public void OrientationChanged()
	{
		GL.that.renderOnce("Core.CacheListView");
	}

	@Override
	public String getReceiverName()
	{
		return "Core.CacheListView";
	}

	@Override
	public void onResized(CB_RectF rec)
	{
		super.onResized(rec);
		listView.setSize(rec);
		listView.setHeight(rec.getHeight() + searchPlaceholder);
		listView.setZeroPos();
	}

	private float searchPlaceholder = 0;

	public void setTopPlaceHolder(float PlaceHoldHeight)
	{
		searchPlaceholder = -PlaceHoldHeight;
		onResized(this);
	}

	public void resetPlaceHolder()
	{
		searchPlaceholder = 0;
		onResized(this);
	}

	public V_ListView getListView()
	{
		return listView;
	}

	@Override
	public Priority getPriority()
	{
		return Priority.Normal;
	}

	@Override
	public void SpeedChanged()
	{
	}

}
