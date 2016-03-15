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
package CB_Locator.Map;

import org.slf4j.LoggerFactory;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;

import CB_UI_Base.GL_UI.IRunOnGL;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.settings.CB_UI_Base_Settings;
import CB_Utils.Lists.CB_List;
import CB_Utils.Log.Log;

/**
 * @author ging-buh
 * @author Longri
 */
public class TileGL_Bmp extends TileGL {
	final static org.slf4j.Logger log = LoggerFactory.getLogger(TileGL_Bmp.class);
	public static int LifeCount;
	private Texture texture = null;
	private byte[] bytes;
	private boolean inCreation = false;
	private final Format format;

	public TileGL_Bmp(Descriptor desc, byte[] bytes, TileState state, Format format) {
		Descriptor = desc;
		this.texture = null;
		this.bytes = bytes;
		this.format = format;
		State = state;
		LifeCount++;
		createTexture();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see CB_Locator.Map.TileGL#canDraw()
	 */
	@Override
	public boolean canDraw() {
		if (texture != null)
			return true;
		if (bytes == null)
			return false;
		if (inCreation)
			return false;
		createTexture();
		if (texture != null)
			return true;
		return false;
	}

	private void createTexture() {
		if (inCreation)
			return;
		inCreation = true;

		if (GL.isGlThread()) {
			if (texture != null)
				return;
			if (bytes == null)
				return;
			try {
				Pixmap pixmap = new Pixmap(bytes, 0, bytes.length);
				texture = new Texture(pixmap, format, CB_UI_Base_Settings.useMipMap.getValue());
				pixmap.dispose();
				pixmap = null;
			} catch (Exception ex) {
				Log.debug(log, "[TileGL] can't create Pixmap or Texture: " + ex.getMessage());
			}
			bytes = null;
			inCreation = false;
		} else {
			// create Texture on next GlThread
			GL.that.RunOnGL(new IRunOnGL() {
				@Override
				public void run() {
					if (isDisposed)
						return;
					if (texture != null)
						return;
					if (bytes == null)
						return;
					try {
						Pixmap pixmap = new Pixmap(bytes, 0, bytes.length);
						texture = new Texture(pixmap, format, CB_UI_Base_Settings.useMipMap.getValue());
						pixmap.dispose();
						pixmap = null;
					} catch (Exception ex) {
						Log.debug(log, "[TileGL] can't create Pixmap or Texture: " + ex.getMessage());
					}
					bytes = null;
					inCreation = false;
					GL.that.renderOnce();
				}
			});
		}

	}

	@Override
	public String toString() {
		return "[Age: " + Age + " " + State.toString() + ", " + Descriptor.ToString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see CB_Locator.Map.TileGL#draw(com.badlogic.gdx.graphics.g2d.SpriteBatch, float, float, float, float)
	 */
	@Override
	public void draw(Batch batch, float x, float y, float width, float height, CB_List<TileGL_RotateDrawables> rotateList) {
		if (texture != null)
			batch.draw(texture, x, y, width, height);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see CB_Locator.Map.TileGL#getWidth()
	 */
	@Override
	public long getWidth() {
		if (texture != null)
			return texture.getWidth();
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see CB_Locator.Map.TileGL#getHeight()
	 */
	@Override
	public long getHeight() {
		if (texture != null)
			return texture.getHeight();
		return 0;
	}

	@Override
	public void dispose() {
		if (isDisposed)
			return;

		if (GL.isGlThread()) {
			try {
				if (texture != null)
					texture.dispose();
			} catch (java.lang.NullPointerException e) {
				e.printStackTrace();
			}
			texture = null;
		} else {
			GL.that.RunOnGL(new IRunOnGL() {

				@Override
				public void run() {
					try {
						if (texture != null)
							texture.dispose();
					} catch (java.lang.NullPointerException e) {
						e.printStackTrace();
					}
					texture = null;
				}
			});
		}

		bytes = null;
		LifeCount--;
		isDisposed = true;
	}

	@Override
	public boolean isDisposed() {
		return isDisposed;
	}

}
