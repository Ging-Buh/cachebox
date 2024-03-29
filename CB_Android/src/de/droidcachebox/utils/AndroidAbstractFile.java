package de.droidcachebox.utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import de.droidcachebox.Platform;
import de.droidcachebox.utils.log.Log;


/**
 * Created by Longri on 17.02.2016.
 */
public class AndroidAbstractFile extends AbstractFile {
    private static final String sClass = "AndroidFile";
    private static final String content = "content";

    private java.io.File mFile;
    private String contentFile;
    boolean isContentFile;

    private AndroidAbstractFile(java.io.File file) {
        mFile = new java.io.File(file, "");
        isContentFile = false;
    }

    public AndroidAbstractFile(String path) {
        if (path.startsWith(content)) {
            contentFile = path;
            isContentFile = true;
        }
        else {
            mFile = new java.io.File(path);
            isContentFile = false;
        }
    }

    public AndroidAbstractFile(AbstractFile abstractFile) {
        this(abstractFile.getAbsolutePath());
    }

    public AndroidAbstractFile(AbstractFile abstractFile, String child) {
        mFile = new java.io.File(abstractFile.getAbsolutePath(), child);
        isContentFile = false;
    }

    public AndroidAbstractFile(String parent, String child) {
        mFile = new java.io.File(parent, child);
        isContentFile = false;
    }

    @Override
    public boolean exists() {
        if (isContentFile) {
            try {
                Platform.getInputStream(contentFile);
                return true;
            } catch (Exception e) {
                return false;
            }
        }
        else
            return mFile.exists();
    }

    @Override
    public boolean delete() throws IOException {
        boolean ret = mFile.delete();
        if (mFile.exists()) {
            throw new IOException("File not deleted");
        }
        return ret;
    }

    @Override
    public AbstractFile getParentFile() {
        return new AndroidAbstractFile(mFile.getParentFile());
    }

    @Override
    public boolean mkdirs() {
        mFile.mkdirs();
        return mFile.isDirectory();
    }

    @Override
    public boolean isDirectory() {
        return mFile.isDirectory();
    }

    @Override
    public boolean isFile() {
        if (isContentFile)
            return true; // todo
        else
            return mFile.isFile();
    }

    @Override
    public long lastModified() {
        return mFile.lastModified();
    }

    @Override
    public String[] list() {
        return mFile.list();
    }

    @Override
    public String[] list(final de.droidcachebox.utils.FilenameFilter filenameFilter) {
        String[] list = mFile.list((dir, name) -> filenameFilter.accept(new AndroidAbstractFile(dir), name));
        return list;
    }

    @Override
    public long length() {
        return mFile.length();
    }

    @Override
    public boolean createNewFile() throws IOException {
        return mFile.createNewFile();
    }

    @Override
    public String getName() {
        return mFile.getName();
    }

    @Override
    public AbstractFile[] listFiles(final de.droidcachebox.utils.FilenameFilter filenameFilter) {
        String names[] = list();
        if (names == null || filenameFilter == null) {
            return null;
        }
        List<String> v = new ArrayList<>();
        for (int i = 0; i < names.length; i++) {
            if (filenameFilter.accept(this, names[i])) {
                v.add(names[i]);
            }
        }

        if (v.isEmpty())
            return null;
        AbstractFile[] ret = new AbstractFile[v.size()];

        for (int i = 0; i < v.size(); i++)
            ret[i] = new AndroidAbstractFile(this, v.get(i));

        return ret;
    }

    @Override
    public String getAbsolutePath() {
        return mFile.getAbsolutePath();
    }

    @Override
    public boolean mkdir() {
        return mFile.mkdir();
    }

    @Override
    public String getParent() {
        return mFile.getParent();
    }

    @Override
    public boolean canRead() {
        return mFile.canRead();
    }

    @Override
    public boolean canWrite() {
        return mFile.canWrite();
    }

    @Override
    public String getPath() {
        return mFile.getPath();
    }

    @Override
    public AbstractFile[] listFiles() {
        String[] list = mFile.list();

        AbstractFile[] ret = new AbstractFile[list.length];

        int index = 0;
        for (String s : list) {
            ret[index++] = new AndroidAbstractFile(this, s);
        }
        return ret;
    }

    @Override
    public boolean isAbsolute() {
        return mFile.isAbsolute();
    }

    @Override
    public AbstractFile getCanonicalPath() throws IOException {
        return new AndroidAbstractFile(mFile.getCanonicalPath());
    }

    @Override
    public URL toURL() throws MalformedURLException {
        return mFile.toURL();
    }

    @Override
    public boolean renameTo(AbstractFile abstractFile) {
        boolean ret = mFile.renameTo(((AndroidAbstractFile) abstractFile).mFile);
        if (!ret) {
            try {
                // Log.debug("rename has no success. doing copyFile and delete");
                ret = copyToFile(abstractFile);
                if (ret) {
                    mFile.delete();
                }
            } catch (Exception e) {
                Log.err(sClass,e.getLocalizedMessage());
                ret = false;
            }

        }
        return ret;
    }

    private boolean copyToFile(AbstractFile dst) throws IOException {

        AbstractFile prntAbstractFile = dst.getParentFile();
        if (!prntAbstractFile.exists()) {
            prntAbstractFile.mkdirs();
        }

        if (!prntAbstractFile.canWrite()) {
            Log.err(sClass, "can't write to destination" + prntAbstractFile.getAbsolutePath());
            return false;
        }
        FileInputStream inStream = new FileInputStream(mFile);
        FileOutputStream outStream = new FileOutputStream(dst.getAbsolutePath());
        FileChannel inChannel = inStream.getChannel();
        FileChannel outChannel = outStream.getChannel();
        try {
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } finally {
            if (inChannel != null)
                inChannel.close();
            if (outChannel != null)
                outChannel.close();
        }
        inStream.close();
        outStream.close();
        return true;
    }

    @Override
    public void setLastModified(long time) {
        mFile.setLastModified(time);
    }

    @Override
    public AbstractFile getAbsoluteFile() {
        return new AndroidAbstractFile(mFile.getAbsoluteFile());
    }

    @Override
    public int compareTo(AbstractFile otherAbstractFile) {
        return mFile.compareTo(((AndroidAbstractFile) otherAbstractFile).mFile);
    }

    @Override
    public FileOutputStream getFileOutputStream() throws FileNotFoundException {
        if (isContentFile) {
            return (FileOutputStream) Platform.getOutputStream(contentFile);
        }
        else {
            return new FileOutputStream(mFile);
        }
    }

    @Override
    public FileInputStream getFileInputStream() throws FileNotFoundException {
        if (isContentFile) {
            return (FileInputStream) Platform.getInputStream(contentFile);
        }
        else {
            return new FileInputStream(mFile);
        }
    }

    @Override
    public FileReader getFileReader() throws FileNotFoundException {
        return new FileReader(mFile);
    }

    @Override
    public RandomAccessFile getRandomAccessFile(String mode) throws FileNotFoundException {
        return new RandomAccessFile(mFile, mode);
    }

    @Override
    public FileWriter getFileWriter() throws IOException {
        return new FileWriter(mFile);
    }
}
