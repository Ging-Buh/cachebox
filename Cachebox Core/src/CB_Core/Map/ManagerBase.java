package CB_Core.Map;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import CB_Core.Config;
import CB_Core.FileIO;
import CB_Core.Map.Layer.Type;

public abstract class ManagerBase
{
	protected final int CONECTION_TIME_OUT = 15000;

	public static boolean RenderThemeChanged = true;

	public static ManagerBase Manager = null;

	public static long NumBytesLoaded = 0;

	public static int NumTilesLoaded = 0;

	public static int NumTilesCached = 0;

	public ArrayList<PackBase> mapPacks = new ArrayList<PackBase>();

	public ArrayList<TmsMap> tmsMaps = new ArrayList<TmsMap>();

	public ArrayList<Layer> Layers = new ArrayList<Layer>();

	protected String RenderTheme;

	public void setRenderTheme(String theme)
	{
		RenderTheme = theme;
		RenderThemeChanged = true;
	}

	public boolean isRenderThemeSetted()
	{
		if (RenderTheme != null && RenderTheme.length() > 0) return true;
		return false;
	}

	public PackBase CreatePack(String file) throws IOException
	{
		return new PackBase(this, file);
	}

	// / <summary>
	// / Läd ein Map Pack und fügt es dem Manager hinzu
	// / </summary>
	// / <param name="file"></param>
	// / <returns>true, falls das Pack erfolgreich geladen wurde, sonst
	// false</returns>
	public boolean LoadMapPack(String file)
	{
		try
		{
			PackBase pack = CreatePack(file);
			mapPacks.add(pack);

			// Nach Aktualität sortieren
			Collections.sort(mapPacks);
			return true;
		}
		catch (Exception exc)
		{
		}
		return false;
	}

	public Layer GetLayerByName(String Name, String friendlyName, String url)
	{
		if (Name == "OSM") Name = "Mapnik";

		for (Layer layer : Layers)
		{
			if (layer.Name.equalsIgnoreCase(Name)) return layer;
		}

		Layer newLayer = new Layer(Type.normal, Name, Name, url);
		Layers.add(newLayer);

		return newLayer;
	}

	public byte[] LoadInvertedPixmap(Layer layer, Descriptor desc)
	{
		byte[] tmp = LoadLocalPixmap(layer, desc);

		if (!Config.settings.nightMode.getValue()) return tmp;

		if (layer.isMapsForge && mapsforgeNightThemeExist()) return tmp;

		if (tmp == null) return null;

		ImageData imgData = getImagePixel(tmp);

		imgData = getImageDataWithColormatrixManipulation(NIGHT_COLOR_MATRIX, imgData);

		tmp = getImageFromData(imgData);

		return tmp;
	}

	protected boolean useInvertedNightTheme;

	public void setUseInvertedNightTheme(boolean value)
	{
		useInvertedNightTheme = value;
	}

	protected boolean mapsforgeNightThemeExist()
	{
		return false;
	}

	public class ImageData
	{
		public int[] PixelColorArray;
		public int width;
		public int height;
	}

	public byte[] LoadLocalPixmap(String layer, Descriptor desc)
	{
		return LoadLocalPixmap(GetLayerByName(layer, layer, ""), desc);
	}

	public byte[] LoadLocalPixmap(Layer layer, Descriptor desc)
	{
		if (layer == null) return null;
		// Vorerst nur im Pack suchen
		// Kachel im Pack suchen
		long cachedTileAge = 0;
		for (int i = 0; i < mapPacks.size(); i++)
		{
			PackBase mapPack = mapPacks.get(i);
			if ((mapPack.Layer.Name.equalsIgnoreCase(layer.Name)) && (mapPack.MaxAge >= cachedTileAge))
			{
				BoundingBox bbox = mapPacks.get(i).Contains(desc);

				if (bbox != null)
				{
					return mapPacks.get(i).LoadFromBoundingBoxByteArray(bbox, desc);
				}
			}
		}
		return null;
	}

	protected abstract ImageData getImagePixel(byte[] img);

	protected abstract byte[] getImageFromData(ImageData imgData);

