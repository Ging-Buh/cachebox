package org.mapsforge.map.awt;

import java.io.IOException;
import java.io.InputStream;

import org.mapsforge.core.graphics.ResourceBitmap;
import org.mapsforge.core.graphics.TileBitmap;

import CB_UI_Base.graphics.extendedIntrefaces.ext_Bitmap;
import CB_UI_Base.graphics.extendedIntrefaces.ext_Canvas;
import CB_UI_Base.graphics.extendedIntrefaces.ext_GraphicFactory;
import CB_UI_Base.graphics.extendedIntrefaces.ext_Matrix;
import CB_UI_Base.graphics.extendedIntrefaces.ext_Paint;
import CB_UI_Base.graphics.extendedIntrefaces.ext_Path;

public class ext_AwtGraphicFactory extends AwtGraphicFactory implements ext_GraphicFactory
{

	private final float ScaleFactor;

	public ext_AwtGraphicFactory(float ScaleFactor)
	{
		this.ScaleFactor = ScaleFactor;
	}

	@Override
	public ext_Matrix createMatrix(ext_Matrix matrix)
	{
		return new ext_AwtMatrix(matrix);
	}

	@Override
	public ext_Paint createPaint(ext_Paint paint)
	{
		return new ext_AwtPaint(paint);
	}

	@Override
	public int setColorAlpha(int color, float paintOpacity)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public ext_Bitmap createBitmap(int width, int height)
	{
		return new ext_AwtBitmap(width, height);
	}

	@Override
	public ext_Canvas createCanvas()
	{
		return new ext_AwtCanvas();
	}

	@Override
	public ext_Path createPath()
	{
		return new ext_AwtPath();
	}

	@Override
	public TileBitmap createTileBitmap(int tileSize, boolean hasAlpha)
	{
		return new ext_AwtBitmap(tileSize);
	}

	@Override
	public ResourceBitmap createResourceBitmap(InputStream inputStream, int hash) throws IOException
	{
		return new ext_AwtResourceBitmap(inputStream, hash, this.ScaleFactor);
	}

	public static ext_GraphicFactory getInstance(float ScaleFactor)
	{
		if (FactoryList.containsKey(ScaleFactor)) return FactoryList.get(ScaleFactor);

		ext_AwtGraphicFactory factory = new ext_AwtGraphicFactory(ScaleFactor);
		FactoryList.put(ScaleFactor, factory);
		return factory;
	}
}
