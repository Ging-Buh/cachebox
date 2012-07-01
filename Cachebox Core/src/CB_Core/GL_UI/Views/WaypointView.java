package CB_Core.GL_UI.Views;

import CB_Core.GlobalCore;
import CB_Core.Plattform;
import CB_Core.DAO.WaypointDAO;
import CB_Core.DB.Database;
import CB_Core.Enums.CacheTypes;
import CB_Core.Events.SelectedCacheEvent;
import CB_Core.Events.SelectedCacheEventList;
import CB_Core.Events.platformConector;
import CB_Core.GL_UI.Fonts;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.ViewConst;
import CB_Core.GL_UI.Activitys.ActivityBase;
import CB_Core.GL_UI.Activitys.EditWaypoint;
import CB_Core.GL_UI.Activitys.EditWaypoint.ReturnListner;
import CB_Core.GL_UI.Controls.Label;
import CB_Core.GL_UI.Controls.List.Adapter;
import CB_Core.GL_UI.Controls.List.ListViewItemBase;
import CB_Core.GL_UI.Controls.List.V_ListView;
import CB_Core.GL_UI.Controls.MessageBox.GL_MsgBox;
import CB_Core.GL_UI.Controls.MessageBox.GL_MsgBox.OnMsgBoxClickListener;
import CB_Core.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_Core.GL_UI.Controls.MessageBox.MessageBoxIcon;
import CB_Core.GL_UI.Menu.Menu;
import CB_Core.GL_UI.Menu.MenuItem;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.UiSizes;
import CB_Core.Types.Cache;
import CB_Core.Types.Coordinate;
import CB_Core.Types.Waypoint;

public class WaypointView extends V_ListView implements SelectedCacheEvent
{
	CustomAdapter lvAdapter;

	public Waypoint aktWaypoint = null;
	boolean createNewWaypoint = false;
	public Cache aktCache = null;

	public static WaypointView that;

	public WaypointView(CB_RectF rec, String Name)
	{
		super(rec, Name);
		that = this;
		Label lblDummy = new Label(CB_RectF.ScaleCenter(rec, 0.8f), "DummyLabel");
		lblDummy.setFont(Fonts.getNormal());
		lblDummy.setText("Dummy WaypointView");
		setBackground(SpriteCache.ListBack);

		if (GlobalCore.platform == Plattform.Desktop) this.addChild(lblDummy);

		SetSelectedCache(GlobalCore.SelectedCache(), GlobalCore.SelectedWaypoint());
		SelectedCacheEventList.Add(this);
	}

	@Override
	public void onShow()
	{
		// TODO Rufe ANDROID VIEW auf
		platformConector.showView(ViewConst.WAYPOINT_VIEW, this.getX(), this.getY(), this.getWidth(), this.getHeight());

		// aktuellen Waypoint in der List anzeigen
		int first = this.getFirstVisiblePosition();
		int last = this.getLastVisiblePosition();

		if (aktCache == null) return;

		int itemCount = aktCache.waypoints.size() + 1;
		int itemSpace = this.getMaxItemCount();

		if (itemSpace >= itemCount)
		{
			this.setUndragable();
		}
		else
		{
			this.setDragable();
		}

		if (GlobalCore.SelectedWaypoint() != null)
		{
			aktWaypoint = GlobalCore.SelectedWaypoint();
			int id = 0;

			for (Waypoint wp : aktCache.waypoints)
			{
				id++;
				if (wp == aktWaypoint)
				{
					if (!(first < id && last > id)) this.setSelection(id - 2);
					break;
				}
			}
		}
		else
			this.setSelection(0);
	}

	@Override
	public void onHide()
	{
		platformConector.hideView(ViewConst.WAYPOINT_VIEW);
	}

