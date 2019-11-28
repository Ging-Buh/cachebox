package de.droidcachebox.utils;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Longri on 17.02.2016.
 */
public class DesktopFile extends File {

    private final java.io.File mFile;

    private DesktopFile(java.io.File file) {
        mFile = file;
    }

    DesktopFile(String path) {
        mFile = new java.io.File(path);
    }

    DesktopFile(File parent) {
        mFile = ((DesktopFile) parent).mFile;
    }

    DesktopFile(File parent, String child) {
        mFile = new java.io.File(((DesktopFile) parent).mFile, child);
    }

    DesktopFile(String parent, String child) {
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
    public File getParentFile() {
        return new DesktopFile(mFile.getParentFile());
    }

    @Override
    public boolean mkdirs() {
        return mFile.mkdirs();
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
    public String[] list(final FilenameFilter filenameFilter) {
        return mFile.list((dir, name) -> filenameFilter.accept(new DesktopFile(dir), name));
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
    public File[] listFiles(final FilenameFilter filenameFilter) {
        String[] names = list();
        if (names == null || filenameFilter == null) {
            return null;
        }
        List<String> v = new ArrayList<>();
        for (String name : names) {
            if (filenameFilter.accept(this, name)) {
                v.add(name);
            }
        }

        if (v.isEmpty())
            return null;
        File[] ret = new File[v.size()];

        for (int i = 0; i < v.size(); i++)
            ret[i] = new DesktopFile(this, v.get(i));

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
    public File[] listFiles() {
        String[] list = mFile.list();
        if (list == null) list = new String[]{};
        File[] ret = new File[list.length];
        int index = 0;
        for (String s : list) {
            ret[index++] = new DesktopFile(this, s);
        }
        return ret;
    }

    @Override
    public boolean isAbsolute() {
        return mFile.isAbsolute();
    }

    @Override
    public File getCanonicalPath() throws IOException {
        return new DesktopFile(mFile.getCanonicalPath());
    }

    @Override
    public URL toURL() throws MalformedURLException {
        return mFile.toURI().toURL();
    }

    @Override
    public boolean renameTo(File file) {
        return mFile.renameTo(((DesktopFile) file).mFile);
    }

    @Override
    public void setLastModified(long time) {
        if (!mFile.setLastModified(time))
            throw new IllegalArgumentException("Negative time");
    }

    @Override
    public File getAbsoluteFile() {
        return new DesktopFile(mFile.getAbsoluteFile());
    }

    @Override
    public int compareTo(File otherFile) {
        return mFile.compareTo(((DesktopFile) otherFile).mFile);
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
