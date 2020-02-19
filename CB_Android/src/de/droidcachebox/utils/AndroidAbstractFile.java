package de.droidcachebox.utils;

import de.droidcachebox.utils.log.Log;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by Longri on 17.02.2016.
 */
public class AndroidAbstractFile extends AbstractFile {
    private static final String sKlasse = "AndroidFile";

    private java.io.File mFile;

    private AndroidAbstractFile(java.io.File file) {
        mFile = new java.io.File(file, "");
    }

    public AndroidAbstractFile(String path) {
        mFile = new java.io.File(path);
    }

    public AndroidAbstractFile(AbstractFile abstractFile) {
        mFile = new java.io.File(abstractFile.getAbsolutePath());
    }

    public AndroidAbstractFile(AbstractFile abstractFile, String child) {
        mFile = new java.io.File(abstractFile.getAbsolutePath(), child);
    }

    public AndroidAbstractFile(String parent, String child) {
        mFile = new java.io.File(parent, child);
    }

    @Override
    public boolean exists() {
        return mFile.exists();
    }

    @Override
    public boolean delete() {
        return mFile.delete();
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
                // log.info("rename has no success. doing copyFile and delete");
                ret = copyToFile(abstractFile);
                if (ret) {
                    mFile.delete();
                }
            } catch (Exception e) {
                Log.err(sKlasse,e.getLocalizedMessage());
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
            Log.err(sKlasse, "can't write to destination" + prntAbstractFile.getAbsolutePath());
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
        return new FileOutputStream(mFile);
    }

    @Override
    public FileInputStream getFileInputStream() throws FileNotFoundException {
        return new FileInputStream(mFile);
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
