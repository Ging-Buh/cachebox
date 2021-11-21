package de.droidcachebox.locator.map;

import static de.droidcachebox.locator.LocatorBasePlatFormMethods.loadFromBoundingBoxByteArray;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import de.droidcachebox.utils.AbstractFile;
import de.droidcachebox.utils.FileFactory;
import de.droidcachebox.utils.FileIO;

public class MapPackLayer extends Layer implements Comparable<MapPackLayer> {

    public static boolean Cancel = false;
    long maxAge;
    private String fileName;
    private ArrayList<BoundingBox> BoundingBoxes = new ArrayList<>();

    MapPackLayer(String file) throws Exception {
        fileName = file;
        AbstractFile queryAbstractFile = FileFactory.createFile(file);
        FileInputStream stream = queryAbstractFile.getFileInputStream();
        DataInputStream reader = new DataInputStream(stream);

        name = readString(reader, 32);
        friendlyName = readString(reader, 128);
        url = readString(reader, 256);
        mapType = MapType.MapPack;
        layerUsage = LayerUsage.normal;
        storageType = Layer.StorageType.PNG;

        maxAge = Long.reverseBytes(reader.readLong());

        int numBoundingBoxes = Integer.reverseBytes(reader.readInt());
        for (int i = 0; i < numBoundingBoxes; i++)
            BoundingBoxes.add(new BoundingBox(reader));

        reader.close();
        stream.close();

    }

    // make a new one from the existing BoundingBoxes
    // WritePackFromBoundingBoxes();
    public void WritePackFromBoundingBoxes() throws IOException {
        /*
         * FileStream stream = new FileStream(filename, FileMode.Create); BinaryWriter writer = new BinaryWriter(stream);
         */
        FileOutputStream stream = new FileOutputStream(fileName + ".new");
        DataOutputStream writer = new DataOutputStream(stream);

        Write(writer);
        writer.flush();
        writer.close();

        if (Cancel) {
            AbstractFile abstractFile = FileFactory.createFile(fileName);
            abstractFile.delete();
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
    private void writeString(String text, DataOutputStream writer, int length) throws IOException {
        if (text.length() > length)
            text = text.substring(0, length);
        else
            while (text.length() < length)
                text += " ";
        byte[] asciiBytes = text.getBytes(StandardCharsets.US_ASCII);
        for (int i = 0; i < length; i++)
            writer.write(asciiBytes[i]);
    }

    private String readString(DataInputStream reader, int length) throws IOException {
        byte[] asciiBytes = new byte[length];
        int last = 0;
        for (int i = 0; i < length; i++) {
            asciiBytes[i] = reader.readByte();
            if (asciiBytes[i] > 32)
                last = i;
        }
        return new String(asciiBytes, 0, last + 1, StandardCharsets.US_ASCII);
    }

    private void createBoudingBoxesFromBounds(int minZoom, int maxZoom, double minLat, double maxLat, double minLon, double maxLon) {
        for (int zoom = minZoom; zoom <= maxZoom; zoom++) {
            int minX = (int) Descriptor.longitudeToTileX(zoom, minLon);
            int maxX = (int) Descriptor.longitudeToTileX(zoom, maxLon);

            int minY = (int) Descriptor.latitudeToTileY(zoom, maxLat);
            int maxY = (int) Descriptor.latitudeToTileY(zoom, minLat);

            BoundingBoxes.add(new BoundingBox(zoom, minX, maxX, minY, maxY, 0));
        }
    }

    public void generatePack(String fileName, long maxAge, int minZoom, int maxZoom, double minLat, double maxLat, double minLon, double maxLon) throws IOException {
        this.fileName = fileName;
        this.maxAge = maxAge;

        createBoudingBoxesFromBounds(minZoom, maxZoom, minLat, maxLat, minLon, maxLon);
        /*
         * FileStream stream = new FileStream(filename, FileMode.Create); BinaryWriter writer = new BinaryWriter(stream);
         */
        FileOutputStream stream = new FileOutputStream(fileName);
        DataOutputStream writer = new DataOutputStream(stream);

        Write(writer);
        writer.flush();
        writer.close();

        if (Cancel) {
            AbstractFile abstractFile = FileFactory.createFile(fileName);
            abstractFile.delete();
        }
    }

    // / <summary>
    // / Speichert ein im lokalen Dateisystem vorliegendes Pack in den writer
    // / </summary>
    // / <param name="writer"></param>
    public void Write(DataOutputStream writer) throws IOException {
        // int numTilesTotal = NumTilesTotal();

        // Header
        writeString(name, writer, 32);
        writeString(friendlyName, writer, 128);
        writeString(url, writer, 256);
        writer.writeLong(Long.reverseBytes(maxAge));
        writer.writeInt(Integer.reverseBytes(BoundingBoxes.size()));

        // Offsets berechnen
        long offset = 32 + 128 + 256 + 8 + 4 + 8 + BoundingBoxes.size() * 28 /* BoundingBox.SizeOf */;
        for (BoundingBox boundingBox : BoundingBoxes) {
            boundingBox.OffsetToIndex = offset;
            offset += boundingBox.NumTilesTotal() * 8;
        }

        // Bounding Boxes schreiben
        for (BoundingBox boundingBox : BoundingBoxes) boundingBox.Write(writer);

        // Indexe erzeugen
        for (BoundingBox bbox : BoundingBoxes) {
            for (int y = bbox.MinY; y <= bbox.MaxY && !Cancel; y++) {
                for (int x = bbox.MinX; x <= bbox.MaxX && !Cancel; x++) {
                    // Offset zum Bild absaven
                    writer.writeLong(Long.reverseBytes(offset));

                    Descriptor desc = new Descriptor(x, y, bbox.Zoom);

                    String local = getLocalFilename(desc);

                    if (FileIO.fileExists(local)) {
                        AbstractFile info = FileFactory.createFile(local);
                        if (info.lastModified() < maxAge)
                            downloadTile(desc);
                    } else
                        downloadTile(desc);

                    // Nicht vorhandene Tiles haben die L�nge 0
                    if (!FileIO.fileExists(local))
                        offset += 0;
                    else {
                        AbstractFile info = FileFactory.createFile(local);
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
        for (BoundingBox bbox : BoundingBoxes) {
            for (int y = bbox.MinY; y <= bbox.MaxY; y++) {
                for (int x = bbox.MinX; x <= bbox.MaxX; x++) {
                    if (Cancel) break;
                    Descriptor desc = new Descriptor(x, y, bbox.Zoom);

                    String local = getLocalFilename(desc);
                    AbstractFile f = FileFactory.createFile(local);
                    if (!f.exists() || f.lastModified() < maxAge)
                        if (!downloadTile(desc))
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

    byte[] LoadFromBoundingBoxByteArray(BoundingBox bbox, Descriptor desc) {
        return loadFromBoundingBoxByteArray(fileName, bbox, desc);
    }

    @Override
    public int compareTo(MapPackLayer arg0) {
        return Long.compare(maxAge, arg0.maxAge);
    }

}