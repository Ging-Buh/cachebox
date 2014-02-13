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
import java.util.Arrays;

import org.mapsforge.core.graphics.FillRule;

import CB_UI_Base.graphics.Geometry.CircularSegment;
import CB_UI_Base.graphics.extendedIntrefaces.ext_Matrix;
import CB_UI_Base.graphics.extendedIntrefaces.ext_Path;
import CB_UI_Base.graphics.fromAndroid.RectF;

import com.badlogic.gdx.math.Bezier;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;

/**
 * @author Longri
 */
public class GL_Path implements ext_Path, Disposable
{
	final static float MIN_SEGMENTH_LENGTH = 10;

	public float[] items;
	private float[] last;
	public int size = 0;
	float[] PathSectionLength;
	private ArrayList<Integer> pathBegins = new ArrayList<Integer>();
	private int aktBeginn = 0;
	private boolean isDisposed = false;

	private float averageDirection = Float.MAX_VALUE;

	public GL_Path()
	{
		this(54);
	}

	public GL_Path(int capacity)
	{
		items = new float[capacity];
		last = new float[2];
	}

	public GL_Path(GL_Path path)
	{
		this(path.size + 2);
		size = path.size;
		last[0] = path.last[0];
		last[1] = path.last[1];
		if (PathSectionLength != null)
		{
			PathSectionLength = new float[path.PathSectionLength.length];
			System.arraycopy(path.PathSectionLength, 0, PathSectionLength, 0, PathSectionLength.length);
		}

		items = new float[path.items.length];
		System.arraycopy(path.items, 0, items, 0, items.length);

		for (int t : path.pathBegins)
		{
			pathBegins.add(t);
		}
	}

	private void setLast(float x, float y)
	{
		last[0] = x;
		last[1] = y;
	}

	public void setToMaxItems(int value)
	{
		if (items.length < value * 2) resize(value * 2);
	}

	/**
	 * Add a line from the last point to the specified point (x,y).
	 */
	@Override
	public void lineTo(float x, float y)
	{
		if (!isMoveTo)
		{
			if (last[0] == x && last[1] == y)
			{
				return;
			}
		}

		isMoveTo = false;

		if (size + 2 >= items.length) resize(size + (size >> 1));
		items[size++] = x;
		items[size++] = y;
		setLast(x, y);
	}

	private void resize(int newSize)
	{
		this.items = Arrays.copyOf(this.items, newSize);
	}

	private boolean isMoveTo = false;

	@Override
	public void moveTo(float x, float y)
	{
		aktBeginn = size;
		pathBegins.add(size);
		isMoveTo = true;
		lineTo(x, y);
	}

	@Override
	public void clear()
	{
		size = 0;
		aktBeginn = 0;
		pathBegins.clear();
	}

	public ArrayList<float[]> getVertices()
	{

		ArrayList<float[]> tmp = new ArrayList<float[]>();

		if (pathBegins.size() > 1)
		{
			// Multi path
			for (int i = 0; i < pathBegins.size(); i++)
			{
				int pathBegin = pathBegins.get(i);

				int pathLength = 0;
				if (i + 1 == pathBegins.size())
				{
					pathLength = size - pathBegin;
				}
				else
				{
					int pathEnd = pathBegins.get(i + 1);
					pathLength = pathEnd - pathBegin;
				}

				float[] array = new float[pathLength];
				System.arraycopy(items, pathBegin, array, 0, pathLength);
				tmp.add(array);
			}
		}
		else
		{
			float[] array = new float[size];
			System.arraycopy(items, 0, array, 0, size);
			tmp.add(array);
		}

		return tmp;
	}

	@Override
	public void setFillRule(FillRule fillRule)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void rMoveTo(float x, float y)
	{
		moveTo(last[0] + x, last[1] + y);
	}

	/**
	 * Close the current contour. If the current point is not equal to the first point of the contour, a line segment is automatically
	 * added.
	 */
	@Override
	public void close()
	{
		if (isClosed()) return;
		lineTo(items[aktBeginn], items[aktBeginn + 1]);
	}

