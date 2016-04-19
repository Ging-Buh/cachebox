/* 
 * Copyright (C) 2015 team-cachebox.de
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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import org.slf4j.LoggerFactory;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.TextureLoader;
import com.badlogic.gdx.assets.loaders.resolvers.AbsoluteFileHandleResolver;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

import CB_UI_Base.CB_Texturepacker.Settings;
import CB_UI_Base.CB_Texturepacker.TexturePacker_Base;
import CB_UI_Base.GL_UI.IRunOnGL;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.GL_UI.Sprites.IconName;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.GL_UI.utils.GifDecoder;
import CB_UI_Base.Math.UI_Size_Base;
import CB_UI_Base.settings.CB_UI_Base_Settings;
import CB_Utils.Log.Log;
import CB_Utils.Util.Downloader;
import CB_Utils.Util.FileIO;
import CB_Utils.fileProvider.FileFactory;

/**
 * @author Longri
 */
public class ImageLoader {

	final static org.slf4j.Logger log = LoggerFactory.getLogger(ImageLoader.class);

	private final boolean thumbnail;
	boolean ImageLoadError = false;
	private int State = 0;
	private Thread loadingThread;
	private String mPath;
	private Texture mImageTex = null;
	private Drawable mDrawable = null;
	private Thread ImageDownloadThread;
	private boolean isPacking = false;
	private HashMap<String, TextureAtlas> Atlanten;
	private String AtlasPath;
	private String ImgName;
	private boolean inGenerate = false;
	private float frameCounter = 0;
	private SpriteDrawable animDrawable;
	private Sprite animSprite;

	private Animation anim;

	boolean inLoad = false;
	private float spriteWidth;
	private float spriteHeight;
	boolean reziseHeight;

	public ImageLoader() {
		thumbnail = false;
	}

	public ImageLoader(boolean thumb) {
		thumbnail = thumb;
	}

	public interface resize {
		public void sizechanged(float width, float height);
	}

	private resize resizeListener;
	private float resizeWidth = 0;

	public void setResizeListener(resize listener, float width) {
		resizeListener = listener;
		resizeWidth = width;
	}

	private String ThumbPräfix = "";

	public void setThumbWidth(float width, String präfix) {
		resizeWidth = width;
		ThumbPräfix = präfix;
	}

	public resize getResizeListener() {
		return resizeListener;
	}

	/**
	 * Pack the images from Folder into a Atlas and Load the Image from Atlas
	 */
	private void packImagesToTextureAtlas(String ImagePath, boolean reziseHeight) {
		if (isPacking)
			return;
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
		String outputFolder = CB_UI_Base_Settings.ImageCacheFolder.getValue();
		String Name = getCachedAtlasName(inputFolder);

		try {
			TexturePacker_Base.process(textureSettings, inputFolder, outputFolder, Name);
		} catch (Exception e) {
			e.printStackTrace();
			ImageLoadError = true;
		}

		Sprite spt = tryToLoadFromCreatedAtlas(ImagePath);
		if (spt != null)
			setSprite(spt, reziseHeight);

		isPacking = false;
		GL.that.renderOnce();
	}

	private String getCachedAtlasName(String inputFolder) {
		String Name = inputFolder.replace("/", "_");
		Name = Name.replace("\\", "_");
		Name = Name.replace(".", "");
		Name = Name + ".spp";
		return Name;
	}

	private Sprite tryToLoadFromCreatedAtlas(String ImagePath) {

		if (Atlanten == null)
			Atlanten = new HashMap<String, TextureAtlas>();

		String inputFolder = FileIO.GetDirectoryName(ImagePath);
		String ImageName = FileIO.GetFileNameWithoutExtension(ImagePath);
		String Name = getCachedAtlasName(inputFolder);

		final String AtlasPath = CB_UI_Base_Settings.ImageCacheFolder.getValue() + "/" + Name;
		if (!FileIO.FileExistsNotEmpty(AtlasPath))
			return null;
		TextureAtlas atlas = null;
		if (Atlanten.containsKey(AtlasPath)) {
			atlas = Atlanten.get(AtlasPath);
		} else {
			this.AtlasPath = AtlasPath;
			this.ImgName = ImageName;
			State = 6;
		}

		Sprite tmp = null;
		if (atlas != null) {
			tmp = atlas.createSprite(ImageName);
		}
		return tmp;

	}

	void setAtlas(String atlasPath, String imgName, boolean reziseHeight) {
		State = 7;
		TextureAtlas atlas = new TextureAtlas(Gdx.files.absolute(atlasPath));
		Atlanten.put(atlasPath, atlas);

		Sprite tmp = null;
		if (atlas != null) {
			tmp = atlas.createSprite(imgName);
		}

		if (tmp != null)
			setSprite(tmp, reziseHeight);
	}

