package CB_Core.Import;

import CB_Utils.Log.Log;
import CB_Utils.fileProvider.File;
import CB_Utils.fileProvider.FileFactory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

/**
 * @author Longri from => http://stackoverflow.com/questions/981578/how-to-unzip-files-recursively-in-java
 */
public class UnZip {
    private static final String log = "UnZip";

    /**
     * Extract the given ZIP-File
     *
     * @param zipFile
     * @return Extracted Folder Path as String
     * @throws ZipException
     * @throws IOException
     */
    static public String extractFolder(String zipFile) throws ZipException, IOException {
        Log.info(log, "unzip from " + zipFile);
        int BUFFER = 2048;
        File file = FileFactory.createFile(zipFile);
        ZipFile zip = new ZipFile(file.getAbsolutePath(), Charset.forName("ISO-8859-1"));
        String newPath = file.getParentFile().getAbsolutePath(); //  zipFile.substring(0, zipFile.length() - 4);

        FileFactory.createFile(newPath).mkdir();
        Enumeration<?> zipFileEntries = zip.entries();

        // Process each entry
        while (zipFileEntries.hasMoreElements()) {
            // grab a zip file entry
            ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();
            String currentEntry = entry.getName();
            File destFile = FileFactory.createFile(newPath, currentEntry);
            if (destFile.exists()) {
                destFile.delete();
            }
            Log.info(log, "  zipEntry: " + destFile.getAbsolutePath());

            File destinationParent = destFile.getParentFile();
            destinationParent.mkdirs();
            destinationParent.setLastModified(entry.getTime()); // set original Datetime to be able to import ordered oldest first

            if (!entry.isDirectory()) {

                int currentByte;
                byte data[] = new byte[BUFFER];

                FileOutputStream fos = destFile.getFileOutputStream();
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
            destFile.setLastModified(entry.getTime()); // set original Datetime to be able to import ordered oldest first
            Log.info(log, "  done with size " + destFile.length());

            if (currentEntry.endsWith(".zip")) {
                // found a zip file, try to open recursiv
                extractFolder(destFile.getAbsolutePath());
            }
        }

        zip.close();

        return newPath;
    }
}
