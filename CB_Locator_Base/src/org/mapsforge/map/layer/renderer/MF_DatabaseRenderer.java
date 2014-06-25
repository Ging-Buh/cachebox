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
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;

import org.mapsforge.core.graphics.Bitmap;
import org.mapsforge.core.graphics.GraphicFactory;
import org.mapsforge.core.graphics.Paint;
import org.mapsforge.core.graphics.TileBitmap;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.Point;
import org.mapsforge.core.model.Tag;
import org.mapsforge.core.model.Tile;
import org.mapsforge.core.util.MercatorProjection;
import org.mapsforge.map.model.DisplayModel;
import org.mapsforge.map.reader.MapDatabase;
import org.mapsforge.map.reader.MapReadResult;
import org.mapsforge.map.reader.PointOfInterest;
import org.mapsforge.map.reader.Way;
import org.mapsforge.map.reader.header.MapFileInfo;
import org.mapsforge.map.rendertheme.RenderCallback;
import org.mapsforge.map.rendertheme.XmlRenderTheme;
import org.mapsforge.map.rendertheme.rule.CB_RenderTheme;
import org.mapsforge.map.rendertheme.rule.CB_RenderThemeHandler;
import org.xml.sax.SAXException;

import CB_Locator.Map.Descriptor;
import CB_Locator.Map.TileGL;
import CB_Locator.Map.TileGL.TileState;
import CB_Locator.Map.TileGL_Bmp;
import CB_UI_Base.graphics.extendedIntrefaces.ext_Bitmap;
import CB_Utils.Lists.F_List;

import com.badlogic.gdx.graphics.Pixmap.Format;

/**
 * @author Longri
 */
public class MF_DatabaseRenderer implements IDatabaseRenderer, RenderCallback
{

	private double tileLatLon_0_x, tileLatLon_0_y, tileLatLon_1_x, tileLatLon_1_y;
	private double divLon, divLat;

	private static final Byte DEFAULT_START_ZOOM_LEVEL = Byte.valueOf((byte) 12);
	private static final byte LAYERS = 11;
	private static final Logger LOGGER = Logger.getLogger(DatabaseRenderer.class.getName());
	private static final double STROKE_INCREASE = 1.5;
	private static final byte STROKE_MIN_ZOOM_LEVEL = 12;
	private static final Tag TAG_NATURAL_WATER = new Tag("natural", "water");
	private static final byte ZOOM_MAX = 22;

	private static Point[][] getTilePixelCoordinates(int tileSize)
	{
		Point point1 = new Point(0, 0);
		Point point2 = new Point(tileSize, 0);
		Point point3 = new Point(tileSize, tileSize);
		Point point4 = new Point(0, tileSize);
		return new Point[][]
			{
				{ point1, point2, point3, point4, point1 } };
	}

	private static byte getValidLayer(byte layer)
	{
		if (layer < 0)
		{
			return 0;
		}
		else if (layer >= LAYERS)
		{
			return LAYERS - 1;
		}
		else
		{
			return layer;
		}
	}

	private final List<PointTextContainer> areaLabels;
	private final CanvasRasterer canvasRasterer;
	private Point[][] coordinates;
	private RendererJob currentRendererJob;
	private List<List<ShapePaintContainer>> drawingLayers;
	private final GraphicFactory graphicFactory;

	private final LabelPlacement labelPlacement;
	private final MapDatabase mapDatabase;
	private List<PointTextContainer> nodes;
	private final List<SymbolContainer> pointSymbols;
	private Point poiPosition;
	private XmlRenderTheme previousJobTheme;
	private float previousTextScale;
	private byte previousZoomLevel;
	private CB_RenderTheme renderTheme;
	private ShapeContainer shapeContainer;
	private final List<WayTextContainer> wayNames;
	private final List<List<List<ShapePaintContainer>>> ways;
	private final List<SymbolContainer> waySymbols;

	/**
	 * Constructs a new DatabaseRenderer.
	 * 
	 * @param mapDatabase
	 *            the MapDatabase from which the map data will be read.
	 */
	public MF_DatabaseRenderer(MapDatabase mapDatabase, GraphicFactory graphicFactory)
	{
		this.mapDatabase = mapDatabase;
		this.graphicFactory = graphicFactory;

		this.canvasRasterer = new CanvasRasterer(graphicFactory);
		this.labelPlacement = new LabelPlacement();

		this.ways = new F_List<List<List<ShapePaintContainer>>>(LAYERS);
		this.wayNames = new F_List<WayTextContainer>(64);
		this.nodes = new F_List<PointTextContainer>(64);
		this.areaLabels = new F_List<PointTextContainer>(64);
		this.waySymbols = new F_List<SymbolContainer>(64);
		this.pointSymbols = new F_List<SymbolContainer>(64);
	}

