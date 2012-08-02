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

import CB_Core.GL_UI.ButtonSprites;
import CB_Core.GL_UI.CB_View_Base;
import CB_Core.GL_UI.Fonts;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Controls.Label.VAlignment;
import CB_Core.GL_UI.GL_Listener.GL_Listener;
import CB_Core.Math.CB_RectF;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public class Button extends CB_View_Base
{
	protected BitmapFont mFont;

	protected NinePatch mNinePatch;
	protected NinePatch mNinePatchPressed;
	protected NinePatch mNinePatchDisabled;

	protected boolean isPressed = false;
	protected boolean isDisabled = false;
	protected Label lblTxt;
	protected boolean dragableButton = false;

	public Button(float X, float Y, float Width, float Height, String Name)
	{
		super(X, Y, Width, Height, Name);
		this.isClickable = true;
	}

	public Button(CB_RectF rec, GL_View_Base parent, String name)
	{
		super(rec, parent, name);
		this.isClickable = true;
	}

	public Button(CB_RectF rec, String name)
	{
		super(rec, name);
		this.isClickable = true;
	}

	public Button(CB_RectF rec, String name, ButtonSprites sprites)
	{
		super(rec, name);
		setButtonSprites(sprites);
		this.isClickable = true;
	}

	public void setninePatch(NinePatch ninePatch)
	{
		mNinePatch = ninePatch;
	}

	public void setninePatchPressed(NinePatch ninePatch)
	{
		mNinePatchPressed = ninePatch;
	}

	public void setninePatchDisabled(NinePatch ninePatch)
	{
		mNinePatchDisabled = ninePatch;
	}

	public void setButtonSprites(ButtonSprites sprites)
	{
		if (sprites != null)
		{
			mNinePatch = sprites.getNormal();
			mNinePatchPressed = sprites.getPressed();
			mNinePatchDisabled = sprites.getDisabled();
		}
	}

	public void setFont(BitmapFont font)
	{
		mFont = font;
	}

	@Override
	protected void render(SpriteBatch batch)
	{

		if (!isPressed && !isDisabled)
		{
			if (mNinePatch != null)
			{
				mNinePatch.draw(batch, 0, 0, width, height);
			}
			else
			{
				Initial();
				GL_Listener.glListener.renderOnce(this.getName() + " render");
			}
		}
		else if (isPressed)
		{
			if (mNinePatchPressed != null)
			{
				mNinePatchPressed.draw(batch, 0, 0, width, height);
			}
		}
		else
		{
			if (mNinePatchDisabled != null)
			{
				mNinePatchDisabled.draw(batch, 0, 0, width, height);
			}
		}

	}

	@Override
	public boolean onTouchDown(int x, int y, int pointer, int button)
	{
		if (!isDisabled)
		{
			isPressed = true;
			GL_Listener.glListener.renderOnce(this.getName() + " touchDown");
		}
		return dragableButton ? false : true;
	}

	@Override
	public boolean onTouchDragged(int x, int y, int pointer, boolean KineticPan)
	{
		isPressed = false;
		GL_Listener.glListener.renderOnce(this.getName() + " Dragged");
		return false;
	}

	@Override
	public boolean onTouchUp(int x, int y, int pointer, int button)
	{

		isPressed = false;
		GL_Listener.glListener.renderOnce(this.getName() + " touchUp");
		return dragableButton ? false : true;
	}

	public void enable()
	{
		isDisabled = false;
	}

	public void disable()
	{
		isDisabled = true;
	}

	public boolean isDisabled()
	{
		return isDisabled;
	}

	@Override
	public boolean click(int x, int y, int pointer, int button)
	{
		// wenn Button disabled ein Behandelt zurück schicken,
		// damit keine weiteren Abfragen durchgereicht werden.
		// Auch wenn dieser Button ein OnClickListner hat.
		if (isDisabled)
		{
			return true;
		}

		else

			// Logger.LogCat("Button " + this.name + " Clicked");
			return super.click(x, y, pointer, button);
	}

	public void setText(String Text)
	{
		if (Text == null || Text.equals(""))
		{
			if (lblTxt != null)
			{

				this.removeChild(lblTxt);
				lblTxt.dispose();
			}

			lblTxt = null;
			GL_Listener.glListener.renderOnce(this.getName() + " setText");
			return;
		}

		if (lblTxt == null)
		{

			CB_RectF r = this.ScaleCenter(0.9f);

			float l = (this.width - r.getWidth()) / 2;
			float b = (this.height - r.getHeight()) / 2;

			r.setPos(new Vector2(l, b));

			lblTxt = new Label(r, this, name + "Label");
			if (mFont != null)
			{
				lblTxt.setFont(mFont);
			}
			else
			{
				lblTxt.setFont(Fonts.getNormal());
			}

			lblTxt.setHAlignment(HAlignment.CENTER);
			lblTxt.setVAlignment(VAlignment.CENTER);
			this.addChild(lblTxt);
		}

		lblTxt.setText(Text);
		GL_Listener.glListener.renderOnce(this.getName() + " setText2");
	}

	@Override
	protected void Initial()
	{
		if (mNinePatch == null)
		{
			mNinePatch = new NinePatch(SpriteCache.getThemedSprite("btn-normal"), 16, 16, 16, 16);
		}
		if (mNinePatchPressed == null)
		{
			mNinePatchPressed = new NinePatch(SpriteCache.getThemedSprite("btn-pressed"), 16, 16, 16, 16);
		}
		if (mNinePatchDisabled == null)
		{
			mNinePatchDisabled = new NinePatch(SpriteCache.getThemedSprite("btn-disabled"), 8, 8, 8, 8);
		}

	}

	public void setDrageble()
	{
		setDrageble(true);
	}

	public void setDrageble(boolean value)
	{
		dragableButton = value;
	}

	@Override
	protected void SkinIsChanged()
	{
		mNinePatch = null;

		mNinePatchPressed = null;

		mNinePatchDisabled = null;
		mFont = null;
		lblTxt = null;
		this.removeChilds();
	}

	public String getText()
	{
		if (lblTxt != null) return lblTxt.getText();
		return null;
	}

	public void performClick()
	{
		click(0, 0, 0, 0);

	}

}
