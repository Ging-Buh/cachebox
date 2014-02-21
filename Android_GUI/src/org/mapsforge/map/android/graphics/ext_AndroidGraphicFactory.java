package org.mapsforge.map.android.graphics;

import org.mapsforge.core.graphics.TileBitmap;

import CB_UI_Base.graphics.extendedIntrefaces.ext_Bitmap;
import CB_UI_Base.graphics.extendedIntrefaces.ext_Canvas;
import CB_UI_Base.graphics.extendedIntrefaces.ext_GraphicFactory;
import CB_UI_Base.graphics.extendedIntrefaces.ext_Matrix;
import CB_UI_Base.graphics.extendedIntrefaces.ext_Paint;
import CB_UI_Base.graphics.extendedIntrefaces.ext_Path;
import android.app.Application;

public class ext_AndroidGraphicFactory extends AndroidGraphicFactory implements ext_GraphicFactory
{
	protected ext_AndroidGraphicFactory(Application app)
	{
		super(app);
	}

	public static ext_GraphicFactory INSTANCE;

	@Override
	public ext_Matrix createMatrix(ext_Matrix matrix)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public static void createInstance(Application app)
	{
		INSTANCE = new ext_AndroidGraphicFactory(app);
	}

	@Override
	public ext_Paint createPaint(ext_Paint paint)
	{
		return new ext_AndroidPaint(paint);
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
		return new ext_AndroidBitmap(width, height);
	}

	@Override
	public ext_Canvas createCanvas()
	{
		return new ext_AndroidCanvas();
	}

	@Override
	public ext_Path createPath()
	{
		return new ext_AndroidPath();
	}

	public TileBitmap createTileBitmap(int tileSize, boolean isTransparent)
	{
		return new ext_AndroidBitmap(tileSize, tileSize);
	}

}
