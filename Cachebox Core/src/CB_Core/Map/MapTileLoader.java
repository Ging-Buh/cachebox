package CB_Core.Map;

import java.util.ArrayList;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.security.auth.DestroyFailedException;

import CB_Core.Energy;
import CB_Core.GL_UI.GL_Listener.GL;
import CB_Core.GL_UI.Views.MapView;
import CB_Core.Log.Logger;

public class MapTileLoader
{
	public static final int MAX_MAP_ZOOM = 22;
	public Layer CurrentLayer = null;

	protected SortedMap<Long, TileGL> loadedTiles = new TreeMap<Long, TileGL>();
	final Lock loadedTilesLock = new ReentrantLock();
	protected SortedMap<Long, Descriptor> queuedTiles = new TreeMap<Long, Descriptor>();
	private Lock queuedTilesLock = new ReentrantLock();
	private Thread queueProcessor = null;

	int maxNumTiles = 0;

	public MapTileLoader()
	{
		super();
		if (queueProcessor == null)
		{
			queueProcessor = new queueProcessor();
			queueProcessor.setPriority(Thread.MIN_PRIORITY);
			queueProcessor.start();
		}
	}

	public int QueuedTilesSize()
	{
		return queuedTiles.size();
	}

	public int LoadedTilesSize()
	{
		return loadedTiles.size();
	}