	void ThreadLoad(final boolean reziseHeight) {
		State = 5;
		if (isPacking)
			return;

		if (loadingThread != null) {
			if (loadingThread.getState() != Thread.State.TERMINATED)
				return;
			else
				loadingThread = null;
		}

		loadingThread = new Thread(new Runnable() {

			@Override
			public void run() {
				Sprite spt = tryToLoadFromCreatedAtlas(mPath);

				if (spt != null) {
					setSprite(spt, reziseHeight);
				} else {
					packImagesToTextureAtlas(mPath, reziseHeight);
				}

			}
		});

		if (loadingThread.getState() == Thread.State.NEW)
			loadingThread.start();
	}

	public void setDrawable(Drawable drawable) {
		mDrawable = drawable;
		inLoad = false;
		GL.that.renderOnce();
	}

	/**
	 * Sets a Image URl and Downlowd this Image if this don't exist on Cache
	 *
	 * @param iconUrl
	 */
	public void setImageURL(final String iconUrl) {
		if (iconUrl == null)
			return;
		if (iconUrl.length() == 0)
			return;

		if (ImageDownloadThread != null) {
			if (ImageDownloadThread.getState() != Thread.State.TERMINATED)
				return;
			else
				ImageDownloadThread = null;
		}

		ImageDownloadThread = new Thread(new Runnable() {

			@Override
			public void run() {
				final String CachePath = FileFactory.createFile(CB_UI_Base_Settings.ImageCacheFolder.getValue()).getAbsolutePath();

				// Search first slash after Http or www
				int slashPos = -1;
				slashPos = iconUrl.indexOf("http");
				if (slashPos == -1)
					slashPos = iconUrl.indexOf("www");
				if (slashPos == -1)
					slashPos = iconUrl.indexOf("file");
				if (slashPos == -1) {
					ImageLoadError = true;
					return; // invalid URL
				}
				slashPos += 7;
				slashPos = iconUrl.indexOf("/", slashPos);

				final String LocalPath = iconUrl.substring(slashPos);

				// check if Image exist on Cache
				if (FileIO.FileExistsNotEmpty(CachePath + LocalPath)) {
					setImage(CachePath + LocalPath);
					return;
				}

				inLoad = true;

				// Download Image to Cache
				try {
					URL url = new URL(iconUrl);

					final Downloader dl = new Downloader(url, FileFactory.createFile(CachePath + LocalPath));

					Thread DLThread = new Thread(new Runnable() {
						@Override
						public void run() {
							dl.run();
							inLoad = false;

							// chk if Download complied
							if (!FileIO.FileExistsNotEmpty(CachePath + LocalPath)) {
								// Download Error
								ImageLoadError = true;
								return;
							}

							setImage(CachePath + LocalPath);
						}
					});

					DLThread.run();
				} catch (MalformedURLException e) {
					Log.err(log, "ImageDownloader wrong URL: " + iconUrl, e);
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		ImageDownloadThread.start();
		generate();
	}

	public void clearImage() {
		mDrawable = null;
		mPath = null;
	}

	public void setImage(String Path) {
		State = 3;
		mPath = Path.replace("file://", "");
		if (getDrawable(0) != null) {
			dispose();
			// das laden des Images in das Sprite darf erst in der Render Methode passieren, damit es aus dem GL_Thread herraus l�uft.
		}
		generate();
	}

	public void setSprite(Sprite sprite, boolean reziseHeight) {

		State = 7;
		inLoad = false;
		spriteWidth = sprite.getWidth();
		spriteHeight = sprite.getHeight();

		if (this.resizeListener != null) {
			float proportionWidth = resizeWidth / spriteWidth;
			if (proportionWidth > UI_Size_Base.that.getScale()) {
				proportionWidth = UI_Size_Base.that.getScale();
			}

			float newWidth = spriteWidth * proportionWidth;//* UI_Size_Base.that.getScale();
			float newHeight = spriteHeight * proportionWidth;//* UI_Size_Base.that.getScale();
			sprite.scale(proportionWidth);
			this.resizeListener.sizechanged(newWidth, newHeight);

		}

		mDrawable = new SpriteDrawable(sprite);
		GL.that.renderOnce();
	}

	private void generate() {
		inLoad = true;
		if (ImageLoadError) {
			setSprite(Sprites.getSprite(IconName.DELETE.name()), this.reziseHeight);
			ImageLoadError = false;
			return;
		}

		if (State == 3) {
			if (inGenerate)
				return;
			GL.that.RunOnGL(new IRunOnGL() {

				@Override
				public void run() {
					try {
						inGenerate = true;

						if (mPath.endsWith(".gif")) {
							anim = GifDecoder.loadGIFAnimation(PlayMode.LOOP, Gdx.files.absolute(mPath).read());
						} else {
							loadAsync();
						}

					} catch (com.badlogic.gdx.utils.GdxRuntimeException e) {
						ImageLoadError = true;
						Log.err(log, "Load GL Image", e);
						State = 4;
					} catch (Exception e) {
						ImageLoadError = true;
						Log.err(log, "Load GL Image", e);
						e.printStackTrace();
					}
					inGenerate = false;
				}
			});

			return;
		}

		if (State == 4)
			ThreadLoad(reziseHeight);

		if (State == 6)
			setAtlas(this.AtlasPath, this.ImgName, reziseHeight);
	}

	private static AssetManager assetManager = new AssetManager();

	private void loadAsync() {

		Thread th = new Thread(new Runnable() {

			@Override
			public void run() {

				Log.info(log, "LoadAsync " + mPath + ":" + ImgName);

				if (thumbnail)
					createThumb();
				final TextureLoader tl = new TextureLoader(new AbsoluteFileHandleResolver());
				tl.loadAsync(assetManager, ImgName, Gdx.files.absolute(mPath), null);

				GL.that.RunOnGL(new IRunOnGL() {

					@Override
					public void run() {
						Log.info(log, "LoadSync " + mPath + ":" + ImgName);
						mImageTex = tl.loadSync(assetManager, ImgName, Gdx.files.absolute(mPath), null);
						Sprite sprite = new com.badlogic.gdx.graphics.g2d.Sprite(mImageTex);
						spriteWidth = sprite.getWidth();
						spriteHeight = sprite.getHeight();
						setSprite(sprite, reziseHeight);
						Log.info(log, "LoadSync " + mPath + ":" + ImgName + " ready");
					}
				});
			}
		});
		th.start();
	}

	private String originalPath = null;

	private void createThumb() {
		if (ThumbPräfix.length() > 0)
			Log.info(log, "createThumb " + FileIO.GetFileName(mPath));
		else
			Log.info(log, "create" + ThumbPräfix + "Thumb " + FileIO.GetFileName(mPath));
		String tmp = FileFactory.createThumb(mPath, (int) resizeWidth, ThumbPräfix);
		if (tmp != null) {
			originalPath = mPath;
			mPath = tmp;
		} else {
			Log.err(log, "Thumb not generated for " + mPath + " ! " + ThumbPräfix);
		}
	}

	/**
	 * Returns the drawable was is loaded.
	 * <p>
	 * If the loaded image a Animation, like GIF, so returns the drawable for the given KeyFrame.
	 *
	 * @param keyFrame
	 * @return
	 */
	public Drawable getDrawable(float keyFrame) {
		if (anim != null) {
			frameCounter += keyFrame;
			TextureRegion tex = anim.getKeyFrame(frameCounter);

			if (animSprite == null) {
				animSprite = new com.badlogic.gdx.graphics.g2d.Sprite(tex);
				spriteWidth = animSprite.getWidth();
				spriteHeight = animSprite.getHeight();

				if (this.resizeListener != null) {
					float proportionWidth = resizeWidth / spriteWidth;
					if (proportionWidth > UI_Size_Base.that.getScale()) {
						proportionWidth = UI_Size_Base.that.getScale();
					}

					float newWidth = spriteWidth * proportionWidth;
					float newHeight = spriteHeight * proportionWidth;
					animSprite.scale(proportionWidth);
					this.resizeListener.sizechanged(newWidth, newHeight);

				}

			} else {
				animSprite.setRegion(tex);
			}

			if (animDrawable == null) {
				animDrawable = new SpriteDrawable(animSprite);
			} else {
				animDrawable.setSprite(animSprite);
			}

			return animDrawable;
		}
		return mDrawable;
	}

	public boolean isDrawableNULL() {
		if (mDrawable == null && anim == null)
			return true;
		return false;
	}

	public int getAnimDelay() {
		if (anim != null) {
			return (int) (anim.getFrameDuration() * 1000);
		}
		return 0;
	}

	public void dispose() {

		GL.that.RunOnGL(new IRunOnGL() {

			@Override
			public void run() {
				if (mImageTex != null) {
					try {
						assetManager.unload(ImgName);
					} catch (Exception e) {
					}
					mImageTex.dispose();
				}

				mImageTex = null;
				mDrawable = null;
				loadingThread = null;
				mPath = null;

				ImageDownloadThread = null;
				if (Atlanten != null) {
					Atlanten.clear();
					Atlanten = null;
				}

				AtlasPath = null;
				ImgName = null;
				animDrawable = null;

				animSprite = null;
				anim = null;
			}
		});
	}

	public float getSpriteWidth() {
		return spriteWidth;
	}

	public void setSpriteWidth(float spriteWidth) {
		this.spriteWidth = spriteWidth;
	}

	public float getSpriteHeight() {
		return spriteHeight;
	}

	public void setSpriteHeight(float spriteHeight) {
		this.spriteHeight = spriteHeight;
	}

	public String getImagePath() {
		return mPath;
	}

	public String getOriginalImagePath() {
		if (originalPath == null)
			return mPath;
		return originalPath;
	}

}