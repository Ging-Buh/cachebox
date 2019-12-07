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
package de.droidcachebox.locator.map;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import de.droidcachebox.gdx.graphics.CircleDrawable;
import de.droidcachebox.gdx.graphics.GL_Paint;
import de.droidcachebox.gdx.math.UiSizes;

/**
 * Drawable for drawing accuracy cycle
 *
 * @author Longri
 */
public class AccuracyDrawable {

    final float size;
    final float step;
    private final int CIRCLE_COUNT = 7;
    CircleDrawable[] fills = new CircleDrawable[CIRCLE_COUNT];
    CircleDrawable[] strokes = new CircleDrawable[CIRCLE_COUNT];
    float[] radien = new float[CIRCLE_COUNT];

    AccuracyDrawable(float maxWidth, float maxHeight) {

        size = Math.max(maxWidth, maxHeight);
        // create CIRCLE_COUNT circle with different radius
        step = size / CIRCLE_COUNT;


        float radius = step;
        for (int index = 0; index < CIRCLE_COUNT; index = index + 1) {
            strokes[index] = createStroke(radius);
            fills[index] = createFill(radius);
            radien[index] = radius;
            radius = radius + step;
        }
    }

    private CircleDrawable createFill(float radius) {
        GL_Paint paint = new GL_Paint();
        paint.setColor(Color.BLUE);
        paint.setAlpha(50);
        return new CircleDrawable(0, 0, radius, paint, size, size);
    }

    private CircleDrawable createStroke(float radius) {
        GL_Paint paint = new GL_Paint();
        paint.setColor(Color.BLUE);
        paint.setStyle(GL_Paint.GL_Style.STROKE);
        paint.setStrokeWidth(3 * UiSizes.getInstance().getScale());
        return new CircleDrawable(0, 0, radius, paint, size, size);
    }

    public void draw(Batch batch, float x, float y, float radius) {

        int index = 0;
        while (true) {
            if (radius <= radien[index])
                break;
            index++;
            if (index == CIRCLE_COUNT - 1)
                break;
        }

        float scale = (radius / radien[index]) * size;

        fills[index].draw(batch, x, y, scale, scale, 0);
        strokes[index].draw(batch, x, y, scale, scale, 0);
    }

}