	public boolean isClosed()
	{
		return (items[aktBeginn] == last[0] && items[aktBeginn + 1] == last[1]);
	}

	@Override
	public void rLineTo(float x, float y)
	{
		lineTo(last[0] + x, last[1] + y);
	}

	/**
	 * Add a cubic bezier from the last point, approaching control points (x1,y1) and (x2,y2), and ending at (x3,y3). If no moveTo() call
	 * has been made for this contour, the first point is automatically set to (0,0).
	 * 
	 * @param x1
	 *            The x-coordinate of the 1st control point on a cubic curve
	 * @param y1
	 *            The y-coordinate of the 1st control point on a cubic curve
	 * @param x2
	 *            The x-coordinate of the 2nd control point on a cubic curve
	 * @param y2
	 *            The y-coordinate of the 2nd control point on a cubic curve
	 * @param x3
	 *            The x-coordinate of the end point on a cubic curve
	 * @param y3
	 *            The y-coordinate of the end point on a cubic curve
	 */
	@Override
	public void cubicTo(float x1, float y1, float x2, float y2, float x3, float y3)
	{

		// calculate distance
		float distance = calcDistance(last[0], last[1], x1, y1);
		distance += calcDistance(x1, y1, x2, y2);
		distance += calcDistance(x2, y2, x3, y3);
		distance = Math.min(0.2f, 1 / (distance / MIN_SEGMENTH_LENGTH));

		if (items.length == 0)
		{
			throw new IllegalStateException("Missing initial moveTo()");
		}

		Vector2 vec0 = new Vector2(last[0], last[1]);
		Vector2 vec1 = new Vector2(x1, y1);
		Vector2 vec2 = new Vector2(x2, y2);
		Vector2 vec3 = new Vector2(x3, y3);

		for (float location = distance; !(location > 1); location += distance)
		{
			Vector2 out = new Vector2();
			Vector2 tmp = new Vector2();
			Bezier.cubic(out, location, vec0, vec1, vec2, vec3, tmp);
			lineTo(out.x, out.y);
		}
		lineTo(x3, y3);
	}

	private float calcDistance(float x1, float y1, float x2, float y2)
	{
		float dx = x1 - x2;
		float dy = y1 - y2;
		return (float) Math.sqrt(dx * dx + dy * dy);
	}

	@Override
	public void addArc(RectF oval, float angleStart, float angleExtent)
	{
		CircularSegment cir = new CircularSegment(oval.centerX(), oval.centerY(), oval.width(), angleStart, angleStart + angleExtent);

		float[] array = cir.getVertices();
		int insertPos = size + 1;
		if (size + array.length + 1 >= items.length)
		{
			size += array.length;
			resize(size + (size >> 1));
		}
		System.arraycopy(array, 0, items, insertPos, array.length);

		// #######################################

		// Maby use CPP-Code from Android Path
		// #######################################

		// void SkPath::addOval(const SkRect& oval, Direction dir) {
		// SkAutoPathBoundsUpdate apbu(this, oval);
		//
		// SkScalar cx = oval.centerX();
		// SkScalar cy = oval.centerY();
		// SkScalar rx = SkScalarHalf(oval.width());
		// SkScalar ry = SkScalarHalf(oval.height());
		// if 0 // these seem faster than using quads (1/2 the number of edges)
		// SkScalar sx = SkScalarMul(rx, CUBIC_ARC_FACTOR);
		// SkScalar sy = SkScalarMul(ry, CUBIC_ARC_FACTOR);
		//
		// this->incReserve(13);
		// this->moveTo(cx + rx, cy);
		// if (dir == kCCW_Direction) {
		// this->cubicTo(cx + rx, cy - sy, cx + sx, cy - ry, cx, cy - ry);
		// this->cubicTo(cx - sx, cy - ry, cx - rx, cy - sy, cx - rx, cy);
		// this->cubicTo(cx - rx, cy + sy, cx - sx, cy + ry, cx, cy + ry);
		// this->cubicTo(cx + sx, cy + ry, cx + rx, cy + sy, cx + rx, cy);
		// } else {
		// this->cubicTo(cx + rx, cy + sy, cx + sx, cy + ry, cx, cy + ry);
		// this->cubicTo(cx - sx, cy + ry, cx - rx, cy + sy, cx - rx, cy);
		// this->cubicTo(cx - rx, cy - sy, cx - sx, cy - ry, cx, cy - ry);
		// this->cubicTo(cx + sx, cy - ry, cx + rx, cy - sy, cx + rx, cy);
		// }
		//

	}

