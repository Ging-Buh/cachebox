package de.droidcachebox.ex_import;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import de.droidcachebox.Platform;
import de.droidcachebox.utils.AbstractFile;
import de.droidcachebox.utils.FileFactory;
import de.droidcachebox.utils.log.Log;

/**
 * @author Longri from => http://stackoverflow.com/questions/981578/how-to-unzip-files-recursively-in-java
 */
public class UnZip {
    private static final String sClass = "UnZip";

    static public void extractHere(String zipFileName) throws IOException {
        extract(FileFactory.createFile(zipFileName), true);
    }

    static public void extract(String zipFileName, boolean here) throws IOException {
        extract(FileFactory.createFile(zipFileName), here);
    }

    /**
     * Extract the given ZIP-File
     */
    static public void extract(AbstractFile zipFile, boolean here) throws IOException {
        String zipPathAndName = zipFile.getAbsolutePath();
        Log.debug(sClass, "unzip from " + zipFile.getName());
        int BUFFER = 2048;
        ZipFile zip;
        if (Platform.AndroidVersion >= 24) {
            zip = new ZipFile(zipPathAndName, StandardCharsets.ISO_8859_1);
        } else {
            zip = new ZipFile(zipPathAndName);
        }

        String newPath;
        if (here) {
            // extract the content to the path of the zipFile
            newPath = zipFile.getParent();
        } else {
            // extract the content to a path including the name of the zipFile (is perhaps a new directory)
            newPath = zipPathAndName.substring(0, zipPathAndName.length() - 4);
            FileFactory.createFile(newPath).mkdir();
        }

        Enumeration<?> zipFileEntries = zip.entries();
        // Process each entry
        while (zipFileEntries.hasMoreElements()) {
            // grab a zip file entry
            ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();
            String currentEntry = entry.getName();
            AbstractFile destAbstractFile = FileFactory.createFile(newPath, currentEntry);
            if (destAbstractFile.exists()) {
                if (destAbstractFile.isFile()) {
                    destAbstractFile.delete();
                }
            }
            Log.debug(sClass, "  zipEntry: " + destAbstractFile.getAbsolutePath());

            AbstractFile destinationParent = destAbstractFile.getParentFile();
            destinationParent.mkdirs();
            destinationParent.setLastModified(entry.getTime()); // set original Datetime to be able to import ordered oldest first

            if (!entry.isDirectory()) {

                int currentByte;
                byte[] data = new byte[BUFFER];

                FileOutputStream fos = destAbstractFile.getFileOutputStream();
                BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER);

                BufferedInputStream is = new BufferedInputStream(zip.getInputStream(entry));
                while ((currentByte = is.read(data, 0, BUFFER)) != -1) {
                    dest.write(data, 0, currentByte);
                }
                is.close();

                dest.flush();
                dest.close();
                fos.flush();
                fos.close();

            }
            destAbstractFile.setLastModified(entry.getTime()); // set original Datetime to be able to import ordered oldest first
            Log.debug(sClass, "  done with size " + destAbstractFile.length());

            if (currentEntry.endsWith(".zip")) {
                // found a zip file, try to open recursive
                extractHere(destAbstractFile.getAbsolutePath());
            }
        }

        zip.close();
    }
}
