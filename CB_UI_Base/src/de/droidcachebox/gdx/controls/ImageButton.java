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

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.math.CB_RectF;

public class ImageButton extends CB_Button {
    protected final Image image;
    float mScale = 1f;
    float mAngle = 0;

    public ImageButton() {
        super("");
        image = new Image(this.scaleCenter(0.8f), "", false);
        this.addChild(image);
    }

    public ImageButton(Sprites.IconName iconName) {
        super(null, "");
        image = new Image(this.scaleCenter(0.8f), "", false);
        image.setSprite(Sprites.getSprite(iconName.name()));
        setText("");
    }

    @Override
    protected void layout() {
        if (image == null) return;
        initRow(BOTTOMUP);
        image.setHeight(innerHeight);
        image.setWidth(innerHeight);
        if (lblTxt != null) {
            addNext(image, FIXED);
            addLast(lblTxt);
        }
        else {
            addLast(image, FIXED);
        }
        GL.that.renderOnce();
    }

    public ImageButton(CB_RectF rec, String name) {
        super(rec, name);
        image = new Image(this.scaleCenter(0.8f), "", false);
        this.addChild(image);
    }

    public ImageButton(ImageLoader img) {
        super("");
        image = new Image(img, this.scaleCenter(0.8f), "", false);
        this.addChild(image);
    }

    public ImageButton(Image image) {
        super("");
        if (image == null) {
            this.image = new Image(this.scaleCenter(0.8f), "", false);
        } else {
            this.image = image;
        }
        this.addChild(image);
    }

    @Override
    protected void render(Batch batch) {
        super.render(batch);
        if (isDisabled) {
            image.setColor(new Color(1f, 1f, 1f, 0.5f));
        } else {
            image.setColor(null);
        }
    }

    private void chkImagePos() {
        CB_RectF thisRectF = new CB_RectF(this);
        thisRectF.setPos(0, 0);
        image.setRec(thisRectF.scaleCenter(0.8f * mScale));
    }

    public void setImage(Drawable drawable) {
        image.setDrawable(drawable);
        chkImagePos();
    }

    public void setImage(Sprite sprite) {
        image.setSprite(sprite);
        chkImagePos();
    }

    public void setImageRotation(Float angle) {
        mAngle = angle;
        chkImagePos();
        image.setRotate(angle);
        image.setOrigin(image.getHalfWidth(), image.getHalfHeight());
    }

    public void setImageScale(float scale) {
        mScale = scale;
        chkImagePos();
        image.setRotate(mAngle);
        image.setOrigin(image.getHalfWidth(), image.getHalfHeight());
    }

    @Override
    public void resize(float width, float height) {
        chkImagePos();
        image.setRotate(mAngle);
        image.setOrigin(image.getHalfWidth(), image.getHalfHeight());
    }

    @Override
    public void dispose() {
        image.dispose();
        super.dispose();
    }

    public void clearImage() {
        image.clearImage();
    }

}
