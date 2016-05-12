package de.CB_Utils.fileProvider;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import CB_Utils.fileProvider.File;
import CB_Utils.fileProvider.FilenameFilter;

/**
 * Created by Longri on 17.02.2016.
 */
public class AndroidDocumentFile extends File {

    private final java.io.File mFile;

    private AndroidDocumentFile(java.io.File file) {
	mFile = file;
    }

    public AndroidDocumentFile(String path) {
	mFile = new java.io.File(path);
    }

    public AndroidDocumentFile(File parent) {
	mFile = ((AndroidDocumentFile) parent).mFile;
    }

    public AndroidDocumentFile(File parent, String child) {
	mFile = new java.io.File(((AndroidDocumentFile) parent).mFile, child);
    }

    public AndroidDocumentFile(String parent, String child) {
	mFile = new java.io.File(parent, child);
    }

    @Override
    public boolean exists() {
	return mFile.exists();
    }

    @Override
    public boolean delete() throws IOException {
	return mFile.delete();
    }

    @Override
    public File getParentFile() {
	return new AndroidDocumentFile(mFile.getParentFile());
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

	String[] list = mFile.list(new java.io.FilenameFilter() {
	    @Override
	    public boolean accept(java.io.File dir, String name) {
		return filenameFilter.accept(new AndroidDocumentFile(dir), name);
	    }
	});
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
    public File[] listFiles(final FilenameFilter filenameFilter) {
	String names[] = list();
	if (names == null || filenameFilter == null) {
	    return null;
	}
	List<String> v = new ArrayList<String>();
	for (int i = 0; i < names.length; i++) {
	    if (filenameFilter.accept(this, names[i])) {
		v.add(names[i]);
	    }
	}

	if (v.isEmpty())
	    return null;
	File[] ret = new File[v.size()];

	for (int i = 0; i < v.size(); i++)
	    ret[i] = new AndroidDocumentFile(this, v.get(i));

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

	File[] ret = new File[list.length];

	int index = 0;
	for (String s : list) {
	    ret[index++] = new AndroidDocumentFile(this, s);
	}
	return ret;
    }

    @Override
    public boolean isAbsolute() {
	return mFile.isAbsolute();
    }

    @Override
    public File getCanonicalPath() throws IOException {
	return new AndroidDocumentFile(mFile.getCanonicalPath());
    }

    @Override
    public URL toURL() throws MalformedURLException {
	return mFile.toURL();
    }

    @Override
    public boolean renameTo(File file) {
	return mFile.renameTo(((AndroidDocumentFile) file).mFile);
    }

    @Override
    public void setLastModified(long time) {
	mFile.setLastModified(time);
    }

    @Override
    public File getAbsoluteFile() {
	return new AndroidDocumentFile(mFile.getAbsoluteFile());
    }

    @Override
    public int compareTo(File otherFile) {
	return mFile.compareTo(((AndroidDocumentFile) otherFile).mFile);
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
