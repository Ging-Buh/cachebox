package CB_Locator;

import CB_Locator.Map.BoundingBox;
import CB_Locator.Map.Descriptor;
import CB_UI_Base.graphics.extendedInterfaces.ext_GraphicFactory;
import com.badlogic.gdx.graphics.Texture;
import org.mapsforge.core.graphics.GraphicFactory;
import org.mapsforge.core.graphics.TileBitmap;

import java.io.IOException;

public class LocatorBasePlatFormMethods {
    private static Methods methods;

    public static void setMethods(Methods methods) {
        LocatorBasePlatFormMethods.methods = methods;
    }

    public static byte[] loadFromBoundingBoxByteArray(String filename, BoundingBox bbox, Descriptor desc) {
        return methods.loadFromBoundingBoxByteArray(filename, bbox, desc);
    }

    public static byte[] getImageFromFile(String cachedTileFilename) throws IOException {
        return methods.getImageFromFile(cachedTileFilename);
    }

    public static ImageData getImagePixel(byte[] b) {
        return methods.getImagePixel(b);
    }

    public static byte[] getImageFromData(ImageData imageDataWithColorMatrixManipulation) {
        return methods.getImageFromData(imageDataWithColorMatrixManipulation);
    }

    public static GraphicFactory getMapsForgeGraphicFactory() {
        return methods.getMapsForgeGraphicFactory();
    }

    public static Texture getTexture(TileBitmap tileBitmap) {
        return methods.getTexture(tileBitmap);
    }

    public interface Methods {
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
