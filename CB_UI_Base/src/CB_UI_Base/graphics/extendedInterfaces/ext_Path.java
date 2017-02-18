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
package CB_UI_Base.graphics.extendedInterfaces;

import CB_UI_Base.graphics.fromAndroid.RectF;

/**
 * @author Longri
 */
public interface ext_Path extends org.mapsforge.core.graphics.Path {
	public enum FillType {
		WINDING, EVEN_ODD

	}

	@Override
	void lineTo(float x, float y);

	@Override
	void moveTo(float x, float y);

	/**
	 * Set the beginning of the next contour relative to the last point on the previous contour. If there is no previous contour, this is
	 * treated the same as moveTo().Parameters
	 * 
	 * @param x
	 *            The amount to add to the x-coordinate of the end of the previous contour, to specify the start of a new contour
	 * @param y
	 *            The amount to add to the y-coordinate of the end of the previous contour, to specify the start of a new contour
	 */
	void rMoveTo(float x, float y);

	void close();

	void rLineTo(float x, float y);

	void cubicTo(float x1, float y1, float x2, float y2, float x, float y);

	void addArc(RectF oval, float angleStart, float angleExtent);

	void transform(ext_Matrix currentMatrix, ext_Path transformedPath);

	void computeBounds(RectF pathBounds, boolean b);

	void quadTo(float x1, float y1, float x2, float y2);

	void addPath(ext_Path path, ext_Matrix combinedPathMatrix);

	void setFillType(FillType clipRuleFromState);

	FillType getFillType();

	void addPath(ext_Path spanPath);

	void transform(ext_Matrix transform);
}
