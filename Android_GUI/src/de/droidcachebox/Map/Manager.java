package de.droidcachebox.Map;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.mapsforge.android.maps.CanvasRenderer;
import org.mapsforge.android.maps.MapDatabase;
import org.mapsforge.android.maps.MapGeneratorJob;
import org.mapsforge.android.maps.MapViewMode;
import org.mapsforge.android.maps.Tile;

import de.droidcachebox.Global;

import CB_Core.FileIO;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.Config;
import android.graphics.Color;

public class Manager {

	
/*
    public delegate void FetchAreaCallback();
*/
    public ArrayList<Layer> Layers = new ArrayList<Layer>();
    
    public static long NumBytesLoaded = 0;

    public static int NumTilesLoaded = 0;

    public static int NumTilesCached = 0;

    public ArrayList<Pack> mapPacks = new ArrayList<Pack>();

    public ArrayList<String> mapsForgeMaps = new ArrayList<String>();
    
    public Manager()
    {
//        Layers.add(new Layer("MapsForge", "MapsForge", ""));
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
	    		mapDatabase.openFile(de.droidcachebox.Config.GetString("MapPackFolder") + "/" + layer.Name);
	    		renderer = new CanvasRenderer();
	    		renderer.setDatabase(mapDatabase);
				tileBitmap = Bitmap.createBitmap(256, 256, Config.RGB_565);
				renderer.setupMapGenerator(tileBitmap);
				mapsForgeFile = layer.Name;
	    	}
			Tile tile = new Tile(desc.X, desc.Y, (byte)desc.Zoom);
			MapGeneratorJob job = new MapGeneratorJob(tile, MapViewMode.CANVAS_RENDERER, "xxx", 1.333f, false, false, false);

//			renderer.setupMapGenerator(tileBitmap);
			renderer.prepareMapGeneration();
			renderer.executeJob(job);
//			Bitmap bit = renderer.tileBitmap.copy(Config.RGB_565, true);
			
			ByteArrayOutputStream baos = new ByteArrayOutputStream();  
            renderer.tileBitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
			Bitmap bitj = BitmapFactory.decodeByteArray(baos.toByteArray(), 0, baos.size());
			try {
				baos.close();
			} catch (IOException e) {
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

    /// <summary>
    /// Läd die Kachel mit dem übergebenen Descriptor
    /// </summary>
    /// <param name="layer"></param>
    /// <param name="tile"></param>
    /// <returns></returns>
    public boolean CacheTile(Layer layer, Descriptor tile)
    {
    	// Gibts die Kachel schon in einem Mappack? Dann kann sie übersprungen werden!
    	for (Pack pack : mapPacks)
    		if (pack.Layer == layer)
    			if (pack.Contains(tile) != null)
    				return true;

    	String filename = layer.GetLocalFilename(tile);
    	String path = layer.GetLocalPath(tile);
    	String url = layer.GetUrl(tile);
    	
    	// Falls Kachel schon geladen wurde, kann sie übersprungen werden
    	synchronized (this)
    	{
    		if (FileIO.FileExists(filename))
    			return true;
    	}

    	// Kachel laden
    	HttpClient httpclient = new DefaultHttpClient();
    	HttpResponse response = null;

    	try
    	{
    		response = httpclient.execute(new HttpGet(url));
    		StatusLine statusLine = response.getStatusLine();
    		if(statusLine.getStatusCode() == HttpStatus.SC_OK){
    	        ByteArrayOutputStream out = new ByteArrayOutputStream();
    	        response.getEntity().writeTo(out);
    	        out.close();
    	        
    	        String responseString = out.toString();
    	        
    	        
        		// Verzeichnis anlegen
    	        synchronized (this)
        		{
    	        	if (!FileIO.DirectoryExists(path))
    	        		return false;
        		}
        		// Datei schreiben
        		synchronized (this)
        		{
        			FileOutputStream stream = new FileOutputStream(filename, false);
        			
        			out.writeTo(stream);
        			stream.close();
        		}

        		NumTilesLoaded++;
//        		Global.TransferredBytes += result.Length;
    	        
    	        //..more logic
    	    } else{
    	        //Closes the connection.
    	        response.getEntity().getContent().close();
//    	        throw new IOException(statusLine.getReasonPhrase());
    	        return false;
    	    }
/*    		
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
    		*/
    	}
    	catch (Exception ex)
    	{
    		return false;
    	}
/*      finally
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
      }*/
      return true;
    }	
 
}
