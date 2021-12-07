package de.droidcachebox.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Longri on 17.02.2016.
 */
public class AndroidFileFactory extends FileFactory {

    @Override
    protected AbstractFile createPlatformFile(String path) {
        return new AndroidAbstractFile(path);
    }

    @Override
    protected AbstractFile createPlatformFile(AbstractFile parent) {
        return new AndroidAbstractFile(parent);
    }

    @Override
    protected AbstractFile createPlatformFile(AbstractFile parent, String child) {
        return new AndroidAbstractFile(parent, child);
    }

    @Override
    protected AbstractFile createPlatformFile(String parent, String child) {
        return new AndroidAbstractFile(parent, child);
    }

    @Override
    protected String createPlatformThumb(String Path, int scaledWidth, String thumbPrefix) {

        String storePath = FileIO.getDirectoryName(Path) + "/";
        String storeName = FileIO.getFileNameWithoutExtension(Path);
        String storeExt = FileIO.getFileExtension(Path).toLowerCase();
        String ThumbPath = storePath + thumbPrefix + THUMB + storeName + "." + storeExt;

        java.io.File ThumbFile = new java.io.File(ThumbPath);

        if (ThumbFile.exists())
            return ThumbPath;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        if (BitmapFactory.decodeFile(Path, options) == null) {
            // seems as if decodeFile always returns null (independant from success)
            // todo delete a bad original file (Path)
            // return null;
            // will now perhaps produce bad thumbs
        }

        int oriWidth = options.outWidth;
        int oriHeight = options.outHeight;
        float scalefactor = (float) scaledWidth / (float) oriWidth;

        if (scalefactor >= 1)
            return Path; // don't need a thumb, return original path

        int newHeight = (int) (oriHeight * scalefactor);
        int newWidth = (int) (oriWidth * scalefactor);

        //Find the correct scale value. It should be the power of 2.
        int scale = 1;
        while (oriWidth / scale / 2 >= newWidth && oriHeight / scale / 2 >= newHeight)
            scale *= 2;

        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        Bitmap resized = null;
        try {
            resized = BitmapFactory.decodeStream(new FileInputStream(Path), null, o2);
        } catch (FileNotFoundException e1) {

            e1.printStackTrace();
        }

        FileOutputStream out = null;
        try {
            out = new FileOutputStream(ThumbPath);
            Bitmap.CompressFormat format = Bitmap.CompressFormat.PNG;

            if (storeExt.equals("jpg"))
                format = Bitmap.CompressFormat.JPEG;

            if (resized == null) {
                return null;
            }
            resized.compress(format, 80, out);

            resized.recycle();

            return ThumbPath;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }
}
