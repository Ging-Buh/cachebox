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

import java.util.ArrayList;
import java.util.List;

import org.mapsforge.core.graphics.GraphicFactory;
import org.mapsforge.core.graphics.Style;
import org.mapsforge.core.mapelements.PointTextContainer;
import org.mapsforge.core.mapelements.SymbolContainer;
import org.mapsforge.core.model.Point;
import org.mapsforge.map.model.DisplayModel;

import com.badlogic.gdx.math.MathUtils;

import CB_UI_Base.graphics.CircleDrawable;
import CB_UI_Base.graphics.GL_GraphicFactory;
import CB_UI_Base.graphics.GL_Paint;
import CB_UI_Base.graphics.GL_Path;
import CB_UI_Base.graphics.PolygonDrawable;
import CB_UI_Base.graphics.PolylineDrawable;
import CB_UI_Base.graphics.SymbolDrawable;
import CB_UI_Base.graphics.TextDrawable;
import CB_UI_Base.graphics.TextDrawableFlipped;
import CB_UI_Base.graphics.Images.BitmapDrawable;
import CB_UI_Base.graphics.Images.VectorDrawable;

/**
 * @author Longri
 */
public class GL_Rasterer {
	private static final String UNKNOWN_STYLE = "unknown style: ";
	private final int TILE_SIZE;
	private final DisplayModel DISPLAY_MODEL;

	// private final GraphicFactory GRAPHIC_FACTORY;
	// private final CB_RectF TILE_REG;

	public GL_Rasterer(GraphicFactory graphicFactory, DisplayModel displayModel) {
		// this.symbolMatrix = (ext_Matrix) graphicFactory.createMatrix();
		// GRAPHIC_FACTORY = graphicFactory;
		DISPLAY_MODEL = displayModel;
		// TILE_REG = new CB_RectF(new SizeF(displayModel.getTileSize(), displayModel.getTileSize()));
		TILE_SIZE = displayModel.getTileSize();
	}

	public void drawWays(VectorDrawable drw, List<List<List<ShapePaintContainer>>> ways) {
		int levelsPerLayer = ways.get(0).size();

		for (int layer = 0, layers = ways.size(); layer < layers; ++layer) {
			List<List<ShapePaintContainer>> shapePaintContainers = ways.get(layer);

			for (int level = 0; level < levelsPerLayer; ++level) {
				List<ShapePaintContainer> wayList = shapePaintContainers.get(level);

				for (int index = wayList.size() - 1; index >= 0; --index) {
					drawShapePaintContainer(drw, wayList.get(index));
				}
			}
		}
	}

	private void drawShapePaintContainer(VectorDrawable drw, ShapePaintContainer shapePaintContainer) {
		ShapeType shapeType = shapePaintContainer.shapeContainer.getShapeType();
		switch (shapeType) {
		case CIRCLE:
			drawCircleContainer(drw, shapePaintContainer);
			return;

		case POLYLINE:
			PolylineContainer polylineContainer = (PolylineContainer) shapePaintContainer.shapeContainer;
			//	drawPath(drw, shapePaintContainer, polylineContainer.getCoordinatesRelativeToTile());
			return;
		}
	}

	private void drawCircleContainer(VectorDrawable drw, ShapePaintContainer shapePaintContainer) {
		CircleContainer circleContainer = (CircleContainer) shapePaintContainer.shapeContainer;

		GL_Paint Paint = (GL_Paint) shapePaintContainer.paint;

		Point point = circleContainer.point;

		float PointX = (float) (point.x);
		float PointY = (float) (TILE_SIZE - point.y);// Flip y axis
		float radius = circleContainer.radius * DISPLAY_MODEL.getScaleFactor();

		{// TODO Don't draw if circle complete outside of tile

			// CB_RectF rec = new CB_RectF(PointX - radius, PointY - radius, radius * 2, radius * 2);

			// TODO TILE_REG.contains(rec) is wrong

			// if (!TILE_REG.contains(rec))
			// {
			// return;
			// }
		}

		drw.addDrawable(new CircleDrawable(PointX, PointY, radius, Paint, TILE_SIZE, TILE_SIZE), true);
	}

