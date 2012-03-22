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
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Ein Control, welches ein Bild aus einem Pfad Darstellt.
 * 
 * @author Longri
 */
public class Image extends CB_View_Base
{

	private float mRotate = 0;

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
		if (mImageSprite != null)
		{
			mImageSprite.setBounds(0, 0, width, height);
			mImageSprite.setRotation(mRotate);
			mImageSprite.draw(batch);

		}
		else if (mPath != null && !mPath.equals(""))
		{ // das laden darf erst hier passieren, damit es aus dem GL_Thread herraus läuft.
			try
			{

				Logger.LogCat("Load GL Image Texture Path= " + mPath);

				mImageTex = new Texture(Gdx.files.internal(mPath));
				mImageSprite = new com.badlogic.gdx.graphics.g2d.Sprite(mImageTex);

				mImageSprite.setBounds(0, 0, width, height);
				mImageSprite.setRotation(mRotate);
				mImageSprite.draw(batch);

			}
			catch (Exception e)
			{
				Logger.LogCat("E Load GL Image" + e.getMessage());
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onRezised(CB_RectF rec)
	{

		if (mImageSprite != null) mImageSprite.setBounds(0, 0, width, height);

	}

	private String mPath;
	private Texture mImageTex = null;
	Sprite mImageSprite = null;
	private int mLoadCounter = 0;

	public void setImage(String Path)
	{

		mPath = Path;
		if (mImageSprite != null)
		{
			dispose();
			// das laden des Images in das Sprite darf erst in der Render Methode passieren, damit es aus dem GL_Thread herraus läuft.
		}

	}

	public void setSprite(Sprite sprite)
	{
		mImageSprite = new Sprite(sprite);
		mImageSprite.setSize(width, height);
		mImageSprite.setBounds(0, 0, this.width, this.height);
	}

	public void dispose()
	{
		mImageTex.dispose();
		mImageTex = null;

		mImageSprite = null;
	}

	public void setRotate(float Rotate)
	{
		mRotate = Rotate;
	}

	public void setOrigin(float originX, float originY)
	{
		mImageSprite.setOrigin(originX, originY);
	}

	/**
	 * setzt den Scale Factor des dargestellten Images, wobei die Größe nicht verändert wird. Ist das Image größer, wird es abgeschnitten
	 * 
	 * @param value
	 */
	public void setScale(float value)
	{
		mImageSprite.setScale(value);
	}

	@Override
	protected void Initial()
	{
		// TODO Auto-generated method stub

	}

}
