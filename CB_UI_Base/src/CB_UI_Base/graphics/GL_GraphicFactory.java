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
package CB_UI_Base.graphics;

import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;

import org.mapsforge.core.graphics.Bitmap;
import org.mapsforge.core.graphics.Color;
import org.mapsforge.core.graphics.Display;
import org.mapsforge.core.graphics.Matrix;
import org.mapsforge.core.graphics.Paint;
import org.mapsforge.core.graphics.Position;
import org.mapsforge.core.graphics.ResourceBitmap;
import org.mapsforge.core.graphics.TileBitmap;
import org.mapsforge.core.mapelements.PointTextContainer;
import org.mapsforge.core.mapelements.SymbolContainer;
import org.mapsforge.core.model.Point;

import com.badlogic.gdx.math.EarClippingTriangulator;

import CB_UI_Base.graphics.Images.BitmapDrawable;
import CB_UI_Base.graphics.Images.VectorDrawable;
import CB_UI_Base.graphics.SVG.SVG;
import CB_UI_Base.graphics.SVG.SVGParseException;
import CB_UI_Base.graphics.extendedInterfaces.ext_Bitmap;
import CB_UI_Base.graphics.extendedInterfaces.ext_Canvas;
import CB_UI_Base.graphics.extendedInterfaces.ext_GraphicFactory;
import CB_UI_Base.graphics.extendedInterfaces.ext_Matrix;
import CB_UI_Base.graphics.extendedInterfaces.ext_Paint;
import CB_UI_Base.graphics.extendedInterfaces.ext_Path;
import CB_Utils.Util.HSV_Color;

/**
 * @author Longri
 */
public class GL_GraphicFactory implements ext_GraphicFactory {
	public final static EarClippingTriangulator ECT = new EarClippingTriangulator();
	public final static HSV_Color TRANSPARENT = new HSV_Color(0, 0, 0, 0);
	private final float scaleFactor;
	private final Hashtable<Integer, ResourceBitmap> BmpBuffer = new Hashtable<Integer, ResourceBitmap>();

	public GL_GraphicFactory(float ScaleFactor) {
		this.scaleFactor = ScaleFactor;
	}

	static HSV_Color getColor(Color color) {
		switch (color) {
		case BLACK:
			return new HSV_Color(com.badlogic.gdx.graphics.Color.BLACK);
		case BLUE:
			return new HSV_Color(com.badlogic.gdx.graphics.Color.BLUE);
		case GREEN:
			return new HSV_Color(com.badlogic.gdx.graphics.Color.GREEN);
		case RED:
			return new HSV_Color(com.badlogic.gdx.graphics.Color.RED);
		case TRANSPARENT:
			return TRANSPARENT;
		case WHITE:
			return new HSV_Color(com.badlogic.gdx.graphics.Color.WHITE);
		}

		throw new IllegalArgumentException("unknown color: " + color);
	}

	public int createColor(HSV_Color color) {
		return color.toInt();
	}

	public float getScaleFactor() {

		return 0;
	}

	@Override
	public InputStream platformSpecificSources(String relativePathPrefix, String src) throws IOException {

		return null;
	}

	public GL_Matrix createMatrix(GL_Matrix matrix) {
		return new GL_Matrix(matrix);
	}

	@Override
	public Paint createPaint(Paint paint) {
		return new GL_Paint(paint);
	}

	@Override
	public int setColorAlpha(int color, float paintOpacity) {
		HSV_Color c = new HSV_Color(color);
		c.a = paintOpacity;
		return c.toInt();
	}

	@Override
	public ext_Bitmap createBitmap(int width, int height) {
		return new VectorDrawable(width, height);
	}

	@Override
	public Bitmap createBitmap(int width, int height, boolean isTransparent) {
		return createBitmap(width, height);
	}

	@Override
	public Matrix createMatrix() {
		return new GL_Matrix();
	}

	@Override
	public ext_Path createPath() {
		return new GL_Path();
	}

	@Override
	public ext_Paint createPaint() {
		return new GL_Paint();
	}

	@Override
	public ext_Canvas createCanvas() {
		return new GL_Canvas();
	}

	@Override
	public ext_Matrix createMatrix(ext_Matrix matrix) {
		return new GL_Matrix(matrix);
	}

	@Override
	public ext_Paint createPaint(ext_Paint paint) {
		return new GL_Paint(paint);
	}

	@Override
	public int createColor(Color color) {
		return getColor(color).toInt();
	}

	@Override
	public int createColor(int alpha, int red, int green, int blue) {
		return new HSV_Color(alpha, red, green, blue).toInt();
	}

	@Override
	public ResourceBitmap createResourceBitmap(InputStream inputStream, int HashCode) throws IOException {
		// First show at Buffer
		if (BmpBuffer.containsKey(HashCode)) {
			return BmpBuffer.get(HashCode);
		}

		// ResourceBitmap bmp = new BitmapDrawable(inputStream, scaleFactor);
		ResourceBitmap bmp = new BitmapDrawable(inputStream, HashCode, scaleFactor);
		BmpBuffer.put(HashCode, bmp);
		return bmp;
	}

	@Override
	public TileBitmap createTileBitmap(int tileSize, boolean isTransparent) {

		return null;
	}

	@Override
	public TileBitmap createTileBitmap(InputStream inputStream, int tileSize, boolean isTransparent) throws IOException {

		return null;
	}

	@Override
	public PointTextContainer createPointTextContainer(Point xy, Display display, int priority, String text, Paint paintFront, Paint paintBack, SymbolContainer symbolContainer, Position position, int maxTextWidth) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResourceBitmap renderSvg(InputStream inputStream, float scaleFactor, int width, int height, int percent, int hash) throws IOException {
		// First show at Buffer
		if (BmpBuffer.containsKey(hash)) {
			return BmpBuffer.get(hash);
		}

		try {
			ResourceBitmap bmp = SVG.createBmpFromSVG(this, inputStream, scaleFactor);
			BmpBuffer.put(hash, bmp);
			return bmp;
		} catch (SVGParseException e) {

			e.printStackTrace();
		}
		return null;
	}

}
