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
package de.droidcachebox.gdx.controls;

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
import de.droidcachebox.CB_UI_Base_Settings;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;
import de.droidcachebox.gdx.graphics.GifDecoder;
import de.droidcachebox.gdx.math.UiSizes;
import de.droidcachebox.gdx.texturepacker.Settings;
import de.droidcachebox.gdx.texturepacker.TexturePacker_Base;
import de.droidcachebox.utils.FileFactory;
import de.droidcachebox.utils.FileIO;
import de.droidcachebox.utils.http.Download;
import de.droidcachebox.utils.log.Log;

import java.util.HashMap;

/**
 * @author Longri
 */
public class ImageLoader {

    private static final String log = "ImageLoader";
    private static AssetManager assetManager = new AssetManager();
    private final boolean thumbnail;
    boolean ImageLoadError = false;
    boolean inLoad = false;
    boolean reziseHeight;
    private int State = 0;
    private Thread loadingThread;
    private String mPath;
    private Texture mImageTex = null;
    private Drawable mDrawable = null;
    private Thread ImageDownloadThread;
    private boolean isPacking = false;
    private HashMap<String, TextureAtlas> atlanten;
    private String AtlasPath;
    private String ImgName;
    private boolean inGenerate = false;
    private float frameCounter = 0;
    private SpriteDrawable animDrawable;
    private Sprite animSprite;
    private Animation anim;
    private float spriteWidth;
    private float spriteHeight;
    private resize resizeListener;
    private float resizeWidth = 0;
    private String thumbPraefix = "";
    private String originalPath = null;

    ImageLoader() {
        thumbnail = false;
    }

    public ImageLoader(boolean thumb) {
        thumbnail = thumb;
    }

    void setResizeListener(resize listener, float width) {
        resizeListener = listener;
        resizeWidth = width;
    }

    public void setThumbWidth(float width, String praefix) {
        resizeWidth = width;
        thumbPraefix = praefix;
    }

    resize getResizeListener() {
        return resizeListener;
    }

    /**
     * Pack the images from Folder into a Atlas and Load the Image from Atlas
     */
    private void packImagesToTextureAtlas(String imagePath) {
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

        String inputFolder = FileIO.getDirectoryName(imagePath);
        String outputFolder = CB_UI_Base_Settings.imageCacheFolder.getValue();
        String Name = getCachedAtlasName(inputFolder);

        try {
            TexturePacker_Base.process(textureSettings, inputFolder, outputFolder, Name);
        } catch (Exception e) {
            e.printStackTrace();
            ImageLoadError = true;
        }

        Sprite spt = tryToLoadFromCreatedAtlas(imagePath);
        if (spt != null)
            setSprite(spt);

        isPacking = false;
        GL.that.renderOnce();
    }

    private String getCachedAtlasName(String inputFolder) {
        String Name = inputFolder.replace("/", "_");
        Name = Name.replace("\\", "_");
        Name = Name.replace(".", "");
        Name = Name + ".spp.atlas";
        return Name;
    }

