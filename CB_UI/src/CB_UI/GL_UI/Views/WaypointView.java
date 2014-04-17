package CB_UI.GL_UI.Views;

import CB_Core.DAO.WaypointDAO;
import CB_Core.DB.Database;
import CB_Core.Enums.CacheTypes;
import CB_Core.Types.Cache;
import CB_Core.Types.CacheLite;
import CB_Core.Types.Waypoint;
import CB_Locator.Coordinate;
import CB_Locator.Locator;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.GlobalCore;
import CB_UI.Events.SelectedCacheEvent;
import CB_UI.Events.SelectedCacheEventList;
import CB_UI.Events.WaypointListChangedEvent;
import CB_UI.Events.WaypointListChangedEventList;
import CB_UI.GL_UI.Activitys.EditWaypoint;
import CB_UI.GL_UI.Activitys.EditWaypoint.ReturnListner;
import CB_UI.GL_UI.Activitys.MeasureCoordinate;
import CB_UI.GL_UI.Activitys.ProjectionCoordinate;
import CB_UI.GL_UI.Activitys.ProjectionCoordinate.Type;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.SpriteCacheBase;
import CB_UI_Base.GL_UI.Activitys.ActivityBase;
import CB_UI_Base.GL_UI.Controls.List.Adapter;
import CB_UI_Base.GL_UI.Controls.List.ListViewItemBase;
import CB_UI_Base.GL_UI.Controls.List.V_ListView;
import CB_UI_Base.GL_UI.Controls.MessageBox.GL_MsgBox;
import CB_UI_Base.GL_UI.Controls.MessageBox.GL_MsgBox.OnMsgBoxClickListener;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxIcon;
import CB_UI_Base.GL_UI.Menu.Menu;
import CB_UI_Base.GL_UI.Menu.MenuID;
import CB_UI_Base.GL_UI.Menu.MenuItem;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.UiSizes;
import CB_Utils.Log.Logger;
import CB_Utils.Math.Point;

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

		setBackground(SpriteCacheBase.ListBack);

		SetSelectedCache(GlobalCore.getSelectedCache(), GlobalCore.getSelectedWaypoint());
		SelectedCacheEventList.Add(this);
		WaypointListChangedEventList.Add(this);
	}

	@Override
	public void onShow()
	{

		// // aktuellen Waypoint in der List anzeigen
		// int first = this.getFirstVisiblePosition();
		// int last = this.getLastVisiblePosition();
		//
		// if (aktCache == null) return;
		//
		// int itemCount = aktCache.waypoints.size() + 1;
		// int itemSpace = this.getMaxItemCount();
		//
		// if (itemSpace >= itemCount)
		// {
		// this.setUndragable();
		// }
		// else
		// {
		// this.setDragable();
		// }
		//
		// if (GlobalCore.getSelectedWaypoint() != null)
		// {
		// aktWaypoint = GlobalCore.getSelectedWaypoint();
		// int id = 0;
		//
		// for (Waypoint wp : aktCache.waypoints)
		// {
		// id++;
		// if (wp == aktWaypoint)
		// {
		// if (!(first < id && last > id)) this.setSelection(id);
		// break;
		// }
		// }
		// }
		// else
		// this.setSelection(0);

		SetSelectedCache(aktCache, aktWaypoint);
		chkSlideBack();

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
			if (cache != null && cache.waypoints != null) return cache.waypoints.size() + 1;
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
			// alle Items haben die gleiche Größe (Höhe)
			return UiSizes.that.getCacheListItemRec().getHeight();
		}

	}

	public void SetSelectedCache(CacheLite cache, Waypoint waypoint)
	{

		if (aktCache == null || (aktCache != null && aktCache.Id != cache.Id))
		{
			// Liste nur dann neu Erstellen, wenn der aktuelle Cache geändert
			// wurde
			aktCache = GlobalCore.getSelectedCache();
			this.setBaseAdapter(null);
			lvAdapter = new CustomAdapter(aktCache);
			this.setBaseAdapter(lvAdapter);

		}
		else
		{

		}

		// aktuellen Waypoint in der List anzeigen

		Point lastAndFirst = this.getFirstAndLastVisibleIndex();

		Logger.DEBUG("[Waypoint Select]");
		try
		{
			Logger.DEBUG("First visible:[" + lastAndFirst.x + "]" + this.lvAdapter.getItem(lastAndFirst.x).toString());
		}
		catch (Exception e)
		{
			Logger.DEBUG("no firstItem with index :" + lastAndFirst.x);
		}
		try
		{
			Logger.DEBUG("Last visible:[" + lastAndFirst.y + "]" + this.lvAdapter.getItem(lastAndFirst.y).toString());
		}
		catch (Exception e)
		{
			Logger.DEBUG("no lastItem with index :" + lastAndFirst.y);
		}

		if (aktCache == null) return;

		int itemCount = aktCache.waypoints == null ? 1 : aktCache.waypoints.size() + 1;
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

			if (aktWaypoint != null && aktWaypoint.equals(GlobalCore.getSelectedWaypoint()))
			{
				// is selected
				return;
			}

			aktWaypoint = GlobalCore.getSelectedWaypoint();
			int id = 0;

			for (int i = 0, n = aktCache.waypoints.size(); i < n; i++)
			{
				Waypoint wp = aktCache.waypoints.get(i);
				id++;
				if (wp.equals(aktWaypoint))
				{
					this.setSelection(id);
					if (this.isDragable())
					{
						if (!(lastAndFirst.x <= id && lastAndFirst.y >= id))
						{
							this.scrollToItem(id);
							Logger.DEBUG("Scroll to:" + id);
						}
					}

					break;
				}
			}
		}
		else
		{
			aktWaypoint = null;
			this.setSelection(0);
			if (this.isDragable())
			{
				if (!(lastAndFirst.x <= 0 && lastAndFirst.y >= 0))
				{
					this.scrollToItem(0);
					Logger.DEBUG("Scroll to:" + 0);
				}
			}
		}

	}

	@Override
	public void SelectedCacheChanged(CacheLite cache, Waypoint waypoint)
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
			newGcCode = Database.CreateFreeGcCode(GlobalCore.getSelectedCache().getGcCode());
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
						aktWaypoint.setTitle(waypoint.getTitle());
						aktWaypoint.Type = waypoint.Type;
						aktWaypoint.Pos = waypoint.Pos;
						aktWaypoint.setDescription(waypoint.getDescription());
						aktWaypoint.IsStart = waypoint.IsStart;
						aktWaypoint.setClue(waypoint.getClue());

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
		}, showCoordinateDialog, false);
		EdWp.show();

	}

	private void deleteWP()
	{
		GL_MsgBox.Show(Translation.Get("?DelWP") + "\n\n[" + aktWaypoint.getTitle() + "]", Translation.Get("!DelWP"), MessageBoxButtons.YesNo,
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

		Logger.DEBUG("WaypointView.addProjection()");
		Logger.DEBUG("   AktWaypoint:" + ((aktWaypoint == null) ? "null" : aktWaypoint.toString()));
		Logger.DEBUG("   AktCache:" + ((aktCache == null) ? "null" : aktCache.toString()));
		Logger.DEBUG("   using Coord:" + coord.toString());

		ProjectionCoordinate pC = new ProjectionCoordinate(ActivityBase.ActivityRec(), "Projection", coord,
				new CB_UI.GL_UI.Activitys.ProjectionCoordinate.ReturnListner()
				{

					@Override
					public void returnCoord(Coordinate targetCoord, Coordinate startCoord, double Bearing, double distance)
					{
						if (coord == null || targetCoord == null || targetCoord.equals(coord)) return;

						String newGcCode = "";
						try
						{
							newGcCode = Database.CreateFreeGcCode(GlobalCore.getSelectedCache().getGcCode());
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
					newGcCode = Database.CreateFreeGcCode(GlobalCore.getSelectedCache().getGcCode());
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
