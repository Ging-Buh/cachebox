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
package de.droidcachebox.gdx.graphics.mapsforge;

import de.droidcachebox.gdx.graphics.GL_Paint;
import de.droidcachebox.gdx.graphics.HSV_Color;
import de.droidcachebox.gdx.graphics.Join;
import de.droidcachebox.gdx.graphics.TileMode;

/**
 * @author Longri
 */
public interface ext_Paint extends org.mapsforge.core.graphics.Paint {

    void setAlpha(int i);

    void setStrokeJoin(Join join);

    void setRadialGradiant(float x, float y, float radius, int[] colors, float[] positions, TileMode tileMode);

    void setGradientMatrix(ext_Matrix matrix);

    void setLinearGradient(float x1, float y1, float x2, float y2, int[] colors, float[] positions, TileMode tileMode);

    GL_Paint.GL_Style getGL_Style();

    float getTextSize();

    void setDashPathEffect(float[] strokeDasharray, float offset);

    void delDashPathEffect(); // set null

    ext_Matrix getGradiantMatrix();

    void setStyle(GL_Paint.GL_Style fill);

    GL_Paint.GL_FontStyle getGLFontStyle();

    GL_Paint.GL_FontFamily getGLFontFamily();

    HSV_Color getHSV_Color();

    @Override
    float getStrokeWidth();
}
