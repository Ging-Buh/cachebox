package de.droidcachebox.locator;

import com.badlogic.gdx.graphics.Texture;

import org.mapsforge.core.graphics.GraphicFactory;
import org.mapsforge.core.graphics.TileBitmap;

import java.io.IOException;

import de.droidcachebox.locator.map.BoundingBox;
import de.droidcachebox.locator.map.Descriptor;

/**
 * gives static access to the methods that have to be implemented for the used platform
 */
public class LocatorMethods {
    private static PlatformLocatorMethods m;

    public static void init(PlatformLocatorMethods m) {
        LocatorMethods.m = m;
    }

    public static byte[] loadFromBoundingBoxByteArray(String filename, BoundingBox bbox, Descriptor desc) {
        return m.loadFromBoundingBoxByteArray(filename, bbox, desc);
    }

    public static byte[] getImageFromFile(String cachedTileFilename) throws IOException {
        return m.getImageFromFile(cachedTileFilename);
    }

    public static ImageData getImagePixel(byte[] b) {
        return m.getImagePixel(b);
    }

    public static byte[] getImageFromData(ImageData imageDataWithColorMatrixManipulation) {
        return m.getImageFromData(imageDataWithColorMatrixManipulation);
    }

    public static GraphicFactory getMapsForgeGraphicFactory() {
        return m.getMapsForgeGraphicFactory();
    }

    public static Texture getTexture(TileBitmap tileBitmap) {
        return m.getTexture(tileBitmap);
    }

    /**
     these methods need platform specific implementations
     */
    public interface PlatformLocatorMethods {
        byte[] loadFromBoundingBoxByteArray(String filename, BoundingBox bbox, Descriptor desc);

        byte[] getImageFromFile(String cachedTileFilename) throws IOException;

        ImageData getImagePixel(byte[] img);

        byte[] getImageFromData(ImageData imgData);

        GraphicFactory getMapsForgeGraphicFactory();

        Texture getTexture(TileBitmap tileBitmap);
    }

    public static class ImageData {
        public int[] PixelColorArray;
        public int width;
        public int height;
    }
}
