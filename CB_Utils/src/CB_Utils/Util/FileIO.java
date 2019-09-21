package CB_Utils.Util;

import CB_Utils.fileProvider.File;
import CB_Utils.fileProvider.FileFactory;
import CB_Utils.fileProvider.FilenameFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Pattern;

public class FileIO {
    /**
     * überprüft ob ein File existiert!
     *
     * @param filename
     * @return true, wenn das File existiert, ansonsten false.
     */
    public static boolean fileExists(String filename) {
        File file = FileFactory.createFile(filename);
        return file.exists();
    }

    /**
     * überprüft ob ein File existiert! Und nicht leer ist (0 Bytes)
     *
     * @param filename
     * @return true, wenn das File existiert, ansonsten false.
     */
    public static boolean fileExistsNotEmpty(String filename) {
        File file = FileFactory.createFile(filename);
        if (!file.exists())
            return false;
        if (file.length() <= 0)
            return false;

        return true;
    }

    /**
     * überprüft ob ein File existiert! <br>
     * Und nicht älter als die angegebene Zeit in Minuten ist!
     *
     * @param filename
     * @param maxAge
     * @return true, wenn das File existiert und das Alter nicht größer als {maxAge} ist, ansonsten false.
     */
    public static boolean fileExistsMaxAge(String filename, int maxAge) {
        File file = FileFactory.createFile(filename);
        if (!file.exists())
            return false;

        int age = (int) ((new Date().getTime() - file.lastModified()) / 100000);

        if (age > maxAge)
            return false;
        return true;
    }

    /**
     * Returns TRUE has the given PATH write permission!
     *
     * @param path
     * @return
     */
    public static boolean canWrite(String path) {
        boolean result = true;
        try {
            String testFolderName = path + "/Test/";

            File testFolder = FileFactory.createFile(testFolderName);
            if (testFolder.mkdirs()) {
                File test = FileFactory.createFile(testFolderName + "Test.txt");
                test.createNewFile();
                if (!test.exists()) {
                    result = false;
                } else {
                    test.delete();
                    testFolder.delete();
                }
            } else {
                result = false;
            }
        } catch (IOException e) {
            result = false;
            e.printStackTrace();
        }
        return result;
    }

    /**
     * überprüft ob ein Ordner existiert und legt ihn an, wenn er nicht existiert.
     *
     * @param folder Pfad des Ordners
     * @return true, wenn er existiert oder angelegt wurde. false, wenn das Anlegen nicht funktioniert hat.
     */
    public static boolean createDirectory(String folder) {

        // remove extension
        int extPos = folder.lastIndexOf("/");
        String ext = "";
        if (extPos > -1)
            ext = folder.substring(extPos);

        if (ext.length() > 0 && ext.contains(".")) {
            folder = folder.replace(ext, "");
        }

        if (!canWrite(folder)) {
            return false;
        }

        File f = FileFactory.createFile(folder);

        if (f.isDirectory())
            return true;
        else {
            // have the object build the directory structure, if needed.
            return f.mkdirs();
        }
    }

    public static File createFile(String pathAndName) {
        // works, if FileFactory is initialized
        try {
            File file = FileFactory.createFile(pathAndName);
            File path = file.getParentFile();
            path.mkdirs();
            if (path.exists()) {
                file.createNewFile();
            }
            if (file.exists())
                return file;
            else
                return null;
        } catch (IOException ignored) {
            return null;
        }
    }

    public static String getFileExtension(String filename) {
        int dotposition = filename.lastIndexOf(".");
        String ext = "";
        if (dotposition > -1) {
            ext = filename.substring(dotposition + 1, filename.length());
        }

        return ext;
    }

    public static String getFileNameWithoutExtension(String filename) {
        int dotposition = filename.lastIndexOf(".");
        if (dotposition >= 0)
            filename = filename.substring(0, dotposition);
        int slashposition = Math.max(filename.lastIndexOf("/"), filename.lastIndexOf("\\"));
        if (slashposition >= 0)
            filename = filename.substring(slashposition + 1, filename.length());
        return filename;

    }

    public static String getFileName(String filename) {
        int slashposition = Math.max(filename.lastIndexOf("/"), filename.lastIndexOf("\\"));
        if (slashposition >= 0)
            filename = filename.substring(slashposition + 1, filename.length());
        return filename;

    }

