package CB_Core.GL_UI.Views;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import CB_Core.GlobalCore;
import CB_Core.DB.Database;
import CB_Core.Enums.CacheTypes;
import CB_Core.Events.CachListChangedEventList;
import CB_Core.Events.CacheListChangedEventListner;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.Log.Logger;
import CB_Core.Map.Descriptor;
import CB_Core.Types.Cache;
import CB_Core.Types.Waypoint;
import CB_Core.Util.MoveableList;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;

public class MapViewCacheList implements CacheListChangedEventListner
{
	private int maxZoomLevel;
	private queueProcessor queueProcessor = null;

	/**
	 * State 0: warten auf neuen Update Befehl <br>
	 * State 1: Berechnen <br>
	 * State 2: Berechnung in Gang <br>
	 * State 3: Berechnung fertig - warten auf abholen <br>
	 * State 4: queueProcessor abgebrochen
	 */
	private AtomicInteger state = new AtomicInteger(0);
	private Vector2 point1;
	private Vector2 point2;
	private int zoom = 15;
	public ArrayList<WaypointRenderInfo> list = new ArrayList<MapViewCacheList.WaypointRenderInfo>();
	private MoveableList<WaypointRenderInfo> tmplist;
	private WaypointRenderInfo selectedWP;
	public int anz = 0;
	private boolean hideMyFinds = false;
	private boolean showAllWaypoints = false;

	// public ArrayList<ArrayList<Sprite>> NewMapIcons = null;
	// public ArrayList<ArrayList<Sprite>> NewMapOverlay = null;

	public MapViewCacheList(int maxZoomLevel)
	{
		super();
		this.maxZoomLevel = maxZoomLevel;

		StartQueueProcessor();

		// register as CacheListChangedEventListner
		CachListChangedEventList.Add(this);

	}

	private void StartQueueProcessor()
	{

		try
		{
			Logger.DEBUG("MapCacheList.queueProcessor Create");
			queueProcessor = new queueProcessor();
			queueProcessor.setPriority(Thread.MIN_PRIORITY);
		}
		catch (Exception ex)
		{
			Logger.Error("MapCacheList.queueProcessor", "onCreate", ex);
		}

		Logger.DEBUG("MapCacheList.queueProcessor Start");
		queueProcessor.start();

		state.set(0);
	}

	private class queueProcessor extends Thread
	{

		@Override
		public void run()
		{
			// boolean queueEmpty = false;
			try
			{
				do
				{
					if (state.compareAndSet(1, 2))
					{
						int iconSize = 0; // 8x8
						if ((zoom >= 13) && (zoom <= 14)) iconSize = 1; // 13x13
						else if (zoom > 14) iconSize = 2; // default Images

						tmplist = new MoveableList<MapViewCacheList.WaypointRenderInfo>();
						selectedWP = null;
						synchronized (Database.Data.Query)
						{
							for (Cache cache : Database.Data.Query)
							{
								// Funde
								if (hideMyFinds && cache.Found) continue;
								boolean showWaypoints = showAllWaypoints || GlobalCore.getSelectedCache() == cache;
								double MapX = 256.0 * Descriptor.LongitudeToTileX(maxZoomLevel, cache.Longitude());
								double MapY = -256.0 * Descriptor.LatitudeToTileY(maxZoomLevel, cache.Latitude());
								Waypoint fwp = null; // Final Waypoint
								Waypoint swp = null; // Start Waypoint
								// sichtbare Wegpunkte hinzufügen, auch wenn der Cache nicht sichtbar ist
								if (showWaypoints)
								{
									addWaypoints(cache, iconSize);
								}
								else
								{
									if (cache.Type == CacheTypes.Mystery)
									{
										if (!cache.hasCorrectedCoordinates())
										{
											fwp = cache.GetFinalWaypoint();
											if (fwp != null)
											{
												// nehme Mystery-Final
												MapX = 256.0 * Descriptor.LongitudeToTileX(maxZoomLevel, fwp.Pos.getLongitude());
												MapY = -256.0 * Descriptor.LatitudeToTileY(maxZoomLevel, fwp.Pos.getLatitude());
											}
										}
									}
									if ((cache.Type == CacheTypes.Multi) || (cache.Type == CacheTypes.Mystery))
									{
										if (!cache.hasCorrectedCoordinates() && (fwp == null))
										{
											// Suche, ob zu diesem Cache ein Start-Waypoint definiert ist
											// Wenn ja, und wenn es kein Mystery mit Final ist dann wird das CacheIcon in der Map auf diesen
											// WP verschoben wenn der Cache nicht selected ist.
											swp = cache.GetStartWaypoint();
											if (swp != null)
											{
												// nehme Start Waypoint
												MapX = 256 * Descriptor.LongitudeToTileX(maxZoomLevel, swp.Pos.getLongitude());
												MapY = -256 * Descriptor.LatitudeToTileY(maxZoomLevel, swp.Pos.getLatitude());
											}
										}
									}
								}
								if (isVisible(MapX, MapY) || (GlobalCore.getSelectedCache() == cache))
								{
									// sichtbaren Cache/Mystery-Final hinzufügen
									WaypointRenderInfo wpi = new WaypointRenderInfo();
									wpi.MapX = (float) MapX;
									wpi.MapY = (float) MapY;
									if (cache.Archived || !cache.Available) wpi.OverlayIcon = SpriteCache.MapOverlay.get(2);
									wpi.UnderlayIcon = getUnderlayIcon(cache, null, iconSize);
									wpi.Icon = getCacheIcon(cache, iconSize);
									wpi.Cache = cache;
									wpi.Waypoint = null; // = fwp; ist null, ausser bei Mystery-Final // null -> Beschriftung Name vom Cache
									wpi.Selected = (GlobalCore.getSelectedCache() == cache);
									if (wpi.Selected && selectedWP == null) selectedWP = wpi;// select nur wenn kein WP selectiert ist (draw
																								// last)
									tmplist.add(wpi);
								}
							}
						}

						synchronized (list)
						{

							// move selected WPI to last
							int index = tmplist.indexOf(selectedWP);
							if (index >= 0 && index <= tmplist.size()) tmplist.MoveItemLast(index);

							list.clear();
							list = tmplist;
							tmplist = null;
						}
						Thread.sleep(50);
						state.set(0);
						anz++;
						if (savedQuery != null)
						{
							// es steht noch eine Anfrage an!
							// Diese jetzt ausführen!
							MapViewCacheListUpdateData data = new MapViewCacheListUpdateData(savedQuery);
							data.hideMyFinds = MapViewCacheList.this.hideMyFinds;
							data.showAllWaypoints = MapViewCacheList.this.showAllWaypoints;
							savedQuery = null;
							update(data);
						}
					}
					else
					{
						Thread.sleep(100);
					}
				}
				while (true);
			}
			catch (Exception ex3)
			{
				Logger.Error("MapCacheList.queueProcessor.doInBackground()", "3", ex3);
			}
			finally
			{
				// wenn der Thread beendet wurde, muss er neu gestartet werden!
				state.set(4);
			}
			return;
		}
	}

