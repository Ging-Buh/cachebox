package cb_server.Views;

import CB_Core.Database;
import CB_Core.Types.Cache;
import CB_Core.Types.Waypoint;
import Rpc.RpcFunctionsServer;
import cb_server.Events.SelectedCacheChangedEventList;
import com.google.gwt.dev.util.collect.HashMap;
import com.vaadin.server.ExternalResource;
import org.vaadin.addon.leaflet.*;
import org.vaadin.addon.leaflet.shared.Bounds;
import org.vaadin.addon.leaflet.shared.Control;
import org.vaadin.addon.leaflet.shared.Point;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;

public class MapView extends CB_ViewBase {

	private static final long serialVersionUID = 5665480835651086183L;
	public LMap leafletMap;
	private Bounds lastBounds = null;
	private Cache selectedCache = null;
	private String host;
	private final int bigIconSize = 24;
	private final int bigBackgroundSize = 28;
	private final int mediumIconSize = 20;
	private final int mediumBackgroundSize = 24;
	private final int smallIconSize = 15;
	private final int smallBackgroundSize = 15;

	public MapView() {
		System.out.println("MapView()");
		leafletMap = new LMap();
		this.setCompositionRoot(leafletMap);
		this.setSizeFull();
		leafletMap.setSizeFull();
		leafletMap.setWidth("100%");
		leafletMap.setHeight("100%");
		leafletMap.setCenter(60.4525, 22.301);
		leafletMap.setZoomLevel(15);
		leafletMap.setControls(new ArrayList<Control>(Arrays.asList(Control.values())));

		LTileLayer baselayer = new LTileLayer();
		// baselayer.setName("CloudMade");
		baselayer.setAttributionString("&copy;OpenStreetMap contributors");

		LPolyline leafletPolyline = new LPolyline(new Point(60.45, 22.295), new Point(60.4555, 22.301), new Point(60.45, 22.307));
		leafletPolyline.setColor("#FF00FF");
		leafletPolyline.setFill(true);
		leafletPolyline.setFillColor("#00FF00");
		// leafletPolyline.addClickListener(listener);
		leafletMap.addComponent(leafletPolyline);

		// Note, this url should only be used for testing purposes. If you wish
		// to use cloudmade base maps, get your own API key.
		//		baselayer.setUrl("http://{s}.tile.cloudmade.com/a751804431c2443ab399100902c651e8/997/256/{z}/{x}/{y}.png");
		baselayer.setUrl("http://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png");

		// This will make everything sharper on "retina devices", but also text
		// quite small
		// baselayer.setDetectRetina(true);

		LTileLayer pk = new LTileLayer();
		pk.setUrl("http://{s}.kartat.kapsi.fi/peruskartta/{z}/{x}/{y}.png");
		pk.setAttributionString("Maanmittauslaitos, hosted by kartat.kapsi.fi");
		pk.setMaxZoom(18);
		pk.setSubDomains("tile2");
		pk.setDetectRetina(false);
		pk.setVisible(false);

		int port = RpcFunctionsServer.jettyPort;

		try {
			LTileLayer lk = new LTileLayer();
			InetAddress addr = InetAddress.getLocalHost();

			//Getting IPAddress of localhost - getHostAddress return IP Address
			// in textual format
			String ipAddress = addr.getHostAddress();

			lk.setUrl("http://" + ipAddress + ":" + String.valueOf(port) + "/map/{z}/{x}/{y}.png");
			lk.setMaxZoom(19);
			lk.setDetectRetina(false);
			lk.setSubDomains("tile2");
			lk.setVisible(true);
			lk.setActive(false);
			leafletMap.addBaseLayer(lk, "MapsForge");

		} catch (UnknownHostException e) {

			e.printStackTrace();
		}

		leafletMap.addBaseLayer(pk, "");
		leafletMap.addBaseLayer(baselayer, "");
		leafletMap.addMoveEndListener(new LeafletMoveEndListener() {

			@Override
			public void onMoveEnd(LeafletMoveEndEvent event) {
				System.out.println("Move");
				lastBounds = event.getBounds();
				System.out.println(lastBounds.getNorthEastLat());
				updateIcons(event.getZoomLevel(), event.getBounds());

			}
		});
		leafletMap.addClickListener(new LeafletClickListener() {

			@Override
			public void onClick(LeafletClickEvent event) {
				System.out.println("Click:" + event.toString());
			}
		});
		// add to SelectedCacheChangedListener

		// updateIcons(leafletMap.);
		System.out.println("MapView() finished");
	}

