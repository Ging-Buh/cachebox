package CB_Core.GL_UI.Views;

import CB_Core.GlobalCore;
import CB_Core.DAO.WaypointDAO;
import CB_Core.DB.Database;
import CB_Core.Enums.CacheTypes;
import CB_Core.Events.SelectedCacheEvent;
import CB_Core.Events.SelectedCacheEventList;
import CB_Core.Events.WaypointListChangedEvent;
import CB_Core.Events.WaypointListChangedEventList;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Activitys.ActivityBase;
import CB_Core.GL_UI.Activitys.EditWaypoint;
import CB_Core.GL_UI.Activitys.EditWaypoint.ReturnListner;
import CB_Core.GL_UI.Activitys.MeasureCoordinate;
import CB_Core.GL_UI.Activitys.ProjectionCoordinate;
import CB_Core.GL_UI.Activitys.ProjectionCoordinate.Type;
import CB_Core.GL_UI.Controls.List.Adapter;
import CB_Core.GL_UI.Controls.List.ListViewItemBase;
import CB_Core.GL_UI.Controls.List.V_ListView;
import CB_Core.GL_UI.Controls.MessageBox.GL_MsgBox;
import CB_Core.GL_UI.Controls.MessageBox.GL_MsgBox.OnMsgBoxClickListener;
import CB_Core.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_Core.GL_UI.Controls.MessageBox.MessageBoxIcon;
import CB_Core.GL_UI.Menu.Menu;
import CB_Core.GL_UI.Menu.MenuID;
import CB_Core.GL_UI.Menu.MenuItem;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.UiSizes;
import CB_Core.TranslationEngine.Translation;
import CB_Core.Types.Cache;
import CB_Core.Types.Waypoint;
import CB_Locator.Coordinate;
import CB_Locator.Locator;

