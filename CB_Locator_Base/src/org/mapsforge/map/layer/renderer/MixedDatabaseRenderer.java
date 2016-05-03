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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mapsforge.core.graphics.Color;
import org.mapsforge.core.graphics.GraphicFactory;
import org.mapsforge.core.graphics.TileBitmap;
import org.mapsforge.core.mapelements.MapElementContainer;
import org.mapsforge.core.mapelements.SymbolContainer;
import org.mapsforge.core.mapelements.WayTextContainer;
import org.mapsforge.core.model.Point;
import org.mapsforge.core.model.Rectangle;
import org.mapsforge.map.datastore.MapDataStore;
import org.mapsforge.map.datastore.MapReadResult;
import org.mapsforge.map.layer.labels.TileBasedLabelStore;
import org.mapsforge.map.rendertheme.RenderContext;
import org.mapsforge.map.rendertheme.rule.RenderTheme;

import com.badlogic.gdx.graphics.Pixmap.Format;

import CB_Locator.Map.Descriptor;
import CB_Locator.Map.TileGL;
import CB_Locator.Map.TileGL.TileState;
import CB_Locator.Map.TileGL_Mixed;
import CB_UI_Base.graphics.GL_Matrix;
import CB_UI_Base.graphics.SymbolDrawable;
import CB_UI_Base.graphics.Images.MatrixDrawable;
import CB_UI_Base.graphics.Images.SortedRotateList;
import CB_UI_Base.graphics.extendedIntrefaces.ext_Bitmap;
import CB_Utils.Lists.CB_List;

/**
 * Mixed Database render for render MapTile with Mapsforge Tile as Bitmap without Symbols and Names.<br>
 * Symbols and Names are rendered in OpenGl for re rotating on runtime.
 * 
 * @author Longri
 */
public class MixedDatabaseRenderer extends MF_DatabaseRenderer implements IDatabaseRenderer {

	private static final Logger LOGGER = Logger.getLogger(MixedDatabaseRenderer.class.getName());

	private HashMap<String, CB_List<WayTextContainer>> NameList;

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

		this.bitmap = executeJob(rendererJob, rotateList);

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

	/**
	 * Called when a job needs to be executed.
	 * 
	 * @param rendererJob
	 *            the job that should be executed.
	 */
	private TileBitmap executeJob(RendererJob rendererJob, SortedRotateList rotateList) {

	RenderTheme renderTheme;
	try {
		renderTheme = rendererJob.renderThemeFuture.get();
	} catch (Exception e) {
		LOGGER.log(Level.SEVERE, "Error to retrieve render theme from future", e);
		return null;
	}

	RenderContext renderContext = null;
	try {
		renderContext = new RenderContext(renderTheme, rendererJob, new CanvasRasterer(graphicFactory));

		if (renderBitmap(renderContext)) {
		TileBitmap bitmap = null;

		if (this.mapDatabase != null) {
			MapReadResult mapReadResult = this.mapDatabase.readMapData(rendererJob.tile);
			processReadMapData(renderContext, mapReadResult);
		}

		if (!rendererJob.labelsOnly) {
			bitmap = this.graphicFactory.createTileBitmap(renderContext.rendererJob.tile.tileSize, renderContext.rendererJob.hasAlpha);
			bitmap.setTimestamp(rendererJob.mapDataStore.getDataTimestamp(renderContext.rendererJob.tile));
			renderContext.canvasRasterer.setCanvasBitmap(bitmap);
			if (!rendererJob.hasAlpha && rendererJob.displayModel.getBackgroundColor() != renderContext.renderTheme.getMapBackground()) {
			renderContext.canvasRasterer.fill(renderContext.renderTheme.getMapBackground());
			}
			renderContext.canvasRasterer.drawWays(renderContext);
		}

		// store Labels in Rotate List
		Set<MapElementContainer> labelsToDraw = processLabels(renderContext);
		drawMapElements(rendererJob, labelsToDraw, rotateList);

		if (!rendererJob.labelsOnly && renderContext.renderTheme.hasMapBackgroundOutside()) {
			// blank out all areas outside of map
			Rectangle insideArea = this.mapDatabase.boundingBox().getPositionRelativeToTile(renderContext.rendererJob.tile);
			if (!rendererJob.hasAlpha) {
			renderContext.canvasRasterer.fillOutsideAreas(renderContext.renderTheme.getMapBackgroundOutside(), insideArea);
			} else {
			renderContext.canvasRasterer.fillOutsideAreas(Color.TRANSPARENT, insideArea);
			}
		}
		return bitmap;
		}
		// outside of map area with background defined:
		return createBackgroundBitmap(renderContext);
	} finally {
		if (renderContext != null) {
		renderContext.destroy();
		}
	}
	}

	void drawMapElements(RendererJob rendererJob, Set<MapElementContainer> elements, SortedRotateList rotateList) {
	// we have a set of all map elements (needed so we do not draw elements twice),
	// but we need to draw in priority order as we now allow overlaps. So we
	// convert into list, then sort, then draw.
	List<MapElementContainer> elementsAsList = new ArrayList<MapElementContainer>(elements);
	// draw elements in order of priority: lower priority first, so more important
	// elements will be drawn on top (in case of display=true) items.
	Collections.sort(elementsAsList);

	for (MapElementContainer element : elementsAsList) {
		if (element instanceof SymbolContainer) {
		processSymbolContainer(rendererJob, (SymbolContainer) element, rotateList);
		}
	}
	}

	private void processSymbolContainer(RendererJob rendererJob, SymbolContainer symbolContainer, SortedRotateList rotateList) {

	float tileSize = rendererJob.tile.tileSize;

	Point tileOrigin = rendererJob.tile.getOrigin();

	float PointX = (float) (symbolContainer.xy.x - tileOrigin.x);
	float PointY = (float) (tileSize - (symbolContainer.xy.y - tileOrigin.y));

	ext_Bitmap bmp = (ext_Bitmap) symbolContainer.symbol;

	SymbolDrawable drw = new SymbolDrawable(bmp.getGlBmpHandle(), PointX, PointY, tileSize, tileSize, symbolContainer.alignCenter);
	MatrixDrawable maDr = new MatrixDrawable(drw, new GL_Matrix(), true);
	rotateList.add(maDr);
	}

}
