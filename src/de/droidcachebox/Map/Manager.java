package de.droidcachebox.Map;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import de.droidcachebox.Global;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class Manager {

	
/*
    public delegate void FetchAreaCallback();
*/
    public ArrayList<Layer> Layers = new ArrayList<Layer>();
    
    public static long NumBytesLoaded = 0;

    public static int NumTilesLoaded = 0;

    public static int NumTilesCached = 0;

    public ArrayList<Pack> mapPacks = new ArrayList<Pack>();

    public Manager()
    {
      Layers.add(new Layer("Mapnik", "Mapnik", "http://a.tile.openstreetmap.org/"));
      Layers.add(new Layer("OSM Cycle Map", "Open Cycle Map", "http://b.andy.sandbox.cloudmade.com/tiles/cycle/"));
      Layers.add(new Layer("TilesAtHome", "Osmarender", "http://a.tah.openstreetmap.org/Tiles/tile/"));
    }
    /*

    /// <summary>
    /// *berprüft, ob die übergebene Kachel bei OSM überhaupt existiert
    /// </summary>
    /// <param name="desc">Deskriptor der zu prüfenden Kachel</param>
    /// <returns>true, falls die Kachel existiert, sonst false</returns>
    public static bool ExistsTile(Descriptor desc)
    {
      int range = (int)Math.Pow(2, desc.Zoom);
      return desc.X < range && desc.Y < range && desc.X >= 0 && desc.Y >= 0;
    }
*/
    /// <summary>
    /// Läd ein Map Pack und fügt es dem Manager hinzu
    /// </summary>
    /// <param name="file"></param>
    /// <returns>true, falls das Pack erfolgreich geladen wurde, sonst false</returns>
    public boolean LoadMapPack(String file)
    {
      try
      {
        Pack pack = new Pack(this, file);
        mapPacks.add(pack);

        // Nach Aktualität sortieren
        Collections.sort(mapPacks);
        return true;
      }
      catch (Exception exc) { }
      return false;
    }

    public Layer GetLayerByName(String Name, String friendlyName, String url)
    {
      if (Name == "OSM")
        Name = "Mapnik";

      for (Layer layer : Layers)
      {
        if (layer.Name.equalsIgnoreCase(Name))
          return layer;
      }

      Layer newLayer = new Layer(Name, Name, url);
      Layers.add(newLayer);

      return newLayer;
    }

    public Bitmap LoadLocalBitmap(String layer, Descriptor desc)
    {
      return LoadLocalBitmap(GetLayerByName(layer, layer, ""), desc);
    }

    /// <summary>
    /// Versucht eine Kachel aus dem Cache oder Map Pack zu laden
    /// </summary>
    /// <param name="layer">Layer</param>
    /// <param name="desc">Descriptor der Kachel</param>
    /// <returns>Bitmap oder null, falls Kachel nicht lokal vorliegt</returns>
    public Bitmap LoadLocalBitmap(Layer layer, Descriptor desc)
    {
      try
      {
        // Schauen, ob Tile im Cache liegt
        String cachedTileFilename = layer.GetLocalFilename(desc);

        long cachedTileAge = 0;

        if (Global.FileExists(cachedTileFilename))
        {
          File info = new File(cachedTileFilename);
          cachedTileAge = info.lastModified();
        }


        // Kachel im Pack suchen
        for (int i = 0; i < mapPacks.size(); i++)
        {
        	Pack mapPack = mapPacks.get(i);
          if ((mapPack.Layer.Name.equalsIgnoreCase(layer.Name)) && (mapPack.MaxAge >= cachedTileAge))
          {
            BoundingBox bbox = mapPacks.get(i).Contains(desc);

            if (bbox != null)
              return mapPacks.get(i).LoadFromBoundingBox(bbox, desc);
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
/*#if DEBUG
        Global.AddLog("Manager.LoadLocalBitmap: " + exc.ToString());
#endif*/
      }
      return null;
    }
/*
    /// <summary>
    /// Läd die Kachel mit dem übergebenen Descriptor
    /// </summary>
    /// <param name="layer"></param>
    /// <param name="tile"></param>
    /// <returns></returns>
    public bool CacheTile(Layer layer, Descriptor tile)
    {
      // Gibts die Kachel schon in einem Mappack? Dann kann sie übersprungen werden!
      foreach (Pack pack in mapPacks)
        if (pack.Layer == layer)
          if (pack.Contains(tile) != null)
            return true;

      String filename = layer.GetLocalFilename(tile);
      String path = layer.GetLocalPath(tile);
      String url = layer.GetUrl(tile);

      // Falls Kachel schon geladen wurde, kann sie übersprungen werden
      lock (this)
        if (File.Exists(filename))
          return true;

      // Kachel laden
      HttpWebRequest webRequest = null;
      WebResponse webResponse = null;
      Stream stream = null;
      Stream responseStream = null;

      try
      {
        webRequest = (HttpWebRequest)WebRequest.Create(url);
        webRequest.Timeout = 15000;
        webRequest.Proxy = Global.Proxy;
        webResponse = webRequest.GetResponse();

        if (!webRequest.HaveResponse)
          return false;

        responseStream = webResponse.GetResponseStream();
        byte[] result = Global.ReadFully(responseStream, 64000);

        // Verzeichnis anlegen
        lock (this)
          if (!Directory.Exists(path))
            Directory.CreateDirectory(path);

        // Datei schreiben
        lock (this)
        {
          stream = new FileStream(filename, FileMode.CreateNew);
          stream.Write(result, 0, result.Length);
        }

        NumTilesLoaded++;
        Global.TransferredBytes += result.Length;
      }
      catch (Exception)
      {
        return false;
      }
      finally
      {
        if (stream != null)
        {
          stream.Close();
          stream = null;
        }

        if (responseStream != null)
        {
          responseStream.Close();
          responseStream = null;
        }

        if (webResponse != null)
        {
          webResponse.Close();
          webResponse = null;
        }


        if (webRequest != null)
        {
          webRequest.Abort();
          webRequest = null;
        }
        GC.Collect();
      }
      return true;
    }	
 */
}
