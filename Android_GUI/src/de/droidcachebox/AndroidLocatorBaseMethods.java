package de.droidcachebox;

import CB_Locator.LocatorBasePlatFormMethods;
import CB_Locator.Map.BoundingBox;
import CB_Locator.Map.Descriptor;
import CB_Utils.Log.Log;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLUtils;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import org.mapsforge.core.graphics.GraphicFactory;
import org.mapsforge.core.graphics.TileBitmap;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.util.Arrays;

import static org.mapsforge.map.android.graphics.AndroidGraphicFactory.getBitmap;

public class AndroidLocatorBaseMethods implements LocatorBasePlatFormMethods.Methods {
    private static final String sKlasse = "AndroidLocatorBaseMethods";
    private AndroidApplication androidApplication;
    private Activity mainActivity;
    private Main mainMain;

    public AndroidLocatorBaseMethods(Main main) {
        androidApplication = main;
        mainActivity = main;
        mainMain = main;
    }

    /**
     * Gets the subarray of length <tt>length</tt> from <tt>array</tt> that starts at <tt>offset</tt>.
     */
    private static byte[] get(byte[] array, int offset, int length) {
        byte[] result = new byte[length];
        System.arraycopy(array, offset, result, 0, length);
        return result;
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

    @Override
    public byte[] getImageFromFile(String cachedTileFilename) {
        android.graphics.Bitmap result = BitmapFactory.decodeFile(cachedTileFilename);
        if (result != null) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            result.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, stream);
            byte[] b = stream.toByteArray();
            return b;
        }
        return null;
    }


    @Override
    public LocatorBasePlatFormMethods.ImageData getImagePixel(byte[] img) {
        android.graphics.Bitmap bitmap = BitmapFactory.decodeByteArray(img, 0, img.length);
        // Buffer dst = null;
        int[] pixels = new int[bitmap.getWidth() * bitmap.getHeight()];
        // bitmap.getPixels(pixels, 0, 0, 0, 0, bitmap.getWidth(), bitmap.getHeight());

        bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        LocatorBasePlatFormMethods.ImageData imgData = new LocatorBasePlatFormMethods.ImageData();
        imgData.width = bitmap.getWidth();
        imgData.height = bitmap.getHeight();
        imgData.PixelColorArray = pixels;

        return imgData;
    }

    @Override
    public byte[] getImageFromData(LocatorBasePlatFormMethods.ImageData imgData) {
        android.graphics.Bitmap bmp = android.graphics.Bitmap.createBitmap(imgData.PixelColorArray, imgData.width, imgData.height, android.graphics.Bitmap.Config.RGB_565);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    @Override
    public GraphicFactory getMapsForgeGraphicFactory() {
        if (AndroidGraphicFactory.INSTANCE == null) {
            AndroidGraphicFactory.createInstance(mainActivity.getApplication());
        }
        return AndroidGraphicFactory.INSTANCE;
    }

    @Override
    public Texture getTexture(TileBitmap bitmap) {
            /*
              // direct Buffer swap
              If the goal is to convert an Android Bitmap to a libgdx Texture, you don't need to use Pixmap at all. You can do it directly with
              the help of simple OpenGL and Android GLUtils. Try the followings; it is 100x faster than your solution. I assume that you are
              not in the rendering thread (you should not most likely). If you are, you don't need to call postRunnable().
              Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    Texture tex = new Texture(bitmap.getWidth(), bitmap.getHeight(), Format.RGBA8888);
                    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, tex.getTextureObjectHandle());
                    GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
                    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
                    bitmap.recycle(); // now you have the texture to do whatever you want
                }
              });
            */
        Texture tex = new Texture(bitmap.getWidth(), bitmap.getHeight(), Pixmap.Format.RGBA8888);
        Gdx.gl20.glBindTexture(Gdx.gl20.GL_TEXTURE_2D, tex.getTextureObjectHandle());
        GLUtils.texImage2D(Gdx.gl20.GL_TEXTURE_2D, 0, getBitmap(bitmap), 0);
        Gdx.gl20.glBindTexture(Gdx.gl20.GL_TEXTURE_2D, 0);
        // bitmap.recycle(); // now you have the texture to do whatever you want
        return tex;
    }
}