    private Sprite tryToLoadFromCreatedAtlas(String ImagePath) {

        if (atlanten == null)
            atlanten = new HashMap<>();

        String inputFolder = FileIO.getDirectoryName(ImagePath);
        String ImageName = FileIO.getFileNameWithoutExtension(ImagePath);
        String Name = getCachedAtlasName(inputFolder);

        final String AtlasPath = CB_UI_Base_Settings.imageCacheFolder.getValue() + "/" + Name;
        if (!FileIO.fileExistsNotEmpty(AtlasPath))
            return null;
        TextureAtlas atlas = null;
        if (atlanten.containsKey(AtlasPath)) {
            atlas = atlanten.get(AtlasPath);
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

    private void setAtlas(String atlasPath, String imgName) {
        State = 7;
        TextureAtlas atlas = new TextureAtlas(Gdx.files.absolute(atlasPath));
        atlanten.put(atlasPath, atlas);

        Sprite tmp;
        tmp = atlas.createSprite(imgName);

        if (tmp != null)
            setSprite(tmp);
    }

    private void loadThread() {
        State = 5;
        if (isPacking)
            return;

        if (loadingThread != null) {
            if (loadingThread.getState() != Thread.State.TERMINATED)
                return;
            else
                loadingThread = null;
        }

        loadingThread = new Thread(() -> {
            Sprite spt = tryToLoadFromCreatedAtlas(mPath);

            if (spt != null) {
                setSprite(spt);
            } else {
                packImagesToTextureAtlas(mPath);
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
     */
    void setImageURL(final String iconUrl) {
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

        ImageDownloadThread = new Thread(() -> {
            final String CachePath = FileFactory.createFile(CB_UI_Base_Settings.imageCacheFolder.getValue()).getAbsolutePath();

            // Search first slash after Http or www
            int slashPos;
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
            if (FileIO.fileExistsNotEmpty(CachePath + LocalPath)) {
                setImage(CachePath + LocalPath);
                return;
            }

            inLoad = true;

            // Download Image to Cache
            try {
                new Thread(() -> {
                    Download.download(iconUrl, CachePath + LocalPath);
                    inLoad = false;

                    // chk if Download complied
                    if (!FileIO.fileExistsNotEmpty(CachePath + LocalPath)) {
                        // Download Error
                        ImageLoadError = true;
                        return;
                    }

                    setImage(CachePath + LocalPath);
                }).run();

            } catch (Exception e) {
                Log.err(log, "for " + iconUrl, e);
            }
        });
        ImageDownloadThread.start();
        generate();
    }

    void clearImage() {
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

    void setSprite(Sprite sprite) {

        State = 7;
        inLoad = false;
        spriteWidth = sprite.getWidth();
        spriteHeight = sprite.getHeight();

        if (this.resizeListener != null) {
            float proportionWidth = resizeWidth / spriteWidth;
            if (proportionWidth > UiSizes.getInstance().getScale()) {
                proportionWidth = UiSizes.getInstance().getScale();
            }

            float newWidth = spriteWidth * proportionWidth;//* UI_Size_Base.that.getScale();
            float newHeight = spriteHeight * proportionWidth;//* UI_Size_Base.that.getScale();
            sprite.scale(proportionWidth);
            this.resizeListener.sizeChanged(newWidth, newHeight);

        }

        mDrawable = new SpriteDrawable(sprite);
        GL.that.renderOnce();
    }

    private void generate() {
        inLoad = true;
        if (ImageLoadError) {
            setSprite(Sprites.getSprite(IconName.DELETE.name()));
            ImageLoadError = false;
            return;
        }

        if (State == 3) {
            if (inGenerate)
                return;
            GL.that.RunOnGL(() -> {
                try {
                    inGenerate = true;

                    if (mPath != null) {
                        if (mPath.endsWith(".gif")) {
                            anim = GifDecoder.loadGIFAnimation(PlayMode.LOOP, Gdx.files.absolute(mPath).read());
                        } else {
                            loadAsync();
                        }
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
            });

            return;
        }

        if (State == 4)
            loadThread();

        if (State == 6)
            setAtlas(this.AtlasPath, this.ImgName);
    }

    private void loadAsync() {
        new Thread(() -> {
            if (thumbnail)
                createThumb();
            final TextureLoader tl = new TextureLoader(new AbsoluteFileHandleResolver());
            try {
                tl.loadAsync(assetManager, ImgName, Gdx.files.absolute(mPath), null);
                GL.that.RunOnGL(() -> {
                    // Log.info(log, "LoadSync " + mPath + ":" + ImgName);
                    mImageTex = tl.loadSync(assetManager, ImgName, Gdx.files.absolute(mPath), null);
                    Sprite sprite = new Sprite(mImageTex);
                    spriteWidth = sprite.getWidth();
                    spriteHeight = sprite.getHeight();
                    setSprite(sprite);
                    // Log.info(log, "LoadSync " + mPath + ":" + ImgName + " ready");
                });
            } catch (Exception ignored) {
            }
        }).start();
    }

    private void createThumb() {
        String tmp = FileFactory.createThumb(mPath, (int) resizeWidth, thumbPraefix);
        if (tmp != null) {
            originalPath = mPath;
            mPath = tmp;
        } else {
            Log.err(log, "Thumb not generated for " + mPath + " ! " + thumbPraefix);
        }
    }

    /**
     * Returns the drawable was is loaded.
     * <p>
     * If the loaded image a Animation, like GIF, so returns the drawable for the given KeyFrame.
     *
     */
    Drawable getDrawable(float keyFrame) {
        if (anim != null) {
            frameCounter += keyFrame;
            TextureRegion tex = (TextureRegion) anim.getKeyFrame(frameCounter);

            if (animSprite == null) {
                animSprite = new com.badlogic.gdx.graphics.g2d.Sprite(tex);
                spriteWidth = animSprite.getWidth();
                spriteHeight = animSprite.getHeight();

                if (this.resizeListener != null) {
                    float proportionWidth = resizeWidth / spriteWidth;
                    if (proportionWidth > UiSizes.getInstance().getScale()) {
                        proportionWidth = UiSizes.getInstance().getScale();
                    }

                    float newWidth = spriteWidth * proportionWidth;
                    float newHeight = spriteHeight * proportionWidth;
                    animSprite.scale(proportionWidth);
                    this.resizeListener.sizeChanged(newWidth, newHeight);

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

    boolean isDrawableNULL() {
        return mDrawable == null && anim == null;
    }

    int getAnimDelay() {
        if (anim != null) {
            return (int) (anim.getFrameDuration() * 1000);
        }
        return 0;
    }

    public void dispose() {

        GL.that.RunOnGL(() -> {
            if (mImageTex != null) {
                try {
                    assetManager.unload(ImgName);
                } catch (Exception ignored) {
                }
                mImageTex.dispose();
            }

            mImageTex = null;
            mDrawable = null;
            loadingThread = null;
            mPath = null;

            ImageDownloadThread = null;
            if (atlanten != null) {
                atlanten.clear();
                atlanten = null;
            }

            AtlasPath = null;
            ImgName = null;
            animDrawable = null;

            animSprite = null;
            anim = null;
        });
    }

    public float getSpriteWidth() {
        return spriteWidth;
    }

    public float getSpriteHeight() {
        return spriteHeight;
    }

    public String getImagePath() {
        return mPath;
    }

    public String getOriginalImagePath() {
        if (originalPath == null)
            return mPath;
        return originalPath;
    }

    public interface resize {
        void sizeChanged(float width, float height);
    }

}