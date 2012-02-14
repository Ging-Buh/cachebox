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

import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Controls.Label.VAlignment;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.UiSizes;

import com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public class Button extends GL_View_Base
{

	protected static NinePatch ninePatch;
	protected static NinePatch ninePatchPressed;
	protected static NinePatch ninePatchDisabled;

	private boolean isPressed = false;
	private boolean isDisabled = false;
	private String text;
	private Label lblTxt;

	public Button(float X, float Y, float Width, float Height)
	{
		super(X, Y, Width, Height);

	}

	@Override
	protected void render(SpriteBatch batch)
	{
		if (ninePatch == null)
		{
			ninePatch = new NinePatch(SpriteCache.uiAtlas.findRegion("day_btn_default_normal"), 8, 8, 8, 8);
			ninePatchPressed = new NinePatch(SpriteCache.uiAtlas.findRegion("day_btn_default_normal_pressed"), 8, 8, 8, 8);
			ninePatchDisabled = new NinePatch(SpriteCache.uiAtlas.findRegion("day_btn_default_normal_disabled"), 8, 8, 8, 8);
		}

		if (!isPressed && !isDisabled)
		{
			if (ninePatch != null)
			{
				ninePatch.draw(batch, 0, 0, width, height);
			}
		}
		else if (isPressed)
		{
			if (ninePatchPressed != null)
			{
				ninePatchPressed.draw(batch, 0, 0, width, height);
			}
		}
		else
		{
			if (ninePatchDisabled != null)
			{
				ninePatchDisabled.draw(batch, 0, 0, width, height);
			}
		}

	}

	@Override
	public void onRezised(CB_RectF rec)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onTouchDown(int x, int y, int pointer, int button)
	{
		if (!isDisabled)
		{
			isPressed = true;
		}
		return true;
	}

	@Override
	public boolean onTouchDragged(int x, int y, int pointer)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onTouchUp(int x, int y, int pointer, int button)
	{

		isPressed = false;
		return true;
	}

	@Override
	public void dispose()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onLongClick(int x, int y, int pointer, int button)
	{
		// TODO Auto-generated method stub
		return false;
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
		// Auch wenn dieser Button ein OnClickLÖistner hat.
		if (isDisabled)
		{
			return true;
		}

		else
			return super.click(x, y, pointer, button);
	}

	public void setText(String Text)
	{
		if (Text == null || Text.equals(""))
		{
			text = "";
			if (lblTxt != null)
			{

				this.removeChild(lblTxt);
				lblTxt.dispose();
			}

			lblTxt = null;
		}

		text = Text;
		if (lblTxt == null)
		{

			CB_RectF r = this.ScaleCenter(0.9f);

			float l = (this.width - r.getWidth()) / 2;
			float b = (this.height - r.getHeight()) / 2;

			r.setPos(new Vector2(l, b));

			lblTxt = new Label(r);
			lblTxt.setFont(UiSizes.GL.fontAB18);
			lblTxt.setHAlignment(HAlignment.CENTER);
			lblTxt.setVAlignment(VAlignment.CENTER);
			this.addChild(lblTxt);
		}

		lblTxt.setText(Text);
	}

}
