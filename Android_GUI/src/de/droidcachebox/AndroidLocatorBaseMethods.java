package de.droidcachebox;

import CB_Locator.LocatorBasePlatFormMethods;
import CB_Locator.Map.BoundingBox;
import CB_Locator.Map.Descriptor;
import CB_Utils.Log.Log;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.util.Arrays;

public class AndroidLocatorBaseMethods implements LocatorBasePlatFormMethods.Methods {
    private static final String sKlasse = "AndroidLocatorBaseMethods";
    public AndroidLocatorBaseMethods() {

    }

    public Bitmap loadFromBoundingBox(String filename, BoundingBox bbox, Descriptor desc) {
        try {
            byte[] buffer = loadFromBoundingBoxByteArray(filename, bbox, desc);
            if (buffer == null)
                return null;

            Bitmap result = BitmapFactory.decodeByteArray(buffer, 0, buffer.length);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            result.compress(Bitmap.CompressFormat.JPEG, 80, baos);
            Bitmap bitj = BitmapFactory.decodeByteArray(baos.toByteArray(), 0, baos.size());
            result.recycle();
            baos.close();
            return bitj;
        } catch (Exception ex) {
            Log.err(sKlasse, "LoadFromBoundingBox");
        }
        return null;
    }

    @Override
    public byte[] loadFromBoundingBoxByteArray(String filename, BoundingBox bbox, Descriptor desc) {
        try {
            if (bbox.Zoom != desc.getZoom())
                return null;

            int index = (desc.getY() - bbox.MinY) * bbox.Stride + (desc.getX() - bbox.MinX) - 1;
            long offset = bbox.OffsetToIndex + index * 8;

            FileInputStream stream = new FileInputStream(filename);
            /* Stream stream = new FileStream(Filename, FileMode.Open, FileAccess.Read); */
            // stream.Seek(offset, SeekOrigin.Begin);
            stream.skip(offset);

            // BinaryReader reader = new BinaryReader(stream);
            DataInputStream reader = new DataInputStream(stream);

            long tileOffset = Long.reverseBytes(reader.readLong());
            long nextOffset = Long.reverseBytes(reader.readLong());
            int length = (int) (nextOffset - tileOffset);

            if (length == 0) {
                reader.close();
                return null;
            }

            stream.skip(tileOffset - offset - 16);
            byte[] buffer = new byte[length];
            stream.read(buffer, 0, length);

            reader.close();

            // check for support / conversion
            byte[] signature = new byte[]{(byte) 137, (byte) 80, (byte) 78, (byte) 71, (byte) 13, (byte) 10, (byte) 26, (byte) 10};
            if (Arrays.equals(signature, get(buffer, 0, 8))) {
                // es ist ein png
                byte BitDepth = buffer[24];
                // byte ColourType = buffer[25];
                // byte CompressionMethod = buffer[26];
                // BitDepth not supported by pixmap
                switch (BitDepth) {
                    case 4:
                        // Log.debug(log, "[PackBase] unsupported png in Pack " + this.Filename + " tile: " + desc);
                        Bitmap result = BitmapFactory.decodeByteArray(buffer, 0, buffer.length);
                        ByteArrayOutputStream bas = new ByteArrayOutputStream();
                        result.compress(Bitmap.CompressFormat.JPEG, 80, bas);
                        result.recycle();
                        byte[] data = bas.toByteArray();
                        bas.close();
                        return data;
                    // break;
                    case 8:
                        // supported
                        break;
                    default:
                        // perhaps supported
                        break;
                }
            }
            return buffer;
        } catch (Exception ex) {
            Log.err(sKlasse, "LoadFromBoundingBoxByteArray", ex);
        }

        return null;

    }

    /**
     * Gets the subarray of length <tt>length</tt> from <tt>array</tt> that starts at <tt>offset</tt>.
     */
    private static byte[] get(byte[] array, int offset, int length) {
        byte[] result = new byte[length];
        System.arraycopy(array, offset, result, 0, length);
        return result;
    }

}
