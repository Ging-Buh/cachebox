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

import CB_UI_Base.GL_UI.IRunOnGL;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.graphics.extendedIntrefaces.ext_Paint;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextOnPath;
import com.badlogic.gdx.math.Matrix3;

/**
 * @author Longri
 */
public class TextDrawableFlipped extends TextDrawable
{

	// extract base with non flipping

	TextOnPath flippedCache;
	private final AtomicBoolean isFlipped = new AtomicBoolean(false);
	private float pathFlipDirection;

	public TextDrawableFlipped(final String text, GL_Path path, float defaultWidth, float defaultHeight, final ext_Paint fill,
			final ext_Paint stroke, final boolean center)
	{
		super(text, path, defaultWidth, defaultHeight, fill, stroke, center);

		final GL_Path workPath = new GL_Path(path);

		GL.that.RunOnGL(new IRunOnGL()
		{

			@Override
			public void run()
			{
				workPath.revert();
				flippedCache = new TextOnPath(text, workPath, fill, stroke, center);
				pathFlipDirection = workPath.getAverageDirection();
				workPath.dispose();
			}
		});

	}

	@Override
	public void draw(Batch batch, float x, float y, float width, float height, float rotated)
	{
		if (isDisposed) return;

		if (!isFlipped.get() && pathDirection + rotated > 100) isFlipped.set(true);

		if (isFlipped.get() && pathDirection + rotated < 100) isFlipped.set(false);

		float scaleWidth = width / DEFAULT_WIDTH;
		float scaleHeight = height / DEFAULT_HEIGHT;

		// PROJECTION_CHK

		// Matrix4 ori = batch.getProjectionMatrix().cpy();
		// if (scaleWidth != 1 || scaleHeight != 1)
		// {
		// Matrix4 transform = batch.getProjectionMatrix().cpy();
		// transform.scale(scaleWidth, scaleHeight, 1);
		// batch.setProjectionMatrix(transform);
		// }

		Matrix3 transform2 = new Matrix3();
		transform2.translate(x, y);

		transform2.scale(scaleWidth, scaleHeight);

		if (isFlipped.get())
		{
			if (flippedCache != null) flippedCache.draw(batch, transform2);
		}
		else
		{
			if (Cache != null) Cache.draw(batch, transform2);
		}
		// batch.setProjectionMatrix(ori);
	}

	@Override
	public void dispose()
	{
		if (flippedCache != null) flippedCache.dispose();
		flippedCache = null;
		super.dispose();
	}

	public void flip(boolean value)
	{
		isFlipped.set(value);
	}

}
