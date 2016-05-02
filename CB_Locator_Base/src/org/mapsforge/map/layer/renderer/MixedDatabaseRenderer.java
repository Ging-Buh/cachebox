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

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.mapsforge.core.graphics.GraphicFactory;
import org.mapsforge.core.graphics.TileBitmap;
import org.mapsforge.map.datastore.MapDataStore;
import org.mapsforge.map.layer.labels.TileBasedLabelStore;

import com.badlogic.gdx.graphics.Pixmap.Format;

import CB_Locator.Map.Descriptor;
import CB_Locator.Map.TileGL;
import CB_Locator.Map.TileGL.TileState;
import CB_Locator.Map.TileGL_Mixed;
import CB_UI_Base.graphics.Images.SortedRotateList;
import CB_Utils.Lists.CB_List;

/**
 * Mixed Database render for render MapTile with Mapsforge Tile as Bitmap without Symbols and Names.<br>
 * Symbols and Names are rendered in OpenGl for re rotating on runtime.
 * 
 * @author Longri
 */
public class MixedDatabaseRenderer extends DatabaseRenderer implements IDatabaseRenderer {

	private HashMap<String, CB_List<GL_WayTextContainer>> NameList;

	public MixedDatabaseRenderer(MapDataStore mapDatabase, GraphicFactory graphicFactory, TileBasedLabelStore labelStore) {
		super(mapDatabase, graphicFactory, labelStore);
	}

	AtomicBoolean inWork = new AtomicBoolean(false);
	// UnsaveByteArrayOutputStream baos = new UnsaveByteArrayOutputStream(256 * 256 * 2);
	UnsaveByteArrayOutputStream baos = new UnsaveByteArrayOutputStream();
	private TileBitmap bitmap;

	@Override
	public TileGL execute(RendererJob rendererJob) {

		if (inWork.get()) {
			// CB_Utils.Log.Log.debug(log, "MixedDatabaseRenderer in Work [" + ThreadId + "]");
			return null;
		}
		inWork.set(true);
		try {
			SortedRotateList rotateList = new SortedRotateList();

			this.bitmap = executeJob(rendererJob);

			try {

				this.bitmap.compress(baos);
				byte[] b = baos.toByteArray();

				Descriptor desc = new Descriptor(rendererJob.tile.tileX, rendererJob.tile.tileY, rendererJob.tile.zoomLevel, false);

				TileGL_Mixed mixedTile = new TileGL_Mixed(desc, b, TileState.Present, Format.RGB565);
				mixedTile.add(rotateList);
				baos.clear();
				b = null;
				inWork.set(false);
				return mixedTile;
			} catch (IOException e) {
				e.printStackTrace();
			}

			inWork.set(false);
			return null;
		} finally {
			inWork.set(false);
		}
	}
}
