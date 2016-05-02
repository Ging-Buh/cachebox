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

import java.util.List;

import org.mapsforge.core.graphics.Bitmap;
import org.mapsforge.core.graphics.Paint;
import org.mapsforge.core.mapelements.SymbolContainer;
import org.mapsforge.core.model.Point;

import CB_UI_Base.graphics.GL_Path;
import CB_Utils.MathUtils;

/**
 * @author Longri
 */
public final class GL_WayDecorator {
	/**
	 * Minimum distance in pixels before the symbol is repeated.
	 */
	public final float DISTANCE_BETWEEN_SYMBOLS;

	/**
	 * Minimum distance in pixels before the way name is repeated.
	 */
	public final float DISTANCE_BETWEEN_WAY_NAMES;

	/**
	 * Distance in pixels to skip from both ends of a segment.
	 */
	public final float SEGMENT_SAFETY_DISTANCE;

	public GL_WayDecorator(float ScaleFactor) {
		DISTANCE_BETWEEN_SYMBOLS = (int) (200 * ScaleFactor);
		DISTANCE_BETWEEN_WAY_NAMES = (int) (500 * ScaleFactor);
		SEGMENT_SAFETY_DISTANCE = (int) (30 * ScaleFactor);

	}

	public void renderSymbol(Bitmap symbolBitmap, boolean alignCenter, boolean repeatSymbol, Point[][] coordinates, List<SymbolContainer> waySymbols) {

		GL_Path path = new GL_Path();

		path.moveTo((float) coordinates[0][0].x, (float) coordinates[0][0].y);
		for (int i = 1; i < coordinates[0].length; ++i) {
			path.lineTo((float) coordinates[0][i].x, (float) coordinates[0][i].y);
		}

		float symbolWidth = symbolBitmap.getWidth();

		float distance = symbolWidth / 1.5f + SEGMENT_SAFETY_DISTANCE;// + DISTANCE_BETWEEN_SYMBOLS;
		boolean finish = repeatSymbol;
		float[] res = path.getPointOnPathAfter(distance);
		if (res == null)
			return; // not enough space for the symbol
		Point point = new Point(res[0], res[1]);
		float angle = res[2] + 5;

		// angle = 0;

		// check if the end of the Symbol + SEGMENT_SAFETY_DISTANCE on the Path
		// res = path.getPointOnPathAfter(distance + symbolWidth + SEGMENT_SAFETY_DISTANCE);
		// if (res == null) return; // not enough space for the symbol

		waySymbols.add(new SymbolContainer(point, symbolBitmap, alignCenter, angle));

		while (finish) {
			distance += symbolBitmap.getWidth() + DISTANCE_BETWEEN_SYMBOLS;
			res = path.getPointOnPathAfter(distance);
			if (res != null) {
				point = new Point(res[0], res[1]);
				angle = res[2] + 5;
				// check if the end of the Symbol + SEGMENT_SAFETY_DISTANCE on the Path
				res = path.getPointOnPathAfter(distance + SEGMENT_SAFETY_DISTANCE + (symbolWidth / 1.5f));
				if (res == null)
					return; // not enough space for the symbol
				// angle = 0;
				waySymbols.add(new SymbolContainer(point, symbolBitmap, alignCenter, angle));
			} else {
				finish = false;
			}
		}

	}

	public static void renderText(String textKey, Paint fill, Paint stroke, Point[][] coordinates, List<GL_WayTextContainer> wayNames, float TileSize) {

		GL_Path path = new GL_Path(coordinates[0].length);

		path.moveTo((float) coordinates[0][0].x, (float) coordinates[0][0].y);

		for (int i = 1; i < coordinates[0].length; ++i) {
			path.lineTo((float) coordinates[0][i].x, (float) coordinates[0][i].y);
		}

		if (MathUtils.LegalizeDegreese(path.getAverageDirection()) > 180) {
			path.revert();
		}

		// Calculate Average center point. For skip same name drawing closer DISTANCE_BETWEEN_WAY_NAMES
		double averageX = 0;
		double averageY = 0;
		int averageCount = 0;
		for (int i = 0; i < coordinates[0].length; ++i) {
			// get the current way point coordinates
			averageX += coordinates[0][i].x;
			averageY += coordinates[0][i].y;
			averageCount++;
		}
		averageX /= averageCount;
		averageY /= averageCount;

		float max = TileSize + (TileSize / 20);
		float min = 0 - (TileSize / 20);

		if (averageX < min || averageX > max || averageY < min || averageY > max) {
			return; // outside of Tile
		}

		wayNames.add(new GL_WayTextContainer(path, textKey, fill, stroke, averageX, averageY));

	}

}
