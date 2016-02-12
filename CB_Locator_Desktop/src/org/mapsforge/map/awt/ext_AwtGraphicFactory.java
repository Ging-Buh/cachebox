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
package org.mapsforge.map.awt;

import java.io.IOException;
import java.io.InputStream;

import org.mapsforge.core.graphics.Color;
import org.mapsforge.core.graphics.ResourceBitmap;
import org.mapsforge.core.graphics.TileBitmap;

import CB_UI_Base.graphics.extendedIntrefaces.ext_Bitmap;
import CB_UI_Base.graphics.extendedIntrefaces.ext_Canvas;
import CB_UI_Base.graphics.extendedIntrefaces.ext_GraphicFactory;
import CB_UI_Base.graphics.extendedIntrefaces.ext_Matrix;
import CB_UI_Base.graphics.extendedIntrefaces.ext_Paint;
import CB_UI_Base.graphics.extendedIntrefaces.ext_Path;
import CB_UI_Base.settings.CB_UI_Base_Settings;
import CB_Utils.Util.HSV_Color;

/**
 * @author Longri
 */
public class ext_AwtGraphicFactory extends AwtGraphicFactory implements ext_GraphicFactory {

	private final float ScaleFactor;

	public ext_AwtGraphicFactory(float ScaleFactor) {
		this.ScaleFactor = ScaleFactor;
	}

	@Override
	public ext_Matrix createMatrix(ext_Matrix matrix) {
		return new ext_AwtMatrix(matrix);
	}

	@Override
	public ext_Paint createPaint(ext_Paint paint) {
		return new ext_AwtPaint(paint);
	}

	@Override
	public int setColorAlpha(int color, float paintOpacity) {

		return 0;
	}

	@Override
	public ext_Bitmap createBitmap(int width, int height) {
		return new ext_AwtBitmap(width, height);
	}

	@Override
	public ext_Canvas createCanvas() {
		return new ext_AwtCanvas();
	}

	@Override
	public ext_Path createPath() {
		return new ext_AwtPath();
	}

	@Override
	public TileBitmap createTileBitmap(int tileSize, boolean hasAlpha) {
		return new ext_AwtTileBitmap(tileSize);
	}

	@Override
	public ResourceBitmap createResourceBitmap(InputStream inputStream, int hash) throws IOException {
		return new ext_AwtResourceBitmap(inputStream, hash, this.ScaleFactor);
	}

	public static ext_GraphicFactory getInstance(float ScaleFactor) {
		if (FactoryList.containsKey(ScaleFactor))
			return FactoryList.get(ScaleFactor);

		ext_AwtGraphicFactory factory = new ext_AwtGraphicFactory(ScaleFactor);
		FactoryList.put(ScaleFactor, factory);
		return factory;
	}

	@Override
	public int createColor(Color color) {
		int c = getColor(color).getRGB();
		if (CB_UI_Base_Settings.nightMode.getValue())
			c = HSV_Color.colorMatrixManipulation(c, HSV_Color.NIGHT_COLOR_MATRIX);
		return c;
	}

	@Override
	public int createColor(int alpha, int red, int green, int blue) {
		int c = new java.awt.Color(red, green, blue, alpha).getRGB();
		if (CB_UI_Base_Settings.nightMode.getValue())
			c = HSV_Color.colorMatrixManipulation(c, HSV_Color.NIGHT_COLOR_MATRIX);
		return c;
	}
}
