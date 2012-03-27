package CB_Core.GL_UI.Views;

import CB_Core.GlobalCore;
import CB_Core.DB.Database;
import CB_Core.Events.PositionChangedEvent;
import CB_Core.Events.PositionChangedEventList;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.Controls.List.Adapter;
import CB_Core.GL_UI.Controls.List.ListViewItemBase;
import CB_Core.GL_UI.Controls.List.V_ListView;
import CB_Core.GL_UI.Menu.CB_AllContextMenuHandler;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.UiSizes;
import CB_Core.Types.Cache;
import CB_Core.Types.CacheList;
import CB_Core.Types.Locator;
import CB_Core.Types.Waypoint;

public class CacheListView extends V_ListView implements PositionChangedEvent
{
	private CustomAdapter lvAdapter;

	public CacheListView(CB_RectF rec, CharSequence Name)
	{
		super(rec, Name);
		lvAdapter = new CustomAdapter(Database.Data.Query);
		this.setBaseAdapter(lvAdapter);
		this.setDisposeFlag(false);
	}

	@Override
	protected void Initial()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onShow()
	{
		PositionChangedEventList.Add(this);

		// aktuellen Cache in der List anzeigen
		if (GlobalCore.SelectedCache() != null)
		{
			setSelectedCacheVisible();
		}
		else
			this.setSelection(0);
	}

	/**
	 * setzt den Aktuell selectierten Cache an die 2. Pos in der Liste
	 */
	public void setSelectedCacheVisible()
	{
		setSelectedCacheVisible(2);
	}

	/**
	 * setzt den Aktuell selectierten Cache an pos
	 * 
	 * @param pos
	 */
	public void setSelectedCacheVisible(int pos)
	{
		int id = 0;
		int first = this.getFirstVisiblePosition();
		int last = this.getLastVisiblePosition();

		for (Cache ca : Database.Data.Query)
		{
			if (ca == GlobalCore.SelectedCache())
			{
				if (!(first < id && last > id)) this.setSelection(id - pos);
				break;
			}
			id++;
		}
	}

	@Override
	public void onHide()
	{
		PositionChangedEventList.Remove(this);

	}

	@Override
	public void OrientationChanged(float heading)
	{
		this.invalidate();
	}

	private OnClickListener onItemClickListner = new OnClickListener()
	{

		@Override
		public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
		{
			int selectionIndex = ((ListViewItemBase) v).getIndex();

			GlobalCore.SelectedCache(Database.Data.Query.get(selectionIndex));

			setSelection(selectionIndex);
			return true;
		}
	};

	private OnLongClickListener onItemLongClickListner = new OnLongClickListener()
	{

		@Override
		public boolean onLongClick(GL_View_Base v, int x, int y, int pointer, int button)
		{
			int selectionIndex = ((ListViewItemBase) v).getIndex();

			Cache cache = Database.Data.Query.get(selectionIndex);

			Waypoint finalWp = null;
			if (cache.HasFinalWaypoint()) finalWp = cache.GetFinalWaypoint();
			// shutdown AutoResort when selecting a cache by hand
			GlobalCore.autoResort = false;
			GlobalCore.SelectedWaypoint(cache, finalWp);

			invalidate();
			CB_AllContextMenuHandler.showBtnCacheContextMenu();
			return true;
		}
	};

	public class CustomAdapter implements Adapter
	{
		private CacheList cacheList;

		public CustomAdapter(CacheList cacheList)
		{
			this.cacheList = cacheList;
		}

		public int getCount()
		{
			return cacheList.size();
		}

		public Object getItem(int position)
		{
			return cacheList.get(position);
		}

		@Override
		public ListViewItemBase getView(int position)
		{
			Cache cache = cacheList.get(position);
			CacheListViewItem v = new CacheListViewItem(UiSizes.getCacheListItemRec().asFloat(), position, cache);
			v.setClickable(true);
			v.setOnClickListener(onItemClickListner);
			v.setOnLongClickListener(onItemLongClickListner);

			return v;
		}

		@Override
		public float getItemSize(int position)
		{
			// alle Items haben die gleiche Größe (Höhe)
			return UiSizes.getCacheListItemRec().getHeight();
		}

	}

	@Override
	public void PositionChanged(Locator locator)
	{
		this.invalidate();
	}

}