	private void addWaypoints(Cache cache, int iconSize)
	{
		for (Waypoint wp : cache.waypoints)
		{
			addWaypoint(cache, wp, iconSize);
		}
	}

	private void addWaypoint(Cache cache, Waypoint wp, int iconSize)
	{
		// im Bild ?
		double MapX = 256.0 * Descriptor.LongitudeToTileX(maxZoomLevel, wp.Pos.getLongitude());
		double MapY = -256.0 * Descriptor.LatitudeToTileY(maxZoomLevel, wp.Pos.getLatitude());
		if (isVisible(MapX, MapY) || (GlobalCore.getSelectedWaypoint() == wp))
		{
			WaypointRenderInfo wpi = new WaypointRenderInfo();
			wpi.MapX = (float) MapX;
			wpi.MapY = (float) MapY;

			wpi.Icon = getWaypointIcon(wp);
			wpi.Cache = cache;
			wpi.Waypoint = wp;
			wpi.UnderlayIcon = getUnderlayIcon(wpi.Cache, wpi.Waypoint, iconSize);
			wpi.Selected = (GlobalCore.getSelectedWaypoint() == wp);
			if (wpi.Selected) selectedWP = wpi;
			tmplist.add(wpi);
		}
	}

	private boolean isVisible(double x, double y)
	{
		return ((x >= point1.x) && (x < point2.x) && (Math.abs(y) > Math.abs(point1.y)) && (Math.abs(y) < Math.abs(point2.y)));
	}

	private Sprite getWaypointIcon(Waypoint waypoint)
	{
		if ((waypoint.Type == CacheTypes.MultiStage) && (waypoint.IsStart)) return SpriteCache.MapIcons.get(24);
		else
			return SpriteCache.MapIcons.get(waypoint.Type.ordinal());
	}

	private Sprite getCacheIcon(Cache cache, int iconSize)
	{
		if ((iconSize < 1) && (cache != GlobalCore.getSelectedCache()))
		{
			return getSmallMapIcon(cache);
		}
		else
		{
			// der SelectedCache wird immer mit den großen Symbolen dargestellt!
			return getMapIcon(cache);
		}
	}

	private Sprite getMapIcon(Cache cache)
	{
		int IconId;
		if (cache.ImTheOwner()) IconId = 26;
		else if (cache.Found) IconId = 19;
		else if ((cache.Type == CacheTypes.Mystery) && cache.CorrectedCoordiantesOrMysterySolved()) IconId = 21;
		else if ((cache.Type == CacheTypes.Multi) && cache.HasStartWaypoint()) IconId = 23; // Multi mit Startpunkt
		else if ((cache.Type == CacheTypes.Mystery) && cache.HasStartWaypoint()) IconId = 25; // Mystery ohne Final aber mit Startpunkt
		else if ((cache.Type == CacheTypes.Munzee)) IconId = 22;
		else
			IconId = cache.Type.ordinal();
		return SpriteCache.MapIcons.get(IconId);
	}

