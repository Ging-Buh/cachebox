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
package CB_UI_Base.graphics.Images;

import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.graphics.GL_Matrix;
import CB_UI_Base.graphics.SymbolDrawable;
import CB_UI_Base.graphics.extendedIntrefaces.ext_Matrix;
import CB_Utils.Lists.CB_List;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;

/**
 * Holds a list of Drawables with rectangle interpret the drawing rectangle of a TileGL_Vector.
 * 
 * @author Longri
 */
public class TileGL_RotateDrawables
{
	private final Float[] REC;
	private final CB_List<MatrixDrawable> DRAWABLELIST;

	public TileGL_RotateDrawables(float x, float y, float width, float height, CB_List<MatrixDrawable> drawableList)
	{
		REC = new Float[4];
		REC[0] = x;
		REC[1] = y;
		REC[2] = width;
		REC[3] = height;
		DRAWABLELIST = drawableList;
	}

	public void draw(SpriteBatch batch, float rotated)
	{
		Matrix4 oriMatrix = GL.batch.getProjectionMatrix().cpy();

		Matrix4 thisDrawMatrix = oriMatrix.cpy();

		int count = 0;
		boolean MatrixChanged = false;

		for (MatrixDrawable drw : DRAWABLELIST)
		{

			if (drw.drawable instanceof SymbolDrawable) continue;

			if (count++ > 2500)
			{
				GL.batch.flush();
				count = 0;
			}
			Matrix4 matrix = thisDrawMatrix.cpy();
			ext_Matrix drwMatrix = new GL_Matrix(drw.matrix);
			matrix.mul(drwMatrix.getMatrix4().cpy());

			if (!transformEquals(matrix, oriMatrix))
			{
				GL.batch.setProjectionMatrix(matrix);
				MatrixChanged = true;
			}

			drw.drawable.draw(GL.batch, REC[0], REC[1], REC[2], REC[3], rotated);
		}
		if (MatrixChanged) GL.batch.setProjectionMatrix(oriMatrix);

		oriMatrix = null;

	}

	private boolean transformEquals(Matrix4 transform1, Matrix4 transform2)
	{

		for (int i = 0; i < 16; i++)
		{
			if (transform1.val[i] != transform2.val[i]) return false;
		}

		return true;
	}
}
