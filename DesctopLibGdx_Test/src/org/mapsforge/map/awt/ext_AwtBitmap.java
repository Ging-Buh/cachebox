package org.mapsforge.map.awt;

import java.io.IOException;
import java.io.InputStream;

import org.mapsforge.core.graphics.TileBitmap;

import CB_UI_Base.graphics.Images.BitmapDrawable;
import CB_UI_Base.graphics.extendedIntrefaces.ext_Bitmap;

import com.badlogic.gdx.graphics.Texture;

/**
 * Extends the original Mapsforge AwtBitmap with the ext_Bitmap interface.
 * 
 * @author Longri
 */
public class ext_AwtBitmap extends AwtBitmap implements ext_Bitmap, TileBitmap
{
	int instCount = 0;
	protected final BitmapDrawable GL_image;

	ext_AwtBitmap(InputStream inputStream, int HashCode, float scaleFactor) throws IOException
	{
		super(inputStream);

		if (scaleFactor != 1)
		{
			int w = (int) (this.getWidth() * scaleFactor);
			int h = (int) (this.getHeight() * scaleFactor);
			this.scaleTo(w, h);
		}

		GL_image = new BitmapDrawable(inputStream, HashCode, scaleFactor);
		instCount++;
	}

	ext_AwtBitmap(int width, int height)
	{
		super(width, height);
		GL_image = null;
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