	//	public void cacheListChanged(CB_Core.Types.CacheList cacheList) {
	//		super.cacheListChanged(cacheList);
	//	};

	@Override
	public void cacheListChanged() {
		super.cacheListChanged();
		first = true;
		System.out.println("MapView - cacheListChanged()");
		updateIcons(leafletMap.getZoomLevel(), lastBounds);
		System.out.println("MapView - cacheListChanged() finished");
	};

	HashMap<Long, LMarker> markers = null;
	ArrayList<LMarker> cacheMarkers = null; // für Waypoint des selectedCache
	HashMap<Long, LMarker> dMarkers = null; // für D/T-Wertungen
	HashMap<Long, LMarker> tMarkers = null; // für D/T-Wertungen
	LLayerGroup llg = null;
	LLayerGroup lgCache = null;
	LLayerGroup lgDT = null;
	HashMap<Long, LMarker> underlays = null;
	boolean first = true;

	private void updateIcons(double zoom, Bounds bounds) {
		this.getUI().getPage();
		this.getUI().getPage();
		host = com.vaadin.server.Page.getCurrent().getLocation().getScheme() + "://" + com.vaadin.server.Page.getCurrent().getLocation().getAuthority() + "/";
		long start = System.currentTimeMillis();
		int iconSize = smallIconSize;
		int backgroundSize = smallBackgroundSize;
		if ((zoom >= 13) && (zoom <= 14)) {
			iconSize = mediumIconSize;
			backgroundSize = mediumBackgroundSize;
		} else if (zoom > 14) {
			iconSize = bigIconSize; // default Images
			backgroundSize = bigBackgroundSize;
		}
		if (lgDT == null) {
			lgDT = new LLayerGroup();
			leafletMap.addLayer(lgDT);
			dMarkers = new HashMap<Long, LMarker>();
			tMarkers = new HashMap<Long, LMarker>();
		}
		if (llg == null) {
			llg = new LLayerGroup();
			leafletMap.addLayer(llg);
			markers = new HashMap<Long, LMarker>();
		}
		if (first) {
			first = false;
			//			llg = new LLayerGroup();
			underlays = new HashMap<Long, LMarker>();
			for (int i = 0, n = Database.Data.Query.size(); i < n; i++) {
				Cache cache = Database.Data.Query.get(i);
				Waypoint waypoint = cache.GetFinalWaypoint();
				if (waypoint == null) {
					waypoint = cache.GetStartWaypoint();
				}
				LMarker dMarker = null;
				LMarker tMarker = null;
				LMarker marker = null;
				if (waypoint != null) {
					dMarker = new LMarker(waypoint.Pos.getLatitude(), waypoint.Pos.getLongitude());
					tMarker = new LMarker(waypoint.Pos.getLatitude(), waypoint.Pos.getLongitude());
					marker = new LMarker(waypoint.Pos.getLatitude(), waypoint.Pos.getLongitude());
				} else {
					dMarker = new LMarker(cache.Latitude(), cache.Longitude());
					tMarker = new LMarker(cache.Latitude(), cache.Longitude());
					marker = new LMarker(cache.Latitude(), cache.Longitude());
				}
				dMarker.setIconSize(new Point((10.0 / 48 * bigBackgroundSize), bigBackgroundSize));
				dMarker.setIconAnchor(new Point((bigBackgroundSize + (10.0 / 48 * bigBackgroundSize * 2)) / 2 + 1, bigBackgroundSize / 2));
				dMarker.setIcon(new ExternalResource(getDTIcon(cache, bigBackgroundSize, (int) (cache.getDifficulty() * 2))));
				dMarker.setVisible(false);
				dMarkers.put(cache.Id, dMarker);
				lgDT.addComponent(dMarker);
				tMarker.setIconSize(new Point((10.0 / 48 * bigBackgroundSize), bigBackgroundSize));
				tMarker.setIconAnchor(new Point(-(bigBackgroundSize + 0 * (10.0 / 48 * bigBackgroundSize * 2)) / 2, bigBackgroundSize / 2));
				tMarker.setIcon(new ExternalResource(getDTIcon(cache, bigBackgroundSize, (int) (cache.getTerrain() * 2))));
				tMarker.setVisible(false);
				tMarkers.put(cache.Id, tMarker);
				lgDT.addComponent(tMarker);

				marker.setIconSize(new Point(backgroundSize, backgroundSize));
				marker.setIconAnchor(new Point(backgroundSize / 2, backgroundSize / 2));
				marker.setTitle(cache.getName());
				marker.setPopup(cache.getShortDescription());

				marker.setIcon(new ExternalResource(getCacheIcon(cache, iconSize, backgroundSize, false)));
				marker.setVisible(false);
				marker.setCaption("Caption");
				marker.setDescription("Description");
				marker.setLabel(null);
				markers.put(cache.Id, marker);
				llg.addComponent(marker);
				marker.addClickListener(cacheClickListener);
				marker.setData(cache);

			}
			//			leafletMap.addLayer(llg);
		}
		if (selectedCache != SelectedCacheChangedEventList.getCache()) {
			selectedCache = SelectedCacheChangedEventList.getCache();
			// The selectedCache has changed - recalculate the Waypoits of the selectedCache
			if (cacheMarkers == null) {
				cacheMarkers = new ArrayList<LMarker>();
			} else {
				cacheMarkers.clear();
			}
			if (lgCache == null) {
				lgCache = new LLayerGroup();
				leafletMap.addLayer(lgCache);
			} else {
				lgCache.removeAllComponents();
			}
			if ((selectedCache != null) && (selectedCache.waypoints != null)) {
				for (int i = 0, n = selectedCache.waypoints.size(); i <= n; i++) {
					Waypoint waypoint = null;
					String title;
					String description;
					boolean selected = false;
					// letzter Durchlauf für das Icon des Caches selbst, da dies z.B. bei gelösten Mysterys noch nicht gezeigt wird
					if (i < n) {
						waypoint = selectedCache.waypoints.get(i);
						title = waypoint.getTitle();
						description = waypoint.getDescription();
						selected = SelectedCacheChangedEventList.getWaypoint() == waypoint;
					} else {
						title = selectedCache.getName();
						description = selectedCache.getShortDescription();
						selected = SelectedCacheChangedEventList.getWaypoint() == null;
					}

					LMarker marker = null;
					if (waypoint != null) {
						marker = new LMarker(waypoint.Pos.getLatitude(), waypoint.Pos.getLongitude());
					} else {
						marker = new LMarker(selectedCache.Latitude(), selectedCache.Longitude());
					}
					marker.addClickListener(cacheClickListener);
					marker.setIconSize(new Point(bigBackgroundSize, bigBackgroundSize));
					marker.setIconAnchor(new Point(bigBackgroundSize / 2, bigBackgroundSize / 2));
					marker.setTitle(title);
					marker.setPopup(description);

					if (waypoint != null) {
						marker.setIcon(new ExternalResource(getWaypointIcon(waypoint, bigIconSize, bigBackgroundSize, selected)));
						marker.setData(waypoint);
					} else {
						marker.setIcon(new ExternalResource(getCacheIcon(selectedCache, bigIconSize, bigBackgroundSize, selected)));
						marker.setData(selectedCache);
					}
					marker.setVisible(true);
					marker.setLabel(null);

					if (zoom > 15) {
						marker.setLabel(title);
					} else {
						marker.setLabel(null);
					}

					cacheMarkers.add(marker);
					lgCache.addComponent(marker);
				}
			}
		}

		for (int i = 0, n = Database.Data.Query.size(); i < n; i++) {
			Cache cache = Database.Data.Query.get(i);
			LMarker marker = null;
			LMarker dMarker = null;
			LMarker tMarker = null;
			try {
				marker = markers.get(cache.Id);
				dMarker = dMarkers.get(cache.Id);
				tMarker = tMarkers.get(cache.Id);
			} catch (Exception ex) {
				continue; // TODO
			}
			if (marker == null) {
				continue;
			}
			if (dMarker == null) {
				continue;
			}
			if (tMarker == null) {
				continue;
			}
			boolean visible = false;
			if (cache == selectedCache) {
				// hier diesen Marker verstecken, da der Cache-Marker für den SelectedCache mit den Waypoints erzeugt wird
				marker.setVisible(false);
				visible = isInBounds(cache.Latitude(), cache.Longitude(), bounds); // fuer DT-Icon
			} else {
				Waypoint waypoint = cache.GetFinalWaypoint();
				if (waypoint == null) {
					waypoint = cache.GetStartWaypoint();
				}

				if (waypoint != null) {
					visible = isInBounds(waypoint.Pos.getLatitude(), waypoint.Pos.getLongitude(), bounds);
				} else {
					visible = isInBounds(cache.Latitude(), cache.Longitude(), bounds);
				}
				marker.setVisible(visible);
			}
			marker.setIcon(new ExternalResource(getCacheIcon(cache, iconSize, backgroundSize, false)));

			marker.setActive(false);
			marker.setIconSize(new Point(backgroundSize, backgroundSize));
			marker.setIconAnchor(new Point(backgroundSize / 2, backgroundSize / 2));

			if (zoom > 15) {
				marker.setLabel(cache.getName());
			} else {
				marker.setLabel(null);
			}

			dMarker.setVisible(visible && (zoom > 14));
			tMarker.setVisible(visible && (zoom > 14));

		}

		long end = System.currentTimeMillis();
		System.out.println("UpdateIcons Duration: " + String.valueOf(end - start));
	}