	private OnClickListener onItemClickListner = new OnClickListener()
	{

		@Override
		public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
		{
			int selectionIndex = ((ListViewItemBase) v).getIndex();

			if (selectionIndex == 0)
			{
				// Cache selected
				GlobalCore.SelectedCache(aktCache);
			}
			else
			{
				// waypoint selected
				WaypointViewItem wpi = (WaypointViewItem) v;
				if (wpi != null)
				{
					aktWaypoint = wpi.getWaypoint();
				}
				GlobalCore.SelectedWaypoint(aktCache, aktWaypoint);
			}

			setSelection(selectionIndex);
			return true;
		}
	};

	@Override
	protected void Initial()
	{
		// TODO Auto-generated method stub

	}

	@Override
	protected void SkinIsChanged()
	{
		// TODO Auto-generated method stub

	}

	public class CustomAdapter implements Adapter
	{
		private Cache cache;

		public CustomAdapter(Cache cache)
		{
			this.cache = cache;
		}

		public void setCache(Cache cache)
		{
			this.cache = cache;

		}

		public int getCount()
		{
			if (cache != null) return cache.waypoints.size() + 1;
			else
				return 0;
		}

		public Object getItem(int position)
		{
			if (cache != null)
			{
				if (position == 0) return cache;
				else
					return cache.waypoints.get(position - 1);
			}
			else
				return null;
		}

		public long getItemId(int position)
		{
			return position;
		}

		@Override
		public ListViewItemBase getView(int position)
		{
			if (cache != null)
			{
				if (position == 0)
				{
					WaypointViewItem v = new WaypointViewItem(UiSizes.getCacheListItemRec().asFloat(), position, cache, null);
					v.setClickable(true);
					v.setOnClickListener(onItemClickListner);
					return v;
				}
				else
				{
					Waypoint waypoint = cache.waypoints.get(position - 1);
					WaypointViewItem v = new WaypointViewItem(UiSizes.getCacheListItemRec().asFloat(), position, cache, waypoint);
					v.setClickable(true);
					v.setOnClickListener(onItemClickListner);
					return v;
				}
			}
			else
				return null;
		}

		@Override
		public float getItemSize(int position)
		{
			// alle Items haben die gleiche Größe (Höhe)
			return UiSizes.getCacheListItemRec().getHeight();
		}

	}

	public void SetSelectedCache(Cache cache, Waypoint waypoint)
	{
		if (aktCache != cache)
		{
			// Liste nur dann neu Erstellen, wenn der aktuelle Cache geändert
			// wurde
			aktCache = cache;
			this.setBaseAdapter(null);
			lvAdapter = new CustomAdapter(cache);
			this.setBaseAdapter(lvAdapter);

		}
		// aktuellen Waypoint in der List anzeigen
		int first = this.getFirstVisiblePosition();
		int last = this.getLastVisiblePosition();

		if (aktCache == null) return;

		int itemCount = aktCache.waypoints.size() + 1;
		int itemSpace = this.getMaxItemCount();

		if (itemSpace >= itemCount)
		{
			this.setUndragable();
		}
		else
		{
			this.setDragable();
		}

		if (GlobalCore.SelectedWaypoint() != null)
		{
			aktWaypoint = GlobalCore.SelectedWaypoint();
			int id = 0;

			for (Waypoint wp : aktCache.waypoints)
			{
				id++;
				if (wp == aktWaypoint)
				{
					this.setSelection(id);
					if (!(first < id && last > id)) this.scrollToItem(id);
					break;
				}
			}
		}
		else
			this.setSelection(0);
	}

	@Override
	public void SelectedCacheChanged(Cache cache, Waypoint waypoint)
	{
		SetSelectedCache(cache, waypoint);
	}

	private final int MI_EDIT = 0;
	private final int MI_ADD = 1;
	private final int MI_DELETE = 2;
	private final int MI_PROJECTION = 3;
	private final int MI_FROM_GPS = 4;

