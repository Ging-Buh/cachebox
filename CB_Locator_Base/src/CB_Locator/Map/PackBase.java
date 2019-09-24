package CB_Locator.Map;

import CB_Locator.Map.Layer.LayerType;
import CB_Locator.Map.Layer.MapType;
import CB_Utils.Util.FileIO;
import CB_Utils.fileProvider.File;
import CB_Utils.fileProvider.FileFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public abstract class PackBase implements Comparable<PackBase> {

    public static boolean Cancel = false;
    public long MaxAge = 0;
    public Layer layer = null;
    public boolean IsOverlay = false;
    public String Filename = "";
    public ArrayList<BoundingBox> BoundingBoxes = new ArrayList<BoundingBox>();

    public PackBase(Layer layer) {
        this.layer = layer;
    }

    public PackBase(String file) throws IOException {
        Filename = file;

        File queryFile = FileFactory.createFile(file);
        FileInputStream stream = queryFile.getFileInputStream();
        DataInputStream reader = new DataInputStream(stream);

        /*
         * DataInputStream reader = new DataInputStream() Stream stream = new FileStream(file, FileMode.Open); BinaryReader reader = new
         * BinaryReader(stream);
         */
        String layerName = readString(reader, 32);
        String friendlyName = readString(reader, 128);
        String url = readString(reader, 256);
        layer = new Layer(MapType.BITMAP, LayerType.normal, Layer.StorageType.PNG, layerName, friendlyName, url);

        long ticks = Long.reverseBytes(reader.readLong());
        MaxAge = ticks;

        int numBoundingBoxes = Integer.reverseBytes(reader.readInt());
        for (int i = 0; i < numBoundingBoxes; i++)
            BoundingBoxes.add(new BoundingBox(reader));

        reader.close();
        stream.close();

    }

    /**
     * Gets the subarray of length <tt>length</tt> from <tt>array</tt> that starts at <tt>offset</tt>.
     */
    protected static byte[] get(byte[] array, int offset, int length) {
        byte[] result = new byte[length];
        System.arraycopy(array, offset, result, 0, length);
        return result;
    }

    public abstract byte[] LoadFromBoundingBoxByteArray(BoundingBox bbox, Descriptor desc);

    // make a new one from the existing BoundingBoxes
    // WritePackFromBoundingBoxes();
    public void WritePackFromBoundingBoxes() throws IOException {
        /*
         * FileStream stream = new FileStream(filename, FileMode.Create); BinaryWriter writer = new BinaryWriter(stream);
         */
        FileOutputStream stream = new FileOutputStream(Filename + ".new");
        DataOutputStream writer = new DataOutputStream(stream);

        Write(writer);
        writer.flush();
        writer.close();

        if (Cancel) {
            File file = FileFactory.createFile(Filename);
            file.delete();
        }
    }

    // / <summary>
    // / überprüft, ob der Descriptor in diesem Map Pack enthalten ist und liefert
    // / die BoundingBox, falls dies der Fall ist, bzw. null
    // / </summary>
    // / <param name="desc">Deskriptor, dessen </param>
    // / <returns></returns>
    public BoundingBox contains(Descriptor desc) {
        for (BoundingBox bbox : BoundingBoxes)
            if (bbox.Zoom == desc.getZoom() && desc.getX() <= bbox.MaxX && desc.getX() >= bbox.MinX && desc.getY() <= bbox.MaxY && desc.getY() >= bbox.MinY)
                return bbox;
        return null;
    }

    public int NumTilesTotal() {
        int result = 0;
        for (BoundingBox bbox : BoundingBoxes)
            result += bbox.NumTilesTotal();

        return result;
    }

    /*
     * public delegate void ProgressDelegate(String msg, int zoom, int x, int y, int num, int total);
     */
    protected void writeString(String text, DataOutputStream writer, int length) throws IOException {
        if (text.length() > length)
            text = text.substring(0, length);
        else
            while (text.length() < length)
                text += " ";
        byte[] asciiBytes = text.getBytes(StandardCharsets.US_ASCII);
        for (int i = 0; i < length; i++)
            writer.write(asciiBytes[i]);
    }

    protected String readString(DataInputStream reader, int length) throws IOException {
        byte[] asciiBytes = new byte[length];
        int last = 0;
        for (int i = 0; i < length; i++) {
            asciiBytes[i] = reader.readByte();
            if (asciiBytes[i] > 32)
                last = i;
        }
        return new String(asciiBytes, 0, last + 1, StandardCharsets.US_ASCII);
    }

    public void CreateBoudingBoxesFromBounds(int minZoom, int maxZoom, double minLat, double maxLat, double minLon, double maxLon) {
        for (int zoom = minZoom; zoom <= maxZoom; zoom++) {
            int minX = (int) Descriptor.LongitudeToTileX(zoom, minLon);
            int maxX = (int) Descriptor.LongitudeToTileX(zoom, maxLon);

            int minY = (int) Descriptor.LatitudeToTileY(zoom, maxLat);
            int maxY = (int) Descriptor.LatitudeToTileY(zoom, minLat);

            BoundingBoxes.add(new BoundingBox(zoom, minX, maxX, minY, maxY, 0));
        }
    }

    public void GeneratePack(String filename, long maxAge, int minZoom, int maxZoom, double minLat, double maxLat, double minLon, double maxLon) throws IOException {
        MaxAge = maxAge;
        Filename = filename;

        CreateBoudingBoxesFromBounds(minZoom, maxZoom, minLat, maxLat, minLon, maxLon);
        /*
         * FileStream stream = new FileStream(filename, FileMode.Create); BinaryWriter writer = new BinaryWriter(stream);
         */
        FileOutputStream stream = new FileOutputStream(filename);
        DataOutputStream writer = new DataOutputStream(stream);

        Write(writer);
        writer.flush();
        writer.close();

        if (Cancel) {
            File file = FileFactory.createFile(filename);
            file.delete();
        }
    }

    // / <summary>
    // / Speichert ein im lokalen Dateisystem vorliegendes Pack in den writer
    // / </summary>
    // / <param name="writer"></param>
    public void Write(DataOutputStream writer) throws IOException {
        // int numTilesTotal = NumTilesTotal();

        // Header
        writeString(layer.Name, writer, 32);
        writeString(layer.FriendlyName, writer, 128);
        writeString(layer.Url, writer, 256);
        writer.writeLong(Long.reverseBytes(MaxAge));
        writer.writeInt(Integer.reverseBytes(BoundingBoxes.size()));

        // Offsets berechnen
        long offset = 32 + 128 + 256 + 8 + 4 + 8 + BoundingBoxes.size() * 28 /* BoundingBox.SizeOf */;
        for (int i = 0; i < BoundingBoxes.size(); i++) {
            BoundingBoxes.get(i).OffsetToIndex = offset;
            offset += BoundingBoxes.get(i).NumTilesTotal() * 8;
        }

        // Bounding Boxes schreiben
        for (int i = 0; i < BoundingBoxes.size(); i++)
            BoundingBoxes.get(i).Write(writer);

        // Indexe erzeugen
        for (int i = 0; i < BoundingBoxes.size(); i++) {
            BoundingBox bbox = BoundingBoxes.get(i);

            for (int y = bbox.MinY; y <= bbox.MaxY && !Cancel; y++) {
                for (int x = bbox.MinX; x <= bbox.MaxX && !Cancel; x++) {
                    // Offset zum Bild absaven
                    writer.writeLong(Long.reverseBytes(offset));

                    Descriptor desc = new Descriptor(x, y, bbox.Zoom, false);

                    // Dateigröße ermitteln
                    String local = layer.GetLocalFilename(desc);

                    if (FileIO.fileExists(local)) {
                        File info = FileFactory.createFile(local);
                        if (info.lastModified() < MaxAge)
                            layer.DownloadTile(desc);
                    } else
                        layer.DownloadTile(desc);

                    // Nicht vorhandene Tiles haben die L�nge 0
                    if (!FileIO.fileExists(local))
                        offset += 0;
                    else {
                        File info = FileFactory.createFile(local);
                        offset += info.length();
                    }

                    /*
                     * if (OnProgressChanged != null) OnProgressChanged("Building index...", cnt++, numTilesTotal);
                     */

                    desc.dispose();

                }
            }
        }

        // Zur Längenberechnung
        writer.writeLong(Long.reverseBytes(offset));

        // So, und nun kopieren wir noch den Mist rein
        for (int i = 0; i < BoundingBoxes.size() && !Cancel; i++) {
            BoundingBox bbox = BoundingBoxes.get(i);

            for (int y = bbox.MinY; y <= bbox.MaxY && !Cancel; y++) {
                for (int x = bbox.MinX; x <= bbox.MaxX && !Cancel; x++) {
                    Descriptor desc = new Descriptor(x, y, bbox.Zoom, false);

                    String local = layer.GetLocalFilename(desc);
                    File f = FileFactory.createFile(local);
                    if (!f.exists() || f.lastModified() < MaxAge)
                        if (!layer.DownloadTile(desc))
                            continue;
                    FileInputStream imageStream = new FileInputStream(local);
                    int anzAvailable = (int) f.length();
                    byte[] temp = new byte[anzAvailable];
                    imageStream.read(temp);
                    writer.write(temp);
                    imageStream.close();

                    // if (OnProgressChanged != null) OnProgressChanged("Linking package...", cnt++, numTilesTotal);
                    desc.dispose();
                }
            }
        }
    }

    @Override
    public int compareTo(PackBase arg0) {
        if (this.MaxAge < arg0.MaxAge)
            return -1;

        if (this.MaxAge > arg0.MaxAge)
            return 1;

        return 0;
    }

}