/* 
 * Copyright (C) 2014 team-cachebox.de
 *
 * Licensed under the : GNU General Public License (GPL);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.gnu.org/licenses/gpl.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.cachebox_test.Map;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import org.mapsforge.core.graphics.GraphicFactory;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.model.DisplayModel;

import CB_Locator.Map.BoundingBox;
import CB_Locator.Map.Descriptor;
import CB_Locator.Map.GL_RenderType;
import CB_Locator.Map.Layer;
import CB_Locator.Map.ManagerBase;
import CB_Locator.Map.PackBase;
import CB_Locator.Map.TileGL;
import CB_Locator.Map.TileGL.TileState;
import CB_Locator.Map.TileGL_Bmp;
import CB_Utils.Log.Logger;
import CB_Utils.Util.FileIO;
import android.graphics.BitmapFactory;
import de.cachebox_test.main;

/**
 * @author ging-buh
 * @author Longri
 */
public class AndroidManager extends ManagerBase
{
	public AndroidManager(GL_RenderType renderingTyp)
	{
		super(renderingTyp);

	}

	@Override
	public PackBase CreatePack(String file) throws IOException
	{
		return new AndroidPack(this, file);
	}

	public android.graphics.Bitmap LoadLocalBitmap(String layer, Descriptor desc)
	{
		return LoadLocalBitmap(GetLayerByName(layer, layer, ""), desc);
	}

	@Override
	public GraphicFactory getGraphicFactory()
	{
		if (AndroidGraphicFactory.INSTANCE == null)
		{
			AndroidGraphicFactory.createInstance(main.mainActivity.getApplication());
			DisplayModel.setDeviceScaleFactor(1);
		}

		return AndroidGraphicFactory.INSTANCE;
	}

	@Override
	public TileGL LoadLocalPixmap(Layer layer, Descriptor desc, int ThreadIndex)
	{

		if (layer.isMapsForge)
		{
			return getMapsforgePixMap(layer, desc, ThreadIndex);
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
						byte[] b = mapPacks.get(i).LoadFromBoundingBoxByteArray(bbox, desc);
						TileGL_Bmp bmpTile = new TileGL_Bmp(desc, b, TileState.Present);
						return bmpTile;
					}
				}
			}
			// Kein Map Pack am Start!
			// Falls Kachel im Cache liegt, diese von dort laden!
			if (cachedTileAge != 0)
			{
				android.graphics.Bitmap result = BitmapFactory.decodeFile(cachedTileFilename);
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				result.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, stream);
				byte[] b = stream.toByteArray();
				TileGL_Bmp bmpTile = new TileGL_Bmp(desc, b, TileState.Present);
				return bmpTile;
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

	public android.graphics.Bitmap LoadLocalBitmap(Layer layer, Descriptor desc)
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
		 * Bitmap bitj = BitmapFactory.decodeByteArray(baos.toByteArray(), 0, baos.size()); try { baos.close(); } catch (IOException e) { }
		 * return bitj; }
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
				AndroidPack mapPack = (AndroidPack) mapPacks.get(i);
				if ((mapPack.Layer.Name.equalsIgnoreCase(layer.Name)) && (mapPack.MaxAge >= cachedTileAge))
				{
					BoundingBox bbox = mapPacks.get(i).Contains(desc);

					if (bbox != null) return ((AndroidPack) (mapPacks.get(i))).LoadFromBoundingBox(bbox, desc);
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

		}
		return null;
	}

	@Override
	protected ImageData getImagePixel(byte[] img)
	{
		android.graphics.Bitmap bitmap = BitmapFactory.decodeByteArray(img, 0, img.length);
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
		android.graphics.Bitmap bmp = android.graphics.Bitmap.createBitmap(imgData.PixelColorArray, imgData.width, imgData.height,
				android.graphics.Bitmap.Config.ARGB_8888);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bmp.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, baos);
		byte[] b = baos.toByteArray();
		return b;
	}

}