	public void destroy()
	{
		this.canvasRasterer.destroy();
		// there is a chance that the renderer is being destroyed from the
		// DestroyThread before the rendertheme has been completely created
		// and assigned. If that happens bitmap memory held by the
		// RenderThemeHandler
		// will be leaked
		if (this.renderTheme != null)
		{
			this.renderTheme.destroy();
		}
		else
		{
			LOGGER.log(Level.SEVERE, "RENDERTHEME Could not destroy RenderTheme");
		}
	}

	/**
	 * Called when a job needs to be executed.
	 * 
	 * @param rendererJob
	 *            the job that should be executed.
	 */
	public TileBitmap executeJob(RendererJob rendererJob)
	{
		this.currentRendererJob = rendererJob;

		XmlRenderTheme jobTheme = rendererJob.xmlRenderTheme;
		if (!jobTheme.equals(this.previousJobTheme))
		{
			this.renderTheme = getRenderTheme(jobTheme, rendererJob.displayModel);
			if (this.renderTheme == null)
			{
				this.previousJobTheme = null;
				return null;
			}
			createWayLists();
			this.previousJobTheme = jobTheme;
			this.previousZoomLevel = Byte.MIN_VALUE;
		}

		byte zoomLevel = rendererJob.tile.zoomLevel;
		if (zoomLevel != this.previousZoomLevel)
		{
			setScaleStrokeWidth(zoomLevel);
			this.previousZoomLevel = zoomLevel;
		}

		float textScale = rendererJob.textScale;
		if (Float.compare(textScale, this.previousTextScale) != 0)
		{
			this.renderTheme.scaleTextSize(textScale);
			this.previousTextScale = textScale;
		}

		if (this.mapDatabase != null)
		{
			MapReadResult mapReadResult = this.mapDatabase.readMapData(rendererJob.tile);
			processReadMapData(mapReadResult);
		}

		this.nodes = this.labelPlacement.placeLabels(this.nodes, this.pointSymbols, this.areaLabels, rendererJob.tile,
				rendererJob.displayModel.getTileSize());

		TileBitmap bitmap = this.graphicFactory.createTileBitmap(rendererJob.displayModel.getTileSize(), rendererJob.hasAlpha);
		this.canvasRasterer.setCanvasBitmap(bitmap);
		if (rendererJob.displayModel.getBackgroundColor() != this.renderTheme.getMapBackground())
		{
			this.canvasRasterer.fill(this.renderTheme.getMapBackground());
		}
		this.canvasRasterer.drawWays(this.ways);
		this.canvasRasterer.drawSymbols(this.waySymbols);
		this.canvasRasterer.drawSymbols(this.pointSymbols);
		this.canvasRasterer.drawWayNames(this.wayNames);
		this.canvasRasterer.drawNodes(this.nodes);
		this.canvasRasterer.drawNodes(this.areaLabels);

		clearLists();
		return bitmap;
	}

	public MapDatabase getMapDatabase()
	{
		return this.mapDatabase;
	}

	/**
	 * @return the start point (may be null).
	 */
	public LatLong getStartPoint()
	{
		if (this.mapDatabase != null && this.mapDatabase.hasOpenFile())
		{
			MapFileInfo mapFileInfo = this.mapDatabase.getMapFileInfo();
			if (mapFileInfo.startPosition != null)
			{
				return mapFileInfo.startPosition;
			}
			return mapFileInfo.boundingBox.getCenterPoint();
		}

		return null;
	}

	/**
	 * @return the start zoom level (may be null).
	 */
	public Byte getStartZoomLevel()
	{
		if (this.mapDatabase != null && this.mapDatabase.hasOpenFile())
		{
			MapFileInfo mapFileInfo = this.mapDatabase.getMapFileInfo();
			if (mapFileInfo.startZoomLevel != null)
			{
				return mapFileInfo.startZoomLevel;
			}
		}

		return DEFAULT_START_ZOOM_LEVEL;
	}

	/**
	 * @return the maximum zoom level.
	 */
	public byte getZoomLevelMax()
	{
		return ZOOM_MAX;
	}

	@Override
	public void renderArea(Paint fill, Paint stroke, int level)
	{
		List<ShapePaintContainer> list = this.drawingLayers.get(level);
		list.add(new ShapePaintContainer(this.shapeContainer, stroke));
		list.add(new ShapePaintContainer(this.shapeContainer, fill));
	}