	public void ShowContextMenu()
	{
		Menu cm = new Menu("CacheListContextMenu");

		cm.setItemClickListner(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				switch (((MenuItem) v).getMenuItemId())
				{
				case MI_ADD:
					addWP();
					return true;
				case MI_EDIT:
					editWP();
					return true;
				case MI_DELETE:
					deleteWP();
					return true;
				}
				return false;
			}
		});

		if (aktWaypoint != null) cm.addItem(MI_EDIT, "edit");
		cm.addItem(MI_ADD, "addWaypoint");
		if ((aktWaypoint != null) && (aktWaypoint.IsUserWaypoint)) cm.addItem(MI_DELETE, "delete");
		if (aktWaypoint != null || aktCache != null) cm.addItem(MI_PROJECTION, "Projection");

		cm.addItem(MI_FROM_GPS, "FromGps");

		cm.show();

	}

	private void addWP()
	{
		createNewWaypoint = true;
		String newGcCode = "";
		try
		{
			newGcCode = Database.CreateFreeGcCode(GlobalCore.SelectedCache().GcCode);
		}
		catch (Exception e)
		{
			return;
		}
		Coordinate coord = GlobalCore.LastValidPosition;
		if ((coord == null) || (!coord.Valid)) coord = GlobalCore.SelectedCache().Pos;
		Waypoint newWP = new Waypoint(newGcCode, CacheTypes.ReferencePoint, "", coord.Latitude, coord.Longitude,
				GlobalCore.SelectedCache().Id, "", GlobalCore.Translations.Get("wyptDefTitle"));
		editWP(newWP);

	}

	private void editWP()
	{
		if (aktWaypoint != null)
		{
			createNewWaypoint = false;
			editWP(aktWaypoint);
		}
	}

	private void editWP(Waypoint wp)
	{

		EditWaypoint EdWp = new EditWaypoint(ActivityBase.ActivityRec(), "EditWP", wp, new ReturnListner()
		{

			@Override
			public void returnedWP(Waypoint waypoint)
			{
				if (waypoint != null)
				{
					if (createNewWaypoint)
					{
						GlobalCore.SelectedCache().waypoints.add(waypoint);
						that.setBaseAdapter(lvAdapter);
						aktWaypoint = waypoint;
						GlobalCore.SelectedWaypoint(GlobalCore.SelectedCache(), waypoint);
						WaypointDAO waypointDAO = new WaypointDAO();
						waypointDAO.WriteToDatabase(waypoint);

						int itemCount = lvAdapter.getCount();
						int itemSpace = that.getMaxItemCount();

						if (itemSpace >= itemCount)
						{
							that.setUndragable();
						}
						else
						{
							that.setDragable();
						}

					}
					else
					{
						aktWaypoint.Title = waypoint.Title;
						aktWaypoint.Type = waypoint.Type;
						aktWaypoint.Pos = waypoint.Pos;
						aktWaypoint.Description = waypoint.Description;
						aktWaypoint.Clue = waypoint.Clue;
						WaypointDAO waypointDAO = new WaypointDAO();
						waypointDAO.UpdateDatabase(aktWaypoint);
						that.setBaseAdapter(lvAdapter);
					}
				}
			}
		});
		EdWp.show();

	}

	private void deleteWP()
	{
		GL_MsgBox.Show(GlobalCore.Translations.Get("?DelWP") + "\n\n[" + aktWaypoint.Title + "]", GlobalCore.Translations.Get("!DelWP"),
				MessageBoxButtons.YesNo, MessageBoxIcon.Question, new OnMsgBoxClickListener()
				{

					@Override
					public boolean onClick(int which)
					{
						switch (which)
						{
						case GL_MsgBox.BUTTON_POSITIVE:
							// Yes button clicked
							Database.DeleteFromDatabase(aktWaypoint);
							GlobalCore.SelectedCache().waypoints.remove(aktWaypoint);
							GlobalCore.SelectedWaypoint(GlobalCore.SelectedCache(), null);
							aktWaypoint = null;
							that.setBaseAdapter(lvAdapter);

							int itemCount = lvAdapter.getCount();
							int itemSpace = that.getMaxItemCount();

							if (itemSpace >= itemCount)
							{
								that.setUndragable();
							}
							else
							{
								that.setDragable();
							}

							that.scrollToItem(0);

							break;
						case GL_MsgBox.BUTTON_NEGATIVE:
							// No button clicked
							break;
						}
						return true;
					}
				});
	}
}
