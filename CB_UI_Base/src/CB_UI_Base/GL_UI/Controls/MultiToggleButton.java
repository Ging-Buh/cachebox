/* 
 * Copyright (C) 2011-2012 team-cachebox.de
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

import java.util.ArrayList;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;

import CB_UI_Base.GL_UI.COLOR;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.UI_Size_Base;
import CB_Utils.Util.HSV_Color;

public class MultiToggleButton extends Button {

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
		super(0, 0, UI_Size_Base.that.getButtonWidth(), UI_Size_Base.that.getButtonHeight(), Name);
	}

	private States aktState;
	private int StateId = 0;
	private Drawable led;
	private final ArrayList<States> State = new ArrayList<MultiToggleButton.States>();
	private OnStateChangeListener mOnStateChangeListener;

	/**
	 * wenn True wird der letzte State nur über ein LongClick angewählt
	 */
	private boolean lastStateWithLongClick = false;

	@Override
	public void setText(String Text) {
		super.setText(Text);

		if (lblTxt == null)
			return;

		// verschiebe Text nach oben, wegen Platz für LED
		CB_RectF r = this.ScaleCenter(0.9f);
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
			if (StateId == State.size() - 2) {
				StateId = 0;
			} else {
				StateId++;
			}
		} else {
			StateId++;
		}

		setState(StateId, true);

		return super.click(x, y, pointer, button);

	}

	@Override
	public boolean onTouchDown(int x, int y, int pointer, int button) {
		wasLongClicked = false;
		return super.onTouchDown(x, y, pointer, button);
	}

	boolean wasLongClicked = false;

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
			setState(State.size() - 1, true);
		} else {
			onLongClick(x, y, pointer, button);
		}
		return true;
	}

	public void addState(String Text, HSV_Color color) {
		State.add(new States(Text, color));
		setState(0, true);
	}

	public void setState(int ID) {
		setState(ID, false);
	}

	public void setState(int ID, boolean force) {
		if (StateId == ID && !force)
			return;

		StateId = ID;
		if (StateId > State.size() - 1)
			StateId = 0;
		aktState = State.get(StateId);
		this.setText(aktState.Text);
		led = null;

		if (mOnStateChangeListener != null)
			mOnStateChangeListener.onStateChange(this, StateId);

	}

	public void clearStates() {
		State.clear();
	}

	public class States {
		public String Text;
		public HSV_Color color;

		public States(String text, HSV_Color color) {
			Text = text;
			this.color = color;
		}
	}

	public int getState() {
		return StateId;
	}

	public static void initialOn_Off_ToggleStates(MultiToggleButton bt) {
		String ButtonTxt = "";
		bt.clearStates();
		bt.addState(ButtonTxt, new HSV_Color(Color.GRAY));
		bt.addState(ButtonTxt, COLOR.getHighLightFontColor());
		bt.setState(0, true);
	}

	public static void initialOn_Off_ToggleStates(MultiToggleButton bt, String txtOn, String txtOff) {
		bt.clearStates();
		bt.addState(txtOff, new HSV_Color(Color.GRAY));
		bt.addState(txtOn, COLOR.getHighLightFontColor());
		bt.setState(0, true);
	}

	public void setLastStateWithLongClick(boolean value) {
		lastStateWithLongClick = value;
		setState(0, true);
	}

	/**
	 * Interface definition for a callback to be invoked when a view is clicked.
	 */
	public interface OnStateChangeListener {
		/**
		 * Called when the state from ToggleButton changed.
		 * 
		 * @param v
		 *            The view that was state changed.
		 * @param State
		 *            The state to changed.
		 */
		void onStateChange(GL_View_Base v, int State);
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

			float A = 0, R = 0, G = 0, B = 0; // Farbwerte der batch um diese wieder einzustellen, wenn ein ColorFilter angewandt wurde!

			Color c = batch.getColor();
			A = c.a;
			R = c.r;
			G = c.g;
			B = c.b;

			GL.setBatchColor(aktState.color);

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
	protected void SkinIsChanged() {
		drawableNormal = null;

		drawablePressed = null;

		drawableDisabled = null;
		mFont = null;
		lblTxt = null;
		this.removeChilds();
		setState(getState(), true);

	}

}
