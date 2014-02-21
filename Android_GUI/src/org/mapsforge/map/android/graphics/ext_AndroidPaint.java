package org.mapsforge.map.android.graphics;

import CB_UI_Base.GL_UI.utils.HSV_Color;
import CB_UI_Base.graphics.GL_FontFamily;
import CB_UI_Base.graphics.GL_FontStyle;
import CB_UI_Base.graphics.GL_Style;
import CB_UI_Base.graphics.Join;
import CB_UI_Base.graphics.TileMode;
import CB_UI_Base.graphics.extendedIntrefaces.ext_Matrix;
import CB_UI_Base.graphics.extendedIntrefaces.ext_Paint;

public class ext_AndroidPaint extends AndroidPaint implements ext_Paint
{

	public ext_AndroidPaint(ext_Paint paint)
	{
		// TODO Auto-generated constructor stub
	}

	@Override
	public void setAlpha(int i)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void setStrokeJoin(Join join)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void setRadialGradiant(float x, float y, float radius, int[] colors, float[] positions, TileMode tileMode)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void setGradientMatrix(ext_Matrix matrix)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void setLinearGradient(float x1, float y1, float x2, float y2, int[] colors, float[] positions, TileMode tileMode)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public GL_Style getGL_Style()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public float getTextSize()
	{
		return this.paint.getTextSize();
	}

	@Override
	public void setDashPathEffect(float[] strokeDasharray, float offset)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void delDashPathEffect()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public ext_Matrix getGradiantMatrix()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setStyle(GL_Style fill)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public GL_FontStyle getGLFontStyle()
	{

		switch (fontStyle)
		{
		case BOLD:
			return GL_FontStyle.BOLD;
		case BOLD_ITALIC:
			return GL_FontStyle.BOLD_ITALIC;
		case ITALIC:
			return GL_FontStyle.ITALIC;
		case NORMAL:
			return GL_FontStyle.NORMAL;
		default:
			return GL_FontStyle.NORMAL;
		}

	}

	@Override
	public GL_FontFamily getGLFontFamily()
	{
		switch (fontFamily)
		{
		case DEFAULT:
			return GL_FontFamily.DEFAULT;
		case MONOSPACE:
			return GL_FontFamily.MONOSPACE;
		case SANS_SERIF:
			return GL_FontFamily.SANS_SERIF;
		case SERIF:
			return GL_FontFamily.SERIF;
		default:
			return GL_FontFamily.DEFAULT;
		}
	}

	@Override
	public HSV_Color getHSV_Color()
	{

		HSV_Color c = new HSV_Color(this.paint.getColor());

		return c;

	}

	@Override
	public float getStrokeWidth()
	{
		return this.paint.getStrokeWidth();
	}

}
