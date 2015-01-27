/* 
 * Copyright (C) 2015 team-cachebox.de
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

import CB_UI_Base.GL_UI.SpriteCacheBase;
import CB_UI_Base.GL_UI.Main.Actions.CB_ActionCommand;
import CB_UI_Base.GL_UI.Menu.MenuID;

import com.badlogic.gdx.graphics.g2d.Sprite;

public class CB_Action_switch_Description extends CB_ActionCommand {

    public static boolean SHOW_ANDROID = true;

    public static switchEventListner event;

    public interface switchEventListner {
	public void descSwitch();
    }

    public CB_Action_switch_Description() {
	super("switchDesc", MenuID.AID_SWITCH_DESCRIPTION);
    }

    @Override
    public boolean getEnabled() {
	return true;
    }

    @Override
    public Sprite getIcon() {
	return SpriteCacheBase.getThemedSprite("tf-paste");
    }

    @Override
    public void Execute() {
	SHOW_ANDROID = !SHOW_ANDROID;
	if (event != null)
	    event.descSwitch();
    }
}