	public void loadTiles(MapView mapView, Descriptor lo, Descriptor ru, int aktZoom)
	{
		if (ManagerBase.Manager == null) return; // Kann nichts laden, wenn der Manager Null ist!

		deleteUnusedTiles();
		// alle notwendigen Tiles zum Laden einstellen in die Queue

		loadedTilesLock.lock();
		queuedTilesLock.lock();
		// Queue jedesmal löschen, damit die Tiles, die eigentlich
		// mal
		// gebraucht wurden aber trotzdem noch nicht geladen sind
		// auch nicht mehr geladen werden
		// dabei aber die MapView berücksichtigen, die die queuedTiles angefordert hat
		// queuedTiles.clear();
		ArrayList<Descriptor> toDelete = new ArrayList<Descriptor>();
		for (Descriptor desc : queuedTiles.values())
		{
			if (desc.Data == mapView)
			{
				toDelete.add(desc);
			}
		}
		for (Descriptor desc : toDelete)
		{
			queuedTiles.remove(desc.GetHashCode());
		}
		try
		{
			for (int i = lo.X; i <= ru.X; i++)
			{
				for (int j = lo.Y; j <= ru.Y; j++)
				{
					Descriptor desc = new Descriptor(i, j, aktZoom);
					// speichern, zu welche MapView diesen Descriptor angefordert hat
					desc.Data = mapView;

					try
					{
						if (loadedTiles.containsKey(desc.GetHashCode()))
						{
							continue; // Dieses
										// Tile
										// existiert
										// schon!
						}
						if (queuedTiles.containsKey(desc.GetHashCode())) continue;
						queueTile(desc);
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			}
		}
		finally
		{
			queuedTilesLock.unlock();
			loadedTilesLock.unlock();
		}
	}

	private void deleteUnusedTiles()
	{
		// Ist Auslagerung überhaupt nötig?
		if (numLoadedTiles() <= maxNumTiles) return;
		// Wenn die Anzahl der maximal gleichzeitig geladenen Tiles
		// überschritten ist
		// die ältesten Tiles löschen
		do
		{
			loadedTilesLock.lock();
			try
			{
				// Kachel mit maximalem Alter suchen
				long maxAge = Integer.MIN_VALUE;
				Descriptor maxDesc = null;

				for (TileGL tile : loadedTiles.values())
					if (/* tile.texture != null && */tile.Age > maxAge)
					{
						maxAge = tile.Age;
						maxDesc = tile.Descriptor;
					}

				// Instanz freigeben und Eintrag löschen
				if (maxDesc != null)
				{
					try
					{
						TileGL tile = loadedTiles.get(maxDesc.GetHashCode());
						loadedTiles.remove(maxDesc.GetHashCode());
						tile.destroy();
					}
					catch (Exception ex)
					{
						Logger.Error("MapView.preemptTile()", "", ex);
					}
				}
			}
			finally
			{
				loadedTilesLock.unlock();
			}
		}
		while (numLoadedTiles() > maxNumTiles);
	}

	int numLoadedTiles()
	{
		return loadedTiles.size();
	}

	public void setMaxNumTiles(int maxNumTiles2)
	{
		if (maxNumTiles2 > maxNumTiles) maxNumTiles = maxNumTiles2;
	}

	private void queueTile(Descriptor desc)
	{
		queuedTilesLock.lock();
		try
		{
			if (queuedTiles.containsKey(desc.GetHashCode())) return;

			queuedTiles.put(desc.GetHashCode(), desc);
		}
		finally
		{
			queuedTilesLock.unlock();
		}

	}

	public void clearLoadedTiles()
	{
		loadedTilesLock.lock();
		try
		{
			for (TileGL tile : loadedTiles.values())
			{
				try
				{
					tile.destroy();
				}
				catch (DestroyFailedException e)
				{
					e.printStackTrace();
				}
			}
			loadedTiles.clear();
		}
		finally
		{
			loadedTilesLock.unlock();
		}
	}

	public void increaseLoadedTilesAge()
	{
		// das Alter aller Tiles um 1 erhöhen
		for (TileGL tile : loadedTiles.values())
		{
			tile.Age++;
		}
	}

	public TileGL getLoadedTile(Descriptor desc)
	{
		return loadedTiles.get(desc.GetHashCode());
	}

	private class queueProcessor extends Thread
	{
		@Override
		public void run()
		{
			try
			{

				do
				{
					Descriptor desc = null;
					if (!Energy.DisplayOff() /* && MapView.this.isVisible() */&& queuedTiles.size() > 0)
					{

						try
						{
							queuedTilesLock.lock();
							try
							{
								// ArrayList<KachelOrder> kOrder = new
								// ArrayList<KachelOrder>();
								// long posFactor = getMapTilePosFactor(zoom);
								// for (int i = lo.X; i <= ru.X; i++)
								// {
								// for (int j = lo.Y; j <= ru.Y; j++)
								// {
								// Descriptor desc = new Descriptor(i, j, zoom);
								// double dist = Math.sqrt(Math.pow((double)
								// desc.X * posFactor * 256 +
								// 128 - screenCenterW.X, 2)
								// + Math.pow((double) desc.Y * posFactor * 256
								// - 128 + screenCenterW.Y,
								// 2));
								// kOrder.add(new KachelOrder(desc, dist));
								// }
								// }
								// Collections.sort(kOrder);

								Descriptor nearestDesc = null;
								double nearestDist = Double.MAX_VALUE;
								int nearestZoom = 0;
								for (Descriptor tmpDesc : queuedTiles.values())
								{
									// zugehörige MapView aus dem Data vom Descriptor holen
									MapView mapView = null;
									if ((tmpDesc.Data != null) && (tmpDesc.Data instanceof MapView)) mapView = (MapView) tmpDesc.Data;
									if (mapView == null) continue;

									long posFactor = getMapTilePosFactor(tmpDesc.Zoom);

									double dist = Math
											.sqrt(Math.pow(
													(double) tmpDesc.X * posFactor * 256 + 128 * posFactor - mapView.screenCenterW.x, 2)
													+ Math.pow((double) tmpDesc.Y * posFactor * 256 + 128 * posFactor
															+ mapView.screenCenterW.y, 2));

									if (Math.abs(mapView.aktZoom - nearestZoom) > Math.abs(mapView.aktZoom - tmpDesc.Zoom))
									{
										// der Zoomfaktor des bisher besten
										// Tiles ist weiter entfernt vom
										// aktuellen Zoom als der vom tmpDesc ->
										// tmpDesc verwenden
										nearestDist = dist;
										nearestDesc = tmpDesc;
										nearestZoom = tmpDesc.Zoom;
									}

									if (dist < nearestDist)
									{
										if (Math.abs(mapView.aktZoom - nearestZoom) < Math.abs(mapView.aktZoom - tmpDesc.Zoom))
										{
											// zuerst die Tiles, die dem
											// aktuellen Zoom Faktor am nächsten
											// sind.
											continue;
										}
										nearestDist = dist;
										nearestDesc = tmpDesc;
										nearestZoom = tmpDesc.Zoom;
									}
								}
								desc = nearestDesc;

							}
							finally
							{
								queuedTilesLock.unlock();
							}

							// if (desc.Zoom == zoom)
							{
								LoadTile(desc);
							}

							// if (queuedTiles.size() < mapView.maxTilesPerScreen) Thread.sleep(100);

						}
						catch (Exception ex1)
						{
							Logger.Error("MapViewGL.queueProcessor.doInBackground()", "1", ex1);
						}

					}
					else
					{
						Thread.sleep(200);
					}
				}
				while (true);
			}
			catch (Exception ex3)
			{
				Logger.Error("MapViewGL.queueProcessor.doInBackground()", "3", ex3);
			}
			finally
			{
				// damit im Falle einer Exception der Thread neu gestartet wird
				// queueProcessor = null;
			}
			return;
		}
	}

	public long getMapTilePosFactor(float zoom)
	{
		long result = 1;

		// for (int z = zoom; z < MAX_MAP_ZOOM; z++)
		// {
		// result *= 2;
		// }

		result = (long) Math.pow(2.0, MAX_MAP_ZOOM - zoom);

		return result;
	}

	private void LoadTile(Descriptor desc)
	{
		TileGL.TileState tileState = TileGL.TileState.Disposed;

		byte[] bytes = null;
		if (ManagerBase.Manager != null)
		{
			bytes = ManagerBase.Manager.LoadInvertedPixmap(CurrentLayer, desc);
		}
		// byte[] bytes = MapManagerEventPtr.OnGetMapTile(CurrentLayer, desc);
		// Texture texture = new Texture(new Pixmap(bytes, 0, bytes.length));
		if (bytes != null && bytes.length > 0)
		{
			tileState = TileGL.TileState.Present;
			addLoadedTile(desc, bytes, tileState);
			// Redraw Map after a new Tile was loaded or generated
			GL.that.renderOnce("MapTileLoader loadTile");
		}
		else
		{
			ManagerBase.Manager.CacheTile(CurrentLayer, desc);
		}

	}

	private void addLoadedTile(Descriptor desc, byte[] bytes, TileGL.TileState state)
	{
		loadedTilesLock.lock();
		try
		{
			if (loadedTiles.containsKey(desc.GetHashCode()))
			{

			}
			else
			{
				TileGL tile = new TileGL(desc, bytes, state);
				loadedTiles.put(desc.GetHashCode(), tile);
			}

		}
		finally
		{
			loadedTilesLock.unlock();
		}

		queuedTilesLock.lock();
		try
		{
			if (queuedTiles.containsKey(desc.GetHashCode()))
			{
				queuedTiles.remove(desc.GetHashCode());
			}
		}
		finally
		{
			queuedTilesLock.unlock();
		}

	}

}
