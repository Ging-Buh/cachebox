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
import CB_UI_Base.graphics.extendedIntrefaces.ext_Paint;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextOnPath;
import com.badlogic.gdx.math.Matrix3;
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

	public TextDrawable(final String text, GL_Path path, float defaultWidth, float defaultHeight, final ext_Paint fill,
			final ext_Paint stroke, final boolean center)
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
			// PROJECTION_CHK
			// Matrix4 ori = batch.getProjectionMatrix().cpy();

			float scaleWidth = width / DEFAULT_WIDTH;
			float scaleHeight = height / DEFAULT_HEIGHT;

			// if (scaleWidth != 1 || scaleHeight != 1)
			// {
			// Matrix4 transform = batch.getProjectionMatrix().cpy();
			// transform.scale(scaleWidth, scaleHeight, 1);
			// batch.setProjectionMatrix(transform);
			// }

			Matrix3 transform2 = new Matrix3().scale(scaleWidth, scaleHeight);

			if (rotated != 0)
			{

				float[] center = Cache.getCenterPoint();

				// Matrix4 Orim = batch.getProjectionMatrix().cpy();
				// Matrix4 m = batch.getProjectionMatrix().cpy();

				// m.translate(center[0], center[1], 0);
				// m.rotate(0, 0, 1, rotated);
				// m.translate(-center[0], -center[1], 0);

				// batch.setProjectionMatrix(m);
				// Cache.draw(batch, transform2);
				// batch.setProjectionMatrix(Orim);

				// Matrix3 m3 = new Matrix3();
				// m3.set(batch.getProjectionMatrix());
				//
				// m3.translate(center[0], center[1]);
				// m3.rotate(rotated);
				// m3.translate(-center[0], -center[1]);

				// transform2.mul(m3);
				// m3.mul(transform2);

				transform2.translate(x, y);
				transform2.translate(center[0], center[1]);
				transform2.rotate(rotated);
				transform2.translate(-center[0], -center[1]);
				Cache.draw(batch, transform2);
			}
			else
			{
				transform2.translate(x, y);
				Cache.draw(batch, transform2);
			}
			// batch.setProjectionMatrix(ori);
		}
	}
}