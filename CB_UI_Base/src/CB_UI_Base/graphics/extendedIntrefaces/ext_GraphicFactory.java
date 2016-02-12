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

import java.util.HashMap;

import org.mapsforge.core.graphics.Matrix;
import org.mapsforge.core.graphics.Paint;

/**
 * @author Longri
 */
public interface ext_GraphicFactory extends org.mapsforge.core.graphics.GraphicFactory {

	final static HashMap<Float, ext_GraphicFactory> FactoryList = new HashMap<Float, ext_GraphicFactory>();

	@Override
	public abstract ext_Bitmap createBitmap(int width, int height);

	@Override
	public abstract Matrix createMatrix();

	@Override
	public abstract ext_Path createPath();

	@Override
	public abstract Paint createPaint();

	@Override
	public abstract ext_Canvas createCanvas();

	public abstract ext_Matrix createMatrix(ext_Matrix matrix);

	public abstract ext_Paint createPaint(ext_Paint paint);

	public abstract int setColorAlpha(int color, float paintOpacity);

}
