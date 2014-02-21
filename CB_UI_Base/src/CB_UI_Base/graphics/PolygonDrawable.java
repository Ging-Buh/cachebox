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

import java.util.concurrent.atomic.AtomicBoolean;

import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.graphics.Images.IRotateDrawable;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Matrix4;

/**
 * @author Longri
 */
public class PolygonDrawable implements IRotateDrawable
{
	public String DEBUG_NAME;
	protected GL_Paint PAINT;
	protected final float WIDTH, HEIGHT;
	protected float[] VERTICES;
	protected short[] TRIANGLES;
	protected TextureRegion texReg;
	protected Texture tex;
	protected Pixmap pix;
	protected PolygonRegion po;
	private final AtomicBoolean isDisposed = new AtomicBoolean(false);

	public PolygonDrawable(final float[] vertices, final short[] triangles, final GL_Paint paint, float width, float height)
	{
		this(paint, width, height);
		this.VERTICES = new float[vertices.length];
		System.arraycopy(vertices, 0, this.VERTICES, 0, vertices.length);
		this.TRIANGLES = new short[triangles.length];
		System.arraycopy(triangles, 0, this.TRIANGLES, 0, triangles.length);
	}

	protected PolygonDrawable(final GL_Paint paint, float width, float height)
	{
		this.PAINT = paint;
		this.WIDTH = width;
		this.HEIGHT = height;
	}

	@Override
	public void draw(Batch batch, float x, float y, float width, float height, float rotate)
	{
		synchronized (isDisposed)
		{

			if (isDisposed.get()) return;

			if (po == null)
			{

				if (this.PAINT.getBitmapShader() == null)
				{
					if (texReg == null) createTexRegFromPixMap();
					po = new PolygonRegion(texReg, VERTICES, TRIANGLES);
				}
				else
				{
					Texture inputTex = this.PAINT.getBitmapShader().getTexture();
					if (inputTex != null)
					{
						inputTex.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
						po = new PolygonRegion(new TextureRegion(inputTex, (int) this.WIDTH, (int) this.HEIGHT), VERTICES, TRIANGLES);
					}
				}

			}

			if (po == null) return;

			Matrix4 projection = batch.getProjectionMatrix();
			batch.end();

			GL.polygonBatch.begin();
			GL.polygonBatch.setProjectionMatrix(projection);

			{
				if (this.PAINT.getBitmapShader() == null)
				{
					GL.polygonBatch.setColor(PAINT.getGlColor());
				}
				else
				{
					GL.polygonBatch.setColor(new Color(Color.WHITE));
				}

				try
				{
					GL.polygonBatch.draw(po, x, y, width, height);
				}
				catch (Exception e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			GL.polygonBatch.end();

			GL.polygonBatch.flush();

			batch.begin();
		}
	}

	private void createTexRegFromPixMap()
	{
		if (isDisposed.get()) return;
		int w = 2;
		int h = 2;
		pix = new Pixmap(w, h, Pixmap.Format.RGB565);
		pix.setColor(new Color(Color.WHITE));

		pix.fillRectangle(0, 0, w, h);

		try
		{
			tex = new Texture(pix);
		}
		catch (Exception e)
		{
			tex = null;
		}

		if (tex != null)
		{
			tex.setFilter(TextureFilter.Linear, TextureFilter.MipMapLinearLinear);
			texReg = new TextureRegion(tex, (int) this.WIDTH, (int) this.HEIGHT);

		}

		pix.dispose();
		pix = null;
	}

	public boolean isDisposed()
	{
		return isDisposed.get();
	}

	@Override
	public void dispose()
	{
		synchronized (isDisposed)
		{
			if (isDisposed.get()) return;
			PAINT = null;

			VERTICES = null;
			TRIANGLES = null;
			texReg = null;
			if (tex != null) tex.dispose();
			tex = null;
			if (pix != null) pix.dispose();
			pix = null;
			po = null;
			isDisposed.set(true);
		}
	}
}
