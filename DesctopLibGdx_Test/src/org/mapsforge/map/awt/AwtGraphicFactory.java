/*
 * Copyright 2010, 2011, 2012, 2013 mapsforge.org
 * Copyright © 2014 Ludwig M Brinckmann
 * Copyright © 2014 Christian Pesch
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.mapsforge.map.awt;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.mapsforge.core.graphics.Bitmap;
import org.mapsforge.core.graphics.Color;
import org.mapsforge.core.graphics.GraphicContext;
import org.mapsforge.core.graphics.Matrix;
import org.mapsforge.core.graphics.Paint;
import org.mapsforge.core.graphics.Path;
import org.mapsforge.core.graphics.ResourceBitmap;
import org.mapsforge.core.graphics.TileBitmap;

import CB_UI_Base.graphics.extendedIntrefaces.ext_Bitmap;
import CB_UI_Base.graphics.extendedIntrefaces.ext_Canvas;
import CB_UI_Base.graphics.extendedIntrefaces.ext_GraphicFactory;
import CB_UI_Base.graphics.extendedIntrefaces.ext_Matrix;
import CB_UI_Base.graphics.extendedIntrefaces.ext_Paint;
import CB_UI_Base.graphics.extendedIntrefaces.ext_Path;

public final class AwtGraphicFactory implements ext_GraphicFactory
{
	public static final ext_GraphicFactory INSTANCE = new AwtGraphicFactory();
	private static final java.awt.Color TRANSPARENT = new java.awt.Color(0, 0, 0, 0);

	public static GraphicContext createGraphicContext(Graphics graphics)
	{
		return new AwtCanvas((Graphics2D) graphics);
	}

	static AffineTransform getAffineTransform(Matrix matrix)
	{
		return ((AwtMatrix) matrix).affineTransform;
	}

	static AwtPaint getAwtPaint(Paint paint)
	{
		return (AwtPaint) paint;
	}

	static AwtPath getAwtPath(Path path)
	{
		return (AwtPath) path;
	}

	static BufferedImage getBufferedImage(Bitmap bitmap)
	{
		return ((AwtBitmap) bitmap).bufferedImage;
	}

	static java.awt.Color getColor(Color color)
	{
		switch (color)
		{
		case BLACK:
			return java.awt.Color.BLACK;
		case BLUE:
			return java.awt.Color.BLUE;
		case GREEN:
			return java.awt.Color.GREEN;
		case RED:
			return java.awt.Color.RED;
		case TRANSPARENT:
			return TRANSPARENT;
		case WHITE:
			return java.awt.Color.WHITE;
		}

		throw new IllegalArgumentException("unknown color: " + color);
	}

	private AwtGraphicFactory()
	{
	}

	@Override
	public ext_Bitmap createBitmap(int width, int height)
	{
		return new AwtBitmap(width, height);
	}

	@Override
	public Bitmap createBitmap(int width, int height, boolean isTransparent)
	{
		if (isTransparent)
		{
			throw new UnsupportedOperationException("No transparencies in AWT implementation");
		}
		return new AwtBitmap(width, height);
	}

	@Override
	public ext_Canvas createCanvas()
	{
		return new AwtCanvas();
	}

	@Override
	public int createColor(Color color)
	{
		return getColor(color).getRGB();
	}

	@Override
	public int createColor(int alpha, int red, int green, int blue)
	{
		return new java.awt.Color(red, green, blue, alpha).getRGB();
	}

	@Override
	public Matrix createMatrix()
	{
		return new AwtMatrix();
	}

	@Override
	public Paint createPaint()
	{
		return new AwtPaint();
	}

	@Override
	public ext_Path createPath()
	{
		return new AwtPath();
	}

	@Override
	public ResourceBitmap createResourceBitmap(InputStream inputStream, int hash) throws IOException
	{
		return new AwtResourceBitmap(inputStream);
	}

	@Override
	public TileBitmap createTileBitmap(InputStream inputStream, int tileSize, boolean hasAlpha) throws IOException
	{
		return new AwtTileBitmap(inputStream);
	}

	@Override
	public TileBitmap createTileBitmap(int tileSize, boolean hasAlpha)
	{
		return new AwtTileBitmap(tileSize);
	}

	@Override
	public InputStream platformSpecificSources(String relativePathPrefix, String src) throws FileNotFoundException
	{
		return null;
	}

	@Override
	public ResourceBitmap renderSvg(InputStream inputStream, float scaleFactor, int hash)
	{
		AwtResourceBitmap b = new AwtResourceBitmap(2, 2);
		return b;
	}

	@Override
	public ext_Matrix createMatrix(ext_Matrix matrix)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ext_Paint createPaint(ext_Paint paint)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int setColorAlpha(int color, float paintOpacity)
	{
		// TODO Auto-generated method stub
		return 0;
	}

}
