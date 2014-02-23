package org.mapsforge.map.awt;

import java.io.IOException;
import java.io.InputStream;

import CB_UI_Base.graphics.Images.BitmapDrawable;
import CB_UI_Base.graphics.extendedIntrefaces.ext_Bitmap;

import com.badlogic.gdx.graphics.Texture;

public class ext_AwtResourceBitmap extends AwtResourceBitmap implements ext_Bitmap
{
	protected final BitmapDrawable GL_image;

	public ext_AwtResourceBitmap(InputStream stream, int HashCode, float scaleFactor) throws IOException
	{
		super(stream);

		if (scaleFactor != 1)
		{
			int w = (int) (this.getWidth() * scaleFactor);
			int h = (int) (this.getHeight() * scaleFactor);
			this.scaleTo(w, h);
		}

		GL_image = new BitmapDrawable(stream, HashCode, scaleFactor);
	}

	@Override
	public void recycle()
	{
		// TODO Auto-generated method stub

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
