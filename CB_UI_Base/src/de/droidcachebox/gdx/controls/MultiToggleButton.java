/*
 * Copyright (C) 2011-2022 team-cachebox.de
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
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;

import java.util.ArrayList;

import de.droidcachebox.gdx.COLOR;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.GL_View_Base;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.graphics.HSV_Color;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.UiSizes;

public class MultiToggleButton extends CB_Button {

    private final ArrayList<State> states = new ArrayList<>();
    private boolean wasLongClicked = false;
    private State aktState;
    private int stateId = 0;
    private Drawable led;
    private OnStateChangeListener mOnStateChangeListener;
    /**
     * wenn True wird der letzte State nur über ein LongClick angewählt
     */
    private boolean lastStateWithLongClick = false;

    public MultiToggleButton(float X, float Y, float Width, float Height, String Name) {
        super(X, Y, Width, Height, Name);
        setClickable(true);
        setLongClickable(true);
    }

    public MultiToggleButton(CB_RectF rec, String Name) {
        super(rec, Name);
        setClickable(true);
        setLongClickable(true);
    }

    public MultiToggleButton(String Name) {
        super(0, 0, UiSizes.getInstance().getButtonHeight(), UiSizes.getInstance().getButtonHeight(), Name);
    }

    @Override
    public void setText(String Text) {
        super.setText(Text);

        if (lblTxt == null)
            return;

        // verschiebe Text nach oben, wegen Platz für LED
        CB_RectF r = scaleCenter(0.9f);
        float l = (r.getHeight() / 2);
        lblTxt.setY(l);
        lblTxt.setHeight(l);
    }

    @Override
    public boolean click(int x, int y, int pointer, int button) {
        // wenn Button disabled ein Behandelt zurück schicken,
        // damit keine weiteren Abfragen durchgereicht werden.
        // Auch wenn dieser Button ein OnClickListener hat.
        if (isDisabled || wasLongClicked) {
            return true;
        }
        if (lastStateWithLongClick) {
            if (stateId == states.size() - 2) {
                stateId = 0;
            } else {
                stateId++;
            }
        } else {
            stateId++;
        }
        setState(stateId, true);
        return super.click(x, y, pointer, button);

    }

    @Override
    public boolean onTouchDown(int x, int y, int pointer, int button) {
        wasLongClicked = false;
        return super.onTouchDown(x, y, pointer, button);
    }

    @Override
    public boolean longClick(int x, int y, int pointer, int button) {
        wasLongClicked = true;
        // wenn Button disabled ein Behandelt zurück schicken,
        // damit keine weiteren Abfragen durchgereicht werden.
        // Auch wenn dieser Button ein OnClickListener hat.
        if (isDisabled) {
            return true;
        }

        if (lastStateWithLongClick) {
            setState(states.size() - 1, true);
        } else {
            onLongClick(x, y, pointer, button);
        }
        return true;
    }

    public void addState(String Text, HSV_Color color) {
        states.add(new State(Text, color));
        setState(0, true);
    }

    private void setState(int newStateId, boolean force) {
        if (stateId == newStateId && !force)
            return;

        stateId = newStateId;
        if (newStateId > states.size() - 1)
            stateId = 0;

        aktState = states.get(stateId);
        setText(aktState.Text);
        led = null;

        if (mOnStateChangeListener != null)
            mOnStateChangeListener.onStateChange(this, stateId);

    }

    private void clearStates() {
        states.clear();
    }

    public int getState() {
        return stateId;
    }

    public void setState(int ID) {
        setState(ID, false);
    }

    public void initialOn_Off_ToggleStates(String txtOn, String txtOff) {
        clearStates();
        addState(txtOff, new HSV_Color(Color.GRAY));
        addState(txtOn, COLOR.getHighLightFontColor());
        setState(0, true);
    }

    public void setLastStateWithLongClick(boolean value) {
        lastStateWithLongClick = value;
        setState(0, true);
    }

    @Override
    protected void render(Batch batch) {
        super.render(batch); // draw Button with Txt

        // Draw LED
        if (aktState != null) {
            if (led == null) {
                Sprite sprite = Sprites.ToggleBtn.get(2);
                int patch = (int) ((sprite.getWidth() / 2) - 5);
                led = new NinePatchDrawable(new NinePatch(sprite, patch, patch, 1, 1));
            }

            float A, R, G, B; // Farbwerte der batch um diese wieder einzustellen, wenn ein ColorFilter angewandt wurde!

            Color c = batch.getColor();
            A = c.a;
            R = c.r;
            G = c.g;
            B = c.b;

            GL.that.setBatchColor(aktState.color);

            if (led != null)
                led.draw(batch, 0, 0, getWidth(), getHeight());

            batch.setColor(R, G, B, A);
        }

    }

    public void setOnStateChangedListener(OnStateChangeListener l) {
        setClickable(true);
        mOnStateChangeListener = l;
    }

    @Override
    protected void skinIsChanged() {
        drawableNormal = null;
        drawablePressed = null;
        drawableDisabled = null;
        mFont = null;
        lblTxt = null;
        removeChildren();
        setState(getState(), true);
    }

    /**
     * Interface definition for a callback to be invoked when a view is clicked.
     */
    public interface OnStateChangeListener {
        /**
         * Called when the state from ToggleButton changed.
         *
         * @param v     The view that was state changed.
         * @param state The state to changed.
         */
        void onStateChange(GL_View_Base v, int state);
    }

    public static class State {
        public String Text;
        public HSV_Color color;

        State(String text, HSV_Color newColor) {
            Text = text;
            color = newColor;
        }
    }

}
