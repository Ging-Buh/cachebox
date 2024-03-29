package de.droidcachebox.locator;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;

import org.mapsforge.core.graphics.GraphicFactory;
import org.mapsforge.core.graphics.TileBitmap;
import org.mapsforge.map.awt.graphics.AwtGraphicFactory;

import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.DataBufferInt;
import java.awt.image.Raster;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import javax.imageio.ImageIO;

import de.droidcachebox.gdx.graphics.mapsforge.GDXBitmap;
import de.droidcachebox.locator.map.BoundingBox;
import de.droidcachebox.locator.map.Descriptor;
import de.droidcachebox.settings.Settings;
import de.droidcachebox.utils.AbstractFile;
import de.droidcachebox.utils.FileFactory;
import de.droidcachebox.utils.log.Log;

public class DesktopLocatorMethods implements LocatorMethods.PlatformLocatorMethods {
    private static final String sClass = "DesktopLocatorBaseMethods";

    public DesktopLocatorMethods() {

    }

    /*
    // unpack all files to cache
    // extractImages();
    private void extractImages()
    {
        for (BoundingBox bbox : BoundingBoxes)
        {
            int z = bbox.Zoom;
            for (int x = bbox.MinX; x <= bbox.MaxX; x++)
            {
                for (int y = bbox.MinY; y <= bbox.MaxY; y++)
                {
                    Descriptor desc = new Descriptor(x, y, z);
                    byte[] b = LoadFromBoundingBoxByteArray(bbox, desc);
                    String fname = Layer.GetLocalFilename(desc);
                    File ff = new File(fname);
                    if (!ff.getParentFile().exists())
                    {
                        ff.getParentFile().mkdirs();
                    }
                    try
                    {
                        FileOutputStream fos = new FileOutputStream(ff.getAbsoluteFile());
                        fos.write(b);
                        fos.close();
                    }
                    catch (Exception e)
                    {
                        // handle exception
                    }
                }
            }

        }

    }
    */

    /**
     * Gets the subarray of length <tt>length</tt> from <tt>array</tt> that starts at <tt>offset</tt>.
     */
    private byte[] get(byte[] array, int offset, int length) {
        byte[] result = new byte[length];
        System.arraycopy(array, offset, result, 0, length);
        return result;
    }

    @Override
    public byte[] loadFromBoundingBoxByteArray(String filename, BoundingBox bbox, Descriptor desc) {
        try {
            if (bbox.Zoom != desc.getZoom())
                return null;

            int index = (desc.getY() - bbox.MinY) * bbox.Stride + (desc.getX() - bbox.MinX) - 1;
            long offset = bbox.OffsetToIndex + index * 8L;

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
                        InputStream in = new ByteArrayInputStream(buffer);
                        BufferedImage img = ImageIO.read(in);
                        ByteArrayOutputStream bas = new ByteArrayOutputStream();
                        ImageIO.write(img, "jpg", bas);
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
        } catch (Exception exc) {
            /*
             * #if DEBUG Global.AddLog("Pack.LoadFromBoundingBox: Out of memory!" + exc.ToString()); Global.AddMemoryLog(); #endif
             */
        }

        return null;

    }

    @Override
    public byte[] getImageFromFile(String cachedTileFilename) throws IOException {
        AbstractFile myImageAbstractFile = FileFactory.createFile(cachedTileFilename);
        BufferedImage img = ImageIO.read(myImageAbstractFile.getFileInputStream());
        ByteArrayOutputStream bas = new ByteArrayOutputStream();
        ImageIO.write(img, "png", bas);
        return bas.toByteArray();
    }

    @Override
    public LocatorMethods.ImageData getImagePixel(byte[] img) {
        InputStream in = new ByteArrayInputStream(img);
        BufferedImage bImage;
        try {
            bImage = ImageIO.read(in);
        } catch (IOException e) {
            return null;
        }

        LocatorMethods.ImageData imgData = new LocatorMethods.ImageData();
        imgData.width = bImage.getWidth();
        imgData.height = bImage.getHeight();

        BufferedImage intimg = new BufferedImage(bImage.getWidth(), bImage.getHeight(), BufferedImage.TYPE_INT_ARGB);

        ColorConvertOp op = new ColorConvertOp(null);
        op.filter(bImage, intimg);

        Raster ras = intimg.getData();
        DataBufferInt db = (DataBufferInt) ras.getDataBuffer();
        imgData.PixelColorArray = db.getData();

        return imgData;

    }

    @Override
    public byte[] getImageFromData(LocatorMethods.ImageData imgData) {

        BufferedImage dstImage = new BufferedImage(imgData.width, imgData.height, BufferedImage.TYPE_INT_RGB);

        dstImage.getRaster().setDataElements(0, 0, imgData.width, imgData.height, imgData.PixelColorArray);
        ByteArrayOutputStream bas = new ByteArrayOutputStream();
        try {
            ImageIO.write(dstImage, "png", bas);
        } catch (IOException e) {
            return null;
        }
        return bas.toByteArray();
    }

    @Override
    public GraphicFactory getMapsForgeGraphicFactory() {
        return AwtGraphicFactory.INSTANCE;
    }

    @Override
    public Texture getTexture(TileBitmap bitmap) {
        byte[] byteArray;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(baos);
            byteArray = baos.toByteArray(); // takes long
            baos.close();
            if (bitmap instanceof GDXBitmap) ((GDXBitmap) bitmap).recycle();
        } catch (Exception ex) {
            Log.err(sClass, "convert bitmap to byteArray", ex);
            return null;
        }

        try {
            Pixmap pixmap = new Pixmap(byteArray, 0, byteArray.length);
            Texture texture = new Texture(pixmap, Pixmap.Format.RGB565, Settings.useMipMap.getValue());
            pixmap.dispose();
            return texture;
        } catch (Exception ex) {
            Log.err(sClass, "[TileGL] can't create Pixmap or Texture: ", ex);
        }
        return null;
    }
}
