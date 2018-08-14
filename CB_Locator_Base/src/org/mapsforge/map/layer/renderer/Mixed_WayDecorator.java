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

import org.mapsforge.core.graphics.Bitmap;
import org.mapsforge.core.mapelements.SymbolContainer;
import org.mapsforge.core.model.Point;

import java.util.List;

/**
 * WayDecorator with scaled Distance values!
 *
 * @author Longri
 */
public class Mixed_WayDecorator {
    /**
     * Minimum distance in pixels before the symbol is repeated.
     */
    public static int DISTANCE_BETWEEN_SYMBOLS;

    /**
     * Minimum distance in pixels before the way name is repeated.
     */
    public static int DISTANCE_BETWEEN_WAY_NAMES;

    /**
     * Distance in pixels to skip from both ends of a segment.
     */
    public static int SEGMENT_SAFETY_DISTANCE;

    private static float ScaleFactor = -1;

    static void renderSymbol(float scale, Bitmap symbolBitmap, boolean alignCenter, boolean repeatSymbol, Point[][] coordinates, List<SymbolContainer> waySymbols) {

        if (ScaleFactor == -1 || ScaleFactor != scale) {
            ScaleFactor = scale;
            DISTANCE_BETWEEN_SYMBOLS = (int) (200 * ScaleFactor);
            DISTANCE_BETWEEN_WAY_NAMES = (int) (500 * ScaleFactor);
            SEGMENT_SAFETY_DISTANCE = (int) (30 * ScaleFactor);
        }

        int skipPixels = SEGMENT_SAFETY_DISTANCE;

        // get the first way point coordinates
        double previousX = coordinates[0][0].x;
        double previousY = coordinates[0][0].y;

        // draw the symbol on each way segment
        float segmentLengthRemaining;
        float segmentSkipPercentage;
        float theta;
        for (int i = 1; i < coordinates[0].length; ++i) {
            // get the current way point coordinates
            double currentX = coordinates[0][i].x;
            double currentY = coordinates[0][i].y;

            // calculate the length of the current segment (Euclidian distance)
            double diffX = currentX - previousX;
            double diffY = currentY - previousY;
            double segmentLengthInPixel = Math.sqrt(diffX * diffX + diffY * diffY);
            segmentLengthRemaining = (float) segmentLengthInPixel;

            while (segmentLengthRemaining - skipPixels > SEGMENT_SAFETY_DISTANCE) {
                // calculate the percentage of the current segment to skip
                segmentSkipPercentage = skipPixels / segmentLengthRemaining;

                // move the previous point forward towards the current point
                previousX += diffX * segmentSkipPercentage;
                previousY += diffY * segmentSkipPercentage;
                theta = (float) Math.atan2(currentY - previousY, currentX - previousX);

                Point point = new Point(previousX, previousY);
                //waySymbols.add(new SymbolContainer(symbolBitmap, point, alignCenter, theta));

                // check if the symbol should only be rendered once
                if (!repeatSymbol) {
                    return;
                }

                // recalculate the distances
                diffX = currentX - previousX;
                diffY = currentY - previousY;

                // recalculate the remaining length of the current segment
                segmentLengthRemaining -= skipPixels;

                // set the amount of pixels to skip before repeating the symbol
                skipPixels = DISTANCE_BETWEEN_SYMBOLS;
            }

            skipPixels -= segmentLengthRemaining;
            if (skipPixels < SEGMENT_SAFETY_DISTANCE) {
                skipPixels = SEGMENT_SAFETY_DISTANCE;
            }

            // set the previous way point coordinates for the next loop
            previousX = currentX;
            previousY = currentY;
        }
    }
}
