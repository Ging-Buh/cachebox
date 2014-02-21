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
