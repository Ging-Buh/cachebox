/* 
 * Copyright (C) 2011-2012 team-cachebox.de
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

package CB_UI_Base.GL_UI.Controls;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import CB_UI_Base.CB_Texturepacker.Settings;
import CB_UI_Base.CB_Texturepacker.TexturePacker_Base;
import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.SpriteCacheBase;
import CB_UI_Base.GL_UI.SpriteCacheBase.IconName;
import CB_UI_Base.GL_UI.IRunOnGL;
import CB_UI_Base.GL_UI.Controls.Animation.AnimationBase;
import CB_UI_Base.GL_UI.Controls.Animation.WorkAnimation;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.settings.CB_UI_Base_Settings;
import CB_Utils.Log.Logger;
import CB_Utils.Util.Downloader;
import CB_Utils.Util.FileIO;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

/**
 * Ein Control, welches ein Bild aus einem Pfad Darstellt.
 * 
 * @author Longri
 */
public class Image extends CB_View_Base
{

	private AnimationBase Wait;

	private Color mColor = new Color(1, 1, 1, 1);
	private float spriteWidth = -1;
	private float spriteHeight = -1;
	private boolean inLoad = false;
	private boolean ImageLoadError = false;

	private int State = 0;

	public Image(float X, float Y, float Width, float Height, String Name)
	{
		super(X, Y, Width, Height, Name);
	}

	public Image(CB_RectF rec, String Name)
	{
		super(rec, Name);
	}

	@Override
	protected void render(SpriteBatch batch)
	{
		if (ImageLoadError)
		{
			// TODO Draw error Image
			if (Wait != null)
			{
				GL.that.removeRenderView(Wait);
				this.removeChild(Wait);
				Wait = null;
			}

			setSprite(SpriteCacheBase.Icons.get(IconName.delete_28.ordinal()));
			ImageLoadError = false;
			return;
		}

		if (State == 3)
		{
			try
			{

				mImageTex = new Texture(Gdx.files.absolute(mPath));
				Sprite sprite = new com.badlogic.gdx.graphics.g2d.Sprite(mImageTex);

				spriteWidth = sprite.getWidth();
				spriteHeight = sprite.getHeight();

				setSprite(sprite);

			}
			catch (com.badlogic.gdx.utils.GdxRuntimeException e)
			{
				State = 4;
			}
			catch (Exception e)
			{
				ImageLoadError = true;
				Logger.LogCat("E Load GL Image" + e.getMessage());
				e.printStackTrace();
			}
			return;
		}

		if (State == 4) ThreadLoad();

		if (State == 6) setAtlas(this.AtlasPath, this.ImgName);

		Color altColor = batch.getColor().cpy();

		batch.setColor(mColor);

		if (mDrawable != null)
		{
			if (Wait != null)
			{
				GL.that.removeRenderView(Wait);
				this.removeChild(Wait);
				Wait = null;
			}
			inLoad = false;
			float drawwidth = width;
			float drawHeight = height;
			float drawX = 0;
			float drawY = 0;

			if (spriteWidth > 0 && spriteHeight > 0)
			{
				float proportionWidth = width / spriteWidth;
				float proportionHeight = height / spriteHeight;

				float proportion = Math.min(proportionWidth, proportionHeight);

				drawwidth = spriteWidth * proportion;
				drawHeight = spriteHeight * proportion;
				drawX = (width - drawwidth) / 2;
				drawY = (height - drawHeight) / 2;
			}

			mDrawable.draw(batch, drawX, drawY, drawwidth, drawHeight);

		}
		else if (inLoad)
		{
			if (Wait == null)
			{
				CB_RectF animationRec = new CB_RectF(0, 0, this.width, this.height);
				Wait = WorkAnimation.GetINSTANCE(animationRec);
				GL.that.addRenderView(Wait, GL.FRAME_RATE_ACTION);
				this.addChild(Wait);
			}

			GL.that.renderOnce("Image Loading Animation");
		}

		batch.setColor(altColor);
	}

