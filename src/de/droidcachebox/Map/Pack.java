package de.droidcachebox.Map;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import org.apache.http.util.EncodingUtils;

import de.droidcachebox.Global;

import de.droidcachebox.Geocaching.Cache;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.method.DateTimeKeyListener;

public class Pack implements Comparable<Pack> {

    public static boolean Cancel = false;
	/*
  public class Pack : IComparable
  {

    public delegate void ProgressChanged(String message, int count, int total);
    public event ProgressChanged OnProgressChanged;
*/
    public Layer Layer = null;

    /// <summary>
    /// true, falls dieses Pack mit OSM-Karten überlagert werden soll
    /// </summary>
    public boolean IsOverlay = false;

    /// <summary>
    /// Maximales Alter einer der enthaltenen Kacheln
    /// </summary>
    public long MaxAge = 0;

    /// <summary>
    /// Filename des Map Packs
    /// </summary>
    public String Filename = "";

    public ArrayList<BoundingBox> BoundingBoxes = new ArrayList<BoundingBox>();

    /// <summary>
    /// Überprüft, ob der Descriptor in diesem Map Pack enthalten ist und liefert
    /// die BoundingBox, falls dies der Fall ist, bzw. null
    /// </summary>
    /// <param name="desc">Deskriptor, dessen </param>
    /// <returns></returns>
    public BoundingBox Contains(Descriptor desc)
    {
      for (BoundingBox bbox : BoundingBoxes)
        if (bbox.Zoom == desc.Zoom && desc.X <= bbox.MaxX && desc.X >= bbox.MinX && desc.Y <= bbox.MaxY && desc.Y >= bbox.MinY)
          return bbox;

      return null;
    }

    public Pack(Layer layer)
    {
      this.Layer = layer;
    }

    public Pack(Manager manager, String file) throws IOException
    {
      Filename = file;

      File queryFile = new File(file);
      FileInputStream stream = new FileInputStream(queryFile);
      DataInputStream reader = new DataInputStream(stream);
      
/*      
      DataInputStream reader = new DataInputStream()
      Stream stream = new FileStream(file, FileMode.Open);
      BinaryReader reader = new BinaryReader(stream);
*/
      String layerName = readString(reader, 32);
      String friendlyName = readString(reader, 128);
      String url = readString(reader, 256);
      Layer = manager.GetLayerByName(layerName, friendlyName, url);

      long ticks = Long.reverseBytes(reader.readLong());
      MaxAge = ticks;

      int numBoundingBoxes = Integer.reverseBytes(reader.readInt());
      for (int i = 0; i < numBoundingBoxes; i++)
        BoundingBoxes.add(new BoundingBox(reader));

      reader.close();
      stream.close();
    }


    public int NumTilesTotal()
    {
        int result = 0;
        for (BoundingBox bbox : BoundingBoxes)
          result += bbox.NumTilesTotal();

        return result;
    }

    public void GeneratePack(String filename, long maxAge, int minZoom, int maxZoom, double minLat, double maxLat, double minLon, double maxLon) throws IOException
    {
      MaxAge = maxAge;
      Filename = filename;

      CreateBoudingBoxesFromBounds(minZoom, maxZoom, minLat, maxLat, minLon, maxLon);
/*
      FileStream stream = new FileStream(filename, FileMode.Create);
      BinaryWriter writer = new BinaryWriter(stream);
*/
      FileOutputStream stream = new FileOutputStream(filename);
      DataOutputStream writer = new DataOutputStream(stream);

      Write(writer);
      writer.flush();
      writer.close();

      if (Cancel)
      {
    	  File file = new File(filename);
    	  file.delete();
      }
    }

    public void CreateBoudingBoxesFromBounds(int minZoom, int maxZoom, double minLat, double maxLat, double minLon, double maxLon)
    {
      for (int zoom = minZoom; zoom <= maxZoom; zoom++)
      {
        int minX = (int)Descriptor.LongitudeToTileX(zoom, minLon);
        int maxX = (int)Descriptor.LongitudeToTileX(zoom, maxLon);

        int minY = (int)Descriptor.LatitudeToTileY(zoom, maxLat);
        int maxY = (int)Descriptor.LatitudeToTileY(zoom, minLat);

        BoundingBoxes.add(new BoundingBox(zoom, minX, maxX, minY, maxY, 0));
      }
    }
/*
    public delegate void ProgressDelegate(String msg, int zoom, int x, int y, int num, int total);
*/
    void writeString(String text, DataOutputStream writer, int length) throws IOException
    {
      if (text.length() > length)
        text = text.substring(0, length);
      else
        while (text.length() < length)
          text += " ";
      byte[] asciiBytes = EncodingUtils.getAsciiBytes(text);
      for (int i = 0; i < length; i++)
        writer.write(asciiBytes[i]);
    }

    String readString(DataInputStream reader, int length) throws IOException
    {
      byte[] asciiBytes = new byte[length];
      int last = 0;
      for (int i = 0; i < length; i++)
      {
        asciiBytes[i] = reader.readByte();
        if (asciiBytes[i] > 32)
           	last = i;        	
      }
      return EncodingUtils.getAsciiString(asciiBytes, 0, last+1).trim();
    }


