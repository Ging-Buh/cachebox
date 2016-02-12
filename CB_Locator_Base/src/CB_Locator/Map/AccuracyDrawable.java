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
package CB_Locator.Map;

import CB_UI_Base.Math.UI_Size_Base;
import CB_UI_Base.graphics.CircleDrawable;
import CB_UI_Base.graphics.GL_Paint;
import CB_UI_Base.graphics.GL_Style;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;

/**
 * Drawable for drawing accuracy cycle
 * 
 * @author Longri
 */
public class AccuracyDrawable {

	private final int CIRCLE_COUNT = 7;

	CircleDrawable[] fills = new CircleDrawable[CIRCLE_COUNT];
	CircleDrawable[] strokes = new CircleDrawable[CIRCLE_COUNT];
	float[] radien = new float[CIRCLE_COUNT];
	final float size;
	final float step;

	AccuracyDrawable(float maxWidth, float maxHeight) {

		size = Math.max(maxWidth, maxHeight);
		// create CIRCLE_COUNT circle with different radius
		step = size / (CIRCLE_COUNT);

		int index = 0;
		for (float radius = step; index < CIRCLE_COUNT; radius += step) {
			createDrawablesFor(index++, radius);
		}

	}

	private void createDrawablesFor(int index, float radius) {

		GL_Paint paint = new GL_Paint();
		paint.setColor(Color.BLUE);
		paint.setStyle(GL_Style.STROKE);
		paint.setStrokeWidth(3 * UI_Size_Base.that.getScale());
		strokes[index] = new CircleDrawable(0, 0, radius, paint, size, size);

		GL_Paint paint2 = new GL_Paint();
		paint2.setColor(Color.BLUE);
		paint2.setAlpha(50);
		fills[index] = new CircleDrawable(0, 0, radius, paint2, size, size);

		radien[index] = radius;
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
