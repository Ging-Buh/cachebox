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
package org.mapsforge.map.layer.renderer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.mapsforge.core.graphics.GraphicFactory;
import org.mapsforge.core.graphics.TileBitmap;
import org.mapsforge.map.reader.MapDatabase;

import CB_Locator.Map.Descriptor;
import CB_Locator.Map.TileGL;
import CB_Locator.Map.TileGL.TileState;
import CB_Locator.Map.TileGL_Bmp;
import CB_UI_Base.graphics.extendedIntrefaces.ext_Bitmap;

/**
 * @author Longri
 */
public class MF_DatabaseRenderer extends DatabaseRenderer implements IDatabaseRenderer
{

	public MF_DatabaseRenderer(MapDatabase mapDatabase, GraphicFactory graphicFactory)
	{
		super(mapDatabase, graphicFactory);

	}

	@Override
	public TileGL execute(RendererJob rendererJob)
	{

		// Fixme direct Buffer swap
		/*
		 * If the goal is to convert an Android Bitmap to a libgdx Texture, you don't need to use Pixmap at all. You can do it directly with
		 * the help of simple OpenGL and Android GLUtils. Try the followings; it is 100x faster than your solution. I assume that you are
		 * not in the rendering thread (you should not most likely). If you are, you don't need to call postRunnable().
		 * 
		 * Gdx.app.postRunnable(new Runnable() {
		 * 
		 * @Override public void run() { Texture tex = new Texture(bitmap.getWidth(), bitmap.getHeight(), Format.RGBA8888);
		 * GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, tex.getTextureObjectHandle()); GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
		 * GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0); bitmap.recycle(); // now you have the texture to do whatever you want } });
		 */

		TileBitmap bmp = executeJob(rendererJob);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try
		{
			bmp.compress(baos);
			byte[] b = baos.toByteArray();

			Descriptor desc = new Descriptor((int) rendererJob.tile.tileX, (int) rendererJob.tile.tileY, rendererJob.tile.zoomLevel, false);

			TileGL_Bmp bmpTile = new TileGL_Bmp(desc, b, TileState.Present);

			((ext_Bitmap) bmp).recycle();

			return bmpTile;
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
