/* 
 * Copyright (C) 2014-2016 team-cachebox.de
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mapsforge.core.graphics.Color;
import org.mapsforge.core.graphics.Filter;
import org.mapsforge.core.graphics.GraphicFactory;
import org.mapsforge.core.graphics.TileBitmap;
import org.mapsforge.core.mapelements.MapElementContainer;
import org.mapsforge.core.mapelements.PointTextContainer;
import org.mapsforge.core.mapelements.SymbolContainer;
import org.mapsforge.core.mapelements.WayTextContainer;
import org.mapsforge.core.model.Point;
import org.mapsforge.core.model.Rectangle;
import org.mapsforge.map.datastore.MapDataStore;
import org.mapsforge.map.datastore.MapReadResult;
import org.mapsforge.map.layer.cache.TileCache;
import org.mapsforge.map.layer.labels.TileBasedLabelStore;
import org.mapsforge.map.rendertheme.RenderContext;
import org.mapsforge.map.rendertheme.rule.RenderTheme;

import com.badlogic.gdx.graphics.Pixmap.Format;

import CB_Locator.Map.Descriptor;
import CB_Locator.Map.TileGL;
import CB_Locator.Map.TileGL.TileState;
import CB_Locator.Map.TileGL_Mixed;
import CB_UI_Base.graphics.GL_Matrix;
import CB_UI_Base.graphics.GL_Paint;
import CB_UI_Base.graphics.GL_Path;
import CB_UI_Base.graphics.SymbolDrawable;
import CB_UI_Base.graphics.TextDrawable;
import CB_UI_Base.graphics.TextDrawableFlipped;
import CB_UI_Base.graphics.Images.MatrixDrawable;
import CB_UI_Base.graphics.Images.SortedRotateList;
import CB_UI_Base.graphics.extendedInterfaces.ext_Bitmap;
import CB_Utils.Lists.CB_List;

/**
 * Mixed Database render for render MapTile with Mapsforge Tile as Bitmap without Symbols and Names.<br>
 * Symbols and Names are rendered in OpenGl for re rotating on runtime.
 * 
 * @author Longri
 */
public class MixedDatabaseRenderer extends MF_DatabaseRenderer implements IDatabaseRenderer {

	private static final Logger LOGGER = Logger.getLogger(MixedDatabaseRenderer.class.getName());

	public MixedDatabaseRenderer(MapDataStore mapDataStore, GraphicFactory graphicFactory, TileCache tileCache, TileBasedLabelStore labelStore, boolean renderLabels, boolean cacheLabels) {
		super(mapDataStore, graphicFactory, firstLevelTileCache, labelStore, renderLabels, cacheLabels);
		this.mapDatabase = mapDataStore;
	}

	AtomicBoolean inWork = new AtomicBoolean(false);
	// UnsaveByteArrayOutputStream baos = new UnsaveByteArrayOutputStream(256 * 256 * 2);
	UnsaveByteArrayOutputStream baos = new UnsaveByteArrayOutputStream();
	private TileBitmap bitmap;
	private final MapDataStore mapDatabase;