	private Sprite getSmallMapIcon(Cache cache)
	{
		int iconId = 0;

		switch (cache.Type)
		{
		case Traditional:
			iconId = 0;
			break;
		case Letterbox:
			iconId = 0;
			break;
		case Multi:
			if (cache.HasStartWaypoint()) iconId = 1;
			else
				iconId = 1;
			break;
		case Event:
			iconId = 2;
			break;
		case MegaEvent:
			iconId = 2;
			break;
		case Virtual:
			iconId = 3;
			break;
		case Camera:
			iconId = 3;
			break;
		case Earth:
			iconId = 3;
			break;
		case Mystery:
		{
			if (cache.HasFinalWaypoint()) iconId = 5;
			else if (cache.HasStartWaypoint()) iconId = 5;
			else
				iconId = 4;
			break;
		}
		case Wherigo:
			iconId = 4;
			break;

		default:
			iconId = 0;
		}

		if (cache.Found) iconId = 6;
		if (cache.ImTheOwner()) iconId = 7;

		if (cache.Archived || !cache.Available) iconId += 8;

		if (cache.Type == CacheTypes.MyParking) iconId = 16;
		if (cache.Type == CacheTypes.Munzee) iconId = 17;

		return SpriteCache.MapIconsSmall.get(iconId);

	}

	private Sprite getUnderlayIcon(Cache cache, Waypoint waypoint, int iconSize)
	{
		if ((iconSize == 0) && (cache != GlobalCore.getSelectedCache()))
		{
			return null;
		}
		else
		{
			if (waypoint == null)
			{
				if ((cache == null) || (cache == GlobalCore.getSelectedCache()))
				{
					return SpriteCache.MapOverlay.get(1);
				}
				else
				{
					return SpriteCache.MapOverlay.get(0);
				}
			}
			else
			{
				if (waypoint == GlobalCore.getSelectedWaypoint())
				{
					return SpriteCache.MapOverlay.get(1);
				}
				else
				{
					return SpriteCache.MapOverlay.get(0);
				}
			}
		}
	}

	private Vector2 lastPoint1;
	private Vector2 lastPoint2;
	private int lastzoom;

	public static class MapViewCacheListUpdateData
	{
		public Vector2 point1;
		public Vector2 point2;
		public int zoom;
		public boolean doNotCheck;
		public boolean hideMyFinds = false;
		public boolean showAllWaypoints = false;

		public MapViewCacheListUpdateData(Vector2 point1, Vector2 point2, int zoom, boolean doNotCheck)
		{
			this.point1 = point1;
			this.point2 = point2;
			this.zoom = zoom;
			this.doNotCheck = doNotCheck;
		}

		public MapViewCacheListUpdateData(MapViewCacheListUpdateData data)
		{
			this.point1 = data.point1;
			this.point2 = data.point2;
			this.zoom = data.zoom;
			this.doNotCheck = data.doNotCheck;
		}
	}

	MapViewCacheListUpdateData savedQuery = null;

	MapViewCacheListUpdateData LastUpdateData = null;

	public void update(MapViewCacheListUpdateData data)
	{
		LastUpdateData = data;
		this.showAllWaypoints = data.showAllWaypoints;
		this.hideMyFinds = data.hideMyFinds;

		if (data.point1 == null || data.point2 == null) return;

		if (state.get() == 4)
		{
			// der queueProcessor wurde gestoppt und muss neu gestartet werden
			StartQueueProcessor();
		}

		if (state.get() != 0)
		{
			// Speichere Update anfrage und führe sie aus, wenn der queueProcessor wieder bereit ist!
			savedQuery = data;
			return;
		}

		if ((data.zoom == lastzoom) && (!data.doNotCheck))
		{
			// wenn LastPoint == 0 muss eine neue Liste Berechnet werden!
			if (lastPoint1 != null && lastPoint2 != null)
			{
				// Prüfen, ob überhaupt eine neue Liste berechnet werden muß
				if ((data.point1.x >= lastPoint1.x) && (data.point2.x <= lastPoint2.x) && (data.point1.y >= lastPoint1.y)
						&& (data.point2.y <= lastPoint2.y)) return;
			}

		}

		// Bereich erweitern, damit von vorne herein gleiche mehr Caches geladen werden und diese Liste nicht so oft berechnet werden muss
		Vector2 size = new Vector2(data.point2.x - data.point1.x, data.point2.y - data.point1.y);
		data.point1.x -= size.x;
		data.point2.x += size.x;
		data.point1.y -= size.y;
		data.point2.y += size.y;

		this.lastzoom = data.zoom;
		lastPoint1 = data.point1;
		lastPoint2 = data.point2;

		this.zoom = data.zoom;
		this.point1 = data.point1;
		this.point2 = data.point2;
		state.set(1);
	}

	public boolean hasNewResult()
	{
		return state.get() == 3;
	}

	public static class WaypointRenderInfo
	{
		public float MapX;
		public float MapY;
		public Cache Cache;
		public Waypoint Waypoint;
		public boolean Selected;
		public Sprite Icon;
		public Sprite UnderlayIcon;
		public Sprite OverlayIcon;
	}

	@Override
	public void CacheListChangedEvent()
	{
		if (LastUpdateData != null)
		{
			LastUpdateData.doNotCheck = true;
			update(LastUpdateData);
		}
	};

}
