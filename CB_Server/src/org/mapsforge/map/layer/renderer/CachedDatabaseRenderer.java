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

import CB_Locator.LocatorSettings;
import CB_Locator.Map.Descriptor;
import CB_UI_Base.graphics.GL_RenderType;
import org.mapsforge.core.graphics.GraphicFactory;
import org.mapsforge.core.graphics.TileBitmap;
import org.mapsforge.map.awt.graphics.AwtGraphicFactory;
import org.mapsforge.map.awt.graphics.AwtTileBitmap;
import org.mapsforge.map.datastore.MultiMapDataStore;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;

/**
 * Extends the Mapsforge DatabaseRenderer with a Disk Cached MapTile Loader
 * 
 * 
 * @author Longri
 *
 */
public class CachedDatabaseRenderer extends DatabaseRenderer {

	public CachedDatabaseRenderer(MultiMapDataStore mapDatabase, GraphicFactory graphicFactory) {
		super(mapDatabase, graphicFactory);
		LocatorSettings.MapsforgeRenderType.setEnumValue(GL_RenderType.Mapsforge);
		//		this.CachePath = CachePath;
	}

	@Override
	public TileBitmap executeJob(RendererJob rendererJob) {
		TileBitmap bmp = null;

		File cacheFile = getCacheFile(rendererJob);

		// Try to Load Cached Tile
		bmp = getCacheTile(cacheFile);

		if (bmp == null) {
			bmp = super.executeJob(rendererJob);

			// save MapTile to Cache
			saveMapTile(cacheFile, bmp);
		}

		return bmp;
	}

	private void saveMapTile(File cacheFile, TileBitmap tileBmp) {
		AwtTileBitmap bmp = (AwtTileBitmap) tileBmp;
		BufferedImage bufferedImage = bmp.getBufferedImage();

		//create folder
		cacheFile.mkdirs();

		try {
			ImageIO.write(bufferedImage, "PNG", cacheFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private File getCacheFile(RendererJob rendererJob) {

		Descriptor desc = new Descriptor((int) rendererJob.tile.tileX, (int) rendererJob.tile.tileY, rendererJob.tile.zoomLevel, false);
		String filePath = desc.getLocalCachePath("Mapsforge") + ".png";
		return new File(filePath);
	}

	private TileBitmap getCacheTile(File cacheFile) {
		if (!cacheFile.exists())
			return null;

		InputStream is;
		TileBitmap bmp = null;
		try {
			is = new FileInputStream(cacheFile);
			bmp = AwtGraphicFactory.INSTANCE.createTileBitmap(is, 256, false);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		return bmp;
	}

}