	@Override
	public TileGL execute(RendererJob rendererJob) {

		if (inWork.get()) {
			// CB_Utils.Log.Log.debug(log, "MixedDatabaseRenderer in Work [" + ThreadId + "]");
			return null;
		}
		inWork.set(true);
		try {
			SortedRotateList rotateList = new SortedRotateList();
			try {
				this.bitmap = executeJob(rendererJob, rotateList);

				this.bitmap.compress(baos);
				byte[] b = baos.toByteArray();

				Descriptor desc = new Descriptor(rendererJob.tile.tileX, rendererJob.tile.tileY, rendererJob.tile.zoomLevel, false);

				TileGL_Mixed mixedTile = new TileGL_Mixed(desc, b, TileState.Present, Format.RGB565);
				mixedTile.add(rotateList);
				baos.clear();
				b = null;
				inWork.set(false);
				return mixedTile;
			} catch (Exception e) {
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
	 * @throws ExecutionException 
	 * @throws InterruptedException 
	 */
	private TileBitmap executeJob(RendererJob rendererJob, SortedRotateList rotateList) throws InterruptedException, ExecutionException {

		RenderTheme renderTheme;
		try {
			renderTheme = rendererJob.renderThemeFuture.get();
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error to retrieve render theme from future", e);
			return null;
		}

		RenderContext renderContext = null;
		try {
			renderContext = new RenderContext(rendererJob, new CanvasRasterer(graphicFactory));
			List<GL_WayTextContainer> wayNames = new ArrayList<GL_WayTextContainer>();
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
				drawMapElements(renderContext, rendererJob, labelsToDraw, rotateList, wayNames);

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

	void drawMapElements(RenderContext renderContext, RendererJob rendererJob, Set<MapElementContainer> elements, SortedRotateList rotateList, List<GL_WayTextContainer> wayNames) {
		// we have a set of all map elements (needed so we do not draw elements twice),
		// but we need to draw in priority order as we now allow overlaps. So we
		// convert into list, then sort, then draw.
		List<MapElementContainer> elementsAsList = new ArrayList<MapElementContainer>(elements);
		// draw elements in order of priority: lower priority first, so more important
		// elements will be drawn on top (in case of display=true) items.
		Collections.sort(elementsAsList);

		for (MapElementContainer element : elementsAsList) {
			if (element instanceof SymbolContainer) {
				processSymbolContainer(renderContext, rendererJob, (SymbolContainer) element, rotateList);
			} else if (element instanceof PointTextContainer) {
				processPointTextContainer(rendererJob, (PointTextContainer) element, rotateList);
			} else if (element instanceof WayTextContainer) {
				element.draw(renderContext.canvasRasterer.canvas, rendererJob.tile.getOrigin(), renderContext.canvasRasterer.symbolMatrix, Filter.NONE);
				//				processWayTextContainer(renderContext, rendererJob, (WayTextContainer) element, rotateList, wayNames);
			} else {
				throw new RuntimeException("Unknown ElementType. " + element.getClass().getName());
			}
		}

		//Finally store flipped way names to rotate list
		drawWayNames(rotateList, wayNames, rendererJob);
	}

	//	private void processWayTextContainer(RenderContext renderContext, RendererJob rendererJob, WayTextContainer element, SortedRotateList rotateList, List<GL_WayTextContainer> wayNames) {
	//		GL_WayDecorator.renderText(element.text, element.paintFront, element.paintBack, rendererJob.tile.getOrigin(), element.coordinates, wayNames, rendererJob.tile.tileSize);
	//	}

	public void drawWayNames(SortedRotateList rotateList, List<GL_WayTextContainer> wayNames2, RendererJob rendererJob) {

		HashMap<String, CB_List<GL_WayTextContainer>> NameList = new HashMap<String, CB_List<GL_WayTextContainer>>();

		// for (int index = wayNames2.size() - 1; index >= 0; --index)
		for (int index = 0; index < wayNames2.size(); ++index) {
			GL_WayTextContainer wayTextContainer = wayNames2.get(index);

			if (NameList.containsKey(wayTextContainer.text)) {
				NameList.get(wayTextContainer.text).add(wayTextContainer);
			} else {
				CB_List<GL_WayTextContainer> list = new CB_List<GL_WayTextContainer>();
				list.add(wayTextContainer);
				NameList.put(wayTextContainer.text, list);
			}
		}

		ArrayList<CB_List<GL_WayTextContainer>> values = new ArrayList<CB_List<GL_WayTextContainer>>(NameList.values());

		for (int index = values.size() - 1; index >= 0; --index) {
			CB_List<GL_WayTextContainer> sameName = values.get(index);

			// search the biggest
			GL_WayTextContainer biggestWayTextContainer = null;
			for (int i = 0, n = sameName.size(); i < n; i++) {
				GL_WayTextContainer wayTextContainer = sameName.get(i);
				if (biggestWayTextContainer == null) {
					biggestWayTextContainer = wayTextContainer;
				} else {
					if (biggestWayTextContainer.path.getLength() < wayTextContainer.path.getLength()) {
						biggestWayTextContainer = wayTextContainer;
					}
				}

				biggestWayTextContainer = wayTextContainer;

				float tileSize = rendererJob.tile.tileSize;
				biggestWayTextContainer.path.flipY(tileSize);
				GL_Paint fill = new GL_Paint(biggestWayTextContainer.fill);
				GL_Paint stroke = new GL_Paint(biggestWayTextContainer.stroke);
				TextDrawableFlipped textDrw = new TextDrawableFlipped(biggestWayTextContainer.text, biggestWayTextContainer.path, tileSize, tileSize, fill, stroke, true);
				MatrixDrawable maDr = new MatrixDrawable(textDrw, new GL_Matrix(), true);
				rotateList.add(maDr);
			}

		}
	}

	private void processPointTextContainer(RendererJob rendererJob, PointTextContainer pointTextContainer, SortedRotateList rotateList) {
		float TextWidth = (float) (pointTextContainer.boundary.getWidth());
		float tileSize = rendererJob.tile.tileSize;
		Point tileOrigin = rendererJob.tile.getOrigin();

		float PointX = (float) (pointTextContainer.xy.x - tileOrigin.x);
		float PointY = (float) (tileSize - (pointTextContainer.xy.y - tileOrigin.y));

		if (PointX < 0)
			return;
		if (PointX > tileSize)
			return;
		if (PointY < 0)
			return;
		if (PointY > tileSize)
			return;

		float halfWidth = TextWidth / 2;

		GL_Path path = new GL_Path();
		path.moveTo(PointX - halfWidth, PointY);
		path.lineTo(PointX + halfWidth, PointY);

		GL_Paint front = new GL_Paint(pointTextContainer.paintFront);
		GL_Paint back = new GL_Paint(pointTextContainer.paintBack);

		TextDrawable textDrw = new TextDrawable(pointTextContainer.text, path, tileSize, tileSize, front, back, true);
		MatrixDrawable maDr = new MatrixDrawable(textDrw, new GL_Matrix(), true);

		rotateList.add(maDr);
	}

	private void processSymbolContainer(RenderContext renderContext, RendererJob rendererJob, SymbolContainer symbolContainer, SortedRotateList rotateList) {

		if (symbolContainer.theta != 0) {
			// symbol has an own rotation, draw direct to Tile
			symbolContainer.draw(renderContext.canvasRasterer.canvas, rendererJob.tile.getOrigin(), renderContext.canvasRasterer.symbolMatrix, null);
			return;
		}

		float tileSize = rendererJob.tile.tileSize;
		Point tileOrigin = rendererJob.tile.getOrigin();

		float PointX = (float) (symbolContainer.xy.x - tileOrigin.x);
		float PointY = (float) (tileSize - (symbolContainer.xy.y - tileOrigin.y));

		ext_Bitmap bmp = (ext_Bitmap) symbolContainer.symbol;

		GL_Matrix matrix = new GL_Matrix();

		SymbolDrawable drw = new SymbolDrawable(bmp.getGlBmpHandle(), PointX, PointY, tileSize, tileSize, symbolContainer.alignCenter);
		MatrixDrawable maDr = new MatrixDrawable(drw, matrix, true);
		rotateList.add(maDr);
	}

}
