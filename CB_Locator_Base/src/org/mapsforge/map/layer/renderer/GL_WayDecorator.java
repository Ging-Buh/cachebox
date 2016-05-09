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

import org.mapsforge.core.graphics.Paint;
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

    public static void renderText(String textKey, Paint fill, Paint stroke, Point tileOrigin, Point[][] coordinates, List<GL_WayTextContainer> wayNames, float TileSize) {

	// change coordinates to Tile points
	Point[][] tilePoints = new Point[coordinates.length][];
	int idx = 0;
	for (Point[] points : coordinates) {
	    tilePoints[idx] = new Point[points.length];
	    int idx2 = 0;
	    for (Point point : points) {
		tilePoints[idx][idx2++] = point.offset(-tileOrigin.x, -tileOrigin.y);
	    }
	    idx++;
	}

	GL_Path path = new GL_Path(tilePoints[0].length);

	path.moveTo((float) tilePoints[0][0].x, (float) tilePoints[0][0].y);

	for (int i = 1; i < tilePoints[0].length; ++i) {
	    path.lineTo((float) tilePoints[0][i].x, (float) tilePoints[0][i].y);
	}

	if (MathUtils.LegalizeDegreese(path.getAverageDirection()) > 180) {
	    path.revert();
	}

	// Calculate Average center point. For skip same name drawing closer DISTANCE_BETWEEN_WAY_NAMES
	double averageX = 0;
	double averageY = 0;
	int averageCount = 0;
	for (int i = 0; i < tilePoints[0].length; ++i) {
	    // get the current way point coordinates
	    averageX += tilePoints[0][i].x;
	    averageY += tilePoints[0][i].y;
	    averageCount++;
	}
	averageX /= averageCount;
	averageY /= averageCount;

	float max = TileSize + (TileSize / 20);
	float min = 0 - (TileSize / 20);
	//
	//	if (averageX < min || averageX > max || averageY < min || averageY > max) {
	//	    return; // outside of Tile
	//	}

	wayNames.add(new GL_WayTextContainer(path, textKey, fill, stroke, averageX, averageY));

    }

}
