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

import java.awt.geom.Path2D;

import org.mapsforge.core.graphics.FillRule;

import CB_UI_Base.graphics.extendedIntrefaces.ext_Matrix;
import CB_UI_Base.graphics.extendedIntrefaces.ext_Path;
import CB_UI_Base.graphics.fromAndroid.RectF;

class AwtPath implements ext_Path
{
	private static int getWindingRule(FillRule fillRule)
	{
		switch (fillRule)
		{
		case EVEN_ODD:
			return Path2D.WIND_EVEN_ODD;
		case NON_ZERO:
			return Path2D.WIND_NON_ZERO;
		}

		throw new IllegalArgumentException("unknown fill rule:" + fillRule);
	}

	final Path2D path2D = new Path2D.Float();

	@Override
	public void clear()
	{
		this.path2D.reset();
	}

	@Override
	public void lineTo(float x, float y)
	{
		this.path2D.lineTo(x, y);
	}

	@Override
	public void moveTo(float x, float y)
	{
		this.path2D.moveTo(x, y);
	}

	@Override
	public void setFillRule(FillRule fillRule)
	{
		this.path2D.setWindingRule(getWindingRule(fillRule));
	}

	@Override
	public void rMoveTo(float x, float y)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void close()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void rLineTo(float x, float y)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void cubicTo(float x1, float y1, float x2, float y2, float x, float y)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void addArc(RectF oval, float angleStart, float angleExtent)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void transform(ext_Matrix currentMatrix, ext_Path transformedPath)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void computeBounds(RectF pathBounds, boolean b)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void quadTo(float x1, float y1, float x2, float y2)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void addPath(ext_Path path, ext_Matrix combinedPathMatrix)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void setFillType(FillType clipRuleFromState)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public FillType getFillType()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addPath(ext_Path spanPath)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void transform(ext_Matrix transform)
	{
		// TODO Auto-generated method stub

	}
}
