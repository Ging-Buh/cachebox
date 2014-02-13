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
package CB_UI_Base.graphics.Images;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import CB_UI_Base.GL_UI.IRunOnGL;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.GL_UI.utils.EmptyDrawable;
import CB_UI_Base.graphics.extendedIntrefaces.ext_Bitmap;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Gdx2DPixmap;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;

/**
 * @author Longri
 */
public class BitmapDrawable extends EmptyDrawable implements ext_Bitmap, Disposable
{
	/**
	 * @uml.property name="tex"
	 * @uml.associationEnd
	 */
	protected Texture tex;
	/**
	 * @uml.property name="buffer" multiplicity="(0 -1)" dimension="1"
	 */
	private byte[] buffer;
	/**
	 * @uml.property name="isDisposed"
	 */
	private boolean isDisposed = false;
	/**
	 * @uml.property name="scaleFactor"
	 */
	private final float scaleFactor;

	protected BitmapDrawable()
	{
		scaleFactor = 1;
	}

	public BitmapDrawable(InputStream stream, float scaleFactor)
	{
		this.scaleFactor = scaleFactor;
		try
		{
			int length = stream.available();
			if (length == 0) length = 512;
			buffer = new byte[length];
			int position = 0;

			while (true)
			{
				int count = stream.read(buffer, position, buffer.length - position);
				if (count == -1) break;
				position += count;
				if (position == buffer.length)
				{
					int b = stream.read();
					if (b == -1) break;
					// Grow buffer.
					byte[] newBuffer = new byte[buffer.length * 2];
					System.arraycopy(buffer, 0, newBuffer, 0, position);
					buffer = newBuffer;
					buffer[position++] = (byte) b;
				}
			}

			if (position < buffer.length)
			{
				// Shrink buffer.
				byte[] newBuffer = new byte[position];
				System.arraycopy(buffer, 0, newBuffer, 0, position);
				buffer = newBuffer;
			}

		}
		catch (IOException ex)
		{
			throw new GdxRuntimeException("Error reading file: " + this, ex);
		}
		finally
		{
			try
			{
				if (stream != null) stream.close();
			}
			catch (IOException ignored)
			{
			}
		}

		GL.that.RunOnGL(new IRunOnGL()
		{

			@Override
			public void run()
			{
				prepare();
			}
		});

	}

	public BitmapDrawable(Texture texture)
	{
		scaleFactor = 1f;
		tex = texture;
	}

	public BitmapDrawable(final Pixmap p)
	{
		scaleFactor = 1f;
		GL.that.RunOnGL(new IRunOnGL()
		{

			@Override
			public void run()
			{
				tex = new Texture(p);
				p.dispose();
			}
		});

	}

	@Override
	public int getHeight()
	{
		return tex == null ? 0 : tex.getHeight();
	}

	@Override
	public int getWidth()
	{

		return tex == null ? 0 : tex.getWidth();
	}

	public boolean isPrepared()
	{
		return tex != null && buffer == null;
	}

	@Override
	public void draw(SpriteBatch batch, float x, float y, float width, float height)
	{

		if (tex != null && batch != null)
		{

			batch.draw(tex, x, y, getWidth(), getHeight());
		}

	}

	public Texture getTexture()
	{
		if (!isPrepared()) prepare();
		return tex;
	}

	public void prepare()
	{
		if (tex == null && buffer != null)
		{

			// ################ Create Pixmap from Buffer #################################

			Pixmap pixmap = null;
			try
			{
				pixmap = new Pixmap(new Gdx2DPixmap(buffer, 0, buffer.length, 0));
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}

			// ################ Last step, create Texture from Pixmap #################################

			if (pixmap != null)
			{

				if (this.scaleFactor != 1f)
				{
					Pixmap scPic = new Pixmap((int) (pixmap.getWidth() * scaleFactor), (int) (pixmap.getHeight() * scaleFactor),
							pixmap.getFormat());
					scPic.drawPixmap(pixmap, 0, 0, pixmap.getWidth(), pixmap.getHeight(), 0, 0, (int) (pixmap.getWidth() * scaleFactor),
							(int) (pixmap.getHeight() * scaleFactor));
					tex = new Texture(scPic);
					pixmap.dispose();
					scPic.dispose();
					buffer = null;
				}
				else
				{
					tex = new Texture(pixmap);
					pixmap.dispose();
					buffer = null;
				}

			}
		}

	}

	@Override
	public void compress(OutputStream outputStream) throws IOException
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void incrementRefCount()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void decrementRefCount()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void scaleTo(int width, int height)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void setBackgroundColor(int color)
	{
		// TODO Auto-generated method stub

	}

	/**
	 * @return
	 * @uml.property name="isDisposed"
	 */
	public boolean isDisposed()
	{
		return isDisposed;
	}

	@Override
	public void dispose()
	{
		tex = null;
		buffer = null;
		isDisposed = true;
	}

	@Override
	public void recycle()
	{
		dispose();
	}

	@Override
	public void getPixels(int[] maskBuf, int i, int w, int j, int y, int w2, int k)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void setPixels(int[] maskedContentBuf, int i, int w, int j, int y, int w2, int k)
	{
		// TODO Auto-generated method stub

	}
}