    public static String getDirectoryName(String filename) {
        int slashposition = Math.max(filename.lastIndexOf("/"), filename.lastIndexOf("\\"));
        if (slashposition >= 0)
            filename = filename.substring(0, slashposition);
        return filename;
    }

    public static String getRelativePath(String targetPath, String basePath, String pathSeparator) { // We need the -1 argument to split to make sure we get a trailing
        // "" token if the base ends in the path separator and is therefore
        // a directory. We require directory paths to end in the path
        // separator -- otherwise they are indistinguishable from files.
        String[] base = basePath.split(Pattern.quote(pathSeparator), -1);
        String[] target = targetPath.split(Pattern.quote(pathSeparator), 0);
        // First get all the common elements. Store them as a string,
        // and also count how many of them there are.
        String common = "";
        int commonIndex = 0;
        for (int i = 0; i < target.length && i < base.length; i++) {
            if (target[i].equals(base[i])) {
                common += target[i] + pathSeparator;
                commonIndex++;
            } else
                break;
        }

        if (commonIndex == 0) { // Whoops -- not even a single common path element. This most
            // likely indicates differing drive letters, like C: and D:.
            // These paths cannot be relativized. Return the target path.
            return targetPath;
            // This should never happen when all absolute paths
            // begin with / as in *nix.
        }
        String relative = "";
        if (base.length == commonIndex) {
            // Comment this out if you prefer that a relative path not start with ./
            // relative = "." + pathSeparator;
        } else {
            int numDirsUp = base.length - commonIndex;
            // The number of directories we have to backtrack is the length of
            // the base path MINUS the number of common path elements, minus
            // one because the last element in the path isn't a directory.
            for (int i = 1; i <= (numDirsUp); i++) {
                relative += ".." + pathSeparator;
            }
        }
        relative += targetPath.substring(common.length());
        return relative;
    }

    public static String removeInvalidFatChars(String str) {
        String[] invalidChars = new String[]{":", "\\", "/", "<", ">", "?", "*", "|", "\"", ";", "#"};

        for (int i = 0; i < invalidChars.length; i++)
            str = str.replace(invalidChars[i], "");

        return str;
    }

    /**
     * Gibt eine ArrayList<File> zurück, die alle Files mit der Endung gpx enthält.
     *
     * @param directory
     * @param files
     * @return
     */
    public static ArrayList<File> recursiveDirectoryReader(File directory, ArrayList<File> files) {
        return recursiveDirectoryReader(directory, files, "gpx", false);
    }

    /**
     * Gibt eine ArrayList<File> zurück, die alle Files mit der angegebenen Endung haben.
     *
     * @param directory
     * @param files
     * @return
     */
    public static ArrayList<File> recursiveDirectoryReader(File directory, ArrayList<File> files, final String Endung, boolean exludeHides) {

        File[] filelist = directory.listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String filename) {
                return filename.contains("." + Endung);
            }
        });

        if (filelist != null) {
            for (File localFile : filelist)
                files.add(localFile);
        }

        File[] directories = directory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return dir.isDirectory();
            }
        });
        if (directories != null) {
            for (File recursiveDir : directories) {
                if (recursiveDir.isDirectory()) {
                    if (!(exludeHides && recursiveDir.getName().startsWith("."))) {
                        recursiveDirectoryReader(recursiveDir, files, Endung, exludeHides);
                    }
                }
            }
        }
        return files;
    }

    public static void deleteDirectory(File directory) {
        if (directory.isDirectory()) {

            // directory is empty, then delete it
            if (directory.list().length == 0) {

                try {
                    directory.delete();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("Directory is deleted : " + directory.getAbsolutePath());

            } else {

                // list all the directory contents
                String files[] = directory.list();

                for (String temp : files) {
                    // construct the file structure
                    File fileDelete = FileFactory.createFile(directory, temp);

                    // recursive delete
                    deleteDirectory(fileDelete);
                }

                // check the directory again, if empty then delete it
                if (directory.list().length == 0) {
                    try {
                        directory.delete();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    System.out.println("Directory is deleted : " + directory.getAbsolutePath());
                }
            }

        } else {
            // if file, then delete it
            try {
                directory.delete();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("File is deleted : " + directory.getAbsolutePath());
        }
    }

    public static boolean directoryExists(String pathName) {
        return FileFactory.createFile(pathName).exists();
    }
}