	private void drawPath(VectorDrawable drw, ShapePaintContainer shape, Point[][] coordinates) {
		GL_Paint paint = (GL_Paint) shape.paint;

		// if (true) return;

		if (paint.isTransparent()) {
			return; // nothing to draw
		}

		GL_Path path = new GL_Path();

		for (int j = 0; j < coordinates.length; ++j) {
			Point[] points = coordinates[j];
			if (points.length >= 2) {
				path.setToMaxItems(points.length);
				Point point = points[0];

				path.moveTo((float) point.x, (float) (TILE_SIZE - point.y));
				for (int i = 1; i < points.length; ++i) {
					point = points[i];
					path.lineTo((float) point.x, (float) (TILE_SIZE - point.y));
				}
			}
		}

		Style style = paint.getStyle();

		ArrayList<float[]> pathes = path.getVertices();

		switch (style) {
		case FILL:
			for (float[] singlePath : pathes) {
				try {
					if (singlePath.length < 6)
						break; // Nothing to Draw
					short[] triangles = GL_GraphicFactory.ECT.computeTriangles(singlePath).toArray();
					drw.addDrawable(new PolygonDrawable(singlePath, triangles, paint, TILE_SIZE, TILE_SIZE), false);
				} catch (Exception e) {

				}
			}
			return;

		case STROKE:
			for (float[] singlePath : pathes) {
				if (singlePath.length < 4)
					break; // Nothing to Draw
				drw.addDrawable(new PolylineDrawable(singlePath, paint, TILE_SIZE, TILE_SIZE), true);

				// if (paint.getStrokeWidth() < 4) return;
				//
				// // DEBUG draw point on all PolyLine points
				// GL_Paint p = new GL_Paint();
				// p.setColor(Color.MAGENTA);
				// GL_Paint p2 = new GL_Paint();
				// p2.setColor(Color.BLACK);
				// for (int i = 0; i < singlePath.length; i += 2)
				// {
				// drw.addDrawable(new CircleDrawable(singlePath[i], singlePath[i + 1], 3, p, TILE_SIZE, TILE_SIZE), true);
				// drw.addDrawable(new CircleDrawable(singlePath[i], singlePath[i + 1], 1, p2, TILE_SIZE, TILE_SIZE), true);
				// return;
				// }

			}
			return;
		}

		throw new IllegalArgumentException(UNKNOWN_STYLE + style);

	}

	public void drawSymbols(VectorDrawable drw, List<SymbolContainer> symbolContainers) {
		// FIXME use Methode from MixedDataBaseRenderer

		for (int index = symbolContainers.size() - 1; index >= 0; --index) {
			SymbolContainer symbolContainer = symbolContainers.get(index);

			float PointX = (float) (symbolContainer.xy.x);
			float PointY = (float) (TILE_SIZE - symbolContainer.xy.y);

			BitmapDrawable bmp = (BitmapDrawable) symbolContainer.symbol;

			if (symbolContainer.theta != 0) {

				float theta = (360 - (symbolContainer.theta * MathUtils.radiansToDegrees)) * MathUtils.degreesToRadians;

				drw.addDrawable(new SymbolDrawable(bmp, PointX, PointY, TILE_SIZE, TILE_SIZE, symbolContainer.alignCenter, theta), true);
				// drw.addRotateDrawable(new SymbolDrawable(bmp, PointX, PointY, TILE_SIZE, TILE_SIZE, symbolContainer.alignCenter));

			} else {

				drw.addRotateDrawable(new SymbolDrawable(bmp, PointX, PointY, TILE_SIZE, TILE_SIZE, symbolContainer.alignCenter));

			}
		}
	}

	public void drawNodes(VectorDrawable drw, List<PointTextContainer> pointTextContainers) {
		// FIXME use Methode from MixedDataBaseRenderer

		for (int index = pointTextContainers.size() - 1; index >= 0; --index) {
			PointTextContainer pointTextContainer = pointTextContainers.get(index);

			float TextWidth = (float) (pointTextContainer.boundary.getWidth());

			float PointX = (float) pointTextContainer.xy.x;
			float PointY = (float) (DISPLAY_MODEL.getTileSize() - pointTextContainer.xy.y);

			GL_Path path = new GL_Path();
			path.moveTo(PointX, PointY);
			path.lineTo(PointX + TextWidth, PointY);

			drw.addRotateDrawable(new TextDrawable(pointTextContainer.text, path, DISPLAY_MODEL.getTileSize(), DISPLAY_MODEL.getTileSize(), (GL_Paint) pointTextContainer.paintFront, (GL_Paint) pointTextContainer.paintBack, false));

		}
	}

	public void drawWayNames(VectorDrawable drw, List<GL_WayTextContainer> wayTextContainers) {
		// FIXME use Methode from MixedDataBaseRenderer

		for (int index = wayTextContainers.size() - 1; index >= 0; --index) {
			GL_WayTextContainer wayTextContainer = wayTextContainers.get(index);
			wayTextContainer.path.flipY(DISPLAY_MODEL.getTileSize());
			drw.addRotateDrawable(new TextDrawableFlipped(wayTextContainer.text, wayTextContainer.path, DISPLAY_MODEL.getTileSize(), DISPLAY_MODEL.getTileSize(), (GL_Paint) wayTextContainer.fill, (GL_Paint) wayTextContainer.stroke, true));

		}
	}
}
