/* 
 * Copyright (C) 2011-2015 team-cachebox.de
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

import org.slf4j.LoggerFactory;

import CB_UI_Base.CB_Texturepacker.Settings;
import CB_UI_Base.CB_Texturepacker.TexturePacker_Base;
import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.IRunOnGL;
import CB_UI_Base.GL_UI.SpriteCacheBase;
import CB_UI_Base.GL_UI.SpriteCacheBase.IconName;
import CB_UI_Base.GL_UI.Controls.Label.HAlignment;
import CB_UI_Base.GL_UI.Controls.Animation.AnimationBase;
import CB_UI_Base.GL_UI.Controls.Animation.WorkAnimation;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.UI_Size_Base;
import CB_UI_Base.settings.CB_UI_Base_Settings;
import CB_Utils.Util.Downloader;
import CB_Utils.Util.FileIO;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

/**
 * Ein Control, welches ein Bild aus einem Pfad Darstellt.
 * 
 * @author Longri
 */
public class Image extends CB_View_Base {
    final static org.slf4j.Logger log = LoggerFactory.getLogger(Image.class);
    private AnimationBase Wait;

    private Color mColor = new Color(1, 1, 1, 1);
    private float spriteWidth = -1;
    private float spriteHeight = -1;
    private boolean inLoad = false;
    private boolean ImageLoadError = false;
    private final boolean reziseHeight;
    private int State = 0;
    private HAlignment hAlignment = HAlignment.CENTER;

    public Image(float X, float Y, float Width, float Height, String Name, boolean reziseHeight) {
	super(X, Y, Width, Height, Name);
	this.reziseHeight = reziseHeight;
    }

    public Image(CB_RectF rec, String Name, boolean reziseHeight) {
	super(rec, Name);
	this.reziseHeight = reziseHeight;
    }

    @Override
    protected void render(Batch batch) {
	if (ImageLoadError) {
	    if (Wait != null) {
		GL.that.removeRenderView(Wait);
		this.removeChild(Wait);
		Wait = null;
	    }

	    setSprite(SpriteCacheBase.Icons.get(IconName.delete_28.ordinal()), this.reziseHeight);
	    ImageLoadError = false;
	    return;
	}

	if (State == 3) {
	    try {

		mImageTex = new Texture(Gdx.files.absolute(mPath));
		Sprite sprite = new com.badlogic.gdx.graphics.g2d.Sprite(mImageTex);

		spriteWidth = sprite.getWidth();
		spriteHeight = sprite.getHeight();

		setSprite(sprite, this.reziseHeight);

	    } catch (com.badlogic.gdx.utils.GdxRuntimeException e) {
		State = 4;
	    } catch (Exception e) {
		ImageLoadError = true;
		log.debug("E Load GL Image", e);
		e.printStackTrace();
	    }
	    return;
	}

	if (State == 4)
	    ThreadLoad(this.reziseHeight);

	if (State == 6)
	    setAtlas(this.AtlasPath, this.ImgName, this.reziseHeight);

	Color altColor = batch.getColor().cpy();

	batch.setColor(mColor);

	if (mDrawable != null) {
	    if (Wait != null) {
		GL.that.removeRenderView(Wait);
		this.removeChild(Wait);
		Wait = null;
	    }
	    inLoad = false;
	    float drawwidth = getWidth();
	    float drawHeight = getHeight();
	    float drawX = 0;
	    float drawY = 0;

	    if (spriteWidth > 0 && spriteHeight > 0) {
		float proportionWidth = getWidth() / spriteWidth;
		float proportionHeight = getHeight() / spriteHeight;

		float proportion = Math.min(proportionWidth, proportionHeight);

		drawwidth = spriteWidth * proportion;
		drawHeight = spriteHeight * proportion;

		switch (hAlignment) {
		case CENTER:
		    drawX = (getWidth() - drawwidth) / 2;
		    break;
		case LEFT:
		    drawX = 0;
		    break;
		case RIGHT:
		    drawX = getWidth() - drawwidth;
		    break;
		case SCROLL_CENTER:
		    drawX = (getWidth() - drawwidth) / 2;
		    break;
		case SCROLL_LEFT:
		    drawX = 0;
		    break;
		case SCROLL_RIGHT:
		    drawX = getWidth() - drawwidth;
		    break;
		default:
		    drawX = (getWidth() - drawwidth) / 2;
		    break;

		}

		drawY = (getHeight() - drawHeight) / 2;
	    }

	    mDrawable.draw(batch, drawX, drawY, drawwidth, drawHeight);

	} else if (inLoad) {
	    if (Wait == null) {
		CB_RectF animationRec = new CB_RectF(0, 0, this.getWidth(), this.getHeight());
		Wait = WorkAnimation.GetINSTANCE(animationRec);
		GL.that.addRenderView(Wait, GL.FRAME_RATE_ACTION);
		this.addChild(Wait);
	    }

	    GL.that.renderOnce();
	}

	batch.setColor(altColor);

	//	//Draw Debug Rec
	//	if (DebugSprite != null) {
	//	    batch.flush();
	//	    DebugSprite.draw(batch);
	//
	//	} else {
	//	    writeDebug();
	//	}

    }

