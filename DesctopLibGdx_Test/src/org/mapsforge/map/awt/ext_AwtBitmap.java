package org.mapsforge.map.awt;

import java.io.IOException;
import java.io.InputStream;

import org.mapsforge.core.graphics.TileBitmap;

import CB_UI_Base.graphics.extendedIntrefaces.ext_Bitmap;

/**
 * Extends the original Mapsforge AwtBitmap with the ext_Bitmap interface.
 * 
 * @author Longri
 */
public class ext_AwtBitmap extends AwtBitmap implements ext_Bitmap, TileBitmap
{
	int instCount = 0;

	ext_AwtBitmap(InputStream inputStream) throws IOException
	{
		super(inputStream);
		instCount++;
	}

	ext_AwtBitmap(int width, int height)
	{
		super(width, height);
		instCount++;
	}

	public ext_AwtBitmap(int tileSize)
	{
		this(tileSize, tileSize);
	}

	@Override
	public void recycle()
	{
		instCount++;
		this.bufferedImage = null;
	}

	@Override
	public void getPixels(int[] maskBuf, int i, int w, int j, int y, int w2, int k)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void setPixels(int[] maskedContentBuf, int i, int w, int j, int y, int w2, int k)
	{
		// TODO Auto-generated method stub

	}

}
