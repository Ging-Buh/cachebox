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

import java.util.concurrent.atomic.AtomicBoolean;

import org.mapsforge.core.graphics.Matrix;

import CB_UI_Base.graphics.extendedIntrefaces.ext_Matrix;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;

/**
 * @author Longri
 */
public class GL_Matrix implements ext_Matrix, Matrix
{
	private final AtomicBoolean isDisposed = new AtomicBoolean(false);
	private Matrix4 matrix4;
	private static final Matrix4 DEFAULT = new Matrix4();

	public GL_Matrix(ext_Matrix matrix)
	{
		if (matrix == null)
		{
			matrix4 = new Matrix4(DEFAULT);
		}
		else
		{
			if (((GL_Matrix) matrix).matrix4 == null)
			{
				matrix4 = new Matrix4(DEFAULT);
			}
			else
			{
				matrix4 = new Matrix4(((GL_Matrix) matrix).matrix4);
			}
		}
	}

	public GL_Matrix()
	{
		matrix4 = new Matrix4();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see CB_UI_Base.graphics.extended.ext_Matrix2#reset()
	 */

	@Override
	public void reset()
	{
		matrix4.idt();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see CB_UI_Base.graphics.extended.ext_Matrix2#rotate(float)
	 */

	@Override
	public void rotate(float theta)
	{
		preRotate(theta * MathUtils.radiansToDegrees);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see CB_UI_Base.graphics.extended.ext_Matrix2#rotate(float, float, float)
	 */

	@Override
	public void rotate(float theta, float pivotX, float pivotY)
	{
		preRotate(theta * MathUtils.radiansToDegrees, pivotX, pivotY);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see CB_UI_Base.graphics.extended.ext_Matrix2#scale(float, float)
	 */

	@Override
	public void scale(float scaleX, float scaleY)
	{
		preScale(scaleX, scaleY);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see CB_UI_Base.graphics.extended.ext_Matrix2#scale(float, float, float, float)
	 */

	@Override
	public void scale(float scaleX, float scaleY, float pivotX, float pivotY)
	{
		preScale(scaleX, scaleY, pivotX, pivotY);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see CB_UI_Base.graphics.extended.ext_Matrix2#translate(float, float)
	 */

	@Override
	public void translate(float translateX, float translateY)
	{
		preTranslate(translateX, translateY);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see CB_UI_Base.graphics.extended.ext_Matrix2#set(CB_UI_Base.graphics.extended.ext_Matrix)
	 */
	@Override
	public void set(ext_Matrix matrix)
	{
		this.matrix4.set(((GL_Matrix) matrix).matrix4);
	}

	private void set(Matrix4 matrix)
	{
		this.matrix4.set(matrix);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see CB_UI_Base.graphics.extended.ext_Matrix2#postConcat(CB_UI_Base.graphics.extended.ext_Matrix)
	 */
	@Override
	public void postConcat(ext_Matrix matrix)
	{
		this.addTransform(((GL_Matrix) matrix).matrix4);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see CB_UI_Base.graphics.extended.ext_Matrix2#preTranslate(float, float)
	 */
	@Override
	public void preTranslate(float x, float y)
	{
		Matrix4 m = new Matrix4();
		m.translate(x, y, 0);
		m.mul(this.matrix4);
		set(m);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see CB_UI_Base.graphics.extended.ext_Matrix2#preScale(float, float)
	 */
	@Override
	public void preScale(float x, float y)
	{
		Matrix4 m = new Matrix4();
		m.scale(x, y, 1);
		m.mul(this.matrix4);
		set(m);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see CB_UI_Base.graphics.extended.ext_Matrix2#preScale(float, float, float, float)
	 */
	@Override
	public void preScale(float sx, float sy, float px, float py)
	{
		Matrix4 m = new Matrix4();
		m.scale(sx, sy, 1);

		Matrix4 m2 = new Matrix4();
		m2.translate(px, py, 0);

		m.mul(m2);

		m.mul(this.matrix4);
		set(m);

	}

	private void addTransform(Matrix4 matrix)
	{
		this.matrix4.mul(matrix);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see CB_UI_Base.graphics.extended.ext_Matrix2#postRotate(float)
	 */
	@Override
	public void postRotate(float angle)
	{
		Matrix4 m = new Matrix4();
		m.rotate(0, 0, 1, angle);
		this.matrix4.mul(m);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see CB_UI_Base.graphics.extended.ext_Matrix2#postScale(float, float)
	 */
	@Override
	public void postScale(float rx, float ry)
	{
		Matrix4 m = new Matrix4();
		m.scale(rx, ry, 1);
		this.matrix4.mul(m);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see CB_UI_Base.graphics.extended.ext_Matrix2#postTranslate(float, float)
	 */
	@Override
	public void postTranslate(float cx, float cy)
	{
		Matrix4 m = new Matrix4();
		m.translate(cx, cy, 0);
		this.matrix4.mul(m);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see CB_UI_Base.graphics.extended.ext_Matrix2#mapPoints(float[])
	 */
	@Override
	public void mapPoints(float[] src)
	{

		float[] dst = new float[src.length];

		for (int i = 0; i * 2 < src.length; i++)
		{

			int j = i * 2;

			float x = src[j] * matrix4.val[Matrix4.M00] + src[j + 1] * matrix4.val[Matrix4.M01] + matrix4.val[Matrix4.M03];
			float y = src[j] * matrix4.val[Matrix4.M10] + src[j + 1] * matrix4.val[Matrix4.M11] + matrix4.val[Matrix4.M13];

			dst[j] = x;
			dst[j + 1] = y;

		}

		System.arraycopy(dst, 0, src, 0, src.length);

	}

	public void mapVertices(float[] src)
	{

		float[] dst = new float[src.length];

		for (int i = 0; i * 5 < src.length; i++)
		{

			int j = i * 5;

			float x = src[j] * this.matrix4.val[0] + src[j + 1] * this.matrix4.val[3] + this.matrix4.val[6];
			float y = src[j] * this.matrix4.val[1] + src[j + 1] * this.matrix4.val[4] + this.matrix4.val[7];

			dst[j] = x;
			dst[j + 1] = y;
			dst[j + 2] = src[j + 2];
			dst[j + 3] = src[j + 3];
			dst[j + 4] = src[j + 4];

		}

		System.arraycopy(dst, 0, src, 0, src.length);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see CB_UI_Base.graphics.extended.ext_Matrix2#mapPoints(float[], int, float[], int, int)
	 */
	@Override
	public void mapPoints(float[] dst, int dstIndex, float[] src, int srcIndex, int pointCount)
	{
		checkPointArrays(src, srcIndex, dst, dstIndex, pointCount);

		float[] mValues = new float[9];
		getValues(mValues);

		for (int i = 0; i < pointCount; i++)
		{
			// just in case we are doing in place, we better put this in temp vars

			int j = i * 2;

			float x = mValues[0] * src[j + srcIndex] + mValues[1] * src[j + srcIndex + 1] + mValues[2];
			float y = mValues[3] * src[j + srcIndex] + mValues[4] * src[j + srcIndex + 1] + mValues[5];

			dst[j + dstIndex] = x;
			dst[j + dstIndex + 1] = y;
		}
	}

	public void mapPoints(GL_Path path2)
	{
		mapPoints(path2.items);
	}

	// private helper to perform range checks on arrays of "points"
	private static void checkPointArrays(float[] src, int srcIndex, float[] dst, int dstIndex, int pointCount)
	{
		// check for too-small and too-big indices
		int srcStop = srcIndex + (pointCount << 1);
		int dstStop = dstIndex + (pointCount << 1);
		if ((pointCount | srcIndex | dstIndex | srcStop | dstStop) < 0 || srcStop > src.length || dstStop > dst.length)
		{
			throw new ArrayIndexOutOfBoundsException();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see CB_UI_Base.graphics.extended.ext_Matrix2#getMatrix4()
	 */
	@Override
	public Matrix4 getMatrix4()
	{
		return matrix4;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see CB_UI_Base.graphics.extended.ext_Matrix2#preRotate(float)
	 */
	@Override
	public void preRotate(float angle)
	{
		Matrix4 m = new Matrix4();
		m.rotate(0, 0, 1, angle);
		m.mul(this.matrix4);
		set(m);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see CB_UI_Base.graphics.extended.ext_Matrix2#setValues(float[])
	 */
	@Override
	public void setValues(float[] fs)
	{

		matrix4.val[Matrix3.M00] = fs[0];
		matrix4.val[Matrix3.M01] = fs[1];
		matrix4.val[Matrix3.M02] = fs[2];
		matrix4.val[Matrix3.M10] = fs[3];
		matrix4.val[Matrix3.M11] = fs[4];
		matrix4.val[Matrix3.M12] = fs[5];
		matrix4.val[Matrix3.M20] = fs[6];
		matrix4.val[Matrix3.M21] = fs[7];
		matrix4.val[Matrix3.M22] = fs[8];

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see CB_UI_Base.graphics.extended.ext_Matrix2#preSkew(float, float)
	 */
	@Override
	public void preSkew(float f, float tan)
	{
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see CB_UI_Base.graphics.extended.ext_Matrix2#preRotate(java.lang.Float, java.lang.Float, java.lang.Float)
	 */
	@Override
	public void preRotate(Float angle, Float cx, Float cy)
	{
		Matrix4 m = new Matrix4();
		m.rotate(0, 0, 1, angle);
		Matrix4 m2 = new Matrix4();
		m2.translate(cx, cy, 0);

		m.mul(m2);
		m.mul(this.matrix4);
		set(m);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see CB_UI_Base.graphics.extended.ext_Matrix2#getValues(float[])
	 */
	@Override
	public void getValues(float[] mValues)
	{
		System.arraycopy(matrix4.getValues(), 0, mValues, 0, mValues.length);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see CB_UI_Base.graphics.extended.ext_Matrix2#preConcat(CB_UI_Base.graphics.extended.ext_Matrix)
	 */
	@Override
	public void preConcat(ext_Matrix matrix)
	{
		((GL_Matrix) matrix).matrix4.mul(this.matrix4);
		this.set(matrix);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see CB_UI_Base.graphics.extended.ext_Matrix2#invert()
	 */
	@Override
	public boolean invert()
	{
		try
		{
			this.matrix4.inv();
		}
		catch (Exception e)
		{
			return false;
		}

		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see CB_UI_Base.graphics.extended.ext_Matrix2#toString()
	 */

	@Override
	public String toString()
	{
		return "[" + this.matrix4.val[0] + "|" + this.matrix4.val[3] + "|" + this.matrix4.val[6] + "]\n" + "[" + this.matrix4.val[1] + "|"
				+ this.matrix4.val[4] + "|" + this.matrix4.val[7] + "]\n" + "[" + this.matrix4.val[2] + "|" + this.matrix4.val[5] + "|"
				+ this.matrix4.val[8] + "]";
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
			matrix4 = null;
			isDisposed.set(true);
		}
	}

	@Override
	public boolean isDefault()
	{
		return MatrixEquals(this.matrix4, DEFAULT);
	}

	public static boolean MatrixEquals(Matrix4 matrix1, Matrix4 matrix2)
	{

		for (int i = 0; i < 16; i++)
		{
			if (matrix1.val[i] != matrix2.val[i]) return false;
		}

		return true;
	}

	public static void MapPoints(float[] values, Matrix4 matrix4)
	{
		int index = 0;
		while (index < values.length)
		{
			float x0 = values[index] * matrix4.val[Matrix4.M00] + values[index + 1] * matrix4.val[Matrix4.M01] + matrix4.val[Matrix4.M03];
			float y0 = values[index] * matrix4.val[Matrix4.M10] + values[index + 1] * matrix4.val[Matrix4.M11] + matrix4.val[Matrix4.M13];

			values[index] = x0;
			values[index + 1] = y0;

			index += 2;
		}
	}

	public static void MapPoints(float[] values, Matrix3 matrix3)
	{
		int index = 0;
		while (index < values.length)
		{
			float x0 = values[index] * matrix3.val[0] + values[index + 1] * matrix3.val[3] + matrix3.val[6];
			float y0 = values[index] * matrix3.val[1] + values[index + 1] * matrix3.val[4] + matrix3.val[7];

			values[index] = x0;
			values[index + 1] = y0;

			index += 2;
		}
	}

	public static void MapPoint(float x, float y, Matrix4 matrix4, float[] mapedPoint)
	{
		mapedPoint[0] = x * matrix4.val[Matrix4.M00] + y * matrix4.val[Matrix4.M01] + matrix4.val[Matrix4.M03];
		mapedPoint[1] = x * matrix4.val[Matrix4.M10] + y * matrix4.val[Matrix4.M11] + matrix4.val[Matrix4.M13];
	}

	public static void MapPoint(float x, float y, Matrix3 matrix3, float[] mapedPoint)
	{
		mapedPoint[0] = x * matrix3.val[0] + y * matrix3.val[3] + matrix3.val[6];
		mapedPoint[1] = x * matrix3.val[1] + y * matrix3.val[4] + matrix3.val[7];
	}
}