    private Thread loadingThread;

    private void ThreadLoad(final boolean reziseHeight) {
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
		Sprite spt = tryToLoadFromCreatetdAtlas(mPath);

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

    private String mPath;
    private Texture mImageTex = null;
    Drawable mDrawable = null;

    public void setImage(String Path) {
	State = 3;
	mPath = Path;
	if (mDrawable != null) {
	    dispose();
	    // das laden des Images in das Sprite darf erst in der Render Methode passieren, damit es aus dem GL_Thread herraus läuft.
	}
	GL.that.renderOnce();
    }

    public void setDrawable(Drawable drawable) {
	mDrawable = drawable;
	inLoad = false;
	GL.that.renderOnce();
    }

    @Override
    public void dispose() {
	GL.that.RunOnGL(new IRunOnGL() {

	    @Override
	    public void run() {
		if (mImageTex != null)
		    mImageTex.dispose();
		mImageTex = null;
		mDrawable = null;
	    }
	});
    }

    @Override
    protected void Initial() {
    }

    @Override
    protected void SkinIsChanged() {

    }

    public void setColor(Color color) {
	if (color == null)
	    mColor = new Color(1, 1, 1, 1);
	else
	    mColor = color;
    }

    private Thread ImageDownloadThread;

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
		final String CachePath = new File(CB_UI_Base_Settings.ImageCacheFolderLocal.getValue()).getAbsolutePath();

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
		    final Downloader dl = new Downloader(new URL(iconUrl), new File(CachePath + LocalPath));

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
		    e.printStackTrace();
		} catch (Exception e) {
		    e.printStackTrace();
		}
	    }
	});
	ImageDownloadThread.start();

	GL.that.renderOnce();
    }

    public void setSprite(Sprite sprite, boolean reziseHeight) {

	State = 7;
	inLoad = false;
	spriteWidth = sprite.getWidth();
	spriteHeight = sprite.getHeight();

	if (this.reziseHeight) {
	    float proportionWidth = getWidth() / spriteWidth;
	    if (proportionWidth > 1) {
		proportionWidth = 1;
	    }

	    float newWidth = spriteWidth * proportionWidth * UI_Size_Base.that.getScale();
	    float newHeight = spriteHeight * proportionWidth * UI_Size_Base.that.getScale();
	    this.setSize(newWidth, newHeight);

	}

	mDrawable = new SpriteDrawable(sprite);
	GL.that.renderOnce();
    }

    public void clearImage() {
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
	String outputFolder = CB_UI_Base_Settings.ImageCacheFolderLocal.getValue();
	String Name = getCachedAtlasName(inputFolder);

	try {
	    TexturePacker_Base.process(textureSettings, inputFolder, outputFolder, Name);
	} catch (Exception e) {
	    e.printStackTrace();
	    ImageLoadError = true;
	}

	Sprite spt = tryToLoadFromCreatetdAtlas(ImagePath);
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

    private HashMap<String, TextureAtlas> Atlanten;

    private Sprite tryToLoadFromCreatetdAtlas(String ImagePath) {

	if (Atlanten == null)
	    Atlanten = new HashMap<String, TextureAtlas>();

	String inputFolder = FileIO.GetDirectoryName(ImagePath);
	String ImageName = FileIO.GetFileNameWithoutExtension(ImagePath);
	String Name = getCachedAtlasName(inputFolder);

	final String AtlasPath = CB_UI_Base_Settings.ImageCacheFolderLocal.getValue() + "/" + Name;
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

    private String AtlasPath;
    private String ImgName;

    private void setAtlas(String atlasPath, String imgName, boolean reziseHeight) {
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

    public void setHAlignment(HAlignment alignment) {
	this.hAlignment = alignment;
    }
}