	// / <summary>
	// / Läd die Kachel mit dem übergebenen Descriptor
	// / </summary>
	// / <param name="layer"></param>
	// / <param name="tile"></param>
	// / <returns></returns>
	public boolean CacheTile(Layer layer, Descriptor tile)
	{

		if (layer == null) return false;

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
		// set the connection timeout value to 15 seconds (15000 milliseconds)
		final HttpParams httpParams = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParams, CONECTION_TIME_OUT);

		HttpClient httpclient = new DefaultHttpClient(httpParams);
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

	/**
	 * The matrix is stored in a single array, and its treated as follows: [ a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s, t ] <br>
	 * <br>
	 * When applied to a color [r, g, b, a], the resulting color is computed as (after clamping) <br>
	 * R' = a*R + b*G + c*B + d*A + e;<br>
	 * G' = f*R + g*G + h*B + i*A + j;<br>
	 * B' = k*R + l*G + m*B + n*A + o;<br>
	 * A' = p*R + q*G + r*B + s*A + t;<br>
	 * 
	 * @param src
	 * @param matrix
	 * @return
	 */
	public static ImageData getImageDataWithColormatrixManipulation(float[] matrix, ImageData imgData)
	{

		int[] data = imgData.PixelColorArray;

		int len = data.length;

		int[] dst = new int[len];

		for (int i = 0; i < len; i++)
		{
			int[] color = new int[4];

			color[0] = (int) (data[i] >> 24) & (0xff);
			color[1] = (int) ((data[i] << 8) >> 24) & (0xff);
			color[2] = (int) ((data[i] << 16) >> 24) & (0xff);
			color[3] = (int) ((data[i] << 24) >> 24) & (0xff);

			int R = color[1];
			int G = color[2];
			int B = color[3];
			int A = color[0];

			color[1] = Math
					.max(0, Math.min(255, (int) ((matrix[0] * R) + (matrix[1] * G) + (matrix[2] * B) + (matrix[3] * A) + matrix[4])));
			color[2] = Math
					.max(0, Math.min(255, (int) ((matrix[5] * R) + (matrix[6] * G) + (matrix[7] * B) + (matrix[8] * A) + matrix[9])));
			color[3] = Math.max(0,
					Math.min(255, (int) ((matrix[10] * R) + (matrix[11] * G) + (matrix[12] * B) + (matrix[13] * A) + matrix[14])));
			color[0] = Math.max(0,
					Math.min(255, (int) ((matrix[15] * R) + (matrix[16] * G) + (matrix[17] * B) + (matrix[18] * A) + matrix[19])));

			dst[i] = ((color[0] & 0xFF) << 24) | ((color[1] & 0xFF) << 16) | ((color[2] & 0xFF) << 8) | ((color[3] & 0xFF));
		}

		imgData.PixelColorArray = dst;

		return imgData;
	}

	/**
	 * Night Color Matrix <br>
	 * <br>
	 * R= -1.0f, 0.0f, 0.0f, 0.0f, 255.0f, <br>
	 * G= 0.0f, -1.5f, 0.0f, 0.0f, 200.0f, <br>
	 * B= 0.0f, 0.0f, -1.5f, 0.0f, 0.f, <br>
	 * A= 0.0f, 0.0f, 0.0f, 0.0f, 255f <br>
	 */
	public static final float[] NIGHT_COLOR_MATRIX =
		{ /* */
		-1.0f, 0.0f, 0.0f, 0.0f, 255.0f, /* */
		0.0f, -1.5f, 0.0f, 0.0f, 200.0f, /* */
		0.0f, 0.0f, -1.5f, 0.0f, 0.f, /* */
		0.0f, 0.0f, 0.0f, 0.0f, 255f };

	public void LoadTMS(String string)
	{
		try
		{
			TmsMap tmsMap = new TmsMap(string);
			if ((tmsMap.name == null) || (tmsMap.url == null))
			{
				return;
			}
			tmsMaps.add(tmsMap);
			Layers.add(new TmsLayer(Type.normal, tmsMap));
		}
		catch (Exception ex)
		{

		}

	}

	public void LoadBSH(String string)
	{
		try
		{
			BshLayer layer = new BshLayer(Type.normal, string);
			Layers.add(layer);
		}
		catch (Exception ex)
		{

		}

	}
}
