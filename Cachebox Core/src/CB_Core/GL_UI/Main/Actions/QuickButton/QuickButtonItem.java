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

import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Controls.Button;
import CB_Core.GL_UI.Controls.Image;
import CB_Core.GL_UI.Controls.List.ListViewItemBase;
import CB_Core.GL_UI.Main.Actions.CB_Action;
import CB_Core.Math.CB_RectF;

/**
 * Stellt ein Item der Quick Button List dar
 * 
 * @author Longri
 */
public class QuickButtonItem extends ListViewItemBase
{
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

}
