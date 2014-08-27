/* 
 * Copyright (C) 2011-2014 team-cachebox.de
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
package CB_UI.GL_UI.Main.Actions;

import CB_UI_Base.Events.platformConector;
import CB_UI_Base.GL_UI.SpriteCacheBase;
import CB_UI_Base.GL_UI.SpriteCacheBase.IconName;
import CB_UI_Base.GL_UI.Main.Actions.CB_ActionCommand;
import CB_UI_Base.GL_UI.Menu.MenuID;

import com.badlogic.gdx.graphics.g2d.Sprite;

public class CB_Action_switch_Torch extends CB_ActionCommand
{

	public CB_Action_switch_Torch()
	{
		super("Torch", MenuID.AID_TORCH);
	}

	@Override
	public boolean getEnabled()
	{
		return platformConector.isTorchAvailable();
	}

	@Override
	public Sprite getIcon()
	{
		if (platformConector.isTorchOn())
		{
			return SpriteCacheBase.Icons.get(IconName.torch_on_67.ordinal());
		}
		else
		{
			return SpriteCacheBase.Icons.get(IconName.torch_Off_68.ordinal());
		}
	}

	@Override
	public void Execute()
	{
		platformConector.switchTorch();
	}
}