	private Thread loadingThread;

	private void ThreadLoad()
	{
		State = 5;
		if (isPacking) return;

		if (loadingThread != null)
		{
			if (loadingThread.getState() != Thread.State.TERMINATED) return;
			else
				loadingThread = null;
		}

		loadingThread = new Thread(new Runnable()
		{

			@Override
			public void run()
			{
				Sprite spt = tryToLoadFromCreatetdAtlas(mPath);

				if (spt != null)
				{
					setSprite(spt);
				}
				else
				{
					packImagesToTextureAtlas(mPath);
				}

			}
		});

		if (loadingThread.getState() == Thread.State.NEW) loadingThread.start();
	}

	private String mPath;
	private Texture mImageTex = null;
	Drawable mDrawable = null;

	public void setImage(String Path)
	{
		State = 3;
		mPath = Path;
		if (mDrawable != null)
		{
			dispose();
			// das laden des Images in das Sprite darf erst in der Render Methode passieren, damit es aus dem GL_Thread herraus läuft.
		}
		GL.that.renderOnce("Image");
	}

	public void setDrawable(Drawable drawable)
	{
		mDrawable = drawable;
		inLoad = false;
		GL.that.renderOnce("Image");
	}

	@Override
	public void dispose()
	{
		GL.that.RunOnGL(new IRunOnGL()
		{

			@Override
			public void run()
			{
				if (mImageTex != null) mImageTex.dispose();
				mImageTex = null;
				mDrawable = null;
			}
		});
	}

	@Override
	protected void Initial()
	{
	}

	@Override
	protected void SkinIsChanged()
	{

	}

	public void setColor(Color color)
	{
		if (color == null) mColor = new Color(1, 1, 1, 1);
		else
			mColor = color;
	}

	private Thread ImageDownloadThread;

