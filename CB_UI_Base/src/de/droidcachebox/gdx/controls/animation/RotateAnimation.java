/*
 * Copyright (C) 2013-2014 team-cachebox.de
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
package de.droidcachebox.gdx.controls.animation;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.math.CB_RectF;

/**
 * @author Longri
 */
public abstract class RotateAnimation extends AnimationBase {
    static final int ANIMATION_DURATION = 2000;
    private float mOriginX;
    private float mOriginY;
    private float mScale = 1f;
    private Sprite mDrawable;

    RotateAnimation(CB_RectF rec, String Name) {
        super(rec, Name);
    }

    /**
     * Time for 360°
     */
    protected void play(int duration) {
        this.mDuration = duration;
        mPlaying = true;
        mPause = false;
    }

    @Override
    public void stop() {
        mPlaying = false;
        mPause = false;
    }

    @Override
    public void pause() {
        mPause = true;
    }

    @Override
    public void setOrigin(float x, float y) {
        mOriginX = x;
        mOriginY = y;
    }

    @Override
    public void setScale(float scale) {
        mScale = scale;
    }

    public void setSprite(Sprite sprite) {
        mSpriteWidth = sprite.getWidth();
        mSpriteHeight = sprite.getHeight();
        mDrawable = sprite;// new SpriteDrawable(sprite);
    }

    @Override
    protected void render(Batch batch) {

        if (mDrawable == null)
            return;

        // ####################################################################
        // calc Rotation
        // ####################################################################

        float animateRotateValue = (1 + ((int) (GL.that.getStateTime() * 1000f) % mDuration) / (mDuration / 360f));

        // ####################################################################
        // Draw
        // ####################################################################

        float drawwidth = getWidth();
        float drawHeight = getHeight();
        float drawX = 0;
        float drawY = 0;

        if (mSpriteWidth > 0 && mSpriteHeight > 0) {
            float proportionWidth = getWidth() / mSpriteWidth;
            float proportionHeight = getHeight() / mSpriteHeight;

            float proportion = Math.min(proportionWidth, proportionHeight);

            drawwidth = mSpriteWidth * proportion;
            drawHeight = mSpriteHeight * proportion;
            drawX = (getWidth() - drawwidth) / 2;
            drawY = (getHeight() - drawHeight) / 2;
        }

        batch.draw(mDrawable, drawX, drawY, mOriginX, mOriginY, drawwidth, drawHeight, mScale, mScale, animateRotateValue);

        GL.that.renderOnce();

    }

}
