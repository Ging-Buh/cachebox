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

import org.slf4j.LoggerFactory;

import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.Controls.Label.HAlignment;
import CB_UI_Base.GL_UI.Controls.Animation.AnimationBase;
import CB_UI_Base.GL_UI.Controls.Animation.WorkAnimation;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.Math.CB_RectF;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

/**
 * Ein Control, welches ein Bild aus einem Pfad Darstellt.
 * 
 * @author Longri
 */
public class Image extends CB_View_Base {
    final static org.slf4j.Logger log = LoggerFactory.getLogger(Image.class);
    private AnimationBase Wait;
    private ImageLoader image;

    private Color mColor = new Color(1, 1, 1, 1);
    private HAlignment hAlignment = HAlignment.CENTER;

    public Image(float X, float Y, float Width, float Height, String Name, boolean reziseHeight) {
	super(X, Y, Width, Height, Name);
	this.image = new ImageLoader();
	this.image.reziseHeight = reziseHeight;
	if (this.image.reziseHeight && this.image.getResizeListner() == null) {
	    this.image.setResizeListner(new CB_UI_Base.GL_UI.Controls.ImageLoader.resize() {

		@Override
		public void sizechanged(float newWidth, float newHeight) {
		    Image.this.setSize(newWidth, newHeight);
		}
	    }, this.getWidth());
	}

    }

    public Image(CB_RectF rec, String Name, boolean reziseHeight) {
	super(rec, Name);
	this.image = new ImageLoader();
	this.image.reziseHeight = reziseHeight;
	if (this.image.reziseHeight && this.image.getResizeListner() == null) {
	    this.image.setResizeListner(new CB_UI_Base.GL_UI.Controls.ImageLoader.resize() {

		@Override
		public void sizechanged(float newWidth, float newHeight) {
		    Image.this.setSize(newWidth, newHeight);
		}
	    }, this.getWidth());
	}
    }

    public Image(ImageLoader img, CB_RectF rec, String Name, boolean reziseHeight) {
	super(rec, Name);
	this.image = img;
	this.image.reziseHeight = reziseHeight;
	if (this.image.reziseHeight && this.image.getResizeListner() == null) {
	    this.image.setResizeListner(new CB_UI_Base.GL_UI.Controls.ImageLoader.resize() {

		@Override
		public void sizechanged(float newWidth, float newHeight) {
		    Image.this.setSize(newWidth, newHeight);
		}
	    }, this.getWidth());
	}

    }

    @Override
    protected void render(Batch batch) {

	Color altColor = batch.getColor().cpy();

	batch.setColor(mColor);

	if (image.getDrawable() != null) {
	    if (Wait != null) {
		GL.that.removeRenderView(Wait);
		this.removeChild(Wait);
		Wait = null;
	    }
	    image.inLoad = false;
	    float drawwidth = getWidth();
	    float drawHeight = getHeight();
	    float drawX = 0;
	    float drawY = 0;

	    if (image.spriteWidth > 0 && image.spriteHeight > 0) {
		float proportionWidth = getWidth() / image.spriteWidth;
		float proportionHeight = getHeight() / image.spriteHeight;

		float proportion = Math.min(proportionWidth, proportionHeight);

		drawwidth = image.spriteWidth * proportion;
		drawHeight = image.spriteHeight * proportion;

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

	    image.getDrawable().draw(batch, drawX, drawY, drawwidth, drawHeight);

	} else if (image.inLoad) {
	    if (Wait == null) {
		CB_RectF animationRec = new CB_RectF(0, 0, this.getWidth(), this.getHeight());
		Wait = WorkAnimation.GetINSTANCE(animationRec);
		GL.that.addRenderView(Wait, GL.FRAME_RATE_ACTION);
		this.addChild(Wait);
	    }

	    GL.that.renderOnce();
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
	image.setImage(Path);
    }

    public void setDrawable(Drawable drawable) {
	image.setDrawable(drawable);
    }

    @Override
    public void dispose() {
	image.dispose();
	image = null;
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

    /**
     * Sets a Image URl and Downlowd this Image if this don't exist on Cache
     * 
     * @param iconUrl
     */
    public void setImageURL(final String iconUrl) {
	image.setImageURL(iconUrl);
    }

    //    @Override
    //    public void setSprite(Sprite sprite, boolean reziseHeight) {
    //
    //	State = 7;
    //	inLoad = false;
    //	spriteWidth = sprite.getWidth();
    //	spriteHeight = sprite.getHeight();
    //
    //	if (this.reziseHeight) {
    //	    float proportionWidth = getWidth() / spriteWidth;
    //	    if (proportionWidth > 1) {
    //		proportionWidth = 1;
    //	    }
    //
    //	    float newWidth = spriteWidth * proportionWidth * UI_Size_Base.that.getScale();
    //	    float newHeight = spriteHeight * proportionWidth * UI_Size_Base.that.getScale();
    //	    this.setSize(newWidth, newHeight);
    //
    //	}
    //
    //	mDrawable = new SpriteDrawable(sprite);
    //	GL.that.renderOnce();
    //    }

    public void clearImage() {
	image.clearImage();
	mColor = new Color(1, 1, 1, 1);
	mScale = 1;
	setOriginCenter();
    }

    public void setHAlignment(HAlignment alignment) {
	this.hAlignment = alignment;
    }

    public void setSprite(Sprite sprite, boolean reziseHeight) {
	image.setSprite(sprite, reziseHeight);
    }
}
