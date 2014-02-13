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
package org.mapsforge.map.awt;

import java.awt.geom.AffineTransform;

import org.mapsforge.core.graphics.Matrix;

import CB_UI_Base.graphics.extendedIntrefaces.ext_Matrix;

import com.badlogic.gdx.math.Matrix4;

class AwtMatrix implements ext_Matrix, Matrix
{
	final AffineTransform affineTransform = new AffineTransform();

	@Override
	public void reset()
	{
		this.affineTransform.setToIdentity();
	}

	@Override
	public void rotate(float theta)
	{
		this.affineTransform.rotate(theta);
	}

	@Override
	public void rotate(float theta, float pivotX, float pivotY)
	{
		this.affineTransform.rotate(theta, pivotX, pivotY);
	}

	@Override
	public void scale(float scaleX, float scaleY)
	{
		this.affineTransform.scale(scaleX, scaleY);
	}

	@Override
	public void scale(float scaleX, float scaleY, float pivotX, float pivotY)
	{
		this.affineTransform.translate(pivotX, pivotY);
		this.affineTransform.scale(scaleX, scaleY);
		this.affineTransform.translate(-pivotX, -pivotY);
	}

	@Override
	public void translate(float translateX, float translateY)
	{
		this.affineTransform.translate(translateX, translateY);
	}

	@Override
	public void dispose()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void set(ext_Matrix matrix)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void postConcat(ext_Matrix matrix)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void preTranslate(float x, float y)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void preScale(float x, float y)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void preScale(float sx, float sy, float px, float py)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void postRotate(float angle)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void postScale(float rx, float ry)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void postTranslate(float cx, float cy)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void mapPoints(float[] src)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void mapPoints(float[] dst, int dstIndex, float[] src, int srcIndex, int pointCount)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public Matrix4 getMatrix4()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void preRotate(float angle)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void setValues(float[] fs)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void preSkew(float f, float tan)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void preRotate(Float angle, Float cx, Float cy)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void getValues(float[] mValues)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void preConcat(ext_Matrix matrix)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public boolean invert()
	{
		// TODO Auto-generated method stub
		return false;
	}

}
