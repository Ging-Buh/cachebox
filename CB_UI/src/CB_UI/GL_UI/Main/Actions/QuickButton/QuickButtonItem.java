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

package CB_UI.GL_UI.Main.Actions.QuickButton;

import CB_UI.GlobalCore;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.SpriteCacheBase;
import CB_UI_Base.GL_UI.SpriteCacheBase.IconName;
import CB_UI_Base.GL_UI.Controls.Button;
import CB_UI_Base.GL_UI.Controls.Image;
import CB_UI_Base.GL_UI.Controls.List.ListViewItemBase;
import CB_UI_Base.GL_UI.Main.Actions.CB_Action;
import CB_UI_Base.GL_UI.Menu.MenuID;
import CB_UI_Base.Math.CB_RectF;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

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
	private QuickActions quickActionsEnum;

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
	public QuickButtonItem(CB_RectF rec, int Index, CB_Action action, String Desc, QuickActions type)
	{
		super(rec, Index, action.getName());
		quickActionsEnum = type;
		mAction = action;
		mButtonIcon = new Image(rec.ScaleCenter(0.7f), "QuickListItemImage");
		mButtonIcon.setDrawable(new SpriteDrawable(action.getIcon()));
		mButtonIcon.setClickable(false);

		mActionDesc = Desc;

		mButton = new Button(rec, "QuickListItemButton");
		mButton.setButtonSprites(SpriteCacheBase.QuickButton);
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
		if (childs.size() == 0)
		{
			this.addChild(mButton);
			this.addChild(mButtonIcon);
		}

		super.render(batch);

		if (mAction.getId() == MenuID.AID_AUTO_RESORT)
		{
			if (GlobalCore.getAutoResort() && autoResortState != 1)
			{
				mButtonIcon.setDrawable(new SpriteDrawable(SpriteCacheBase.Icons.get(IconName.autoSelectOn_15.ordinal())));
				autoResortState = 1;
			}
			else if (!GlobalCore.getAutoResort() && autoResortState != 0)
			{
				mButtonIcon.setDrawable(new SpriteDrawable(SpriteCacheBase.Icons.get(IconName.autoSelectOff_16.ordinal())));
				autoResortState = 0;
			}
		}
		else if (mAction.getId() == MenuID.AID_SHOW_SPOILER)
		{
			boolean hasSpoiler = false;
			if (GlobalCore.getSelectedCache() != null) hasSpoiler = GlobalCore.getSelectedCache().SpoilerExists();

			if (hasSpoiler && spoilerState != 1)
			{
				mButtonIcon.setDrawable(new SpriteDrawable(SpriteCacheBase.Icons.get(IconName.images_18.ordinal())));
				spoilerState = 1;
			}
			else if (!hasSpoiler && spoilerState != 0)
			{
				Sprite sprite = new Sprite(SpriteCacheBase.Icons.get(IconName.images_18.ordinal()));
				sprite.setColor(DISABLE_COLOR);
				mButtonIcon.setDrawable(new SpriteDrawable(sprite));
				spoilerState = 0;
			}
		}
		else if (mAction.getId() == MenuID.AID_SHOW_HINT)
		{

			if (mAction.getEnabled() && hintState != 1)
			{
				mButtonIcon.setDrawable(new SpriteDrawable(SpriteCacheBase.Icons.get(IconName.hint_19.ordinal())));
				hintState = 1;
			}
			else if (!mAction.getEnabled() && hintState != 0)
			{
				Sprite sprite = new Sprite(SpriteCacheBase.Icons.get(IconName.hint_19.ordinal()));
				sprite.setColor(DISABLE_COLOR);
				mButtonIcon.setDrawable(new SpriteDrawable(sprite));
				hintState = 0;
			}
		}
	}

	@Override
	protected void SkinIsChanged()
	{
	}

	public QuickActions getAction()
	{
		return quickActionsEnum;
	}
}
