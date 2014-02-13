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
package CB_UI_Base.graphics.Geometry;

import java.util.concurrent.atomic.AtomicBoolean;

import CB_UI_Base.Global;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.graphics.GL_Cap;
import CB_UI_Base.graphics.Join;
import CB_UI_Base.graphics.fromAndroid.RectF;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector2;

/**
 * Holds the four edge points of a square.<br>
 * <br>
 * The triangle indices are final {0,1,2,2,3,0}
 * 
 * @author Longri
 */
public class Quadrangle implements IGeometry
{
	public float[] cor;
	public float[] vertices = new float[8];
	public short[] triangleIndices = new short[]
		{ 0, 1, 2, 2, 3, 0 };

	protected AtomicBoolean isDisposed = new AtomicBoolean(false);

	public Quadrangle(float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4)
	{
		this.cor = null;

		vertices[0] = x1;
		vertices[1] = y1;

		vertices[2] = x2;
		vertices[3] = y2;

		vertices[4] = x3;
		vertices[5] = y3;

		vertices[6] = x4;
		vertices[7] = y4;
	}

	public Quadrangle(CB_RectF rec)
	{
		this.cor = null;

		vertices[0] = rec.getX();
		vertices[1] = rec.getY();

		vertices[2] = rec.getX();
		vertices[3] = rec.getMaxY();

		vertices[4] = rec.getMaxX();
		vertices[5] = rec.getMaxY();

		vertices[6] = rec.getMaxX();
		vertices[7] = rec.getY();
	}

	public Quadrangle(Line line, float strokeWidth)
	{
		this.cor = line.getVertices();
		float dX = cor[2] - cor[0];
		float dY = cor[3] - cor[1];
		float lineLength = (float) Math.sqrt(dX * dX + dY * dY);

		float scale = (strokeWidth) / (2 * lineLength);

		float dx = -scale * dY;
		float dy = scale * dX;

		vertices[0] = cor[0] + dx;
		vertices[1] = cor[1] + dy;
		vertices[2] = cor[0] - dx;
		vertices[3] = cor[1] - dy;
		vertices[4] = cor[2] - dx;
		vertices[5] = cor[3] - dy;
		vertices[6] = cor[2] + dx;
		vertices[7] = cor[3] + dy;

	}

	public Quadrangle(RectF rec)
	{
		this.cor = null;

		vertices[0] = rec.left;
		vertices[1] = rec.bottom;

		vertices[2] = rec.left;
		vertices[3] = rec.top;

		vertices[4] = rec.right;
		vertices[5] = rec.top;

		vertices[6] = rec.right;
		vertices[7] = rec.bottom;
	}

	public Quadrangle(float[] vertices2)
	{
		System.arraycopy(vertices2, 0, vertices, 0, 8);
		this.cor = null;
	}

	@Override
	public float[] getVertices()
	{
		return vertices;
	}

	@Override
	public short[] getTriangles()
	{
		return triangleIndices;
	}