public class WaypointView extends V_ListView implements SelectedCacheEvent, WaypointListChangedEvent
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

		setBackground(SpriteCache.ListBack);

		SetSelectedCache(GlobalCore.getSelectedCache(), GlobalCore.getSelectedWaypoint());
		SelectedCacheEventList.Add(this);
		WaypointListChangedEventList.Add(this);
	}

	@Override
	public void onShow()
	{

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

		if (GlobalCore.getSelectedWaypoint() != null)
		{
			aktWaypoint = GlobalCore.getSelectedWaypoint();
			int id = 0;

			for (Waypoint wp : aktCache.waypoints)
			{
				id++;
				if (wp == aktWaypoint)
				{
					if (!(first < id && last > id)) this.setSelection(id);
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
				GlobalCore.setSelectedCache(aktCache);
			}
			else
			{
				// waypoint selected
				WaypointViewItem wpi = (WaypointViewItem) v;
				if (wpi != null)
				{
					aktWaypoint = wpi.getWaypoint();
				}
				GlobalCore.setSelectedWaypoint(aktCache, aktWaypoint);
			}

			setSelection(selectionIndex);
			return true;
		}
	};

	private OnClickListener onItemLongClickListner = new OnClickListener()
	{

		@Override
		public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
		{
			int selectionIndex = ((ListViewItemBase) v).getIndex();

			if (selectionIndex == 0)
			{
				// Cache selected
				GlobalCore.setSelectedCache(aktCache);
			}
			else
			{
				// waypoint selected
				WaypointViewItem wpi = (WaypointViewItem) v;
				if (wpi != null)
				{
					aktWaypoint = wpi.getWaypoint();
				}
				GlobalCore.setSelectedWaypoint(aktCache, aktWaypoint);
			}

			setSelection(selectionIndex);
			getContextMenu().Show();
			return true;
		}
	};

	@Override
	public void Initial()
	{
		super.Initial();
	}

	@Override
	protected void SkinIsChanged()
	{

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
					WaypointViewItem v = new WaypointViewItem(UiSizes.that.getCacheListItemRec().asFloat(), position, cache, null);
					v.setClickable(true);
					v.setOnClickListener(onItemClickListner);
					v.setOnLongClickListener(onItemLongClickListner);
					return v;
				}
				else
				{
					Waypoint waypoint = cache.waypoints.get(position - 1);
					WaypointViewItem v = new WaypointViewItem(UiSizes.that.getCacheListItemRec().asFloat(), position, cache, waypoint);
					v.setClickable(true);
					v.setOnClickListener(onItemClickListner);
					v.setOnLongClickListener(onItemLongClickListner);
					return v;
				}
			}
			else
				return null;
		}

		@Override
		public float getItemSize(int position)
		{
			// alle Items haben die gleiche Gr��e (H�he)
			return UiSizes.that.getCacheListItemRec().getHeight();
		}

	}

	public void SetSelectedCache(Cache cache, Waypoint waypoint)
	{
		if (aktCache != cache)
		{
			// Liste nur dann neu Erstellen, wenn der aktuelle Cache ge�ndert
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

		if (GlobalCore.getSelectedWaypoint() != null)
		{
			aktWaypoint = GlobalCore.getSelectedWaypoint();
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

	@Override
	public void WaypointListChanged(Cache cache)
	{
		if (cache != aktCache) return;
		aktCache = null;
		SetSelectedCache(cache, aktWaypoint);
	}

	public Menu getContextMenu()
	{
		Menu cm = new Menu("CacheListContextMenu");

		cm.addItemClickListner(new OnClickListener()
		{
			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				switch (((MenuItem) v).getMenuItemId())
				{
				case MenuID.MI_ADD:
					addWP();
					return true;
				case MenuID.MI_WP_SHOW:
					editWP(false);
					return true;
				case MenuID.MI_EDIT:
					editWP(true);
					return true;
				case MenuID.MI_DELETE:
					deleteWP();
					return true;
				case MenuID.MI_PROJECTION:
					addProjection();
					return true;
				case MenuID.MI_FROM_GPS:
					addMeasure();
					return true;

				}
				return false;
			}
		});

		if (aktWaypoint != null) cm.addItem(MenuID.MI_WP_SHOW, "show");
		if (aktWaypoint != null) cm.addItem(MenuID.MI_EDIT, "edit");
		cm.addItem(MenuID.MI_ADD, "addWaypoint");
		if ((aktWaypoint != null) && (aktWaypoint.IsUserWaypoint)) cm.addItem(MenuID.MI_DELETE, "delete");
		if (aktWaypoint != null || aktCache != null) cm.addItem(MenuID.MI_PROJECTION, "Projection");

		cm.addItem(MenuID.MI_FROM_GPS, "FromGps");

		return cm;
	}

	public void addWP()
	{
		createNewWaypoint = true;
		String newGcCode = "";
		try
		{
			newGcCode = Database.CreateFreeGcCode(GlobalCore.getSelectedCache().GcCode);
		}
		catch (Exception e)
		{
			return;
		}
		Coordinate coord = GlobalCore.getSelectedCoord();
		if (coord == null) coord = Locator.getCoordinate();
		if ((coord == null) || (!coord.isValid())) coord = GlobalCore.getSelectedCache().Pos;
		Waypoint newWP = new Waypoint(newGcCode, CacheTypes.ReferencePoint, "", coord.getLatitude(), coord.getLongitude(),
				GlobalCore.getSelectedCache().Id, "", Translation.Get("wyptDefTitle"));
		editWP(newWP, true);

	}

	private void editWP(boolean showCoordinateDialog)
	{
		if (aktWaypoint != null)
		{
			createNewWaypoint = false;
			editWP(aktWaypoint, showCoordinateDialog);
		}
	}

	private void editWP(Waypoint wp, boolean showCoordinateDialog)
	{

		EditWaypoint EdWp = new EditWaypoint(wp, new ReturnListner()
		{

			@Override
			public void returnedWP(Waypoint waypoint)
			{
				if (waypoint != null)
				{
					if (createNewWaypoint)
					{

						GlobalCore.getSelectedCache().waypoints.add(waypoint);
						that.setBaseAdapter(lvAdapter);
						aktWaypoint = waypoint;
						GlobalCore.setSelectedWaypoint(GlobalCore.getSelectedCache(), waypoint);
						if (waypoint.IsStart)
						{
							// Es muss hier sichergestellt sein dass dieser Waypoint der einzige dieses Caches ist, der als Startpunkt
							// definiert
							// ist!!!
							WaypointDAO wpd = new WaypointDAO();
							wpd.ResetStartWaypoint(GlobalCore.getSelectedCache(), waypoint);
						}
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
						aktWaypoint.IsStart = waypoint.IsStart;
						aktWaypoint.Clue = waypoint.Clue;

						// set waypoint as UserWaypoint, because waypoint is changed by user
						aktWaypoint.IsUserWaypoint = true;

						if (waypoint.IsStart)
						{
							// Es muss hier sichergestellt sein dass dieser Waypoint der einzige dieses Caches ist, der als Startpunkt
							// definiert
							// ist!!!
							WaypointDAO wpd = new WaypointDAO();
							wpd.ResetStartWaypoint(GlobalCore.getSelectedCache(), aktWaypoint);
						}
						WaypointDAO waypointDAO = new WaypointDAO();
						waypointDAO.UpdateDatabase(aktWaypoint);
						that.setBaseAdapter(lvAdapter);
					}
				}
			}
		}, showCoordinateDialog);
		EdWp.show();

	}

	private void deleteWP()
	{
		GL_MsgBox.Show(Translation.Get("?DelWP") + "\n\n[" + aktWaypoint.Title + "]", Translation.Get("!DelWP"), MessageBoxButtons.YesNo,
				MessageBoxIcon.Question, new OnMsgBoxClickListener()
				{

					@Override
					public boolean onClick(int which, Object data)
					{
						switch (which)
						{
						case GL_MsgBox.BUTTON_POSITIVE:
							// Yes button clicked
							Database.DeleteFromDatabase(aktWaypoint);
							GlobalCore.getSelectedCache().waypoints.remove(aktWaypoint);
							GlobalCore.setSelectedWaypoint(GlobalCore.getSelectedCache(), null);
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

	private void addProjection()
	{
		createNewWaypoint = true;

		final Coordinate coord = (aktWaypoint != null) ? aktWaypoint.Pos : (aktCache != null) ? aktCache.Pos : Locator.getCoordinate();

		ProjectionCoordinate pC = new ProjectionCoordinate(ActivityBase.ActivityRec(), "Projection", coord,
				new CB_Core.GL_UI.Activitys.ProjectionCoordinate.ReturnListner()
				{

					@Override
					public void returnCoord(Coordinate targetCoord, Coordinate startCoord, double Bearing, double distance)
					{
						if (coord == null || targetCoord == null || targetCoord.equals(coord)) return;

						String newGcCode = "";
						try
						{
							newGcCode = Database.CreateFreeGcCode(GlobalCore.getSelectedCache().GcCode);
						}
						catch (Exception e)
						{

							return;
						}
						Waypoint newWP = new Waypoint(newGcCode, CacheTypes.ReferencePoint, "Entered Manually", targetCoord.getLatitude(),
								targetCoord.getLongitude(), GlobalCore.getSelectedCache().Id, "", "projiziert");
						GlobalCore.getSelectedCache().waypoints.add(newWP);
						that.setBaseAdapter(lvAdapter);
						aktWaypoint = newWP;
						GlobalCore.setSelectedWaypoint(GlobalCore.getSelectedCache(), newWP);
						WaypointDAO waypointDAO = new WaypointDAO();
						waypointDAO.WriteToDatabase(newWP);

					}

				}, Type.projetion);

		pC.show();

	}

	private void addMeasure()
	{
		createNewWaypoint = true;

		MeasureCoordinate mC = new MeasureCoordinate(ActivityBase.ActivityRec(), "Projection", new MeasureCoordinate.ReturnListner()
		{

			@Override
			public void returnCoord(Coordinate returnCoord)
			{
				if (returnCoord == null) return;

				String newGcCode = "";
				try
				{
					newGcCode = Database.CreateFreeGcCode(GlobalCore.getSelectedCache().GcCode);
				}
				catch (Exception e)
				{

					return;
				}
				Waypoint newWP = new Waypoint(newGcCode, CacheTypes.ReferencePoint, "Measured", returnCoord.getLatitude(),
						returnCoord.getLongitude(), GlobalCore.getSelectedCache().Id, "", "Measured");
				GlobalCore.getSelectedCache().waypoints.add(newWP);
				that.setBaseAdapter(lvAdapter);
				aktWaypoint = newWP;
				GlobalCore.setSelectedWaypoint(GlobalCore.getSelectedCache(), newWP);
				WaypointDAO waypointDAO = new WaypointDAO();
				waypointDAO.WriteToDatabase(newWP);

			}
		});

		mC.show();

	}

}