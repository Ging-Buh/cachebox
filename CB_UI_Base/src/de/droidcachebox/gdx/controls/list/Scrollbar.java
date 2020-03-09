/*
 * Copyright (C) 2014-2015 team-cachebox.de
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
package de.droidcachebox.gdx.controls.list;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import de.droidcachebox.gdx.CB_View_Base;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.controls.animation.Fader;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.UiSizes;

/**
 * @author Longri
 */
public class Scrollbar extends CB_View_Base {

    private final IScrollbarParent ListView;
    private final CB_RectF SliderPuchRec = new CB_RectF();
    private final CB_RectF SliderRec = new CB_RectF();
    private final Fader mPushSliderFader = new Fader();
    private final Fader mSliderFader = new Fader();
    protected int mLastTouch = 0;
    protected float mLastPos_onTouch = 0;
    private Drawable Slider, SliderPushed;
    private float mSliderPos = 0;
    private float mSliderSollHeight = 0;
    private float mSliderIstHight = 0;
    private boolean mSliderPushed = false;
    private float mPuchSliderPos = 0;
    private float mPushSliderTouch = 0;
    private float mPushSliderIstHight = 0;
    private float mPushSliderAlpha = 1f;
    private float mSliderAlpha = 1f;
    private boolean mSliderAlwaysVisible = false;
    private float mMinSliderHeight = -1;

    public Scrollbar(IScrollbarParent Parent) {
        super(Parent.getView(), Parent.getView(), "ScrollBar-on-" + Parent.getView().toString());
        ListView = Parent;
        mPushSliderFader.setTimeToFadeOut(4000);
    }

    @Override
    public void onShow() {
        mPushSliderFader.beginnFadeout();
    }

    public void setSliderAlwaysVisible(boolean value) {
        mSliderAlwaysVisible = value;
    }

    @Override
    public void render(Batch batch) {
        // Wenn Liste l�nger als Clintbereich zeige Slider
        if (ListView.isDraggable()) {
            if (mPushSliderFader.isVisible() || mSliderFader.isVisible() || mSliderAlwaysVisible) {
                Color color = batch.getColor();// get current Color, you can't modify directly
                float oldAlpha = color.a; // save its alpha

                if (Slider == null || SliderPushed == null) {
                    LoadSliderImagesNew();
                }

                // Draw Slider
                CalcSliderPos();

                mPushSliderAlpha = mPushSliderFader.isVisible() ? mPushSliderFader.getValue() : 0;

                if (mSliderAlwaysVisible) {
                    mSliderAlpha = 1f;
                } else {
                    mSliderAlpha = mSliderFader.isVisible() ? mSliderFader.getValue() : 0;
                }

                color.a = oldAlpha * mPushSliderAlpha;
                batch.setColor(color); // set it
                SliderPushed.draw(batch, SliderPuchRec.getX(), SliderPuchRec.getY(), SliderPuchRec.getWidth(), SliderPuchRec.getHeight());

                color.a = oldAlpha * mSliderAlpha;
                batch.setColor(color); // set it
                Slider.draw(batch, SliderRec.getX(), SliderRec.getY(), SliderRec.getWidth(), SliderRec.getHeight());

                // Set it back to orginial alpha when you're done with your alpha manipulation
                color.a = oldAlpha;
                batch.setColor(color);
            }
        }
    }

    private void LoadSliderImagesNew() {
        Slider = Sprites.slider;
        SliderPushed = Sprites.sliderPushed;

        float minWidth = Slider.getMinWidth();

        SliderRec.setX(this.getWidth() - (minWidth / 1.35f));
        SliderRec.setWidth(minWidth);

        mPushSliderIstHight = UiSizes.getInstance().getButtonHeight() * 0.8f;

        SliderPuchRec.setX(SliderRec.getX() - mPushSliderIstHight + Slider.getLeftWidth());
        SliderPuchRec.setY(mSliderPos);
        SliderPuchRec.setWidth(mPushSliderIstHight);
        SliderPuchRec.setHeight(mPushSliderIstHight);

    }

    /**
     * l= Visible height<br>
     * s= Slider height<br>
     * p= position of slider<br>
     * <br>
     * ll= complete height of all items<br>
     * lp= position of List
     */
    private void CalcSliderPos() {
        CalcSliderHeight();

        float lp = ListView.getScrollPos();
        float ll = ListView.getAllListSize();

        float s = mSliderIstHight;
        float l = this.getHeight();

        mSliderPos = this.getHeight() + ((lp / (ll - l)) * (l - s)) - s;
        SliderRec.setY(mSliderPos);

        s = mPushSliderIstHight;

        mPuchSliderPos = this.getHeight() + ((lp / (ll - l)) * (l - s)) - s;
        SliderPuchRec.setY(mPuchSliderPos);

    }

    private void CalcSliderHeight() {

        mSliderSollHeight = (this.getHeight() / ListView.getAllListSize()) * this.getHeight();

        if (mSliderSollHeight > this.getHalfHeight()) {
            mSliderIstHight = this.getHalfHeight();
        } else if (mSliderSollHeight < getMinSliderHeight()) {
            mSliderIstHight = getMinSliderHeight();
        } else {
            mSliderIstHight = mSliderSollHeight;
        }

        SliderRec.setHeight(mSliderIstHight);

    }

    private float getMinSliderHeight() {
        if (mMinSliderHeight > 0)
            return mMinSliderHeight;
        return mMinSliderHeight = UiSizes.getInstance().getButtonHeight() * 0.5f;
    }

    @Override
    public boolean onTouchUp(int x, int y, int pointer, int button) {
        if (mSliderPushed) {
            mPushSliderFader.resetFadeOut();
            mSliderPushed = false;
            ListView.chkSlideBack();
            return true;
        }
        return false;
    }

    @Override
    public boolean onTouchDown(int x, int y, int pointer, int button) {

        if (SliderPuchRec.contains(x, y)) {
            mSliderFader.resetFadeOut();
            mPushSliderFader.resetFadeOut();
            mLastTouch = y;

            mPushSliderTouch = SliderPuchRec.getY() - y;
            mLastPos_onTouch = ListView.getScrollPos();
            mSliderPushed = true;
            return true;
        }

        return false;
    }

    @Override
    public boolean onTouchDragged(int x, int y, int pointer, boolean KineticPan) {

        if (mSliderPushed) {
            mPushSliderFader.stopTimer();
            float ll = ListView.getAllListSize();
            float ls = this.getHeight();

            if (ls > ll)
                return true;

            float p = -ls + y - mPushSliderTouch;
            float s = mPushSliderIstHight;
            float l = this.getHeight();

            float lp = (p / (l - s)) * (ll - ls);

            this.ListView.setListPos(lp);

            return true;
        }

        return false;
    }

    @Override
    public void onParentResized(CB_RectF rec) {
        this.setWidth(rec.getWidth());
        this.setHeight(rec.getHeight());
    }

    public void ScrollPositionChanged() {
        mSliderFader.resetFadeOut();
    }

    public float getSliderWidth() {
        if (Slider == null) {
            LoadSliderImagesNew();
        }
        return SliderRec.getWidth();
    }

    public float getPushSliderWidth() {
        if (Slider == null) {
            LoadSliderImagesNew();
        }
        return SliderPuchRec.getWidth();
    }

    public float getSliderHeight() {
        return mSliderIstHight;
    }

}
