package de.CB_Utils.fileProvider;

import CB_Utils.fileProvider.File;
import CB_Utils.fileProvider.FileFactory;

/**
 * Created by Longri on 17.02.2016.
 */
public class DesktopFileFactory extends FileFactory {
    @Override
    protected File createPlatformFile(String path) {
        return new DesktopFile(path);
    }

    @Override
    protected File createPlatformFile(File parent) {
        return new DesktopFile(parent);
    }

    @Override
    protected File createPlatformFile(File parent, String child) {
        return new DesktopFile(parent, child);
    }

    @Override
    protected File createPlatformFile(String parent, String child) {
        return new DesktopFile(parent, child);
    }
}
