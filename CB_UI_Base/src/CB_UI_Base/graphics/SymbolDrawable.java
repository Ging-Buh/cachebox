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

import CB_UI_Base.graphics.Images.BitmapDrawable;
import CB_UI_Base.graphics.Images.IRotateDrawable;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;

/**
 * @author Longri
 */
public class SymbolDrawable implements IRotateDrawable
{

	private BitmapDrawable BITMAP;
	private final float DEFAULT_WIDTH;
	private final float DEFAULT_HEIGHT;
	private final boolean alignCenter;
	private final float pointX;
	private final float pointY;
	private Matrix4 lastMatrix = null;
	private float lastRotate;
	private final float postRotate;

	private final AtomicBoolean isDisposed = new AtomicBoolean(false);

	/**
	 * Constructor for symbols are rotated back on drawing
	 * 
	 * @param bmp
	 * @param pointX
	 * @param pointY
	 * @param defaultWidth
	 * @param defaultHeight
	 * @param scale
	 * @param alignCenter
	 */
	public SymbolDrawable(BitmapDrawable bmp, float pointX, float pointY, float defaultWidth, float defaultHeight, boolean alignCenter)
	{
		BITMAP = bmp;
		DEFAULT_WIDTH = defaultWidth;
		DEFAULT_HEIGHT = defaultHeight;
		this.alignCenter = alignCenter;
		this.pointX = pointX;
		this.pointY = pointY;
		this.postRotate = 0;
	}

	public SymbolDrawable(BitmapDrawable bmp, float pointX, float pointY, int defaultWidth, int defaultHeight, boolean alignCenter,
			float theta)
	{
		BITMAP = bmp;
		DEFAULT_WIDTH = defaultWidth;
		DEFAULT_HEIGHT = defaultHeight;
		this.alignCenter = alignCenter;
		this.pointX = pointX;
		this.pointY = pointY;
		this.postRotate = theta;
	}

	public void resetMatrixForFboDrawing()
	{
		lastMatrix = null;
	}

	@Override
	public void draw(SpriteBatch batch, float x, float y, float width, float height, float rotate)
	{
		if (isDisposed.get()) return;
		if (BITMAP == null) return;

		if (BITMAP.getTexture() == null) return;
		Texture tex = BITMAP.getTexture();

		float scaleWidth = width / DEFAULT_WIDTH;
		float scaleHeight = height / DEFAULT_HEIGHT;

		boolean scaled = scaleWidth != 1 || scaleHeight != 1;

		float offsetX = 0;// (tex.getWidth() * scaleWidth) / 2;
		float offsetY = tex.getHeight();

		TextureRegion reg = new TextureRegion(tex);
		Matrix4 Orim = batch.getProjectionMatrix();

		rotate += this.postRotate;
		rotate *= MathUtils.degreesToRadians;

		if (lastMatrix == null || lastRotate != rotate)
		{
			lastRotate = rotate;
			Matrix4 m = batch.getProjectionMatrix().cpy();

			GL_Matrix matrix = new GL_Matrix();

			if (alignCenter)
			{
				float pivotX = (BITMAP.getWidth()) / 2;
				float pivotY = (BITMAP.getHeight()) / 2;

				matrix.translate(-pivotX, pivotY);
				matrix.translate(-offsetX, -offsetY);
				if (scaled) matrix.scale(1 / scaleWidth, 1 / scaleHeight);
				matrix.rotate(rotate);
				if (scaled) matrix.scale(scaleWidth, scaleHeight);
				matrix.translate(pointX + pivotX, pointY - pivotY);
				matrix.translate(-pivotX, -pivotY);
			}
			else
			{
				float pivotX = (BITMAP.getWidth()) / 2;
				float pivotY = (BITMAP.getHeight()) / 2;
				matrix.translate(-pivotX, pivotY);
				matrix.translate(-offsetX, -offsetY);
				if (scaled) matrix.scale(1 / scaleWidth, 1 / scaleHeight);
				matrix.rotate(rotate);
				if (scaled) matrix.scale(scaleWidth, scaleHeight);
				matrix.translate(pointX + pivotX, pointY - pivotY);

			}

			if (scaled) m.scale(scaleWidth, scaleHeight, 1);
			m.mul(matrix.getMatrix4());

			lastMatrix = m;
		}

		batch.setProjectionMatrix(lastMatrix);
		// batch.disableBlending();
		batch.draw(reg, 0, 0);
		// batch.enableBlending();
		batch.setProjectionMatrix(Orim);

	}

	public boolean isDisposed()
	{
		return isDisposed.get();
	}

	@Override
	public void dispose()
	{
		synchronized (isDisposed)
		{
			if (isDisposed.get()) return;
			if (BITMAP != null) BITMAP.dispose();
			BITMAP = null;
			lastMatrix = null;
			isDisposed.set(true);
		}
	}

}
