package org.mapsforge.map.android.graphics;

import org.mapsforge.core.graphics.TileBitmap;

import CB_UI_Base.graphics.extendedIntrefaces.ext_Bitmap;
import android.graphics.Bitmap.Config;

/**
 * Extends the original Mapsforge AwtBitmap with the ext_Bitmap interface.
 * 
 * @author Longri
 */
public class ext_AndroidBitmap extends AndroidBitmap implements ext_Bitmap, TileBitmap
{
	int instCount = 0;

	// ext_AndroidBitmap(InputStream inputStream) throws IOException
	// {
	// super(inputStream);
	// instCount++;
	// }
	//
	ext_AndroidBitmap(int width, int height)
	{
		super(width, height, Config.ARGB_8888);
		instCount++;
	}

	@Override
	public void recycle()
	{
		instCount++;
		this.destroyBitmap();
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
