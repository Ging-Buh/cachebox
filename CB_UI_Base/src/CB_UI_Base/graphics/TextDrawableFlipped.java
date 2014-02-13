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

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextOnPath;

/**
 * @author Longri
 */
public class TextDrawableFlipped extends TextDrawable
{

	// extract base with non flipping

	TextOnPath flippedCache;
	private final AtomicBoolean isFlipped = new AtomicBoolean(false);
	private float pathFlipDirection;

	public TextDrawableFlipped(final String text, GL_Path path, float defaultWidth, float defaultHeight, final GL_Paint fill,
			final GL_Paint stroke, final boolean center)
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
	public void draw(SpriteBatch batch, float x, float y, float width, float height, float rotated)
	{
		if (isDisposed) return;

		if (!isFlipped.get() && pathDirection + rotated > 100) isFlipped.set(true);

		if (isFlipped.get() && pathDirection + rotated < 100) isFlipped.set(false);

		if (isFlipped.get())
		{
			if (flippedCache != null) flippedCache.draw(batch);
		}
		else
		{
			if (Cache != null) Cache.draw(batch);
		}

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
