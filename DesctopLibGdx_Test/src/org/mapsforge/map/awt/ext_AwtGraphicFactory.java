package org.mapsforge.map.awt;

import org.mapsforge.core.graphics.TileBitmap;

import CB_UI_Base.graphics.extendedIntrefaces.ext_Bitmap;
import CB_UI_Base.graphics.extendedIntrefaces.ext_Canvas;
import CB_UI_Base.graphics.extendedIntrefaces.ext_GraphicFactory;
import CB_UI_Base.graphics.extendedIntrefaces.ext_Matrix;
import CB_UI_Base.graphics.extendedIntrefaces.ext_Paint;
import CB_UI_Base.graphics.extendedIntrefaces.ext_Path;

public class ext_AwtGraphicFactory extends AwtGraphicFactory implements ext_GraphicFactory
{
	public static final ext_GraphicFactory INSTANCE = new ext_AwtGraphicFactory();

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
}
