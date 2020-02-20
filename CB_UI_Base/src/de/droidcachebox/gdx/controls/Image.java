/*
 * Copyright (C) 2011-2020 team-cachebox.de
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
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import de.droidcachebox.gdx.CB_View_Base;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;
import de.droidcachebox.gdx.controls.CB_Label.HAlignment;
import de.droidcachebox.gdx.controls.animation.AnimationBase;
import de.droidcachebox.gdx.controls.animation.WorkAnimation;
import de.droidcachebox.gdx.math.CB_RectF;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Ein Control, welches ein Bild aus einem Pfad Darstellt.
 *
 * @author Longri
 */
public class Image extends CB_View_Base {
    private AtomicBoolean isAsRenderViewRegistered = new AtomicBoolean(false);
    private AnimationBase Wait;
    private ImageLoader imageLoader;
    private Color mColor = new Color(1, 1, 1, 1);
    private HAlignment hAlignment = HAlignment.CENTER;

    public Image(float X, float Y, float Width, float Height, String Name, boolean resizeHeight) {
        super(X, Y, Width, Height, Name);
        imageLoader = new ImageLoader();
        imageLoader.resizeHeight = resizeHeight;
        if (imageLoader.resizeHeight && imageLoader.getResizeListener() == null) {
            imageLoader.setResizeListener(Image.this::setSize, getWidth());
        }
    }

    public Image(CB_RectF rec, String Name, boolean resizeHeight) {
        super(rec, Name);
        imageLoader = new ImageLoader();
        imageLoader.resizeHeight = resizeHeight;
        if (imageLoader.resizeHeight && imageLoader.getResizeListener() == null) {
            imageLoader.setResizeListener(Image.this::setSize, getWidth());
        }
    }

    public Image(ImageLoader img, CB_RectF rec, String Name, boolean resizeHeight) {
        super(rec, Name);
        imageLoader = img;
        imageLoader.resizeHeight = resizeHeight;
        if (imageLoader.resizeHeight && imageLoader.getResizeListener() == null) {
            imageLoader.setResizeListener(Image.this::setSize, getWidth());
        }

    }

    @Override
    public void onShow() {
        if (imageLoader != null) {
            if (imageLoader.getAnimDelay() > 0) {
                GL.that.addRenderView(this, imageLoader.getAnimDelay());
                isAsRenderViewRegistered.set(true);
            }
        }
    }

    @Override
    public void onHide() {
        if (imageLoader != null) {
            if (imageLoader.getAnimDelay() > 0) {
                GL.that.removeRenderView(this);
                isAsRenderViewRegistered.set(false);
            }
        }
    }

    @Override
    protected void render(Batch batch) {

        if (imageLoader == null)
            return;

        Color altColor = batch.getColor().cpy();
        batch.setColor(mColor);
        try {
            if (!imageLoader.isDrawableNULL()) {
                if (Wait != null) {
                    GL.that.removeRenderView(Wait);
                    removeChild(Wait);
                    Wait = null;
                }
                imageLoader.inLoad = false;
                float drawwidth = getWidth();
                float drawHeight = getHeight();
                float drawX = 0;
                float drawY = 0;

                if (imageLoader.getSpriteWidth() > 0 && imageLoader.getSpriteHeight() > 0) {
                    float proportionWidth = getWidth() / imageLoader.getSpriteWidth();
                    float proportionHeight = getHeight() / imageLoader.getSpriteHeight();

                    float proportion = Math.min(proportionWidth, proportionHeight);

                    drawwidth = imageLoader.getSpriteWidth() * proportion;
                    drawHeight = imageLoader.getSpriteHeight() * proportion;

                    switch (hAlignment) {
                        case LEFT:
                        case SCROLL_LEFT:
                            drawX = 0;
                            break;
                        case RIGHT:
                        case SCROLL_RIGHT:
                            drawX = getWidth() - drawwidth;
                            break;
                        default:
                            drawX = (getWidth() - drawwidth) / 2;
                            break;
                    }

                    drawY = (getHeight() - drawHeight) / 2;
                }

                imageLoader.getDrawable(Gdx.graphics.getDeltaTime()).draw(batch, drawX, drawY, drawwidth, drawHeight);

                if (!isAsRenderViewRegistered.get() && imageLoader.getAnimDelay() > 0) {
                    GL.that.addRenderView(this, imageLoader.getAnimDelay());
                    isAsRenderViewRegistered.set(true);
                }
            } else if (imageLoader.inLoad & !imageLoader.ImageLoadError) {
                if (Wait == null) {
                    Wait = new WorkAnimation();
                    GL.that.addRenderView(Wait, GL.FRAME_RATE_ACTION);
                    addChild(Wait);
                }

                GL.that.renderOnce();
            } else if (imageLoader.ImageLoadError) {
                if (Wait != null) {
                    removeChild(Wait);

                    //set error image
                    setSprite(new Sprite(Sprites.getSprite(IconName.disabled.name())));

                }

                GL.that.renderOnce();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        batch.setColor(altColor);

        //Draw Debug Rec
        //	if (DebugSprite != null) {
        //	    batch.flush();
        //	    DebugSprite.draw(batch);
        //
        //	} else {
        //	    writeDebug();
        //	}

    }

    public void setImage(String Path) {
        if (imageLoader != null)
            imageLoader.setImage(Path);
    }

    @Override
    public void dispose() {
        GL.that.removeRenderView(this);
        isAsRenderViewRegistered.set(false);
        if (imageLoader != null)
            imageLoader.dispose();
        imageLoader = null;
    }

    public void setColor(Color color) {
        if (color == null)
            mColor = new Color(1, 1, 1, 1);
        else
            mColor = color;
    }

    /**
     * Sets a Image URl and Downlowd this Image if this don't exist on Cache
     *
     */
    public void setImageURL(final String iconUrl) {
        if (imageLoader != null)
            imageLoader.setImageURL(iconUrl);
    }

    void clearImage() {
        if (imageLoader != null)
            imageLoader.clearImage();
        mColor = new Color(1, 1, 1, 1);
        mScale = 1;
        setOriginCenter();
    }

    public void setHAlignment(HAlignment alignment) {
        hAlignment = alignment;
    }

    public void setSprite(Sprite sprite) {
        if (imageLoader != null)
            imageLoader.setSprite(sprite);
    }

    public Drawable getDrawable() {
        if (imageLoader == null)
            return null;
        return imageLoader.getDrawable(Gdx.graphics.getDeltaTime());
    }

    public void setDrawable(Drawable drawable) {
        if (imageLoader != null)
            imageLoader.setDrawable(drawable);
    }

    public ImageLoader getImageLoader() {
        return imageLoader;
    }
}
