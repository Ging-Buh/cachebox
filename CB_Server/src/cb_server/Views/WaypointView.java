package cb_server.Views;

import CB_Core.CacheTypes;
import CB_Core.DAO.WaypointDAO;
import CB_Core.Database;
import CB_Core.Types.Cache;
import CB_Core.Types.Waypoint;
import CB_Locator.Coordinate;
import cb_server.Events.SelectedCacheChangedEventList;
import cb_server.Events.SelectedCacheChangedEventListner;
import cb_server.Views.Dialogs.WaypointDialog;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import org.vaadin.peter.contextmenu.ContextMenu;
import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuItem;
import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuItemClickEvent;
import org.vaadin.peter.contextmenu.ContextMenu.ContextMenuItemClickListener;

import java.io.Serializable;

public class WaypointView extends Panel implements SelectedCacheChangedEventListner {

	private static final long serialVersionUID = -5376708430763812238L;
	private BeanItemContainer<WaypointBean> beans;
	public Table table;
	private boolean doNotUpdate = false;
	private boolean createNewWaypoint = false;

	public WaypointView() {
		beans = new BeanItemContainer<WaypointBean>(WaypointBean.class);

		this.table = new Table("WaypointList", beans);
		this.setContent(table);
		this.setSizeFull();
		table.setSizeFull();
		table.setSelectable(true);
		table.setImmediate(true);

		table.addValueChangeListener(new ValueChangeListener() {
			private static final long serialVersionUID = -1246546962581855595L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				Object o = table.getValue();
				if (o instanceof WaypointBean) {
					doNotUpdate = true;
					SelectedCacheChangedEventList.Call(SelectedCacheChangedEventList.getCache(), ((WaypointBean) o).waypoint);
					doNotUpdate = false;
				}
			}
		});
		SelectedCacheChangedEventList.Add(this);

		// Context Menu
		ContextMenu contextMenu = new ContextMenu();
		contextMenu.setOpenAutomatically(true);
		ContextMenuItem cmi = contextMenu.addItem("Edit");
		cmi.addItemClickListener(new ContextMenuItemClickListener() {
			@Override
			public void contextMenuItemClicked(ContextMenuItemClickEvent event) {
				createNewWaypoint = false;
				if (SelectedCacheChangedEventList.getWaypoint() == null)
					return;
				WaypointDialog dial = new WaypointDialog(SelectedCacheChangedEventList.getWaypoint(), new WaypointDialog.ReturnListner() {
					@Override
					public void returnedWP(Waypoint waypoint) {
						if (waypoint != null) {
							if (waypoint.IsStart) {
								// Es muss hier sichergestellt sein dass dieser Waypoint der einzige dieses Caches ist, der als Startpunkt
								// definiert
								// ist!!!
								WaypointDAO wpd = new WaypointDAO();
								wpd.ResetStartWaypoint(SelectedCacheChangedEventList.getCache(), waypoint);
							}
							WaypointDAO waypointDAO = new WaypointDAO();
							waypointDAO.UpdateDatabase(waypoint);
							WaypointView.this.table.setContainerDataSource(WaypointView.this.beans);
							//							that.setBaseAdapter(lvAdapter);
							SelectedCacheChangedEventList.WaypointChanged(SelectedCacheChangedEventList.getCache(), waypoint);
						}

					}
				});
				WaypointView.this.getUI().addWindow(dial);

			}
		});
		cmi = contextMenu.addItem("New Waypoint");
		cmi.addItemClickListener(new ContextMenuItemClickListener() {
			@Override
			public void contextMenuItemClicked(ContextMenuItemClickEvent event) {
				createNewWaypoint = true;
				String newGcCode = "";
				try {
					newGcCode = Database.CreateFreeGcCode(SelectedCacheChangedEventList.getCache().getGcCode());
				} catch (Exception e) {
					return;
				}
				Coordinate coord = SelectedCacheChangedEventList.getCache().Pos;
				Waypoint newWP = new Waypoint(newGcCode, CacheTypes.ReferencePoint, "", coord.getLatitude(), coord.getLongitude(), SelectedCacheChangedEventList.getCache().Id, "", "Waypoint"/*Translation.Get("wyptDefTitle")*/);
				WaypointDialog dial = new WaypointDialog(newWP, new WaypointDialog.ReturnListner() {
					@Override
					public void returnedWP(Waypoint waypoint) {
						if (waypoint != null) {
							SelectedCacheChangedEventList.getCache().waypoints.add(waypoint);
							WaypointView.this.table.setContainerDataSource(WaypointView.this.beans);
							SelectedCacheChangedEventList.Call(SelectedCacheChangedEventList.getCache(), waypoint);
							if (waypoint.IsStart) {
								// Es muss hier sichergestellt sein dass dieser Waypoint der einzige dieses Caches ist, der als Startpunkt
								// definiert
								// ist!!!
								WaypointDAO wpd = new WaypointDAO();
								wpd.ResetStartWaypoint(SelectedCacheChangedEventList.getCache(), waypoint);
							}
							WaypointDAO waypointDAO = new WaypointDAO();
							waypointDAO.WriteToDatabase(waypoint);

							//							SelectedCacheChangedEvent(SelectedCacheChangedEventList.getCache(), waypoint, false, false);
							SelectedCacheChangedEventList.WaypointChanged(SelectedCacheChangedEventList.getCache(), waypoint);

						}

					}
				});
				WaypointView.this.getUI().addWindow(dial);

			}
		});
		contextMenu.setAsTableContextMenu(table);
	}

	@Override
	public void SelectedCacheChangedEvent(Cache cache, Waypoint waypoint, boolean cacheChanged, boolean waypointChanged) {
		if (doNotUpdate)
			return;
		beans.removeAllItems();
		beans.addBean(new WaypointBean(SelectedCacheChangedEventList.getCache(), null));

		WaypointDAO dao = new WaypointDAO();

		//		CB_List<Waypoint> waypoints = dao.getWaypointsFromCacheID(cache.Id,true);

		for (int i = 0, n = cache.waypoints.size(); i < n; i++) {
			beans.addBean(new WaypointBean(SelectedCacheChangedEventList.getCache(), cache.waypoints.get(i)));
		}
		table.setData(beans);
	}

	public class WaypointBean implements Serializable {

		private static final long serialVersionUID = -6410465924774846650L;
		private String GCCode;
		private String Title;
		private String Description;
		private CacheTypes type;
		private final Cache cache;
		private final Waypoint waypoint;
		private String Coord;

		public WaypointBean(Cache cache, Waypoint waypoint) {
			this.cache = cache;
			this.waypoint = waypoint;
			this.setGCCode("");
			this.setTitle("");
			this.setType(CacheTypes.Cache);
		}

		public String getTitle() {
			if (waypoint == null)
				return cache.getName();
			else
				return waypoint.getTitle();
		}

		public void setTitle(String title) {
			Title = title;
		}

		public String getGCCode() {
			if (waypoint == null) {
				return cache.getGcCode();
			} else {
				return waypoint.getGcCode();
			}
		}

		public void setGCCode(String gCCode) {
			GCCode = gCCode;
		}

		public CacheTypes getType() {
			if (waypoint == null) {
				return cache.Type;
			} else {
				return waypoint.Type;
			}
		}

		public void setType(CacheTypes type) {
			this.type = type;
		}

		public String getCoord() {
			if (waypoint != null) {
				return waypoint.Pos.FormatCoordinate();
			} else {
				return cache.Pos.FormatCoordinate();
			}
		}

		public void setCoord(String coord) {
			Coord = coord;
		}
	}
}
