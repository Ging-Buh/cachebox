package CB_Utils.fileProvider;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Longri on 17.02.2016.
 */
public abstract class File {

	public final static String separator = java.io.File.separator;
	public final static String pathSeparator = java.io.File.pathSeparator;
	public final static char separatorChar = java.io.File.separatorChar;

	public abstract boolean exists();

	public abstract boolean delete() throws IOException;

	public abstract File getParentFile();

	public abstract boolean mkdirs();

	public abstract boolean isDirectory();

	public abstract boolean isFile();

	public abstract long lastModified();

	public abstract String[] list();

	public abstract String[] list(FilenameFilter filenameFilter);

	public abstract long length();

	public abstract boolean createNewFile() throws IOException;

	public abstract String getName();

	public abstract File[] listFiles(FilenameFilter filenameFilter);

	public abstract String getAbsolutePath();

	public abstract boolean mkdir();

	public abstract String getParent();

	public abstract boolean canRead();

	public abstract boolean canWrite();

	public abstract String getPath();

	public abstract File[] listFiles();

	public abstract boolean isAbsolute();

	public abstract File getCanonicalPath() throws IOException;

	public abstract URL toURL() throws MalformedURLException;

	public abstract boolean renameTo(File file);

	public abstract void setLastModified(long time);

	public abstract File getAbsoluteFile();

	public abstract int compareTo(File otherFile);

	public abstract FileOutputStream getFileOutputStream() throws FileNotFoundException;

	public abstract FileInputStream getFileInputStream() throws FileNotFoundException;

	public abstract FileReader getFileReader() throws FileNotFoundException;

	public abstract RandomAccessFile getRandomAccessFile(String mode) throws FileNotFoundException;

	public abstract FileWriter getFileWriter() throws IOException;
}
