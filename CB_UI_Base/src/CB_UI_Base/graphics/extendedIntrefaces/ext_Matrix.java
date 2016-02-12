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
package CB_UI_Base.graphics.extendedIntrefaces;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Disposable;

/**
 * @author Longri
 */
public interface ext_Matrix extends Disposable {

	/** Set the matrix to identity */
	public abstract void reset();

	public abstract void rotate(float theta);

	public abstract void rotate(float theta, float pivotX, float pivotY);

	public abstract void scale(float scaleX, float scaleY);

	public abstract void scale(float scaleX, float scaleY, float pivotX, float pivotY);

	public abstract void translate(float translateX, float translateY);

	public abstract void set(ext_Matrix matrix);

	public abstract void postConcat(ext_Matrix matrix);

	public abstract void preTranslate(float x, float y);

	public abstract void preScale(float x, float y);

	public abstract void preScale(float sx, float sy, float px, float py);

	public abstract void postRotate(float angle);

	public abstract void postScale(float rx, float ry);

	public abstract void postTranslate(float cx, float cy);

	public abstract void mapPoints(float[] src);

	/**
	 * Apply this matrix to the array of 2D points specified by src, and write the transformed points into the array of points specified by
	 * dst. The two arrays represent their "points" as pairs of floats [x, y].
	 * 
	 * @param dst
	 *            The array of dst points (x,y pairs)
	 * @param dstIndex
	 *            The index of the first [x,y] pair of dst floats
	 * @param src
	 *            The array of src points (x,y pairs)
	 * @param srcIndex
	 *            The index of the first [x,y] pair of src floats
	 * @param pointCount
	 *            The number of points (x,y pairs) to transform
	 */
	public abstract void mapPoints(float[] dst, int dstIndex, float[] src, int srcIndex, int pointCount);

	public abstract Matrix4 getMatrix4();

	public abstract void preRotate(float angle);

	public abstract void setValues(float[] fs);

	public abstract void preSkew(float f, float tan);

	public abstract void preRotate(Float angle, Float cx, Float cy);

	public abstract void getValues(float[] mValues);

	public abstract void preConcat(ext_Matrix matrix);

	public abstract boolean invert();

	@Override
	public abstract String toString();

	public abstract boolean isDefault();

}