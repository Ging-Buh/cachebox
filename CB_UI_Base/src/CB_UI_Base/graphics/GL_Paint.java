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

import org.mapsforge.core.graphics.Align;
import org.mapsforge.core.graphics.Bitmap;
import org.mapsforge.core.graphics.Cap;
import org.mapsforge.core.graphics.FontFamily;
import org.mapsforge.core.graphics.FontStyle;
import org.mapsforge.core.graphics.Paint;
import org.mapsforge.core.graphics.Style;

import CB_UI_Base.GL_UI.IRunOnGL;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.graphics.Images.BitmapDrawable;
import CB_UI_Base.graphics.extendedIntrefaces.ext_Matrix;
import CB_UI_Base.graphics.extendedIntrefaces.ext_Paint;
import CB_Utils.Util.HSV_Color;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

/**
 * @author Longri
 */
public class GL_Paint implements ext_Paint {
    protected HSV_Color color;
    private GL_Style style;
    protected GL_Align align;
    protected GL_Cap cap;
    protected Join join;
    protected float[] strokeDasharray = null;
    protected float strokeWidth = 1f;
    protected float textSize;
    protected BitmapDrawable bitmapShader;
    private GL_FontFamily fontFamily = GL_FontFamily.DEFAULT;
    protected GL_FontStyle fontStyle = GL_FontStyle.NORMAL;
    private BitmapFont font;

    public GL_Paint() {
	this.cap = GL_Cap.BUTT;
	this.join = Join.MITER;
	this.color = new HSV_Color(com.badlogic.gdx.graphics.Color.BLACK);
	this.style = GL_Style.FILL;
    }

    public GL_Paint(Paint paint) {
	if (paint instanceof GL_Paint) {
	    GL_Paint p = (GL_Paint) paint;
	    this.cap = p.cap;
	    this.join = p.join;
	    this.color = new HSV_Color(p.color);
	    this.style = p.style;
	    this.textSize = p.textSize;
	    this.strokeWidth = p.strokeWidth;
	    this.strokeDasharray = p.strokeDasharray;
	} else {
	    Cap cap = paint.getCap();
	    switch (cap) {
	    case BUTT:
		this.cap = GL_Cap.BUTT;
		break;
	    case ROUND:
		this.cap = GL_Cap.ROUND;
		break;
	    case SQUARE:
		this.cap = GL_Cap.SQUARE;
		break;
	    default:
		this.cap = GL_Cap.ROUND;
		break;
	    }

	    this.color = new HSV_Color(paint.getColor());

	    Style style = paint.getStyle();
	    switch (style) {
	    case FILL:
		this.style = GL_Style.FILL;
		break;
	    case STROKE:
		this.style = GL_Style.STROKE;
		break;
	    default:
		this.style = GL_Style.FILL;
		break;
	    }
	}
	this.textSize = paint.getTextSize();
	this.strokeWidth = paint.getStrokeWidth();
	this.strokeDasharray = paint.getDashArray();
    }

    @Override
    public HSV_Color getHSV_Color() {
	if (color == null)
	    return null;
	HSV_Color c = new HSV_Color(color);
	c.clamp();
	return c;
    }

    @Override
    public int getTextHeight(String text) {
	if (font == null)
	    return 0;
	return (int) font.getBounds(text).height;

    }

    @Override
    public int getTextWidth(String text) {
	if (font == null)
	    return 0;
	return (int) font.getBounds(text).width;
    }

    public void setColor(Color color) {
	this.color = new HSV_Color(color);
    }

    @Override
    public void setColor(int color) {
	this.color = new HSV_Color(color);
    }

    @Override
    public void setDashPathEffect(float[] strokeDasharray) {
	this.strokeDasharray = strokeDasharray;
    }

    @Override
    public void setStrokeWidth(float strokeWidth) {
	this.strokeWidth = strokeWidth;
    }

    @Override
    public void setStyle(GL_Style style) {
	this.style = style;
    }

    public void setTextAlign(GL_Align align) {
	this.align = align;
    }

    @Override
    public void setTextSize(float textSize) {
	this.textSize = textSize;
	createFont();
    }

    public void setTypeface(GL_FontFamily fontFamily, GL_FontStyle fontStyle) {
	this.setFontFamily(fontFamily);
	this.fontStyle = fontStyle;
	createFont();
    }

    private void createFont() {
	if (this.textSize > 0) {
	    GL.that.RunOnGL(new IRunOnGL() {

		@Override
		public void run() {
		    GL_Paint.this.font = FontCache.get(GL_Paint.this.getGLFontFamily(), GL_Paint.this.getGLFontStyle(), GL_Paint.this.getTextSize());
		}
	    });

	} else {
	    this.font = null;
	}
    }

    @Override
    public boolean isTransparent() {
	return this.bitmapShader == null && this.color.a == 0;
    }

    @Override
    public float getStrokeWidth() {
	return strokeWidth;
    }

    public BitmapDrawable getBitmapShader() {
	return bitmapShader;
    }

    public GL_Cap getGL_Cap() {
	return cap;
    }

    public void setCap(GL_Cap cap) {
	this.cap = cap;
    }

