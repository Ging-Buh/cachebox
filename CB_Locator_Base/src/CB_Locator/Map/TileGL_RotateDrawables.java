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
package CB_Locator.Map;

import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.graphics.Images.MatrixDrawable;
import CB_UI_Base.graphics.Images.SortedRotateList;
import CB_Utils.Lists.CB_List;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Matrix4;

/**
 * Holds a list of Drawables with rectangle interpret the drawing rectangle of a TileGL_Vector.
 * 
 * @author Longri
 */
public class TileGL_RotateDrawables
{
	private final Float[] REC;
	public final SortedRotateList DRAWABLELIST;
	public final TileGL tile;
	private final CB_List<MatrixDrawable> clearList = new CB_List<MatrixDrawable>();

	public TileGL_RotateDrawables(float x, float y, float width, float height, TileGL Tile, SortedRotateList drawableList)
	{
		REC = new Float[4];
		REC[0] = x;
		REC[1] = y;
		REC[2] = width;
		REC[3] = height;
		DRAWABLELIST = drawableList;
		tile = Tile;
	}

	private final Matrix4 oriMatrix = new Matrix4();
	private final Matrix4 thisDrawMatrix = new Matrix4();
	private final Matrix4 workMatrix = new Matrix4();

	public void draw(Batch batch, float rotated)
	{
		oriMatrix.set(GL.batch.getProjectionMatrix());

		thisDrawMatrix.set(oriMatrix);

		boolean MatrixChanged = false;

		for (MatrixDrawable drw : DRAWABLELIST)
		{

			boolean cantDraw = false;
			clearList.clear();
			if (drw.matrix == null)
			{
				cantDraw = drw.drawable.draw(GL.batch, REC[0], REC[1], REC[2], REC[3], rotated);
			}
			else
			{
				// Matrix4 matrix = thisDrawMatrix.cpy();
				// ext_Matrix drwMatrix = new GL_Matrix(drw.matrix);
				// matrix.mul(drwMatrix.getMatrix4().cpy());

				workMatrix.set(thisDrawMatrix);
				workMatrix.mul(drw.matrix.getMatrix4());

				if (!transformEquals(workMatrix, oriMatrix))
				{
					GL.batch.setProjectionMatrix(workMatrix);
					MatrixChanged = true;
				}

				cantDraw = drw.drawable.draw(GL.batch, REC[0], REC[1], REC[2], REC[3], rotated);
			}

			if (cantDraw)
			{
				// remove from Drawable List
				clearList.add(drw);
			}

		}

		if (!clearList.isEmpty())
		{
			DRAWABLELIST.remove(clearList);
		}

		if (MatrixChanged) GL.batch.setProjectionMatrix(oriMatrix);

	}

	private boolean transformEquals(Matrix4 transform1, Matrix4 transform2)
	{

		for (int i = 0; i < 16; i++)
		{
			if (transform1.val[i] != transform2.val[i]) return false;
		}

		return true;
	}

	public void set(float x, float y, float width, float height)
	{
		REC[0] = x;
		REC[1] = y;
		REC[2] = width;
		REC[3] = height;
	}
}
