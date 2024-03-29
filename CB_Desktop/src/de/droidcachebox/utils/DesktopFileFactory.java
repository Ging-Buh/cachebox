package de.droidcachebox.utils;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import de.droidcachebox.utils.log.Log;

/**
 * Created by Longri on 17.02.2016.
 */
public class DesktopFileFactory extends FileFactory {

    @Override
    protected AbstractFile createPlatformFile(String path) {
        return new DesktopAbstractFile(path);
    }

    @Override
    protected AbstractFile createPlatformFile(AbstractFile parent) {
        return new DesktopAbstractFile(parent);
    }

    @Override
    protected AbstractFile createPlatformFile(AbstractFile parent, String child) {
        return new DesktopAbstractFile(parent, child);
    }

    @Override
    protected AbstractFile createPlatformFile(String parent, String child) {
        return new DesktopAbstractFile(parent, child);
    }

    @Override
    protected String createPlatformThumb(String Path, int scaledWidth, String thumbPrefix) {
        try {

            String storePath = FileIO.getDirectoryName(Path) + "/";
            String storeName = FileIO.getFileNameWithoutExtension(Path);
            String storeExt = FileIO.getFileExtension(Path).toLowerCase();
            String ThumbPath = storePath + thumbPrefix + THUMB + storeName + "." + storeExt;

            java.io.File ThumbFile = new java.io.File(ThumbPath);

            if (ThumbFile.exists())
                return ThumbPath;

            java.io.File orgFile = new java.io.File(Path);
            if (orgFile.exists()) {
                BufferedImage ori = ImageIO.read(orgFile);
                if (ori == null) {
                    orgFile.delete();
                    return null;
                }
                float scalefactor = (float) scaledWidth / (float) ori.getWidth();

                if (scalefactor >= 1)
                    return Path; // don't need a thumb, return original path

                int newHeight = (int) (ori.getHeight() * scalefactor);
                int newWidth = (int) (ori.getWidth() * scalefactor);

                Image scaled = ori.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
                BufferedImage img = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
                img.createGraphics().drawImage(scaled, 0, 0, null);
                ImageIO.write(img, storeExt, ThumbFile);

                img.flush();
                ori.flush();
                scaled.flush();

                img = null;
                ori = null;
                scaled = null;

                return ThumbPath;
            }
        } catch (IOException e) {
            Log.err("DesktopFileFactory:createPlatformThumb", " for " + Path);
            e.printStackTrace();
            return null;
        }
        return null;
    }

}
