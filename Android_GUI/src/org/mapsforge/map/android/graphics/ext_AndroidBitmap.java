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
package org.mapsforge.map.android.graphics;

import org.mapsforge.core.graphics.TileBitmap;

import CB_UI_Base.graphics.Images.BitmapDrawable;
import CB_UI_Base.graphics.extendedIntrefaces.ext_Bitmap;
import android.graphics.Bitmap.Config;

import com.badlogic.gdx.graphics.Texture;

/**
 * Extends the original Mapsforge AwtBitmap with the ext_Bitmap interface.
 * 
 * @author Longri
 */
public class ext_AndroidBitmap extends AndroidBitmap implements ext_Bitmap, TileBitmap
{
	int instCount = 0;

	protected final BitmapDrawable GL_image;

	ext_AndroidBitmap(int width, int height)
	{
		super(width, height, Config.RGB_565);
		GL_image = null;
		instCount++;
	}

	protected ext_AndroidBitmap()
	{
		super(1, 1, Config.ALPHA_8);
		this.GL_image = null;
		this.bitmap = null;
	}

	@Override
	public void recycle()
	{
		instCount++;
		this.destroyBitmap();
	}

	@Override
	public void getPixels(int[] maskBuf, int i, int w, int j, int y, int w2, int k)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void setPixels(int[] maskedContentBuf, int i, int w, int j, int y, int w2, int k)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public BitmapDrawable getGlBmpHandle()
	{
		return GL_image;
	}

	@Override
	public Texture getTexture()
	{
		if (GL_image == null) return null;
		return GL_image.getTexture();
	}

}
