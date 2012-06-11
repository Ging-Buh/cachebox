/* 
 * Copyright (C) 2011 team-cachebox.de
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

package CB_Core.GL_UI.Main.Actions.QuickButton;

import CB_Core.GlobalCore;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Controls.Button;
import CB_Core.GL_UI.Controls.Image;
import CB_Core.GL_UI.Controls.List.ListViewItemBase;
import CB_Core.GL_UI.Main.Actions.CB_Action;
import CB_Core.Math.CB_RectF;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Stellt ein Item der Quick Button List dar
 * 
 * @author Longri
 */
public class QuickButtonItem extends ListViewItemBase
{
	private final Color DISABLE_COLOR = new Color(0.2f, 0.2f, 0.2f, 0.2f);

	private CB_Action mAction;
	private Image mButtonIcon;
	private String mActionDesc;
	private Button mButton;

	/**
	 * Constructor
	 * 
	 * @param Action
	 *            Action Enum
	 * @param Icon
	 *            Action icon
	 * @param Desc
	 *            Action Beschreibung
	 */
	public QuickButtonItem(CB_RectF rec, int Index, CB_Action action, String Desc)
	{
		super(rec, Index, action.getName());

		mAction = action;
		mButtonIcon = new Image(rec.ScaleCenter(0.7f), "QuickListItemImage");
		mButtonIcon.setSprite(action.getIcon());
		mButtonIcon.setClickable(false);

		mActionDesc = Desc;

		mButton = new Button(rec, "QuickListItemButton");
		mButton.setButtonSprites(SpriteCache.QuickButton);
		mButton.setDrageble();
		this.addChild(mButton);
		this.addChild(mButtonIcon);

		mButton.setOnClickListener(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				mAction.CallExecute();
				return true;
			}
		});
	}

	/**
	 * Gibt die Beschreibung dieses Items wieder
	 * 
	 * @return String
	 */
	public String getDesc()
	{
		return mActionDesc;
	}

	@Override
	protected void Initial()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onTouchUp(int x, int y, int pointer, int button)
	{
		return mButton.onTouchUp(x, y, pointer, button);
	}

	@Override
	public boolean click(int x, int y, int pointer, int button)
	{
		return mButton.click(x, y, pointer, button);
	}

	private int autoResortState = -1;
	private int spoilerState = -1;
	private int hintState = -1;

	@Override
	protected void render(SpriteBatch batch)
	{
		super.render(batch);

		if (mAction.getName().equals("AutoResort"))
		{
			if (GlobalCore.autoResort && autoResortState != 1)
			{
				mButtonIcon.setSprite(SpriteCache.Icons.get(15));
				autoResortState = 1;
			}
			else if (!GlobalCore.autoResort && autoResortState != 0)
			{
				mButtonIcon.setSprite(SpriteCache.Icons.get(16));
				autoResortState = 0;
			}
		}
		else if (mAction.getName().toLowerCase().equals("spoiler"))
		{

			if (mAction.getEnabled() && spoilerState != 1)
			{
				mButtonIcon.setSprite(SpriteCache.Icons.get(18));
				spoilerState = 1;
			}
			else if (!mAction.getEnabled() && spoilerState != 0)
			{
				Sprite sprite = new Sprite(SpriteCache.Icons.get(18));
				sprite.setColor(DISABLE_COLOR);
				mButtonIcon.setSprite(sprite);
				spoilerState = 0;
			}
		}
		else if (mAction.getName().equals("hint"))
		{

			if (mAction.getEnabled() && hintState != 1)
			{
				mButtonIcon.setSprite(SpriteCache.Icons.get(19));
				hintState = 1;
			}
			else if (!mAction.getEnabled() && hintState != 0)
			{
				Sprite sprite = new Sprite(SpriteCache.Icons.get(19));
				sprite.setColor(DISABLE_COLOR);
				mButtonIcon.setSprite(sprite);
				hintState = 0;
			}
		}
	}

	@Override
	protected void SkinIsChanged()
	{
		// TODO Auto-generated method stub

	}
}
