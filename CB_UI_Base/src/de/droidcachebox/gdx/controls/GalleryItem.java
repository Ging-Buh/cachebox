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
import com.badlogic.gdx.graphics.g2d.NinePatch;

import de.droidcachebox.gdx.GL_Input;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.controls.CB_Label.HAlignment;
import de.droidcachebox.gdx.controls.list.ListViewItemBackground;
import de.droidcachebox.gdx.math.CB_RectF;

/**
 * @author Longri
 */
public class GalleryItem extends ListViewItemBackground {

    protected static boolean mBackIsInitial = false;
    private static NinePatch backSelect;
    private final Image img;
    protected boolean isPressed = false;

    public GalleryItem(CB_RectF rec, int Index, ImageLoader loader) {
        super(rec, Index, "");
        CB_RectF imgRec = new CB_RectF(rec);
        imgRec.setPos(0, 0);
        img = new Image(loader, imgRec.scaleCenter(0.95f), "", false);
        img.setHAlignment(HAlignment.CENTER);
        this.addChild(img);
    }

    /*
    public static void resetBackground() {
        mBackIsInitial = false;
    }
     */

    @Override
    public void onResized(CB_RectF rec) {
        super.onResized(rec);
        CB_RectF imgRec = new CB_RectF(rec);
        imgRec.setPos(0, 0);
        img.setRec(imgRec.scaleCenter(0.95f));
    }

    public Image getImage() {
        return img;
    }

    @Override
    protected void renderInit() {
        if (!mBackIsInitial) {
            backSelect = new NinePatch(Sprites.getSprite("listrec-first"), 13, 13, 13, 13);
            mBackIsInitial = true;
        }
    }

    @Override
    protected void render(Batch batch) {
        if (isPressed) {
            isPressed = GL_Input.that.getIsTouchDown();
        }

        if (isDisposed || !this.isVisible())
            return;
        super.render(batch);
        // Draw Background
        if (mBackIsInitial) {
            if (isSelected) {
                backSelect.draw(batch, 0, 0, this.getWidth(), this.getHeight());
            }

        } else {
            renderInit();
        }

    }

}
