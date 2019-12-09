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
package de.droidcachebox.gdx.graphics;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.graphics.mapsforge.GDXMatrix;
import de.droidcachebox.gdx.graphics.mapsforge.GDXPaint;
import org.mapsforge.core.graphics.Style;
import org.mapsforge.core.graphics.*;
import org.mapsforge.core.model.Point;

/**
 * @author Longri
 */
public class GL_Paint implements GDXPaint {
    protected HSV_Color hsvColor;
    protected GL_Align align;
    protected Join join;
    protected float strokeWidth = 1f;
    protected float textSize;
    GL_Cap cap;
    float[] strokeDasharray = null;
    private BitmapDrawable bitmapShader;
    private GL_FontStyle fontStyle = GL_FontStyle.NORMAL;
    private GlyphLayout layout;
    private GL_Style style;
    private GL_FontFamily fontFamily = GL_FontFamily.DEFAULT;
    private BitmapFont font;

    public GL_Paint() {
        cap = GL_Cap.BUTT;
        join = Join.MITER;
        hsvColor = new HSV_Color(com.badlogic.gdx.graphics.Color.BLACK);
        style = GL_Style.FILL;
    }

    /*
    public GL_Paint(Paint paint) {
        if (paint instanceof GL_Paint) {
            GL_Paint p = (GL_Paint) paint;
            cap = p.cap;
            join = p.join;
            color = new HSV_Color(p.color);
            style = p.style;
            textSize = p.textSize;
            strokeWidth = p.strokeWidth;
            strokeDasharray = p.strokeDasharray;
        } else {
            Cap cap = paint.getCap();
            switch (cap) {
                case BUTT:
                    cap = GL_Cap.BUTT;
                    break;
                case SQUARE:
                    cap = GL_Cap.SQUARE;
                    break;
                default:
                    cap = GL_Cap.ROUND;
                    break;
            }

            color = new HSV_Color(paint.getColor());

            Style style = paint.getStyle();
            if (style == Style.STROKE) {
                style = GL_Style.STROKE;
            } else {
                style = GL_Style.FILL;
            }
        }
        textSize = paint.getTextSize();
        strokeWidth = paint.getStrokeWidth();
        strokeDasharray = paint.getDashArray();
    }

     */

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
        strokeDasharray = strokeDasharray;
    }

    private void createFont() {
        if (textSize > 0) {
            GL.that.RunOnGL(() -> GL_Paint.this.font = FontCache.get(GL_Paint.this.getGLFontFamily(), GL_Paint.this.getGLFontStyle(), GL_Paint.this.getTextSize()));
        } else {
            font = null;
        }
    }

    @Override
    public boolean isTransparent() {
        return bitmapShader == null && hsvColor.a == 0;
    }

    @Override
    public float getStrokeWidth() {
        return strokeWidth;
    }

    @Override
    public void setStrokeWidth(float strokeWidth) {
        this.strokeWidth = strokeWidth;
    }

    BitmapDrawable getBitmapShader() {
        return bitmapShader;
    }

    @Override
    public void setBitmapShader(Bitmap bitmap) {
        if (bitmap instanceof BitmapDrawable) {
            bitmapShader = (BitmapDrawable) bitmap;
        }
    }

    // Override mapsforge getDashArray
    public float[] getDashArray() {
        // chk if DashArray empty, then return null
        if (strokeDasharray != null && strokeDasharray.length < 2)
            return null;
        return strokeDasharray;
    }

    @Override
    public void setAlpha(int i) {
        hsvColor.a = i / 255f;
    }

    @Override
    public void setStrokeJoin(Join join) {
        this.join = join;
    }

    @Override
    public void setRadialGradiant(float x, float y, float radius, int[] colors, float[] positions, TileMode tileMode) {

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
        return textSize;
    }

    @Override
    public void setTextSize(float textSize) {
        this.textSize = textSize;
        createFont();
    }

    @Override
    public void delDashPathEffect() {
        strokeDasharray = null;
    }

    @Override
    public GDXMatrix getGradiantMatrix() {

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
    public void setGradientMatrix(GDXMatrix matrix) {

    }

    @Override
    public GL_Style getGL_Style() {
        return style;
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
        if (font == null) {
            font = FontCache.get(getGLFontFamily(), fontStyle, textSize);
        }

        return font;
    }

    // @Override von mapsforge
    public Cap getCap() {
        switch (cap) {
            case BUTT:
                return Cap.BUTT;
            case SQUARE:
                return Cap.SQUARE;
            default:
                return Cap.ROUND;

        }
    }

    // @Override von mapsforge
    public Style getStyle() {
        if (style == GL_Style.STROKE) {
            return Style.STROKE;
        }
        return Style.FILL;
    }

    @Override
    public void setStyle(GL_Style style) {
        this.style = style;
    }

    @Override
    public void setStyle(Style style) {
        if (style == Style.STROKE) {
            this.style = GL_Style.STROKE;
        } else {
            this.style = GL_Style.FILL;
        }
    }

    @Override
    public void setBitmapShaderShift(Point origin) {
    }

    @Override
    public void setStrokeJoin(org.mapsforge.core.graphics.Join join) {
    }

    @Override
    public int getColor() {
        return hsvColor.toInt(); // ? argb // set 0
    }

    public void setColor(com.badlogic.gdx.graphics.Color color) {
        hsvColor = new HSV_Color(color);
    }

    HSV_Color getGlColor() {
        return hsvColor;
    }

    @Override
    public void setColor(int color) {
        hsvColor = new HSV_Color(color);
    }

    @Override
    public void setColor(org.mapsforge.core.graphics.Color color) {
        hsvColor = new HSV_Color(color2HSVColor(color));
    }

    private HSV_Color color2HSVColor(org.mapsforge.core.graphics.Color color) {
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

    public enum GL_Cap {
        BUTT, ROUND, SQUARE, DEFAULT
    }

    public enum GL_Align {
        CENTER, LEFT, RIGHT
    }

    public enum GL_FontStyle {
        BOLD, BOLD_ITALIC, ITALIC, NORMAL
    }

    public enum GL_FontFamily {
        DEFAULT, MONOSPACE, SANS_SERIF, SERIF
    }

    public enum GL_Style {
        FILL, STROKE
    }

}
