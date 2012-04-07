package de.cachebox_test.Map;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import org.mapsforge.android.maps.CanvasRenderer;
import org.mapsforge.android.maps.MapDatabase;
import org.mapsforge.android.maps.MapGeneratorJob;
import org.mapsforge.android.maps.MapViewMode;
import org.mapsforge.android.maps.Tile;

import CB_Core.FileIO;
import CB_Core.Map.BoundingBox;
import CB_Core.Map.Descriptor;
import CB_Core.Map.Layer;
import CB_Core.Map.ManagerBase;
import CB_Core.Map.PackBase;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import de.cachebox_test.Global;
import de.cachebox_test.main;
import de.cachebox_test.Views.MapView;

public class Manager extends ManagerBase
{

	/*
	 * public delegate void FetchAreaCallback();
	 */

	public Manager()
	{
		// for the Access to the manager in the CB_Core
		CB_Core.Map.ManagerBase.Manager = this;
		// Layers.add(new Layer("MapsForge", "MapsForge", ""));
		Layers.add(new Layer("Mapnik", "Mapnik", "http://a.tile.openstreetmap.org/"));
		Layers.add(new Layer("OSM Cycle Map", "Open Cycle Map", "http://b.andy.sandbox.cloudmade.com/tiles/cycle/"));
		Layers.add(new Layer("TilesAtHome", "Osmarender", "http://a.tah.openstreetmap.org/Tiles/tile/"));
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
		if (layer.isMapsForge)
		{
			if ((mapDatabase == null) || (!mapsForgeFile.equalsIgnoreCase(layer.Name)))
			{
				mapDatabase = new MapDatabase();
				mapDatabase.openFile(CB_Core.Config.settings.MapPackFolder.getValue() + "/" + layer.Name);
				renderer = new CanvasRenderer();
				renderer.setDatabase(mapDatabase);
				tileBitmap = Bitmap.createBitmap(256, 256, Config.RGB_565);
				renderer.setupMapGenerator(tileBitmap);
				mapsForgeFile = layer.Name;
			}
			Tile tile = new Tile(desc.X, desc.Y, (byte) desc.Zoom);

			/**
			 * Original value = 1.333f now 1.333 * dpiScaleFactorX from MapView
			 */
			float DPIawareFaktor = (float) (MapView.dpiScaleFactorX * 1.333);

			MapGeneratorJob job = new MapGeneratorJob(tile, MapViewMode.CANVAS_RENDERER, "xxx", DPIawareFaktor, false, false, false);

			// renderer.setupMapGenerator(tileBitmap);
			renderer.prepareMapGeneration();
			renderer.executeJob(job);
			// Bitmap bit = renderer.tileBitmap.copy(Config.RGB_565, true);

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			if (main.N)
			{
				Bitmap b = Bitmap.createBitmap(256, 256, Config.RGB_565);
				Canvas c = new Canvas(b);
				c.drawBitmap(renderer.tileBitmap, 0, 0, main.N ? Global.invertPaint : new Paint());
				b.compress(Bitmap.CompressFormat.PNG, 50, baos);
			}
			else
			{
				renderer.tileBitmap.compress(Bitmap.CompressFormat.PNG, 50, baos);
			}

			byte[] result = baos.toByteArray();

			try
			{
				baos.close();
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return result;
		}

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
	private MapDatabase mapDatabase = null;
	private CanvasRenderer renderer = null;
	private Bitmap tileBitmap = null;
	private String mapsForgeFile = "";

	public Bitmap LoadLocalBitmap(Layer layer, Descriptor desc)
	{
		if (layer.isMapsForge)
		{
			if ((mapDatabase == null) || (!mapsForgeFile.equalsIgnoreCase(layer.Name)))
			{
				mapDatabase = new MapDatabase();
				mapDatabase.openFile(CB_Core.Config.settings.MapPackFolder.getValue() + "/" + layer.Name);
				renderer = new CanvasRenderer();
				renderer.setDatabase(mapDatabase);
				tileBitmap = Bitmap.createBitmap(256, 256, Config.RGB_565);
				renderer.setupMapGenerator(tileBitmap);
				mapsForgeFile = layer.Name;
			}
			Tile tile = new Tile(desc.X, desc.Y, (byte) desc.Zoom);

			/**
			 * Original value = 1.333f now 1.333 * dpiScaleFactorX from MapView
			 */
			float DPIawareFaktor = (float) (MapView.dpiScaleFactorX * 1.333);

			MapGeneratorJob job = new MapGeneratorJob(tile, MapViewMode.CANVAS_RENDERER, "xxx", DPIawareFaktor, false, false, false);

			// renderer.setupMapGenerator(tileBitmap);
			renderer.prepareMapGeneration();
			renderer.executeJob(job);
			// Bitmap bit = renderer.tileBitmap.copy(Config.RGB_565, true);

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			renderer.tileBitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
			Bitmap bitj = BitmapFactory.decodeByteArray(baos.toByteArray(), 0, baos.size());
			try
			{
				baos.close();
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return bitj;
		}

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

}
