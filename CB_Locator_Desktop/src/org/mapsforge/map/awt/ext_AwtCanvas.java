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

import org.mapsforge.core.graphics.Matrix;
import org.mapsforge.core.graphics.Paint;

import CB_UI_Base.graphics.extendedIntrefaces.ext_Canvas;
import CB_UI_Base.graphics.extendedIntrefaces.ext_Matrix;
import CB_UI_Base.graphics.extendedIntrefaces.ext_Paint;
import CB_UI_Base.graphics.extendedIntrefaces.ext_Path;
import CB_UI_Base.graphics.fromAndroid.RectF;

/**
 * Extends the original Mapsforge AwtCanvas with the interface ext_Canvas
 * 
 * @author Longri
 */
public class ext_AwtCanvas extends AwtCanvas implements ext_Canvas
{

	@Override
	public void drawText(String text, float x, float y, Paint paint)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void drawRect(RectF rect, ext_Paint strokePaint)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void drawRoundRect(RectF rect, float rx, float ry, ext_Paint strokePaint)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void drawOval(RectF rect, ext_Paint fillPaint)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void scale(float sx, float sy)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void setMatrix(ext_Matrix matrix)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public ext_Matrix getMatrix()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void save()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void restore()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void concat(ext_Matrix matrix)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void drawTextOnPath(String text, ext_Path path, float x, float y, ext_Paint fillPaint)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void clipRect(float left, float top, float right, float bottom)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void clipPath(ext_Path path)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void translate(float stepX, float stepY)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void saveMatrix()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void setMatrix(Matrix matrix)
	{
		// TODO Auto-generated method stub

	}

}