    /// <summary>
    /// Speichert ein im lokalen Dateisystem vorliegendes Pack in den writer
    /// </summary>
    /// <param name="writer"></param>
    public void Write(DataOutputStream writer) throws IOException
    {
      int numTilesTotal = NumTilesTotal();

      // Header
      writeString(Layer.Name, writer, 32);
      writeString(Layer.FriendlyName, writer, 128);
      writeString(Layer.Url, writer, 256);
      writer.writeLong(MaxAge);
      writer.writeInt(BoundingBoxes.size());

      // Offsets berechnen
      long offset = 32 + 128 + 256 + 8 + 4 + 8 + BoundingBoxes.size() * 28 /*BoundingBox.SizeOf*/;
      for (int i = 0; i < BoundingBoxes.size(); i++)
      {
        BoundingBoxes.get(i).OffsetToIndex = offset;
        offset += BoundingBoxes.get(i).NumTilesTotal() * 8;
      }

      // Bounding Boxes schreiben
      for (int i = 0; i < BoundingBoxes.size(); i++)
        BoundingBoxes.get(i).Write(writer);

      // Indexe erzeugen
      int cnt = 0;
      for (int i = 0; i < BoundingBoxes.size(); i++)
      {
        BoundingBox bbox = BoundingBoxes.get(i);

        for (int y = bbox.MinY; y <= bbox.MaxY && !Cancel; y++)
        {
          for (int x = bbox.MinX; x <= bbox.MaxX && !Cancel; x++)
          {
            // Offset zum Bild absaven
            writer.writeLong(offset);

            // Dateigröße ermitteln
            String local = Layer.GetLocalFilename(new Descriptor(x, y, bbox.Zoom));

            if (Global.FileExists(local))
            {
              File info = new File(local);
              if (info.lastModified() < MaxAge)
                Layer.DownloadTile(new Descriptor(x, y, bbox.Zoom));
            }
            else
              Layer.DownloadTile(new Descriptor(x, y, bbox.Zoom));

            // Nicht vorhandene Tiles haben die Länge 0
            if (!Global.FileExists(local))
              offset += 0;
            else
            {
              File info = new File(local);
              offset += info.length();
            }


/*
            if (OnProgressChanged != null)
              OnProgressChanged("Building index...", cnt++, numTilesTotal);
*/
          }
        }
      }

      // Zur Längenberechnung
      writer.writeLong(offset);

      // So, und nun kopieren wir noch den Mist rein
      cnt = 0;
      for (int i = 0; i < BoundingBoxes.size() && !Cancel; i++)
      {
        BoundingBox bbox = BoundingBoxes.get(i);

        for (int y = bbox.MinY; y <= bbox.MaxY && !Cancel; y++)
        {
          for (int x = bbox.MinX; x <= bbox.MaxX && !Cancel; x++)
          {
            String local = Layer.GetLocalFilename(new Descriptor(x, y, bbox.Zoom));
/*
            Stream imageStream = null;

            if (!File.Exists(local) || File.GetCreationTime(local) < MaxAge)
              if (!Layer.DownloadTile(new Descriptor(x, y, bbox.Zoom)))
                continue;

            imageStream = new FileStream(local, FileMode.Open);

            byte[] temp = Global.ReadFully(imageStream, 32000);
            writer.Write(temp);

            if (OnProgressChanged != null)
              OnProgressChanged("Linking package...", cnt++, numTilesTotal);
*/
          }
        }
      }
    }
/*
    public int CompareTo(object obj)
    {
      Pack cmp = obj as Pack;
      if (this.MaxAge < cmp.MaxAge)
        return -1;

      if (this.MaxAge > cmp.MaxAge)
        return 1;

      return 0;
    }
*/
    /// <summary>
    /// 
    /// </summary>
    /// <param name="bbox">Bounding Box</param>
    /// <param name="desc">Descriptor</param>
    /// <returns>Bitmap der Kachel</returns>
    public Bitmap LoadFromBoundingBox(BoundingBox bbox, Descriptor desc)
    {
      try
      {
    	  if (bbox.Zoom != desc.Zoom)
    		  return null;

        int index = (desc.Y - bbox.MinY) * bbox.Stride + (desc.X - bbox.MinX) - 1;
        long offset = bbox.OffsetToIndex + index * 8;

        FileInputStream stream = new FileInputStream(Filename);
/*        Stream stream = new FileStream(Filename, FileMode.Open, FileAccess.Read);*/
//        stream.Seek(offset, SeekOrigin.Begin);
        stream.skip(offset);

//        BinaryReader reader = new BinaryReader(stream);
        DataInputStream reader = new DataInputStream(stream);

        long tileOffset = Long.reverseBytes(reader.readLong());
        long nextOffset = Long.reverseBytes(reader.readLong());
        long length = nextOffset - tileOffset;

        if (length == 0)  
          return null;

        
        stream.skip(tileOffset - offset - 16);
        byte[] buffer = new byte[(int)length];
        stream.read(buffer, 0, (int)length);
        // Bitmap noch mal Kopieren, da sonst bei Google Maps Karten ein Fehler wegen Indexed Bitmap... aufgetreten ist.?.
        Bitmap result2 = BitmapFactory.decodeByteArray(buffer, 0, (int)length);
        Bitmap result = result2.copy(Config.RGB_565, true);
        result2.recycle();

        return result;
      }
      catch (Exception exc)
      {
    	  String s = exc.getMessage(); 
/*#if DEBUG
        Global.AddLog("Pack.LoadFromBoundingBox: Out of memory!" + exc.ToString());
        Global.AddMemoryLog();
#endif*/
      }

      return null;
    
	}

    @Override
	public int compareTo(Pack arg0) 
    {
        if (this.MaxAge < arg0.MaxAge)
          return -1;

        if (this.MaxAge > arg0.MaxAge)
          return 1;

        return 0;
    }
}
