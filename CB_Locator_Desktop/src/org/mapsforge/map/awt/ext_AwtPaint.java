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

import java.awt.FontMetrics;
import java.awt.image.BufferedImage;

import CB_UI_Base.graphics.GL_FontFamily;
import CB_UI_Base.graphics.GL_FontStyle;
import CB_UI_Base.graphics.GL_Style;
import CB_UI_Base.graphics.Join;
import CB_UI_Base.graphics.TileMode;
import CB_UI_Base.graphics.extendedIntrefaces.ext_Matrix;
import CB_UI_Base.graphics.extendedIntrefaces.ext_Paint;
import CB_Utils.Util.HSV_Color;

/**
 * @author Longri
 */
public class ext_AwtPaint extends AwtPaint implements ext_Paint {

	public ext_AwtPaint(ext_Paint paint) {

	}

	@Override
	public void setAlpha(int i) {

	}

	@Override
	public void setStrokeJoin(Join join) {

	}

	@Override
	public void setRadialGradiant(float x, float y, float radius, int[] colors, float[] positions, TileMode tileMode) {

	}

	@Override
	public void setGradientMatrix(ext_Matrix matrix) {

	}

	@Override
	public void setLinearGradient(float x1, float y1, float x2, float y2, int[] colors, float[] positions, TileMode tileMode) {

	}

	@Override
	public GL_Style getGL_Style() {

		return null;
	}

	@Override
	public float getTextSize() {
		return this.textSize;
	}

	@Override
	public void setDashPathEffect(float[] strokeDasharray, float offset) {

	}

	@Override
	public void delDashPathEffect() {

	}

	@Override
	public ext_Matrix getGradiantMatrix() {

		return null;
	}

	@Override
	public void setStyle(GL_Style fill) {

	}

	@Override
	public GL_FontStyle getGLFontStyle() {

		switch (fontStyle) {
		case 0:
			return GL_FontStyle.NORMAL;
		case 1:
			return GL_FontStyle.BOLD;
		case 2:
			return GL_FontStyle.ITALIC;
		case 3:
			return GL_FontStyle.BOLD_ITALIC;
		}

		return GL_FontStyle.NORMAL;
	}

	@Override
	public GL_FontFamily getGLFontFamily() {
		if (fontName == null)
			return GL_FontFamily.DEFAULT;

		if (fontName.equals("DEFAULT"))
			return GL_FontFamily.DEFAULT;
		if (fontName.equals("MONOSPACE"))
			return GL_FontFamily.MONOSPACE;
		if (fontName.equals("SANS_SERIF"))
			return GL_FontFamily.SANS_SERIF;
		if (fontName.equals("SERIF"))
			return GL_FontFamily.SERIF;
		return GL_FontFamily.DEFAULT;
	}

	@Override
	public HSV_Color getHSV_Color() {
		int r = color.getRed();
		int g = color.getGreen();
		int b = color.getBlue();
		int a = color.getAlpha();

		HSV_Color c = new HSV_Color(a, r, g, b);

		return c;

	}

	@Override
	public float getStrokeWidth() {
		return strokeWidth;
	}

	@Override
	public int getTextHeight(String text) {
		if (this.font == null)
			return 0;
		BufferedImage bufferedImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		FontMetrics fontMetrics = bufferedImage.getGraphics().getFontMetrics(this.font);
		return fontMetrics.getHeight();
	}

	@Override
	public int getTextWidth(String text) {
		if (this.font == null)
			return 0;
		BufferedImage bufferedImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		FontMetrics fontMetrics = bufferedImage.getGraphics().getFontMetrics(this.font);
		return fontMetrics.stringWidth(text);
	}

}
