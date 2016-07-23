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

import java.io.IOException;
import java.io.InputStream;

import org.mapsforge.core.graphics.Color;
import org.mapsforge.core.graphics.Matrix;
import org.mapsforge.core.graphics.Paint;
import org.mapsforge.core.graphics.ResourceBitmap;
import org.mapsforge.core.graphics.TileBitmap;
import org.mapsforge.map.model.DisplayModel;

import CB_UI_Base.graphics.extendedIntrefaces.ext_Bitmap;
import CB_UI_Base.graphics.extendedIntrefaces.ext_Canvas;
import CB_UI_Base.graphics.extendedIntrefaces.ext_GraphicFactory;
import CB_UI_Base.graphics.extendedIntrefaces.ext_Matrix;
import CB_UI_Base.graphics.extendedIntrefaces.ext_Paint;
import CB_UI_Base.graphics.extendedIntrefaces.ext_Path;
import CB_UI_Base.settings.CB_UI_Base_Settings;
import CB_Utils.Util.HSV_Color;
import android.app.Application;

/**
 * @author Longri
 */
public class ext_AndroidGraphicFactory extends AndroidGraphicFactory implements ext_GraphicFactory {
	public static Application aplication;
	private final float ScaleFactor;

	public static void createInstance(Application app) {
		aplication = app;
		INSTANCE = new AndroidGraphicFactory(app);
	}

	protected ext_AndroidGraphicFactory(Application app) {
		super(app);
		aplication = app;
		DisplayModel.setDeviceScaleFactor(1f);
		this.ScaleFactor = 1f;
	}

	protected ext_AndroidGraphicFactory(float scaleFactor) {
		super(aplication);
		DisplayModel.setDeviceScaleFactor(scaleFactor);
		this.ScaleFactor = scaleFactor;
	}

	// ############################################################################################
	// Overrides for CB.ext_GraphicFactory
	// ############################################################################################

	@Override
	public ext_Matrix createMatrix(ext_Matrix matrix) {

		return null;
	}

	@Override
	public ext_Paint createPaint(ext_Paint paint) {
		return new ext_AndroidPaint(paint);
	}

	@Override
	public int setColorAlpha(int color, float paintOpacity) {

		return 0;
	}

	public static ext_GraphicFactory getInstance(float ScaleFactor) {
		if (FactoryList.containsKey(ScaleFactor))
			return FactoryList.get(ScaleFactor);

		ext_AndroidGraphicFactory factory = new ext_AndroidGraphicFactory(ScaleFactor);
		FactoryList.put(ScaleFactor, factory);
		return factory;
	}

	// ############################################################################################
	// Overrides for mapsforge.AndroidGraphicFactory
	// ############################################################################################

	@Override
	public Paint createPaint() {
		return new ext_AndroidPaint();
	}

	@Override
	public Matrix createMatrix() {
		return new ext_AndroidMatrix();
	}

	@Override
	public ext_Bitmap createBitmap(int width, int height) {
		return new ext_AndroidBitmap(width, height);
	}

	@Override
	public TileBitmap createTileBitmap(int tileSize, boolean hasAlpha) {
		return new ext_AndroidTileBitmap(tileSize);
	}

	@Override
	public ext_Canvas createCanvas() {
		return new ext_AndroidCanvas();
	}

	@Override
	public ext_Path createPath() {
		return new ext_AndroidPath();
	}

	@Override
	public ResourceBitmap createResourceBitmap(InputStream inputStream, int hash) throws IOException {
		return new ext_AndroidResourceBitmap(inputStream, hash, this.ScaleFactor);
	}

	@Override
	public int createColor(Color color) {
		int c = getColor(color);
		if (CB_UI_Base_Settings.nightMode.getValue())
			c = HSV_Color.colorMatrixManipulation(c, HSV_Color.NIGHT_COLOR_MATRIX);
		return c;
	}

	@Override
	public int createColor(int alpha, int red, int green, int blue) {
		int c = android.graphics.Color.argb(alpha, red, green, blue);
		if (CB_UI_Base_Settings.nightMode.getValue())
			c = HSV_Color.colorMatrixManipulation(c, HSV_Color.NIGHT_COLOR_MATRIX);
		return c;
	}

	@Override
	public ResourceBitmap renderSvg(InputStream inputStream, float scaleFactor, int width, int height, int percent, int hash) throws IOException {
		return new ext_AndroidSvgBitmap(inputStream, hash, scaleFactor, width, height, percent);
	}
}