	@Override
	public void renderAreaCaption(String caption, float verticalOffset, Paint fill, Paint stroke)
	{
		Point centerPosition = GeometryUtils.calculateCenterOfBoundingBox(this.coordinates[0]);
		this.areaLabels.add(new PointTextContainer(caption, centerPosition.x, centerPosition.y, fill, stroke));
	}

	@Override
	public void renderAreaSymbol(Bitmap symbol)
	{
		Point centerPosition = GeometryUtils.calculateCenterOfBoundingBox(this.coordinates[0]);
		int halfSymbolWidth = symbol.getWidth() / 2;
		int halfSymbolHeight = symbol.getHeight() / 2;
		double pointX = centerPosition.x - halfSymbolWidth;
		double pointY = centerPosition.y - halfSymbolHeight;
		Point shiftedCenterPosition = new Point(pointX, pointY);
		this.pointSymbols.add(new SymbolContainer(symbol, shiftedCenterPosition));
	}

	@Override
	public void renderPointOfInterestCaption(String caption, float verticalOffset, Paint fill, Paint stroke)
	{
		this.nodes.add(new PointTextContainer(caption, this.poiPosition.x, this.poiPosition.y + verticalOffset, fill, stroke));
	}

	@Override
	public void renderPointOfInterestCircle(float radius, Paint fill, Paint stroke, int level)
	{
		List<ShapePaintContainer> list = this.drawingLayers.get(level);
		list.add(new ShapePaintContainer(new CircleContainer(this.poiPosition, radius), stroke));
		list.add(new ShapePaintContainer(new CircleContainer(this.poiPosition, radius), fill));
	}

	@Override
	public void renderPointOfInterestSymbol(Bitmap symbol)
	{
		int halfSymbolWidth = symbol.getWidth() / 2;
		int halfSymbolHeight = symbol.getHeight() / 2;
		double pointX = this.poiPosition.x - halfSymbolWidth;
		double pointY = this.poiPosition.y - halfSymbolHeight;
		Point shiftedCenterPosition = new Point(pointX, pointY);
		this.pointSymbols.add(new SymbolContainer(symbol, shiftedCenterPosition));
	}

	@Override
	public void renderWay(Paint stroke, int level)
	{
		this.drawingLayers.get(level).add(new ShapePaintContainer(this.shapeContainer, stroke));
	}

	@Override
	public void renderWaySymbol(Bitmap symbolBitmap, boolean alignCenter, boolean repeatSymbol)
	{
		WayDecorator.renderSymbol(symbolBitmap, alignCenter, repeatSymbol, this.coordinates, this.waySymbols);
	}

	@Override
	public void renderWayText(String textKey, Paint fill, Paint stroke)
	{
		WayDecorator.renderText(textKey, fill, stroke, this.coordinates, this.wayNames);
	}

	private void clearLists()
	{
		for (int i = this.ways.size() - 1; i >= 0; --i)
		{
			List<List<ShapePaintContainer>> innerWayList = this.ways.get(i);
			for (int j = innerWayList.size() - 1; j >= 0; --j)
			{
				innerWayList.get(j).clear();
			}
		}

		this.areaLabels.clear();
		this.nodes.clear();
		this.pointSymbols.clear();
		this.wayNames.clear();
		this.waySymbols.clear();
	}

	private void createWayLists()
	{
		int levels = this.renderTheme.getLevels();
		this.ways.clear();

		for (byte i = LAYERS - 1; i >= 0; --i)
		{
			List<List<ShapePaintContainer>> innerWayList = new F_List<List<ShapePaintContainer>>(levels);
			for (int j = levels - 1; j >= 0; --j)
			{
				innerWayList.add(new F_List<ShapePaintContainer>(0));
			}
			this.ways.add(innerWayList);
		}
	}

	private CB_RenderTheme getRenderTheme(XmlRenderTheme jobTheme, DisplayModel displayModel)
	{
		try
		{
			return CB_RenderThemeHandler.getRenderTheme(this.graphicFactory, displayModel, jobTheme);
		}
		catch (ParserConfigurationException e)
		{
			LOGGER.log(Level.SEVERE, null, e);
		}
		catch (SAXException e)
		{
			LOGGER.log(Level.SEVERE, null, e);
		}
		catch (IOException e)
		{
			LOGGER.log(Level.SEVERE, null, e);
		}
		return null;
	}

