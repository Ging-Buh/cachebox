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

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import de.droidcachebox.gdx.ParentInfo;
import de.droidcachebox.gdx.math.CB_RectF;

public class ImageMultiToggleButton extends MultiToggleButton {
    private final Image image;
    float mScale = 1f;
    float mAngle = 0;

    public ImageMultiToggleButton(CB_RectF rec, String name) {
        super(rec, name);
        image = new Image(this.scaleCenter(0.4f), "", false);
        image.setY(this.getHeight() - image.getHeight() - image.getHalfHeight());
        this.addChild(image);
    }

    public void setImage(Drawable drawable) {
        image.setDrawable(drawable);
    }

    public void setImageRotation(Float angle) {
        mAngle = angle;
        CB_RectF imgRec = this.copy();
        imgRec.setPos(0, 0);
        image.setRec(imgRec.scaleCenter(0.4f * mScale));
        image.setY(this.getHeight() - image.getHeight() - image.getHalfHeight());
        image.setRotate(angle);
        image.setOrigin(image.getHalfWidth(), image.getHalfHeight() - image.getHalfHeight());
    }

    public void setImageScale(float scale) {
        mScale = scale;
        CB_RectF imgRec = this.copy();
        imgRec.setPos(0, 0);
        image.setRec(imgRec.scaleCenter(0.4f * mScale));
        image.setY(this.getHeight() - image.getHeight() - image.getHalfHeight());
        image.setRotate(mAngle);
        image.setOrigin(image.getHalfWidth(), image.getHalfHeight() - image.getHalfHeight());
    }

    @Override
    public void resize(float width, float height) {
        super.resize(width, height);

        CB_RectF imgRec = this.copy();
        imgRec.setPos(0, 0);
        image.setRec(imgRec.scaleCenter(0.4f * mScale));
        image.setY(this.getHeight() - image.getHeight() - image.getHalfHeight());
        image.setRotate(mAngle);
        image.setOrigin(image.getHalfWidth(), image.getHalfHeight() - image.getHalfHeight());
    }

    @Override
    public void render(Batch batch) {
        super.render(batch);
    }

    @Override
    public void renderChilds(final Batch batch, ParentInfo parentInfo) {
        super.renderChilds(batch, parentInfo);
    }
}
