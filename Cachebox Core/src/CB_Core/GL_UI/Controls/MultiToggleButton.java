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

package CB_Core.GL_UI.Controls;

import java.util.ArrayList;

import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.Math.CB_RectF;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class MultiToggleButton extends Button
{

	public MultiToggleButton(float X, float Y, float Width, float Height, GL_View_Base Parent, String Name)
	{
		super(X, Y, Width, Height, Parent, Name);
		setClickable(true);

	}

	private States aktState;
	private int StateId = 0;
	private Sprite led;
	private ArrayList<States> State = new ArrayList<MultiToggleButton.States>();
	private OnStateChangeListener mOnStateChangeListener;

	/**
	 * wenn True wird der letzte State nur über ein LongClick angewählt
	 */
	private boolean lastStateWithLongClick = false;

	@Override
	public void setText(String Text)
	{
		super.setText(Text);

		if (lblTxt == null) return;

		// verschiebe Text nach oben, wegen Platz für LED
		CB_RectF r = this.ScaleCenter(0.9f);
		float l = (float) ((this.width - r.getWidth()) / 1.8);
		lblTxt.setY(l);
	}

	@Override
	public boolean click(int x, int y, int pointer, int button)
	{
		if (lastStateWithLongClick)
		{
			if (StateId == State.size() - 2)
			{
				StateId = 0;
			}
			else
			{
				StateId++;
			}
		}
		else
		{
			StateId++;
		}

		setState(StateId, true);

		return true;
	}

	@Override
	public boolean onLongClick(int x, int y, int pointer, int button)
	{
		if (lastStateWithLongClick)
		{
			setState(State.size() - 1, true);
		}
		else
		{
			click(x, y, pointer, button);
		}
		return true;
	}

	public void addState(String Text, Color color)
	{
		State.add(new States(Text, color));
	}

	public void setState(int ID)
	{
		setState(ID, false);
	}

	public void setState(int ID, boolean force)
	{
		if (StateId == ID && !force) return;

		StateId = ID;
		if (StateId > State.size() - 1) StateId = 0;
		aktState = State.get(StateId);
		this.setText(aktState.Text);
		led = null;
		System.gc();

		if (mOnStateChangeListener != null) mOnStateChangeListener.onStateChange(this, StateId);

	}

	public void clearStates()
	{
		State.clear();
	}

	public class States
	{
		public String Text;
		public Color color;

		public States(String text, Color color)
		{
			Text = text;
			this.color = color;
		}
	}

	public int getState()
	{
		return StateId;
	}

	public static void initialOn_Off_ToggleStates(MultiToggleButton bt)
	{
		String ButtonTxt = "";
		bt.clearStates();
		bt.addState(ButtonTxt, Color.GRAY);
		bt.addState(ButtonTxt, Color.GREEN);
		bt.setState(0, true);
	}

	public static void initialOn_Off_ToggleStates(MultiToggleButton bt, String txtOn, String txtOff)
	{
		bt.clearStates();
		bt.addState(txtOff, Color.GRAY);
		bt.addState(txtOn, Color.GREEN);
		bt.setState(0, true);
	}

	public void setLastStateWithLongClick(boolean value)
	{
		lastStateWithLongClick = value;
		setState(0, true);
	}

	/**
	 * Interface definition for a callback to be invoked when a view is clicked.
	 */
	public interface OnStateChangeListener
	{
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
	protected void render(SpriteBatch batch)
	{
		super.render(batch); // draw Button with Txt

		// Draw LED
		led = SpriteCache.ToggleBtn.get(2);
		led.setBounds(Pos.x, Pos.y, width, height);
		led.draw(batch);

	}

	public void setOnStateChangedListner(OnStateChangeListener l)
	{
		setClickable(true);
		mOnStateChangeListener = l;
	}
}