	LeafletClickListener cacheClickListener = new LeafletClickListener() {
		@Override
		public void onClick(LeafletClickEvent event) {
			Object source = event.getSource();
			if (source instanceof LMarker) {
				Object data = ((LMarker) source).getData();
				if (data instanceof Cache) {
					selectedCache = null; // damit die Icons neu aufgebaut werden
					SelectedCacheChangedEventList.Call((Cache) data, null);
				} else if (data instanceof Waypoint) {
					selectedCache = null; // damit die Icons neu aufgebaut werden
					SelectedCacheChangedEventList.Call(SelectedCacheChangedEventList.getCache(), (Waypoint) data);
				}
			}
		}
	};

	private boolean isInBounds(double latitude, double longitude, Bounds bounds) {
		if (bounds == null)
			return false;
		if (latitude > bounds.getNorthEastLat())
			return false;
		if (latitude < bounds.getSouthWestLat())
			return false;
		if (longitude < bounds.getSouthWestLon())
			return false;
		if (longitude > bounds.getNorthEastLon())
			return false;
		return true;
	}

	@Override
	public void SelectedCacheChangedEvent(Cache cache, Waypoint waypoint, boolean cacheChanged, boolean waypointChanged) {
		if (cache == null) {
			return;
		}
		if (cacheChanged || waypointChanged) {
			// reset selectedCache to force update of the cache/waypoint information
			selectedCache = null;
		}
		if ((cache != selectedCache) && (lastBounds != null)) {
			updateIcons(leafletMap.getZoomLevel(), lastBounds);
		}
		if (waypoint == null) {
			if (cache != null) {
				leafletMap.setCenter(cache.Latitude(), cache.Longitude());
			}
		} else {
			leafletMap.setCenter(waypoint.Pos.getLatitude(), waypoint.Pos.getLongitude());
		}
	}

