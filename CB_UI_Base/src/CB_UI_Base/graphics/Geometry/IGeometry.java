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

import com.badlogic.gdx.utils.Disposable;

/**
 * @author Longri
 */
public interface IGeometry extends Disposable
{
	final static float MIN_CIRCLE_SEGMENTH_LENGTH = 10;
	final static int MIN_CIRCLE_SEGMENTH_COUNT = 18;

	/**
	 * get the points as float[] <br>
	 * one point stored as two float, first is x and last is y
	 * 
	 * @return
	 */
	public float[] getVertices();

	/**
	 * get the triangle indices of this geometry <br>
	 * or NULL if this geometry has no triangles (like single line)
	 * 
	 * @return
	 */
	public short[] getTriangles();

}
