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

import org.mapsforge.core.graphics.ResourceBitmap;
import org.mapsforge.core.graphics.TileBitmap;

import CB_UI_Base.graphics.extendedIntrefaces.ext_Bitmap;
import CB_UI_Base.graphics.extendedIntrefaces.ext_Canvas;
import CB_UI_Base.graphics.extendedIntrefaces.ext_GraphicFactory;
import CB_UI_Base.graphics.extendedIntrefaces.ext_Matrix;
import CB_UI_Base.graphics.extendedIntrefaces.ext_Paint;
import CB_UI_Base.graphics.extendedIntrefaces.ext_Path;
import android.app.Application;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

/**
 * @author Longri
 */
public class ext_AndroidGraphicFactory extends AndroidGraphicFactory implements ext_GraphicFactory
{
	public static Application aplication;
	private final float ScaleFactor;

	public static void createInstance(Application app)
	{
		aplication = app;
		INSTANCE = new AndroidGraphicFactory(app);
	}

	protected ext_AndroidGraphicFactory(Application app)
	{
		super(app);
		aplication = app;
		DisplayMetrics metrics = new DisplayMetrics();
		((WindowManager) app.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(metrics);
		this.ScaleFactor = metrics.scaledDensity;
	}

	protected ext_AndroidGraphicFactory(float scaleFactor)
	{
		super(aplication);
		this.ScaleFactor = scaleFactor;
	}

	@Override
	public ext_Matrix createMatrix(ext_Matrix matrix)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ext_Paint createPaint(ext_Paint paint)
	{
		return new ext_AndroidPaint(paint);
	}

	@Override
	public int setColorAlpha(int color, float paintOpacity)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public ext_Bitmap createBitmap(int width, int height)
	{
		return new ext_AndroidBitmap(width, height);
	}

	@Override
	public TileBitmap createTileBitmap(int tileSize, boolean hasAlpha)
	{
		return new ext_AndroidTileBitmap(tileSize);
	}

	@Override
	public ext_Canvas createCanvas()
	{
		return new ext_AndroidCanvas();
	}

	@Override
	public ext_Path createPath()
	{
		return new ext_AndroidPath();
	}

	@Override
	public ResourceBitmap createResourceBitmap(InputStream inputStream, int hash) throws IOException
	{
		return new ext_AndroidResourceBitmap(inputStream, hash, this.ScaleFactor);
	}

	public static ext_GraphicFactory getInstance(float ScaleFactor)
	{
		if (FactoryList.containsKey(ScaleFactor)) return FactoryList.get(ScaleFactor);

		ext_AndroidGraphicFactory factory = new ext_AndroidGraphicFactory(ScaleFactor);
		FactoryList.put(ScaleFactor, factory);
		return factory;
	}

}
