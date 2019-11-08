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
public class AndroidFile extends de.droidcachebox.utils.File {
    private static final String sKlasse = "AndroidFile";

    private java.io.File mFile;

    private AndroidFile(java.io.File file) {
        mFile = new java.io.File(file, "");
    }

    public AndroidFile(String path) {
        mFile = new java.io.File(path);
    }

    public AndroidFile(de.droidcachebox.utils.File file) {
        mFile = new java.io.File(file.getAbsolutePath());
    }

    public AndroidFile(de.droidcachebox.utils.File file, String child) {
        mFile = new java.io.File(file.getAbsolutePath(), child);
    }

    public AndroidFile(String parent, String child) {
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
    public de.droidcachebox.utils.File getParentFile() {
        return new AndroidFile(mFile.getParentFile());
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
        String[] list = mFile.list((dir, name) -> filenameFilter.accept(new AndroidFile(dir), name));
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
    public de.droidcachebox.utils.File[] listFiles(final de.droidcachebox.utils.FilenameFilter filenameFilter) {
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
        de.droidcachebox.utils.File[] ret = new de.droidcachebox.utils.File[v.size()];

        for (int i = 0; i < v.size(); i++)
            ret[i] = new AndroidFile(this, v.get(i));

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
    public de.droidcachebox.utils.File[] listFiles() {
        String[] list = mFile.list();

        de.droidcachebox.utils.File[] ret = new de.droidcachebox.utils.File[list.length];

        int index = 0;
        for (String s : list) {
            ret[index++] = new AndroidFile(this, s);
        }
        return ret;
    }

    @Override
    public boolean isAbsolute() {
        return mFile.isAbsolute();
    }

    @Override
    public de.droidcachebox.utils.File getCanonicalPath() throws IOException {
        return new AndroidFile(mFile.getCanonicalPath());
    }

    @Override
    public URL toURL() throws MalformedURLException {
        return mFile.toURL();
    }

    @Override
    public boolean renameTo(de.droidcachebox.utils.File file) {
        boolean ret = mFile.renameTo(((AndroidFile) file).mFile);
        if (!ret) {
            try {
                // log.info("rename has no success. doing copyFile and delete");
                ret = copyToFile(file);
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

    private boolean copyToFile(de.droidcachebox.utils.File dst) throws IOException {

        de.droidcachebox.utils.File prntFile = dst.getParentFile();
        if (!prntFile.exists()) {
            prntFile.mkdirs();
        }

        if (!prntFile.canWrite()) {
            Log.err(sKlasse, "can't write to destination" + prntFile.getAbsolutePath());
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
    public de.droidcachebox.utils.File getAbsoluteFile() {
        return new AndroidFile(mFile.getAbsoluteFile());
    }

    @Override
    public int compareTo(de.droidcachebox.utils.File otherFile) {
        return mFile.compareTo(((AndroidFile) otherFile).mFile);
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
