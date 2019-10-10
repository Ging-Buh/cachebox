package CB_Locator;

import CB_Locator.Map.BoundingBox;
import CB_Locator.Map.Descriptor;

public class LocatorBasePlatFormMethods {
    private static Methods methods;

    public static void setMethods(Methods methods) {
        LocatorBasePlatFormMethods.methods = methods;
    }

    public static byte[] loadFromBoundingBoxByteArray(String filename, BoundingBox bbox, Descriptor desc) {
        return methods.loadFromBoundingBoxByteArray(filename, bbox,desc);
    }

    public interface Methods {
        byte[] loadFromBoundingBoxByteArray(String filename, BoundingBox bbox, Descriptor desc);
    }
}
