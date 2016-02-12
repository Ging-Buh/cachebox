/*
 * Copyright 2010, 2011, 2012, 2013 mapsforge.org
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
package org.mapsforge.map.android.graphics;

import org.mapsforge.core.graphics.Align;
import org.mapsforge.core.graphics.Cap;
import org.mapsforge.core.graphics.Color;
import org.mapsforge.core.graphics.FontFamily;
import org.mapsforge.core.graphics.FontStyle;
import org.mapsforge.core.graphics.Paint;
import org.mapsforge.core.graphics.Style;

import android.graphics.BitmapShader;
import android.graphics.DashPathEffect;
import android.graphics.Paint.Join;
import android.graphics.PathEffect;
import android.graphics.Rect;
import android.graphics.Shader.TileMode;
import android.graphics.Typeface;

class AndroidPaint implements Paint {
	private static android.graphics.Paint.Align getAndroidAlign(Align align) {
		switch (align) {
		case CENTER:
			return android.graphics.Paint.Align.CENTER;
		case LEFT:
			return android.graphics.Paint.Align.LEFT;
		case RIGHT:
			return android.graphics.Paint.Align.RIGHT;
		}

		throw new IllegalArgumentException("unknown align: " + align);
	}

	private static android.graphics.Paint.Cap getAndroidCap(Cap cap) {
		switch (cap) {
		case BUTT:
			return android.graphics.Paint.Cap.BUTT;
		case ROUND:
			return android.graphics.Paint.Cap.ROUND;
		case SQUARE:
			return android.graphics.Paint.Cap.SQUARE;
		}

		throw new IllegalArgumentException("unknown cap: " + cap);
	}

	private static android.graphics.Paint.Style getAndroidStyle(Style style) {
		switch (style) {
		case FILL:
			return android.graphics.Paint.Style.FILL;
		case STROKE:
			return android.graphics.Paint.Style.STROKE;
		}

		throw new IllegalArgumentException("unknown style: " + style);
	}

	private static int getFontStyle(FontStyle fontStyle) {
		switch (fontStyle) {
		case BOLD:
			return Typeface.BOLD;
		case BOLD_ITALIC:
			return Typeface.BOLD_ITALIC;
		case ITALIC:
			return Typeface.ITALIC;
		case NORMAL:
			return Typeface.NORMAL;
		}

		throw new IllegalArgumentException("unknown font style: " + fontStyle);
	}

	private static Typeface getTypeface(org.mapsforge.core.graphics.FontFamily fontFamily) {
		switch (fontFamily) {
		case DEFAULT:
			return Typeface.DEFAULT;
		case MONOSPACE:
			return Typeface.MONOSPACE;
		case SANS_SERIF:
			return Typeface.SANS_SERIF;
		case SERIF:
			return Typeface.SERIF;
		}

		throw new IllegalArgumentException("unknown font family: " + fontFamily);
	}

	final android.graphics.Paint paint = new android.graphics.Paint();

	AndroidPaint() {
		this.paint.setAntiAlias(true);
		this.paint.setStrokeCap(getAndroidCap(Cap.ROUND));
		this.paint.setStrokeJoin(Join.ROUND);
		this.paint.setStyle(getAndroidStyle(Style.FILL));
	}

	@Override
	public int getTextHeight(String text) {
		Rect rect = new Rect();
		this.paint.getTextBounds(text, 0, text.length(), rect);
		return rect.height();
	}

	@Override
	public int getTextWidth(String text) {
		Rect rect = new Rect();
		this.paint.getTextBounds(text, 0, text.length(), rect);
		return rect.width();
	}

	@Override
	public boolean isTransparent() {
		return this.paint.getShader() == null && this.paint.getAlpha() == 0;
	}

	@Override
	public void setBitmapShader(org.mapsforge.core.graphics.Bitmap bitmap) {
		if (bitmap == null) {
			return;
		}

		this.paint.setColor(AndroidGraphicFactory.getColor(Color.WHITE));
		this.paint.setShader(new BitmapShader(AndroidGraphicFactory.getBitmap(bitmap), TileMode.REPEAT, TileMode.REPEAT));
	}

	@Override
	public void setColor(Color color) {
		this.paint.setColor(AndroidGraphicFactory.getColor(color));
	}

	@Override
	public void setColor(int color) {
		this.paint.setColor(color);
	}

	float[] strokeDasharray;

	@Override
	public void setDashPathEffect(float[] strokeDasharray) {
		this.strokeDasharray = strokeDasharray;
		PathEffect pathEffect = new DashPathEffect(this.strokeDasharray, 0);
		this.paint.setPathEffect(pathEffect);
	}

	@Override
	public void setStrokeCap(Cap cap) {
		this.paint.setStrokeCap(getAndroidCap(cap));
	}

	@Override
	public void setStrokeWidth(float strokeWidth) {
		this.paint.setStrokeWidth(strokeWidth);
	}

	@Override
	public void setStyle(Style style) {
		this.paint.setStyle(getAndroidStyle(style));
	}

	@Override
	public void setTextAlign(Align align) {
		this.paint.setTextAlign(getAndroidAlign(align));
	}

	@Override
	public void setTextSize(float textSize) {
		this.paint.setTextSize(textSize);
	}

	FontFamily fontFamily;
	FontStyle fontStyle;

	@Override
	public void setTypeface(FontFamily fontFamily, FontStyle fontStyle) {
		this.fontFamily = fontFamily;
		this.fontStyle = fontStyle;
		this.paint.setTypeface(Typeface.create(getTypeface(fontFamily), getFontStyle(fontStyle)));
	}

	@Override
	public Cap getCap() {
		switch (this.paint.getStrokeCap()) {
		case BUTT:
			return Cap.BUTT;
		case ROUND:
			return Cap.ROUND;
		case SQUARE:
			return Cap.SQUARE;
		default:
			return Cap.BUTT;
		}

	}

	@Override
	public int getColor() {
		return this.paint.getColor();
	}

	@Override
	public Style getStyle() {
		switch (this.paint.getStyle()) {
		case FILL:
			return Style.FILL;
		case FILL_AND_STROKE:
			return Style.FILL;
		case STROKE:
			return Style.STROKE;
		default:
			return Style.STROKE;
		}
	}

	@Override
	public float getTextSize() {
		return this.paint.getTextSize();
	}

	@Override
	public float getStrokeWidth() {
		return this.paint.getTextSize();
	}

	@Override
	public float[] getDashArray() {
		return this.strokeDasharray;
	}

	@Override
	public FontFamily getFontFamily() {
		return this.fontFamily;
	}

	@Override
	public FontStyle getFontStyle() {
		return this.fontStyle;
	}
}
