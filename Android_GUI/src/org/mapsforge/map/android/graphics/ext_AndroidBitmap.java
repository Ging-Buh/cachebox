package org.mapsforge.map.android.graphics;

import org.mapsforge.core.graphics.TileBitmap;

import CB_UI_Base.graphics.Images.BitmapDrawable;
import CB_UI_Base.graphics.extendedIntrefaces.ext_Bitmap;
import android.graphics.Bitmap.Config;

import com.badlogic.gdx.graphics.Texture;

/**
 * Extends the original Mapsforge AwtBitmap with the ext_Bitmap interface.
 * 
 * @author Longri
 */
public class ext_AndroidBitmap extends AndroidBitmap implements ext_Bitmap, TileBitmap
{
	int instCount = 0;

	protected final BitmapDrawable GL_image;

	ext_AndroidBitmap(int width, int height)
	{
		super(width, height, Config.ARGB_8888);
		GL_image = null;
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

	@Override
	public BitmapDrawable getGlBmpHandle()
	{
		return GL_image;
	}

	@Override
	public Texture getTexture()
	{
		if (GL_image == null) return null;
		return GL_image.getTexture();
	}

}
