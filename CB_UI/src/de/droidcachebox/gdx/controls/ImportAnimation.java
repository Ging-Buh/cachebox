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
package de.droidcachebox.gdx.controls;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

import de.droidcachebox.gdx.controls.animation.AnimationBase;
import de.droidcachebox.gdx.controls.animation.DownloadAnimation;
import de.droidcachebox.gdx.controls.animation.WorkAnimation;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.utils.log.Log;

public class ImportAnimation extends Box {
    private static final String sClass = "ImportAnimation";

    private AnimationBase mAnimation;
    private Drawable back;

    public ImportAnimation(CB_RectF rec) {
        super(rec, "");
        setAnimationType(AnimationType.Work);
    }

    public void setAnimationType(final AnimationType Type) {
        try {
            float size = getHalfWidth() / 2;
            float halfSize = getHalfWidth() / 4;
            CB_RectF imageRec = new CB_RectF(getHalfWidth() - halfSize, getHalfHeight() - halfSize, size, size);

            removeChilds();

            switch (Type) {
                case Work:
                    mAnimation = new WorkAnimation(imageRec);
                    break;

                case Download:
                    mAnimation = new DownloadAnimation(imageRec);
                    break;
            }

            addChild(mAnimation);
        }
        catch (Exception ex) {
            Log.err(sClass, ex);
        }
    }

    public void render(Batch batch) {
        if (drawableBackground != null) {
            back = drawableBackground;
            drawableBackground = null;
        }

        if (back != null) {
            Color c = batch.getColor();

            float a = c.a;
            float r = c.r;
            float g = c.g;
            float b = c.b;

            Color trans = new Color(0, 0.3f, 0, 0.40f);
            batch.setColor(trans);
            back.draw(batch, 0, 0, this.getWidth(), this.getHeight());

            batch.setColor(new Color(r, g, b, a));

        }
    }

    @Override
    public void onHide() {
        mAnimation.dispose();
    }

    @Override
    public boolean onTouchDown(int x, int y, int pointer, int button) {
        return true;
    }

    // alle Touch events abfangen

    @Override
    public void onLongClick(int x, int y, int pointer, int button) {
    }

    @Override
    public boolean onTouchDragged(int x, int y, int pointer, boolean KineticPan) {
        return true;
    }

    @Override
    public boolean onTouchUp(int x, int y, int pointer, int button) {
        return true;
    }

    @Override
    public boolean click(int x, int y, int pointer, int button) {
        return true;
    }

    public enum AnimationType {
        Work, Download
    }
}
