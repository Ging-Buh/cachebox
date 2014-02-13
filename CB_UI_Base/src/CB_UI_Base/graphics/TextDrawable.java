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

import java.security.InvalidParameterException;

import CB_UI_Base.GL_UI.IRunOnGL;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.graphics.Images.IRotateDrawable;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextOnPath;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Disposable;

/**
 * @author Longri
 */
public class TextDrawable implements IRotateDrawable, Disposable
{

	protected TextOnPath Cache;
	protected final float DEFAULT_WIDTH;
	protected final float DEFAULT_HEIGHT;
	protected boolean isDisposed = false;
	protected float pathDirection;
	protected final String debugText;

	public TextDrawable(final String text, GL_Path path, float defaultWidth, float defaultHeight, final GL_Paint fill,
			final GL_Paint stroke, final boolean center)
	{
		super();

		if (path == null || path.size < 4) throw new InvalidParameterException("not valid Path for TextDrawable");

		debugText = text;
		final GL_Path workPath = new GL_Path(path);

		this.DEFAULT_WIDTH = defaultWidth;
		this.DEFAULT_HEIGHT = defaultHeight;

		GL.that.RunOnGL(new IRunOnGL()
		{
			@Override
			public void run()
			{
				Cache = new TextOnPath(text, workPath, fill, stroke, center);
				pathDirection = workPath.getAverageDirection();
				GL.that.RunOnGL(new IRunOnGL()
				{
					@Override
					public void run()
					{
						workPath.dispose();
					}
				});
			}
		});

	}

	public boolean isDisposed()
	{
		return isDisposed;
	}

	@Override
	public void dispose()
	{
		if (isDisposed) return;
		if (Cache != null) Cache.dispose();
		Cache = null;

		isDisposed = true;
	}

	@Override
	public void draw(SpriteBatch batch, float x, float y, float width, float height, float rotated)
	{
		if (isDisposed) return;

		if (Cache != null)
		{
			if (rotated != 0)
			{

				Matrix4 Orim = batch.getProjectionMatrix().cpy();
				GL_Matrix matrix = new GL_Matrix();
				Matrix4 m = batch.getProjectionMatrix().cpy();

				float[] center = Cache.getCenterPoint();

				m.mul(matrix.getMatrix4());

				m.translate(center[0], center[1], 0);
				m.rotate(0, 0, 1, rotated);
				m.translate(-center[0], -center[1], 0);
				// m.translate(center[0], center[1]);
				// m.rotate(rotated);
				// matrix.translate(-center[0], -center[1]);

				batch.setProjectionMatrix(m);
				Cache.draw(batch);
				batch.setProjectionMatrix(Orim);
			}
			else
			{
				Cache.draw(batch);
			}
		}
	}
}