/* 
 * Copyright (C) 2011-2012 team-cachebox.de
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

package CB_Core.GL_UI.Controls;

import CB_Core.GL_UI.CB_View_Base;
import CB_Core.Log.Logger;
import CB_Core.Math.CB_RectF;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

/**
 * Ein Control, welches ein Bild aus einem Pfad Darstellt.
 * 
 * @author Longri
 */
public class Image extends CB_View_Base
{

	private float mRotate = 0;
	private Color mColor = new Color(1, 1, 1, 1);

	public Image(float X, float Y, float Width, float Height, String Name)
	{
		super(X, Y, Width, Height, Name);
	}

	public Image(CB_RectF rec, String Name)
	{
		super(rec, Name);
	}

	@Override
	protected void render(SpriteBatch batch)
	{
		Color altColor = batch.getColor().cpy();

		batch.setColor(mColor);

		// set rotation
		boolean isRotated = false;

		if (mRotate != 0 || mScale != 1)
		{
			isRotated = true;

			Matrix4 matrix = new Matrix4();

			matrix.idt();
			matrix.translate(this.getX() + mOriginX, this.getY() + mOriginY, 0);
			matrix.rotate(0, 0, 1, mRotate);
			matrix.scale(mScale, mScale, 1);
			matrix.translate(-mOriginX, -mOriginY, 0);

			batch.setTransformMatrix(matrix);
		}

		if (mDrawable != null)
		{
			mDrawable.draw(batch, 0, 0, width, height);

		}
		else if (mPath != null && !mPath.equals(""))
		{ // das laden darf erst hier passieren, damit es aus dem GL_Thread herraus läuft.
			try
			{

				// Logger.LogCat("Load GL Image Texture Path= " + mPath);

				mImageTex = new Texture(Gdx.files.internal(mPath));
				mDrawable = new SpriteDrawable(new com.badlogic.gdx.graphics.g2d.Sprite(mImageTex));

				mDrawable.draw(batch, 0, 0, width, height);

			}
			catch (Exception e)
			{
				Logger.LogCat("E Load GL Image" + e.getMessage());
				e.printStackTrace();
			}
		}

		batch.setColor(altColor);
		if (isRotated)
		{
			Matrix4 matrix = new Matrix4();

			matrix.idt();
			// matrix.translate(mOriginX, mOriginY, 0);
			matrix.rotate(0, 0, 1, 0);
			matrix.scale(1, 1, 1);
			// matrix.translate(-mOriginX, -mOriginY, 0);

			batch.setTransformMatrix(matrix);

		}

	}

	private String mPath;
	private Texture mImageTex = null;
	Drawable mDrawable = null;

	public void setImage(String Path)
	{

		mPath = Path;
		if (mDrawable != null)
		{
			dispose();
			// das laden des Images in das Sprite darf erst in der Render Methode passieren, damit es aus dem GL_Thread herraus läuft.
		}

	}

	public void setDrawable(Drawable drawable)
	{
		mDrawable = drawable;
	}

	public void dispose()
	{
		if (mImageTex != null) mImageTex.dispose();
		mImageTex = null;

		mDrawable = null;
	}

	public void setRotate(float Rotate)
	{
		mRotate = Rotate;
	}

	private float mOriginX;
	private float mOriginY;
	private float mScale = 1f;

	public void setOrigin(float originX, float originY)
	{
		mOriginX = originX;
		mOriginY = originY;
	}

	/**
	 * setzt den Scale Factor des dargestellten Images, wobei die Größe nicht verändert wird. Ist das Image größer, wird es abgeschnitten
	 * 
	 * @param value
	 */
	public void setScale(float value)
	{
		mScale = value;
	}

	@Override
	protected void Initial()
	{
	}

	@Override
	protected void SkinIsChanged()
	{

	}

	public void setColor(Color color)
	{
		mColor = color;
	}

}
