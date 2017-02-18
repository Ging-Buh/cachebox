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
package org.mapsforge.map.android.graphics;

import org.mapsforge.core.graphics.Matrix;
import org.mapsforge.core.graphics.Paint;

import CB_UI_Base.graphics.extendedInterfaces.ext_Canvas;
import CB_UI_Base.graphics.extendedInterfaces.ext_Matrix;
import CB_UI_Base.graphics.extendedInterfaces.ext_Paint;
import CB_UI_Base.graphics.extendedInterfaces.ext_Path;
import CB_UI_Base.graphics.fromAndroid.RectF;

/**
 * Extends the original Mapsforge AwtCanvas with the interface ext_Canvas
 * 
 * @author Longri
 */
public class ext_AndroidCanvas extends AndroidCanvas implements ext_Canvas {

	@Override
	public void drawText(String text, float x, float y, Paint paint) {

	}

	@Override
	public void drawRect(RectF rect, ext_Paint strokePaint) {

	}

	@Override
	public void drawRoundRect(RectF rect, float rx, float ry, ext_Paint strokePaint) {

	}

	@Override
	public void drawOval(RectF rect, ext_Paint fillPaint) {

	}

	@Override
	public void scale(float sx, float sy) {

	}

	@Override
	public void setMatrix(ext_Matrix matrix) {

	}

	@Override
	public ext_Matrix getMatrix() {

		return null;
	}

	@Override
	public void save() {

	}

	@Override
	public void restore() {

	}

	@Override
	public void concat(ext_Matrix matrix) {

	}

	@Override
	public void drawTextOnPath(String text, ext_Path path, float x, float y, ext_Paint fillPaint) {

	}

	@Override
	public void clipRect(float left, float top, float right, float bottom) {

	}

	@Override
	public void clipPath(ext_Path path) {

	}

	@Override
	public void translate(float stepX, float stepY) {

	}

	@Override
	public void saveMatrix() {

	}

	@Override
	public void setMatrix(Matrix matrix) {

	}

}
