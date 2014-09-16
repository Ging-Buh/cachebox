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
package CB_UI_Base.graphics.extendedIntrefaces;

import CB_UI_Base.graphics.GL_FontFamily;
import CB_UI_Base.graphics.GL_FontStyle;
import CB_UI_Base.graphics.GL_Style;
import CB_UI_Base.graphics.Join;
import CB_UI_Base.graphics.TileMode;
import CB_Utils.Util.HSV_Color;

/**
 * @author Longri
 */
public interface ext_Paint extends org.mapsforge.core.graphics.Paint
{

	void setAlpha(int i);

	void setStrokeJoin(Join join);

	void setRadialGradiant(float x, float y, float radius, int[] colors, float[] positions, TileMode tileMode);

	void setGradientMatrix(ext_Matrix matrix);

	void setLinearGradient(float x1, float y1, float x2, float y2, int[] colors, float[] positions, TileMode tileMode);

	GL_Style getGL_Style();

	@Override
	float getTextSize();

	void setDashPathEffect(float[] strokeDasharray, float offset);

	void delDashPathEffect(); // set null

	ext_Matrix getGradiantMatrix();

	void setStyle(GL_Style fill);

	GL_FontStyle getGLFontStyle();

	GL_FontFamily getGLFontFamily();

	HSV_Color getHSV_Color();

	@Override
	float getStrokeWidth();
}