	private String getCacheIcon(Cache cache, int iconSize, int backgroundSize, boolean selected) {
		String url = host + "ics/";
		url += "C";
		url += String.format("%02d", cache.Type.ordinal()); // 2 stellig
		if (cache.isArchived())
			url += "A";
		if (!cache.isAvailable())
			url += "N";
		if (cache.isFound())
			url += "F";
		if (cache.ImTheOwner())
			url += "O";
		if (cache.CorrectedCoordiantesOrMysterySolved())
			url += "S";
		if (cache.HasStartWaypoint())
			url += "T";
		if (selected)
			url += "L";
		url += "_S" + iconSize;
		if (backgroundSize > 0) {
			url += "_B" + backgroundSize;
		}
		return url + ".png";
	}

	private String getDTIcon(Cache cache, int iconSize, int value) {
		String url = host + "ics/";
		url += "X";
		url += "_D";
		url += Math.round(value);
		url += "_S" + iconSize;
		return url + ".png";
	}

	private String getWaypointIcon(Waypoint waypoint, int iconSize, int backgroundSize, boolean selected) {
		String url = host + "ics/";
		url += "W";
		url += String.format("%02d", waypoint.Type.ordinal()); // 2 stellig
		if (selected)
			url += "L";
		url += "_S" + iconSize;
		if (backgroundSize > 0) {
			url += "_B" + backgroundSize;
		}
		return url + ".png";
	}
}
