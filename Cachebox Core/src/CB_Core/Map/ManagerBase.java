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

import CB_Core.FileIO;

public class ManagerBase
{
	public static ManagerBase Manager = null;

	public static long NumBytesLoaded = 0;

	public static int NumTilesLoaded = 0;

	public static int NumTilesCached = 0;

	public ArrayList<PackBase> mapPacks = new ArrayList<PackBase>();

	public ArrayList<String> mapsForgeMaps = new ArrayList<String>();

	public ArrayList<Layer> Layers = new ArrayList<Layer>();

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

		Layer newLayer = new Layer(Name, Name, url);
		Layers.add(newLayer);

		return newLayer;
	}

	public byte[] LoadLocalPixmap(String layer, Descriptor desc)
	{
		return LoadLocalPixmap(GetLayerByName(layer, layer, ""), desc);
	}

	public byte[] LoadLocalPixmap(Layer layer, Descriptor desc)
	{
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

}
