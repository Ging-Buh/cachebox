package de.droidcachebox.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class FileList extends ArrayList<AbstractFile> implements Comparator<AbstractFile> {
    private static final long serialVersionUID = 2454564654L;

    public FileList(String path, String extension) {
        this(path, extension, false);
    }

    public FileList(String path, String extension, boolean useAbsolutePath) {
        AbstractFile dir = FileFactory.createFile(path);
        String[] fileNames = dir.list();
        String absolutePath = useAbsolutePath ? path + "/" : "";
        if (!(fileNames == null)) {
            if (fileNames.length > 0) {
                for (String fileName : fileNames) {
                    if (FileIO.getFileExtension(fileName).equalsIgnoreCase(extension)) {
                        AbstractFile newfile = FileFactory.createFile(absolutePath + fileName);
                        this.add(newfile);
                    }
                }
            }
        }
        Collections.sort(this, this);
    }

    @Override
    public int compare(AbstractFile object1, AbstractFile object2) {
        return Long.compare(object1.lastModified(), object2.lastModified());
    }

}