	/**
	 * Transform the points in this path by matrix, and write the answer into dst. If dst is null, then the the original path is modified.
	 * 
	 * @param matrix
	 *            The matrix to apply to the path
	 * @param dst
	 *            The transformed path is written here. If dst is null, then the the original path is modified
	 */
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

	/**
	 * Add a quadratic bezier from the last point, approaching control point (x1,y1), and ending at (x2,y2). If no moveTo() call has been
	 * made for this contour, the first point is automatically set to (0,0).
	 * 
	 * @param x1
	 *            The x-coordinate of the control point on a quadratic curve
	 * @param y1
	 *            The y-coordinate of the control point on a quadratic curve
	 * @param x2
	 *            The x-coordinate of the end point on a quadratic curve
	 * @param y2
	 *            The y-coordinate of the end point on a quadratic curve
	 */
	@Override
	public void quadTo(float x1, float y1, float x2, float y2)
	{

		// calculate distance
		float distance = calcDistance(last[0], last[1], x1, y1);
		distance += calcDistance(x1, y1, x2, y2);
		distance = Math.min(0.25f, 1 / (distance / MIN_SEGMENTH_LENGTH));

		if (items.length == 0)
		{
			throw new IllegalStateException("Missing initial moveTo()");
		}

		Vector2 vec0 = new Vector2(last[0], last[1]);
		Vector2 vec1 = new Vector2(x1, y1);
		Vector2 vec2 = new Vector2(x2, y2);

		for (float location = 0; location < 1; location += distance)
		{
			Vector2 out = new Vector2();
			Vector2 tmp = new Vector2();
			Bezier.quadratic(out, location, vec0, vec1, vec2, tmp);
			lineTo(out.x, out.y);
		}
		lineTo(x2, y2);
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

	public float getAverageDirection()
	{
		if (averageDirection == Float.MAX_VALUE) calcSectionLength();
		return averageDirection;
	}

	private void calcSectionLength()
	{
		averageDirection = 0;
		PathSectionLength = new float[((size - 2) / 2)];

		int index = 0;
		int angleCount = 0;
		float length = PathSectionLength[index] = 0;

		for (int i = 0; i < size - 2; i += 2)
		{

			float x1 = items[i];
			float y1 = items[i + 1];

			float x2 = items[i + 2];
			float y2 = items[i + 3];

			float segmentLength = (float) Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));

			length = PathSectionLength[index] = length + segmentLength;

			float angle = getAngle(index, index + 1);
			if (!Float.isInfinite(angle))
			{
				averageDirection += angle;
				angleCount++;
			}

			index++;
		}
		averageDirection /= angleCount;
	}

	/**
	 * Returns an Float array with the Position of a point on the Path after given distance!<br>
	 * or NULL, if the Path.length closer the given distance
	 * 
	 * @param distance
	 * @return float[3] <br>
	 *         [0]=x-value <br>
	 *         [1]=y-value <br>
	 *         [2]= angle of the Line segment from the point <br>
	 */
	public float[] getPointOnPathAfter(float distance)
	{
		if (size < 4) return null; // no Path
		if (distance < 0) return null; // not on Path

		if (distance == 0)
		{ // first Point of Path
			float[] ret = new float[3];
			ret[0] = items[0];
			ret[1] = items[1];

			// calc angle
			ret[2] = getAngle(0, 1);
			return ret;
		}
		else
		{
			if (distance > getLength())
			{
				return null;
			}

			if (PathSectionLength == null) calcSectionLength();

			int ind = 0;
			do
			{
				if (ind > PathSectionLength.length - 1)
				{
					// Path to close
					return null;
				}
				if (PathSectionLength[ind] >= distance)
				{
					// calc point on path with restDis on path line[ind]

					float movedLength = ind == 0 ? 0 : PathSectionLength[ind - 1];

					float dis = distance - movedLength;
					float[] res;

					int lineBeginn = (ind) * 2;

					if (dis > 0 && distance > 0)
					{
						res = computePointOnLine(items[lineBeginn], items[lineBeginn + 1], items[lineBeginn + 2], items[lineBeginn + 3],
								dis);
					}
					else
					{
						res = new float[]
							{ items[ind], items[ind + 1] };
					}

					float[] ret = new float[3];

					ret[0] = res[0];
					ret[1] = res[1];

					// calc angle
					ret[2] = getAngle(ind, ind < size ? ind + 1 : ind - 1);

					return ret;
				}
				ind++;
			}
			while (true);
		}
	}

	private float getAngle(int index1, int index2)
	{
		float ret = MathUtils.atan2((items[index2 * 2] - items[index1 * 2]), (items[index2 * 2 + 1] - items[index1 * 2 + 1]))
				* MathUtils.radiansToDegrees;

		// float ret = (float) (Math.atan2((items[index2 * 2] - items[index1 * 2]), (items[index2 * 2 + 1] - items[index1 * 2 + 1])) *
		// MathUtils.radiansToDegrees);

		ret = 90 - ret;

		return ret;
	}

	/**
	 * Find the point on the line p0,p1 [x,y] a given fraction from p0. Fraction of 0.0 whould give back p0, 1.0 give back p1, 0.5 returns
	 * midpoint of line p0,p1 and so on. F raction can be >1 and it can be negative to return any point on the line specified by p0,p1.
	 * 
	 * @param p0
	 *            First coordinate of line [x,y].
	 * @param p1
	 *            Second coordinate of line [x,y].
	 * @param fractionFromP0
	 *            Point we are looking for coordinates of
	 * @return p Coordinate of point we are looking for
	 */
	private static float[] computePointOnLine(float p0x, float p0y, float p1x, float p1y, float distance)
	{
		float[] p = new float[2];
		float vectorX = p1x - p0x;
		float vectorY = p1y - p0y;
		float factor = (float) (distance / Math.sqrt((vectorX * vectorX) + (vectorY * vectorY)));
		vectorX *= factor;
		vectorY *= factor;
		p[0] = (p0x + vectorX);
		p[1] = (p0y + vectorY);
		return p;
	}

	public float getLength()
	{
		if (PathSectionLength == null) calcSectionLength();
		if (PathSectionLength.length < 1)
		{
			return 0;
		}
		return PathSectionLength[PathSectionLength.length - 1];

	}

	/**
	 * Flip all y value by Y= flipSize - Y
	 * 
	 * @param flipSize
	 */
	public void flipY(float flipSize)
	{
		for (int i = 1; i < size; i += 2)
		{
			items[i] = flipSize - items[i];
		}
	}

	public boolean isDisposed()
	{
		return isDisposed;
	}

	@Override
	public void dispose()
	{
		if (isDisposed) return;
		items = null;
		pathBegins = null;
		last = null;
		PathSectionLength = null;
		isDisposed = true;
	}

	/**
	 * revert the beginn and end of this Path
	 */
	public void revert()
	{
		float[] tmp = new float[size + 2];
		for (int i = 0; i < size; i += 2)
		{
			int rev = size - 2 - i;

			tmp[i] = items[rev];
			tmp[i + 1] = items[rev + 1];
		}
		items = tmp;
		PathSectionLength = null;
		averageDirection = Float.NaN;
	}

}