	/**
	 * Sets a Image URl and Downlowd this Image if this don't exist on Cache
	 * 
	 * @param iconUrl
	 */
	public void setImageURL(final String iconUrl)
	{
		if (iconUrl == null) return;
		if (iconUrl.length() == 0) return;

		if (ImageDownloadThread != null)
		{
			if (ImageDownloadThread.getState() != Thread.State.TERMINATED) return;
			else
				ImageDownloadThread = null;
		}

		ImageDownloadThread = new Thread(new Runnable()
		{

			@Override
			public void run()
			{
				final String CachePath = CB_UI_Base_Settings.ImageCacheFolderLocal.getValue();

				// Search first slash after Http or www
				int slashPos = -1;
				slashPos = iconUrl.indexOf("http");
				if (slashPos == -1) slashPos = iconUrl.indexOf("www");
				if (slashPos == -1) return; // invalid URL
				slashPos += 7;
				slashPos = iconUrl.indexOf("/", slashPos);

				final String LocalPath = iconUrl.substring(slashPos);

				// check if Image exist on Cache
				if (FileIO.FileExists(CachePath + LocalPath))
				{
					setImage(CachePath + LocalPath);
					return;
				}

				inLoad = true;

				// Download Image to Cache
				try
				{
					final Downloader dl = new Downloader(new URL(iconUrl), new File(CachePath + LocalPath));

					Thread DLThread = new Thread(new Runnable()
					{
						@Override
						public void run()
						{
							dl.run();
							inLoad = false;

							// chk if Download complied
							if (!FileIO.FileExists(CachePath + LocalPath))
							{
								// Download Error
								ImageLoadError = true;
								return;
							}

							setImage(CachePath + LocalPath);
						}
					});

					DLThread.run();
				}
				catch (MalformedURLException e)
				{
					e.printStackTrace();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		});
		ImageDownloadThread.start();

		GL.that.renderOnce("Image");
	}

	public void setSprite(Sprite sprite)
	{
		State = 7;
		inLoad = false;
		spriteWidth = sprite.getWidth();
		spriteHeight = sprite.getHeight();
		mDrawable = new SpriteDrawable(sprite);
		GL.that.renderOnce("Image");
	}

	public void clearImage()
	{
		mDrawable = null;
		mColor = new Color(1, 1, 1, 1);
		mPath = null;
		mScale = 1;
		setOriginCenter();
	}

	private boolean isPacking = false;

	/**
	 * Pack the images from Folder into a Atlas and Load the Image from Atlas
	 */
	private void packImagesToTextureAtlas(String ImagePath)
	{
		if (isPacking) return;
		isPacking = true;

		Settings textureSettings = new Settings();

		textureSettings.pot = true;
		textureSettings.paddingX = 2;
		textureSettings.paddingY = 2;
		textureSettings.duplicatePadding = true;
		textureSettings.edgePadding = true;
		textureSettings.rotation = false;
		textureSettings.minWidth = 16;
		textureSettings.minHeight = 16;
		textureSettings.maxWidth = 2048;
		textureSettings.maxHeight = 2048;
		textureSettings.stripWhitespaceX = false;
		textureSettings.stripWhitespaceY = false;
		textureSettings.alphaThreshold = 0;
		textureSettings.filterMin = TextureFilter.Linear;
		textureSettings.filterMag = TextureFilter.Linear;
		textureSettings.wrapX = TextureWrap.ClampToEdge;
		textureSettings.wrapY = TextureWrap.ClampToEdge;
		textureSettings.format = Format.RGBA8888;
		textureSettings.alias = true;
		textureSettings.outputFormat = "png";
		textureSettings.jpegQuality = 0.9f;
		textureSettings.ignoreBlankImages = true;
		textureSettings.fast = false;
		textureSettings.debug = false;

		String inputFolder = FileIO.GetDirectoryName(ImagePath);
		String outputFolder = CB_UI_Base_Settings.ImageCacheFolderLocal.getValue();
		String Name = getCachedAtlasName(inputFolder);

		try
		{
			TexturePacker_Base.process(textureSettings, inputFolder, outputFolder, Name);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		Sprite spt = tryToLoadFromCreatetdAtlas(ImagePath);
		if (spt != null) setSprite(spt);

		isPacking = false;
		GL.that.renderOnce("Image");
	}

	private String getCachedAtlasName(String inputFolder)
	{
		String Name = inputFolder.replace("/", "_");
		Name = Name.replace("\\", "_");
		Name = Name.replace(".", "");
		Name = Name + ".spp";
		return Name;
	}

	private HashMap<String, TextureAtlas> Atlanten;

	private Sprite tryToLoadFromCreatetdAtlas(String ImagePath)
	{

		if (Atlanten == null) Atlanten = new HashMap<String, TextureAtlas>();

		String inputFolder = FileIO.GetDirectoryName(ImagePath);
		String ImageName = FileIO.GetFileNameWithoutExtension(ImagePath);
		String Name = getCachedAtlasName(inputFolder);

		final String AtlasPath = CB_UI_Base_Settings.ImageCacheFolderLocal.getValue() + "/" + Name;
		if (!FileIO.FileExists(AtlasPath)) return null;
		TextureAtlas atlas = null;
		if (Atlanten.containsKey(AtlasPath))
		{
			atlas = Atlanten.get(AtlasPath);
		}
		else
		{
			this.AtlasPath = AtlasPath;
			this.ImgName = ImageName;
			State = 6;
		}

		Sprite tmp = null;
		if (atlas != null)
		{
			tmp = atlas.createSprite(ImageName);
		}
		return tmp;

	}

	private String AtlasPath;
	private String ImgName;

	private void setAtlas(String atlasPath, String imgName)
	{
		State = 7;
		TextureAtlas atlas = new TextureAtlas(Gdx.files.absolute(atlasPath));
		Atlanten.put(atlasPath, atlas);

		Sprite tmp = null;
		if (atlas != null)
		{
			tmp = atlas.createSprite(imgName);
		}

		if (tmp != null) setSprite(tmp);
	}
}
