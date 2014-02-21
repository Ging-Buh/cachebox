package org.mapsforge.map.awt;

import CB_UI_Base.GL_UI.utils.HSV_Color;
import CB_UI_Base.graphics.GL_FontFamily;
import CB_UI_Base.graphics.GL_FontStyle;
import CB_UI_Base.graphics.GL_Style;
import CB_UI_Base.graphics.Join;
import CB_UI_Base.graphics.TileMode;
import CB_UI_Base.graphics.extendedIntrefaces.ext_Matrix;
import CB_UI_Base.graphics.extendedIntrefaces.ext_Paint;

public class ext_AwtPaint extends AwtPaint implements ext_Paint
{

	public ext_AwtPaint(ext_Paint paint)
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
		return this.textSize;
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
		case 0:
			return GL_FontStyle.NORMAL;
		case 1:
			return GL_FontStyle.BOLD;
		case 2:
			return GL_FontStyle.ITALIC;
		case 3:
			return GL_FontStyle.BOLD_ITALIC;
		}

		return GL_FontStyle.NORMAL;
	}

	@Override
	public GL_FontFamily getGLFontFamily()
	{
		if (fontName == null) return GL_FontFamily.DEFAULT;

		if (fontName.equals("DEFAULT")) return GL_FontFamily.DEFAULT;
		if (fontName.equals("MONOSPACE")) return GL_FontFamily.MONOSPACE;
		if (fontName.equals("SANS_SERIF")) return GL_FontFamily.SANS_SERIF;
		if (fontName.equals("SERIF")) return GL_FontFamily.SERIF;
		return GL_FontFamily.DEFAULT;
	}

	@Override
	public HSV_Color getHSV_Color()
	{
		int r = color.getRed();
		int g = color.getGreen();
		int b = color.getBlue();
		int a = color.getAlpha();

		HSV_Color c = new HSV_Color(a, r, g, b);

		return c;

	}

	@Override
	public float getStrokeWidth()
	{
		return strokeWidth;
	}

}
