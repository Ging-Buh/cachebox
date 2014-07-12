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

/**
 * Holds the three edge points of a triangle.<br>
 * <br>
 * The triangle indices are final {0,1,2}
 * 
 * @author Longri
 */
public class Triangle implements IGeometry
{

	private float[] vertices = new float[6];
	private short[] triangleIndices = new short[]
		{ 0, 1, 2 };

	protected AtomicBoolean isDisposed = new AtomicBoolean(false);

	public Triangle(float x1, float y1, float x2, float y2, float x3, float y3)
	{
		vertices[0] = x1;
		vertices[1] = y1;

		vertices[2] = x2;
		vertices[3] = y2;

		vertices[4] = x3;
		vertices[5] = y3;
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
	 * Returns True, if the given Point inside this Triangle
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public boolean containsPoint(float x, float y)
	{
		float v1X = vertices[2] - vertices[0];
		float v1Y = vertices[3] - vertices[1];

		float v2X = vertices[4] - vertices[0];
		float v2Y = vertices[5] - vertices[1];

		float qX = x - vertices[0];
		float qY = y - vertices[1];

		float s = crossProduct(qX, qY, v2X, v2Y) / crossProduct(v1X, v1Y, v2X, v2Y);
		float t = crossProduct(v1X, v1Y, qX, qY) / crossProduct(v1X, v1Y, v2X, v2Y);

		if ((s >= 0) && (t >= 0) && (s + t <= 1))
		{
			/* point is inside the triangle */
			return true;
		}
		else
		{
			return false;
		}
	}

	/**
	 * Calculates crossProduct of two 2D vectors / points.
	 * 
	 * @param p1
	 *            first point used as vector
	 * @param p2
	 *            second point used as vector
	 * @return crossProduct of vectors
	 */
	private float crossProduct(float p1X, float p1Y, float p2X, float p2Y)
	{
		return (p1X * p2Y - p1Y * p2X);
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
			isDisposed.set(true);
		}
	}
}
