package de.cachebox_test.Map;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.mapsforge.android.maps.DebugSettings;
import org.mapsforge.android.maps.mapgenerator.JobParameters;
import org.mapsforge.android.maps.mapgenerator.JobTheme;
import org.mapsforge.android.maps.mapgenerator.MapGenerator;
import org.mapsforge.android.maps.mapgenerator.MapGeneratorFactory;
import org.mapsforge.android.maps.mapgenerator.MapGeneratorInternal;
import org.mapsforge.android.maps.mapgenerator.MapGeneratorJob;
import org.mapsforge.android.maps.mapgenerator.databaserenderer.DatabaseRenderer;
import org.mapsforge.android.maps.mapgenerator.databaserenderer.ExternalRenderTheme;
import org.mapsforge.android.maps.rendertheme.InternalRenderTheme;
import org.mapsforge.core.Tile;
import org.mapsforge.map.reader.MapDatabase;
import org.mapsforge.map.reader.header.FileOpenResult;

import CB_Core.FileIO;
import CB_Core.Log.Logger;
import CB_Core.Map.BoundingBox;
import CB_Core.Map.Descriptor;
import CB_Core.Map.Layer;
import CB_Core.Map.ManagerBase;
import CB_Core.Map.PackBase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class AndroidManager extends ManagerBase
{

	MapGenerator mapGenerator = null;
	MapDatabase mapDatabase = null;
	DatabaseRenderer databaseRenderer = null;
	Bitmap tileBitmap = null;
	File mapFile = null;
	JobParameters jobParameters = null;
	float textScale = 1;
	InternalRenderTheme DEFAULT_RENDER_THEME = InternalRenderTheme.OSMARENDER;
	JobTheme jobTheme = null;
	float DEFAULT_TEXT_SCALE = 1;

	/*
	 * public delegate void FetchAreaCallback();
	 */

	public AndroidManager()
	{
		// for the Access to the manager in the CB_Core
		CB_Core.Map.ManagerBase.Manager = this;
		// Layers.add(new Layer("MapsForge", "MapsForge", ""));
		Layers.add(new Layer("Mapnik", "Mapnik", "http://a.tile.openstreetmap.org/"));
		Layers.add(new Layer("OSM Cycle Map", "Open Cycle Map", "http://c.tile.opencyclemap.org/cycle/"));
		// Layers.add(new Layer("TilesAtHome", "Osmarender", "http://a.tah.openstreetmap.org/Tiles/tile/"));
	}

	/*
	 * /// <summary> /// *berprüft, ob die übergebene Kachel bei OSM überhaupt existiert /// </summary> /// <param name="desc">Deskriptor
	 * der zu prüfenden Kachel</param> /// <returns>true, falls die Kachel existiert, sonst false</returns> public static bool
	 * ExistsTile(Descriptor desc) { int range = (int)Math.Pow(2, desc.Zoom); return desc.X < range && desc.Y < range && desc.X >= 0 &&
	 * desc.Y >= 0; }
	 */

	@Override
	public PackBase CreatePack(String file) throws IOException
	{
		return new Pack(this, file);
	}

	public Bitmap LoadLocalBitmap(String layer, Descriptor desc)
	{
		return LoadLocalBitmap(GetLayerByName(layer, layer, ""), desc);
	}

	@Override
	public byte[] LoadLocalPixmap(Layer layer, Descriptor desc)
	{
		// Mapsforge 3.0
		if (layer.isMapsForge)
		{
			byte[] result = null;

			if (mapGenerator == null) mapGenerator = MapGeneratorFactory.createMapGenerator(MapGeneratorInternal.DATABASE_RENDERER);

			if ((mapDatabase == null) || (!mapsForgeFile.equalsIgnoreCase(layer.Name)))
			{
				mapFile = new File(CB_Core.Config.settings.MapPackFolder.getValue() + "/" + layer.Name);
				mapDatabase = new MapDatabase();
				mapDatabase.closeFile();
				FileOpenResult fileOpenResult = mapDatabase.openFile(mapFile);
				Logger.DEBUG("Open MapsForge Map: " + mapFile);

				databaseRenderer = (DatabaseRenderer) mapGenerator;
				databaseRenderer.setMapDatabase(mapDatabase);

				try
				{
					Logger.DEBUG("Suche RenderTheme: " + CB_Core.Config.settings.MapPackFolder.getValue() + "/renderthemes/test.xml");
					File file = new File(CB_Core.Config.settings.MapPackFolder.getValue() + "/renderthemes/test.xml");
					if (file.exists())
					{
						Logger.DEBUG("RenderTheme found!");
						JobTheme jobTheme = new ExternalRenderTheme(file);
						jobParameters = new JobParameters(jobTheme, DEFAULT_TEXT_SCALE);
					}
					else
					{
						Logger.DEBUG("RenderTheme not found!");
						jobParameters = new JobParameters(DEFAULT_RENDER_THEME, DEFAULT_TEXT_SCALE);
					}

				}
				catch (FileNotFoundException e)
				{
					Logger.Error("Load RenderTheme", "Error loading RenderTheme!", e);
					jobParameters = new JobParameters(DEFAULT_RENDER_THEME, DEFAULT_TEXT_SCALE);
				}

				tileBitmap = Bitmap.createBitmap(Tile.TILE_SIZE, Tile.TILE_SIZE, Bitmap.Config.RGB_565);
				mapsForgeFile = layer.Name;
			}

			Tile tile = new Tile(desc.X, desc.Y, (byte) desc.Zoom);

			DebugSettings debugSettings = new DebugSettings(false, false, false);

			MapGeneratorJob job = new MapGeneratorJob(tile, mapFile, jobParameters, debugSettings);

			if (databaseRenderer.executeJob(job, tileBitmap))
			{
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				tileBitmap.compress(Bitmap.CompressFormat.PNG, 50, baos);

				result = baos.toByteArray();

				try
				{
					baos.close();
				}
				catch (IOException e)
				{ // TODO Auto-generated catch block e.printStackTrace(); } return result; }
				}
			}

			return result;
		}
		/*
		 * 0.2.x if (layer.isMapsForge) { if ((mapDatabase == null) || (!mapsForgeFile.equalsIgnoreCase(layer.Name))) { mapDatabase = new
		 * MapDatabase(); mapDatabase.openFile(CB_Core.Config.settings.MapPackFolder.getValue() + "/" + layer.Name); renderer = new
		 * CanvasRenderer(); renderer.setDatabase(mapDatabase); tileBitmap = Bitmap.createBitmap(256, 256, Config.RGB_565);
		 * renderer.setupMapGenerator(tileBitmap); mapsForgeFile = layer.Name; } Tile tile = new Tile(desc.X, desc.Y, (byte) desc.Zoom);
		 * 
		 * // Original value = 1.333f now 1.333 * dpiScaleFactorX from MapView
		 * 
		 * float DPIawareFaktor = (float) (MapView.dpiScaleFactorX * 1.333);
		 * 
		 * MapGeneratorJob job = new MapGeneratorJob(tile, MapViewMode.CANVAS_RENDERER, "xxx", DPIawareFaktor, false, false, false);
		 * 
		 * // renderer.setupMapGenerator(tileBitmap); renderer.prepareMapGeneration(); renderer.executeJob(job); // Bitmap bit =
		 * renderer.tileBitmap.copy(Config.RGB_565, true);
		 * 
		 * ByteArrayOutputStream baos = new ByteArrayOutputStream(); if (main.N) { Bitmap b = Bitmap.createBitmap(256, 256, Config.RGB_565);
		 * Canvas c = new Canvas(b); c.drawBitmap(renderer.tileBitmap, 0, 0, main.N ? Global.invertPaint : new Paint());
		 * b.compress(Bitmap.CompressFormat.PNG, 50, baos); } else { renderer.tileBitmap.compress(Bitmap.CompressFormat.PNG, 50, baos); }
		 * 
		 * byte[] result = baos.toByteArray();
		 * 
		 * try { baos.close(); } catch (IOException e) { // TODO Auto-generated catch block e.printStackTrace(); } return result; }
		 */
		try
		{
			// Schauen, ob Tile im Cache liegt
			String cachedTileFilename = layer.GetLocalFilename(desc);

			long cachedTileAge = 0;

			if (FileIO.FileExists(cachedTileFilename))
			{
				File info = new File(cachedTileFilename);
				cachedTileAge = info.lastModified();
			}

			// Kachel im Pack suchen
			for (int i = 0; i < mapPacks.size(); i++)
			{
				PackBase mapPack = mapPacks.get(i);
				if ((mapPack.Layer.Name.equalsIgnoreCase(layer.Name)) && (mapPack.MaxAge >= cachedTileAge))
				{
					BoundingBox bbox = mapPacks.get(i).Contains(desc);

					if (bbox != null)
					{
						return mapPacks.get(i).LoadFromBoundingBoxByteArray(bbox, desc);
						// Bitmap result = mapPacks.get(i).LoadFromBoundingBox(bbox, desc);
						// ByteArrayOutputStream stream = new ByteArrayOutputStream();
						// result.compress(Bitmap.CompressFormat.PNG, 100, stream);
						// return stream.toByteArray();
					}
				}
			}
			// Kein Map Pack am Start!
			// Falls Kachel im Cache liegt, diese von dort laden!
			if (cachedTileAge != 0)
			{
				Bitmap result = BitmapFactory.decodeFile(cachedTileFilename);
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				result.compress(Bitmap.CompressFormat.PNG, 100, stream);
				return stream.toByteArray();
			}
		}
		catch (Exception exc)
		{
			Logger.Error("Manager", "Exception", exc);
			/*
			 * #if DEBUG Global.AddLog("Manager.LoadLocalBitmap: " + exc.ToString()); #endif
			 */
		}
		return null;
	}

	// / <summary>
	// / Versucht eine Kachel aus dem Cache oder Map Pack zu laden
	// / </summary>
	// / <param name="layer">Layer</param>
	// / <param name="desc">Descriptor der Kachel</param>
	// / <returns>Bitmap oder null, falls Kachel nicht lokal vorliegt</returns>
	// private MapDatabase mapDatabase = null;
	// private CanvasRenderer renderer = null;
	// private Bitmap tileBitmap = null;
	private String mapsForgeFile = "";

	public Bitmap LoadLocalBitmap(Layer layer, Descriptor desc)
	{
		/*
		 * if (layer.isMapsForge) { if ((mapDatabase == null) || (!mapsForgeFile.equalsIgnoreCase(layer.Name))) { mapDatabase = new
		 * MapDatabase(); mapDatabase.openFile(CB_Core.Config.settings.MapPackFolder.getValue() + "/" + layer.Name); renderer = new
		 * CanvasRenderer(); renderer.setDatabase(mapDatabase); tileBitmap = Bitmap.createBitmap(256, 256, Config.RGB_565);
		 * renderer.setupMapGenerator(tileBitmap); mapsForgeFile = layer.Name; } Tile tile = new Tile(desc.X, desc.Y, (byte) desc.Zoom);
		 * 
		 * 
		 * // Original value = 1.333f now 1.333 * dpiScaleFactorX from MapView
		 * 
		 * float DPIawareFaktor = (float) (MapView.dpiScaleFactorX * 1.333);
		 * 
		 * MapGeneratorJob job = new MapGeneratorJob(tile, MapViewMode.CANVAS_RENDERER, "xxx", DPIawareFaktor, false, false, false);
		 * 
		 * // renderer.setupMapGenerator(tileBitmap); renderer.prepareMapGeneration(); renderer.executeJob(job); // Bitmap bit =
		 * renderer.tileBitmap.copy(Config.RGB_565, true);
		 * 
		 * ByteArrayOutputStream baos = new ByteArrayOutputStream(); renderer.tileBitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
		 * Bitmap bitj = BitmapFactory.decodeByteArray(baos.toByteArray(), 0, baos.size()); try { baos.close(); } catch (IOException e) { //
		 * TODO Auto-generated catch block e.printStackTrace(); } return bitj; }
		 */
		try
		{
			// Schauen, ob Tile im Cache liegt
			String cachedTileFilename = layer.GetLocalFilename(desc);

			long cachedTileAge = 0;

			if (FileIO.FileExists(cachedTileFilename))
			{
				File info = new File(cachedTileFilename);
				cachedTileAge = info.lastModified();
			}

			// Kachel im Pack suchen
			for (int i = 0; i < mapPacks.size(); i++)
			{
				Pack mapPack = (Pack) mapPacks.get(i);
				if ((mapPack.Layer.Name.equalsIgnoreCase(layer.Name)) && (mapPack.MaxAge >= cachedTileAge))
				{
					BoundingBox bbox = mapPacks.get(i).Contains(desc);

					if (bbox != null) return ((Pack) (mapPacks.get(i))).LoadFromBoundingBox(bbox, desc);
				}
			}
			// Kein Map Pack am Start!
			// Falls Kachel im Cache liegt, diese von dort laden!
			if (cachedTileAge != 0)
			{
				return BitmapFactory.decodeFile(cachedTileFilename);
			}
		}
		catch (Exception exc)
		{
			/*
			 * #if DEBUG Global.AddLog("Manager.LoadLocalBitmap: " + exc.ToString()); #endif
			 */
		}
		return null;
	}

	// / <summary>
	// / Läd die Kachel mit dem übergebenen Descriptor
	// / </summary>
	// / <param name="layer"></param>
	// / <param name="tile"></param>
	// / <returns></returns>
	public boolean CacheTile(Layer layer, Descriptor tile)
	{
		// Gibts die Kachel schon in einem Mappack? Dann kann sie übersprungen
		// werden!
		for (PackBase pack : mapPacks)
			if (pack.Layer == layer) if (pack.Contains(tile) != null) return true;

		String filename = layer.GetLocalFilename(tile);
		String path = layer.GetLocalPath(tile);
		String url = layer.GetUrl(tile);

		// Falls Kachel schon geladen wurde, kann sie übersprungen werden
		synchronized (this)
		{
			if (FileIO.FileExists(filename)) return true;
		}

		// Kachel laden
		HttpClient httpclient = new DefaultHttpClient();
		HttpResponse response = null;

		try
		{
			response = httpclient.execute(new HttpGet(url));
			StatusLine statusLine = response.getStatusLine();
			if (statusLine.getStatusCode() == HttpStatus.SC_OK)
			{
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				response.getEntity().writeTo(out);
				out.close();

				String responseString = out.toString();

				// Verzeichnis anlegen
				synchronized (this)
				{
					if (!FileIO.DirectoryExists(path)) return false;
				}
				// Datei schreiben
				synchronized (this)
				{
					FileOutputStream stream = new FileOutputStream(filename, false);

					out.writeTo(stream);
					stream.close();
				}

				NumTilesLoaded++;
				// Global.TransferredBytes += result.Length;

				// ..more logic
			}
			else
			{
				// Closes the connection.
				response.getEntity().getContent().close();
				// throw new IOException(statusLine.getReasonPhrase());
				return false;
			}
			/*
			 * webRequest = (HttpWebRequest)WebRequest.Create(url); webRequest.Timeout = 15000; webRequest.Proxy = Global.Proxy; webResponse
			 * = webRequest.GetResponse(); if (!webRequest.HaveResponse) return false; responseStream = webResponse.GetResponseStream();
			 * byte[] result = Global.ReadFully(responseStream, 64000); // Verzeichnis anlegen lock (this) if (!Directory.Exists(path))
			 * Directory.CreateDirectory(path); // Datei schreiben lock (this) { stream = new FileStream(filename, FileMode.CreateNew);
			 * stream.Write(result, 0, result.Length); } NumTilesLoaded++; Global.TransferredBytes += result.Length;
			 */
		}
		catch (Exception ex)
		{
			return false;
		}
		/*
		 * finally { if (stream != null) { stream.Close(); stream = null; } if (responseStream != null) { responseStream.Close();
		 * responseStream = null; } if (webResponse != null) { webResponse.Close(); webResponse = null; } if (webRequest != null) {
		 * webRequest.Abort(); webRequest = null; } GC.Collect(); }
		 */
		return true;
	}

	@Override
	protected ImageData getImagePixel(byte[] img)
	{
		Bitmap bitmap = BitmapFactory.decodeByteArray(img, 0, img.length);
		// Buffer dst = null;
		int[] pixels = new int[bitmap.getWidth() * bitmap.getHeight()];
		// bitmap.getPixels(pixels, 0, 0, 0, 0, bitmap.getWidth(), bitmap.getHeight());

		bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

		ImageData imgData = new ImageData();
		imgData.width = bitmap.getWidth();
		imgData.height = bitmap.getHeight();
		imgData.PixelColorArray = pixels;

		return imgData;
	}

	@Override
	protected byte[] getImageFromData(ImageData imgData)
	{
		Bitmap bmp = Bitmap.createBitmap(imgData.PixelColorArray, imgData.width, imgData.height, Bitmap.Config.ARGB_8888);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bmp.compress(Bitmap.CompressFormat.PNG, 100, baos);
		byte[] b = baos.toByteArray();
		return b;
	}
}
