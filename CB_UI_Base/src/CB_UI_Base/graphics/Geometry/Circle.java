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

import com.badlogic.gdx.math.MathUtils;

/**
 * Holds the center point and radius of a circle geometry <br>
 * <br>
 * With the method Compute() will calculate the vertices and triangle indices.
 * 
 * @author Longri
 */
public class Circle implements IGeometry {
	private final int MIN_SEG;

	protected float centerX;
	protected float centerY;
	protected float radius;
	protected float[] vertices;
	protected short[] triangleIndices;

	protected AtomicBoolean isDisposed = new AtomicBoolean(false);

	/**
	 * false, if vertices and triangle indices are calculated for actual circle values;
	 */
	protected boolean isDirty = true;

	/**
	 * Constructor
	 * 
	 * @param x
	 *            center.x
	 * @param y
	 *            center.y
	 * @param r
	 *            radius
	 */
	public Circle(float x, float y, float r) {
		centerX = x;
		centerY = y;
		radius = r;
		MIN_SEG = MIN_CIRCLE_SEGMENTH_COUNT;
	}

	/**
	 * Constructor
	 * 
	 * @param x
	 *            center.x
	 * @param y
	 *            center.y
	 * @param r
	 *            radius
	 * @param compute
	 *            true for call Compute() with constructor
	 */
	public Circle(float x, float y, float r, boolean compute) {
		this(x, y, r);
		if (compute)
			Compute();
	}

	public Circle(float x, float y, float r, int minimumSegmente) {
		MIN_SEG = minimumSegmente;
		centerX = x;
		centerY = y;
		radius = r;
		Compute();
	}

	/**
	 * Set the X value of center point.<br>
	 * <br>
	 * After change the vertices and triangles must new calculated!
	 * 
	 * @param x
	 */
	public void setCenterX(float x) {
		centerX = x;
		isDirty = true;
	}

	/**
	 * Set the Y value of center point.<br>
	 * <br>
	 * After change the vertices and triangles must new calculated!
	 * 
	 * @param y
	 */
	public void setCenterY(float y) {
		centerY = y;
		isDirty = true;
	}

	/**
	 * Set the radius of this circle.<br>
	 * <br>
	 * After change the vertices and triangles must new calculated!
	 * 
	 * @param r
	 *            Radius
	 */
	public void setRadius(float r) {
		radius = r;
		isDirty = true;
	}

	/**
	 * Calculate the vertices of this circle with a minimum segment length of 10. <br>
	 * OR a minimum segment count of 18. <br>
	 * <br>
	 * For every segment are compute a triangle from the segment start, end and the center of this circle.
	 */
	public void Compute() {
		if (!isDirty)
			return; // Nothing todo

		// calculate segment count
		double alpha = (360 * MIN_CIRCLE_SEGMENTH_LENGTH) / (MathUtils.PI2 * radius);
		int segmente = Math.max(MIN_SEG, (int) (360 / alpha));

		// calculate theta step
		double thetaStep = (MathUtils.PI2 / segmente);

		// initialize arrays
		vertices = new float[(segmente + 1) * 2];
		triangleIndices = new short[(segmente) * 3];

		int index = 0;

		// first point is the center point
		vertices[index++] = centerX;
		vertices[index++] = centerY;

		int triangleIndex = 0;
		short verticeIdex = 1;
		boolean beginnTriangles = false;
		for (float i = 0; index < (segmente + 1) * 2; i += thetaStep) {
			vertices[index++] = centerX + radius * MathUtils.cos(i);
			vertices[index++] = centerY + radius * MathUtils.sin(i);

			if (!beginnTriangles) {
				if (index % 6 == 0)
					beginnTriangles = true;
			}

			if (beginnTriangles) {
				triangleIndices[triangleIndex++] = 0;
				triangleIndices[triangleIndex++] = verticeIdex++;
				triangleIndices[triangleIndex++] = verticeIdex;
			}

		}

		// last triangle
		triangleIndices[triangleIndex++] = 0;
		triangleIndices[triangleIndex++] = verticeIdex;
		triangleIndices[triangleIndex++] = 1;

		isDirty = false;
	}

	@Override
	public float[] getVertices() {
		if (isDirty)
			Compute();
		return vertices;
	}

	@Override
	public short[] getTriangles() {
		if (isDirty)
			Compute();
		return triangleIndices;
	}

	public boolean isDisposed() {
		return isDisposed.get();
	}

	@Override
	public void dispose() {
		synchronized (isDisposed) {
			if (isDisposed.get())
				return;
			vertices = null;
			triangleIndices = null;
			isDisposed.set(true);
		}
	}
}
