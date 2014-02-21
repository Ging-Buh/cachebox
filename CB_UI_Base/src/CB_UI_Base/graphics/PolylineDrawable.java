/* 
 * Copyright (C) 2014 team-cachebox.de
 *
 * Licensed under the : GNU General Public License (GPL);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.gnu.org/licenses/gpl.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package CB_UI_Base.graphics;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import CB_UI_Base.graphics.Geometry.GeometryList;
import CB_UI_Base.graphics.Geometry.IGeometry;
import CB_UI_Base.graphics.Geometry.Line;
import CB_UI_Base.graphics.Geometry.PathLine;
import CB_UI_Base.graphics.Geometry.Quadrangle;
import CB_UI_Base.graphics.Geometry.QuadranglePath;
import CB_UI_Base.graphics.Images.IRotateDrawable;

import com.badlogic.gdx.graphics.g2d.Batch;

/**
 * @author Longri
 */
public class PolylineDrawable implements IRotateDrawable
{

	private GL_Paint PAINT;

	private PolygonDrawable DRAWABLE;
	final float WIDTH;
	final float HEIGHT;
	private final AtomicBoolean isDisposed = new AtomicBoolean(false);

	public PolylineDrawable(float[] coords, GL_Paint paint, float width, float height)
	{
		PAINT = paint;
		WIDTH = width;
		HEIGHT = height;
		createGeometryList(coords, paint);
	}

	public PolylineDrawable(GL_Path path, GL_Paint paint, float width, float height)
	{
		PAINT = paint;
		WIDTH = width;
		HEIGHT = height;
		createGeometryList(path.getVertices(), paint);
	}

	public PolylineDrawable(PathLine pathLine, GL_Paint paint, int width, int height)
	{
		PAINT = paint;
		WIDTH = width;
		HEIGHT = height;

		float[] coords = new float[pathLine.size() * 4];

		int index = 0;
		for (Line line : pathLine)
		{
			coords[index++] = line.points[0];
			coords[index++] = line.points[1];
			coords[index++] = line.points[2];
			coords[index++] = line.points[3];
		}
		createGeometryList(coords, paint);
	}

	private void createGeometryList(float[] coords, GL_Paint paint)
	{
		PathLine lines = new PathLine(coords);
		if (paint.getDashArray() != null) lines.splittWithDashArray(paint.strokeDasharray);
		QuadranglePath quaList = new QuadranglePath(lines, paint);
		createDrawable(quaList, paint);
	}

	private void createGeometryList(ArrayList<float[]> multicoords, GL_Paint paint)
	{

		PathLine allLines = new PathLine();

		for (float[] coords : multicoords)
		{
			PathLine lines = new PathLine(coords);
			allLines.addAll(lines);
		}

		if (paint.getDashArray() != null) allLines.splittWithDashArray(paint.strokeDasharray);
		QuadranglePath quaList = new QuadranglePath(allLines, paint);
		createDrawable(quaList, paint);
	}

	private void createDrawable(QuadranglePath quaList, GL_Paint paint)
	{
		GeometryList GEOMETRYS = new GeometryList();

		// check if closed and add Join for this
		Quadrangle quaLast = quaList.get(quaList.size() - 1);
		Quadrangle quaFirst = quaList.get(0);
		IGeometry geomFirstLast = Quadrangle.getJoin(quaLast, quaFirst, paint.join);
		if (geomFirstLast != null)
		{// is closed, add JOIN
			GEOMETRYS.add(geomFirstLast);
		}
		else
		{// is not closed add CAP
			IGeometry capFirst = Quadrangle.getCap(quaFirst, paint.cap, true);
			if (capFirst != null) GEOMETRYS.add(capFirst);

			IGeometry capLast = Quadrangle.getCap(quaLast, paint.cap, false);
			if (capLast != null) GEOMETRYS.add(capLast);
		}

		for (int i = 0; i < quaList.size(); i++)
		{
			Quadrangle qua = quaList.get(i);
			GEOMETRYS.add(qua);
			if (i + 1 < quaList.size())
			{
				// get Join
				Quadrangle qua2 = quaList.get(i + 1);
				IGeometry geom = Quadrangle.getJoin(qua, qua2, paint.join);
				if (geom != null)
				{// is closed, add JOIN
					GEOMETRYS.add(geom);
				}
				else
				{// is not closed add CAP for qua and qua2
					IGeometry cap = Quadrangle.getCap(qua, paint.cap, false);
					if (cap != null) GEOMETRYS.add(cap);

					IGeometry cap2 = Quadrangle.getCap(qua2, paint.cap, true);
					if (cap2 != null) GEOMETRYS.add(cap2);
				}
			}
		}

		DRAWABLE = new PolygonDrawable(GEOMETRYS.getVertices(), GEOMETRYS.getTriangles(), PAINT, WIDTH, HEIGHT);
	}

	@Override
	public void draw(Batch batch, float x, float y, float width, float height, float rotate)
	{
		synchronized (isDisposed)
		{
			if (isDisposed.get()) return;
			if (DRAWABLE != null)
			{
				DRAWABLE.draw(batch, x, y, width, height, rotate);
			}
		}
	}

	public boolean isDisposed()
	{
		return isDisposed.get();
	}

	@Override
	public void dispose()
	{
		synchronized (isDisposed)
		{
			if (isDisposed.get()) return;
			PAINT = null;
			if (DRAWABLE != null) DRAWABLE.dispose();
			DRAWABLE = null;
			isDisposed.set(true);
		}
	}

}
