/*
 * Copyright 2010, 2011, 2012 mapsforge.org
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

import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import org.mapsforge.core.graphics.Bitmap;
import org.mapsforge.core.graphics.Canvas;
import org.mapsforge.core.graphics.Color;
import org.mapsforge.core.graphics.GraphicFactory;
import org.mapsforge.core.graphics.Matrix;
import org.mapsforge.core.graphics.Paint;
import org.mapsforge.core.graphics.Path;

public final class AwtGraphicFactory implements GraphicFactory {
	public static final GraphicFactory INSTANCE = new AwtGraphicFactory();
	private static final java.awt.Color TRANSPARENT = new java.awt.Color(0, 0, 0, 0);

	public static Canvas createCanvas(java.awt.Graphics2D graphics2D) {
		return new AwtCanvas(graphics2D);
	}

	static AffineTransform getAffineTransform(Matrix matrix) {
		return ((AwtMatrix) matrix).affineTransform;
	}

	static AwtPaint getAwtPaint(Paint paint) {
		return (AwtPaint) paint;
	}

	static AwtPath getAwtPath(Path path) {
		return (AwtPath) path;
	}

	static BufferedImage getBufferedImage(Bitmap bitmap) {
		return ((AwtBitmap) bitmap).bufferedImage;
	}

	static java.awt.Color getColor(Color color) {
		switch (color) {
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

	private AwtGraphicFactory() {
		// do nothing
	}

	@Override
	public Bitmap createBitmap(InputStream inputStream, int HashCode) throws IOException {
		return new AwtBitmap(inputStream);
	}

	@Override
	public Bitmap createBitmap(int width, int height) {
		return new AwtBitmap(width, height);
	}

	@Override
	public Canvas createCanvas() {
		return new AwtCanvas();
	}

	@Override
	public int createColor(Color color) {
		return getColor(color).getRGB();
	}

	@Override
	public int createColor(int alpha, int red, int green, int blue) {
		return new java.awt.Color(red, green, blue, alpha).getRGB();
	}

	@Override
	public Matrix createMatrix() {
		return new AwtMatrix();
	}

	@Override
	public Paint createPaint() {
		return new AwtPaint();
	}

	@Override
	public Path createPath() {
		return new AwtPath();
	}
}
