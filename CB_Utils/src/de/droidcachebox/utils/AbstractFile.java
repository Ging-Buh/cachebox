package de.droidcachebox.utils;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;

/**
 * Created by Longri on 17.02.2016.
 */
public abstract class AbstractFile {

    public final static String separator = java.io.File.separator;
    public final static String pathSeparator = java.io.File.pathSeparator;
    public final static char separatorChar = java.io.File.separatorChar;

    public abstract boolean exists();

    public abstract boolean delete() throws IOException;

    public abstract AbstractFile getParentFile();

    /**
     * Creates the directory named by this abstract pathname, including any necessary but nonexistent parent directories.
     * The result is not usable. It is false, if the directory exists or could not be created. Ask exists()
     */
    public abstract boolean mkdirs();

    public abstract boolean isDirectory();

    public abstract boolean isFile();

    public abstract long lastModified();


    /**
     * Returns an array of strings naming the files and directories in the
     * directory denoted by this abstract pathname.
     *
     * <p> If this abstract pathname does not denote a directory, then this
     * method returns {@code null}.  Otherwise an array of strings is
     * returned, one for each file or directory in the directory.  Names
     * denoting the directory itself and the directory's parent directory are
     * not included in the result.  Each string is a file name rather than a
     * complete path.
     *
     * <p> There is no guarantee that the name strings in the resulting array
     * will appear in any specific order; they are not, in particular,
     * guaranteed to appear in alphabetical order.
     *
     * <p> Note that the {@link java.nio.file.Files} class defines the {@link
     * java.nio.file.Files#newDirectoryStream(Path) newDirectoryStream} method to
     * open a directory and iterate over the names of the files in the directory.
     * This may use less resources when working with very large directories, and
     * may be more responsive when working with remote directories.
     *
     * @return  An array of strings naming the files and directories in the
     *          directory denoted by this abstract pathname.  The array will be
     *          empty if the directory is empty.  Returns {@code null} if
     *          this abstract pathname does not denote a directory, or if an
     *          I/O error occurs.
     *
     * @throws  SecurityException
     *          If a security manager exists and its {@link
     *          SecurityManager#checkRead(String)} method denies read access to
     *          the directory
     */
    public abstract String[] list();

    public abstract String[] list(FilenameFilter filenameFilter);

    public abstract long length();

    public abstract boolean createNewFile() throws IOException;

    /**
     * Returns the name of the file or directory denoted by this abstract
     * pathname.  This is just the last name in the pathname's name
     * sequence.  If the pathname's name sequence is empty, then the empty
     * string is returned.
     *
     * @return  The name of the file or directory denoted by this abstract
     *          pathname, or the empty string if this pathname's name sequence
     *          is empty
     */
    public abstract String getName();

    public abstract AbstractFile[] listFiles(FilenameFilter filenameFilter);

    public abstract String getAbsolutePath();

    public abstract boolean mkdir();

    public abstract String getParent();

    public abstract boolean canRead();

    public abstract boolean canWrite();

    public abstract String getPath();

    public abstract AbstractFile[] listFiles();

    public abstract boolean isAbsolute();

    public abstract AbstractFile getCanonicalPath() throws IOException;

    public abstract URL toURL() throws MalformedURLException;

    public abstract boolean renameTo(AbstractFile abstractFile);

    public abstract void setLastModified(long time);

    public abstract AbstractFile getAbsoluteFile();

    public abstract int compareTo(AbstractFile otherAbstractFile);

    public abstract FileOutputStream getFileOutputStream() throws FileNotFoundException;

    public abstract FileInputStream getFileInputStream() throws FileNotFoundException;

    public abstract FileReader getFileReader() throws FileNotFoundException;

    public abstract RandomAccessFile getRandomAccessFile(String mode) throws FileNotFoundException;

    /**
     * Constructs a FileWriter object given a File object.
     *
     * param  file  a File object to write to.
     * @throws IOException  if the file exists but is a directory rather than
     *                  a regular file, does not exist but cannot be created,
     *                  or cannot be opened for any other reason
     */
    public abstract FileWriter getFileWriter() throws IOException;
}
