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

import CB_Core.GL_UI.GL_View_Base;
import CB_Core.Log.Logger;
import CB_Core.Math.CB_RectF;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Ein Control, welches ein Bild aus einem Pfad Darstellt.
 * 
 * @author Longri
 */
public class Image extends GL_View_Base
{

	public Image(float X, float Y, float Width, float Height)
	{
		super(X, Y, Width, Height);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void render(SpriteBatch batch)
	{
		if (mImageTex != null)
		{
			batch.draw(mImageTex, 0, 0, width, height);
		}
		else
		{
			// Versuche Image noch einmal zu laden max 3 Versuche
			if (mLoadCounter < 3)
			{
				setImage(mPath);
			}
		}

	}

	@Override
	public void onRezised(CB_RectF rec)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onLongClick(int x, int y, int pointer, int button)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onTouchDown(int x, int y, int pointer, int button)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onTouchDragged(int x, int y, int pointer)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onTouchUp(int x, int y, int pointer, int button)
	{
		// TODO Auto-generated method stub
		return false;
	}

	private String mPath;
	private Texture mImageTex = null;
	private int mLoadCounter = 0;

	public void setImage(String Path)
	{
		if (Path.equals(mPath))
		{
			++mLoadCounter;
		}
		else
		{
			mLoadCounter = 0;
		}
		mPath = Path;
		try
		{

			FileHandle marioFileHandle = Gdx.files.internal(mPath);
			mImageTex = new Texture(marioFileHandle);
		}
		catch (Exception e)
		{
			Logger.LogCat("E Load GL Image" + e.getMessage());
			e.printStackTrace();
		}
	}

	public void dispose()
	{
		mImageTex.dispose();
		mImageTex = null;
	}

}
