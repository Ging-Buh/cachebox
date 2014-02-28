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

import CB_UI_Base.GL_UI.IRunOnGL;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.graphics.Images.IRotateDrawable;
import CB_UI_Base.graphics.extendedIntrefaces.ext_Paint;

import com.badlogic.gdx.graphics.g2d.Batch;
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
	protected final Matrix4 transform = new Matrix4();
	protected final String debugText;

	public TextDrawable(final String text, GL_Path path, float defaultWidth, float defaultHeight, final ext_Paint fill,
			final ext_Paint stroke, final boolean center)
	{
		super();

		if (path == null || path.size < 4)
		{
			System.out.print("not valid Path for TextDrawable");
			debugText = null;
			DEFAULT_WIDTH = 0;
			DEFAULT_HEIGHT = 0;
			return;
			// throw new InvalidParameterException("not valid Path for TextDrawable");
		}

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
	public void draw(Batch batch, float x, float y, float width, float height, float rotated)
	{
		if (isDisposed) return;

		if (Cache != null)
		{

			float scaleWidth = width / DEFAULT_WIDTH;
			float scaleHeight = height / DEFAULT_HEIGHT;

			transform.setToTranslation(x, y, 0);

			if (rotated != 0)
			{

				float[] center = Cache.getCenterPoint();
				transform.scale(scaleWidth, scaleHeight, 1);
				transform.translate(center[0], center[1], 0);
				transform.rotate(0, 0, 1, rotated);
				transform.translate(-center[0], -center[1], 0);
			}
			else
			{
				transform.scale(scaleWidth, scaleHeight, 1);
			}

			Cache.draw(batch, transform);

		}
	}
}