    @Override
    public float[] getDashArray() {
	// chk if DashArray empty, then return null
	if (this.strokeDasharray != null && this.strokeDasharray.length < 2)
	    return null;
	return this.strokeDasharray;
    }

    @Override
    public void setAlpha(int i) {
	color.a = i / 255f;
    }

    @Override
    public void setStrokeJoin(Join join) {
	this.join = join;
    }

    @Override
    public void setRadialGradiant(float x, float y, float radius, int[] colors, float[] positions, TileMode tileMode) {
	// TODO Auto-generated method stub

    }

    public void setGradientMatrix(GL_Matrix matrix) {
	// TODO Auto-generated method stub

    }

    @Override
    public void setLinearGradient(float x1, float y1, float x2, float y2, int[] colors, float[] positions, TileMode tileMode) {
	// TODO Auto-generated method stub

    }

    @Override
    public void setDashPathEffect(float[] strokeDasharray, float offset) {
	this.strokeDasharray = strokeDasharray;
    }

    @Override
    public float getTextSize() {
	return this.textSize;
    }

    @Override
    public void delDashPathEffect() {
	this.strokeDasharray = null;
    }

    @Override
    public ext_Matrix getGradiantMatrix() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public void setBitmapShader(Bitmap bitmap) {
	if (bitmap instanceof BitmapDrawable) {
	    bitmapShader = (BitmapDrawable) bitmap;
	}
    }

    public void setBitmapShader(BitmapDrawable bitmap) {
	bitmapShader = bitmap;
    }

    @Override
    public void setColor(org.mapsforge.core.graphics.Color color) {
	this.color = new HSV_Color(GL_GraphicFactory.getColor(color));
    }

    @Override
    public void setStrokeCap(Cap cap) {

	switch (cap) {
	case BUTT:
	    this.cap = GL_Cap.BUTT;
	    break;
	case ROUND:
	    this.cap = GL_Cap.ROUND;
	    break;
	case SQUARE:
	    this.cap = GL_Cap.SQUARE;
	    break;
	default:
	    this.cap = GL_Cap.DEFAULT;
	    break;
	}
    }

    @Override
    public void setTextAlign(Align align) {
	// TODO Auto-generated method stub

    }

    @Override
    public void setTypeface(FontFamily fontFamily, FontStyle fontStyle) {
	// TODO Auto-generated method stub

    }

    @Override
    public void setGradientMatrix(ext_Matrix matrix) {
	// TODO Auto-generated method stub

    }

    @Override
    public void setStyle(Style style) {
	switch (style) {
	case FILL:
	    this.style = GL_Style.FILL;
	    break;
	case STROKE:
	    this.style = GL_Style.STROKE;
	    break;
	default:
	    this.style = GL_Style.FILL;
	    break;

	}
    }

    @Override
    public GL_Style getGL_Style() {
	return this.style;
    }

    public FontFamily getFontFamly() {
	return getFontFamily();
    }

    @Override
    public GL_FontStyle getGLFontStyle() {
	return fontStyle;
    }

    @Override
    public GL_FontFamily getGLFontFamily() {
	return fontFamily;
    }

    public void setFontFamily(GL_FontFamily fontFamily) {
	this.fontFamily = fontFamily;
    }

    public BitmapFont getFont() {
	if (this.font == null) {
	    this.font = FontCache.get(this.getGLFontFamily(), this.fontStyle, this.textSize);
	}

	return this.font;
    }

    @Override
    public Cap getCap() {
	switch (cap) {
	case BUTT:
	    return Cap.BUTT;
	case DEFAULT:
	    return Cap.ROUND;
	case ROUND:
	    return Cap.ROUND;
	case SQUARE:
	    return Cap.SQUARE;
	default:
	    return Cap.ROUND;

	}
    }

    @Override
    public int getColor() {
	return this.color.toIntBits();
    }

    public HSV_Color getGlColor() {
	return this.color;
    }

    @Override
    public Style getStyle() {
	switch (this.style) {
	case FILL:
	    return Style.FILL;
	case STROKE:
	    return Style.STROKE;
	default:
	    return Style.FILL;
	}
    }

    @Override
    public FontFamily getFontFamily() {
	switch (this.fontFamily) {
	case DEFAULT:
	    return FontFamily.DEFAULT;
	case MONOSPACE:
	    return FontFamily.MONOSPACE;
	case SANS_SERIF:
	    return FontFamily.SANS_SERIF;
	case SERIF:
	    return FontFamily.SERIF;
	default:
	    return FontFamily.DEFAULT;

	}
    }

    @Override
    public FontStyle getFontStyle() {
	switch (this.fontStyle) {
	case BOLD:
	    return FontStyle.BOLD;
	case BOLD_ITALIC:
	    return FontStyle.BOLD_ITALIC;
	case ITALIC:
	    return FontStyle.ITALIC;
	case NORMAL:
	    return FontStyle.NORMAL;
	default:
	    return FontStyle.NORMAL;

	}
    }

    public void setGLColor(Color color) {
	this.color = new HSV_Color(color);
    }

}