	private void processReadMapData(MapReadResult mapReadResult)
	{
		if (mapReadResult == null)
		{
			return;
		}

		for (PointOfInterest pointOfInterest : mapReadResult.pointOfInterests)
		{
			renderPointOfInterest(pointOfInterest);
		}

		for (Way way : mapReadResult.ways)
		{
			renderWay(way);
		}

		if (mapReadResult.isWater)
		{
			renderWaterBackground();
		}
	}

	private void renderPointOfInterest(PointOfInterest pointOfInterest)
	{
		this.drawingLayers = this.ways.get(getValidLayer(pointOfInterest.layer));
		this.poiPosition = scaleLatLong(pointOfInterest.position, this.currentRendererJob.displayModel.getTileSize());
		this.renderTheme.matchNode(this, pointOfInterest.tags, this.currentRendererJob.tile.zoomLevel);
	}

	private void renderWaterBackground()
	{
		this.drawingLayers = this.ways.get(0);
		this.coordinates = getTilePixelCoordinates(this.currentRendererJob.displayModel.getTileSize());
		this.shapeContainer = new PolylineContainer(this.coordinates);
		this.renderTheme.matchClosedWay(this, Arrays.asList(TAG_NATURAL_WATER), this.currentRendererJob.tile.zoomLevel);
	}

	private void renderWay(Way way)
	{
		this.drawingLayers = this.ways.get(getValidLayer(way.layer));
		// TODO what about the label position?

		LatLong[][] latLongs = way.latLongs;
		this.coordinates = new Point[latLongs.length][];
		for (int i = 0; i < this.coordinates.length; ++i)
		{
			if (latLongs[i] == null)
			{
				return;
			}
			this.coordinates[i] = new Point[latLongs[i].length];
			for (int j = 0; j < this.coordinates[i].length; ++j)
			{
				this.coordinates[i][j] = scaleLatLong(latLongs[i][j], this.currentRendererJob.displayModel.getTileSize());
			}
		}
		this.shapeContainer = new PolylineContainer(this.coordinates);

		if (GeometryUtils.isClosedWay(this.coordinates[0]))
		{
			this.renderTheme.matchClosedWay(this, way.tags, this.currentRendererJob.tile.zoomLevel);
		}
		else
		{
			this.renderTheme.matchLinearWay(this, way.tags, this.currentRendererJob.tile.zoomLevel);
		}
	}

	/**
	 * Sets the scale stroke factor for the given zoom level.
	 * 
	 * @param zoomLevel
	 *            the zoom level for which the scale stroke factor should be set.
	 */
	private void setScaleStrokeWidth(byte zoomLevel)
	{
		int zoomLevelDiff = Math.max(zoomLevel - STROKE_MIN_ZOOM_LEVEL, 0);
		this.renderTheme.scaleStrokeWidth((float) Math.pow(STROKE_INCREASE, zoomLevelDiff));
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

		int tileSize = rendererJob.displayModel.getTileSize();
		Tile tile = rendererJob.tile;
		tileLatLon_0_x = MercatorProjection.tileXToLongitude(tile.tileX, tile.zoomLevel);
		tileLatLon_0_y = MercatorProjection.tileYToLatitude(tile.tileY, tile.zoomLevel);
		tileLatLon_1_x = MercatorProjection.tileXToLongitude(tile.tileX + 1, tile.zoomLevel);
		tileLatLon_1_y = MercatorProjection.tileYToLatitude(tile.tileY + 1, tile.zoomLevel);

		divLon = (tileLatLon_0_x - tileLatLon_1_x) / tileSize;
		divLat = (tileLatLon_0_y - tileLatLon_1_y) / tileSize;

		TileBitmap bmp = executeJob(rendererJob);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try
		{
			bmp.compress(baos);
			byte[] b = baos.toByteArray();

			Descriptor desc = new Descriptor((int) rendererJob.tile.tileX, (int) rendererJob.tile.tileY, rendererJob.tile.zoomLevel, false);

			TileGL_Bmp bmpTile = new TileGL_Bmp(desc, b, TileState.Present, Format.RGB565);

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

	/**
	 * Converts the given LatLong into XY coordinates on the current object.
	 * 
	 * @param latLong
	 *            the LatLong to convert.
	 * @return the XY coordinates on the current object.
	 */
	private Point scaleLatLong(LatLong latLong, int tileSize)
	{
		double pixelX = (tileLatLon_0_x - latLong.getLongitude()) / divLon;
		double pixelY = (tileLatLon_0_y - latLong.getLatitude()) / divLat;

		return new Point((float) pixelX, (float) pixelY);
	}

}
