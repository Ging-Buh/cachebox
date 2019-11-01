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

import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.graphics.Images.BitmapDrawable;
import CB_UI_Base.graphics.extendedInterfaces.ext_Matrix;
import CB_UI_Base.graphics.extendedInterfaces.ext_Paint;
import CB_Utils.Util.HSV_Color;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import org.mapsforge.core.graphics.Style;
import org.mapsforge.core.graphics.*;
import org.mapsforge.core.model.Point;

/**
 * @author Longri
 */
public class GL_Paint implements ext_Paint {
    protected HSV_Color color;
    protected GL_Align align;
    protected GL_Cap cap;
    protected Join join;
    protected float[] strokeDasharray = null;
    protected float strokeWidth = 1f;
    protected float textSize;
    protected BitmapDrawable bitmapShader;
    protected GL_FontStyle fontStyle = GL_FontStyle.NORMAL;
    GlyphLayout layout;
    private GL_Style style;
    private GL_FontFamily fontFamily = GL_FontFamily.DEFAULT;
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

        if (layout == null)
            layout = new GlyphLayout();
        layout.setText(font, text);

        return (int) layout.height;

    }

    @Override
    public int getTextWidth(String text) {
        if (font == null)
            return 0;

        if (layout == null)
            layout = new GlyphLayout();
        layout.setText(font, text);

        return (int) layout.width;
    }

    @Override
    public void setDashPathEffect(float[] strokeDasharray) {
        this.strokeDasharray = strokeDasharray;
    }

    public void setTextAlign(GL_Align align) {
        this.align = align;
    }

    public void setTypeface(GL_FontFamily fontFamily, GL_FontStyle fontStyle) {
        this.setFontFamily(fontFamily);
        this.fontStyle = fontStyle;
        createFont();
    }

    private void createFont() {
        if (this.textSize > 0) {
            GL.that.RunOnGL(() -> GL_Paint.this.font = FontCache.get(GL_Paint.this.getGLFontFamily(), GL_Paint.this.getGLFontStyle(), GL_Paint.this.getTextSize()));

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

    @Override
    public void setStrokeWidth(float strokeWidth) {
        this.strokeWidth = strokeWidth;
    }

    public BitmapDrawable getBitmapShader() {
        return bitmapShader;
    }

    public void setBitmapShader(BitmapDrawable bitmap) {
        bitmapShader = bitmap;
    }

    @Override
    public void setBitmapShader(Bitmap bitmap) {
        if (bitmap instanceof BitmapDrawable) {
            bitmapShader = (BitmapDrawable) bitmap;
        }
    }

    public GL_Cap getGL_Cap() {
        return cap;
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

    }

    public void setGradientMatrix(GL_Matrix matrix) {

    }

    @Override
    public void setLinearGradient(float x1, float y1, float x2, float y2, int[] colors, float[] positions, TileMode tileMode) {

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
    public void setTextSize(float textSize) {
        this.textSize = textSize;
        createFont();
    }

    @Override
    public void delDashPathEffect() {
        this.strokeDasharray = null;
    }

    @Override
    public ext_Matrix getGradiantMatrix() {

        return null;
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

    }

    @Override
    public void setTypeface(FontFamily fontFamily, FontStyle fontStyle) {

    }

    @Override
    public void setGradientMatrix(ext_Matrix matrix) {

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

    public void setCap(GL_Cap cap) {
        this.cap = cap;
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
    public void setStyle(GL_Style style) {
        this.style = style;
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

    public void setFontFamily(GL_FontFamily fontFamily) {
        this.fontFamily = fontFamily;
    }

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

    //################################################
    @Override
    public void setBitmapShaderShift(Point origin) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setStrokeJoin(org.mapsforge.core.graphics.Join join) {
        // TODO Auto-generated method stub

    }

    @Override
    public int getColor() {
        // TODO Auto-generated method stub
        return 0;
    }

    public void setColor(Color color) {
        this.color = new HSV_Color(color);
    }

    @Override
    public void setColor(int color) {
        this.color = new HSV_Color(color);
    }

    @Override
    public void setColor(org.mapsforge.core.graphics.Color color) {
        this.color = new HSV_Color(getColor(color));
    }

    HSV_Color getColor(org.mapsforge.core.graphics.Color color) {
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
                return new HSV_Color(0, 0, 0, 0);
            case WHITE:
                return new HSV_Color(com.badlogic.gdx.graphics.Color.WHITE);
        }

        throw new IllegalArgumentException("unknown color: " + color);
    }

}