	/**
	 * Returns True, if the given Point inside this Quadrangle
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public boolean containsPoint(float x, float y)
	{
		Triangle t1 = new Triangle(vertices[0], vertices[1], vertices[2], vertices[3], vertices[4], vertices[5]);
		if (t1.containsPoint(x, y)) return true;

		Triangle t2 = new Triangle(vertices[4], vertices[5], vertices[6], vertices[7], vertices[0], vertices[1]);
		if (t2.containsPoint(x, y)) return true;

		return false;
	}

	@Override
	public String toString()
	{
		String ret = "";
		if (cor != null)
		{
			ret = "[l " + cor[0] + "," + cor[1] + "-" + cor[2] + "," + cor[3] + "]" + Global.br;
		}
		return ret + "[" + vertices[0] + "," + vertices[1] + " | " + vertices[2] + "," + vertices[3] + " | " + vertices[4] + ","
				+ vertices[5] + " | " + vertices[6] + "," + vertices[7] + "]";
	}

	/**
	 * returns the Join geometry for the two given Quadrangle <br>
	 * returns NULL, if the end point of qua non equals start point of qua2 ( like not closed)
	 * 
	 * @param qua
	 * @param qua2
	 * @param join
	 * @return
	 */
	public static IGeometry getJoin(Quadrangle qua, Quadrangle qua2, Join join)
	{
		if (qua.cor == null || qua2.cor == null || qua.cor[2] != qua2.cor[0] || qua.cor[3] != qua2.cor[1]) return null;// Can't calculate

		float JoinX = qua.cor[2];
		float JoinY = qua.cor[3];

		float Out1StartX;
		float Out1StartY;
		float Out1EndX;
		float Out1EndY;

		if (qua2.containsPoint(qua.vertices[4], qua.vertices[5]))
		{
			Out1StartX = qua.vertices[6];
			Out1StartY = qua.vertices[7];
			Out1EndX = qua.vertices[0];
			Out1EndY = qua.vertices[1];
		}
		else
		{
			Out1StartX = qua.vertices[4];
			Out1StartY = qua.vertices[5];
			Out1EndX = qua.vertices[2];
			Out1EndY = qua.vertices[3];
		}

		float Out2StartX;
		float Out2StartY;
		float Out2EndX;
		float Out2EndY;

		if (qua.containsPoint(qua2.vertices[0], qua2.vertices[1]))
		{
			Out2StartX = qua2.vertices[2];
			Out2StartY = qua2.vertices[3];
			Out2EndX = qua2.vertices[4];
			Out2EndY = qua2.vertices[5];
		}
		else
		{
			Out2StartX = qua2.vertices[0];
			Out2StartY = qua2.vertices[1];
			Out2EndX = qua2.vertices[6];
			Out2EndY = qua2.vertices[7];
		}

		float r = new Line(JoinX, JoinY, Out1StartX, Out1StartY).length();

		switch (join)
		{
		case BEVEL:
			// return Triangle Out1, Out2, Join
			return new Triangle(Out1StartX, Out1StartY, Out2StartX, Out2StartY, JoinX, JoinY);

		case MITER:
			// return Quadrangle Out1, Out2, Join, mirrored Join

			// get intersection of outer lines
			Vector2 intersection = new Vector2();
			boolean intersect = Intersector.intersectLines(Out1StartX, Out1StartY, Out1EndX, Out1EndY, Out2StartX, Out2StartY, Out2EndX,
					Out2EndY, intersection);

			if (!intersect)
			{// no intersection, then return BEVEL
				return new Triangle(Out1StartX, Out1StartY, Out2StartX, Out2StartY, JoinX, JoinY);
			}

			// chek Line length
			Line li = new Line(JoinX, JoinY, intersection.x, intersection.y);
			Line li2 = new Line(qua2.vertices[0], qua2.vertices[1], qua2.vertices[6], qua2.vertices[7]);
			if (li.length() > li2.length())
			{
				// TODO cut the Triangle

				// use BEVEL, is wrong and must change
				return new Triangle(Out1StartX, Out1StartY, Out2StartX, Out2StartY, JoinX, JoinY);
			}

			return new Quadrangle(JoinX, JoinY, Out1StartX, Out1StartY, intersection.x, intersection.y, Out2StartX, Out2StartY);

		case ROUND:

			return new Circle(JoinX, JoinY, r, true);
		default:

			return new Circle(JoinX, JoinY, r, true);

		}
	}

	public static IGeometry getCap(Quadrangle qua, GL_Cap cap, boolean beginn)
	{
		if (qua.cor == null) return null;// Can't calculate

		float JoinX;
		float JoinY;

		float Out1StartX;
		float Out1StartY;

		float Out2StartX;
		float Out2StartY;

		if (beginn)
		{
			JoinX = qua.cor[0];
			JoinY = qua.cor[1];

			Out1StartX = qua.vertices[0];
			Out1StartY = qua.vertices[1];

			Out2StartX = qua.vertices[2];
			Out2StartY = qua.vertices[3];
		}
		else
		{
			JoinX = qua.cor[2];
			JoinY = qua.cor[3];

			Out1StartX = qua.vertices[4];
			Out1StartY = qua.vertices[5];

			Out2StartX = qua.vertices[6];
			Out2StartY = qua.vertices[7];
		}

		float r = new Line(JoinX, JoinY, Out1StartX, Out1StartY).length();

		switch (cap)
		{
		case BUTT:
			return null; // nothing todo

		case DEFAULT:
			return new Circle(JoinX, JoinY, r, true);

		case ROUND:
			return new Circle(JoinX, JoinY, r, true);

		case SQUARE:

			float[] cor = new float[]
				{ Out1StartX, Out1StartY, Out2StartX, Out2StartY };
			float[] vertices = new float[8];

			float dX = cor[2] - cor[0];
			float dY = cor[3] - cor[1];
			float lineLength = (float) Math.sqrt(dX * dX + dY * dY);

			float scale = (r * 2) / (2 * lineLength);

			float dx = -scale * dY;
			float dy = scale * dX;

			vertices[0] = cor[0] + dx;
			vertices[1] = cor[1] + dy;
			vertices[2] = cor[0] - dx;
			vertices[3] = cor[1] - dy;
			vertices[4] = cor[2] - dx;
			vertices[5] = cor[3] - dy;
			vertices[6] = cor[2] + dx;
			vertices[7] = cor[3] + dy;

			return new Quadrangle(vertices);

		}

		return null;
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
			vertices = null;
			triangleIndices = null;
			cor = null;
			isDisposed.set(true);
		}
	}
}
