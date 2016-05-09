package org.mapsforge.map.android.graphics;

import java.io.IOException;
import java.io.InputStream;

import android.graphics.Bitmap;

public class ext_AndroidSvgBitmap extends ext_AndroidResourceBitmap {

	private static Bitmap getResourceBitmap(InputStream inputStream, int hash, float scaleFactor, int width, int height, int percent) {
	// TODO Auto-generated method stub
	return null;
	}

	ext_AndroidSvgBitmap(InputStream inputStream, int hash, float scaleFactor, int width, int height, int percent) throws IOException {
	super(getResourceBitmap(inputStream, hash, scaleFactor, width, height, percent));
	}

}
