package org.mapsforge.map.android.graphics;

import java.io.IOException;
import java.io.InputStream;

import CB_UI_Base.graphics.Images.BitmapDrawable;
import CB_UI_Base.graphics.extendedIntrefaces.ext_Bitmap;

import com.badlogic.gdx.graphics.Texture;

public class ext_AndroidResourceBitmap extends AndroidResourceBitmap implements ext_Bitmap
{

	protected final BitmapDrawable GL_image;

	ext_AndroidResourceBitmap(InputStream inputStream, int HashCode, float scaleFactor) throws IOException
	{
		super(inputStream, HashCode);
		GL_image = new BitmapDrawable(inputStream, HashCode, scaleFactor);

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
