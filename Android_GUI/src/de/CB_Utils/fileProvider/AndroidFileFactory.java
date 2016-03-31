package de.CB_Utils.fileProvider;

import java.io.FileOutputStream;
import java.io.IOException;

import CB_Utils.Util.FileIO;
import CB_Utils.fileProvider.File;
import CB_Utils.fileProvider.FileFactory;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;

/**
 * Created by Longri on 17.02.2016.
 */
public class AndroidFileFactory extends FileFactory {
	@Override
	protected File createPlatformFile(String path) {
	return new AndroidFile(path);
	}

	@Override
	protected File createPlatformFile(File parent) {
	return new AndroidFile(parent);
	}

	@Override
	protected File createPlatformFile(File parent, String child) {
	return new AndroidFile(parent, child);
	}

	@Override
	protected File createPlatformFile(String parent, String child) {
	return new AndroidFile(parent, child);
	}

	@Override
	protected String createPlatformThumb(String Path, int scaledWidth, String thumbPrefix) {

	String storePath = FileIO.GetDirectoryName(Path) + "/";
	String storeName = FileIO.GetFileNameWithoutExtension(Path);
	String storeExt = FileIO.GetFileExtension(Path).toLowerCase();
	String ThumbPath = storePath + thumbPrefix + THUMB + storeName + "." + storeExt;

	java.io.File ThumbFile = new java.io.File(ThumbPath);

	if (ThumbFile.exists())
		return ThumbPath;

	Bitmap ori = BitmapFactory.decodeFile(Path);

	float scalefactor = (float) scaledWidth / (float) ori.getWidth();

	if (scalefactor >= 1)
		return Path; // don't need a thumb, return original path

	int newHeight = (int) (ori.getHeight() * scalefactor);
	int newWidth = (int) (ori.getWidth() * scalefactor);

	Bitmap resized = ThumbnailUtils.extractThumbnail(ori, newWidth, newHeight);

	FileOutputStream out = null;
	try {
		out = new FileOutputStream(ThumbPath);

		Bitmap.CompressFormat format = Bitmap.CompressFormat.PNG;

		if (storeExt.equals("jpg"))
		format = Bitmap.CompressFormat.JPEG;

		resized.compress(format, 80, out);

		resized.recycle();
		ori.recycle();

		return ThumbPath;
	} catch (Exception e) {
		e.printStackTrace();
	} finally {
		try {
		if (out != null) {
			out.close();
		}
		} catch (IOException e) {
		e.printStackTrace();
		}
	}

	return null;
	}
}
