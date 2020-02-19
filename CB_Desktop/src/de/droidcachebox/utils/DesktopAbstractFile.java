package de.droidcachebox.utils;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Longri on 17.02.2016.
 */
public class DesktopAbstractFile extends AbstractFile {

    private final java.io.File mFile;

    private DesktopAbstractFile(java.io.File file) {
        mFile = file;
    }

    DesktopAbstractFile(String path) {
        mFile = new java.io.File(path);
    }

    DesktopAbstractFile(AbstractFile parent) {
        mFile = ((DesktopAbstractFile) parent).mFile;
    }

    DesktopAbstractFile(AbstractFile parent, String child) {
        mFile = new java.io.File(((DesktopAbstractFile) parent).mFile, child);
    }

    DesktopAbstractFile(String parent, String child) {
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
        return new DesktopAbstractFile(mFile.getParentFile());
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
        return mFile.list((dir, name) -> filenameFilter.accept(new DesktopAbstractFile(dir), name));
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
    public AbstractFile[] listFiles(final FilenameFilter filenameFilter) {
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
        AbstractFile[] ret = new AbstractFile[v.size()];

        for (int i = 0; i < v.size(); i++)
            ret[i] = new DesktopAbstractFile(this, v.get(i));

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
        if (list == null) list = new String[]{};
        AbstractFile[] ret = new AbstractFile[list.length];
        int index = 0;
        for (String s : list) {
            ret[index++] = new DesktopAbstractFile(this, s);
        }
        return ret;
    }

    @Override
    public boolean isAbsolute() {
        return mFile.isAbsolute();
    }

    @Override
    public AbstractFile getCanonicalPath() throws IOException {
        return new DesktopAbstractFile(mFile.getCanonicalPath());
    }

    @Override
    public URL toURL() throws MalformedURLException {
        return mFile.toURI().toURL();
    }

    @Override
    public boolean renameTo(AbstractFile abstractFile) {
        return mFile.renameTo(((DesktopAbstractFile) abstractFile).mFile);
    }

    @Override
    public void setLastModified(long time) {
        if (!mFile.setLastModified(time))
            throw new IllegalArgumentException("Negative time");
    }

    @Override
    public AbstractFile getAbsoluteFile() {
        return new DesktopAbstractFile(mFile.getAbsoluteFile());
    }

    @Override
    public int compareTo(AbstractFile otherAbstractFile) {
        return mFile.compareTo(((DesktopAbstractFile) otherAbstractFile).mFile);
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
